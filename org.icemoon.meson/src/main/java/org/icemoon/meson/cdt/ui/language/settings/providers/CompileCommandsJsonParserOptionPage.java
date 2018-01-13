/*******************************************************************************
 * Copyright (c) 2017 Martin Weber.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Martin Weber - Initial implementation
 *******************************************************************************/
package org.icemoon.meson.cdt.ui.language.settings.providers;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.icemoon.meson.cdt.language.settings.providers.CompileCommandsJsonParser;

/**
 * Option page for CompileCommandsJsonParse
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class CompileCommandsJsonParserOptionPage extends AbstractParserOptionPage<CompileCommandsJsonParser> {

	@Override
	protected CompileCommandsJsonParser getProvider() {
		ILanguageSettingsProvider provider = LanguageSettingsManager
				.getWorkspaceProvider("org.icemoon.meson.cdt.language.settings.providers.CompileCommandsJsonParser");
		return (CompileCommandsJsonParser) LanguageSettingsManager.getRawProvider(provider);
	}
}
