# Architecture Research: FlashCard JavaFX

## Component Diagram

```
┌─────────────────────────────────────────────────────┐
│                    Main (Launcher)                    │
├─────────────────────────────────────────────────────┤
│                    JavaFX Stage                       │
├─────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │
│  │ ThemeSelect  │  │  StudyRound  │  │   Reports   │  │
│  │   Screen     │  │   Screen     │  │   Screen    │  │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  │
│         │                │                │          │
│  ┌──────┴────────────────┴────────────────┴──────┐  │
│  │              ScreenController                    │  │
│  │         (Scene/Screen Navigation)                │  │
│  └──────────────────────┬─────────────────────────┘  │
│                         │                             │
├─────────────────────────┴───────────────────────────┤
│  ┌────────────────────────────────────────────────┐  │
│  │              Business Logic Layer                │  │
│  │  ┌──────────┐  ┌─────────────┐  ┌──────────┐    │  │
│  │  │ Question  │  │ Round Engine│  │  Stats   │    │  │
│  │  │  Service  │  │  (Shuffle,  │  │  Service │    │  │
│  │  │           │  │   no-rep,   │  │          │    │  │
│  │  │           │  │ reinforce)  │  │          │    │  │
│  │  └─────┬─────┘  └──────┬──────┘  └────┬─────┘    │  │
│  └────────┼───────────────┼──────────────┼──────────┘  │
│           │               │              │             │
├───────────┼───────────────┼──────────────┼───────────┤
│  ┌────────┴───────────────┴──────────────┴────────┐   │
│  │              Data Access Layer                    │  │
│  │  ┌────────────────┐  ┌───────────────────────┐   │  │
│  │  │  JsonReader/   │  │  PerformanceRepository │   │  │
│  │  │  ThemeLoader   │  │  (stats.json)          │   │  │
│  │  └────────┬───────┘  └───────────┬───────────┘   │  │
│  └───────────┼─────────────────────┼────────────────┘  │
│              │                     │                    │
└──────────────┼─────────────────────┼──────────────────┘
               │                     │
        ┌──────┴──────┐      ┌──────┴──────┐
        │  themes/    │      │flashcard-   │
        │  *.json     │      │stats.json   │
        └─────────────┘      └─────────────┘
```

## Data Flow

1. **App start** → `ThemeLoader` scans `themes/` folder → reads all `.json` files → discovers themes
2. **Theme selection** → User picks themes + question count → `RoundEngine` initializes
3. **Study round** → `RoundEngine` serves questions (random order, no repeats, alternativas shuffled) → user answers → feedback displayed → stats updated
4. **Reinforcement mode** → `RoundEngine` queries `StatsService` for highest-error questions → prioritizes those
5. **Reports** → `StatsService` reads performance data → shows per-theme and overall metrics

## File Structure

```
flashCardJava/
├── src/main/java/org/IsmaelSS/
│   ├── Main.java
│   ├── controller/
│   │   ├── ScreenController.java      (scene navigation)
│   │   ├── ThemeSelectionController.java
│   │   ├── StudyRoundController.java
│   │   └── ReportsController.java
│   ├── model/
│   │   ├── Question.java
│   │   ├── Theme.java
│   │   ├── RoundResult.java
│   │   └── PerformanceStats.java
│   ├── service/
│   │   ├── ThemeLoader.java           (JSON → Theme objects)
│   │   ├── RoundEngine.java           (shuffle, no-repeat, reinforcement)
│   │   └── StatsService.java          (read/write performance data)
│   └── view/
│       ├── ThemeSelectionView.java
│       ├── StudyRoundView.java
│       └── ReportsView.java
├── themes/                            (question JSON files)
│   ├── matematica.json
│   ├── historia.json
│   └── ...
├── flashcard-stats.json               (performance data)
└── pom.xml
```
