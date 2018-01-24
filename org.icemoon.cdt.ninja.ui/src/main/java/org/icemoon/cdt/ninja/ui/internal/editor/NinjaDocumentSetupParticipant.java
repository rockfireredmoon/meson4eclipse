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

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

/**
 * NinjaDocumentSetupParticipant
 * The document setup participant for Makefile.
 */
public class NinjaDocumentSetupParticipant  implements IDocumentSetupParticipant {
	/**
	 * The name of the Makefiile partitioning.
	 */
	public final static String MAKEFILE_PARTITIONING= "___makefile_partitioning";  //$NON-NLS-1$

	public NinjaDocumentSetupParticipant() {
	}

	@Override
	public void setup(IDocument document) {
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension3= (IDocumentExtension3) document;
			IDocumentPartitioner partitioner = createDocumentPartitioner();
			extension3.setDocumentPartitioner(MAKEFILE_PARTITIONING, partitioner);
			partitioner.connect(document);
		}
	}

	private IDocumentPartitioner createDocumentPartitioner() {
		return new FastPartitioner(
				new NinjaPartitionScanner(), NinjaPartitionScanner.MAKE_PARTITIONS);
	}
}
