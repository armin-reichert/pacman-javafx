/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.event.GameEventType;
import org.tinylog.Logger;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public class ScoreManager {

    private final Score score = new Score();
    private final Score highScore = new Score();
    private Set<Integer> extraLifeScores;
    private boolean scoreEnabled;
    private boolean highScoreEnabled;
    private File highScoreFile;

    public Score score() {
        return score;
    }

    public Score highScore() {
        return highScore;
    }

    public void resetScore() {
        score.reset();
    }

    public boolean isScoreEnabled() {
        return scoreEnabled;
    }

    public void setScoreEnabled(boolean scoreEnabled) {
        this.scoreEnabled = scoreEnabled;
    }

    public void setHighScoreEnabled(boolean highScoreEnabled) {
        this.highScoreEnabled = highScoreEnabled;
    }

    public void setHighScoreFile(File highScoreFile) {
        this.highScoreFile = highScoreFile;
    }

    public void resetHighScore() {
        new Score().save(highScoreFile, "High Score, reset at %s".formatted(LocalTime.now()));
    }

    public void setLevelNumber(int levelNumber) {
        score.setLevelNumber(levelNumber);
    }

    public void setExtraLifeScores(Integer... scores) {
        extraLifeScores = Set.of(scores);
    }

    public void scorePoints(GameModel game, int points) {
        if (!scoreEnabled) {
            return;
        }
        int oldScore = score.points();
        int newScore = oldScore + points;
        score.setPoints(newScore);
        if (highScoreEnabled) {
            if (newScore > highScore.points()) {
                highScore.setPoints(newScore);
                highScore.setLevelNumber(score.levelNumber());
                highScore.setDate(LocalDate.now());
            }
        }
        //TODO: this does not belong here
        for (Integer extraLifeScore : extraLifeScores) {
            if (oldScore < extraLifeScore && newScore >= extraLifeScore) {
                game.addLives(1);
                game.publishGameEvent(GameEventType.EXTRA_LIFE_WON);
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
}