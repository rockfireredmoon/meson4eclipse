package org.icemoon.cdt.ninja.ui.internal.language.settings.providers;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.icemoon.cdt.ninja.core.language.settings.providers.CompileCommandsJsonParser;

public class CompileCommandsJsonParserOptionPage extends AbstractParserOptionPage<CompileCommandsJsonParser> {

	@Override
	protected CompileCommandsJsonParser getProvider() {
		ILanguageSettingsProvider provider = LanguageSettingsManager
				.getWorkspaceProvider(CompileCommandsJsonParser.class.getName());
		return (CompileCommandsJsonParser) LanguageSettingsManager.getRawProvider(provider);
	}
}
