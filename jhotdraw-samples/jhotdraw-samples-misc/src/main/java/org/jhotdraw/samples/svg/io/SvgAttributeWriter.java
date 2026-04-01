/*
 * @(#)SvgAttributeWriter.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.samples.svg.io;

import static org.jhotdraw.samples.svg.SVGAttributeKeys.*;
import static org.jhotdraw.samples.svg.SVGConstants.SVG_NAMESPACE;

import java.awt.BasicStroke;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys.WindingRule;
import org.jhotdraw.samples.svg.Gradient;
import org.w3c.dom.Element;

final class SvgAttributeWriter {

  interface GradientIdResolver {
    String resolveGradientId(Gradient gradient) throws IOException;
  }

  private static final HashMap<Integer, String> STROKE_LINEJOIN;

  static {
    STROKE_LINEJOIN = new HashMap<Integer, String>();
    STROKE_LINEJOIN.put(BasicStroke.JOIN_MITER, "miter");
    STROKE_LINEJOIN.put(BasicStroke.JOIN_ROUND, "round");
    STROKE_LINEJOIN.put(BasicStroke.JOIN_BEVEL, "bevel");
  }

  private static final HashMap<Integer, String> STROKE_LINECAP;

  static {
    STROKE_LINECAP = new HashMap<Integer, String>();
    STROKE_LINECAP.put(BasicStroke.CAP_BUTT, "butt");
    STROKE_LINECAP.put(BasicStroke.CAP_ROUND, "round");
    STROKE_LINECAP.put(BasicStroke.CAP_SQUARE, "square");
  }

  private final GradientIdResolver gradientIdResolver;

  SvgAttributeWriter(GradientIdResolver gradientIdResolver) {
    this.gradientIdResolver = gradientIdResolver;
  }

  void writeShapeAttributes(Element elem, Map<AttributeKey<?>, Object> attributes)
      throws IOException {
    Gradient gradient = FILL_GRADIENT.get(attributes);
    if (gradient != null) {
      String id = gradientIdResolver.resolveGradientId(gradient);
      writeAttribute(elem, "fill", "url(#" + id + ")", "#000");
    } else {
      writeAttribute(elem, "fill", SVGOutputFormat.toColor(FILL_COLOR.get(attributes)), "#000");
    }

    writeAttribute(elem, "fill-opacity", FILL_OPACITY.get(attributes), 1d);

    if (WINDING_RULE.get(attributes) != WindingRule.NON_ZERO) {
      writeAttribute(elem, "fill-rule", "evenodd", "nonzero");
    }

    gradient = STROKE_GRADIENT.get(attributes);
    if (gradient != null) {
      String id = gradientIdResolver.resolveGradientId(gradient);
      writeAttribute(elem, "stroke", "url(#" + id + ")", "none");
    } else {
      writeAttribute(elem, "stroke", SVGOutputFormat.toColor(STROKE_COLOR.get(attributes)), "none");
    }

    double[] dashes = STROKE_DASHES.get(attributes);
    if (dashes != null) {
      StringBuilder buf = new StringBuilder();
      for (int i = 0; i < dashes.length; i++) {
        if (i != 0) {
          buf.append(',');
        }
        buf.append(SVGOutputFormat.toNumber(dashes[i]));
      }
      writeAttribute(elem, "stroke-dasharray", buf.toString(), null);
    }

    writeAttribute(elem, "stroke-dashoffset", STROKE_DASH_PHASE.get(attributes), 0d);
    writeAttribute(elem, "stroke-linecap", STROKE_LINECAP.get(STROKE_CAP.get(attributes)), "butt");
    writeAttribute(
        elem, "stroke-linejoin", STROKE_LINEJOIN.get(STROKE_JOIN.get(attributes)), "miter");
    writeAttribute(elem, "stroke-miterlimit", STROKE_MITER_LIMIT.get(attributes), 4d);
    writeAttribute(elem, "stroke-opacity", STROKE_OPACITY.get(attributes), 1d);
    writeAttribute(elem, "stroke-width", STROKE_WIDTH.get(attributes), 1d);
  }

  void writeOpacityAttribute(Element elem, Map<AttributeKey<?>, Object> attributes) {
    writeAttribute(elem, "opacity", OPACITY.get(attributes), 1d);
  }

  void writeTransformAttribute(Element elem, Map<AttributeKey<?>, Object> attributes)
      throws IOException {
    AffineTransform transform = TRANSFORM.get(attributes);
    if (transform != null) {
      writeAttribute(elem, "transform", SVGOutputFormat.toTransform(transform), "none");
    }
  }

  void writeFontAttributes(Element elem, Map<AttributeKey<?>, Object> attributes) {
    writeAttribute(elem, "font-family", FONT_FACE.get(attributes).getFontName(), "Dialog");
    writeAttribute(elem, "font-size", FONT_SIZE.get(attributes), 0d);
    writeAttribute(
        elem, "font-style", (FONT_ITALIC.get(attributes)) ? "italic" : "normal", "normal");
    writeAttribute(elem, "font-variant", "normal", "normal");
    writeAttribute(elem, "font-weight", (FONT_BOLD.get(attributes)) ? "bold" : "normal", "normal");
    writeAttribute(
        elem, "text-decoration", (FONT_UNDERLINE.get(attributes)) ? "underline" : "none", "none");
  }

  void writeViewportAttributes(Element elem, Map<AttributeKey<?>, Object> attributes) {
    if (VIEWPORT_WIDTH.get(attributes) != null && VIEWPORT_HEIGHT.get(attributes) != null) {
      writeAttribute(elem, "width", SVGOutputFormat.toNumber(VIEWPORT_WIDTH.get(attributes)), null);
      writeAttribute(
          elem, "height", SVGOutputFormat.toNumber(VIEWPORT_HEIGHT.get(attributes)), null);
    }

    writeAttribute(
        elem, "viewport-fill", SVGOutputFormat.toColor(VIEWPORT_FILL.get(attributes)), "none");
    writeAttribute(elem, "viewport-fill-opacity", VIEWPORT_FILL_OPACITY.get(attributes), 1.0);
  }

  private void writeAttribute(Element elem, String name, String value, String defaultValue) {
    writeAttribute(elem, name, SVG_NAMESPACE, value, defaultValue);
  }

  private void writeAttribute(
      Element elem, String name, String namespace, String value, String defaultValue) {
    if (!value.equals(defaultValue)) {
      elem.setAttribute(name, value);
    }
  }

  private void writeAttribute(Element elem, String name, double value, double defaultValue) {
    writeAttribute(elem, name, SVG_NAMESPACE, value, defaultValue);
  }

  private void writeAttribute(
      Element elem, String name, String namespace, double value, double defaultValue) {
    if (value != defaultValue) {
      elem.setAttribute(name, SVGOutputFormat.toNumber(value));
    }
  }
}
