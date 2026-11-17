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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DOCUMENT ME!
 *
 * @author Cedric Pronzato
 */
public class FontmetricsMojoTest {
  /**
   * DOCUMENT ME!
   *
   * @param mojo DOCUMENT ME!
   */
  void initMojoDefault(FontmetricsMojo mojo) {
    mojo.sourceDirectory = new File(getBasedir(), "src/test/resources/fonts/");
    assertTrue(mojo.sourceDirectory.isDirectory(), "Default sourceDirectory should be valid");
    mojo.targetDirectory = new File(getBasedir(), "target/test/fonts/");
    mojo.targetDirectory.mkdirs();
  }

  /**
   * DOCUMENT ME!
   *
   * @param mojo DOCUMENT ME!
   */
  void validateGen(FontmetricsMojo mojo) {
    File expectedGen = new File(mojo.targetDirectory, "Pecita-metrics.xml");
    assertTrue(expectedGen.exists(), "metrics file not generated: " + expectedGen.getAbsolutePath());
    // space in font name
    expectedGen = new File(mojo.targetDirectory, "Pe cita-metrics.xml");
    assertTrue(expectedGen.exists(), "metrics file not generated: " + expectedGen.getAbsolutePath());
  }

  /**
   * DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  @Test
  void testFontmetricsGen() throws Exception {
    FontmetricsMojo mojo = new FontmetricsMojo();
    initMojoDefault(mojo);
    mojo.execute();

    validateGen(mojo);
  }

  /**
   * DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  @Test
  void testFontmetricsGenSpaceDir() throws Exception {
    FontmetricsMojo mojo = new FontmetricsMojo();
    initMojoDefault(mojo);
    mojo.targetDirectory = new File(getBasedir(), "target/test/fon ts/");
    mojo.targetDirectory.mkdirs();
    mojo.execute();

    validateGen(mojo);
  }

  private static String getBasedir() {
    return System.getProperty("basedir", System.getProperty("user.dir"));
  }
}
