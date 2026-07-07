package org.IsmaelSS.controller;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import org.IsmaelSS.model.RoundResult;
import org.IsmaelSS.model.RoundState;
import org.IsmaelSS.service.StatsService;
import org.IsmaelSS.view.StudyRoundView;

import java.util.ArrayList;
import java.util.List;

public class StudyRoundController {
    private final RoundState roundState;
    private final StudyRoundView view;
    private final ScreenController screenController;
    private final StatsService statsService;
    private final List<RoundResult> results = new ArrayList<>();
    private Runnable onRoundEndCallback;

    public StudyRoundController(RoundState roundState, StudyRoundView view,
                                ScreenController screenController, StatsService statsService) {
        this.roundState = roundState;
        this.view = view;
        this.screenController = screenController;
        this.statsService = statsService;
    }

    public void setOnRoundEndCallback(Runnable callback) {
        this.onRoundEndCallback = callback;
    }

    public void initialize() {
        screenController.registerScreen("studyRound", view.getScene());

        view.setOnOptionClick(this::handleOptionClick);
        view.setOnExit(this::handleExit);
        view.setOnVoltar(this::handleExit);

        showCurrentQuestion();
        screenController.switchTo("studyRound");
    }

    private void showCurrentQuestion() {
        if (roundState.isComplete()) {
            showRoundComplete();
            return;
        }

        view.updateProgress("Questão " + (roundState.getCurrentIndex() + 1) + " / " + roundState.getSelectedQuestionsCount());
        view.setQuestion(
                roundState.getCurrentQuestion().getQuestion(),
                roundState.getCurrentOptions()
        );
    }

    private void handleOptionClick(int index) {
        boolean correct = roundState.checkAnswer(index);

        if (correct) {
            view.highlightCorrect(index);
        } else {
            view.highlightWrong(index, roundState.getCurrentCorrectIndex());
        }

        results.add(new RoundResult(
                roundState.getCurrentThemeName(),
                roundState.getCurrentQuestion().getQuestion(),
                correct
        ));

        view.disableOptions(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> {
            roundState.advanceToNext();
            showCurrentQuestion();
        });
        pause.play();
    }

    private void showRoundComplete() {
        if (!results.isEmpty()) {
            statsService.recordRound(results);
        }
        view.showRoundComplete(roundState.getCorrectCount(), roundState.getTotalAnswered());
    }

    private void handleExit() {
        if (!results.isEmpty()) {
            statsService.recordRound(results);
        }
        if (onRoundEndCallback != null) {
            onRoundEndCallback.run();
        } else {
            screenController.switchTo("themeSelection");
        }
    }
}
