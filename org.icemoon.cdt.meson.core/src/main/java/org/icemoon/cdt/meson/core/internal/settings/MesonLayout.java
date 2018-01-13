package org.icemoon.cdt.meson.core.internal.settings;

public enum MesonLayout {

	MIRROR, FLAT;

	public static String[] names() {
		String[] n = new String[values().length];
		int i = 0;
		for(MesonLayout l : values()) {
			n[i++] = l.name();
		}
		return n;
	}
}
