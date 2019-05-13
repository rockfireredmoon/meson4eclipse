/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.icemoon.cdt.ninja.core.corebuild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.build.CBuilder;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tools.templates.freemarker.FMProjectGenerator;
import org.eclipse.tools.templates.freemarker.SourceRoot;
import org.eclipse.tools.templates.freemarker.TemplateManifest;
import org.icemoon.cdt.ninja.core.NinjaPlugin;
import org.icemoon.cdt.ninja.core.StatusInfo;
import org.osgi.framework.Bundle;

public class NinjaProjectGenerator extends FMProjectGenerator {

	private IToolChain toolChain = null;

	public NinjaProjectGenerator(String manifestFile) {
		super(manifestFile);
	}

	@Override
	protected void initProjectDescription(IProjectDescription description) {
		description.setNatureIds(
				new String[] { CProjectNature.C_NATURE_ID, CCProjectNature.CC_NATURE_ID, NinjaProjectNature.ID });
		ICommand command = description.newCommand();
		CBuilder.setupBuilder(command);
		description.setBuildSpec(new ICommand[] { command });
	}

	@Override
	public Bundle getSourceBundle() {
		return NinjaPlugin.getDefault().getBundle();
	}

	@Override
	protected void populateModel(Map<String, Object> model) {
		super.populateModel(model);

		if (toolChain == null)
			model.put("compiler", "cc"); //$NON-NLS-1$
		else
			model.put("compiler", toolChain.getCompileCommands()[0]); //$NON-NLS-1$
	}

	@Override
	public void generate(Map<String, Object> model, IProgressMonitor monitor) throws CoreException {

		// try the toolchain for the local target
		Map<String, String> properties = new HashMap<>();
		properties.put(IToolChain.ATTR_OS, Platform.getOS());
		properties.put(IToolChain.ATTR_ARCH, Platform.getOSArch());
		IToolChainManager toolChainManager = NinjaPlugin.getService(IToolChainManager.class);
		try {
			for (IToolChain tc : toolChainManager.getToolChainsMatching(properties)) {
				toolChain = tc;
				break;
			}
		} catch (CoreException ce) {
			//
		}

		// local didn't work, try and find one that does
		try {
			if (toolChain == null) {
				for (IToolChain tc : toolChainManager.getToolChainsMatching(new HashMap<>())) {
					toolChain = tc;
					break;
				}
			}
		} catch (CoreException ce) {
		}
		if (toolChain == null) {
			throw new CoreException(new StatusInfo(IStatus.ERROR, String.format(
					"No toolchain could be found that matches this OS and architecture. Pleasure ensure you have one that matches '%s' and '%s' in Preferences -> C/C++ -> Core Build Toolchains",
					Platform.getOS(), Platform.getOSArch())));
		}

		super.generate(model, monitor);

		List<IPathEntry> entries = new ArrayList<>();
		IProject project = getProject();

		// Create the source and output folders
		IFolder buildFolder = getProject().getFolder("build"); //$NON-NLS-1$

		TemplateManifest manifest = getManifest();
		if (manifest != null) {
			List<SourceRoot> srcRoots = getManifest().getSrcRoots();
			if (srcRoots != null && !srcRoots.isEmpty()) {
				for (SourceRoot srcRoot : srcRoots) {
					IFolder sourceFolder = project.getFolder(srcRoot.getDir());
					if (!sourceFolder.exists()) {
						sourceFolder.create(true, true, monitor);
					}

					entries.add(CoreModel.newSourceEntry(sourceFolder.getFullPath(),
							new IPath[] { buildFolder.getFullPath() }));
				}
			} else {
				entries.add(CoreModel.newSourceEntry(getProject().getFullPath()));
			}
		}

		entries.add(CoreModel.newOutputEntry(project.getFullPath())); // $NON-NLS-1$
		CoreModel.getDefault().create(project).setRawPathEntries(entries.toArray(new IPathEntry[entries.size()]),
				monitor);
	}

}
