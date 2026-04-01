/*
 * @(#)SVGOutputFormat.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.samples.svg.io;

import static org.jhotdraw.samples.svg.SVGAttributeKeys.*;
import static org.jhotdraw.samples.svg.SVGConstants.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.geom.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.jhotdraw.datatransfer.InputStreamTransferable;
import org.jhotdraw.draw.*;
import org.jhotdraw.draw.figure.BezierFigure;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.io.OutputFormat;
import org.jhotdraw.samples.svg.Gradient;
import org.jhotdraw.samples.svg.LinearGradient;
import org.jhotdraw.samples.svg.RadialGradient;
import org.jhotdraw.samples.svg.figures.SVGEllipseFigure;
import org.jhotdraw.samples.svg.figures.SVGGroupFigure;
import org.jhotdraw.samples.svg.figures.SVGImageFigure;
import org.jhotdraw.samples.svg.figures.SVGPathFigure;
import org.jhotdraw.samples.svg.figures.SVGRectFigure;
import org.jhotdraw.samples.svg.figures.SVGTextAreaFigure;
import org.jhotdraw.samples.svg.figures.SVGTextFigure;
import org.jhotdraw.utils.geom.path.BezierPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** An output format for storing drawings as Scalable Vector Graphics SVG Tiny 1.2. */
public class SVGOutputFormat implements OutputFormat {

  /** This is a counter used to create the next unique identification. */
  private int nextId;

  /** In this hash map we store all elements to which we have assigned an id. */
  private HashMap<Element, String> identifiedElements;

  /** This element holds all definitions of the SVG file. */
  private Element defs;

  /** Holds the document that is currently being written. */
  private Element document;

  /** Maps gradients to ID's. We use this, so that we need to store the same gradient only once. */
  private HashMap<Gradient, String> gradientToIDMap;

  /** Delegates figure serialization to specialized writers. */
  private final java.util.List<SvgFigureWriter> figureWriters;

  /** Encapsulates serialization of shared SVG attributes. */
  private final SvgAttributeWriter attributeWriter;

  /** Set this to true for pretty printing. */
  private boolean isPrettyPrint;

  /**
   * Set this variable to true if values should be written with float precision instead with double
   * precision. Float precision is less accurate then double precision, but it uses less storage
   * space.
   */
  private static final boolean IS_FLOAT_PRECISION = true;

  public SVGOutputFormat() {
    figureWriters = DefaultSvgFigureWriters.createWriters();
    attributeWriter = new SvgAttributeWriter(this::resolveGradientId);
  }

  public javax.swing.filechooser.FileFilter getFileFilter() {
    return new FileNameExtensionFilter("Scalable Vector Graphics (SVG)", "svg");
  }

  public JComponent getOutputFormatAccessory() {
    return null;
  }

  public void setPrettyPrint(boolean newValue) {
    isPrettyPrint = newValue;
  }

  public boolean isPrettyPrint() {
    return isPrettyPrint;
  }

  protected void writeElement(Element parent, Figure f) throws IOException {
    // Write link attribute as encosing "a" element
    if (f.attr().get(LINK) != null && f.attr().get(LINK).trim().length() > 0) {
      Element aElement = parent.getOwnerDocument().createElement("a");
      aElement.setAttribute("xlink:href", f.attr().get(LINK));
      if (f.attr().get(LINK_TARGET) != null && f.attr().get(LINK).trim().length() > 0) {
        aElement.setAttribute("target", f.attr().get(LINK_TARGET));
      }
      parent.appendChild(aElement);
      parent = aElement;
    }
    for (SvgFigureWriter writer : figureWriters) {
      if (writer.supports(f)) {
        writer.write(this, parent, f);
        return;
      }
    }
    System.out.println("Unable to write: " + f);
  }

  protected void writeCircleElement(Element parent, SVGEllipseFigure f) throws IOException {
    parent.appendChild(createCircle(
        document,
        f.getX() + f.getWidth() / 2d,
        f.getY() + f.getHeight() / 2d,
        f.getWidth() / 2d,
        f.attr().getAttributes()));
  }

  protected Element createCircle(
      Element doc, double cx, double cy, double r, Map<AttributeKey<?>, Object> attributes)
      throws IOException {
    Element elem = doc.getOwnerDocument().createElement("circle");
    writeAttribute(elem, "cx", cx, 0d);
    writeAttribute(elem, "cy", cy, 0d);
    writeAttribute(elem, "r", r, 0d);
    writeShapeAttributes(elem, attributes);
    writeOpacityAttribute(elem, attributes);
    writeTransformAttribute(elem, attributes);
    return elem;
  }

