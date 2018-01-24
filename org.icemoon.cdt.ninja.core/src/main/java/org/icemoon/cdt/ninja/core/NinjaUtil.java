package org.icemoon.cdt.ninja.core;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class NinjaUtil {

	public static void createProjectFolder(IProject project, IPath path, IProgressMonitor monitor)
			throws CoreException {
		if (!path.equals(project.getLocation()) && project.getLocation().isPrefixOf(path)) {
			/*
			 * Make sure parent exists, if it doesn't, traverse up tree as far as project
			 * root
			 */
			IPath parent = path.removeLastSegments(1);
			IFolder parentContainer = project.getFolder(parent);
			if ((!parentContainer.exists()))
				createProjectFolder(project, parent, monitor);
			IFolder newContainer = project.getFolder(path.makeRelativeTo(project.getLocation()));
			if (!newContainer.exists()) {
				newContainer.create(IResource.FORCE | IResource.DERIVED, true, monitor);
			}
		}
	}
}
