package org.IsmaelSS.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Question {
    private String id;
    private String question;
    private List<String> options;
    private int correct;

    public Question() {}

    public Question(@JsonProperty("id") String id,
                    @JsonProperty("question") String question,
                    @JsonProperty("options") List<String> options,
                    @JsonProperty("correct") int correct) {
        this.id = id;
        this.question = question;
        this.options = options;
        this.correct = correct;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public int getCorrect() { return correct; }
    public void setCorrect(int correct) { this.correct = correct; }
}
