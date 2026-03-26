/*
 * @(#)AlignmentButtonFactory.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.gui.action;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.AbstractButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.jhotdraw.api.app.Disposable;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.action.AbstractSelectedAction;
import org.jhotdraw.draw.action.AlignAction;
import org.jhotdraw.draw.action.BringToFrontAction;
import org.jhotdraw.draw.action.MoveAction;
import org.jhotdraw.draw.action.SendToBackAction;
import org.jhotdraw.utils.util.ResourceBundleUtil;

/**
 * AlignmentButtonFactory.
 *
 * <p>Specialized factory for alignment and ordering toolbar buttons (align, move, bring-to-front,
 * send-to-back, toggle grid).
 * Extracted from {@link ButtonFactory} as part of the God class decomposition.
 *
 * <p>Design pattern:<br>
 * Name: Abstract Factory.<br>
 * Role: Concrete Factory (alignment subset).<br>
 * Partners: {@link ButtonFactory} as Facade.
 */
public class AlignmentButtonFactory {

  /** Prevent instance creation. */
  private AlignmentButtonFactory() {}

  // ---------------------------------------------------------------------------
  // addAlignmentButtonsTo
  // ---------------------------------------------------------------------------

  /** Creates toolbar buttons and adds them to the specified JToolBar */
  public static void addAlignmentButtonsTo(JToolBar bar, final DrawingEditor editor) {
    addAlignmentButtonsTo(bar, editor, new ArrayList<>());
  }

  /** Creates toolbar buttons and adds them to the specified JToolBar. */
  public static void addAlignmentButtonsTo(
      JToolBar bar, final DrawingEditor editor, java.util.List<Disposable> dsp) {
    AbstractSelectedAction d;
    bar.add(d = new AlignAction.West(editor)).setFocusable(false);
    dsp.add(d);
    bar.add(d = new AlignAction.East(editor)).setFocusable(false);
    dsp.add(d);
    bar.add(d = new AlignAction.Horizontal(editor)).setFocusable(false);
    dsp.add(d);
    bar.add(d = new AlignAction.North(editor)).setFocusable(false);
    dsp.add(d);
    bar.add(d = new AlignAction.South(editor)).setFocusable(false);
    dsp.add(d);
    bar.add(d = new AlignAction.Vertical(editor)).setFocusable(false);
    dsp.add(d);
    bar.addSeparator();
    bar.add(d = new MoveAction.West(editor)).setFocusable(false);
    dsp.add(d);
    bar.add(d = new MoveAction.East(editor)).setFocusable(false);
    dsp.add(d);
    bar.add(d = new MoveAction.North(editor)).setFocusable(false);
    dsp.add(d);
    bar.add(d = new MoveAction.South(editor)).setFocusable(false);
    dsp.add(d);
    bar.addSeparator();
    bar.add(new BringToFrontAction(editor)).setFocusable(false);
    dsp.add(d);
    bar.add(new SendToBackAction(editor)).setFocusable(false);
    dsp.add(d);
  }

  // ---------------------------------------------------------------------------
  // createToggleGridButton
  // ---------------------------------------------------------------------------

  /** Creates a button which toggles between two GridConstrainer for a DrawingView. */
  public static AbstractButton createToggleGridButton(final DrawingView view) {
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
    final JToggleButton toggleButton;
    toggleButton = new JToggleButton();
    labels.configureToolBarButton(toggleButton, "view.toggleGrid");
    toggleButton.setFocusable(false);
    toggleButton.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        view.setConstrainerVisible(toggleButton.isSelected());
      }
    });
    view.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        // String constants are interned
        if ((evt.getPropertyName() == null && DrawingView.CONSTRAINER_VISIBLE_PROPERTY == null)
            || (evt.getPropertyName() != null
                && evt.getPropertyName().equals(DrawingView.CONSTRAINER_VISIBLE_PROPERTY))) {
          toggleButton.setSelected(view.isConstrainerVisible());
        }
      }
    });
    return toggleButton;
  }
}
