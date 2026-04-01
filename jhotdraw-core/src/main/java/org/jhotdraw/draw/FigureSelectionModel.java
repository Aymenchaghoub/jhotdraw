package org.jhotdraw.draw;

import java.util.Collection;
import java.util.Set;
import org.jhotdraw.draw.event.FigureSelectionListener;
import org.jhotdraw.draw.figure.Figure;

/**
 * Defines selection operations for figures shown by a {@link DrawingView}.
 */
public interface FigureSelectionModel {

  boolean isFigureSelected(Figure checkFigure);

  void addToSelection(Figure figure);

  void addToSelection(Collection<Figure> figures);

  void setSelection(Collection<Figure> figures);

  void removeFromSelection(Figure figure);

  void toggleSelection(Figure figure);

  void clearSelection();

  Set<Figure> getSelectedFigures();

  int getSelectionCount();

  boolean isSelectionEmpty();

  void addFigureSelectionListener(FigureSelectionListener fsl);

  void removeFigureSelectionListener(FigureSelectionListener fsl);
}
