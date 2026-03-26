/*
 * @(#)FontButtonFactory.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.gui.action;

import static org.jhotdraw.draw.AttributeKeys.FONT_BOLD;
import static org.jhotdraw.draw.AttributeKeys.FONT_FACE;
import static org.jhotdraw.draw.AttributeKeys.FONT_ITALIC;
import static org.jhotdraw.draw.AttributeKeys.FONT_UNDERLINE;

import java.awt.Font;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.text.StyledEditorKit;
import org.jhotdraw.api.app.Disposable;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.action.AttributeToggler;
import org.jhotdraw.gui.JComponentPopup;
import org.jhotdraw.gui.JFontChooser;
import org.jhotdraw.gui.JPopupButton;
import org.jhotdraw.utils.util.ActionUtil;
import org.jhotdraw.utils.util.ResourceBundleUtil;

/**
 * FontButtonFactory.
 *
 * <p>Specialized factory for font and text-style toolbar buttons (font chooser, bold, italic,
 * underline).
 * Extracted from {@link ButtonFactory} as part of the God class decomposition.
 *
 * <p>Design pattern:<br>
 * Name: Abstract Factory.<br>
 * Role: Concrete Factory (font subset).<br>
 * Partners: {@link ButtonFactory} as Facade.
 */
public class FontButtonFactory {

  /** Prevent instance creation. */
  private FontButtonFactory() {}

  // ---------------------------------------------------------------------------
  // addFontButtonsTo
  // ---------------------------------------------------------------------------

  public static void addFontButtonsTo(JToolBar bar, DrawingEditor editor) {
    bar.add(createFontButton(editor));
    bar.add(createFontStyleBoldButton(editor));
    bar.add(createFontStyleItalicButton(editor));
    bar.add(createFontStyleUnderlineButton(editor));
  }

  // ---------------------------------------------------------------------------
  // createFontButton
  // ---------------------------------------------------------------------------

  public static JPopupButton createFontButton(DrawingEditor editor) {
    return createFontButton(editor, ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels"));
  }

  public static JPopupButton createFontButton(DrawingEditor editor, ResourceBundleUtil labels) {
    return createFontButton(editor, FONT_FACE, labels);
  }

  public static JPopupButton createFontButton(
      DrawingEditor editor, AttributeKey<Font> key, ResourceBundleUtil labels) {
    return createFontButton(editor, key, labels, new ArrayList<>());
  }

  public static JPopupButton createFontButton(
      DrawingEditor editor,
      AttributeKey<Font> key,
      ResourceBundleUtil labels,
      java.util.List<Disposable> dsp) {
    JPopupButton fontPopupButton;
    fontPopupButton = new JPopupButton();
    labels.configureToolBarButton(fontPopupButton, "attribute.font");
    fontPopupButton.setFocusable(false);
    JComponentPopup popupMenu = new JComponentPopup();
    JFontChooser fontChooser = new JFontChooser();
    dsp.add(new FontChooserHandler(editor, key, fontChooser, popupMenu));
    popupMenu.add(fontChooser);
    fontPopupButton.setPopupMenu(popupMenu);
    fontPopupButton.setFocusable(false);
    return fontPopupButton;
  }

  // ---------------------------------------------------------------------------
  // createFontStyleBoldButton
  // ---------------------------------------------------------------------------

  public static JButton createFontStyleBoldButton(DrawingEditor editor) {
    return createFontStyleBoldButton(
        editor, ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels"));
  }

  public static JButton createFontStyleBoldButton(DrawingEditor editor, ResourceBundleUtil labels) {
    return createFontStyleBoldButton(editor, labels, new ArrayList<>());
  }

  public static JButton createFontStyleBoldButton(
      DrawingEditor editor, ResourceBundleUtil labels, java.util.List<Disposable> dsp) {
    JButton btn;
    btn = new JButton();
    labels.configureToolBarButton(btn, "attribute.fontStyle.bold");
    btn.setFocusable(false);
    AbstractAction a = new AttributeToggler<>(
        editor, FONT_BOLD, Boolean.TRUE, Boolean.FALSE, new StyledEditorKit.BoldAction());
    a.putValue(
        ActionUtil.UNDO_PRESENTATION_NAME_KEY, labels.getString("attribute.fontStyle.bold.text"));
    btn.addActionListener(a);
    return btn;
  }

  // ---------------------------------------------------------------------------
  // createFontStyleItalicButton
  // ---------------------------------------------------------------------------

  public static JButton createFontStyleItalicButton(DrawingEditor editor) {
    return createFontStyleItalicButton(
        editor, ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels"));
  }

  public static JButton createFontStyleItalicButton(
      DrawingEditor editor, ResourceBundleUtil labels) {
    return createFontStyleItalicButton(editor, labels, new ArrayList<>());
  }

  public static JButton createFontStyleItalicButton(
      DrawingEditor editor, ResourceBundleUtil labels, java.util.List<Disposable> dsp) {
    JButton btn;
    btn = new JButton();
    labels.configureToolBarButton(btn, "attribute.fontStyle.italic");
    btn.setFocusable(false);
    AbstractAction a = new AttributeToggler<>(
        editor, FONT_ITALIC, Boolean.TRUE, Boolean.FALSE, new StyledEditorKit.ItalicAction());
    a.putValue(
        ActionUtil.UNDO_PRESENTATION_NAME_KEY, labels.getString("attribute.fontStyle.italic.text"));
    btn.addActionListener(a);
    return btn;
  }

  // ---------------------------------------------------------------------------
  // createFontStyleUnderlineButton
  // ---------------------------------------------------------------------------

  public static JButton createFontStyleUnderlineButton(DrawingEditor editor) {
    return createFontStyleUnderlineButton(
        editor, ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels"));
  }

  public static JButton createFontStyleUnderlineButton(
      DrawingEditor editor, ResourceBundleUtil labels) {
    return createFontStyleUnderlineButton(editor, labels, new ArrayList<>());
  }

  public static JButton createFontStyleUnderlineButton(
      DrawingEditor editor, ResourceBundleUtil labels, java.util.List<Disposable> dsp) {
    JButton btn;
    btn = new JButton();
    labels.configureToolBarButton(btn, "attribute.fontStyle.underline");
    btn.setFocusable(false);
    AbstractAction a = new AttributeToggler<>(
        editor, FONT_UNDERLINE, Boolean.TRUE, Boolean.FALSE, new StyledEditorKit.UnderlineAction());
    a.putValue(
        ActionUtil.UNDO_PRESENTATION_NAME_KEY,
        labels.getString("attribute.fontStyle.underline.text"));
    btn.addActionListener(a);
    return btn;
  }
}