  protected Element createG(Element doc, Map<AttributeKey<?>, Object> attributes)
      throws IOException {
    Element elem = doc.getOwnerDocument().createElement("g");
    writeOpacityAttribute(elem, attributes);
    return elem;
  }

  protected Element createLinearGradient(
      Element doc,
      double x1,
      double y1,
      double x2,
      double y2,
      double[] stopOffsets,
      Color[] stopColors,
      double[] stopOpacities,
      boolean isRelativeToFigureBounds,
      AffineTransform transform)
      throws IOException {
    Element elem = doc.getOwnerDocument().createElement("linearGradient");
    writeAttribute(elem, "x1", toNumber(x1), "0");
    writeAttribute(elem, "y1", toNumber(y1), "0");
    writeAttribute(elem, "x2", toNumber(x2), "1");
    writeAttribute(elem, "y2", toNumber(y2), "0");
    writeAttribute(
        elem,
        "gradientUnits",
        (isRelativeToFigureBounds) ? "objectBoundingBox" : "userSpaceOnUse",
        "objectBoundingBox");
    writeAttribute(elem, "gradientTransform", toTransform(transform), "none");
    for (int i = 0; i < stopOffsets.length; i++) {
      Element stop = doc.getOwnerDocument().createElement("stop");
      writeAttribute(stop, "offset", toNumber(stopOffsets[i]), null);
      writeAttribute(stop, "stop-color", toColor(stopColors[i]), null);
      writeAttribute(stop, "stop-opacity", toNumber(stopOpacities[i]), "1");
      elem.appendChild(stop);
    }
    return elem;
  }

  protected Element createRadialGradient(
      Element doc,
      double cx,
      double cy,
      double fx,
      double fy,
      double r,
      double[] stopOffsets,
      Color[] stopColors,
      double[] stopOpacities,
      boolean isRelativeToFigureBounds,
      AffineTransform transform)
      throws IOException {
    Element elem = doc.getOwnerDocument().createElement("radialGradient");
    writeAttribute(elem, "cx", toNumber(cx), "0.5");
    writeAttribute(elem, "cy", toNumber(cy), "0.5");
    writeAttribute(elem, "fx", toNumber(fx), toNumber(cx));
    writeAttribute(elem, "fy", toNumber(fy), toNumber(cy));
    writeAttribute(elem, "r", toNumber(r), "0.5");
    writeAttribute(
        elem,
        "gradientUnits",
        (isRelativeToFigureBounds) ? "objectBoundingBox" : "userSpaceOnUse",
        "objectBoundingBox");
    writeAttribute(elem, "gradientTransform", toTransform(transform), "none");
    for (int i = 0; i < stopOffsets.length; i++) {
      Element stop = doc.getOwnerDocument().createElement("stop");
      writeAttribute(stop, "offset", toNumber(stopOffsets[i]), null);
      writeAttribute(stop, "stop-color", toColor(stopColors[i]), null);
      writeAttribute(stop, "stop-opacity", toNumber(stopOpacities[i]), "1");
      elem.appendChild(stop);
    }
    return elem;
  }

  private String resolveGradientId(Gradient gradient) throws IOException {
    if (gradientToIDMap.containsKey(gradient)) {
      return gradientToIDMap.get(gradient);
    }

    Element gradientElem;
    if (gradient instanceof LinearGradient) {
      LinearGradient lg = (LinearGradient) gradient;
      gradientElem = createLinearGradient(
          document,
          lg.getX1(),
          lg.getY1(),
          lg.getX2(),
          lg.getY2(),
          lg.getStopOffsets(),
          lg.getStopColors(),
          lg.getStopOpacities(),
          lg.isRelativeToFigureBounds(),
          lg.getTransform());
    } else {
      RadialGradient rg = (RadialGradient) gradient;
      gradientElem = createRadialGradient(
          document,
          rg.getCX(),
          rg.getCY(),
          rg.getFX(),
          rg.getFY(),
          rg.getR(),
          rg.getStopOffsets(),
          rg.getStopColors(),
          rg.getStopOpacities(),
          rg.isRelativeToFigureBounds(),
          rg.getTransform());
    }

    String id = getId(gradientElem);
    gradientElem.setAttributeNS("xml", "id", id);
    defs.appendChild(gradientElem);
    gradientToIDMap.put(gradient, id);
    return id;
  }

