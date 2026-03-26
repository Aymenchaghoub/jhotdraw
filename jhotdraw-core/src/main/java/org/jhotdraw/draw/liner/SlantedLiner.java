/*
 * @(#)SlantedLiner.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.liner;

import java.awt.geom.*;
import org.jhotdraw.draw.connector.Connector;
import org.jhotdraw.draw.figure.ConnectionFigure;
import org.jhotdraw.draw.figure.LineConnectionFigure;
import org.jhotdraw.utils.geom.Geom;
import org.jhotdraw.utils.geom.path.BezierPath;

/**
 * A {@link Liner} that constrains a connection to slanted (diagonal-shoulder) lines.
 *
 * <p>The "same-figure" routing branch is inherited from {@link AbstractLiner}.
 */
public class SlantedLiner extends AbstractLiner {

  private double slantSize;

  public SlantedLiner() {
    this(20);
  }

  public SlantedLiner(double slantSize) {
    this.slantSize = slantSize;
  }

  /** Exposes {@code slantSize} to the shared routing logic in {@link AbstractLiner}. */
  @Override
  protected double getOffsetSize() {
    return slantSize;
  }

  @Override
  public void lineout(ConnectionFigure figure) {
    BezierPath path = ((LineConnectionFigure) figure).getBezierPath();
    Connector start = figure.getStartConnector();
    Connector end = figure.getEndConnector();
    if (start == null || end == null || path == null) {
      return;
    }
    // Same-figure case: delegate to AbstractLiner
    if (figure.getStartFigure() == figure.getEndFigure()) {
      routeSameFigure(figure, slantSize);
    } else {
      routeDifferentFigures(path, start, end, figure);
    }
    for (BezierPath.Node node : path.nodes()) {
      node.setMask(BezierPath.C0_MASK);
    }
    path.invalidatePath();
  }

  // -------------------------------------------------------------------------
  // Different-figure routing (specific to SlantedLiner)
  // -------------------------------------------------------------------------

  /** Routes the connection between two different figures with diagonal shoulders. */
  private void routeDifferentFigures(
      BezierPath path, Connector start, Connector end, ConnectionFigure figure) {
    while (path.size() < 4) {
      path.add(1, new BezierPath.Node(0, 0));
    }
    while (path.size() > 4) {
      path.remove(1);
    }
    Point2D.Double sp = start.findStart(figure);
    Point2D.Double ep = end.findEnd(figure);
    Rectangle2D.Double sb = start.getBounds();
    Rectangle2D.Double eb = end.getBounds();
    int soutcode = computeOutcode(sb, sp, eb);
    int eoutcode = computeOutcode(eb, ep, sb);
    path.nodes().get(0).moveTo(sp);
    path.nodes().get(path.size() - 1).moveTo(ep);
    applyStartOffset(path, sp, soutcode);
    applyEndOffset(path, ep, eoutcode);
  }

  /**
   * Computes the outcode for a point relative to its bounding rectangle,
   * falling back to edge comparison when the point lies exactly on the boundary.
   */
  private int computeOutcode(
      Rectangle2D.Double bounds, Point2D.Double point, Rectangle2D.Double otherBounds) {
    int outcode = bounds.outcode(point);
    if (outcode != 0) {
      return outcode;
    }
    if (point.x <= bounds.x) {
      return Geom.OUT_LEFT;
    } else if (point.y <= bounds.y) {
      return Geom.OUT_TOP;
    } else if (point.x >= bounds.x + bounds.width) {
      return Geom.OUT_RIGHT;
    } else if (point.y >= bounds.y + bounds.height) {
      return Geom.OUT_BOTTOM;
    }
    return Geom.outcode(bounds, otherBounds);
  }

  /** Positions node 1 (first intermediate point) away from the start connector. */
  private void applyStartOffset(BezierPath path, Point2D.Double sp, int soutcode) {
    if ((soutcode & Geom.OUT_RIGHT) != 0) {
      path.nodes().get(1).moveTo(sp.x + slantSize, sp.y);
    } else if ((soutcode & Geom.OUT_LEFT) != 0) {
      path.nodes().get(1).moveTo(sp.x - slantSize, sp.y);
    } else if ((soutcode & Geom.OUT_BOTTOM) != 0) {
      path.nodes().get(1).moveTo(sp.x, sp.y + slantSize);
    } else {
      path.nodes().get(1).moveTo(sp.x, sp.y - slantSize);
    }
  }

  /** Positions node 2 (second intermediate point) away from the end connector. */
  private void applyEndOffset(BezierPath path, Point2D.Double ep, int eoutcode) {
    if ((eoutcode & Geom.OUT_RIGHT) != 0) {
      path.nodes().get(2).moveTo(ep.x + slantSize, ep.y);
    } else if ((eoutcode & Geom.OUT_LEFT) != 0) {
      path.nodes().get(2).moveTo(ep.x - slantSize, ep.y);
    } else if ((eoutcode & Geom.OUT_BOTTOM) != 0) {
      path.nodes().get(2).moveTo(ep.x, ep.y + slantSize);
    } else {
      path.nodes().get(2).moveTo(ep.x, ep.y - slantSize);
    }
  }
}
