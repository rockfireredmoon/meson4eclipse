package org.icemoon.cdt.meson.core.internal.settings;

public enum MesonDefaultLibrary {

	SHARED, STATIC;;

	public static String[] names() {
		String[] n = new String[values().length];
		int i = 0;
		for(MesonDefaultLibrary l : values()) {
			n[i++] = l.name();
		}
		return n;
	}
	
}