  protected void writeEllipseElement(Element parent, SVGEllipseFigure f) throws IOException {
    parent.appendChild(createEllipse(
        document,
        f.getX() + f.getWidth() / 2d,
        f.getY() + f.getHeight() / 2d,
        f.getWidth() / 2d,
        f.getHeight() / 2d,
        f.attr().getAttributes()));
  }

  protected Element createEllipse(
      Element doc,
      double cx,
      double cy,
      double rx,
      double ry,
      Map<AttributeKey<?>, Object> attributes)
      throws IOException {
    Element elem = doc.getOwnerDocument().createElement("ellipse");
    writeAttribute(elem, "cx", cx, 0d);
    writeAttribute(elem, "cy", cy, 0d);
    writeAttribute(elem, "rx", rx, 0d);
    writeAttribute(elem, "ry", ry, 0d);
    writeShapeAttributes(elem, attributes);
    writeOpacityAttribute(elem, attributes);
    writeTransformAttribute(elem, attributes);
    return elem;
  }

  protected void writeGElement(Element parent, SVGGroupFigure f) throws IOException {
    Element elem = createG(document, f.attr().getAttributes());
    for (Figure child : f.getChildren()) {
      writeElement(elem, child);
    }
    parent.appendChild(elem);
  }

  protected void writeImageElement(Element parent, SVGImageFigure f) throws IOException {
    parent.appendChild(createImage(
        document,
        f.getX(),
        f.getY(),
        f.getWidth(),
        f.getHeight(),
        f.getImageData(),
        f.attr().getAttributes()));
  }

  protected Element createImage(
      Element doc,
      double x,
      double y,
      double w,
      double h,
      byte[] imageData,
      Map<AttributeKey<?>, Object> attributes)
      throws IOException {
    Element elem = doc.getOwnerDocument().createElement("image");
    writeAttribute(elem, "x", x, 0d);
    writeAttribute(elem, "y", y, 0d);
    writeAttribute(elem, "width", w, 0d);
    writeAttribute(elem, "height", h, 0d);
    writeAttribute(
        elem,
        "xlink:href",
        "data:image;base64,"
            + Base64.getMimeEncoder(76, new byte[] {'\n'}).encodeToString(imageData),
        "");
    writeOpacityAttribute(elem, attributes);
    writeTransformAttribute(elem, attributes);
    return elem;
  }

  protected void writePathElement(Element parent, SVGPathFigure f) throws IOException {
    BezierPath[] beziers = new BezierPath[f.getChildCount()];
    for (int i = 0; i < beziers.length; i++) {
      beziers[i] = ((BezierFigure) f.getChild(i)).getBezierPath();
    }
    parent.appendChild(createPath(document, beziers, f.attr().getAttributes()));
  }

  protected Element createPath(
      Element doc, BezierPath[] beziers, Map<AttributeKey<?>, Object> attributes)
      throws IOException {
    Element elem = doc.getOwnerDocument().createElement("path");
    writeShapeAttributes(elem, attributes);
    writeOpacityAttribute(elem, attributes);
    writeTransformAttribute(elem, attributes);
    writeAttribute(elem, "d", toPath(beziers), null);
    return elem;
  }

  protected void writePolygonElement(Element parent, SVGPathFigure f) throws IOException {
    LinkedList<Point2D.Double> points = new LinkedList<Point2D.Double>();
    for (int i = 0, n = f.getChildCount(); i < n; i++) {
      BezierPath bezier = ((BezierFigure) f.getChild(i)).getBezierPath();
      for (BezierPath.Node node : bezier.nodes()) {
        points.add(new Point2D.Double(node.x[0], node.y[0]));
      }
    }
    parent.appendChild(createPolygon(
        document, points.toArray(new Point2D.Double[points.size()]), f.attr().getAttributes()));
  }

  protected Element createPolygon(
      Element doc, Point2D.Double[] points, Map<AttributeKey<?>, Object> attributes)
      throws IOException {
    Element elem = doc.getOwnerDocument().createElement("polygon");
    writeAttribute(elem, "points", toPoints(points), null);
    writeShapeAttributes(elem, attributes);
    writeOpacityAttribute(elem, attributes);
    writeTransformAttribute(elem, attributes);
    return elem;
  }

