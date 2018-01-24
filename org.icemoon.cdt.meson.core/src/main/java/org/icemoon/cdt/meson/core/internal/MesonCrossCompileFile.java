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
package org.icemoon.cdt.meson.core.internal;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.core.runtime.CoreException;
import org.icemoon.cdt.meson.core.Activator;
import org.icemoon.cdt.meson.core.IMesonCrossCompileFile;
import org.icemoon.cdt.meson.core.corebuild.internal.MesonBuildConfiguration;

public class MesonCrossCompileFile implements IMesonCrossCompileFile {

	String n;
	private final Path path;
	private IToolChain toolchain;

	final Map<String, String> properties = new HashMap<>();

	public MesonCrossCompileFile(String n, Path path) {
		this.n = n;
		this.path = path;
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public String getProperty(String key) {
		return properties.get(key);
	}

	@Override
	public void setProperty(String key, String value) {
		properties.put(key, value);
	}

	@Override
	public IToolChain getToolChain() throws CoreException {
		if (toolchain == null) {
			IToolChainManager tcManager = Activator.getService(IToolChainManager.class);
			toolchain = tcManager.getToolChain(properties.get(MesonBuildConfiguration.TOOLCHAIN_TYPE),
					properties.get(MesonBuildConfiguration.TOOLCHAIN_ID));

			if (toolchain == null) {
				Collection<IToolChain> tcs = tcManager.getToolChainsMatching(properties);
				if (!tcs.isEmpty()) {
					toolchain = tcs.iterator().next();
				}
			}
		}
		return toolchain;
	}

	boolean matches(Map<String, String> properties) {
		for (Map.Entry<String, String> property : properties.entrySet()) {
			if (!property.getValue().equals(getProperty(property.getKey()))) {
				return false;
			}
		}
		return true;
	}

}
