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
package org.icemoon.meson.cdt.language.settings.providers;

import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.runtime.Path;
import org.icemoon.meson.cdt.language.settings.providers.ResponseFileArgumentParsers;
import org.icemoon.meson.cdt.language.settings.providers.ToolArgumentParsers;
import org.icemoon.meson.cdt.language.settings.providers.ToolCommandlineParser;
import org.junit.Test;

/**
 * @author Martin Weber  @author Emerald Icemoon
 */
public class ToolCommandlineParserTest {

  @Test
  public final void testResponseFileArgumentParser_At() throws Exception {

    ToolCommandlineParser testee = new ToolCommandlineParser("egal", new ResponseFileArgumentParsers.At(),
        new ToolArgumentParsers.IncludePath_C_POSIX(), new ToolArgumentParsers.MacroDefine_C_POSIX());

    List<ICLanguageSettingEntry> entries;

    final String more = " -g -MMD  -o MesonFiles/execut1.dir/util1.c.o"
        + " -c /testprojects/C-subsrc/src/src-sub/main.c";
    ICLanguageSettingEntry parsed;

    // generate response file
    final java.nio.file.Path dirP = Files.createTempDirectory("rfpt");
    final java.nio.file.Path cwdP = dirP.getParent();
    final java.nio.file.Path relRspP = dirP.getFileName().resolve(Paths.get("response.file.txt"));
    final java.nio.file.Path absRspP = cwdP.resolve(relRspP);

    final String incDirName = "an/include/dir";
    final String def1Name = "def1";
    final String def2Name = "def2";
    final String defName = "DEF_ON_COMMANDLINE";

    try (PrintWriter rspFilePw = new PrintWriter(
        Files.newOutputStream(absRspP, StandardOpenOption.WRITE, StandardOpenOption.CREATE));) {
      rspFilePw.printf(" -D%s=234 -I%s -D%s=987", def1Name, incDirName, def2Name);
      rspFilePw.close();
    }
    // @a/response/file.txt
    entries = testee.processArgs(new Path(cwdP.toString()), "@" + relRspP.toString() + " -D" + defName + more, null);
    assertEquals("#entries", 4, entries.size());
    parsed = entries.get(0);
    assertEquals("kind", ICSettingEntry.MACRO, parsed.getKind());
    assertEquals("name", def1Name, parsed.getName());
    parsed = entries.get(1);
    assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
    assertEquals("name", cwdP.resolve(incDirName).toString(), parsed.getName());
    parsed = entries.get(2);
    assertEquals("kind", ICSettingEntry.MACRO, parsed.getKind());
    assertEquals("name", def2Name, parsed.getName());
    parsed = entries.get(3);
    assertEquals("kind", ICSettingEntry.MACRO, parsed.getKind());
    assertEquals("name", defName, parsed.getName());


     // @ a/response.file.txt
    entries = testee.processArgs(new Path(cwdP.toString()), "@ " + relRspP.toString() + " -D" + defName + more, null);
    assertEquals("#entries", 4, entries.size());
    parsed = entries.get(0);
    assertEquals("kind", ICSettingEntry.MACRO, parsed.getKind());
    assertEquals("name", def1Name, parsed.getName());
    parsed = entries.get(1);
    assertEquals("kind", ICSettingEntry.INCLUDE_PATH, parsed.getKind());
    assertEquals("name", cwdP.resolve(incDirName).toString(), parsed.getName());
    parsed = entries.get(2);
    assertEquals("kind", ICSettingEntry.MACRO, parsed.getKind());
    assertEquals("name", def2Name, parsed.getName());
    parsed = entries.get(3);
    assertEquals("kind", ICSettingEntry.MACRO, parsed.getKind());
    assertEquals("name", defName, parsed.getName());

    Files.delete(absRspP);
  }

}