  protected void writePolylineElement(Element parent, SVGPathFigure f) throws IOException {
    LinkedList<Point2D.Double> points = new LinkedList<Point2D.Double>();
    for (int i = 0, n = f.getChildCount(); i < n; i++) {
      BezierPath bezier = ((BezierFigure) f.getChild(i)).getBezierPath();
      for (BezierPath.Node node : bezier.nodes()) {
        points.add(new Point2D.Double(node.x[0], node.y[0]));
      }
    }
    parent.appendChild(createPolyline(
        document, points.toArray(new Point2D.Double[points.size()]), f.attr().getAttributes()));
  }

  protected Element createPolyline(
      Element doc, Point2D.Double[] points, Map<AttributeKey<?>, Object> attributes)
      throws IOException {
    Element elem = doc.getOwnerDocument().createElement("polyline");
    writeAttribute(elem, "points", toPoints(points), null);
    writeShapeAttributes(elem, attributes);
    writeOpacityAttribute(elem, attributes);
    writeTransformAttribute(elem, attributes);
    return elem;
  }

  protected void writeLineElement(Element parent, SVGPathFigure f) throws IOException {
    BezierFigure bezier = (BezierFigure) f.getChild(0);
    parent.appendChild(createLine(
        document,
        bezier.getNode(0).x[0],
        bezier.getNode(0).y[0],
        bezier.getNode(1).x[0],
        bezier.getNode(1).y[0],
        f.attr().getAttributes()));
  }

  protected Element createLine(
      Element doc,
      double x1,
      double y1,
      double x2,
      double y2,
      Map<AttributeKey<?>, Object> attributes)
      throws IOException {
    Element elem = doc.getOwnerDocument().createElement("line");
    writeAttribute(elem, "x1", x1, 0d);
    writeAttribute(elem, "y1", y1, 0d);
    writeAttribute(elem, "x2", x2, 0d);
    writeAttribute(elem, "y2", y2, 0d);
    writeShapeAttributes(elem, attributes);
    writeOpacityAttribute(elem, attributes);
    writeTransformAttribute(elem, attributes);
    return elem;
  }

  protected void writeRectElement(Element parent, SVGRectFigure f) throws IOException {
    parent.appendChild(createRect(
        document,
        f.getX(),
        f.getY(),
        f.getWidth(),
        f.getHeight(),
        f.getArcWidth(),
        f.getArcHeight(),
        f.attr().getAttributes()));
  }

  protected Element createRect(
      Element doc,
      double x,
      double y,
      double width,
      double height,
      double rx,
      double ry,
      Map<AttributeKey<?>, Object> attributes)
      throws IOException {
    Element elem = doc.getOwnerDocument().createElement("rect");
    writeAttribute(elem, "x", x, 0d);
    writeAttribute(elem, "y", y, 0d);
    writeAttribute(elem, "width", width, 0d);
    writeAttribute(elem, "height", height, 0d);
    writeAttribute(elem, "rx", rx, 0d);
    writeAttribute(elem, "ry", ry, 0d);
    writeShapeAttributes(elem, attributes);
    writeOpacityAttribute(elem, attributes);
    writeTransformAttribute(elem, attributes);
    return elem;
  }

  protected void writeTextElement(Element parent, SVGTextFigure f) throws IOException {
    DefaultStyledDocument styledDoc = new DefaultStyledDocument();
    try {
      styledDoc.insertString(0, f.getText(), null);
    } catch (BadLocationException e) {
      InternalError error = new InternalError(e.getMessage());
      error.initCause(e);
      throw error;
    }
    parent.appendChild(createText(
        document, f.getCoordinates(), f.getRotates(), styledDoc, f.attr().getAttributes()));
  }

