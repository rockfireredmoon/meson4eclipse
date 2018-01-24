/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.core.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.core.makefile.IMakefileReaderProvider;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.icemoon.cdt.ninja.core.ISubninja;
import org.icemoon.cdt.ninja.core.NinjaFile;
import org.icemoon.cdt.ninja.core.NinjaFileConstants;

public class Subninja extends Parent implements ISubninja {

	String filename;

	public Subninja(Directive parent, String filename) {
		super(parent);
		this.filename = filename;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(NinjaFileConstants.DIRECTIVE_SUBNINJA);
		if (filename != null)
			sb.append(' ').append(filename);
		return sb.toString();
	}

	@Override
	public String getFilename() {
		return filename;
	}

	private IMakefileReaderProvider getCurrentMakefileReaderProvider() {
		IDirective directive = this;
		while (directive != null) {
			if (directive instanceof IMakefile) {
				IMakefileReaderProvider makefileReaderProvider = ((IMakefile) directive).getMakefileReaderProvider();
				if (makefileReaderProvider != null)
					return makefileReaderProvider;
			}
			directive = directive.getParent();
		}
		return null;
	}

	@Override
	public IDirective[] getDirectives() {
		clearDirectives();
		if (filename != null) {
			URI uri = getMakefile().getFileURI();
			IMakefileReaderProvider makefileReaderProvider = getCurrentMakefileReaderProvider();
			IPath includeFilePath = new Path(filename);
			final IPath path = URIUtil.toPath(uri);
			if (path != null) {
				if (includeFilePath.isAbsolute()) {
					// Try to set the device to that of the parent makefile.
					String device = path.getDevice();
					if (device != null && includeFilePath.getDevice() == null) {
						includeFilePath = includeFilePath.setDevice(device);
					}
					try {
						URI includeURI = URIUtil.toURI(includeFilePath);
						if (!isAlreadyIncluded(includeURI)) {
							NinjaFile gnu = new NinjaFile();
							gnu.parse(includeURI, makefileReaderProvider);
							addDirective(gnu);
						}
					} catch (IOException e) {
					}
				} else {
					try {
						IPath parent = path.removeLastSegments(1);
						IPath testIncludeFilePath = parent.append(includeFilePath);
						String uriPath = testIncludeFilePath.toString();
						if (testIncludeFilePath.getDevice() != null) {
							// special case: device prefix is seen as relative path by URI
							uriPath = '/' + uriPath;
						}
						URI includeURI = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
								uriPath, null, null);
						if (!isAlreadyIncluded(includeURI)) {
							NinjaFile gnu = new NinjaFile();
							gnu.parse(includeURI, makefileReaderProvider);
							addDirective(gnu);
						}
					} catch (IOException e) {
					} catch (URISyntaxException exc) {
					}
				}
			}
		}
		return super.getDirectives();
	}

	private boolean isAlreadyIncluded(URI includeURI) {
		for (IDirective parent = getParent(); parent != null; parent = parent.getParent()) {
			if (parent instanceof IMakefile) {
				if (includeURI.equals(((IMakefile) parent).getFileURI())) {
					return true;
				}
			}
		}
		return false;
	}
}
