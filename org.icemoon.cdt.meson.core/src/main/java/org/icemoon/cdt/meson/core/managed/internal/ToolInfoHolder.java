package org.icemoon.cdt.meson.core.managed.internal;

import java.util.List;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.runtime.IPath;

public class ToolInfoHolder {
	ITool[] buildTools;
	boolean[] buildToolsUsed;
	ManagedBuildMesonToolInfo[] gnuToolInfos;
	Set<String> outputExtensionsSet;
	List<IPath> dependencyMakefiles;
}