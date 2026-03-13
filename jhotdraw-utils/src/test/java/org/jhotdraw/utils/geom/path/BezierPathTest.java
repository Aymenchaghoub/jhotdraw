/*
 * Copyright (C) 2015 JHotDraw.
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
package org.jhotdraw.utils.geom.path;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import org.junit.jupiter.api.Test;

/**
 * @author toben
 */
public class BezierPathTest {

  @Test
  public void testToGeneralPath() {
    BezierPath instance = new BezierPath();
    Point2D.Double c0 = new Point2D.Double(0.0004, 0.002);
    instance.add(c0);
    c0 = new Point2D.Double(21.0004, 56.92827);
    instance.add(c0);
    Path2D.Double gp = instance.toGeneralPath();
    PathIterator pathIterator = gp.getPathIterator(null);
    double[] coords = new double[2];
    int i = 0;
    while (pathIterator.isDone() == false) {
      pathIterator.currentSegment(coords);
      for (int j = 0; j < 3; j++) {
        assertEquals(coords[0], instance.nodes().get(i).getControlPoint(j).x);
        assertEquals(coords[1], instance.nodes().get(i).getControlPoint(j).y);
      }
      i++;
      pathIterator.next();
    }
  }

  /** Test of toPolygonArray method, of class BezierPath. */
  @Test
  public void testToPolygonArray() {
    BezierPath instance = new BezierPath();
    Point2D.Double c0 = new Point2D.Double(0.0004, 0.002);
    instance.add(c0);
    c0 = new Point2D.Double(21.0004, 56.92827);
    instance.add(c0);
    Point2D.Double[] toPolygonArray = instance.toPolygonArray();
    assertEquals(toPolygonArray.length, 2);
    for (int i = 0; i < toPolygonArray.length; i++) {
      for (int j = 0; j < 3; j++) {
        assertEquals(toPolygonArray[i], instance.nodes().get(i).getControlPoint(j));
      }
    }
  }

  @Test
  public void testPathIterator() {
    BezierPath instance = new BezierPath();
    Point2D.Double c0 = new Point2D.Double(0.0004, 0.002);
    instance.add(c0);
    c0 = new Point2D.Double(21.0004, 56.92827);
    instance.add(c0);
    PathIterator pathIterator = instance.getPathIterator(null);
    double[] coords = new double[2];
    int i = 0;
    while (pathIterator.isDone() == false) {
      pathIterator.currentSegment(coords);
      for (int j = 0; j < 3; j++) {
        assertEquals(coords[0], instance.nodes().get(i).getControlPoint(j).x);
        assertEquals(coords[1], instance.nodes().get(i).getControlPoint(j).y);
      }
      i++;
      pathIterator.next();
    }
  }

  @Test
  public void testPathIterator2() {
    BezierPath instance = new BezierPath();
    Point2D.Double c0 = new Point2D.Double(0.0004, 0.002);
    instance.add(c0);
    c0 = new Point2D.Double(21.0004, 56.92827);
    instance.add(c0);
    PathIterator pathIterator = instance.getPathIterator(null, 4);
    double[] coords = new double[2];
    int i = 0;
    while (pathIterator.isDone() == false) {
      pathIterator.currentSegment(coords);
      for (int j = 0; j < 3; j++) {
        assertEquals(coords[0], instance.nodes().get(i).getControlPoint(j).x);
        assertEquals(coords[1], instance.nodes().get(i).getControlPoint(j).y);
      }
      i++;
      pathIterator.next();
    }
  }

  @Test
  public void testCurveToControlPointOverload() {
    BezierPath instance = new BezierPath();
    instance.add(new Point2D.Double(0, 0));

    BezierPath.ControlPoint cp1 = new BezierPath.ControlPoint(1, 2);
    BezierPath.ControlPoint cp2 = new BezierPath.ControlPoint(3, 4);
    BezierPath.ControlPoint end = new BezierPath.ControlPoint(5, 6);

    instance.curveTo(cp1, cp2, end);

    assertEquals(2, instance.nodes().size());
    BezierPath.Node firstNode = instance.nodes().get(0);
    BezierPath.Node secondNode = instance.nodes().get(1);

    assertEquals(cp1.x(), firstNode.getControlPoint(2).x);
    assertEquals(cp1.y(), firstNode.getControlPoint(2).y);
    assertEquals(cp2.x(), secondNode.getControlPoint(1).x);
    assertEquals(cp2.y(), secondNode.getControlPoint(1).y);
    assertEquals(end.x(), secondNode.getControlPoint(0).x);
    assertEquals(end.y(), secondNode.getControlPoint(0).y);
  }
}
