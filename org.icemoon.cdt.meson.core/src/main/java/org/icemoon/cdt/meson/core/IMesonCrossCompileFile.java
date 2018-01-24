/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Emerald Icemoon - Ported to Meson
 *******************************************************************************/
package org.icemoon.cdt.meson.core;

import java.nio.file.Path;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.core.runtime.CoreException;

/**
 * A toolchain file.
 *
 * @noimplement
 * @noextend
 * @author Emerald Icemoon
 */
public interface IMesonCrossCompileFile {

	Path getPath();

	String getProperty(String key);

	void setProperty(String key, String value);

	IToolChain getToolChain() throws CoreException;

}
