/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
 * Copyright (c) 2018 Emerald Icemoon
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *      Emerald Icemoon - Ported to Ninja
 *******************************************************************************/
package org.icemoon.cdt.ninja.ui.internal.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.PatternRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

public class NinjaCodeScanner extends AbstractMakefileCodeScanner {
	@SuppressWarnings("nls")
	private final static String[] keywords = { "rule", "command", "deps", "depfile", "decription", "restart",
			"generator", "build", "phony", "default", "pool", "include", "subninja" };

	public static final String[] fTokenProperties = new String[] { ColorManager.NINJA_KEYWORD_COLOR,
			ColorManager.NINJA_FUNCTION_COLOR, ColorManager.NINJA_VARIABLE_REF_COLOR,
			ColorManager.NINJA_VARIABLE_DEF_COLOR, ColorManager.NINJA_DEFAULT_COLOR };

	private final class KeywordDetector implements IWordDetector {
		@Override
		public boolean isWordPart(char c) {
			return Character.isLetterOrDigit(c);
		}

		@Override
		public boolean isWordStart(char c) {
			return Character.isLetterOrDigit(c) || c == '_';
		}
	}

	private class KeywordRule extends WordRule {
		private KeywordRule() {
			super(new KeywordDetector());
		}

		@Override
		public IToken evaluate(ICharacterScanner scanner) {
			int offset = fOffset;
			IToken token = super.evaluate(scanner);
			if (token != fDefaultToken) {
				// check if the keyword starts from beginning of line possibly indented
				try {
					int line = fDocument.getLineOfOffset(offset);
					int start = fDocument.getLineOffset(line);
					String ident = fDocument.get(start, offset - start);
					if (ident.trim().length() > 0) {
						token = fDefaultToken;
					}
				} catch (BadLocationException ex) {
				}
			}
			return token;
		}
	}

	/**
	 * Constructor for NinjaCodeScanner
	 */
	public NinjaCodeScanner() {
		super();
		initialize();
	}

	@Override
	protected List<IRule> createRules() {
		IToken keywordToken = getToken(ColorManager.NINJA_KEYWORD_COLOR);
		IToken functionToken = getToken(ColorManager.NINJA_FUNCTION_COLOR);
		IToken macroRefToken = getToken(ColorManager.NINJA_VARIABLE_REF_COLOR);
		IToken macroDefToken = getToken(ColorManager.NINJA_VARIABLE_DEF_COLOR);
		IToken defaultToken = getToken(ColorManager.NINJA_DEFAULT_COLOR);

		List<IRule> rules = new ArrayList<IRule>();

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new IWhitespaceDetector() {
			@Override
			public boolean isWhitespace(char character) {
				return Character.isWhitespace(character);
			}
		}, defaultToken));

		// Add word rule for keywords, types, and constants.
		WordRule keyWordRule = new KeywordRule();
		for (String keyword : keywords) {
			keyWordRule.addWord(keyword, keywordToken);
		}
		rules.add(new VariableDefinitionRule(macroDefToken, Token.UNDEFINED));
		rules.add(keyWordRule);
		// rules.add(new TargetRule(functionToken)); // $NON-NLS-1$ //$NON-NLS-2$
		rules.add(new PatternRule("${", "}", macroRefToken, '$', true, true)); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new PatternRule("$", " ", macroRefToken, '$', true, true)); //$NON-NLS-1$ //$NON-NLS-2$

		setDefaultReturnToken(defaultToken);

		return rules;
	}

	@Override
	protected String[] getTokenProperties() {
		return fTokenProperties;
	}

}
