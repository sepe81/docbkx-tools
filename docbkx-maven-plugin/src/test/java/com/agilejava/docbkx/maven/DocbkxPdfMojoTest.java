/*
 * #%L
 * Docbkx Maven Plugin
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
package com.agilejava.docbkx.maven;

import java.io.File;

import org.apache.fop.apps.FopFactory;

import org.apache.maven.plugin.MojoExecutionException;

import org.codehaus.plexus.PlexusTestCase;

import com.agilejava.docbkx.maven.DocbkxPdfMojo;

/**
 * Tests for {@link AbstractFoMojo#createFopFactory()}.
 */
public class DocbkxPdfMojoTest extends PlexusTestCase {
  DocbkxPdfMojo mojo;

  public void setUp() throws Exception {
    super.setUp();
    mojo = new DocbkxPdfMojo();
  }

  /**
   * Default (inline template) config should produce a FopFactory without error.
   */
  public void testCreateFopFactoryDefault() throws MojoExecutionException {
    FopFactory factory = mojo.createFopFactory();
    assertNotNull("createFopFactory() should return a non-null FopFactory", factory);
  }

  /**
   * Invalid external config file should throw MojoExecutionException.
   */
  public void testCreateFopFactoryInvalidFile() {
    mojo.externalFOPConfiguration = new File("/doesnotexist/fop.xconf");
    try {
      mojo.createFopFactory();
      fail("Should have failed with an invalid external FOP configuration file");
    } catch (MojoExecutionException e) {
      // expected
    }
  }

  /**
   * Valid external config file should produce a FopFactory without error.
   */
  public void testCreateFopFactoryValidFile() throws MojoExecutionException {
    mojo.externalFOPConfiguration = new File(PlexusTestCase.getBasedir(), "src/test/resources/fop.xconf");
    FopFactory factory = mojo.createFopFactory();
    assertNotNull("createFopFactory() with valid file should return a non-null FopFactory", factory);
  }
}
