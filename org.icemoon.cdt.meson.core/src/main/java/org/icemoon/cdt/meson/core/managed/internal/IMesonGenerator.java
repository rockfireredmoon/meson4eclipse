package org.icemoon.cdt.meson.core.managed.internal;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.runtime.IPath;

public interface IMesonGenerator {

	Set<String> getOutputExtensions(ToolInfoHolder h);

	StringBuffer getSourceMacroName(String extensionName);

	StringBuffer getDepMacroName(String extensionName);

	IPath getBuildWorkingDir();

	IPath getTopBuildDir();

	HashMap<String, List<IPath>> getBuildOutputVars();

	List<String> getBuildVariableList(ToolInfoHolder h, String variable, int locationType, IPath directory,
			boolean getAll);

	LinkedHashMap<String, String> getTopBuildOutputVars();
	
	void addMacroAdditionFiles(HashMap<String, String> map, String macroName, Vector<String> filenames);
}
