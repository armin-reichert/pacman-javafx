/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.tinylog.Logger;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class ScoreManager {

    private final Score score = new Score();
    private final Score highScore = new Score();
    private Set<Integer> specialScores = Set.of();
    private final BooleanProperty scoreEnabledPy = new SimpleBooleanProperty(true);
    private final BooleanProperty highScoreEnabledPy= new SimpleBooleanProperty(true);
    private File highScoreFile;
    private Consumer<Integer> specialScoreReachedAction;

    public ScoreManager() {
        specialScoreReachedAction = score -> Logger.info("Reached {} points", score);
    }

    public void scorePoints(int points) {
        if (!isScoreEnabled()) {
            return;
        }
        int oldScore = score.points();
        int newScore = oldScore + points;
        score.setPoints(newScore);
        if (isHighScoreEnabled()) {
            if (newScore > highScore.points()) {
                highScore.setPoints(newScore);
                highScore.setLevelNumber(score.levelNumber());
                highScore.setDate(LocalDate.now());
            }
        }
        for (Integer extraLifeScore : specialScores) {
            if (oldScore < extraLifeScore && newScore >= extraLifeScore) {
                specialScoreReachedAction.accept(extraLifeScore);
                break;
            }
        }
    }

    public void loadHighScore() {
        highScore.read(highScoreFile);
        Logger.info("Highscore loaded. File: '{}', {} points, level {}",
            highScoreFile, highScore.points(), highScore.levelNumber());
    }

    public void updateHighScore() {
        var oldHighScore = new Score();
        oldHighScore.read(highScoreFile);
        if (highScore.points() > oldHighScore.points()) {
            highScore.save(highScoreFile, "High Score, last update %s".formatted(LocalTime.now()));
        }
    }

    public void resetScore() {
        score.reset();
    }

    public void resetHighScore() {
        new Score().save(highScoreFile, "High Score, %s".formatted(LocalDateTime.now()));
    }


    public void setSpecialScores(Integer... scores) {
        specialScores = Set.of(scores);
    }

    public void setOnSpecialScoreReached(Consumer<Integer> action) {
        specialScoreReachedAction = requireNonNull(action);
    }

    public Score score() {
        return score;
    }

    public Score highScore() {
        return highScore;
    }

    public BooleanProperty scoreEnabledProperty() { return scoreEnabledPy; }

    public boolean isScoreEnabled() {
        return scoreEnabledPy.get();
    }

    public void setScoreEnabled(boolean enabled) {
        scoreEnabledProperty().set(enabled);
    }

    public BooleanProperty highScoreEnabledProperty() { return highScoreEnabledPy; }

    public boolean isHighScoreEnabled() { return highScoreEnabledProperty().get(); }

    public void setHighScoreEnabled(boolean enabled) { highScoreEnabledProperty().set(enabled); }

    public void setHighScoreFile(File highScoreFile) {
        this.highScoreFile = highScoreFile;
    }

    public void setLevelNumber(int levelNumber) {
        score.setLevelNumber(levelNumber);
    }
}