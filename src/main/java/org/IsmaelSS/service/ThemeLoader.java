package org.IsmaelSS.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.IsmaelSS.model.Question;
import org.IsmaelSS.model.Theme;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class ThemeLoader {
    private static final Logger LOG = Logger.getLogger(ThemeLoader.class.getName());
    private static final String THEMES_DIR = System.getProperty("user.dir") + File.separator + "themes";

    private final ObjectMapper mapper;

    public ThemeLoader() {
        this.mapper = new ObjectMapper();
    }

    public List<Theme> loadAllThemes() {
        List<Theme> themes = new ArrayList<>();
        File themesDir = new File(THEMES_DIR);

        if (!themesDir.exists() || !themesDir.isDirectory()) {
            LOG.warning("Themes directory not found: " + THEMES_DIR);
            return themes;
        }

        File[] jsonFiles = themesDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (jsonFiles == null || jsonFiles.length == 0) {
            LOG.info("No JSON files found in themes directory");
            return themes;
        }

        for (File file : jsonFiles) {
            try {
                Theme theme = loadTheme(file);
                if (theme != null) {
                    themes.add(theme);
                }
            } catch (Exception e) {
                LOG.warning("Skipping invalid theme file: " + file.getName() + " - " + e.getMessage());
            }
        }

        return themes;
    }

    /**
     * Writes a list of questions to themes/{name}.json.
     * Creates the themes directory if it does not exist.
     */
    public void saveTheme(String name, List<Question> questions) {
        File themesDir = new File(THEMES_DIR);
        if (!themesDir.exists()) {
            themesDir.mkdirs();
        }
        File file = new File(themesDir, name + ".json");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, questions);
            LOG.info("Saved theme: " + file.getAbsolutePath());
        } catch (IOException e) {
            LOG.warning("Failed to save theme: " + file.getName() + " - " + e.getMessage());
        }
    }

    /**
     * Deletes themes/{name}.json if it exists.
     */
    public void deleteTheme(String name) {
        File file = new File(THEMES_DIR, name + ".json");
        if (file.exists()) {
            if (file.delete()) {
                LOG.info("Deleted theme: " + file.getName());
            } else {
                LOG.warning("Failed to delete theme: " + file.getName());
            }
        }
    }

    private Theme loadTheme(File file) throws IOException {
        String name = file.getName().replaceAll("\\.json$", "");

        List<Question> questions = mapper.readValue(file, new TypeReference<List<Question>>() {});

        if (questions == null || questions.isEmpty()) {
            LOG.warning("Empty question list in file: " + file.getName());
            return null;
        }

        Set<Integer> seenIds = new HashSet<>();
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            if (q.getQuestion() == null || q.getOptions() == null || q.getOptions().size() != 5) {
                LOG.warning("Invalid question at index " + i + " in file: " + file.getName());
                return null;
            }
            if (q.getCorrect() < 0 || q.getCorrect() >= q.getOptions().size()) {
                LOG.warning("Invalid correct answer index at question " + i + " in file: " + file.getName());
                return null;
            }
            if (q.getId() < 0) {
                LOG.warning("Question at index " + i + " in file: " + file.getName() + " has invalid id: " + q.getId());
                return null;
            }
            if (!seenIds.add(q.getId())) {
                LOG.warning("Duplicate question id " + q.getId() + " at index " + i + " in file: " + file.getName());
                return null;
            }
        }

        return new Theme(name, questions);
    }
}
