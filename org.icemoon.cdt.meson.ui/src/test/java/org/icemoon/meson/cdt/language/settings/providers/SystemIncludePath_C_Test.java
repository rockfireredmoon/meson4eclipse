/*******************************************************************************
 * Copyright (c) 2015 Martin Weber.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Martin Weber - Initial implementation
 *******************************************************************************/
package org.icemoon.cdt.meson.ui.language.settings.providers;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.icemoon.cdt.meson.core.language.settings.providers.ToolArgumentParsers.SystemIncludePath_C;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Martin Weber  @author Emerald Icemoon
 */
public class SystemIncludePath_C_Test {

  private SystemIncludePath_C testee;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    testee = new SystemIncludePath_C();
}

  @Test
  public final void testProcessArgument() {
    final String more = " -g -MMD -MT MesonFiles/execut1.dir/util1.c.o -MF \"MesonFiles/execut1.dir/util1.c.o.d\""
        + " -o MesonFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
    List<ICLanguageSettingEntry> entries = new ArrayList<>();
    ICLanguageSettingEntry parsed;
    final IPath cwd= new Path("");

    String name = "/an/Include/Path";

    // -isystem   /an/Include/Path
    entries.clear();
    assertEquals(8 + name.length() + 3,
        testee.processArgument(entries, cwd, "-isystem   " + name + more));
    assertEquals("#entries", 1, entries.size());
    parsed = entries.get(0);
    assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
    assertEquals("name", name, parsed.getName());
    // -isystem   '/an/Include/Path'
    entries.clear();
    assertEquals(8 + name.length() + 3 + 2,
        testee.processArgument(entries, cwd, "-isystem   " + "'" + name + "'" + more));
    assertEquals("#entries", 1, entries.size());
    parsed = entries.get(0);
    assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
    assertEquals("name", name, parsed.getName());
    // -isystem   "/an/Include/Path"
    entries.clear();
    assertEquals(8 + name.length() + 3 + 2,
        testee.processArgument(entries, cwd, "-isystem   " + "\"" + name + "\"" + more));
    assertEquals("#entries", 1, entries.size());
    parsed = entries.get(0);
    assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
    assertEquals("name", name, parsed.getName());

    name = "A:an\\Include/Path";
    // -isystemA:an\Include/Path
    entries.clear();
    assertEquals(8 + 1+ name.length(),
        testee.processArgument(entries, cwd, "-isystem " + name + more));
    assertEquals("#entries", 1, entries.size());
    parsed = entries.get(0);
    assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
    assertEquals("name", name, parsed.getName());
  }

  /**
   * Test method for
   * {@link org.icemoon.cdt.meson.core.language.settings.providers.ToolArgumentParsers.IncludePath_C_POSIX#processArgument(java.util.List, IPath, java.lang.String)}
   */
  @Test
  public final void testProcessArgument_WS() {
    final String more = " -g -MMD -MT MesonFiles/execut1.dir/util1.c.o -MF \"MesonFiles/execut1.dir/util1.c.o.d\""
        + " -o MesonFiles/execut1.dir/util1.c.o -c /testprojects/C-subsrc/src/src-sub/main1.c";
    List<ICLanguageSettingEntry> entries = new ArrayList<>();
    ICLanguageSettingEntry parsed;
    final IPath cwd= new Path("");

    String name = "/ye olde/In clu de/Pa the";
    // -isystem   '/ye olde/In clu de/Pa the'
    entries.clear();
    assertEquals(8 + name.length() + 3 + 2,
        testee.processArgument(entries, cwd, "-isystem   " + "'" + name + "'" + more));
    assertEquals("#entries", 1, entries.size());
    parsed = entries.get(0);
    assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
    assertEquals("name", name, parsed.getName());
    // -isystem   "/ye olde/In clu de/Pa the"
    entries.clear();
    assertEquals(8 + name.length() + 3 + 2,
        testee.processArgument(entries, cwd, "-isystem   " + "\"" + name + "\"" + more));
    assertEquals("#entries", 1, entries.size());
    parsed = entries.get(0);
    assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
    assertEquals("name", name, parsed.getName());

    name = "A:an\\In CLU  de/Pat h";
    // -isystem   'A:an\In CLU  de/Pat h'
    entries.clear();
    assertEquals(8 + name.length() +3 + 2,
        testee.processArgument(entries, cwd, "-isystem   " + "\"" + name + "\"" + more));
    assertEquals("#entries", 1, entries.size());
    parsed = entries.get(0);
    assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
    assertEquals("name", name, parsed.getName());
  }

}
