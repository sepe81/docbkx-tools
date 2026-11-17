/*
 * #%L
 * Docbkx FOP Support
 * %%
 * Copyright (C) 2006 - 2014 Wilfred Springer, Cedric Pronzato
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.agilejava.docbkx.support.fop;

import java.io.File;

import java.security.Permission;

import org.apache.fop.fonts.apps.TTFReader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * DOCUMENT ME!
 *
 * @author Cedric Pronzato
 */
// System.setSecurityManager() throws UnsupportedOperationException as of Java 24
@DisabledForJreRange(min = JRE.JAVA_24)
public class FOPReaderExitTest {
  /**
   * DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  @BeforeEach
  void setUp() {
    System.setSecurityManager(new NoExitSecurityManager());
  }

  /**
   * DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  @AfterEach
  void tearDown() {
    System.setSecurityManager(null); // or save and restore original
  }

  /**
   * DOCUMENT ME!
   *
   * @param mojo DOCUMENT ME!
   */
  void initMojoDefault(FontmetricsMojo mojo) {
    mojo.sourceDirectory = new File(getBasedir(), "src/test/resources/fontsko/");
    assertTrue(mojo.sourceDirectory.isDirectory(), "Default sourceDirectory should be valid");
    mojo.targetDirectory = new File(getBasedir(), "target/test/fontsko/");
    mojo.targetDirectory.mkdirs();
  }

  /**
   * DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  @Test
  void testFopExistsOnInvalidFont() throws Exception {
    File font = new File(getBasedir(), "src/test/resources/fontsko/test.ttf");
    File metrics = new File(getBasedir(), "target/test/fontsko/test-metrics.xml");
    metrics.getParentFile().mkdirs();

    try {
      TTFReader.main(new String[] { font.getCanonicalPath(), metrics.getCanonicalPath() });
      fail("TTFreader should have fail in loading corrupted font");
    } catch (ExitException e) {
      assertEquals(-1, e.status, "Exit status");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  @Test
  void testFontmetricsMojoOnInvalidFont() throws Exception {
    FontmetricsMojo mojo = new FontmetricsMojo();
    initMojoDefault(mojo);

    try {
      mojo.execute();

      File expectedGen = new File(mojo.targetDirectory, "test-metrics.xml");
      assertTrue(!expectedGen.exists(), "metrics file not generated: " + expectedGen.getAbsolutePath());
    } catch (ExitException e) {
      fail("FontmetricsMojo should not exits");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  @Test
  void testExit() throws Exception {
    try {
      System.exit(42);
    } catch (ExitException e) {
      assertEquals(42, e.status, "Exit status");
    }
  }

  private static String getBasedir() {
    return System.getProperty("basedir", System.getProperty("user.dir"));
  }

  //
  // classes to intercept System.exit()
  //
  protected static class ExitException extends SecurityException {
    public final int status;

    public ExitException(int status) {
      super("There is no escape!");
      this.status = status;
    }
  }

  private static class NoExitSecurityManager extends SecurityManager {
    public void checkPermission(Permission perm) {
      // allow anything.
    }

    public void checkPermission(Permission perm, Object context) {
      // allow anything.
    }

    public void checkExit(int status) {
      super.checkExit(status);
      throw new ExitException(status);
    }
  }
}
