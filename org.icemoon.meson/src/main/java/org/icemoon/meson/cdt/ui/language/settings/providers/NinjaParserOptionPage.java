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
import org.icemoon.meson.cdt.language.settings.providers.NinjaParser;

/**
 * Option page for NinjaParser
 *
 * @author Martin Weber
 * @author Emerald Icemoon
 */
public class NinjaParserOptionPage extends AbstractParserOptionPage<NinjaParser> {

	/**
	 * Get provider being displayed on this Options Page.
	 *
	 * @return provider.
	 */
	protected NinjaParser getProvider() {
		ILanguageSettingsProvider provider = LanguageSettingsManager
				.getWorkspaceProvider("org.icemoon.meson.cdt.language.settings.providers.NinjaParser");
		return (NinjaParser) LanguageSettingsManager.getRawProvider(provider);
	}
}
