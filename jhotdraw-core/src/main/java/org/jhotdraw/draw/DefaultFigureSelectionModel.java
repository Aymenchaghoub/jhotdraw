package org.jhotdraw.draw;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.event.EventListenerList;
import org.jhotdraw.draw.event.FigureSelectionEvent;
import org.jhotdraw.draw.event.FigureSelectionListener;
import org.jhotdraw.draw.figure.Figure;

/**
 * Default implementation of {@link FigureSelectionModel}.
 *
 * <p>It uses a {@link LinkedHashSet} internally to guarantee the order
 * of selection. It delegates event firing to the registered
 * {@link FigureSelectionListener}s.
 */
public class DefaultFigureSelectionModel implements FigureSelectionModel {

  /**
   * Holds the selected figures in an ordered set. The ordering reflects the
   * sequence that was used to select the figures.
   */
  private final Set<Figure> selectedFigures = new LinkedHashSet<>();

  private final Set<Figure> unmodifiableSelectedFigures =
      Collections.unmodifiableSet(selectedFigures);

  /**
   * Listeners for selection changes.
   */
  private final EventListenerList listenerList = new EventListenerList();

  /**
   * The DrawingView that will be provided as the source of FigureSelectionEvents.
   */
  private final DrawingView view;

  /**
   * Creates a new FigureSelectionModel.
   *
   * @param view the view that acts as the source for thrown selection events
   */
  public DefaultFigureSelectionModel(DrawingView view) {
    if (view == null) {
      throw new IllegalArgumentException("view must not be null");
    }
    this.view = view;
  }

  @Override
  public boolean isFigureSelected(Figure checkFigure) {
    return selectedFigures.contains(checkFigure);
  }

  @Override
  public void addToSelection(Figure figure) {
    Set<Figure> oldSelection = snapshotSelection();
    if (selectedFigures.add(figure)) {
      Set<Figure> newSelection = snapshotSelection();
      fireSelectionChanged(oldSelection, newSelection);
    }
  }

  @Override
  public void addToSelection(Collection<Figure> figures) {
    Set<Figure> oldSelection = snapshotSelection();
    boolean changed = false;
    for (Figure f : figures) {
      if (selectedFigures.add(f)) {
        changed = true;
      }
    }
    if (changed) {
      Set<Figure> newSelection = snapshotSelection();
      fireSelectionChanged(oldSelection, newSelection);
    }
  }

  @Override
  public void setSelection(Collection<Figure> figures) {
    Set<Figure> oldSelection = snapshotSelection();
    selectedFigures.clear();
    for (Figure f : figures) {
      selectedFigures.add(f);
    }
    // Also changed if figures were removed...
    if (oldSelection.size() != selectedFigures.size() || !oldSelection.equals(selectedFigures)) {
      Set<Figure> newSelection = snapshotSelection();
      fireSelectionChanged(oldSelection, newSelection);
    }
  }

  @Override
  public void removeFromSelection(Figure figure) {
    Set<Figure> oldSelection = snapshotSelection();
    if (selectedFigures.remove(figure)) {
      Set<Figure> newSelection = snapshotSelection();
      fireSelectionChanged(oldSelection, newSelection);
    }
  }

  @Override
  public void toggleSelection(Figure figure) {
    if (selectedFigures.contains(figure)) {
      removeFromSelection(figure);
    } else {
      addToSelection(figure);
    }
  }

  @Override
  public void clearSelection() {
    if (selectedFigures.isEmpty()) {
      return;
    }
    Set<Figure> oldSelection = snapshotSelection();
    selectedFigures.clear();
    Set<Figure> newSelection = snapshotSelection();
    fireSelectionChanged(oldSelection, newSelection);
  }

  private Set<Figure> snapshotSelection() {
    return new LinkedHashSet<>(selectedFigures);
  }

  @Override
  public Set<Figure> getSelectedFigures() {
    return unmodifiableSelectedFigures;
  }

  @Override
  public int getSelectionCount() {
    return selectedFigures.size();
  }

  @Override
  public boolean isSelectionEmpty() {
    return selectedFigures.isEmpty();
  }

  @Override
  public void addFigureSelectionListener(FigureSelectionListener fsl) {
    listenerList.add(FigureSelectionListener.class, fsl);
  }

  @Override
  public void removeFigureSelectionListener(FigureSelectionListener fsl) {
    listenerList.remove(FigureSelectionListener.class, fsl);
  }

  /**
   * Notifies all listeners that have registered interest for
   * notification on this event type.
   */
  protected void fireSelectionChanged(Set<Figure> oldValue, Set<Figure> newValue) {
    FigureSelectionEvent event = null;
    Object[] listeners = listenerList.getListenerList();

    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == FigureSelectionListener.class) {
        if (event == null) {
          event = new FigureSelectionEvent(view, oldValue, newValue);
        }
        ((FigureSelectionListener) listeners[i + 1]).selectionChanged(event);
      }
    }
  }
}
