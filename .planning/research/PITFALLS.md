# Pitfalls Research: FlashCard JavaFX

## Common Mistakes

### 1. JavaFX Module Path Issues
**Problem:** JavaFX modules not found at runtime when running from Maven.
**Prevention:** Ensure `javafx-maven-plugin` is configured with correct `mainClass`; verify `pom.xml` has all required JavaFX dependencies (`javafx-controls`, `javafx-fxml` if using FXML).
**Phase:** Phase 1 — project setup.

### 2. Ignoring JavaFX Application Thread
**Problem:** Background file I/O (reading JSON) blocks the UI thread, causing freezes.
**Prevention:** Use `Task<Void>` or `Platform.runLater()` for file operations; keep JSON loading in a background thread with a loading indicator.
**Phase:** Phase 1 — architecture decisions.

### 3. Hardcoded File Paths
**Problem:** Themes folder path hardcoded, breaks when running from different directories.
**Prevention:** Resolve `themes/` relative to the app's working directory or use `System.getProperty("user.dir")` / classpath-based resolution.
**Phase:** Phase 2 — theme loading.

### 4. JSON Format Coupling
**Problem:** Tight coupling between JSON parser and question model makes format changes painful.
**Prevention:** Use Jackson annotations on POJOs; define a clear JSON schema with version field for future migration.
**Phase:** Phase 2 — question model design.

### 5. Stats File Corruption
**Problem:** Concurrent writes to performance file during rapid answering causes data loss.
**Prevention:** Read once at startup, batch writes at round end or use atomic file operations.
**Phase:** Phase 3 — stats persistence.

### 6. Shuffling Without Seeding
**Problem:** Deterministic shuffle for testing is hard without a seed.
**Prevention:** Accept an optional `Random` seed parameter in shuffle methods; use `Collections.shuffle(list, random)`.
**Phase:** Phase 3 — round engine.

### 7. Not Handling Empty Themes
**Problem:** User creates a theme folder with no JSON files or malformed JSON.
**Prevention:** Validate each JSON file during theme discovery; show graceful error messages; skip invalid files.
**Phase:** Phase 2 — theme loading.
