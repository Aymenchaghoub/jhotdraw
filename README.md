# JHotDraw

[![CI](https://github.com/Aymenchaghoub/jhotdraw/actions/workflows/ci.yml/badge.svg)](https://github.com/Aymenchaghoub/jhotdraw/actions/workflows/ci.yml)
![Java 17](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Maven 3.9+](https://img.shields.io/badge/Maven-3.9%2B-C71A36?logo=apachemaven&logoColor=white)
![License](https://img.shields.io/badge/License-LGPL%202.1%20%2B%20CC%202.5-blue)

## Overview

JHotDraw is a Java framework for building structured 2D editors and diagramming applications.
This repository is an actively maintained fork targeting Java 17 and a multi-module Maven build.

Typical use cases:

- vector drawing editors
- technical diagram tools
- custom modeling and visualization UIs

## Quick Start

### Prerequisites

- Java 17 (Temurin/OpenJDK)
- Maven 3.9+
- Git

### Clone

```bash
git clone https://github.com/Aymenchaghoub/jhotdraw.git
cd jhotdraw
```

### Build and Test

```bash
mvn -B clean install
mvn -B test
```

### Run a Sample

Preferred (IDE): run class `org.jhotdraw.samples.mini.EditorSample` from module `jhotdraw-samples-mini`.

CLI option (Maven Exec Plugin):

```bash
mvn -pl jhotdraw-samples/jhotdraw-samples-mini -am \
  -Dexec.mainClass=org.jhotdraw.samples.mini.EditorSample \
  org.codehaus.mojo:exec-maven-plugin:3.5.0:java
```

## Architecture

JHotDraw uses a modular Maven layout. Core responsibilities are split by concern.

- `jhotdraw-core`: drawing model, figures, handles, tools, interactions
- `jhotdraw-gui`: reusable Swing UI components and integration helpers
- `jhotdraw-io`: import/export, DOM/XML serialization, persistence formats
- `jhotdraw-samples`: runnable demonstrations (`mini`, `misc`)
- `jhotdraw-utils`: shared utility classes used across modules

```text
jhotdraw/
|-- jhotdraw-utils/
|-- jhotdraw-datatransfer/
|-- jhotdraw-api/
|-- jhotdraw-core/
|-- jhotdraw-actions/
|-- jhotdraw-gui/
|-- jhotdraw-app/
|-- jhotdraw-xml/
|-- jhotdraw-io/
`-- jhotdraw-samples/
    |-- jhotdraw-samples-mini/
    `-- jhotdraw-samples-misc/
```

## Usage

### Maven Dependency

```xml
<dependency>
  <groupId>org.jhotdraw</groupId>
  <artifactId>jhotdraw-core</artifactId>
  <version>10.3-SNAPSHOT</version>
</dependency>
```

### Minimal Example

```java
import java.awt.geom.Point2D;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.figure.DefaultDrawing;
import org.jhotdraw.draw.figure.RectangleFigure;

public class QuickStartExample {
  public static void main(String[] args) {
    // Create a drawing canvas model.
    Drawing drawing = new DefaultDrawing();

    // Add one rectangle figure.
    RectangleFigure rectangle = new RectangleFigure();
    rectangle.setBounds(new Point2D.Double(10, 10), new Point2D.Double(140, 90));
    drawing.add(rectangle);

    System.out.println("Drawing created with " + drawing.getChildren().size() + " figure(s)");
  }
}
```

## CI and Quality

The CI workflow is defined in `.github/workflows/ci.yml` and runs on `push` and `pull_request` to `develop`.

It executes:

- `mvn -B clean install`
- `mvn -B org.jacoco:jacoco-maven-plugin:prepare-agent test`
- `mvn -B org.jacoco:jacoco-maven-plugin:report`

JaCoCo XML and HTML reports are uploaded as workflow artifacts.

## Contributing

### Workflow

- Fork the repository and create a topic branch from `develop`.
- Branch naming suggestions: `feature/<topic>`, `fix/<issue>`, `refactor/<scope>`, `docs/<scope>`.
- Keep commits atomic and use Conventional Commit prefixes (`feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `ci:`).

### Local Validation Before PR

```bash
mvn -B clean install
mvn -B test
```

### Pull Request Checklist

- clear title and motivation
- focused diff (single concern)
- tests updated when behavior changes
- CI green before merge

## Recent History (Phase 2)

- Refactored `SVGOutputFormat` into figure-writer strategies and extracted `SvgAttributeWriter`.
- Added dedicated regression tests for SVG output (`rect`, `path`, `text`, gradient reuse).
- Extracted `FigureSelectionModel` from `DefaultDrawingView` to improve SRP and testability.
- Decomposed `SVGInputFormat.toPath` with dedicated command handlers.
- Added a full CI pipeline for `develop` with JaCoCo report artifacts.

## License

This project includes material under:

- GNU Lesser General Public License v2.1
- Creative Commons Attribution 2.5

See [LICENSE](LICENSE) for details.

## Links

- Upstream project: https://github.com/wumpz/jhotdraw
- Fork repository: https://github.com/Aymenchaghoub/jhotdraw
- Phase 1 report: available in course deliverables
- Phase 2 report: available in course deliverables
