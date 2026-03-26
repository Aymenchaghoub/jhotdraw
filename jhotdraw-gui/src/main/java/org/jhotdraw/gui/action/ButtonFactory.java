/*
 * @(#)ButtonFactory.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.gui.action;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.jhotdraw.action.edit.CopyAction;
import org.jhotdraw.action.edit.CutAction;
import org.jhotdraw.action.edit.DuplicateAction;
import org.jhotdraw.action.edit.PasteAction;
import org.jhotdraw.api.app.Disposable;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.action.AbstractSelectedAction;
import org.jhotdraw.draw.action.ApplyAttributesAction;
import org.jhotdraw.draw.action.BringToFrontAction;
import org.jhotdraw.draw.action.ColorIcon;
import org.jhotdraw.draw.action.GroupAction;
import org.jhotdraw.draw.action.PickAttributesAction;
import org.jhotdraw.draw.action.SelectSameAction;
import org.jhotdraw.draw.action.SendToBackAction;
import org.jhotdraw.draw.action.UngroupAction;
import org.jhotdraw.draw.action.ZoomAction;
import org.jhotdraw.draw.action.ZoomEditorAction;
import org.jhotdraw.draw.event.ToolAdapter;
import org.jhotdraw.draw.event.ToolEvent;
import org.jhotdraw.draw.event.ToolListener;
import org.jhotdraw.draw.tool.DelegationSelectionTool;
import org.jhotdraw.draw.tool.Tool;
import org.jhotdraw.gui.JPopupButton;
import org.jhotdraw.utils.util.ResourceBundleUtil;

/**
 * ButtonFactory.
 *
 * <p>Facade over the specialized button factories. All color, stroke, font, and alignment
 * methods delegate to {@link ColorButtonFactory}, {@link StrokeButtonFactory},
 * {@link FontButtonFactory}, and {@link AlignmentButtonFactory} respectively.
 * This class retains tool management, zoom, and general attribute methods.
 *
 * <p>Design pattern:<br>
 * Name: Abstract Factory.<br>
 * Role: Abstract Factory / Facade.<br>
 * Partners: org.jhotdraw.samples.draw.DrawApplicationModel as Client,
 * org.jhotdraw.samples.draw.DrawView as Client,
 * org.jhotdraw.samples.draw.DrawingPanel as Client.
 *
 * <p>FIXME - All buttons created using the ButtonFactory must automatically become
 * disabled/enabled, when the DrawingEditor is disabled/enabled.
 */
public class ButtonFactory {

  // ---------------------------------------------------------------------------
  // Color palette constants — re-exported from ColorButtonFactory for backward compatibility
  // ---------------------------------------------------------------------------

  /** Mac OS X 'Apple Color Palette'. This palette has 8 columns. */
  public static final java.util.List<ColorIcon> DEFAULT_COLORS = ColorButtonFactory.DEFAULT_COLORS;

  public static final int DEFAULT_COLORS_COLUMN_COUNT =
      ColorButtonFactory.DEFAULT_COLORS_COLUMN_COUNT;

  /** Websave color palette as used by Macromedia Fireworks. This palette has 19 columns. */
  public static final java.util.List<ColorIcon> WEBSAVE_COLORS = ColorButtonFactory.WEBSAVE_COLORS;

  public static final int WEBSAVE_COLORS_COLUMN_COUNT =
      ColorButtonFactory.WEBSAVE_COLORS_COLUMN_COUNT;

  /** HSB color palette — 12 columns, 10 rows. */
  public static final java.util.List<ColorIcon> HSB_COLORS = ColorButtonFactory.HSB_COLORS;

  public static final int HSB_COLORS_COLUMN_COUNT = ColorButtonFactory.HSB_COLORS_COLUMN_COUNT;

  /**
   * Same palette as HSB_COLORS, but all color values are specified in the sRGB color space.
   */
  public static final java.util.List<ColorIcon> HSB_COLORS_AS_RGB =
      ColorButtonFactory.HSB_COLORS_AS_RGB;

  public static final int HSB_COLORS_AS_RGB_COLUMN_COUNT =
      ColorButtonFactory.HSB_COLORS_AS_RGB_COLUMN_COUNT;

  // ---------------------------------------------------------------------------
  // Inner classes
  // ---------------------------------------------------------------------------

  private static class ToolButtonListener implements java.awt.event.ItemListener {

    private Tool tool;
    private DrawingEditor editor;

