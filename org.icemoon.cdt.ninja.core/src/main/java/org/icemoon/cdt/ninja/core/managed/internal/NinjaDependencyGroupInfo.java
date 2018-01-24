/*******************************************************************************
 * Copyright (c) 2006, 2010 Intel Corporation and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.core.managed.internal;


/**
 * 
 * This class contains the description of a group of generated dependency files,
 * e.g., .d files created by compilations
 *
 */

public class NinjaDependencyGroupInfo {
	
	//  Member Variables
	String groupBuildVar;
	boolean conditionallyInclude;
//	ArrayList groupFiles;
	
	//  Constructor
	public NinjaDependencyGroupInfo(String groupName, boolean bConditionallyInclude) {
		groupBuildVar = groupName;
		conditionallyInclude = bConditionallyInclude;
		//  Note: not yet needed
//		groupFiles = null;
	}

}
