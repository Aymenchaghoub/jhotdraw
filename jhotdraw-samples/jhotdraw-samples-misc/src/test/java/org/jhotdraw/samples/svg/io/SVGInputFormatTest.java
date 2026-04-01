/*
 * Copyright (C) 2024 JHotDraw.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.jhotdraw.samples.svg.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.Drawing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit tests for {@link SVGInputFormat}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>Color parsing ({@code toColor}) with hex, rgb, named colors and edge cases</li>
 *   <li>Transform parsing ({@code toTransform}) with translate, scale, rotate</li>
 *   <li>Simple SVG document parsing through the public read API</li>
 * </ul>
 */
public class SVGInputFormatTest {

  private SVGInputFormat format;
  private Method toColorMethod;
  private Element dummyElement;

  @BeforeEach
  void setUp() throws Exception {
    format = new SVGInputFormat();

    // Access private toColor(Element, String) via reflection
    toColorMethod = SVGInputFormat.class.getDeclaredMethod("toColor", Element.class, String.class);
    toColorMethod.setAccessible(true);

    // Create a minimal DOM element for use in toColor calls
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();
    dummyElement = doc.createElement("rect");
  }

  /**
   * Invokes the private toColor method via reflection.
   */
  private Color invokeToColor(String value) throws Exception {
    return (Color) toColorMethod.invoke(format, dummyElement, value);
  }

  // =========================================================================
  // toColor — hex values
  // =========================================================================
  @Nested
  @DisplayName("toColor — hex color values")
  class HexColorTests {

    @Test
    @DisplayName("#ff0000 should parse as red")
    void hexSixDigitRed() throws Exception {
      Color result = invokeToColor("#ff0000");
      assertNotNull(result);
      assertEquals(255, result.getRed());
      assertEquals(0, result.getGreen());
      assertEquals(0, result.getBlue());
    }

    @Test
    @DisplayName("#00ff00 should parse as green")
    void hexSixDigitGreen() throws Exception {
      Color result = invokeToColor("#00ff00");
      assertNotNull(result);
      assertEquals(0, result.getRed());
      assertEquals(255, result.getGreen());
      assertEquals(0, result.getBlue());
    }

    @Test
    @DisplayName("#0000ff should parse as blue")
    void hexSixDigitBlue() throws Exception {
      Color result = invokeToColor("#0000ff");
      assertNotNull(result);
      assertEquals(0, result.getRed());
      assertEquals(0, result.getGreen());
      assertEquals(255, result.getBlue());
    }

    @Test
    @DisplayName("#000000 should parse as black")
    void hexSixDigitBlack() throws Exception {
      Color result = invokeToColor("#000000");
      assertNotNull(result);
      assertEquals(0, result.getRed());
      assertEquals(0, result.getGreen());
      assertEquals(0, result.getBlue());
    }

    @Test
    @DisplayName("#ffffff should parse as white")
    void hexSixDigitWhite() throws Exception {
      Color result = invokeToColor("#ffffff");
      assertNotNull(result);
      assertEquals(255, result.getRed());
      assertEquals(255, result.getGreen());
      assertEquals(255, result.getBlue());
    }
  }

  // =========================================================================
  // toColor — rgb() functional notation
  // =========================================================================
  @Nested
  @DisplayName("toColor — rgb() functional notation")
  class RgbColorTests {

    @Test
    @DisplayName("rgb(255,0,0) should parse as red")
    void rgbAbsoluteRed() throws Exception {
      Color result = invokeToColor("rgb(255,0,0)");
      assertNotNull(result);
      assertEquals(255, result.getRed());
      assertEquals(0, result.getGreen());
      assertEquals(0, result.getBlue());
    }

    @Test
    @DisplayName("rgb(0, 128, 255) should handle spaces and mid-range values")
    void rgbWithSpaces() throws Exception {
      Color result = invokeToColor("rgb(0, 128, 255)");
      assertNotNull(result);
      assertEquals(0, result.getRed());
      assertEquals(128, result.getGreen());
      assertEquals(255, result.getBlue());
    }
  }

  // =========================================================================
  // toColor — named SVG colors
  // =========================================================================
  @Nested
  @DisplayName("toColor — named SVG colors")
  class NamedColorTests {

    @Test
    @DisplayName("\"black\" should parse as Color.BLACK")
    void namedBlack() throws Exception {
      Color result = invokeToColor("black");
      assertNotNull(result);
      assertEquals(0, result.getRed());
      assertEquals(0, result.getGreen());
      assertEquals(0, result.getBlue());
    }

