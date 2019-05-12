/*******************************************************************************
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Emerald Icemoon - Initial implementation
 *******************************************************************************/

package org.icemoon.cdt.ninja.core;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.makefile.IAutomaticVariable;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IMakefileReaderProvider;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.icemoon.cdt.ninja.core.internal.BadDirective;
import org.icemoon.cdt.ninja.core.internal.Build;
import org.icemoon.cdt.ninja.core.internal.Comment;
import org.icemoon.cdt.ninja.core.internal.DefineVariable;
import org.icemoon.cdt.ninja.core.internal.Directive;
import org.icemoon.cdt.ninja.core.internal.EmptyLine;
import org.icemoon.cdt.ninja.core.internal.Include;
import org.icemoon.cdt.ninja.core.internal.NinjaFileReader;
import org.icemoon.cdt.ninja.core.internal.OverrideVariable;
import org.icemoon.cdt.ninja.core.internal.Rule;
import org.icemoon.cdt.ninja.core.internal.Subninja;
import org.icemoon.cdt.ninja.core.internal.Target;
import org.icemoon.cdt.ninja.core.internal.TargetVariable;
import org.icemoon.cdt.ninja.core.internal.Util;
import org.icemoon.cdt.ninja.core.internal.VariableDefinition;

/**
 * @author Emerald Icemoon
 */
public class NinjaFile extends AbstractMakefile {

	private IMakefileReaderProvider makefileReaderProvider;

	public NinjaFile() {
		super(null);
	}

	@Override
	public IMakefileReaderProvider getMakefileReaderProvider() {
		return makefileReaderProvider;
	}

	@Override
	public void parse(String filePath, Reader reader) throws IOException {
		parse(URIUtil.toURI(filePath), new NinjaFileReader(reader));
	}

	@Override
	public void parse(URI fileURI, IMakefileReaderProvider makefileReaderProvider) throws IOException {
		this.makefileReaderProvider = makefileReaderProvider;
		NinjaFileReader reader;
		if (makefileReaderProvider == null) {
			try {
				final IFileStore store = EFS.getStore(fileURI);
				final IFileInfo info = store.fetchInfo();
				if (!info.exists() || info.isDirectory()) {
					throw new IOException();
				}

				reader = new NinjaFileReader(new InputStreamReader(store.openInputStream(EFS.NONE, null)));
			} catch (CoreException e) {
				MakeCorePlugin.log(e);
				throw new IOException(e.getMessage());
			}
		} else {
			reader = new NinjaFileReader(makefileReaderProvider.getReader(fileURI));
		}
		parse(fileURI, reader);
	}

	@Override
	public void parse(URI filePath, Reader reader) throws IOException {
		parse(filePath, new NinjaFileReader(reader));
	}

	public IRuleDef[] getRuleDefs() {
		IDirective[] stmts = getDirectives(true);
		List<IDirective> array = new ArrayList<IDirective>(stmts.length);
		for (IDirective stmt : stmts) {
			if (stmt instanceof IRuleDef) {
				array.add(stmt);
			}
		}
		return array.toArray(new IRuleDef[0]);
	}

	protected void parse(URI fileURI, NinjaFileReader reader) throws IOException {
		String line;
		Build[] builds = null;
		Rule rule = null;
		int startLine = 0;
		int endLine = 0;
		clearDirectives();
		setFileURI(fileURI);

		try {
			while ((line = reader.readLine()) != null) {
				startLine = endLine + 1;
				endLine = reader.getLineNumber();

				if (NinjaFileUtil.isVariableDef(line)) {
					VariableDefinition def = parseVariableDefinition(line);
					def.setLines(startLine, endLine);
					if (rule != null) {
						rule.addDirective(def);
						rule.setEndLine(endLine);
						continue;
					} else if (builds != null) {
						for (Build build : builds) {
							build.addDirective(def);
							build.setEndLine(endLine);
						}
						continue;
					} else {
						addDirective(def);
					}
					continue;
				}

				int pound = Util.indexOfComment(line);
				if (pound != -1) {
					Comment cmt = new Comment(this, line.substring(pound + 1));
					cmt.setLines(startLine, endLine);
					if (builds != null) {
						for (Build build : builds) {
							build.addDirective(cmt);
							build.setEndLine(endLine);
						}
					} else {
						addDirective(cmt);
					}
					line = line.substring(0, pound);
					if (Util.isEmptyLine(line)) {
						continue;
					}
				}

				if (Util.isEmptyLine(line)) {
					Directive empty = new EmptyLine(this);
					empty.setLines(startLine, endLine);
					if (builds != null) {
						for (Build build : builds) {
							build.addDirective(empty);
							build.setEndLine(endLine);
						}
					} else {
						addDirective(empty);
					}
					continue;
				}

				builds = null;
				rule = null;

				if (NinjaFileUtil.isRuleDefinition(line)) {
					rule = parseRuleDefinition(line);
					if (rule != null) {
						addDirective(rule);
					}
					continue;
				}

				if (NinjaFileUtil.isIncluded(line)) {
					Directive directive = parseIncluded(line);
					if (directive != null) {
						directive.setLines(startLine, endLine);
						addDirective(directive);
						continue;
					}
				}

				Build build = parseBuildRule(line);
				if (build != null) {
					builds = new Build[] { build };
					build.setLines(startLine, endLine);
					addDirective(build);
					continue;
				}

				if (NinjaFileUtil.isVariableDefinition(line)) {
					VariableDefinition vd = parseVariableDefinition(line);
					vd.setLines(startLine, endLine);
					addDirective(vd);
					if (!vd.isTargetSpecific()) {
						continue;
					}
				}

				BadDirective stmt = new BadDirective(this, line);
				stmt.setLines(startLine, endLine);
				addDirective(stmt);

			}
			setLines(1, endLine);
		} finally {
			reader.close();
		}
	}

