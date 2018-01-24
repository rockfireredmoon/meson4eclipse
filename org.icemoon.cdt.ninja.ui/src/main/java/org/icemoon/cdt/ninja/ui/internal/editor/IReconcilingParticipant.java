/*******************************************************************************
 * Copyright (c) 2002, 2013 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Emerald Icemoon - Ported to Ninja
 *******************************************************************************/

package org.icemoon.cdt.ninja.ui.internal.editor;

/**
 * IReconcilingParticipant Interface of an object participating in reconciling.
 */
public interface IReconcilingParticipant {

	/**
	 * Called after reconciling has been finished.
	 */
	void reconciled();
}
