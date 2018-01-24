/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
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
import java.util.Collection;
import java.util.Map;

import org.eclipse.cdt.core.build.IToolChain;

/**
 * Manages toolchain files for Meson.
 * 
 * @noimplement
 * @noextend
 */
public interface IMesonCrossCompileManager {

	IMesonCrossCompileFile newToolChainFile(Path path);

	void addToolChainFile(IMesonCrossCompileFile file);

	void removeToolChainFile(IMesonCrossCompileFile file);

	IMesonCrossCompileFile getToolChainFile(Path path);

	Collection<IMesonCrossCompileFile> getToolChainFilesMatching(Map<String, String> properties);

	IMesonCrossCompileFile getToolChainFileFor(IToolChain toolchain);

	Collection<IMesonCrossCompileFile> getToolChainFiles();

	void addListener(IMesonCrossCompileListener listener);

	void removeListener(IMesonCrossCompileListener listener);

}