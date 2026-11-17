# Solitaire Collection

Solitaire Collection is a desktop card game application built with Java and JavaFX. It includes Klondike and Spider Solitaire, with multiple game options and a custom card design.

This project is being developed for CSC 360: Software Development with AI at Eastern Connecticut State University. The goal is to build a functional application while applying object-oriented design, unit testing, test-driven development, and version control.

## Game Modes

### Klondike Solitaire
- Turn 1 or Turn 3 card draw
- Standard seven-column tableau and four foundations
- Unlimited stock recycling

### Spider Solitaire
- One-suit, two-suit, or four-suit games
- Standard ten-column tableau
- Completed King-to-Ace sequences are removed from play

## Planned Features

- Drag-and-drop card movement
- Custom card faces and backs
- New game and restart options
- Undo moves
- Move counter and game timer
- Win detection

The application will use a simple interface with a dark green playing surface. The focus is on clear card layouts, responsive controls, and consistent gameplay across both modes.

## Design

The game logic will be separate from the JavaFX interface so it can be tested independently. Both game modes will share core classes for cards and piles, while Klondike and Spider will implement their own rules.

The initial object-oriented design is documented in [solitaire-collection.puml](solitaire-collection.puml). The diagram includes the game classes, their methods, and their relationships. It does not include JavaFX components.

## Technology

- Java
- JavaFX
- Apache Maven
- JUnit
- JaCoCo
- PlantUML
- Git and GitHub

## Development

The project will be built in stages, beginning with the underlying game logic and unit tests. The JavaFX interface and drag-and-drop controls will be added after the core game functionality is implemented. Documentation, test coverage reports, and an updated UML diagram will be generated as part of the Maven build process.

## Status

Initial design. Implementation has not started.