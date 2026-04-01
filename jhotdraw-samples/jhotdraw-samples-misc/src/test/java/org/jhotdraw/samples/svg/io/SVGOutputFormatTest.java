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
import static org.jhotdraw.samples.svg.SVGAttributeKeys.FILL_GRADIENT;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.samples.svg.LinearGradient;
import org.jhotdraw.samples.svg.figures.SVGBezierFigure;
import org.jhotdraw.samples.svg.figures.SVGPathFigure;
import org.jhotdraw.samples.svg.figures.SVGRectFigure;
import org.jhotdraw.samples.svg.figures.SVGTextFigure;
import org.jhotdraw.utils.geom.path.BezierPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Regression tests for {@link SVGOutputFormat}. */
public class SVGOutputFormatTest {

  @Test
  @DisplayName("write should export rectangles as rect elements")
  void writeRectFigure() throws Exception {
    Drawing drawing = new DefaultDrawing();
    drawing.add(new SVGRectFigure(10, 20, 30, 40));

    String svg = writeSvg(drawing);

    assertThat(svg).contains("<rect");
    assertThat(svg).contains("x=\"10\"");
    assertThat(svg).contains("y=\"20\"");
  }

  @Test
  @DisplayName("write should export multi-segment paths as path elements")
  void writePathFigure() throws Exception {
    Drawing drawing = new DefaultDrawing();

    SVGPathFigure pathFigure = new SVGPathFigure(false);
    SVGBezierFigure segmentOne = createSegment(0, 0, 10, 10);
    SVGBezierFigure segmentTwo = createSegment(10, 10, 20, 5);
    pathFigure.add(segmentOne);
    pathFigure.add(segmentTwo);
    drawing.add(pathFigure);

    String svg = writeSvg(drawing);

    assertThat(svg).contains("<path");
    assertThat(svg).contains("d=\"");
  }

  @Test
  @DisplayName("write should export text figures as text elements")
  void writeTextFigure() throws Exception {
    Drawing drawing = new DefaultDrawing();
    drawing.add(new SVGTextFigure("hello-svg"));

    String svg = writeSvg(drawing);

    assertThat(svg).contains("<text");
    assertThat(svg).contains("hello-svg");
  }

  @Test
  @DisplayName("write should reuse gradient definitions for equivalent figures")
  void writeReusesGradientDefinition() throws Exception {
    Drawing drawing = new DefaultDrawing();
    LinearGradient sharedGradient = new LinearGradient(
        0d,
        0d,
        1d,
        1d,
        new double[] {0d, 1d},
        new Color[] {Color.RED, Color.BLUE},
        new double[] {1d, 1d},
        true,
        new AffineTransform());

    SVGRectFigure first = new SVGRectFigure(0, 0, 20, 20);
    first.attr().set(FILL_GRADIENT, sharedGradient);
    drawing.add(first);

    SVGRectFigure second = new SVGRectFigure(30, 0, 20, 20);
    second.attr().set(FILL_GRADIENT, sharedGradient);
    drawing.add(second);

    String svg = writeSvg(drawing);

    assertThat(countOccurrences(svg, "<linearGradient")).isEqualTo(1);
    assertThat(countOccurrences(svg, "fill=\"url(#")).isEqualTo(2);
  }

  private static SVGBezierFigure createSegment(double x1, double y1, double x2, double y2) {
    SVGBezierFigure segment = new SVGBezierFigure(false);
    segment.addNode(new BezierPath.Node(x1, y1));
    segment.addNode(new BezierPath.Node(x2, y2));
    return segment;
  }

  private static String writeSvg(Drawing drawing) throws IOException {
    SVGOutputFormat outputFormat = new SVGOutputFormat();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    outputFormat.write(out, drawing);
    return out.toString(StandardCharsets.UTF_8.name());
  }

  private static int countOccurrences(String text, String token) {
    int count = 0;
    int index = 0;
    while ((index = text.indexOf(token, index)) != -1) {
      count++;
      index += token.length();
    }
    return count;
  }
}
