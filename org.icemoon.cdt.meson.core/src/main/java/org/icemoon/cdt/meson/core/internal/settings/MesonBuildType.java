package org.icemoon.cdt.meson.core.internal.settings;

public enum MesonBuildType {

	PLAIN, DEBUG, DEBUGOPTIMIZED, RELEASE, MINSIZE;

	public static String[] names() {
		String[] n = new String[values().length];
		int i = 0;
		for(MesonBuildType l : values()) {
			n[i++] = l.name();
		}
		return n;
	}
}