	protected Build parseBuildRule(String line) {
		if (!NinjaFileUtil.isBuildRule(line)) {
			return null;
		}
		line = line.trim();
		int index = Util.indexOf(line, ' ');
		line = line.substring(index + 1);
		String keyword = null;
		String[] reqs = null;
		index = Util.indexOf(line, ':');
		if (index != -1) {
			keyword = line.substring(0, index).trim();
			String req = line.substring(index + 1);
			reqs = NinjaFileUtil.findPrerequisites(req);
		} else {
			keyword = line;
			reqs = new String[0];
		}
		return new Build(this, new Target(keyword), reqs);
	}

	/**
	 * Format of the include directive: include filename1 filename2 ...
	 */
	protected Directive parseIncluded(String line) {
		// TODO Don't think ninja supports mulitple filenames
		String filename = null;
		StringTokenizer st = new StringTokenizer(line);
		int count = st.countTokens();
		String token = null;
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				if (i == 0) {
					token = st.nextToken();
					continue;
				}
				filename = expandString(st.nextToken(), true);
				break;
			}
		} else {
			token = line;
			filename = null;
			;
		}
		if (token.equals(NinjaFileConstants.DIRECTIVE_INCLUDE))
			return new Include(this, filename);
		else
			return new Subninja(this, filename);
	}

	protected Rule parseRuleDefinition(String line) {
		for (int i = 0; i < line.length(); i++) {
			if (Util.isSpace(line.charAt(i))) {
				line = line.substring(i).trim();
				break;
			}
		}
		return new Rule(this, line);
	}

	protected VariableDefinition parseVariableDefinition(String line) {
		line = line.trim();
		VariableDefinition vd;

		// the default type.
		int type = VariableDefinition.TYPE_RECURSIVE_EXPAND;
		boolean isDefine = false;
		boolean isOverride = false;
		boolean isTargetVariable = false;
		String targetName = ""; //$NON-NLS-1$

		String name;
		StringBuffer value = new StringBuffer();

		// Check for Target: Variable-assignment
		isTargetVariable = NinjaFileUtil.isTargetVariable(line);
		if (isTargetVariable) {
			// move to the first ':'
			int colon = Util.indexOf(line, ':');
			if (colon != -1) {
				targetName = line.substring(0, colon).trim();
				line = line.substring(colon + 1).trim();
			} else {
				targetName = ""; //$NON-NLS-1$
			}
		}

		// Check for "define"
		if (NinjaFileUtil.isVariableDef(line)) {
			isDefine = true;
		}

		int index = line.indexOf('=');
		if (index != -1) {
			int separator = index;
			// Check for "+=", ":=", "?="
			if (index > 0) {
				type = line.charAt(index - 1);
				if (type == VariableDefinition.TYPE_SIMPLE_EXPAND || type == VariableDefinition.TYPE_APPEND
						|| type == VariableDefinition.TYPE_CONDITIONAL) {
					separator = index - 1;
				} else {
					type = VariableDefinition.TYPE_RECURSIVE_EXPAND;
				}
			}
			name = line.substring(0, separator).trim();
			value.append(line.substring(index + 1).trim());
		} else {
			name = line;
		}

		if (isTargetVariable) {
			vd = new TargetVariable(this, targetName, name, value, isOverride, type);
		} else if (isDefine) {
			vd = new DefineVariable(this, name, value);
		} else if (isOverride) {
			vd = new OverrideVariable(this, name, value, type);
		} else {
			vd = new VariableDefinition(this, name, value, type);
		}
		return vd;
	}

	@Override
	public IAutomaticVariable[] getAutomaticVariables() {
		return null;
	}

	@Override
	public IDirective[] getBuiltins() {
		return null;
	}
}
