/*
 * @(#)ColorButtonFactory.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.gui.action;

import static org.jhotdraw.draw.AttributeKeys.FILL_COLOR;
import static org.jhotdraw.draw.AttributeKeys.STROKE_COLOR;
import static org.jhotdraw.draw.AttributeKeys.TEXT_COLOR;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.color.ColorSpace;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.plaf.ColorChooserUI;
import org.jhotdraw.api.app.Disposable;
import org.jhotdraw.color.HSBColorSpace;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.action.AttributeAction;
import org.jhotdraw.draw.action.ColorIcon;
import org.jhotdraw.draw.action.DefaultAttributeAction;
import org.jhotdraw.draw.action.DrawingAttributeAction;
import org.jhotdraw.draw.action.DrawingColorChooserAction;
import org.jhotdraw.draw.action.DrawingColorChooserHandler;
import org.jhotdraw.draw.action.DrawingColorIcon;
import org.jhotdraw.draw.action.EditorColorChooserAction;
import org.jhotdraw.draw.action.EditorColorIcon;
import org.jhotdraw.draw.action.SelectionColorChooserAction;
import org.jhotdraw.draw.action.SelectionColorChooserHandler;
import org.jhotdraw.draw.action.SelectionColorIcon;
import org.jhotdraw.draw.event.SelectionComponentRepainter;
import org.jhotdraw.gui.JComponentPopup;
import org.jhotdraw.gui.JPopupButton;
import org.jhotdraw.utils.util.Images;
import org.jhotdraw.utils.util.Methods;
import org.jhotdraw.utils.util.ResourceBundleUtil;

/**
 * ColorButtonFactory.
 *
 * <p>Specialized factory for color-related toolbar buttons (fill, stroke, text color).
 * Extracted from {@link ButtonFactory} as part of the God class decomposition.
 *
 * <p>Design pattern:<br>
 * Name: Abstract Factory.<br>
 * Role: Concrete Factory (color subset).<br>
 * Partners: {@link ButtonFactory} as Facade.
 */
public class ColorButtonFactory {

  /** Mac OS X 'Apple Color Palette'. This palette has 8 columns. */
  public static final java.util.List<ColorIcon> DEFAULT_COLORS;

  static {
    DEFAULT_COLORS = List.of(
        new ColorIcon(0x800000, "Cayenne"),
        new ColorIcon(0x808000, "Asparagus"),
        new ColorIcon(0x008000, "Clover"),
        new ColorIcon(0x008080, "Teal"),
        new ColorIcon(0x000080, "Midnight"),
        new ColorIcon(0x800080, "Plum"),
        new ColorIcon(0x7f7f7f, "Tin"),
        new ColorIcon(0x808080, "Nickel"),
        new ColorIcon(0xff0000, "Maraschino"),
        new ColorIcon(0xffff00, "Lemon"),
        new ColorIcon(0x00ff00, "Spring"),
        new ColorIcon(0x00ffff, "Turquoise"),
        new ColorIcon(0x0000ff, "Blueberry"),
        new ColorIcon(0xff00ff, "Magenta"),
        new ColorIcon(0x666666, "Steel"),
        new ColorIcon(0x999999, "Aluminium"),
        new ColorIcon(0xff6666, "Salmon"),
        new ColorIcon(0xffff66, "Banana"),
        new ColorIcon(0x66ff66, "Flora"),
        new ColorIcon(0x66ffff, "Ice"),
        new ColorIcon(0x6666ff, "Orchid"),
        new ColorIcon(0xff66ff, "Bubblegum"),
        new ColorIcon(0x4c4c4c, "Iron"),
        new ColorIcon(0xb3b3b3, "Magnesium"),
        new ColorIcon(0x804000, "Mocha"),
        new ColorIcon(0x408000, "Fern"),
        new ColorIcon(0x008040, "Moss"),
        new ColorIcon(0x004080, "Ocean"),
        new ColorIcon(0x400080, "Eggplant"),
        new ColorIcon(0x800040, "Maroon"),
        new ColorIcon(0x333333, "Tungsten"),
        new ColorIcon(0xcccccc, "Silver"),
        new ColorIcon(0xff8000, "Tangerine"),
        new ColorIcon(0x80ff00, "Lime"),
        new ColorIcon(0x00ff80, "Sea Foam"),
        new ColorIcon(0x0080ff, "Aqua"),
        new ColorIcon(0x8000ff, "Grape"),
        new ColorIcon(0xff0080, "Strawberry"),
        new ColorIcon(0x191919, "Lead"),
        new ColorIcon(0xe6e6e6, "Mercury"),
        new ColorIcon(0xffcc66, "Cantaloupe"),
        new ColorIcon(0xccff66, "Honeydew"),
        new ColorIcon(0x66ffcc, "Spindrift"),
        new ColorIcon(0x66ccff, "Sky"),
        new ColorIcon(0xcc66ff, "Lavender"),
        new ColorIcon(0xff6fcf, "Carnation"),
        new ColorIcon(0x000000, "Licorice"),
        new ColorIcon(0xffffff, "Snow"));
  }

