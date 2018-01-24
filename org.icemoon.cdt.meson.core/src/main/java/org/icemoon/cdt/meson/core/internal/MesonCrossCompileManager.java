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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.icemoon.cdt.meson.core.Activator;
import org.icemoon.cdt.meson.core.IMesonCrossCompileFile;
import org.icemoon.cdt.meson.core.IMesonCrossCompileListener;
import org.icemoon.cdt.meson.core.IMesonCrossCompileManager;
import org.icemoon.cdt.meson.core.MesonCrossCompileEvent;
import org.icemoon.cdt.meson.core.corebuild.internal.MesonBuildConfiguration;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class MesonCrossCompileManager implements IMesonCrossCompileManager {

	private Map<Path, IMesonCrossCompileFile> files;

	private static final String N = "n"; //$NON-NLS-1$
	private static final String PATH = "__path"; //$NON-NLS-1$

	private final List<IMesonCrossCompileListener> listeners = new LinkedList<>();

	private Preferences getPreferences() {
		return InstanceScope.INSTANCE.getNode(Activator.getId()).node("mesonToolchains"); //$NON-NLS-1$
	}

	private void init() {
		if (files == null) {
			files = new HashMap<>();

			Preferences prefs = getPreferences();
			try {
				for (String childName : prefs.childrenNames()) {
					Preferences tcNode = prefs.node(childName);
					String pathStr = tcNode.get(PATH, "/"); //$NON-NLS-1$
					Path path = Paths.get(pathStr);
					if (Files.exists(path) && !files.containsKey(path)) {
						IMesonCrossCompileFile file = new MesonCrossCompileFile(childName, path);
						for (String key : tcNode.keys()) {
							String value = tcNode.get(key, ""); //$NON-NLS-1$
							if (!value.isEmpty()) {
								file.setProperty(key, value);
							}
						}
						files.put(path, file);
					} else {
						tcNode.removeNode();
						prefs.flush();
					}
				}
			} catch (BackingStoreException e) {
				Activator.log(e);
			}

		}
	}

	@Override
	public IMesonCrossCompileFile newToolChainFile(Path path) {
		return new MesonCrossCompileFile(null, path);
	}

	@Override
	public void addToolChainFile(IMesonCrossCompileFile file) {
		init();
		if (files.containsKey(file.getPath())) {
			removeToolChainFile(file);
		}
		files.put(file.getPath(), file);

		// save it

		MesonCrossCompileFile realFile = (MesonCrossCompileFile) file;
		Preferences prefs = getPreferences();
		String n = realFile.n;
		if (n == null) {
			n = prefs.get(N, "0"); //$NON-NLS-1$
			realFile.n = n;
		}
		prefs.put(N, Integer.toString(Integer.parseInt(n) + 1));

		Preferences tcNode = prefs.node(n);
		tcNode.put(PATH, file.getPath().toString());
		for (Entry<String, String> entry : realFile.properties.entrySet()) {
			tcNode.put(entry.getKey(), entry.getValue());
		}

		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}

		fireEvent(new MesonCrossCompileEvent(MesonCrossCompileEvent.ADDED, file));
	}

	@Override
	public void removeToolChainFile(IMesonCrossCompileFile file) {
		init();
		fireEvent(new MesonCrossCompileEvent(MesonCrossCompileEvent.REMOVED, file));
		files.remove(file.getPath());

		String n = ((MesonCrossCompileFile) file).n;
		if (n != null) {
			Preferences prefs = getPreferences();
			Preferences tcNode = prefs.node(n);
			try {
				tcNode.removeNode();
				prefs.flush();
			} catch (BackingStoreException e) {
				Activator.log(e);
			}
		}
	}

	@Override
	public IMesonCrossCompileFile getToolChainFile(Path path) {
		init();
		return files.get(path);
	}

	@Override
	public Collection<IMesonCrossCompileFile> getToolChainFiles() {
		init();
		return Collections.unmodifiableCollection(files.values());
	}

	@Override
	public Collection<IMesonCrossCompileFile> getToolChainFilesMatching(Map<String, String> properties) {
		List<IMesonCrossCompileFile> matches = new ArrayList<>();
		for (IMesonCrossCompileFile file : getToolChainFiles()) {
			boolean match = true;
			for (Entry<String, String> entry : properties.entrySet()) {
				if (!entry.getValue().equals(file.getProperty(entry.getKey()))) {
					match = false;
					break;
				}
			}

			if (match) {
				matches.add(file);
			}
		}
		return matches;
	}

	@Override
	public IMesonCrossCompileFile getToolChainFileFor(IToolChain toolchain) {
		String id = toolchain.getId();

		for (IMesonCrossCompileFile file : getToolChainFiles()) {
			if (id.equals(file.getProperty(MesonBuildConfiguration.TOOLCHAIN_ID))) {
				return file;
			}
		}

		return null;
	}

	@Override
	public void addListener(IMesonCrossCompileListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(IMesonCrossCompileListener listener) {
		listeners.remove(listener);
	}

	private void fireEvent(MesonCrossCompileEvent event) {
		for (IMesonCrossCompileListener listener : listeners) {
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.handleMesonToolChainEvent(event);
				}

				@Override
				public void handleException(Throwable exception) {
					Activator.log(exception);
				}
			});
		}
	}

}
