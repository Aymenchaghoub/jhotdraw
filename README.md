# jhotdraw

[![Java CI with Maven](https://github.com/wumpz/jhotdraw/actions/workflows/maven.yml/badge.svg)](https://github.com/wumpz/jhotdraw/actions/workflows/maven.yml)

## News

> **ATTENTION**: Due to the refactoring in 10.0-SNAPSHOT this version breaks API of JHotdraw. Some adaptions are needed, e.g.: attributes now using `attr()`, ...

* heavy restructuring of classes and interfaces and cleanup
  * removed FigureListener from some Figures and Handles
  * complete attribute handling of Figure moved in class Attributes, access over **attr()**
  * Drawing has no dependency to CompositeFigure anymore and implementations do not use 
   AbstractCompositeFigure implementations
  * Drawing has its own listener DrawingListener now instead of FigureListener and CompositeFigureListener
  * contains(point, scale) is now called to take view scale into account for finding figures
  * removed DEBUG mode and introduced some logging instead
  * removed DOMStorable from Drawing, Figure
  * introduced a new module **jhotdraw-io** for input output and dom storables
* JDK 17
* maven build process
* restructured project layout
  * introduced submodules

## Getting Started

### Prerequisites

* Java 17
* Maven

### Setup

```bash
git clone https://github.com/wumpz/jhotdraw.git
cd jhotdraw
```

This project's artifacts are not published to Maven Central yet.
Build locally first so all modules are installed in your local Maven repository.

## Build

```bash
mvn clean install
mvn test
```

## Architecture Overview

JHotDraw is a multi-module Maven project. The key modules are:

* **jhotdraw-core**: figure model, drawing infrastructure, tools, and editing behavior
* **jhotdraw-gui**: reusable Swing components and UI integration
* **jhotdraw-io**: import/export and XML/DOM-based persistence
* **jhotdraw-samples**: runnable sample applications showing usage patterns

## Project Structure

```text
jhotdraw/
├── jhotdraw-core/
├── jhotdraw-gui/
├── jhotdraw-io/
├── jhotdraw-samples/
│   ├── jhotdraw-samples-mini/
│   └── jhotdraw-samples-misc/
└── pom.xml
```

## Simple Example

Add a dependency to your application:

```xml
<dependency>
  <groupId>org.jhotdraw</groupId>
  <artifactId>jhotdraw-core</artifactId>
  <version>10.3-SNAPSHOT</version>
</dependency>
```

Minimal code snippet:

```java
Drawing drawing = new DefaultDrawing();
RectangleFigure rectangle = new RectangleFigure();
rectangle.setBounds(new Point2D.Double(10, 10), new Point2D.Double(140, 90));
drawing.add(rectangle);
```

In module `jhotdraw-samples-mini` are small examples mostly highlighting one aspect of JHotdraw usage.
Additionally, module `jhotdraw-samples-misc` contains more sophisticated examples.


## License

* LGPL V2.1
* Creative Commons Attribution 2.5 License

## History 

This is a fork of jhotdraw from http://sourceforge.net/projects/jhotdraw.
