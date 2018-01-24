package org.icemoon.cdt.ninja.core.language.settings.providers;

import java.util.List;

import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsStorage;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.core.resources.IResource;

////////////////////////////////////////////////////////////////////
// inner classes
////////////////////////////////////////////////////////////////////
class TimestampedLanguageSettingsStorage extends LanguageSettingsStorage {
	/** cached file modification time-stamp of last parse */
	long lastModified = 0;

	/**
	 * Sets language settings entries for this storages.
	 *
	 * @param rc
	 *            resource such as file or folder or project. If {@code null} the
	 *            entries are considered to be being defined as project-level
	 *            entries for child resources.
	 * @param languageId
	 *            language id. Must not be {@code null}
	 * @param entries
	 *            language settings entries to set.
	 */
	public void setSettingEntries(IResource rc, String languageId, List<ICLanguageSettingEntry> entries) {
		/*
		 * compile_commands.json holds entries per-file only and does not contain
		 * per-project or per-folder entries. So we map the latter as project entries
		 * (=> null) to make the UI show the include directories we detected.
		 */
		String rcPath = null;
		if (rc != null && rc.getType() == IResource.FILE) {
			rcPath = rc.getProjectRelativePath().toString();
		}
		super.setSettingEntries(rcPath, languageId, entries);
	}

	/**
	 * Sets language settings entries for this storages.
	 *
	 * @param rc
	 *            resource such as file or folder or project. If {@code null} the
	 *            entries are considered to be being defined as project-level
	 *            entries for child resources.
	 * @param languageId
	 *            language id. Must not be {@code null}
	 * @return the list of setting entries or {@code null} if no settings defined.
	 */
	public List<ICLanguageSettingEntry> getSettingEntries(IResource rc, String languageId) {
		/*
		 * compile_commands.json holds entries per-file only and does not contain
		 * per-project or per-folder entries. So we map the latter as project entries
		 * (=> null) to make the UI show the include directories we detected.
		 */
		String rcPath = null;
		if (rc != null && rc.getType() == IResource.FILE) {
			rcPath = rc.getProjectRelativePath().toString();
		}
		return super.getSettingEntries(rcPath, languageId);
	}

	@Override
	public TimestampedLanguageSettingsStorage clone() {
		TimestampedLanguageSettingsStorage cloned = new TimestampedLanguageSettingsStorage();
		cloned.lastModified = this.lastModified;
		cloned.fStorage.putAll(super.fStorage);
		return cloned;
	}

	@Override
	public void clear() {
		synchronized (fStorage) {
			super.clear();
			lastModified = 0;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (lastModified ^ (lastModified >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimestampedLanguageSettingsStorage other = (TimestampedLanguageSettingsStorage) obj;
		if (lastModified != other.lastModified)
			return false;
		return true;
	}

} // TimestampedLanguageSettingsStorage