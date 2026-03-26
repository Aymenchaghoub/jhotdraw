/*
 * @(#)StrokeButtonFactory.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.gui.action;

import static org.jhotdraw.draw.AttributeKeys.END_DECORATION;
import static org.jhotdraw.draw.AttributeKeys.FILL_UNDER_STROKE;
import static org.jhotdraw.draw.AttributeKeys.START_DECORATION;
import static org.jhotdraw.draw.AttributeKeys.STROKE_CAP;
import static org.jhotdraw.draw.AttributeKeys.STROKE_DASHES;
import static org.jhotdraw.draw.AttributeKeys.STROKE_INNER_WIDTH_FACTOR;
import static org.jhotdraw.draw.AttributeKeys.STROKE_JOIN;
import static org.jhotdraw.draw.AttributeKeys.STROKE_PLACEMENT;
import static org.jhotdraw.draw.AttributeKeys.STROKE_TYPE;
import static org.jhotdraw.draw.AttributeKeys.STROKE_WIDTH;

import java.awt.BasicStroke;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JToolBar;
import org.jhotdraw.api.app.Disposable;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.action.AttributeAction;
import org.jhotdraw.draw.action.LineDecorationIcon;
import org.jhotdraw.draw.action.StrokeIcon;
import org.jhotdraw.draw.decoration.ArrowTip;
import org.jhotdraw.draw.decoration.LineDecoration;
import org.jhotdraw.gui.JPopupButton;
import org.jhotdraw.utils.geom.DoubleStroke;
import org.jhotdraw.utils.util.ActionUtil;
import org.jhotdraw.utils.util.ResourceBundleUtil;

/**
 * StrokeButtonFactory.
 *
 * <p>Specialized factory for stroke-related toolbar buttons (width, dashes, type, decoration,
 * placement, cap, join).
 * Extracted from {@link ButtonFactory} as part of the God class decomposition.
 *
 * <p>Design pattern:<br>
 * Name: Abstract Factory.<br>
 * Role: Concrete Factory (stroke subset).<br>
 * Partners: {@link ButtonFactory} as Facade.
 */
public class StrokeButtonFactory {

  /** Prevent instance creation. */
  private StrokeButtonFactory() {}

  // ---------------------------------------------------------------------------
  // addStrokeButtonsTo
  // ---------------------------------------------------------------------------

  public static void addStrokeButtonsTo(JToolBar bar, DrawingEditor editor) {
    bar.add(createStrokeDecorationButton(editor));
    bar.add(createStrokeWidthButton(editor));
    bar.add(createStrokeDashesButton(editor));
    bar.add(createStrokeTypeButton(editor));
    bar.add(createStrokePlacementButton(editor));
    bar.add(createStrokeCapButton(editor));
    bar.add(createStrokeJoinButton(editor));
  }

  // ---------------------------------------------------------------------------
  // createStrokeWidthButton
  // ---------------------------------------------------------------------------

