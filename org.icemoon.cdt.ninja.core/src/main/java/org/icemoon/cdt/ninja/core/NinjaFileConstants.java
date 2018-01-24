/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.core;

public class NinjaFileConstants {

	public static final String RULE_DEFAULT = ".DEFAULT"; //$NON-NLS-1$
	public static final String RULE_IGNORE = ".IGNORE"; //$NON-NLS-1$
	public static final String RULE_POSIX = ".POSIX"; //$NON-NLS-1$
	public static final String RULE_PRECIOUS = ".PRECIOUS"; //$NON-NLS-1$
	public static final String RULE_SCCS_GET = ".SCCS_GET"; //$NON-NLS-1$
	public static final String RULE_SILENT = ".SILENT"; //$NON-NLS-1$
	public static final String RULE_SUFFIXES = ".SUFFIXES"; //$NON-NLS-1$

	public static final String RULEDEF = "rule"; //$NON-NLS-1$

	public static final String DIRECTIVE_VPATH = "vpath"; //$NON-NLS-1$
	public static final String DIRECTIVE_UNEXPORT = "unexport"; //$NON-NLS-1$

	public static final String VARIABLE_EXPORT = "export"; //$NON-NLS-1$

	public static final String FUNCTION_CALL = "call"; //$NON-NLS-1$

	public static final String DIRECTIVE_SUBNINJA = "subninja"; //$NON-NLS-1$
	public static final String DIRECTIVE_INCLUDE = "include"; //$NON-NLS-1$
	public static final String DIRECTIVE_BUILD = "build"; //$NON-NLS-1$

	public static final String RULE_DELETE_ON_ERROR = ".DELETE_ON_ERROR"; //$NON-NLS-1$
	public static final String RULE_PHONY = ".PHONY"; //$NON-NLS-1$
	public static final String RULE_SECONDARY = ".SECONDARY"; //$NON-NLS-1$
	public static final String RULE_LOW_RESOLUTION_TIME = ".LOW_RESOLUTION_TIME"; //$NON-NLS-1$
	public static final String RULE_NOT_PARALLEL = ".NOTPARALLEL"; //$NON-NLS-1$
	public static final String RULE_EXPORT_ALL_VARIABLES = ".EXPORT_ALL_VARIABLES"; //$NON-NLS-1$
	public static final String RULE_INTERMEDIATE = ".INTERMEDIATE"; //$NON-NLS-1$
}
