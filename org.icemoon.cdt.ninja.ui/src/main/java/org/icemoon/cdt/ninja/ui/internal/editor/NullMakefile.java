package org.icemoon.cdt.ninja.ui.internal.editor;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;

import org.eclipse.cdt.make.core.makefile.IAutomaticVariable;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IMakefileReaderProvider;
import org.icemoon.cdt.ninja.core.AbstractMakefile;

/**
 * Makefile : ( statement ) *
 * statement :   rule | macro_definition | comments | empty
 * rule :  inference_rule | target_rule
 * inference_rule : target ':' <nl> ( <tab> command <nl> ) +
 * target_rule : target [ ( target ) * ] ':' [ ( prerequisite ) * ] [ ';' command ] <nl>
                 [ ( command ) * ]
 * macro_definition : string '=' (string)*
 * comments : ('#' (string) <nl>) *
 * empty : <nl>
 * command : <tab> prefix_command string <nl>
 * target : string
 * prefix_command : '-' | '@' | '+'
 * internal_macro :  "$<" | "$*" | "$@" | "$?" | "$%"
 */

public class NullMakefile extends AbstractMakefile {
	private final static IDirective[] EMPTY_DIRECTIVES = new IDirective[0];
	private final static IAutomaticVariable[] EMPTY_AUTOMATIC_VARIABLES = new IAutomaticVariable[0];

	public NullMakefile() {
		super(null);
	}

	@Override
	public IDirective[] getDirectives() {
		return EMPTY_DIRECTIVES;
	}

	@Override
	public IDirective[] getBuiltins() {
		return EMPTY_DIRECTIVES;
	}

	@Override
	public IAutomaticVariable[] getAutomaticVariables() {
		return EMPTY_AUTOMATIC_VARIABLES;
	}

	public void addDirective(IDirective directive) {
	}

	@Override
	public String toString() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public void parse(String name, Reader makefile) throws IOException {
	}

	@Override
	public IMakefileReaderProvider getMakefileReaderProvider() {
		return null;
	}

	public void parse(String name, IMakefileReaderProvider makefileReaderProvider) throws IOException {
	}
	@Override
	public void parse(URI fileURI, Reader makefile) throws IOException {
	}

	@Override
	public void parse(URI fileURI, IMakefileReaderProvider makefileReaderProvider) throws IOException {
	}
}
