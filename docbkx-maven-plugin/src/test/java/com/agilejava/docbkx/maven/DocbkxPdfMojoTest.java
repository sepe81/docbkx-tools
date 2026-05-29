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
 * Tests for {@link AbstractFoMojo#createFopFactory()} and the inline FOP
 * configuration builder.
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

  /**
   * When no resolutions are set, the generated inline config must not contain
   * stray {@code <target-resolution>} / {@code <source-resolution>} elements.
   */
  public void testBuildInlineConfigDefaultsOmitResolutions() throws Exception {
    String xml = mojo.buildInlineConfig();
    assertNotNull("buildInlineConfig() must not return null", xml);
    assertFalse(
        "Default config must not contain <target-resolution>: " + xml,
        xml.contains("<target-resolution>"));
    assertFalse(
        "Default config must not contain <source-resolution>: " + xml,
        xml.contains("<source-resolution>"));
  }

  /**
   * Setting {@code targetResolution} / {@code sourceResolution} must thread
   * the values into the generated inline FOP configuration XML. This guards
   * against a silent regression where mojo parameters stop reaching FOP.
   */
  public void testBuildInlineConfigPropagatesResolutions() throws Exception {
    mojo.targetResolution = 600;
    mojo.sourceResolution = 150;
    String xml = mojo.buildInlineConfig();
    assertTrue(
        "Inline config must contain target-resolution 600: " + xml,
        xml.contains("600"));
    assertTrue(
        "Inline config must contain source-resolution 150: " + xml,
        xml.contains("150"));
  }

  /**
   * The inline-config branch of {@link AbstractFoMojo#createFopFactory()}
   * must apply {@link AbstractFoMojo#baseUrl} as the factory base URI.
   * Regression guard for the case where consumers rely on FOP resolving
   * relative {@code <fo:external-graphic src=&quot;...&quot;/>} URIs against
   * the source XML directory.
   */
  public void testCreateFopFactoryAppliesBaseUrl() throws Exception {
    File tmp = File.createTempFile("docbkx-base-", "");
    tmp.delete();
    assertTrue("Could not create scratch dir", tmp.mkdirs());
    try {
      // Drop a probe file so FOP's resource resolver can find a real target.
      File probe = new File(tmp, "probe.png");
      assertTrue("Could not create probe file", probe.createNewFile());

      mojo.baseUrl = tmp.toURI().toString();
      FopFactory factory = mojo.createFopFactory();
      assertNotNull(factory);

      // Resolve a relative URI through the factory's user agent and verify
      // it is anchored under the configured baseUrl.
      javax.xml.transform.stream.StreamSource resolved =
          factory.newFOUserAgent().resolveURI("probe.png");
      assertNotNull(
          "FOUserAgent.resolveURI should resolve relative URI against baseUrl="
              + mojo.baseUrl,
          resolved);
      assertNotNull(resolved.getSystemId());
      assertTrue(
          "Resolved URI must be anchored under baseUrl. Expected prefix: "
              + tmp.toURI() + " but got: " + resolved.getSystemId(),
          resolved.getSystemId().startsWith(tmp.toURI().toString()));
    } finally {
      // Best-effort cleanup; ignored if files persist.
      new File(tmp, "probe.png").delete();
      tmp.delete();
    }
  }
}
