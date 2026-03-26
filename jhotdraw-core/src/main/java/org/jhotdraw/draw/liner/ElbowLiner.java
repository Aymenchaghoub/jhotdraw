/*
 * @(#)ElbowLiner.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.liner;

import java.awt.geom.*;
import java.util.*;
import org.jhotdraw.draw.connector.Connector;
import org.jhotdraw.draw.figure.ConnectionFigure;
import org.jhotdraw.draw.figure.LineConnectionFigure;
import org.jhotdraw.utils.geom.Geom;
import org.jhotdraw.utils.geom.path.BezierPath;

/**
 * A {@link Liner} that constrains a connection to orthogonal lines.
 *
 * <p>The "same-figure" routing branch is inherited from {@link AbstractLiner}.
 */
public class ElbowLiner extends AbstractLiner {

  private double shoulderSize;

  public ElbowLiner() {
    this(20);
  }

  public ElbowLiner(double shoulderSize) {
    this.shoulderSize = shoulderSize;
  }

  /** Exposes {@code shoulderSize} to the shared routing logic in {@link AbstractLiner}. */
  @Override
  protected double getOffsetSize() {
    return shoulderSize;
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
      routeSameFigure(figure, shoulderSize);
      return;
    }
    // Different-figure case: orthogonal routing
    Point2D.Double sp = start.findStart(figure);
    Point2D.Double ep = end.findEnd(figure);
    path.clear();
    path.add(new BezierPath.Node(sp.x, sp.y));
    if (sp.x == ep.x || sp.y == ep.y) {
      path.add(new BezierPath.Node(ep.x, ep.y));
    } else {
      Rectangle2D.Double sb = start.getBounds();
      sb.x += 5d;
      sb.y += 5d;
      sb.width -= 10d;
      sb.height -= 10d;
      Rectangle2D.Double eb = end.getBounds();
      eb.x += 5d;
      eb.y += 5d;
      eb.width -= 10d;
      eb.height -= 10d;
      int soutcode = sb.outcode(sp);
      if (soutcode == 0) {
        soutcode = Geom.outcode(sb, eb);
      }
      int eoutcode = eb.outcode(ep);
      if (eoutcode == 0) {
        eoutcode = Geom.outcode(eb, sb);
      }
      if ((soutcode & (Geom.OUT_TOP | Geom.OUT_BOTTOM)) != 0
          && (eoutcode & (Geom.OUT_TOP | Geom.OUT_BOTTOM)) != 0) {
        path.add(new BezierPath.Node(sp.x, (sp.y + ep.y) / 2));
        path.add(new BezierPath.Node(ep.x, (sp.y + ep.y) / 2));
      } else if ((soutcode & (Geom.OUT_LEFT | Geom.OUT_RIGHT)) != 0
          && (eoutcode & (Geom.OUT_LEFT | Geom.OUT_RIGHT)) != 0) {
        path.add(new BezierPath.Node((sp.x + ep.x) / 2, sp.y));
        path.add(new BezierPath.Node((sp.x + ep.x) / 2, ep.y));
      } else if (soutcode == Geom.OUT_BOTTOM || soutcode == Geom.OUT_TOP) {
        path.add(new BezierPath.Node(sp.x, ep.y));
      } else {
        path.add(new BezierPath.Node(ep.x, sp.y));
      }
      path.add(new BezierPath.Node(ep.x, ep.y));
    }
    // Ensure all path nodes are straight (C0)
    for (BezierPath.Node node : path.nodes()) {
      node.setMask(BezierPath.C0_MASK);
    }
    path.invalidatePath();
  }
}
