package org.icemoon.cdt.ninja.core.internal;

import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IMakefile;

public abstract class Directive implements IDirective {

	int endLine;
	int startLine;
	Directive parent;

	public Directive(Directive owner) {
		parent = owner;
	}

	public Directive(int start, int end) {
		setLines(start, end);
	}

	@Override
	public abstract String toString();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.make.core.makefile.IDirective#getEndLine()
	 */
	@Override
	public int getEndLine() {
		return endLine;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.make.core.makefile.IDirective#getStartLine()
	 */
	@Override
	public int getStartLine() {
		return startLine;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.make.core.makefile.IDirective#getParent()
	 */
	@Override
	public IDirective getParent() {
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.make.core.makefile.IDirective#getMakefile()
	 */
	@Override
	public IMakefile getMakefile() {
		return parent.getMakefile();
	}

	public void setParent(Directive owner) {
		parent = owner;
	}

	public void setStartLine(int lineno) {
		startLine = lineno;
	}

	public void setEndLine(int lineno) {
		endLine = lineno;
	}

	public void setLines(int start, int end) {
		setStartLine(start);
		setEndLine(end);
	}
}
