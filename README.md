# Solitaire Collection

Solitaire Collection is a Java card game project with Klondike and Spider Solitaire. The current application provides game logic and a console demonstration; a JavaFX desktop interface is planned for Step 7.

This project is being developed for CSC 360: Software Development with AI at Eastern Connecticut State University. The goal is to build a functional application while applying object-oriented design, unit testing, test-driven development, and version control.

## Game Modes

### Klondike Solitaire
- Turn 1 or Turn 3 card draw
- Standard seven-column tableau and four foundations
- Unlimited stock recycling

### Spider Solitaire
- One-suit, two-suit, or four-suit games
- Standard ten-column tableau
- Same-suit descending sequences can be moved together
- Completed same-suit King-to-Ace sequences are removed from play
- Stock deals one card to each column when all columns are nonempty

## Implemented Features

- Shuffled new games and game-specific move validation
- Klondike stock draws and unlimited recycling
- Spider stock row deals and automatic completed-run removal
- Automatic exposure of newly uncovered tableau cards
- Move counting and win detection (four full Klondike foundations or eight completed Spider runs)

## Planned Features

- JavaFX interface and drag-and-drop card movement
- Custom card faces and backs
- New game and restart controls
- Undo moves
- Game timer

The application will use a simple interface with a dark green playing surface. The focus is on clear card layouts, responsive controls, and consistent gameplay across both modes.

## Design

The game logic is independent of JavaFX and is tested with JUnit. Both game modes share core classes for cards and piles, while Klondike and Spider implement their own rules.

The class diagram is generated automatically from `com.jd.solitaireapp`, including JavaFX components, during Maven's `process-classes` phase. Run `.\mvnw.cmd clean test-compile` from `solitaire-app` to generate `solitaire-app/target/generated-docs/classes.puml` (path relative to the repository root).

## Technology

- Java 23
- JavaFX (interface planned)
- Apache Maven
- JUnit
- JaCoCo
- PlantUML
- Git and GitHub

## Development

With JDK 23 installed and `JAVA_HOME` set, run these Maven Wrapper commands from the repository root in PowerShell:

```powershell
cd solitaire-app
.\mvnw.cmd clean package     # Build and run JUnit tests
.\mvnw.cmd clean test site   # Run tests and generate JavaDoc/JaCoCo reports
.\mvnw.cmd compile exec:java # Run the console demonstration
```

On macOS/Linux, replace `.\mvnw.cmd` with `sh ./mvnw`. Reports are generated at `solitaire-app/target/site/reports/apidocs/index.html` (JavaDoc) and `solitaire-app/target/site/jacoco/index.html` (JaCoCo). GitHub Actions also runs tests and generates these reports on each push. The generated UML diagram resides in `target` and is excluded from Git.

## Status

Klondike Turn 1/Turn 3 and Spider with 1, 2, or 4 suits are implemented in `solitaire-app`. Step 7 will add the JavaFX interface.

Undo is still unimplemented in this checkout: `GameSnapshot` stores no state, capture/restore and `undo()` throw `UnsupportedOperationException`, and undo history is never populated. The console demonstration covers Turn 1 and one-suit Spider and still prints outdated Step 4/6 notices.
