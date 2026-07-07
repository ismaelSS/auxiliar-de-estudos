# Stack Research: FlashCard JavaFX

## Recommended Stack

| Layer | Technology | Version | Rationale |
|-------|-----------|---------|-----------|
| Language | Java | 26 | Project already configured for Java 26 |
| UI Framework | JavaFX | 26.0.1 | User requirement; latest stable release compatible with JDK 26 |
| Build Tool | Maven | 3.x | Already configured (pom.xml exists) |
| JSON Parsing | Jackson | 2.17+ | Industry standard for JSON in Java; annotations for POJO mapping |
| Performance Storage | JSON (via Jackson) | — | Consistent with question format, no external DB needed |

## JavaFX Maven Dependencies

```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>26.0.1</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>26.0.1</version>
</dependency>
```

## JavaFX Maven Plugin

```xml
<plugin>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-maven-plugin</artifactId>
    <version>0.0.8</version>
    <configuration>
        <mainClass>org.IsmaelSS.Main</mainClass>
    </configuration>
</plugin>
```

## Jackson Dependency

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.17.2</version>
</dependency>
```

## Architecture Pattern

**MVC (Model-View-Controller)** — standard pattern for JavaFX desktop apps:
- **Model**: POJOs with JavaFX Properties for data binding
- **View**: FXML files + CSS styling (or programmatic SceneGraph construction)
- **Controller**: Java classes handling user actions, coordinating Model↔View

## Key Observations

- Java 26 includes JavaFX modules via OpenJFX; no separate SDK download needed with Maven
- `javafx-maven-plugin` 0.0.8 handles `javafx:run` goal
- FXML is optional; programmatic UI is valid and may be simpler for this project
- Jackson's `ObjectMapper` handles both reading/writing JSON question files and performance data
