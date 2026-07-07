# PLAN: Phase 1 — Project Scaffold & Theme Loading

## Goal
Users can launch the app and see a theme selection screen with auto-detected themes from JSON files.

## Success Criteria
1. App launches a JavaFX window displaying the theme selection screen
2. Themes auto-detected from JSON files in `themes/` folder
3. User sees theme list (with "N/A" hit rate), can check/uncheck themes, set question count
4. App compiles via `mvn clean compile` and runs via `mvn javafx:run`

---

## Wave 1: Project Scaffold & Models
*Dependencies: None*

### 1.1 Update pom.xml
- Add JavaFX dependencies: `javafx-controls` 26.0.1
- Add Jackson: `jackson-databind` 2.17.2
- Add `javafx-maven-plugin` 0.0.8 (mainClass = `org.IsmaelSS.Main`)

### 1.2 Create Question model
- **File:** `src/main/java/org/IsmaelSS/model/Question.java`
- POJO with: `String question`, `List<String> options`, `int correct`
- Jackson annotations for JSON mapping

### 1.3 Create Theme model
- **File:** `src/main/java/org/IsmaelSS/model/Theme.java`
- POJO with: `String name`, `List<Question> questions`, `int questionCount`
- Helper: `questionCount` computed from list size

---

## Wave 2: Data Access
*Dependencies: Wave 1*

### 2.1 Create ThemeLoader service
- **File:** `src/main/java/org/IsmaelSS/service/ThemeLoader.java`
- Methods: `scanThemesDirectory()` → discovers `.json` files; `loadTheme(String filename)` → parses JSON → `Theme`
- Edge cases: empty directory returns empty list; malformed JSON → skip with warning log
- Hit rate field: returns 0.0 / null (stub until Phase 3)

### 2.2 Create sample theme JSON files
- `themes/matematica.json` — 5 math questions (e.g., algebra, geometry)
- `themes/historia.json` — 5 history questions (e.g., Brazil, world history)

---

## Wave 3: UI Layer
*Dependencies: Wave 2*

### 3.1 Update Main.java as JavaFX launcher
- Extend `javafx.application.Application`
- `start(Stage)`: sets title "FlashCard Java", calls ScreenController to load theme selection
- Remove placeholder code

### 3.2 Create ScreenController
- **File:** `src/main/java/org/IsmaelSS/controller/ScreenController.java`
- Singleton: holds `Stage` reference, maps of screen names → scenes
- Methods: `registerScreen(String name, Scene scene)`, `switchTo(String name)`, `switchTo(String name, ThemeSelectionData data)`
- Phase 1: register ThemeSelectionView only

### 3.3 Create ThemeSelectionView
- **File:** `src/main/java/org/IsmaelSS/view/ThemeSelectionView.java`
- JavaFX Scene with:
  - Title label ("Selecione os temas")
  - Scrollable list of themes, each as a row: `[checkbox] ThemeName — (N perguntas) — Hit rate: N/A`
  - "Quantidade de questões por tema" spinner/field (min 1, max = max questions in any selected theme)
  - "Iniciar" button (action → switch to study round scene — placeholder in Phase 1)
- Layout: VBox with padding, organized sections

### 3.4 Create ThemeSelectionController
- **File:** `src/main/java/org/IsmaelSS/controller/ThemeSelectionController.java`
- Loads themes via ThemeLoader at init
- Binds: checkbox state ↔ selected themes; spinner value ↔ question count
- "Iniciar" button: validates at least one theme selected, then navigates (or shows message for Phase 1)

---

## Wave 4: Verification
*Dependencies: Wave 3*

### 4.1 Verify compilation
- Run `mvn clean compile` — must succeed

### 4.2 Verify JavaFX run
- Run `mvn javafx:run` — must show JavaFX window with theme selection screen

### 4.3 Verify theme auto-detection
- Add/remove JSON files in `themes/`, relaunch — theme list updates
- Verify empty themes folder shows appropriate state

### 4.4 Verify invalid JSON handling
- Put malformed JSON in a file in `themes/` — app skips it gracefully (logged warning, no crash)

## State After Phase 1
```
src/main/java/org/IsmaelSS/
├── Main.java                     # JavaFX launcher
├── controller/
│   ├── ScreenController.java     # Scene navigation
│   └── ThemeSelectionController.java  # Theme selection logic
├── model/
│   ├── Question.java             # Question POJO
│   └── Theme.java                # Theme POJO
├── service/
│   └── ThemeLoader.java          # JSON → Theme loader
└── view/
    └── ThemeSelectionView.java   # Theme selection scene

themes/
├── matematica.json               # Sample theme (5 questions)
└── historia.json                 # Sample theme (5 questions)
```
