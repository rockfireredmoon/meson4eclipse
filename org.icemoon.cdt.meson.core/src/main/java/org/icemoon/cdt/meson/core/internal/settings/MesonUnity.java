package org.icemoon.cdt.meson.core.internal.settings;

public enum MesonUnity {
	ON, OFF, SUBPROJECTS;

	public static String[] names() {
		String[] n = new String[values().length];
		int i = 0;
		for(MesonUnity l : values()) {
			n[i++] = l.name();
		}
		return n;
	}
}