  protected Element createText(
      Element doc,
      Point2D.Double[] coordinates,
      double[] rotate,
      StyledDocument text,
      Map<AttributeKey<?>, Object> attributes)
      throws IOException {
    Element elem = doc.getOwnerDocument().createElement("text");
    StringBuilder bufX = new StringBuilder();
    StringBuilder bufY = new StringBuilder();
    for (int i = 0; i < coordinates.length; i++) {
      if (i != 0) {
        bufX.append(',');
        bufY.append(',');
      }
      bufX.append(toNumber(coordinates[i].getX()));
      bufY.append(toNumber(coordinates[i].getY()));
    }
    StringBuilder bufR = new StringBuilder();
    if (rotate != null) {
      for (int i = 0; i < rotate.length; i++) {
        if (i != 0) {
          bufR.append(',');
        }
        bufR.append(toNumber(rotate[i]));
      }
    }
    writeAttribute(elem, "x", bufX.toString(), "0");
    writeAttribute(elem, "y", bufY.toString(), "0");
    writeAttribute(elem, "rotate", bufR.toString(), "");
    String str;
    try {
      str = text.getText(0, text.getLength());
    } catch (BadLocationException e) {
      InternalError error = new InternalError(e.getMessage());
      error.initCause(e);
      throw error;
    }
    elem.setTextContent(str);
    writeShapeAttributes(elem, attributes);
    writeOpacityAttribute(elem, attributes);
    writeTransformAttribute(elem, attributes);
    writeFontAttributes(elem, attributes);
    return elem;
  }

  protected void writeTextAreaElement(Element parent, SVGTextAreaFigure f) throws IOException {
    DefaultStyledDocument styledDoc = new DefaultStyledDocument();
    try {
      styledDoc.insertString(0, f.getText(), null);
    } catch (BadLocationException e) {
      InternalError error = new InternalError(e.getMessage());
      error.initCause(e);
      throw error;
    }
    Rectangle2D.Double bounds = f.getBounds();
    parent.appendChild(createTextArea(
        document,
        bounds.x,
        bounds.y,
        bounds.width,
        bounds.height,
        styledDoc,
        f.attr().getAttributes()));
  }

  protected Element createTextArea(
      Element doc,
      double x,
      double y,
      double w,
      double h,
      StyledDocument text,
      Map<AttributeKey<?>, Object> attributes)
      throws IOException {
    Element elem = doc.getOwnerDocument().createElement("textArea");
    writeAttribute(elem, "x", toNumber(x), "0");
    writeAttribute(elem, "y", toNumber(y), "0");
    writeAttribute(elem, "width", toNumber(w), "0");
    writeAttribute(elem, "height", toNumber(h), "0");
    String str;
    try {
      str = text.getText(0, text.getLength());
    } catch (BadLocationException e) {
      InternalError error = new InternalError(e.getMessage());
      error.initCause(e);
      throw error;
    }
    String[] lines = str.split("\n");
    for (int i = 0; i < lines.length; i++) {
      if (i != 0) {
        elem.appendChild(doc.getOwnerDocument().createElement("tbreak"));
      }
      Element contentElement = doc.getOwnerDocument().createElement(null);
      contentElement.setTextContent(lines[i]);
      elem.appendChild(contentElement);
    }
    writeShapeAttributes(elem, attributes);
    writeTransformAttribute(elem, attributes);
    writeOpacityAttribute(elem, attributes);
    writeFontAttributes(elem, attributes);
    return elem;
  }

  // ------------
  // Attributes
  // ------------
  /* Writes shape attributes.
   */
  protected void writeShapeAttributes(Element elem, Map<AttributeKey<?>, Object> m)
      throws IOException {
    attributeWriter.writeShapeAttributes(elem, m);
  }

  /* Writes the opacity attribute.
   */
  protected void writeOpacityAttribute(Element elem, Map<AttributeKey<?>, Object> m)
      throws IOException {
    attributeWriter.writeOpacityAttribute(elem, m);
  }

  /* Writes the transform attribute as specified in
   * http://www.w3.org/TR/SVGMobile12/coords.html#TransformAttribute
   *
   */
  protected void writeTransformAttribute(Element elem, Map<AttributeKey<?>, Object> a)
      throws IOException {
    attributeWriter.writeTransformAttribute(elem, a);
  }

  /* Writes font attributes as listed in
   * http://www.w3.org/TR/SVGMobile12/feature.html#Font
   */
  private void writeFontAttributes(Element elem, Map<AttributeKey<?>, Object> a)
      throws IOException {
    attributeWriter.writeFontAttributes(elem, a);
  }

  /* Writes viewport attributes.
   */
  private void writeViewportAttributes(Element elem, Map<AttributeKey<?>, Object> a)
      throws IOException {
    attributeWriter.writeViewportAttributes(elem, a);
  }

  protected void writeAttribute(Element elem, String name, String value, String defaultValue) {
    writeAttribute(elem, name, SVG_NAMESPACE, value, defaultValue);
  }

