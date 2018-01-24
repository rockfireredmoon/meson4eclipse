/*******************************************************************************
 * Copyright (c) 2017 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - initial API and implementation
 *******************************************************************************/

package org.icemoon.cdt.meson.core.corebuild;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.build.core.scannerconfig.ScannerConfigBuilder;
import org.eclipse.cdt.build.core.scannerconfig.ScannerConfigNature;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.build.CBuilder;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tools.templates.core.IGenerator;
import org.eclipse.tools.templates.freemarker.FMProjectGenerator;
import org.eclipse.tools.templates.freemarker.SourceRoot;
import org.eclipse.tools.templates.freemarker.TemplateManifest;
import org.icemoon.cdt.meson.core.Activator;
import org.icemoon.cdt.meson.core.MesonNature;
import org.osgi.framework.Bundle;

public class MesonProjectGenerator extends FMProjectGenerator implements IGenerator {

	private boolean managed;

	public MesonProjectGenerator(String manifestFile) {
		this(manifestFile, false);
	}

	public MesonProjectGenerator(String manifestFile, boolean managed) {
		super(manifestFile);
		this.managed = managed;
	}

	@Override
	protected void initProjectDescription(IProjectDescription description) {
		if (managed) {
			description.setNatureIds(new String[] { CProjectNature.C_NATURE_ID, CCProjectNature.CC_NATURE_ID,
					MesonNature.ID, ManagedCProjectNature.MNG_NATURE_ID, ScannerConfigNature.NATURE_ID });

			ICommand command1 = description.newCommand();
			command1.setBuilderName(ManagedCProjectNature.BUILDER_ID);
			command1.setBuilding(IncrementalProjectBuilder.CLEAN_BUILD, true);
			command1.setBuilding(IncrementalProjectBuilder.FULL_BUILD, true);
			command1.setBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD, true);
			ICommand command2 = description.newCommand();
			command2.setBuilderName(ScannerConfigBuilder.BUILDER_ID);
			command2.setBuilding(IncrementalProjectBuilder.FULL_BUILD, true);
			command2.setBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD, true);
			description.setBuildSpec(new ICommand[] { command1, command2 });
		}
		else {
			description.setNatureIds(
					new String[] { CProjectNature.C_NATURE_ID, CCProjectNature.CC_NATURE_ID, MesonNature.ID });
			ICommand command = description.newCommand();
			CBuilder.setupBuilder(command);
			if (managed) {

			}
			description.setBuildSpec(new ICommand[] { command });
		}
	}

	@Override
	public Bundle getSourceBundle() {
		return Activator.getDefault().getBundle();
	}

	@Override
	public void generate(Map<String, Object> model, IProgressMonitor monitor) throws CoreException {
		super.generate(model, monitor);

		List<IPathEntry> entries = new ArrayList<>();
		IProject project = getProject();

		// Create the source folders
		TemplateManifest manifest = getManifest();
		if (manifest != null) {
			List<SourceRoot> srcRoots = getManifest().getSrcRoots();
			if (srcRoots != null && !srcRoots.isEmpty()) {
				for (SourceRoot srcRoot : srcRoots) {
					IFolder sourceFolder = project.getFolder(srcRoot.getDir());
					if (!sourceFolder.exists()) {
						sourceFolder.create(true, true, monitor);
					}

					entries.add(CoreModel.newSourceEntry(sourceFolder.getFullPath()));
				}
			} else {
				entries.add(CoreModel.newSourceEntry(getProject().getFullPath()));
			}
		}

		entries.add(CoreModel.newOutputEntry(getProject().getFullPath())); // $NON-NLS-1$
		CoreModel.getDefault().create(project).setRawPathEntries(entries.toArray(new IPathEntry[entries.size()]),
				monitor);

	}

}
