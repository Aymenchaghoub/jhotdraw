/*
 * Copyright (C) 2026 JHotDraw.
 */
package org.jhotdraw.utils.geom.path;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Point2D;
import org.junit.jupiter.api.Test;

class BezierPathOperationsTest {

  @Test
  void moveToAndLineToAddExpectedNodes() {
    BezierPath path = new BezierPath();

    path.moveTo(0, 0);
    path.lineTo(10, 0);
    path.lineTo(10, 10);

    assertEquals(3, path.nodes().size());
    assertEquals(0d, path.nodes().get(0).getControlPoint(0).x, 0.0001d);
    assertEquals(0d, path.nodes().get(0).getControlPoint(0).y, 0.0001d);
    assertEquals(10d, path.nodes().get(2).getControlPoint(0).x, 0.0001d);
    assertEquals(10d, path.nodes().get(2).getControlPoint(0).y, 0.0001d);
  }

  @Test
  void curveToSetsControlPointsOnCurrentAndNewNode() {
    BezierPath path = new BezierPath();
    path.moveTo(0, 0);

    path.curveTo(1, 2, 3, 4, 5, 6);

    assertEquals(2, path.nodes().size());
    BezierPath.Node first = path.nodes().get(0);
    BezierPath.Node second = path.nodes().get(1);
    assertEquals(1d, first.getControlPoint(2).x, 0.0001d);
    assertEquals(2d, first.getControlPoint(2).y, 0.0001d);
    assertEquals(3d, second.getControlPoint(1).x, 0.0001d);
    assertEquals(4d, second.getControlPoint(1).y, 0.0001d);
    assertEquals(5d, second.getControlPoint(0).x, 0.0001d);
    assertEquals(6d, second.getControlPoint(0).y, 0.0001d);
  }

  @Test
  void splitSegmentInsertsIntermediateNode() {
    BezierPath path = new BezierPath();
    path.moveTo(0, 0);
    path.lineTo(10, 0);

    int insertedIndex = path.splitSegment(new Point2D.Double(5, 0), 0.1d);

    assertEquals(1, insertedIndex);
    assertEquals(3, path.nodes().size());
    assertEquals(5d, path.nodes().get(1).getControlPoint(0).x, 0.0001d);
    assertEquals(0d, path.nodes().get(1).getControlPoint(0).y, 0.0001d);
  }

  @Test
  void getRelativePositionOnPathReturnsValidPositionAndMinusOne() {
    BezierPath path = new BezierPath();
    path.moveTo(0, 0);
    path.lineTo(10, 0);

    // The implementation uses an internal cached path for length computation.
    path.validatePath();

    double onPath = path.getRelativePositionOnPath(new Point2D.Double(5, 0), 0.1d);
    double offPath = path.getRelativePositionOnPath(new Point2D.Double(5, 2), 0.1d);

    assertEquals(0.5d, onPath, 0.0001d);
    assertEquals(-1d, offPath, 0.0001d);
  }
}
