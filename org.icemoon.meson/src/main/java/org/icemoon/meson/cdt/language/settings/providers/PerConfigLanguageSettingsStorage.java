package org.icemoon.meson.cdt.language.settings.providers;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.core.resources.IResource;

class PerConfigLanguageSettingsStorage implements Cloneable {

	/**
	 * Storage to keep settings entries. Key is
	 * {@link ICConfigurationDescription#getId()}
	 */
	private Map<String, TimestampedLanguageSettingsStorage> storages = new WeakHashMap<>();

	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc,
			String languageId) {
		final TimestampedLanguageSettingsStorage store = storages.get(cfgDescription.getId());
		if (store != null) {
			return store.getSettingEntries(rc, languageId);
		}
		return null;
	}

	/**
	 * Gets the settings storage for the specified configuration. Creates a new
	 * settings storage, if none exists.
	 *
	 * @return the storages never {@code null}
	 */
	public TimestampedLanguageSettingsStorage getSettingsForConfig(ICConfigurationDescription cfgDescription) {
		TimestampedLanguageSettingsStorage store = storages.get(cfgDescription.getId());
		if (store == null) {
			store = new TimestampedLanguageSettingsStorage();
			storages.put(cfgDescription.getId(), store);
		}
		return store;
	}

} // PerConfigLanguageSettingsStorage