  public static JPopupButton createStrokeWidthButton(DrawingEditor editor) {
    return createStrokeWidthButton(
        editor,
        new double[] {0d, 0.5d, 1d, 2d, 3d, 5d, 9d, 13d},
        ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels"));
  }

  public static JPopupButton createStrokeWidthButton(
      DrawingEditor editor, ResourceBundleUtil labels) {
    return createStrokeWidthButton(editor, new double[] {0.5d, 1d, 2d, 3d, 5d, 9d, 13d}, labels);
  }

  public static JPopupButton createStrokeWidthButton(DrawingEditor editor, double[] widths) {
    return createStrokeWidthButton(
        editor,
        new double[] {0.5d, 1d, 2d, 3d, 5d, 9d, 13d},
        ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels"));
  }

  public static JPopupButton createStrokeWidthButton(
      DrawingEditor editor, double[] widths, ResourceBundleUtil labels) {
    JPopupButton strokeWidthPopupButton = new JPopupButton();
    labels.configureToolBarButton(strokeWidthPopupButton, "attribute.strokeWidth");
    strokeWidthPopupButton.setFocusable(false);
    NumberFormat formatter = NumberFormat.getInstance();
    if (formatter instanceof DecimalFormat) {
      ((DecimalFormat) formatter).setMaximumFractionDigits(1);
      ((DecimalFormat) formatter).setMinimumFractionDigits(0);
    }
    for (int i = 0; i < widths.length; i++) {
      String label = Double.toString(widths[i]);
      Icon icon = new StrokeIcon(
          new BasicStroke((float) widths[i], BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
      AttributeAction a = new AttributeAction(editor, STROKE_WIDTH, widths[i], label, icon);
      a.putValue(
          ActionUtil.UNDO_PRESENTATION_NAME_KEY, labels.getString("attribute.strokeWidth.text"));
      AbstractButton btn = strokeWidthPopupButton.add(a);
      btn.setDisabledIcon(icon);
    }
    return strokeWidthPopupButton;
  }

  // ---------------------------------------------------------------------------
  // createStrokeDecorationButton
  // ---------------------------------------------------------------------------

  public static JPopupButton createStrokeDecorationButton(DrawingEditor editor) {
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
    JPopupButton strokeDecorationPopupButton = new JPopupButton();
    labels.configureToolBarButton(strokeDecorationPopupButton, "attribute.strokeDecoration");
    strokeDecorationPopupButton.setFocusable(false);
    strokeDecorationPopupButton.setColumnCount(2, false);
    LineDecoration[] decorations = {
      // Arrow
      new ArrowTip(0.35, 12, 11.3),
      // Arrow
      new ArrowTip(0.35, 13, 7),
      // Generalization triangle
      new ArrowTip(Math.PI / 5, 12, 9.8, true, true, false),
      // Dependency arrow
      new ArrowTip(Math.PI / 6, 12, 0, false, true, false),
      // Link arrow
      new ArrowTip(Math.PI / 11, 13, 0, false, true, true),
      // Aggregation diamond
      new ArrowTip(Math.PI / 6, 10, 18, false, true, false),
      // Composition diamond
      new ArrowTip(Math.PI / 6, 10, 18, true, true, true),
      null
    };
    for (LineDecoration decoration : decorations) {
      strokeDecorationPopupButton.add(new AttributeAction(
          editor, START_DECORATION, decoration, null, new LineDecorationIcon(decoration, true)));
      strokeDecorationPopupButton.add(new AttributeAction(
          editor, END_DECORATION, decoration, null, new LineDecorationIcon(decoration, false)));
    }
    return strokeDecorationPopupButton;
  }

  // ---------------------------------------------------------------------------
  // createStrokeDashesButton
  // ---------------------------------------------------------------------------

  public static JPopupButton createStrokeDashesButton(DrawingEditor editor) {
    return createStrokeDashesButton(
        editor, ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels"));
  }

  public static JPopupButton createStrokeDashesButton(
      DrawingEditor editor, ResourceBundleUtil labels) {
    return createStrokeDashesButton(
        editor,
        new double[][] {null, {4d, 4d}, {2d, 2d}, {4d, 2d}, {2d, 4d}, {8d, 2d}, {6d, 2d, 2d, 2d}},
        labels);
  }

  public static JPopupButton createStrokeDashesButton(DrawingEditor editor, double[][] dashes) {
    return createStrokeDashesButton(
        editor, dashes, ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels"));
  }

  public static JPopupButton createStrokeDashesButton(
      DrawingEditor editor, double[][] dashes, ResourceBundleUtil labels) {
    return createStrokeDashesButton(editor, dashes, labels, new ArrayList<>());
  }

  public static JPopupButton createStrokeDashesButton(
      DrawingEditor editor,
      double[][] dashes,
      ResourceBundleUtil labels,
      java.util.List<Disposable> dsp) {
    JPopupButton strokeDashesPopupButton = new JPopupButton();
    labels.configureToolBarButton(strokeDashesPopupButton, "attribute.strokeDashes");
    strokeDashesPopupButton.setFocusable(false);
    for (double[] dashe : dashes) {
      float[] fdashes;
      if (dashe == null) {
        fdashes = null;
      } else {
        fdashes = new float[dashe.length];
        for (int j = 0; j < dashe.length; j++) {
          fdashes[j] = (float) dashe[j];
        }
      }
      Icon icon = new StrokeIcon(
          new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f, fdashes, 0));
      AttributeAction a;
      AbstractButton btn = strokeDashesPopupButton.add(
          a = new AttributeAction(editor, STROKE_DASHES, dashe, null, icon));
      dsp.add(a);
      btn.setDisabledIcon(icon);
    }
    return strokeDashesPopupButton;
  }

  // ---------------------------------------------------------------------------
  // createStrokeTypeButton
  // ---------------------------------------------------------------------------

  public static JPopupButton createStrokeTypeButton(DrawingEditor editor) {
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
    JPopupButton strokeTypePopupButton = new JPopupButton();
    labels.configureToolBarButton(strokeTypePopupButton, "attribute.strokeType");
    strokeTypePopupButton.setFocusable(false);
    strokeTypePopupButton.add(new AttributeAction(
        editor,
        STROKE_TYPE,
        AttributeKeys.StrokeType.BASIC,
        labels.getString("attribute.strokeType.basic"),
        new StrokeIcon(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL))));
    HashMap<AttributeKey<?>, Object> attr = new HashMap<>();
    attr.put(STROKE_TYPE, AttributeKeys.StrokeType.DOUBLE);
    attr.put(STROKE_INNER_WIDTH_FACTOR, 2d);
    strokeTypePopupButton.add(new AttributeAction(
        editor,
        attr,
        labels.getString("attribute.strokeType.double"),
        new StrokeIcon(new DoubleStroke(2, 1))));
    attr = new HashMap<>();
    attr.put(STROKE_TYPE, AttributeKeys.StrokeType.DOUBLE);
    attr.put(STROKE_INNER_WIDTH_FACTOR, 3d);
    strokeTypePopupButton.add(new AttributeAction(
        editor,
        attr,
        labels.getString("attribute.strokeType.double"),
        new StrokeIcon(new DoubleStroke(3, 1))));
    attr = new HashMap<>();
    attr.put(STROKE_TYPE, AttributeKeys.StrokeType.DOUBLE);
    attr.put(STROKE_INNER_WIDTH_FACTOR, 4d);
    strokeTypePopupButton.add(new AttributeAction(
        editor,
        attr,
        labels.getString("attribute.strokeType.double"),
        new StrokeIcon(new DoubleStroke(4, 1))));
    return strokeTypePopupButton;
  }

  // ---------------------------------------------------------------------------
  // createStrokePlacementButton
  // ---------------------------------------------------------------------------

  public static JPopupButton createStrokePlacementButton(DrawingEditor editor) {
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
    JPopupButton strokePlacementPopupButton = new JPopupButton();
    labels.configureToolBarButton(strokePlacementPopupButton, "attribute.strokePlacement");
    strokePlacementPopupButton.setFocusable(false);
    HashMap<AttributeKey<?>, Object> attr;
    attr = new HashMap<>();
    attr.put(STROKE_PLACEMENT, AttributeKeys.StrokePlacement.CENTER);
    attr.put(FILL_UNDER_STROKE, AttributeKeys.Underfill.CENTER);
    strokePlacementPopupButton.add(new AttributeAction(
        editor, attr, labels.getString("attribute.strokePlacement.center"), null));
    attr = new HashMap<>();
    attr.put(STROKE_PLACEMENT, AttributeKeys.StrokePlacement.INSIDE);
    attr.put(FILL_UNDER_STROKE, AttributeKeys.Underfill.CENTER);
    strokePlacementPopupButton.add(new AttributeAction(
        editor, attr, labels.getString("attribute.strokePlacement.inside"), null));
    attr = new HashMap<>();
    attr.put(STROKE_PLACEMENT, AttributeKeys.StrokePlacement.OUTSIDE);
    attr.put(FILL_UNDER_STROKE, AttributeKeys.Underfill.CENTER);
    strokePlacementPopupButton.add(new AttributeAction(
        editor, attr, labels.getString("attribute.strokePlacement.outside"), null));
    attr = new HashMap<>();
    attr.put(STROKE_PLACEMENT, AttributeKeys.StrokePlacement.CENTER);
    attr.put(FILL_UNDER_STROKE, AttributeKeys.Underfill.FULL);
    strokePlacementPopupButton.add(new AttributeAction(
        editor, attr, labels.getString("attribute.strokePlacement.centerFilled"), null));
    attr = new HashMap<>();
    attr.put(STROKE_PLACEMENT, AttributeKeys.StrokePlacement.INSIDE);
    attr.put(FILL_UNDER_STROKE, AttributeKeys.Underfill.FULL);
    strokePlacementPopupButton.add(new AttributeAction(
        editor, attr, labels.getString("attribute.strokePlacement.insideFilled"), null));
    attr = new HashMap<>();
    attr.put(STROKE_PLACEMENT, AttributeKeys.StrokePlacement.OUTSIDE);
    attr.put(FILL_UNDER_STROKE, AttributeKeys.Underfill.FULL);
    strokePlacementPopupButton.add(new AttributeAction(
        editor, attr, labels.getString("attribute.strokePlacement.outsideFilled"), null));
    attr = new HashMap<>();
    attr.put(STROKE_PLACEMENT, AttributeKeys.StrokePlacement.CENTER);
    attr.put(FILL_UNDER_STROKE, AttributeKeys.Underfill.NONE);
    strokePlacementPopupButton.add(new AttributeAction(
        editor, attr, labels.getString("attribute.strokePlacement.centerUnfilled"), null));
    attr = new HashMap<>();
    attr.put(STROKE_PLACEMENT, AttributeKeys.StrokePlacement.INSIDE);
    attr.put(FILL_UNDER_STROKE, AttributeKeys.Underfill.NONE);
    strokePlacementPopupButton.add(new AttributeAction(
        editor, attr, labels.getString("attribute.strokePlacement.insideUnfilled"), null));
    attr = new HashMap<>();
    attr.put(STROKE_PLACEMENT, AttributeKeys.StrokePlacement.OUTSIDE);
    attr.put(FILL_UNDER_STROKE, AttributeKeys.Underfill.NONE);
    strokePlacementPopupButton.add(new AttributeAction(
        editor, attr, labels.getString("attribute.strokePlacement.outsideUnfilled"), null));
    return strokePlacementPopupButton;
  }

  // ---------------------------------------------------------------------------
  // createStrokeCapButton
  // ---------------------------------------------------------------------------

  public static JPopupButton createStrokeCapButton(DrawingEditor editor) {
    return createStrokeCapButton(editor, ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels"));
  }

  public static JPopupButton createStrokeCapButton(
      DrawingEditor editor, ResourceBundleUtil labels) {
    return createStrokeCapButton(editor, labels, new ArrayList<>());
  }

  public static JPopupButton createStrokeCapButton(
      DrawingEditor editor, ResourceBundleUtil labels, java.util.List<Disposable> dsp) {
    JPopupButton popupButton = new JPopupButton();
    labels.configureToolBarButton(popupButton, "attribute.strokeCap");
    popupButton.setFocusable(false);
    HashMap<AttributeKey<?>, Object> attr;
    attr = new HashMap<>();
    attr.put(STROKE_CAP, BasicStroke.CAP_BUTT);
    AttributeAction a;
    popupButton.add(
        a = new AttributeAction(editor, attr, labels.getString("attribute.strokeCap.butt"), null));
    dsp.add(a);
    attr = new HashMap<>();
    attr.put(STROKE_CAP, BasicStroke.CAP_ROUND);
    popupButton.add(
        a = new AttributeAction(editor, attr, labels.getString("attribute.strokeCap.round"), null));
    dsp.add(a);
    attr = new HashMap<>();
    attr.put(STROKE_CAP, BasicStroke.CAP_SQUARE);
    popupButton.add(
        a = new AttributeAction(
            editor, attr, labels.getString("attribute.strokeCap.square"), null));
    dsp.add(a);
    return popupButton;
  }

  // ---------------------------------------------------------------------------
  // createStrokeJoinButton
  // ---------------------------------------------------------------------------

  public static JPopupButton createStrokeJoinButton(DrawingEditor editor) {
    return createStrokeJoinButton(editor, ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels"));
  }

  public static JPopupButton createStrokeJoinButton(
      DrawingEditor editor, ResourceBundleUtil labels) {
    return createStrokeJoinButton(editor, labels, new ArrayList<>());
  }

  public static JPopupButton createStrokeJoinButton(
      DrawingEditor editor, ResourceBundleUtil labels, java.util.List<Disposable> dsp) {
    JPopupButton popupButton = new JPopupButton();
    labels.configureToolBarButton(popupButton, "attribute.strokeJoin");
    popupButton.setFocusable(false);
    HashMap<AttributeKey<?>, Object> attr;
    attr = new HashMap<>();
    attr.put(STROKE_JOIN, BasicStroke.JOIN_BEVEL);
    AttributeAction a;
    popupButton.add(
        a = new AttributeAction(
            editor, attr, labels.getString("attribute.strokeJoin.bevel"), null));
    dsp.add(a);
    attr = new HashMap<>();
    attr.put(STROKE_JOIN, BasicStroke.JOIN_ROUND);
    popupButton.add(
        a = new AttributeAction(
            editor, attr, labels.getString("attribute.strokeJoin.round"), null));
    dsp.add(a);
    attr = new HashMap<>();
    attr.put(STROKE_JOIN, BasicStroke.JOIN_MITER);
    popupButton.add(
        a = new AttributeAction(
            editor, attr, labels.getString("attribute.strokeJoin.miter"), null));
    dsp.add(a);
    return popupButton;
  }
}
