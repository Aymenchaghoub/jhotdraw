/*
 * @(#)DefaultSvgFigureWriters.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.samples.svg.io;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.jhotdraw.draw.figure.BezierFigure;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.samples.svg.figures.SVGEllipseFigure;
import org.jhotdraw.samples.svg.figures.SVGGroupFigure;
import org.jhotdraw.samples.svg.figures.SVGImageFigure;
import org.jhotdraw.samples.svg.figures.SVGPathFigure;
import org.jhotdraw.samples.svg.figures.SVGRectFigure;
import org.jhotdraw.samples.svg.figures.SVGTextAreaFigure;
import org.jhotdraw.samples.svg.figures.SVGTextFigure;
import org.w3c.dom.Element;

final class DefaultSvgFigureWriters {

  private DefaultSvgFigureWriters() {}

  static List<SvgFigureWriter> createWriters() {
    return Arrays.asList(
        new EllipseFigureWriter(),
        new GroupFigureWriter(),
        new ImageFigureWriter(),
        new PathFigureWriter(),
        new RectFigureWriter(),
        new TextFigureWriter(),
        new TextAreaFigureWriter());
  }

  private static final class EllipseFigureWriter implements SvgFigureWriter {

    @Override
    public boolean supports(Figure figure) {
      return figure instanceof SVGEllipseFigure;
    }

    @Override
    public void write(SVGOutputFormat format, Element parent, Figure figure) throws IOException {
      SVGEllipseFigure ellipse = (SVGEllipseFigure) figure;
      if (ellipse.getWidth() == ellipse.getHeight()) {
        format.writeCircleElement(parent, ellipse);
      } else {
        format.writeEllipseElement(parent, ellipse);
      }
    }
  }

  private static final class GroupFigureWriter implements SvgFigureWriter {

    @Override
    public boolean supports(Figure figure) {
      return figure instanceof SVGGroupFigure;
    }

    @Override
    public void write(SVGOutputFormat format, Element parent, Figure figure) throws IOException {
      format.writeGElement(parent, (SVGGroupFigure) figure);
    }
  }

  private static final class ImageFigureWriter implements SvgFigureWriter {

    @Override
    public boolean supports(Figure figure) {
      return figure instanceof SVGImageFigure;
    }

    @Override
    public void write(SVGOutputFormat format, Element parent, Figure figure) throws IOException {
      format.writeImageElement(parent, (SVGImageFigure) figure);
    }
  }

  private static final class PathFigureWriter implements SvgFigureWriter {

    @Override
    public boolean supports(Figure figure) {
      return figure instanceof SVGPathFigure;
    }

    @Override
    public void write(SVGOutputFormat format, Element parent, Figure figure) throws IOException {
      SVGPathFigure path = (SVGPathFigure) figure;
      if (path.getChildCount() == 1) {
        BezierFigure bezier = (BezierFigure) path.getChild(0);
        boolean isLinear = true;
        for (int i = 0, n = bezier.getNodeCount(); i < n; i++) {
          if (bezier.getNode(i).getMask() != 0) {
            isLinear = false;
            break;
          }
        }
        if (isLinear) {
          if (bezier.isClosed()) {
            format.writePolygonElement(parent, path);
          } else if (bezier.getNodeCount() == 2) {
            format.writeLineElement(parent, path);
          } else {
            format.writePolylineElement(parent, path);
          }
        } else {
          format.writePathElement(parent, path);
        }
      } else {
        format.writePathElement(parent, path);
      }
    }
  }

  private static final class RectFigureWriter implements SvgFigureWriter {

    @Override
    public boolean supports(Figure figure) {
      return figure instanceof SVGRectFigure;
    }

    @Override
    public void write(SVGOutputFormat format, Element parent, Figure figure) throws IOException {
      format.writeRectElement(parent, (SVGRectFigure) figure);
    }
  }

  private static final class TextFigureWriter implements SvgFigureWriter {

    @Override
    public boolean supports(Figure figure) {
      return figure instanceof SVGTextFigure;
    }

    @Override
    public void write(SVGOutputFormat format, Element parent, Figure figure) throws IOException {
      format.writeTextElement(parent, (SVGTextFigure) figure);
    }
  }

  private static final class TextAreaFigureWriter implements SvgFigureWriter {

    @Override
    public boolean supports(Figure figure) {
      return figure instanceof SVGTextAreaFigure;
    }

    @Override
    public void write(SVGOutputFormat format, Element parent, Figure figure) throws IOException {
      format.writeTextAreaElement(parent, (SVGTextAreaFigure) figure);
    }
  }
}