  public static final int DEFAULT_COLORS_COLUMN_COUNT = 8;

  /**
   * Websave color palette as used by Macromedia Fireworks. This palette has 19 columns.
   */
  public static final java.util.List<ColorIcon> WEBSAVE_COLORS;

  static {
    List<ColorIcon> m = new ArrayList<>();
    for (int b = 0; b <= 0xff; b += 0x33) {
      int rgb = (b << 16) | (b << 8) | b;
      m.add(new ColorIcon(rgb));
      for (int r = 0; r <= 0x66; r += 0x33) {
        for (int g = 0; g <= 0xff; g += 0x33) {
          rgb = (r << 16) | (g << 8) | b;
          m.add(new ColorIcon(rgb));
        }
      }
    }
    int[] firstColumn = {0xff0000, 0x00ff00, 0x0000ff, 0xff00ff, 0x00ffff, 0xffff00};
    for (int b = 0x0, i = 0; b <= 0xff; b += 0x33, i++) {
      int rgb = (b << 16) | (b << 8) | b;
      m.add(new ColorIcon(firstColumn[i]));
      for (int r = 0x99; r <= 0xff; r += 0x33) {
        for (int g = 0; g <= 0xff; g += 0x33) {
          rgb = 0xff000000 | (r << 16) | (g << 8) | b;
          m.add(new ColorIcon(rgb, "#" + Integer.toHexString(rgb).substring(2)));
        }
      }
    }
    WEBSAVE_COLORS = Collections.unmodifiableList(m);
  }

  public static final int WEBSAVE_COLORS_COLUMN_COUNT = 19;

  /**
   * HSB color palette with a set of colors chosen based on a physical criteria.
   * This palette has 12 columns and 10 rows.
   */
  public static final java.util.List<ColorIcon> HSB_COLORS;

  public static final int HSB_COLORS_COLUMN_COUNT = 12;

  /**
   * This is the same palette as HSB_COLORS, but all color values are specified in the sRGB color
   * space.
   */
  public static final java.util.List<ColorIcon> HSB_COLORS_AS_RGB;

  public static final int HSB_COLORS_AS_RGB_COLUMN_COUNT = 12;

