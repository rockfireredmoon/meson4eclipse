/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
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

import org.icemoon.cdt.ninja.core.internal.PosixMakefileUtil;
import org.icemoon.cdt.ninja.core.internal.Util;

/**
 * NinjaFile
 */
public class NinjaFileUtil extends PosixMakefileUtil {
	public static boolean isIncluded(String line) {
		return isSubninja(line) || isInclude(line);
	}

	public static boolean isInclude(String line) {
		line = line.trim();
		return line.startsWith(NinjaFileConstants.DIRECTIVE_INCLUDE) && line.length() > 8
				&& Character.isWhitespace(line.charAt(7));
	}

	public static boolean isSubninja(String line) {
		line = line.trim();
		return line.startsWith(NinjaFileConstants.DIRECTIVE_SUBNINJA) && line.length() > 9
				&& Character.isWhitespace(line.charAt(8));
	}

	public static boolean isVariableDef(String line) {
		return line.matches("^\\s*[a-zA-Z_]+\\s*=.*");
	}

	public static boolean isRuleDefinition(String line) {
		line = line.trim();
		return line.startsWith(NinjaFileConstants.RULEDEF) && line.length() > 4
				&& Character.isWhitespace(line.charAt(4));
	}

	public static boolean isTargetVariable(String line) {
		// TODO what should target variable map to?
		// line = line.trim();
		// int index = Util.indexOf(line, ':');
		// if (index > 1) {
		// line = line.substring(index + 1).trim();
		// int equal = Util.indexOf(line, '=');
		// if (equal > 1) {
		// return true;
		// }
		// }
		return false;
	}

	public static boolean isVariableDefinition(String line) {
		return isTargetVariable(line) || isVariableDef(line) || isMacroDefinition(line);
	}

	private static boolean isMacroDefinition(String line) {
		// TODO what should macro map to?
		return false;
	}

	public static boolean isBuildRule(String line) {
		if (line.startsWith(NinjaFileConstants.DIRECTIVE_BUILD)
				&& line.length() > NinjaFileConstants.DIRECTIVE_BUILD.length()
				&& line.charAt(NinjaFileConstants.DIRECTIVE_BUILD.length()) == ' ') {
			line = line.trim();
			int colon1 = Util.indexOf(line, ':');
			return colon1 > 0;
		}
		return false;
	}

	public static boolean isPhonyRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(NinjaFileConstants.RULE_PHONY);
		}
		return false;
	}

	public static boolean isIntermediateRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(NinjaFileConstants.RULE_INTERMEDIATE);
		}
		return false;
	}

	public static boolean isSecondaryRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(NinjaFileConstants.RULE_SECONDARY);
		}
		return false;
	}

	public static boolean isDeleteOnErrorRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(NinjaFileConstants.RULE_DELETE_ON_ERROR);
		}
		return false;
	}

	public static boolean isLowResolutionTimeRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(NinjaFileConstants.RULE_LOW_RESOLUTION_TIME);
		}
		return false;
	}

	public static boolean isExportAllVariablesRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(NinjaFileConstants.RULE_EXPORT_ALL_VARIABLES);
		}
		return false;
	}

	public static boolean isNotParallelRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(NinjaFileConstants.RULE_NOT_PARALLEL);
		}
		return false;
	}

}
