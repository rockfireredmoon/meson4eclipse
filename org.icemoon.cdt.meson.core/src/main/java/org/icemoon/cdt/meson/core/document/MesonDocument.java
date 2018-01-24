/*******************************************************************************
 * Copyright (c) 2014-2017 Martin Weber.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Emerald Icemoon - Initial implementation
 *******************************************************************************/
package org.icemoon.cdt.meson.core.document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator2;


/**
 * @author Emerald Icemoon
 */
public class MesonDocument {

	public interface MesonElement {
		StringBuffer append(StringBuffer buffer);
	}

	public class Executable implements MesonElement {

		private String target;
		private List<String> sources = new ArrayList<String>();

		public String getTarget() {
			return target;
		}

		public void setTarget(String target) {
			this.target = target;
		}

		public List<String> getSources() {
			return sources;
		}

		public StringBuffer append(StringBuffer buffer) {
			return buffer.append("project(").append(formatString(target)).append(",").append(formatArray(sources))
					.append(")");
		}
	}

	public class Project implements MesonElement {
		private String name;
		private List<String> languages = new ArrayList<String>();

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<String> getLanguages() {
			return languages;
		}

		public StringBuffer append(StringBuffer buffer) {
			return buffer.append("project(").append(formatString(name)).append(",").append(formatArray(languages))
					.append(")");
		}

	}

	private Map<String, Executable> executables = new LinkedHashMap<String, MesonDocument.Executable>();
	private Project project = new Project();

	public Project getProject() {
		return project;
	}

	public Map<String, Executable> getExecutables() {
		return executables;
	}

	public StringBuffer append(StringBuffer buffer) {
		project.append(buffer).append(IManagedBuilderMakefileGenerator2.NEWLINE);
		for (Map.Entry<String, Executable> en : executables.entrySet()) {
			en.getValue().append(buffer).append(IManagedBuilderMakefileGenerator2.NEWLINE);
		}
		return buffer;
	}

	static public String escapeSpecial(String string) {
		return string.replace("\\", "\\\\").replace("'", "\\'").replace("\r", "\\r").replace("\t", "\\t");
	}

	static public String escapeString(String string) {
		return escapeSpecial(string).replace("\n", "\\n");
	}

	static public String formatArray(List<String> elements) {
		return formatArray(elements.toArray(new String[0]));
	}

	static public String formatArray(String... elements) {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (int i = 0; i < elements.length; i++) {
			if (i > 0)
				buf.append(", ");
			buf.append(formatString(elements[i]));
		}
		buf.append("]");
		return buf.toString();
	}

	static public String formatString(String string) {
		if (string == null)
			return "";
		if (string.contains("\n")) {
			return "'''" + escapeSpecial(string) + "'''";
		} else
			return "'" + escapeString(string) + "'";
	}
}
