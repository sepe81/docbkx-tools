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

import java.nio.file.Files;

import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FopFactory;

import org.apache.maven.plugin.MojoExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.agilejava.docbkx.maven.DocbkxPdfMojo;

/**
 * Tests for {@link AbstractFoMojo#createFopFactory()} and the inline FOP
 * configuration builder.
 */
public class DocbkxPdfMojoTest {
  DocbkxPdfMojo mojo;

  @BeforeEach
  void setUp() {
    mojo = new DocbkxPdfMojo();
  }

  /**
   * Default (inline template) config should produce a FopFactory without error.
   */
  @Test
  void testCreateFopFactoryDefault() throws MojoExecutionException {
    FopFactory factory = mojo.createFopFactory();
    assertNotNull(factory, "createFopFactory() should return a non-null FopFactory");
  }

  /**
   * Invalid external config file should throw MojoExecutionException.
   */
  @Test
  void testCreateFopFactoryInvalidFile() {
    mojo.externalFOPConfiguration = new File("/doesnotexist/fop.xconf");
    assertThrows(MojoExecutionException.class, () -> mojo.createFopFactory(),
        "Should have failed with an invalid external FOP configuration file");
  }

  /**
   * Valid external config file should produce a FopFactory without error.
   */
  @Test
  void testCreateFopFactoryValidFile() throws MojoExecutionException {
    mojo.externalFOPConfiguration = new File(getBasedir(), "src/test/resources/fop.xconf");
    FopFactory factory = mojo.createFopFactory();
    assertNotNull(factory, "createFopFactory() with valid file should return a non-null FopFactory");
  }

  /**
   * When no resolutions are set, the generated inline config must not contain
   * stray {@code <target-resolution>} / {@code <source-resolution>} elements.
   */
  @Test
  void testBuildInlineConfigDefaultsOmitResolutions() throws Exception {
    String xml = mojo.buildInlineConfig();
    assertNotNull(xml, "buildInlineConfig() must not return null");
    assertFalse(
        xml.contains("<target-resolution>"),
        "Default config must not contain <target-resolution>: " + xml);
    assertFalse(
        xml.contains("<source-resolution>"),
        "Default config must not contain <source-resolution>: " + xml);
  }

  /**
   * Setting {@code targetResolution} / {@code sourceResolution} must thread
   * the values into the generated inline FOP configuration XML. This guards
   * against a silent regression where mojo parameters stop reaching FOP.
   */
  @Test
  void testBuildInlineConfigPropagatesResolutions() throws Exception {
    mojo.targetResolution = 600;
    mojo.sourceResolution = 150;
    String xml = mojo.buildInlineConfig();
    assertTrue(
        xml.contains("600"),
        "Inline config must contain target-resolution 600: " + xml);
    assertTrue(
        xml.contains("150"),
        "Inline config must contain source-resolution 150: " + xml);
  }

  /**
   * The inline-config branch of {@link AbstractFoMojo#createFopFactory()}
   * must apply {@link AbstractFoMojo#baseUrl} as the factory base URI.
   * Regression guard for the case where consumers rely on FOP resolving
   * relative {@code <fo:external-graphic src="..."/>} URIs against
   * the source XML directory.
   */
  @Test
  void testCreateFopFactoryAppliesBaseUrl() throws Exception {
    assertResolvesAgainstBaseUrl();
  }

  /**
   * The external-config branch must apply {@link AbstractFoMojo#baseUrl} as well
   * when the configuration file does not declare its own {@code <base>} element.
   */
  @Test
  void testCreateFopFactoryExternalConfigAppliesBaseUrl() throws Exception {
    mojo.externalFOPConfiguration = new File(getBasedir(), "src/test/resources/fop-no-base.xconf");
    assertResolvesAgainstBaseUrl();
  }

  private static String getBasedir() {
    return System.getProperty("basedir", System.getProperty("user.dir"));
  }

  private void assertResolvesAgainstBaseUrl() throws Exception {
    File tmp = Files.createTempDirectory("docbkx-base-").toFile();
    File probe = new File(tmp, "probe.png");
    tmp.deleteOnExit();
    probe.deleteOnExit();
    try {
      // Drop a probe file so FOP's resource resolver can find a real target.
      assertTrue(probe.createNewFile(), "Could not create probe file");

      mojo.baseUrl = tmp.toURI().toString();
      FopFactory factory = mojo.createFopFactory();
      assertNotNull(factory);

      // Resolve a relative URI through the factory's user agent and verify
      // it is anchored under the configured baseUrl.
      StreamSource resolved = factory.newFOUserAgent().resolveURI("probe.png");
      assertNotNull(
          resolved,
          "FOUserAgent.resolveURI should resolve relative URI against baseUrl="
              + mojo.baseUrl);
      assertNotNull(resolved.getSystemId());
      assertTrue(
          resolved.getSystemId().startsWith(tmp.toURI().toString()),
          "Resolved URI must be anchored under baseUrl. Expected prefix: "
              + tmp.toURI() + " but got: " + resolved.getSystemId());
    } finally {
      probe.delete();
      tmp.delete();
    }
  }

  /**
   * The log4j level names accepted by {@code fopLogLevel} must map to the matching
   * java.util.logging levels; unknown names fall back to WARNING.
   */
  @Test
  void testToJulLevel() {
    assertEquals(java.util.logging.Level.OFF, AbstractFoMojo.toJulLevel("OFF"));
    assertEquals(java.util.logging.Level.SEVERE, AbstractFoMojo.toJulLevel("FATAL"));
    assertEquals(java.util.logging.Level.SEVERE, AbstractFoMojo.toJulLevel("ERROR"));
    assertEquals(java.util.logging.Level.WARNING, AbstractFoMojo.toJulLevel("WARN"));
    assertEquals(java.util.logging.Level.INFO, AbstractFoMojo.toJulLevel("INFO"));
    assertEquals(java.util.logging.Level.FINE, AbstractFoMojo.toJulLevel("DEBUG"));
    assertEquals(java.util.logging.Level.FINER, AbstractFoMojo.toJulLevel("TRACE"));
    assertEquals(java.util.logging.Level.ALL, AbstractFoMojo.toJulLevel("ALL"));
    assertEquals(java.util.logging.Level.WARNING, AbstractFoMojo.toJulLevel("nonsense"));
  }
}