    public ToolButtonListener(Tool t, DrawingEditor editor) {
      this.tool = t;
      this.editor = editor;
    }

    @Override
    public void itemStateChanged(java.awt.event.ItemEvent evt) {
      if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
        editor.setTool(tool);
      }
    }
  }

  /** Prevent instance creation. */
  private ButtonFactory() {}

  // ---------------------------------------------------------------------------
  // Drawing & selection actions
  // ---------------------------------------------------------------------------

  public static Collection<Action> createDrawingActions(DrawingEditor editor) {
    return createDrawingActions(editor, new ArrayList<>());
  }

  public static Collection<Action> createDrawingActions(
      DrawingEditor editor, java.util.List<Disposable> dsp) {
    List<Action> list = new ArrayList<>();
    AbstractSelectedAction a;
    list.add(new CutAction());
    list.add(new CopyAction());
    list.add(new PasteAction());
    list.add(a = new SelectSameAction(editor));
    dsp.add(a);
    return list;
  }

  public static Collection<Action> createSelectionActions(DrawingEditor editor) {
    List<Action> a = new ArrayList<>();
    a.add(new DuplicateAction());
    a.add(null); // separator
    a.add(new GroupAction(editor));
    a.add(new UngroupAction(editor));
    a.add(null); // separator
    a.add(new BringToFrontAction(editor));
    a.add(new SendToBackAction(editor));
    return a;
  }

  // ---------------------------------------------------------------------------
  // Tool buttons
  // ---------------------------------------------------------------------------

  public static JToggleButton addSelectionToolTo(JToolBar tb, final DrawingEditor editor) {
    return addSelectionToolTo(
        tb, editor, createDrawingActions(editor), createSelectionActions(editor));
  }

  public static JToggleButton addSelectionToolTo(
      JToolBar tb,
      final DrawingEditor editor,
      Collection<Action> drawingActions,
      Collection<Action> selectionActions) {
    Tool selectionTool = new DelegationSelectionTool(drawingActions, selectionActions);
    return addSelectionToolTo(tb, editor, selectionTool);
  }

  public static JToggleButton addSelectionToolTo(
      JToolBar tb, final DrawingEditor editor, Tool selectionTool) {
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
    JToggleButton t;
    Tool tool;
    HashMap<String, Object> attributes;
    ButtonGroup group;
    if (tb.getClientProperty("toolButtonGroup") instanceof ButtonGroup) {
      group = (ButtonGroup) tb.getClientProperty("toolButtonGroup");
    } else {
      group = new ButtonGroup();
      tb.putClientProperty("toolButtonGroup", group);
    }
    // Selection tool
    editor.setTool(selectionTool);
    t = new JToggleButton();
    final JToggleButton defaultToolButton = t;
    if (!(tb.getClientProperty("toolHandler") instanceof ToolListener)) {
      ToolListener toolHandler;
      toolHandler = new ToolAdapter() {
        @Override
        public void toolDone(ToolEvent event) {
          defaultToolButton.setSelected(true);
        }
      };
      tb.putClientProperty("toolHandler", toolHandler);
    }
    labels.configureToolBarButton(t, "selectionTool");
    t.setSelected(true);
    t.addItemListener(new ToolButtonListener(selectionTool, editor));
    t.setFocusable(false);
    group.add(t);
    tb.add(t);
    return t;
  }

  /** Method addSelectionToolTo must have been invoked prior to this on the JToolBar. */
  public static JToggleButton addToolTo(
      JToolBar tb, DrawingEditor editor, Tool tool, String labelKey, ResourceBundleUtil labels) {
    ButtonGroup group = (ButtonGroup) tb.getClientProperty("toolButtonGroup");
    ToolListener toolHandler = (ToolListener) tb.getClientProperty("toolHandler");
    JToggleButton t = new JToggleButton();
    labels.configureToolBarButton(t, labelKey);
    t.addItemListener(new ToolButtonListener(tool, editor));
    t.setFocusable(false);
    tool.addToolListener(toolHandler);
    group.add(t);
    tb.add(t);
    return t;
  }

  // ---------------------------------------------------------------------------
  // Zoom buttons
  // ---------------------------------------------------------------------------

  public static void addZoomButtonsTo(JToolBar bar, final DrawingEditor editor) {
    bar.add(createZoomButton(editor));
  }

  public static AbstractButton createZoomButton(final DrawingEditor editor) {
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
    final JPopupButton zoomPopupButton = new JPopupButton();
    labels.configureToolBarButton(zoomPopupButton, "view.zoomFactor");
    zoomPopupButton.setFocusable(false);
    if (editor.getDrawingViews().size() == 0) {
      zoomPopupButton.setText("100 %");
    } else {
      zoomPopupButton.setText(
          (int) (editor.getDrawingViews().iterator().next().getScaleFactor() * 100) + " %");
    }
    editor.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        // String constants are interned
        if ((evt.getPropertyName() == null && DrawingEditor.ACTIVE_VIEW_PROPERTY == null)
            || (evt.getPropertyName() != null
                && evt.getPropertyName().equals(DrawingEditor.ACTIVE_VIEW_PROPERTY))) {
          if (evt.getNewValue() == null) {
            zoomPopupButton.setText("100 %");
          } else {
            zoomPopupButton.setText((int) (editor.getActiveView().getScaleFactor() * 100) + " %");
          }
        }
      }
    });
    double[] factors = {16, 8, 5, 4, 3, 2, 1.5, 1.25, 1, 0.75, 0.5, 0.25, 0.10};
    for (int i = 0; i < factors.length; i++) {
      zoomPopupButton.add(new ZoomEditorAction(editor, factors[i], zoomPopupButton) {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
          super.actionPerformed(e);
          zoomPopupButton.setText((int) (editor.getActiveView().getScaleFactor() * 100) + " %");
        }
      });
    }
    zoomPopupButton.setFocusable(false);
    return zoomPopupButton;
  }

  public static AbstractButton createZoomButton(DrawingView view) {
    return createZoomButton(view, new double[] {5, 4, 3, 2, 1.5, 1.25, 1, 0.75, 0.5, 0.25, 0.10});
  }

  public static AbstractButton createZoomButton(final DrawingView view, double[] factors) {
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
    final JPopupButton zoomPopupButton = new JPopupButton();
    labels.configureToolBarButton(zoomPopupButton, "view.zoomFactor");
    zoomPopupButton.setFocusable(false);
    zoomPopupButton.setText((int) (view.getScaleFactor() * 100) + " %");
    view.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        // String constants are interned
        if ("scaleFactor".equals(evt.getPropertyName())) {
          zoomPopupButton.setText((int) (view.getScaleFactor() * 100) + " %");
        }
      }
    });
    for (int i = 0; i < factors.length; i++) {
      zoomPopupButton.add(new ZoomAction(view, factors[i], zoomPopupButton) {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
          super.actionPerformed(e);
          zoomPopupButton.setText((int) (view.getScaleFactor() * 100) + " %");
        }
      });
    }
    zoomPopupButton.setFocusable(false);
    return zoomPopupButton;
  }

  // ---------------------------------------------------------------------------
  // Composite toolbar population — delegates to specialized factories
  // ---------------------------------------------------------------------------

  /** Creates toolbar buttons and adds them to the specified JToolBar */
  public static void addAttributesButtonsTo(JToolBar bar, DrawingEditor editor) {
    JButton b;
    b = bar.add(new PickAttributesAction(editor));
    b.setFocusable(false);
    b = bar.add(new ApplyAttributesAction(editor));
    b.setFocusable(false);
    bar.addSeparator();
    addColorButtonsTo(bar, editor);
    bar.addSeparator();
    addStrokeButtonsTo(bar, editor);
    bar.addSeparator();
    addFontButtonsTo(bar, editor);
  }

  // ---------------------------------------------------------------------------
  // General attribute buttons (not specific to any sub-factory)
  // ---------------------------------------------------------------------------

  public static JButton createPickAttributesButton(DrawingEditor editor) {
    return createPickAttributesButton(editor, new ArrayList<>());
  }

  public static JButton createPickAttributesButton(
      DrawingEditor editor, java.util.List<Disposable> dsp) {
    JButton btn;
    AbstractSelectedAction d;
    btn = new JButton(d = new PickAttributesAction(editor));
    dsp.add(d);
    if (btn.getIcon() != null) {
      btn.putClientProperty("hideActionText", Boolean.TRUE);
    }
    btn.setHorizontalTextPosition(JButton.CENTER);
    btn.setVerticalTextPosition(JButton.BOTTOM);
    btn.setText(null);
    btn.setFocusable(false);
    return btn;
  }

  /**
   * Creates a button that applies the default attributes of the editor to the current selection.
   */
  public static JButton createApplyAttributesButton(DrawingEditor editor) {
    return createApplyAttributesButton(editor, new ArrayList<>());
  }

  public static JButton createApplyAttributesButton(
      DrawingEditor editor, java.util.List<Disposable> dsp) {
    JButton btn;
    AbstractSelectedAction d;
    btn = new JButton(d = new ApplyAttributesAction(editor));
    dsp.add(d);
    if (btn.getIcon() != null) {
      btn.putClientProperty("hideActionText", Boolean.TRUE);
    }
    btn.setHorizontalTextPosition(JButton.CENTER);
    btn.setVerticalTextPosition(JButton.BOTTOM);
    btn.setText(null);
    btn.setFocusable(false);
    return btn;
  }

  // ===========================================================================
  // FACADE — Color button delegation to ColorButtonFactory
  // ===========================================================================

  public static void addColorButtonsTo(JToolBar bar, DrawingEditor editor) {
    ColorButtonFactory.addColorButtonsTo(bar, editor);
  }

  public static void addColorButtonsTo(
      JToolBar bar, DrawingEditor editor, java.util.List<ColorIcon> colors, int columnCount) {
    ColorButtonFactory.addColorButtonsTo(bar, editor, colors, columnCount);
  }

  public static JPopupButton createColorButtonText(DrawingEditor editor) {
    return ColorButtonFactory.createColorButtonText(editor);
  }

  public static JPopupButton createColorButtonText(
      DrawingEditor editor, List<ColorIcon> colors, int columnCount) {
    return ColorButtonFactory.createColorButtonText(editor, colors, columnCount);
  }

  public static JPopupButton createColorButtonFill(DrawingEditor editor) {
    return ColorButtonFactory.createColorButtonFill(editor);
  }

  public static JPopupButton createColorButtonFill(
      DrawingEditor editor, List<ColorIcon> colors, int columnCount) {
    return ColorButtonFactory.createColorButtonFill(editor, colors, columnCount);
  }

  public static JPopupButton createColorButtonStroke(DrawingEditor editor) {
    return ColorButtonFactory.createColorButtonStroke(editor);
  }

  public static JPopupButton createColorButtonStroke(
      DrawingEditor editor, List<ColorIcon> colors, int columnCount) {
    return ColorButtonFactory.createColorButtonStroke(editor, colors, columnCount);
  }

  public static JPopupButton createEditorColorButton(
      DrawingEditor editor,
      AttributeKey<Color> attributeKey,
      java.util.List<ColorIcon> swatches,
      int columnCount,
      String labelKey,
      ResourceBundleUtil labels) {
    return ColorButtonFactory.createEditorColorButton(
        editor, attributeKey, swatches, columnCount, labelKey, labels);
  }

  public static JPopupButton createEditorColorButton(
      DrawingEditor editor,
      AttributeKey<Color> attributeKey,
      java.util.List<ColorIcon> swatches,
      int columnCount,
      String labelKey,
      ResourceBundleUtil labels,
      Map<AttributeKey<?>, Object> defaultAttributes) {
    return ColorButtonFactory.createEditorColorButton(
        editor, attributeKey, swatches, columnCount, labelKey, labels, defaultAttributes);
  }

  public static JPopupButton createEditorColorButton(
      DrawingEditor editor,
      AttributeKey<Color> attributeKey,
      java.util.List<ColorIcon> swatches,
      int columnCount,
      String labelKey,
      ResourceBundleUtil labels,
      Map<AttributeKey<?>, Object> defaultAttributes,
      Shape colorShape) {
    return ColorButtonFactory.createEditorColorButton(
        editor,
        attributeKey,
        swatches,
        columnCount,
        labelKey,
        labels,
        defaultAttributes,
        colorShape);
  }

  public static JPopupButton createSelectionColorButton(
      DrawingEditor editor,
      AttributeKey<Color> attributeKey,
      java.util.List<ColorIcon> swatches,
      int columnCount,
      String labelKey,
      ResourceBundleUtil labels) {
    return ColorButtonFactory.createSelectionColorButton(
        editor, attributeKey, swatches, columnCount, labelKey, labels);
  }

  public static JPopupButton createSelectionColorButton(
      DrawingEditor editor,
      AttributeKey<Color> attributeKey,
      java.util.List<ColorIcon> swatches,
      int columnCount,
      String labelKey,
      ResourceBundleUtil labels,
      Map<AttributeKey<?>, Object> defaultAttributes) {
    return ColorButtonFactory.createSelectionColorButton(
        editor, attributeKey, swatches, columnCount, labelKey, labels, defaultAttributes);
  }

  public static JPopupButton createSelectionColorButton(
      DrawingEditor editor,
      AttributeKey<Color> attributeKey,
      java.util.List<ColorIcon> swatches,
      int columnCount,
      String labelKey,
      ResourceBundleUtil labels,
      Map<AttributeKey<?>, Object> defaultAttributes,
      Shape colorShape) {
    return ColorButtonFactory.createSelectionColorButton(
        editor,
        attributeKey,
        swatches,
        columnCount,
        labelKey,
        labels,
        defaultAttributes,
        colorShape);
  }

  public static JPopupButton createSelectionColorButton(
      DrawingEditor editor,
      AttributeKey<Color> attributeKey,
      java.util.List<ColorIcon> swatches,
      int columnCount,
      String labelKey,
      ResourceBundleUtil labels,
      Map<AttributeKey<?>, Object> defaultAttributes,
      Shape colorShape,
      java.util.List<Disposable> dsp) {
    return ColorButtonFactory.createSelectionColorButton(
        editor,
        attributeKey,
        swatches,
        columnCount,
        labelKey,
        labels,
        defaultAttributes,
        colorShape,
        dsp);
  }

  public static JPopupButton createSelectionColorChooserButton(
      final DrawingEditor editor,
      final AttributeKey<Color> attributeKey,
      String labelKey,
      ResourceBundleUtil labels,
      Map<AttributeKey<?>, Object> defaultAttributes,
      Shape colorShape,
      final java.util.List<Disposable> dsp) {
    return ColorButtonFactory.createSelectionColorChooserButton(
        editor, attributeKey, labelKey, labels, defaultAttributes, colorShape, dsp);
  }

  public static JPopupButton createSelectionColorChooserButton(
      final DrawingEditor editor,
      final AttributeKey<Color> attributeKey,
      String labelKey,
      ResourceBundleUtil labels,
      Map<AttributeKey<?>, Object> defaultAttributes,
      Shape colorShape,
      final Class<?> uiclass,
      final java.util.List<Disposable> dsp) {
    return ColorButtonFactory.createSelectionColorChooserButton(
        editor, attributeKey, labelKey, labels, defaultAttributes, colorShape, uiclass, dsp);
  }

  public static JPopupButton createDrawingColorButton(
      DrawingEditor editor,
      AttributeKey<Color> attributeKey,
      java.util.List<ColorIcon> swatches,
      int columnCount,
      String labelKey,
      ResourceBundleUtil labels) {
    return ColorButtonFactory.createDrawingColorButton(
        editor, attributeKey, swatches, columnCount, labelKey, labels);
  }

  public static JPopupButton createDrawingColorButton(
      DrawingEditor editor,
      AttributeKey<Color> attributeKey,
      java.util.List<ColorIcon> swatches,
      int columnCount,
      String labelKey,
      ResourceBundleUtil labels,
      Map<AttributeKey<?>, Object> defaultAttributes) {
    return ColorButtonFactory.createDrawingColorButton(
        editor, attributeKey, swatches, columnCount, labelKey, labels, defaultAttributes);
  }

  public static JPopupButton createDrawingColorButton(
      DrawingEditor editor,
      AttributeKey<Color> attributeKey,
      java.util.List<ColorIcon> swatches,
      int columnCount,
      String labelKey,
      ResourceBundleUtil labels,
      Map<AttributeKey<?>, Object> defaultAttributes,
      Shape colorShape) {
    return ColorButtonFactory.createDrawingColorButton(
        editor,
        attributeKey,
        swatches,
        columnCount,
        labelKey,
        labels,
        defaultAttributes,
        colorShape);
  }

  public static JPopupButton createDrawingColorButton(
      DrawingEditor editor,
      AttributeKey<Color> attributeKey,
      java.util.List<ColorIcon> swatches,
      int columnCount,
      String labelKey,
      ResourceBundleUtil labels,
      Map<AttributeKey<?>, Object> defaultAttributes,
      Shape colorShape,
      java.util.List<Disposable> dsp) {
    return ColorButtonFactory.createDrawingColorButton(
        editor,
        attributeKey,
        swatches,
        columnCount,
        labelKey,
        labels,
        defaultAttributes,
        colorShape,
        dsp);
  }

  public static JPopupButton createDrawingColorChooserButton(
      final DrawingEditor editor,
      final AttributeKey<Color> attributeKey,
      String labelKey,
      ResourceBundleUtil labels,
      Map<AttributeKey<?>, Object> defaultAttributes,
      Shape colorShape,
      final java.util.List<Disposable> dsp) {
    return ColorButtonFactory.createDrawingColorChooserButton(
        editor, attributeKey, labelKey, labels, defaultAttributes, colorShape, dsp);
  }

  public static JPopupButton createDrawingColorChooserButton(
      final DrawingEditor editor,
      final AttributeKey<Color> attributeKey,
      String labelKey,
      ResourceBundleUtil labels,
      Map<AttributeKey<?>, Object> defaultAttributes,
      Shape colorShape,
      final Class<?> uiclass,
      final java.util.List<Disposable> dsp) {
    return ColorButtonFactory.createDrawingColorChooserButton(
        editor, attributeKey, labelKey, labels, defaultAttributes, colorShape, uiclass, dsp);
  }

  // ===========================================================================
  // FACADE — Stroke button delegation to StrokeButtonFactory
  // ===========================================================================

  public static void addStrokeButtonsTo(JToolBar bar, DrawingEditor editor) {
    StrokeButtonFactory.addStrokeButtonsTo(bar, editor);
  }

  public static JPopupButton createStrokeWidthButton(DrawingEditor editor) {
    return StrokeButtonFactory.createStrokeWidthButton(editor);
  }

  public static JPopupButton createStrokeWidthButton(
      DrawingEditor editor, ResourceBundleUtil labels) {
    return StrokeButtonFactory.createStrokeWidthButton(editor, labels);
  }

  public static JPopupButton createStrokeWidthButton(DrawingEditor editor, double[] widths) {
    return StrokeButtonFactory.createStrokeWidthButton(editor, widths);
  }

  public static JPopupButton createStrokeWidthButton(
      DrawingEditor editor, double[] widths, ResourceBundleUtil labels) {
    return StrokeButtonFactory.createStrokeWidthButton(editor, widths, labels);
  }

  public static JPopupButton createStrokeDecorationButton(DrawingEditor editor) {
    return StrokeButtonFactory.createStrokeDecorationButton(editor);
  }

  public static JPopupButton createStrokeDashesButton(DrawingEditor editor) {
    return StrokeButtonFactory.createStrokeDashesButton(editor);
  }

  public static JPopupButton createStrokeDashesButton(
      DrawingEditor editor, ResourceBundleUtil labels) {
    return StrokeButtonFactory.createStrokeDashesButton(editor, labels);
  }

  public static JPopupButton createStrokeDashesButton(DrawingEditor editor, double[][] dashes) {
    return StrokeButtonFactory.createStrokeDashesButton(editor, dashes);
  }

  public static JPopupButton createStrokeDashesButton(
      DrawingEditor editor, double[][] dashes, ResourceBundleUtil labels) {
    return StrokeButtonFactory.createStrokeDashesButton(editor, dashes, labels);
  }

  public static JPopupButton createStrokeDashesButton(
      DrawingEditor editor,
      double[][] dashes,
      ResourceBundleUtil labels,
      java.util.List<Disposable> dsp) {
    return StrokeButtonFactory.createStrokeDashesButton(editor, dashes, labels, dsp);
  }

  public static JPopupButton createStrokeTypeButton(DrawingEditor editor) {
    return StrokeButtonFactory.createStrokeTypeButton(editor);
  }

  public static JPopupButton createStrokePlacementButton(DrawingEditor editor) {
    return StrokeButtonFactory.createStrokePlacementButton(editor);
  }

  public static JPopupButton createStrokeCapButton(DrawingEditor editor) {
    return StrokeButtonFactory.createStrokeCapButton(editor);
  }

  public static JPopupButton createStrokeCapButton(
      DrawingEditor editor, ResourceBundleUtil labels) {
    return StrokeButtonFactory.createStrokeCapButton(editor, labels);
  }

  public static JPopupButton createStrokeCapButton(
      DrawingEditor editor, ResourceBundleUtil labels, java.util.List<Disposable> dsp) {
    return StrokeButtonFactory.createStrokeCapButton(editor, labels, dsp);
  }

  public static JPopupButton createStrokeJoinButton(DrawingEditor editor) {
    return StrokeButtonFactory.createStrokeJoinButton(editor);
  }

  public static JPopupButton createStrokeJoinButton(
      DrawingEditor editor, ResourceBundleUtil labels) {
    return StrokeButtonFactory.createStrokeJoinButton(editor, labels);
  }

  public static JPopupButton createStrokeJoinButton(
      DrawingEditor editor, ResourceBundleUtil labels, java.util.List<Disposable> dsp) {
    return StrokeButtonFactory.createStrokeJoinButton(editor, labels, dsp);
  }

  // ===========================================================================
  // FACADE — Font button delegation to FontButtonFactory
  // ===========================================================================

  public static void addFontButtonsTo(JToolBar bar, DrawingEditor editor) {
    FontButtonFactory.addFontButtonsTo(bar, editor);
  }

  public static JPopupButton createFontButton(DrawingEditor editor) {
    return FontButtonFactory.createFontButton(editor);
  }

  public static JPopupButton createFontButton(DrawingEditor editor, ResourceBundleUtil labels) {
    return FontButtonFactory.createFontButton(editor, labels);
  }

  public static JPopupButton createFontButton(
      DrawingEditor editor, AttributeKey<Font> key, ResourceBundleUtil labels) {
    return FontButtonFactory.createFontButton(editor, key, labels);
  }

  public static JPopupButton createFontButton(
      DrawingEditor editor,
      AttributeKey<Font> key,
      ResourceBundleUtil labels,
      java.util.List<Disposable> dsp) {
    return FontButtonFactory.createFontButton(editor, key, labels, dsp);
  }

  public static JButton createFontStyleBoldButton(DrawingEditor editor) {
    return FontButtonFactory.createFontStyleBoldButton(editor);
  }

  public static JButton createFontStyleBoldButton(DrawingEditor editor, ResourceBundleUtil labels) {
    return FontButtonFactory.createFontStyleBoldButton(editor, labels);
  }

  public static JButton createFontStyleBoldButton(
      DrawingEditor editor, ResourceBundleUtil labels, java.util.List<Disposable> dsp) {
    return FontButtonFactory.createFontStyleBoldButton(editor, labels, dsp);
  }

  public static JButton createFontStyleItalicButton(DrawingEditor editor) {
    return FontButtonFactory.createFontStyleItalicButton(editor);
  }

  public static JButton createFontStyleItalicButton(
      DrawingEditor editor, ResourceBundleUtil labels) {
    return FontButtonFactory.createFontStyleItalicButton(editor, labels);
  }

  public static JButton createFontStyleItalicButton(
      DrawingEditor editor, ResourceBundleUtil labels, java.util.List<Disposable> dsp) {
    return FontButtonFactory.createFontStyleItalicButton(editor, labels, dsp);
  }

  public static JButton createFontStyleUnderlineButton(DrawingEditor editor) {
    return FontButtonFactory.createFontStyleUnderlineButton(editor);
  }

  public static JButton createFontStyleUnderlineButton(
      DrawingEditor editor, ResourceBundleUtil labels) {
    return FontButtonFactory.createFontStyleUnderlineButton(editor, labels);
  }

  public static JButton createFontStyleUnderlineButton(
      DrawingEditor editor, ResourceBundleUtil labels, java.util.List<Disposable> dsp) {
    return FontButtonFactory.createFontStyleUnderlineButton(editor, labels, dsp);
  }

  // ===========================================================================
  // FACADE — Alignment button delegation to AlignmentButtonFactory
  // ===========================================================================

  /** Creates toolbar buttons and adds them to the specified JToolBar */
  public static void addAlignmentButtonsTo(JToolBar bar, final DrawingEditor editor) {
    AlignmentButtonFactory.addAlignmentButtonsTo(bar, editor);
  }

  /** Creates toolbar buttons and adds them to the specified JToolBar. */
  public static void addAlignmentButtonsTo(
      JToolBar bar, final DrawingEditor editor, java.util.List<Disposable> dsp) {
    AlignmentButtonFactory.addAlignmentButtonsTo(bar, editor, dsp);
  }

  /** Creates a button which toggles between two GridConstrainer for a DrawingView. */
  public static AbstractButton createToggleGridButton(final DrawingView view) {
    return AlignmentButtonFactory.createToggleGridButton(view);
  }
}
