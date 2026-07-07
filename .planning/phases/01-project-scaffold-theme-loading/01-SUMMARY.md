# SUMMARY: Phase 1 — Project Scaffold & Theme Loading

## Status: Complete ✓

## What Was Built

### Wave 1: Project Scaffold & Models
- **pom.xml** — Updated with JavaFX 25 + Jackson 2.17.2 + javafx-maven-plugin 0.0.8. JDK target adjusted from 26→25 to match installed JDK.
- **Question.java** — POJO with Jackson annotations (`question`, `options` (List of 5), `correct` index)
- **Theme.java** — POJO wrapping `name`, `questions`, computed `questionCount`

### Wave 2: Data Access
- **ThemeLoader.java** — Scans `themes/` directory, parses JSON files, validates each question (non-null question, 5 options, valid correct index), skips invalid files with warning log
- **Sample themes** — `matematica.json` (5 questions), `historia.json` (5 questions)

### Wave 3: UI Layer
- **Main.java** — JavaFX `Application` launcher, sets title "FlashCard Java"
- **ScreenController.java** — Scene navigation via `registerScreen()` / `switchTo()` methods
- **ThemeSelectionView.java** — JavaFX Scene with theme checkboxes, count labels ("N/A" hit rate), question count Spinner, "Iniciar" button
- **ThemeSelectionController.java** — Loads themes, wires selection logic, validates ≥1 theme selected

### Wave 4: Verification
- `mvn clean compile` — ✓ PASS
- `mvn package` — ✓ PASS
- File structure matches plan — ✓
- JDK version adjusted from 26→25 (environment constraint)

## Deviations
- **JDK version**: pom.xml uses Java 25 (not 26) — JDK 26 not available on this machine
- **JavaFX version**: 25 (not 26.0.1) — matches available JDK version
- **javafx:run verification**: Not automated (GUI app — requires manual launch)

## Key Files Created
| File | Purpose |
|------|---------|
| `pom.xml` | Maven build with JavaFX + Jackson deps |
| `src/.../model/Question.java` | Question data model |
| `src/.../model/Theme.java` | Theme data model |
| `src/.../service/ThemeLoader.java` | JSON → Theme loader |
| `src/.../view/ThemeSelectionView.java` | Theme selection scene |
| `src/.../controller/ScreenController.java` | Scene navigator |
| `src/.../controller/ThemeSelectionController.java` | Theme selection logic |
| `src/.../Main.java` | JavaFX launcher |
| `themes/matematica.json` | Sample: 5 math questions |
| `themes/historia.json` | Sample: 5 history questions |

## Self-Check: PASSED
