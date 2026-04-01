/*
 * Copyright (C) 2026 JHotDraw.
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
package org.jhotdraw.draw;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.jhotdraw.draw.event.FigureSelectionEvent;
import org.jhotdraw.draw.figure.RectangleFigure;
import org.junit.jupiter.api.Test;

public class DefaultFigureSelectionModelTest {

  @Test
  void addAndRemoveSelectionShouldUpdateModelAndFireEvents() {
    DefaultDrawingView view = new DefaultDrawingView();
    DefaultFigureSelectionModel model = new DefaultFigureSelectionModel(view);
    RectangleFigure figure = new RectangleFigure();
    AtomicInteger eventCount = new AtomicInteger(0);

    model.addFigureSelectionListener(evt -> eventCount.incrementAndGet());

    model.addToSelection(figure);
    assertThat(model.isFigureSelected(figure)).isTrue();
    assertThat(model.getSelectionCount()).isEqualTo(1);

    model.removeFromSelection(figure);
    assertThat(model.isSelectionEmpty()).isTrue();
    assertThat(eventCount.get()).isEqualTo(2);
  }

  @Test
  void clearSelectionShouldEmitSingleEventWithExpectedTransition() {
    DefaultDrawingView view = new DefaultDrawingView();
    DefaultFigureSelectionModel model = new DefaultFigureSelectionModel(view);
    RectangleFigure f1 = new RectangleFigure();
    RectangleFigure f2 = new RectangleFigure();

    AtomicInteger eventCount = new AtomicInteger(0);
    final FigureSelectionEvent[] lastEvent = new FigureSelectionEvent[1];
    model.addFigureSelectionListener(evt -> {
      eventCount.incrementAndGet();
      lastEvent[0] = evt;
    });

    model.addToSelection(Set.of(f1, f2));
    model.clearSelection();

    assertThat(model.isSelectionEmpty()).isTrue();
    assertThat(eventCount.get()).isEqualTo(2);
    assertThat(lastEvent[0]).isNotNull();
    assertThat(lastEvent[0].getOldSelection()).containsExactlyInAnyOrder(f1, f2);
    assertThat(lastEvent[0].getNewSelection()).isEmpty();
  }
}
