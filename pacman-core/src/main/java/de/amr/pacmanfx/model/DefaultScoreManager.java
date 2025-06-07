/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventType;
import org.tinylog.Logger;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static de.amr.pacmanfx.Globals.theGameEventManager;
import static de.amr.pacmanfx.Globals.theSimulationStep;
import static java.util.Objects.requireNonNull;

public class DefaultScoreManager implements ScoreManager {

    private final Score score = new Score();
    private final Score highScore = new Score();
    private File highScoreFile;
    private List<Integer> extraLifeScores = List.of();
    private boolean scoreVisible;

    public DefaultScoreManager() {}

    @Override
    public void setHighScoreFile(File highScoreFile) {
        this.highScoreFile = requireNonNull(highScoreFile);
    }

    @Override
    public void setExtraLifeScores(Integer... scores) {
        extraLifeScores = Arrays.stream(scores).toList();
    }

    @Override
    public boolean isScoreVisible() { return scoreVisible; }

    @Override
    public void setScoreVisible(boolean visible) {
        scoreVisible = visible;
    }

    @Override
    public void scorePoints(int points) {
        if (!score.isEnabled()) {
            return;
        }
        int oldScore = score.points(), newScore = oldScore + points;
        if (highScore().isEnabled() && newScore > highScore.points()) {
            highScore.setPoints(newScore);
            highScore.setLevelNumber(score.levelNumber());
            highScore.setDate(LocalDate.now());
        }
        score.setPoints(newScore);
    }

    @Override
    public void onScoreChanged(GameModel game, int oldScore, int newScore) {
        for (int extraLifeScore : extraLifeScores) {
            // has extra life score been crossed?
            if (oldScore < extraLifeScore && newScore >= extraLifeScore) {
                theSimulationStep().extraLifeWon = true;
                theSimulationStep().extraLifeScore = extraLifeScore;
                game.addLives(1);
                GameEvent event = new GameEvent(game, GameEventType.SPECIAL_SCORE_REACHED);
                event.setPayload("score", extraLifeScore); // just for testing payload implementation
                theGameEventManager().publishEvent(event);
                break;
            }
        }
    }

    @Override
    public void loadHighScore() {
        highScore.read(highScoreFile);
        Logger.info("High Score loaded from '{}': points={}, level={}", highScoreFile, highScore.points(), highScore.levelNumber());
    }

    @Override
    public void updateHighScore() {
        var oldHighScore = new Score();
        oldHighScore.read(highScoreFile);
        if (highScore.points() > oldHighScore.points()) {
            highScore.save(highScoreFile, "High Score, last update %s".formatted(LocalTime.now()));
        }
    }

    @Override
    public void resetScore() {
        score.reset();
    }

    @Override
    public void saveHighScore() {
        new Score().save(highScoreFile, "High Score, %s".formatted(LocalDateTime.now()));
    }

    @Override
    public Score score() {
        return score;
    }

    @Override
    public Score highScore() {
        return highScore;
    }

    @Override
    public void setScoreLevelNumber(int levelNumber) {
        score.setLevelNumber(levelNumber);
    }
}
