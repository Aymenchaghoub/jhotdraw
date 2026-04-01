/*
 * @(#)SvgFigureWriter.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.samples.svg.io;

import java.io.IOException;
import org.jhotdraw.draw.figure.Figure;
import org.w3c.dom.Element;

interface SvgFigureWriter {
  boolean supports(Figure figure);

  void write(SVGOutputFormat format, Element parent, Figure figure) throws IOException;
}