  protected void writeAttribute(
      Element elem, String name, String namespace, String value, String defaultValue) {
    if (!value.equals(defaultValue)) {
      elem.setAttribute(name, value);
    }
  }

  protected void writeAttribute(Element elem, String name, double value, double defaultValue) {
    writeAttribute(elem, name, SVG_NAMESPACE, value, defaultValue);
  }

  protected void writeAttribute(
      Element elem, String name, String namespace, double value, double defaultValue) {
    if (value != defaultValue) {
      elem.setAttribute(name, toNumber(value));
    }
  }

  /**
   * Returns a value as a SVG Path attribute. as specified in
   * http://www.w3.org/TR/SVGMobile12/paths.html#PathDataBNF
   */
  public static String toPath(BezierPath[] paths) {
    StringBuilder buf = new StringBuilder();
    for (int j = 0; j < paths.length; j++) {
      BezierPath path = paths[j];
      if (path.size() == 0) {
        // nothing to do
      } else if (path.size() == 1) {
        BezierPath.Node current = path.nodes().get(0);
        buf.append("M ");
        buf.append(toNumber(current.x[0]));
        buf.append(' ');
        buf.append(toNumber(current.y[0]));
        // buf.append(" L ");
        buf.append(toNumber(current.x[0]));
        buf.append(' ');
        buf.append(toNumber(current.y[0] + 1));
      } else {
        BezierPath.Node previous;
        BezierPath.Node current;
        previous = current = path.nodes().get(0);
        buf.append("M ");
        buf.append(toNumber(current.x[0]));
        buf.append(' ');
        buf.append(toNumber(current.y[0]));
        char nextCommand = 'L';
        for (int i = 1, n = path.size(); i < n; i++) {
          previous = current;
          current = path.nodes().get(i);
          if ((previous.mask & BezierPath.C2_MASK) == 0) {
            if ((current.mask & BezierPath.C1_MASK) == 0) {
              if (nextCommand != 'L') {
                buf.append(" L ");
                nextCommand = 'L';
              } else {
                buf.append(' ');
              }
              buf.append(toNumber(current.x[0]));
              buf.append(' ');
              buf.append(toNumber(current.y[0]));
            } else {
              if (nextCommand != 'Q') {
                buf.append(" Q ");
                nextCommand = 'Q';
              } else {
                buf.append(' ');
              }
              buf.append(toNumber(current.x[1]));
              buf.append(' ');
              buf.append(toNumber(current.y[1]));
              buf.append(' ');
              buf.append(toNumber(current.x[0]));
              buf.append(' ');
              buf.append(toNumber(current.y[0]));
            }
          } else {
            if ((current.mask & BezierPath.C1_MASK) == 0) {
              if (nextCommand != 'Q') {
                buf.append(" Q ");
                nextCommand = 'Q';
              } else {
                buf.append(' ');
              }
              buf.append(toNumber(previous.x[2]));
              buf.append(' ');
              buf.append(toNumber(previous.y[2]));
              buf.append(' ');
              buf.append(toNumber(current.x[0]));
              buf.append(' ');
              buf.append(toNumber(current.y[0]));
            } else {
              if (nextCommand != 'C') {
                buf.append(" C ");
                nextCommand = 'C';
              } else {
                buf.append(' ');
              }
              buf.append(toNumber(previous.x[2]));
              buf.append(' ');
              buf.append(toNumber(previous.y[2]));
              buf.append(' ');
              buf.append(toNumber(current.x[1]));
              buf.append(' ');
              buf.append(toNumber(current.y[1]));
              buf.append(' ');
              buf.append(toNumber(current.x[0]));
              buf.append(' ');
              buf.append(toNumber(current.y[0]));
            }
          }
        }
        if (path.isClosed()) {
          if (path.size() > 1) {
            previous = path.nodes().get(path.size() - 1);
            current = path.nodes().get(0);
            if ((previous.mask & BezierPath.C2_MASK) == 0) {
              if ((current.mask & BezierPath.C1_MASK) == 0) {
                if (nextCommand != 'L') {
                  buf.append(" L ");
                  nextCommand = 'L';
                } else {
                  buf.append(' ');
                }
                buf.append(toNumber(current.x[0]));
                buf.append(' ');
                buf.append(toNumber(current.y[0]));
              } else {
                if (nextCommand != 'Q') {
                  buf.append(" Q ");
                  nextCommand = 'Q';
                } else {
                  buf.append(' ');
                }
                buf.append(toNumber(current.x[1]));
                buf.append(' ');
                buf.append(toNumber(current.y[1]));
                buf.append(' ');
                buf.append(toNumber(current.x[0]));
                buf.append(' ');
                buf.append(toNumber(current.y[0]));
              }
            } else {
              if ((current.mask & BezierPath.C1_MASK) == 0) {
                if (nextCommand != 'Q') {
                  buf.append(" Q ");
                  nextCommand = 'Q';
                } else {
                  buf.append(' ');
                }
                buf.append(toNumber(previous.x[2]));
                buf.append(' ');
                buf.append(toNumber(previous.y[2]));
                buf.append(' ');
                buf.append(toNumber(current.x[0]));
                buf.append(' ');
                buf.append(toNumber(current.y[0]));
              } else {
                if (nextCommand != 'C') {
                  buf.append(" C ");
                  nextCommand = 'C';
                } else {
                  buf.append(' ');
                }
                buf.append(toNumber(previous.x[2]));
                buf.append(' ');
                buf.append(toNumber(previous.y[2]));
                buf.append(' ');
                buf.append(toNumber(current.x[1]));
                buf.append(' ');
                buf.append(toNumber(current.y[1]));
                buf.append(' ');
                buf.append(toNumber(current.x[0]));
                buf.append(' ');
                buf.append(toNumber(current.y[0]));
              }
            }
          }
          buf.append(" Z");
          nextCommand = '\0';
        }
      }
    }
    return buf.toString();
  }