    @Test
    @DisplayName("\"white\" should parse as Color.WHITE")
    void namedWhite() throws Exception {
      Color result = invokeToColor("white");
      assertNotNull(result);
      assertEquals(255, result.getRed());
      assertEquals(255, result.getGreen());
      assertEquals(255, result.getBlue());
    }

    @Test
    @DisplayName("\"red\" should parse as pure red")
    void namedRed() throws Exception {
      Color result = invokeToColor("red");
      assertNotNull(result);
      assertEquals(255, result.getRed());
      assertEquals(0, result.getGreen());
      assertEquals(0, result.getBlue());
    }
  }

  // =========================================================================
  // toColor — edge cases / invalid values
  // =========================================================================
  @Nested
  @DisplayName("toColor — edge cases and invalid values")
  class EdgeCaseColorTests {

    @Test
    @DisplayName("null value should return null")
    void nullValue() throws Exception {
      Color result = invokeToColor(null);
      assertNull(result);
    }

    @Test
    @DisplayName("\"none\" should return null")
    void noneValue() throws Exception {
      Color result = invokeToColor("none");
      assertNull(result);
    }

    @Test
    @DisplayName("unknown color name should return null")
    void unknownColorName() throws Exception {
      Color result = invokeToColor("notacolor");
      assertNull(result);
    }

    @Test
    @DisplayName("url(...) value should return null")
    void urlValue() throws Exception {
      Color result = invokeToColor("url(#gradient1)");
      assertNull(result);
    }

    @Test
    @DisplayName("empty string should return null")
    void emptyString() throws Exception {
      Color result = invokeToColor("");
      assertNull(result);
    }
  }

  // =========================================================================
  // read(InputStream, Drawing, boolean) — simple SVG parsing
  // =========================================================================
  @Nested
  @DisplayName("read — SVG document parsing")
  class ReadSvgTests {

    @Test
    @DisplayName("minimal valid SVG should parse without exception")
    void parseMinimalSvg() throws Exception {
      String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"><g/></svg>";
      Drawing drawing = new DefaultDrawing();

      format.read(new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)), drawing, true);

      assertNotNull(drawing);
      assertThat(drawing.getChildren()).isEmpty();
    }

    @Test
    @DisplayName("malformed SVG should throw IOException")
    void parseMalformedSvg() {
      String malformedSvg = "<svg xmlns=\"http://www.w3.org/2000/svg\"><g></svg>";
      Drawing drawing = new DefaultDrawing();

      assertThrows(
          IOException.class,
          () -> format.read(
              new ByteArrayInputStream(malformedSvg.getBytes(StandardCharsets.UTF_8)),
              drawing,
              true));
    }
  }

  // =========================================================================
  // toTransform — public static method
  // =========================================================================
  @Nested
  @DisplayName("toTransform — SVG transform attribute parsing")
  class TransformTests {

    @Test
    @DisplayName("\"none\" should return identity transform")
    void noneTransform() throws Exception {
      AffineTransform t = SVGInputFormat.toTransform(dummyElement, "none");
      assertThat(t).isEqualTo(new AffineTransform());
    }

    @Test
    @DisplayName("null value should return identity transform")
    void nullTransform() throws Exception {
      AffineTransform t = SVGInputFormat.toTransform(dummyElement, null);
      assertThat(t).isEqualTo(new AffineTransform());
    }

    @Test
    @DisplayName("translate(10,20) should produce correct translation")
    void translateTransform() throws Exception {
      AffineTransform t = SVGInputFormat.toTransform(dummyElement, "translate(10,20)");
      assertEquals(10.0, t.getTranslateX(), 0.001);
      assertEquals(20.0, t.getTranslateY(), 0.001);
    }

    @Test
    @DisplayName("scale(2) should produce uniform scaling")
    void scaleTransform() throws Exception {
      AffineTransform t = SVGInputFormat.toTransform(dummyElement, "scale(2)");
      assertEquals(2.0, t.getScaleX(), 0.001);
      assertEquals(2.0, t.getScaleY(), 0.001);
    }

    @Test
    @DisplayName("scale(2,3) should produce non-uniform scaling")
    void scaleNonUniformTransform() throws Exception {
      AffineTransform t = SVGInputFormat.toTransform(dummyElement, "scale(2,3)");
      assertEquals(2.0, t.getScaleX(), 0.001);
      assertEquals(3.0, t.getScaleY(), 0.001);
    }
  }
}
