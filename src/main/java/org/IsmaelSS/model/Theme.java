package org.IsmaelSS.model;

import java.util.List;

public class Theme {
    private String name;
    private List<Question> questions;

    public Theme() {}

    public Theme(String name, List<Question> questions) {
        this.name = name;
        this.questions = questions;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    public int getQuestionCount() {
        return questions != null ? questions.size() : 0;
    }
}