  /** Returns a double array as a number attribute value. */
  public static String toNumber(double number) {
    String str = (IS_FLOAT_PRECISION) ? Float.toString((float) number) : Double.toString(number);
    if (str.endsWith(".0")) {
      str = str.substring(0, str.length() - 2);
    }
    return str;
  }

  /**
   * Returns a Point2D.Double array as a Points attribute value. as specified in
   * http://www.w3.org/TR/SVGMobile12/shapes.html#PointsBNF
   */
  public static String toPoints(Point2D.Double[] points) throws IOException {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < points.length; i++) {
      if (i != 0) {
        buf.append(", ");
      }
      buf.append(toNumber(points[i].x));
      buf.append(',');
      buf.append(toNumber(points[i].y));
    }
    return buf.toString();
  }

  /* Converts an AffineTransform into an SVG transform attribute value as specified in
   * http://www.w3.org/TR/SVGMobile12/coords.html#TransformAttribute
   */
  public static String toTransform(AffineTransform t) throws IOException {
    StringBuilder buf = new StringBuilder();
    switch (t.getType()) {
      case AffineTransform.TYPE_IDENTITY:
        buf.append("none");
        break;
      case AffineTransform.TYPE_TRANSLATION:
        // translate(<tx> [<ty>]), specifies a translation by tx and ty.
        // If <ty> is not provided, it is assumed to be zero.
        buf.append("translate(");
        buf.append(toNumber(t.getTranslateX()));
        if (t.getTranslateY() != 0d) {
          buf.append(' ');
          buf.append(toNumber(t.getTranslateY()));
        }
        buf.append(')');
        break;
        /*
        case AffineTransform.TYPE_GENERAL_ROTATION :
        case AffineTransform.TYPE_QUADRANT_ROTATION :
        case AffineTransform.TYPE_MASK_ROTATION :
        // rotate(<rotate-angle> [<cx> <cy>]), specifies a rotation by
        // <rotate-angle> degrees about a given point.
        // If optional parameters <cx> and <cy> are not supplied, the
        // rotate is about the origin of the current user coordinate
        // system. The operation corresponds to the matrix
        // [cos(a) sin(a) -sin(a) cos(a) 0 0].
        // If optional parameters <cx> and <cy> are supplied, the rotate
        // is about the point (<cx>, <cy>). The operation represents the
        // equivalent of the following specification:
        // translate(<cx>, <cy>) rotate(<rotate-angle>)
        // translate(-<cx>, -<cy>).
        buf.append("rotate(");
        buf.append(toNumber(t.getScaleX()));
        buf.append(')');
        break;*/
      case AffineTransform.TYPE_UNIFORM_SCALE:
        // scale(<sx> [<sy>]), specifies a scale operation by sx
        // and sy. If <sy> is not provided, it is assumed to be equal
        // to <sx>.
        buf.append("scale(");
        buf.append(toNumber(t.getScaleX()));
        buf.append(')');
        break;
      case AffineTransform.TYPE_GENERAL_SCALE:
      case AffineTransform.TYPE_MASK_SCALE:
        // scale(<sx> [<sy>]), specifies a scale operation by sx
        // and sy. If <sy> is not provided, it is assumed to be equal
        // to <sx>.
        buf.append("scale(");
        buf.append(toNumber(t.getScaleX()));
        buf.append(' ');
        buf.append(toNumber(t.getScaleY()));
        buf.append(')');
        break;
      default:
        // matrix(<a> <b> <c> <d> <e> <f>), specifies a transformation
        // in the form of a transformation matrix of six values.
        // matrix(a,b,c,d,e,f) is equivalent to applying the
        // transformation matrix [a b c d e f].
        buf.append("matrix(");
        double[] matrix = new double[6];
        t.getMatrix(matrix);
        for (int i = 0; i < matrix.length; i++) {
          if (i != 0) {
            buf.append(' ');
          }
          buf.append(toNumber(matrix[i]));
        }
        buf.append(')');
        break;
    }
    return buf.toString();
  }

  public static String toColor(Color color) {
    if (color == null) {
      return "none";
    }
    String value;
    value = "000000" + Integer.toHexString(color.getRGB());
    value = "#" + value.substring(value.length() - 6);
    if (value.charAt(1) == value.charAt(2)
        && value.charAt(3) == value.charAt(4)
        && value.charAt(5) == value.charAt(6)) {
      value = "#" + value.charAt(1) + value.charAt(3) + value.charAt(5);
    }
    return value;
  }

  @Override
  public String getFileExtension() {
    return "svg";
  }

  @Override
  public void write(URI uri, Drawing drawing) throws IOException {
    write(new File(uri), drawing);
  }

  public void write(File file, Drawing drawing) throws IOException {
    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
    try {
      write(out, drawing);
    } finally {
      out.close();
    }
  }

  @Override
  public void write(OutputStream out, Drawing drawing) throws IOException {
    write(out, drawing, drawing.getChildren());
  }

  /** All other write methods delegate their work to here. */
  public void write(OutputStream out, Drawing drawing, java.util.List<Figure> figures)
      throws IOException {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder;
    try {
      dBuilder = dbFactory.newDocumentBuilder();
    } catch (ParserConfigurationException ex) {
      Logger.getLogger(ImageMapOutputFormat.class.getName()).log(Level.SEVERE, null, ex);
      throw new IOException(ex);
    }
    Document doc = dBuilder.newDocument();
    document = doc.createElementNS(SVG_NAMESPACE, "svg");
    document.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
    document.setAttribute("version", "1.2");
    document.setAttribute("baseProfile", "tiny");
    writeViewportAttributes(document, drawing.attr().getAttributes());
    initStorageContext(document);
    defs = doc.createElement("defs");
    document.appendChild(defs);
    for (Figure f : figures) {
      writeElement(document, f);
    }
    // Write XML prolog
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
    writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    // Write XML content
    Transformer t;
    try {
      t = TransformerFactory.newInstance().newTransformer();
      if (isPrettyPrint) {
        t.setOutputProperty(OutputKeys.INDENT, "yes");
      }
      t.transform(new DOMSource(document), new StreamResult(out));
    } catch (TransformerException ex) {
      Logger.getLogger(SVGOutputFormat.class.getName()).log(Level.SEVERE, null, ex);
    }
    // Flush writer
    writer.flush();
  }

  private void initStorageContext(Element root) {
    identifiedElements = new HashMap<Element, String>();
    gradientToIDMap = new HashMap<Gradient, String>();
  }

  /** Gets a unique ID for the specified element. */
  public String getId(Element element) {
    if (identifiedElements.containsKey(element)) {
      return identifiedElements.get(element);
    } else {
      String id = Integer.toString(nextId++, Character.MAX_RADIX);
      identifiedElements.put(element, id);
      return id;
    }
  }

  @Override
  public Transferable createTransferable(
      Drawing drawing, java.util.List<Figure> figures, double scaleFactor) throws IOException {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    write(buf, drawing, figures);
    return new InputStreamTransferable(
        new DataFlavor(SVG_MIMETYPE, "Image SVG"), buf.toByteArray());
  }
}
