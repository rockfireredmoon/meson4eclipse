/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.core.internal;

import java.util.ArrayList;

import org.eclipse.cdt.make.core.makefile.ICommand;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.ITarget;
import org.eclipse.cdt.make.core.makefile.ITargetRule;
import org.icemoon.cdt.ninja.core.NinjaFileConstants;

public class Build extends Parent implements ITargetRule {

	Target target;
	String[] prerequisites;

	public Build(Directive parent, Target target, String[] reqs, Command[] cmds) {
		super(parent);
		this.target = target;
		if (cmds != null) {
			for (Command cmd : cmds)
				addDirective(cmd);
		}
	}

	public Build(Directive parent, Target tgt, String[] reqs) {
		this(parent, tgt, reqs, new Command[0]);
	}

	@Override
	public ICommand[] getCommands() {
		IDirective[] directives = getDirectives();
		ArrayList<IDirective> cmds = new ArrayList<IDirective>(directives.length);
		for (int i = 0; i < directives.length; i++) {
			if (directives[i] instanceof ICommand) {
				cmds.add(directives[i]);
			}
		}
		return cmds.toArray(new ICommand[0]);
	}

	@Override
	public ITarget getTarget() {
		return target;
	}

	public void setTarget(Target tgt) {
		target = tgt;
	}

	public boolean equals(Build r) {
		return r.getTarget().equals(getTarget());
	}

	@Override
	public String[] getPrerequisites() {
		return prerequisites;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(NinjaFileConstants.DIRECTIVE_BUILD);
		sb.append(' ').append(target).append(':');
		String[] reqs = getPrerequisites();
		if (reqs != null) {
			for (int i = 0; i < reqs.length; i++) {
				sb.append(' ').append(reqs[i]);
			}
		}
		sb.append('\n');
		ICommand[] cmds = getCommands();
		for (int i = 0; i < cmds.length; i++) {
			sb.append(cmds[i]);
		}
		return sb.toString();
	}
}
