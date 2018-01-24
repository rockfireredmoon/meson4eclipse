/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Emerald Icemoon - Ported to Meson
 *******************************************************************************/
package org.icemoon.cdt.meson.core;

/**
 * Listener for toolchain events.
 */
public interface IMesonCrossCompileListener {

	void handleMesonToolChainEvent(MesonCrossCompileEvent event);

}