  static {
    ColorSpace grayCS = ColorSpace.getInstance(ColorSpace.CS_GRAY);
    HSBColorSpace hsbCS = HSBColorSpace.getInstance();
    List<ColorIcon> m = new ArrayList<>();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
    m.add(new ColorIcon(
        new Color(0, true), labels.getToolTipTextProperty("attribute.color.noColor")));
    for (int b = 10; b >= 0; b--) {
      Color c = new Color(grayCS, new float[] {b / 10f}, 1f);
      m.add(new ColorIcon(
          c, labels.getFormatted("attribute.color.grayComponents.toolTipText", b * 10)));
    }
    for (int s = 2; s <= 8; s += 2) {
      for (int h = 0; h < 12; h++) {
        Color c = new Color(hsbCS, new float[] {(h) / 12f, s * 0.1f, 1f}, 1f);
        m.add(new ColorIcon(
            c,
            labels.getFormatted(
                "attribute.color.hsbComponents.toolTipText", h * 360 / 12, s * 10, 100)));
      }
    }
    for (int b = 10; b >= 2; b -= 2) {
      for (int h = 0; h < 12; h++) {
        Color c = new Color(hsbCS, new float[] {(h) / 12f, 1f, b * 0.1f}, 1f);
        m.add(new ColorIcon(
            new Color(hsbCS, new float[] {(h) / 12f, 1f, b * 0.1f}, 1f),
            labels.getFormatted(
                "attribute.color.hsbComponents.toolTipText", h * 360 / 12, 100, b * 10)));
      }
    }
    HSB_COLORS = Collections.unmodifiableList(m);
    m = new ArrayList<>();
    for (ColorIcon ci : HSB_COLORS) {
      if (ci.getColor() == null) {
        m.add(new ColorIcon(
            new Color(0, true), labels.getToolTipTextProperty("attribute.color.noColor")));
      } else {
        Color c = ci.getColor();
        c = c.getColorSpace() == grayCS
            ? new Color(
                c.getGreen(),
                c.getGreen(),
                c.getGreen(),
                c.getAlpha()) // workaround for rounding error
            : new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        m.add(new ColorIcon(
            c,
            labels.getFormatted(
                "attribute.color.rgbComponents.toolTipText",
                c.getRed(),
                c.getGreen(),
                c.getBlue())));
      }
    }
    HSB_COLORS_AS_RGB = Collections.unmodifiableList(m);
  }

  /** Prevent instance creation. */
  private ColorButtonFactory() {}

  // ---------------------------------------------------------------------------
  // addColorButtonsTo
  // ---------------------------------------------------------------------------

  public static void addColorButtonsTo(JToolBar bar, DrawingEditor editor) {
    addColorButtonsTo(bar, editor, DEFAULT_COLORS, DEFAULT_COLORS_COLUMN_COUNT);
  }

  public static void addColorButtonsTo(
      JToolBar bar, DrawingEditor editor, java.util.List<ColorIcon> colors, int columnCount) {
    bar.add(createColorButtonStroke(editor, colors, columnCount));
    bar.add(createColorButtonFill(editor, colors, columnCount));
    bar.add(createColorButtonText(editor, colors, columnCount));
  }

  // ---------------------------------------------------------------------------
  // createColorButton* convenience methods
  // ---------------------------------------------------------------------------

  public static JPopupButton createColorButtonText(DrawingEditor editor) {
    return createColorButtonText(editor, DEFAULT_COLORS, DEFAULT_COLORS_COLUMN_COUNT);
  }

  public static JPopupButton createColorButtonText(
      DrawingEditor editor, List<ColorIcon> colors, int columnCount) {
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
    return createEditorColorButton(
        editor, TEXT_COLOR, colors, columnCount, "attribute.textColor", labels, new HashMap<>());
  }

  public static JPopupButton createColorButtonFill(DrawingEditor editor) {
    return createColorButtonFill(editor, DEFAULT_COLORS, DEFAULT_COLORS_COLUMN_COUNT);
  }

  public static JPopupButton createColorButtonFill(
      DrawingEditor editor, List<ColorIcon> colors, int columnCount) {
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
    return createEditorColorButton(
        editor, FILL_COLOR, colors, columnCount, "attribute.fillColor", labels, new HashMap<>());
  }

  public static JPopupButton createColorButtonStroke(DrawingEditor editor) {
    return createColorButtonStroke(editor, DEFAULT_COLORS, DEFAULT_COLORS_COLUMN_COUNT);
  }

  public static JPopupButton createColorButtonStroke(
      DrawingEditor editor, List<ColorIcon> colors, int columnCount) {
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
    return createEditorColorButton(
        editor,
        STROKE_COLOR,
        colors,
        columnCount,
        "attribute.strokeColor",
        labels,
        new HashMap<>());
  }

  // ---------------------------------------------------------------------------
  // createEditorColorButton
  // ---------------------------------------------------------------------------

  public static JPopupButton createEditorColorButton(
      DrawingEditor editor,
      AttributeKey<Color> attributeKey,
      java.util.List<ColorIcon> swatches,
      int columnCount,
      String labelKey,
      ResourceBundleUtil labels) {
    return createEditorColorButton(
        editor, attributeKey, swatches, columnCount, labelKey, labels, null);
  }

