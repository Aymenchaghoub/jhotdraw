/*
 * @(#)AbstractLiner.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.liner;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Collection;
import org.jhotdraw.draw.connector.Connector;
import org.jhotdraw.draw.figure.ConnectionFigure;
import org.jhotdraw.draw.figure.LineConnectionFigure;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.utils.geom.Geom;
import org.jhotdraw.utils.geom.path.BezierPath;

/**
 * AbstractLiner extracts common logic for ElbowLiner and SlantedLiner.
 *
 * <p>Both liners share an identical ~89-line branch that handles connections
 * where start and end figure are the same. The only difference between the two
 * was the name of the offset parameter ({@code shoulderSize} vs {@code slantSize}).
 * This class removes that duplication by delegating the value to
 * {@link #getOffsetSize()}, implemented by each concrete subclass.
 */
public abstract class AbstractLiner implements Liner, Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Returns the offset size (shoulderSize or slantSize) used when routing
   * the connection away from the connector anchor points.
   */
  protected abstract double getOffsetSize();

  /**
   * Shared routing logic for the case where the connection starts and ends
   * on the same figure. Extracted verbatim from ElbowLiner / SlantedLiner
   * (former lines 41-129), parameterised via {@link #getOffsetSize()}.
   */
  protected void routeSameFigure(ConnectionFigure figure, double offsetSize) {
    BezierPath path = ((LineConnectionFigure) figure).getBezierPath();
    // Ensure path has exactly five nodes
    while (path.size() < 5) {
      path.add(1, new BezierPath.Node(0, 0));
    }
    while (path.size() > 5) {
      path.remove(1);
    }
    Connector start = figure.getStartConnector();
    Connector end = figure.getEndConnector();
    Point2D.Double sp = start.findStart(figure);
    Point2D.Double ep = end.findEnd(figure);
    java.awt.geom.Rectangle2D.Double sb = start.getBounds();
    java.awt.geom.Rectangle2D.Double eb = end.getBounds();
    int soutcode = sb.outcode(sp);
    if (soutcode == 0) {
      soutcode = Geom.outcode(sb, eb);
    }
    int eoutcode = eb.outcode(ep);
    if (eoutcode == 0) {
      eoutcode = Geom.outcode(sb, eb);
    }
    path.nodes().get(0).moveTo(sp);
    path.nodes().get(path.size() - 1).moveTo(ep);
    switch (soutcode) {
      case Geom.OUT_TOP:
        eoutcode = Geom.OUT_LEFT;
        break;
      case Geom.OUT_RIGHT:
        eoutcode = Geom.OUT_TOP;
        break;
      case Geom.OUT_BOTTOM:
        eoutcode = Geom.OUT_RIGHT;
        break;
      case Geom.OUT_LEFT:
        eoutcode = Geom.OUT_BOTTOM;
        break;
      default:
        eoutcode = Geom.OUT_TOP;
        soutcode = Geom.OUT_RIGHT;
        break;
    }
    if ((soutcode & Geom.OUT_RIGHT) != 0) {
      path.nodes().get(1).moveTo(sp.x + offsetSize, sp.y);
    } else if ((soutcode & Geom.OUT_LEFT) != 0) {
      path.nodes().get(1).moveTo(sp.x - offsetSize, sp.y);
    } else if ((soutcode & Geom.OUT_BOTTOM) != 0) {
      path.nodes().get(1).moveTo(sp.x, sp.y + offsetSize);
    } else {
      path.nodes().get(1).moveTo(sp.x, sp.y - offsetSize);
    }
    if ((eoutcode & Geom.OUT_RIGHT) != 0) {
      path.nodes().get(3).moveTo(ep.x + offsetSize, ep.y);
    } else if ((eoutcode & Geom.OUT_LEFT) != 0) {
      path.nodes().get(3).moveTo(ep.x - offsetSize, ep.y);
    } else if ((eoutcode & Geom.OUT_BOTTOM) != 0) {
      path.nodes().get(3).moveTo(ep.x, ep.y + offsetSize);
    } else {
      path.nodes().get(3).moveTo(ep.x, ep.y - offsetSize);
    }
    switch (soutcode) {
      case Geom.OUT_RIGHT:
        path.nodes().get(2).moveTo(path.nodes().get(1).x[0], path.nodes().get(3).y[0]);
        break;
      case Geom.OUT_TOP:
        path.nodes().get(2).moveTo(path.nodes().get(1).y[0], path.nodes().get(3).x[0]);
        break;
      case Geom.OUT_LEFT:
        path.nodes().get(2).moveTo(path.nodes().get(1).x[0], path.nodes().get(3).y[0]);
        break;
      case Geom.OUT_BOTTOM:
      default:
        path.nodes().get(2).moveTo(path.nodes().get(1).y[0], path.nodes().get(3).x[0]);
        break;
    }
  }

  // -------------------------------------------------------------------------
  // Default implementations for the Liner interface
  // -------------------------------------------------------------------------

  @Override
  public Collection<Handle> createHandles(BezierPath path) {
    return java.util.Collections.emptyList();
  }

  @Override
  public Liner clone() {
    try {
      return (Liner) super.clone();
    } catch (CloneNotSupportedException ex) {
      InternalError error = new InternalError(ex.getMessage());
      error.initCause(ex);
      throw error;
    }
  }
}
