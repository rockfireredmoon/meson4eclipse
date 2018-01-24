/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *      Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
/*
 */
package org.icemoon.cdt.ninja.ui.internal.editor;

import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 */
public interface INinjaDocumentProvider extends IDocumentProvider {
	/**
	 * Shuts down this provider.
	 */
	void shutdown();

	/**
	 * Returns the working copy for the given element.
	 *
	 * @param element
	 *            the element
	 * @return the working copy for the given element
	 */
	IMakefile getWorkingCopy(Object element);
}