  public static JPopupButton createEditorColorButton(
      DrawingEditor editor,
      AttributeKey<Color> attributeKey,
      java.util.List<ColorIcon> swatches,
      int columnCount,
      String labelKey,
      ResourceBundleUtil labels,
      Map<AttributeKey<?>, Object> defaultAttributes) {
    return createEditorColorButton(
        editor,
        attributeKey,
        swatches,
        columnCount,
        labelKey,
        labels,
        defaultAttributes,
        new Rectangle(1, 17, 20, 4));
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
    final JPopupButton popupButton = new JPopupButton();
    popupButton.setPopupAlpha(1f);
    if (defaultAttributes == null) {
      defaultAttributes = new HashMap<>();
    }
    popupButton.setAction(
        new DefaultAttributeAction(editor, attributeKey, defaultAttributes) {
          @Override
          protected void updateEnabledState() {
            if (getView() != null) {
              setEnabled(getView().isEnabled());
            } else {
              setEnabled(false);
            }
          }
        },
        new Rectangle(0, 0, 22, 22));
    popupButton.setColumnCount(columnCount, false);
    boolean hasNullColor = false;
    for (ColorIcon swatch : swatches) {
      AttributeAction a;
      HashMap<AttributeKey<?>, Object> attributes = new HashMap<>(defaultAttributes);
      Color swatchColor = swatch.getColor();
      attributes.put(attributeKey, swatchColor);
      if (swatchColor == null || swatchColor.getAlpha() == 0) {
        hasNullColor = true;
      }
      popupButton.add(
          a = new AttributeAction(
              editor, attributes, labels.getToolTipTextProperty(labelKey), swatch));
      a.putValue(Action.SHORT_DESCRIPTION, swatch.getName());
      a.setUpdateEnabledState(false);
    }
    // No color
    if (!hasNullColor) {
      AttributeAction a;
      HashMap<AttributeKey<?>, Object> attributes = new HashMap<>(defaultAttributes);
      attributes.put(attributeKey, null);
      popupButton.add(
          a = new AttributeAction(
              editor,
              attributes,
              labels.getToolTipTextProperty("attribute.color.noColor"),
              new ColorIcon(
                  null,
                  labels.getToolTipTextProperty("attribute.color.noColor"),
                  swatches.get(0).getIconWidth(),
                  swatches.get(0).getIconHeight())));
      a.putValue(
          Action.SHORT_DESCRIPTION, labels.getToolTipTextProperty("attribute.color.noColor"));
      a.setUpdateEnabledState(false);
    }
    // Color chooser
    ImageIcon chooserIcon = new ImageIcon(Images.createImage(
        ColorButtonFactory.class,
        "/org/jhotdraw/draw/action/images/attribute.color.colorChooser.png"));
    Action a;
    popupButton.add(
        a = new EditorColorChooserAction(
            editor, attributeKey, "color", chooserIcon, defaultAttributes));
    labels.configureToolBarButton(popupButton, labelKey);
    a.putValue(
        Action.SHORT_DESCRIPTION, labels.getToolTipTextProperty("attribute.color.colorChooser"));
    Icon icon = new EditorColorIcon(
        editor,
        attributeKey,
        labels.getLargeIconProperty(labelKey, ColorButtonFactory.class).getImage(),
        colorShape);
    popupButton.setIcon(icon);
    popupButton.setDisabledIcon(icon);
    popupButton.setFocusable(false);
    editor.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        popupButton.repaint();
      }
    });
    return popupButton;
  }

  // ---------------------------------------------------------------------------
  // createSelectionColorButton
  // ---------------------------------------------------------------------------

  public static JPopupButton createSelectionColorButton(
      DrawingEditor editor,
      AttributeKey<Color> attributeKey,
      java.util.List<ColorIcon> swatches,
      int columnCount,
      String labelKey,
      ResourceBundleUtil labels) {
    return createSelectionColorButton(
        editor, attributeKey, swatches, columnCount, labelKey, labels, null);
  }

  public static JPopupButton createSelectionColorButton(
      DrawingEditor editor,
      AttributeKey<Color> attributeKey,
      java.util.List<ColorIcon> swatches,
      int columnCount,
      String labelKey,
      ResourceBundleUtil labels,
      Map<AttributeKey<?>, Object> defaultAttributes) {
    return createSelectionColorButton(
        editor,
        attributeKey,
        swatches,
        columnCount,
        labelKey,
        labels,
        defaultAttributes,
        new Rectangle(1, 17, 20, 4));
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
    return createSelectionColorButton(
        editor,
        attributeKey,
        swatches,
        columnCount,
        labelKey,
        labels,
        defaultAttributes,
        colorShape,
        new ArrayList<>());
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
    final JPopupButton popupButton = new JPopupButton();
    popupButton.setPopupAlpha(1f);
    if (defaultAttributes == null) {
      defaultAttributes = new HashMap<>();
    }
    popupButton.setColumnCount(columnCount, false);
    boolean hasNullColor = false;
    for (ColorIcon swatch : swatches) {
      AttributeAction a;
      HashMap<AttributeKey<?>, Object> attributes = new HashMap<>(defaultAttributes);
      if (swatch != null) {
        Color swatchColor = swatch.getColor();
        attributes.put(attributeKey, swatchColor);
        if (swatchColor == null || swatchColor.getAlpha() == 0) {
          hasNullColor = true;
        }
        popupButton.add(
            a = new AttributeAction(
                editor, attributes, labels.getToolTipTextProperty(labelKey), swatch));
        a.putValue(Action.SHORT_DESCRIPTION, swatch.getName());
        a.setUpdateEnabledState(false);
        dsp.add(a);
      } else {
        popupButton.add(new JPanel());
      }
    }
    // No color
    if (!hasNullColor) {
      AttributeAction a;
      HashMap<AttributeKey<?>, Object> attributes = new HashMap<>(defaultAttributes);
      attributes.put(attributeKey, null);
      popupButton.add(
          a = new AttributeAction(
              editor,
              attributes,
              labels.getToolTipTextProperty("attribute.color.noColor"),
              new ColorIcon(null, labels.getToolTipTextProperty("attribute.color.noColor"))));
      a.putValue(
          Action.SHORT_DESCRIPTION, labels.getToolTipTextProperty("attribute.color.noColor"));
      a.setUpdateEnabledState(false);
      dsp.add(a);
    }
    // Color chooser
    ImageIcon chooserIcon = new ImageIcon(Images.createImage(
        ColorButtonFactory.class,
        "/org/jhotdraw/draw/action/images/attribute.color.colorChooser.png"));
    AttributeAction a;
    popupButton.add(
        a = new SelectionColorChooserAction(
            editor,
            attributeKey,
            labels.getToolTipTextProperty("attribute.color.colorChooser"),
            chooserIcon,
            defaultAttributes));
    a.putValue(
        Action.SHORT_DESCRIPTION, labels.getToolTipTextProperty("attribute.color.colorChooser"));
    dsp.add(a);
    labels.configureToolBarButton(popupButton, labelKey);
    Icon icon = new SelectionColorIcon(
        editor,
        attributeKey,
        labels.getLargeIconProperty(labelKey, ColorButtonFactory.class).getImage(),
        colorShape);
    popupButton.setIcon(icon);
    popupButton.setDisabledIcon(icon);
    popupButton.setFocusable(false);
    dsp.add(new SelectionComponentRepainter(editor, popupButton));
    return popupButton;
  }

  // ---------------------------------------------------------------------------
  // createSelectionColorChooserButton
  // ---------------------------------------------------------------------------

  public static JPopupButton createSelectionColorChooserButton(
      final DrawingEditor editor,
      final AttributeKey<Color> attributeKey,
      String labelKey,
      ResourceBundleUtil labels,
      Map<AttributeKey<?>, Object> defaultAttributes,
      Shape colorShape,
      final java.util.List<Disposable> dsp) {
    return createSelectionColorChooserButton(
        editor, attributeKey, labelKey, labels, defaultAttributes, colorShape, null, dsp);
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
    JPopupButton popupButton;
    popupButton = new JPopupButton();
    labels.configureToolBarButton(popupButton, labelKey);
    popupButton.setFocusable(true);
    popupButton.setRequestFocusEnabled(false);
    // We lazily initialize the popup menu because creating a JColorChooser
    // takes a lot of time.
    JComponentPopup popupMenu = new JComponentPopup() {
      private static final long serialVersionUID = 1L;
      private JColorChooser colorChooser;

      @Override
      public void show(Component invoker, int x, int y) {
        if (colorChooser == null) {
          initialize();
        }
        Color c;
        if (editor.getActiveView() != null && editor.getActiveView().getSelectionCount() > 0) {
          c = editor
              .getActiveView()
              .getSelectedFigures()
              .iterator()
              .next()
              .attr()
              .get(attributeKey);
        } else {
          c = editor.getDefaultAttribute(attributeKey);
        }
        colorChooser.setColor(c == null ? new Color(0, true) : c);
        super.show(invoker, x, y);
      }

      private void initialize() {
        colorChooser = new JColorChooser();
        colorChooser.setOpaque(true);
        colorChooser.setBackground(Color.WHITE);
        if (uiclass != null) {
          try {
            colorChooser.setUI((ColorChooserUI) Methods.invokeStatic(
                uiclass, "createUI", new Class<?>[] {JComponent.class}, new Object[] {colorChooser
                }));
          } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
          }
        }
        dsp.add(new SelectionColorChooserHandler(editor, attributeKey, colorChooser, this));
        add(colorChooser);
      }
    };
    popupButton.setPopupMenu(popupMenu);
    popupButton.setPopupAlpha(1.0f); // must be set after we set the popup menu
    Icon icon = new SelectionColorIcon(
        editor,
        attributeKey,
        labels.getLargeIconProperty(labelKey, ColorButtonFactory.class).getImage(),
        colorShape);
    popupButton.setIcon(icon);
    popupButton.setDisabledIcon(icon);
    popupButton.setFocusable(false);
    if (dsp != null) {
      dsp.add(new SelectionComponentRepainter(editor, popupButton));
    }
    return popupButton;
  }

  // ---------------------------------------------------------------------------
  // createDrawingColorButton
  // ---------------------------------------------------------------------------

  public static JPopupButton createDrawingColorButton(
      DrawingEditor editor,
      AttributeKey<Color> attributeKey,
      java.util.List<ColorIcon> swatches,
      int columnCount,
      String labelKey,
      ResourceBundleUtil labels) {
    return createDrawingColorButton(
        editor, attributeKey, swatches, columnCount, labelKey, labels, null);
  }

  public static JPopupButton createDrawingColorButton(
      DrawingEditor editor,
      AttributeKey<Color> attributeKey,
      java.util.List<ColorIcon> swatches,
      int columnCount,
      String labelKey,
      ResourceBundleUtil labels,
      Map<AttributeKey<?>, Object> defaultAttributes) {
    return createDrawingColorButton(
        editor,
        attributeKey,
        swatches,
        columnCount,
        labelKey,
        labels,
        defaultAttributes,
        new Rectangle(1, 17, 20, 4));
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
    return createDrawingColorButton(
        editor,
        attributeKey,
        swatches,
        columnCount,
        labelKey,
        labels,
        defaultAttributes,
        colorShape,
        new ArrayList<>());
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
    final JPopupButton popupButton = new JPopupButton();
    popupButton.setPopupAlpha(1f);
    if (defaultAttributes == null) {
      defaultAttributes = new HashMap<>();
    }
    popupButton.setColumnCount(columnCount, false);
    boolean hasNullColor = false;
    for (ColorIcon swatch : swatches) {
      DrawingAttributeAction a;
      HashMap<AttributeKey<?>, Object> attributes = new HashMap<>(defaultAttributes);
      if (swatch != null) {
        Color swatchColor = swatch.getColor();
        attributes.put(attributeKey, swatchColor);
        if (swatchColor == null || swatchColor.getAlpha() == 0) {
          hasNullColor = true;
        }
        popupButton.add(
            a = new DrawingAttributeAction(
                editor, attributes, labels.getToolTipTextProperty(labelKey), swatch));
        dsp.add(a);
        a.putValue(Action.SHORT_DESCRIPTION, swatch.getName());
        a.setUpdateEnabledState(false);
      } else {
        popupButton.add(new JPanel());
      }
    }
    // No color
    if (!hasNullColor) {
      DrawingAttributeAction a;
      HashMap<AttributeKey<?>, Object> attributes = new HashMap<>(defaultAttributes);
      attributes.put(attributeKey, null);
      popupButton.add(
          a = new DrawingAttributeAction(
              editor,
              attributes,
              labels.getToolTipTextProperty("attribute.color.noColor"),
              new ColorIcon(null, labels.getToolTipTextProperty("attribute.color.noColor"))));
      dsp.add(a);
      a.putValue(
          Action.SHORT_DESCRIPTION, labels.getToolTipTextProperty("attribute.color.noColor"));
      a.setUpdateEnabledState(false);
    }
    // Color chooser
    ImageIcon chooserIcon = new ImageIcon(Images.createImage(
        ColorButtonFactory.class,
        "/org/jhotdraw/draw/action/images/attribute.color.colorChooser.png"));
    DrawingColorChooserAction a;
    popupButton.add(
        a = new DrawingColorChooserAction(
            editor, attributeKey, "color", chooserIcon, defaultAttributes));
    dsp.add(a);
    labels.configureToolBarButton(popupButton, labelKey);
    a.putValue(
        Action.SHORT_DESCRIPTION, labels.getToolTipTextProperty("attribute.color.colorChooser"));
    Icon icon = new DrawingColorIcon(
        editor,
        attributeKey,
        labels.getLargeIconProperty(labelKey, ColorButtonFactory.class).getImage(),
        colorShape);
    popupButton.setIcon(icon);
    popupButton.setDisabledIcon(icon);
    popupButton.setFocusable(false);
    if (editor != null) {
      dsp.add(new SelectionComponentRepainter(editor, popupButton));
    }
    return popupButton;
  }

  // ---------------------------------------------------------------------------
  // createDrawingColorChooserButton
  // ---------------------------------------------------------------------------

  public static JPopupButton createDrawingColorChooserButton(
      final DrawingEditor editor,
      final AttributeKey<Color> attributeKey,
      String labelKey,
      ResourceBundleUtil labels,
      Map<AttributeKey<?>, Object> defaultAttributes,
      Shape colorShape,
      final java.util.List<Disposable> dsp) {
    return createSelectionColorChooserButton(
        editor, attributeKey, labelKey, labels, defaultAttributes, colorShape, null, dsp);
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
    JPopupButton popupButton;
    popupButton = new JPopupButton();
    labels.configureToolBarButton(popupButton, labelKey);
    popupButton.setFocusable(true);
    popupButton.setRequestFocusEnabled(false);
    // We lazily initialize the popup menu because creating a JColorChooser
    // takes a lot of time.
    JComponentPopup popupMenu = new JComponentPopup() {
      private static final long serialVersionUID = 1L;
      private JColorChooser colorChooser;

      @Override
      public void show(Component invoker, int x, int y) {
        if (colorChooser == null) {
          initialize();
        }
        Color c;
        if (editor.getActiveView() != null) {
          c = editor.getActiveView().getDrawing().attr().get(attributeKey);
        } else {
          c = editor.getDefaultAttribute(attributeKey);
        }
        colorChooser.setColor(c == null ? new Color(0, true) : c);
        super.show(invoker, x, y);
      }

      private void initialize() {
        colorChooser = new JColorChooser();
        colorChooser.setOpaque(true);
        colorChooser.setBackground(Color.WHITE);
        if (uiclass != null) {
          try {
            colorChooser.setUI((ColorChooserUI) Methods.invokeStatic(
                uiclass, "createUI", new Class<?>[] {JComponent.class}, new Object[] {colorChooser
                }));
          } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
          }
        }
        dsp.add(new DrawingColorChooserHandler(editor, attributeKey, colorChooser, this));
        add(colorChooser);
      }
    };
    popupButton.setPopupMenu(popupMenu);
    popupButton.setPopupAlpha(1.0f); // must be set after we set the popup menu
    Icon icon = new DrawingColorIcon(
        editor,
        attributeKey,
        labels.getLargeIconProperty(labelKey, ColorButtonFactory.class).getImage(),
        colorShape);
    popupButton.setIcon(icon);
    popupButton.setDisabledIcon(icon);
    popupButton.setFocusable(false);
    if (dsp != null) {
      dsp.add(new SelectionComponentRepainter(editor, popupButton));
    }
    return popupButton;
  }
}
