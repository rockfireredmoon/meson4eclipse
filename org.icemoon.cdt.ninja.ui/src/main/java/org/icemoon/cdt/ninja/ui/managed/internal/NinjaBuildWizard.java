/*******************************************************************************
 * Copyright (c) 2007, 2015 Intel Corporation and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     Red Hat Inc - modification for Autotools project
 *     Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.ui.managed.internal;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyManager;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyType;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.BuildListComparator;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.AbstractCWizard;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSWizardHandler;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.jface.wizard.IWizard;

public class NinjaBuildWizard extends AbstractCWizard {
	public static final String NINJA_PROJECTTYPE_ID = "org.icemoon.cdt.build.ninja.projectType"; //$NON-NLS-1$
	public static final String EMPTY_PROJECT = Messages.AbstractCWizard_0;
	public static final String NAME = Messages.StdBuildWizard_0;

	@Override
	public EntryDescriptor[] createItems(boolean supportedOnly, IWizard wizard) {
		IBuildPropertyManager bpm = ManagedBuildManager.getBuildPropertyManager();
		IBuildPropertyType bpt = bpm.getPropertyType(MBSWizardHandler.ARTIFACT);
		IBuildPropertyValue[] vs = bpt.getSupportedValues();
		Arrays.sort(vs, BuildListComparator.getInstance());

		ArrayList<EntryDescriptor> items = new ArrayList<EntryDescriptor>();

		IToolChain[] tcs = ManagedBuildManager.getExtensionsToolChains(MBSWizardHandler.ARTIFACT, NINJA_PROJECTTYPE_ID,
				false);
		MBSWizardHandler h = new MBSWizardHandler(NINJA_PROJECTTYPE_ID, parent, wizard);
		for (int j = 0; j < tcs.length; j++) {
			if (isValid(tcs[j], supportedOnly, wizard))
				h.addTc(tcs[j]);
		}
		// if (h.getToolChainsCount() > 0) {
		// The project category item.
		items.add(new EntryDescriptor(NINJA_PROJECTTYPE_ID, null, NAME, true, h, null));
		EntryDescriptor entryDescriptor = new EntryDescriptor(NINJA_PROJECTTYPE_ID + ".default", NINJA_PROJECTTYPE_ID, //$NON-NLS-1$
				EMPTY_PROJECT, false, h, null);
		entryDescriptor.setDefaultForCategory(true);
		items.add(entryDescriptor);
		// }

		//
		//
		//
		// // old style project types
		// EntryDescriptor oldsRoot = null;
		// SortedMap<String, IProjectType> sm =
		// ManagedBuildManager.getExtensionProjectTypeMap();
		// for (String s : sm.keySet()) {
		// IProjectType pt = sm.get(s);
		// if (pt.isAbstract() || pt.isSystemObject()) continue;
		// if (supportedOnly && !pt.isSupported()) continue; // not supported
		// String nattr = pt.getNameAttribute();
		// if (nattr == null || nattr.length() == 0) continue; // new proj style
		// MBSWizardHandler h = new MBSWizardHandler(pt, parent, wizard);
		// IToolChain[] tcs = ManagedBuildManager.getExtensionToolChains(pt);
		// for(int i = 0; i < tcs.length; i++){
		// IToolChain t = tcs[i];
		// if(t.isSystemObject())
		// continue;
		// if (!isValid(t, supportedOnly, wizard))
		// continue;
		//
		// h.addTc(t);
		// }
		//// IConfiguration[] cfgs = pt.getConfigurations();
		//// if (cfgs == null || cfgs.length == 0) continue;
		//// IToolChain tc = null;
		//// for (int i=0; i<cfgs.length; i++) {
		//// if (cfgs[i].isSystemObject()) continue;
		//// IToolChain t = cfgs[i].getToolChain();
		//// if (isValid(t, supportedOnly, wizard)) {
		//// tc = t;
		//// break;
		//// }
		//// }
		//// if (tc == null) continue;
		//// h.addTc(tc);
		//
		// String pId = null;
		// if (CDTPrefUtil.getBool(CDTPrefUtil.KEY_OTHERS)) {
		// if (oldsRoot == null) {
		// oldsRoot = new EntryDescriptor(OTHERS_LABEL, null, OTHERS_LABEL, true, null,
		// null);
		// items.add(oldsRoot);
		// }
		// pId = oldsRoot.getId();
		// } else {
		// // do not group to <Others> - pId = null;
		// }
		// items.add(new EntryDescriptor(pt.getId(), pId, pt.getName(), false, h, IMG));
		// }
		// return items.toArray(new EntryDescriptor[items.size()]);

		return items.toArray(new EntryDescriptor[items.size()]);
	}
}
