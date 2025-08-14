package de.amr.pacmanfx.model;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventType;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class DefaultScoreManager implements ScoreManager {

    protected final Score score = new Score();
    protected final Score highScore = new Score();
    protected final File highScoreFile;
    protected Set<Integer> extraLifeScores = Set.of();

    public DefaultScoreManager(Game game, File highScoreFile) {
        this.highScoreFile = requireNonNull(highScoreFile);
        score.pointsProperty().addListener((py, ov, nv) -> onScoreChanged(game, ov.intValue(), nv.intValue()));
    }

    private void onScoreChanged(Game game, int oldScore, int newScore) {
        for (int extraLifeScore : extraLifeScores) {
            // has extra life score been crossed?
            if (oldScore < extraLifeScore && newScore >= extraLifeScore) {
                game.simulationStep().extraLifeWon = true;
                game.simulationStep().extraLifeScore = extraLifeScore;
                game.addLives(1);
                GameEvent event = new GameEvent(game, GameEventType.SPECIAL_SCORE_REACHED);
                game.eventManager().publishEvent(event);
                break;
            }
        }
    }

    @Override
    public void setExtraLifeScores(Set<Integer> scores) {
        extraLifeScores = new HashSet<>(scores);
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
    public void loadHighScore() {
        if (highScoreFile == null) {
            Logger.error("High Score file could not be opened: game variant not set?");
            return;
        }
        try {
            highScore.read(highScoreFile);
            Logger.info("High Score loaded from file '{}': points={}, level={}", highScoreFile, highScore.points(), highScore.levelNumber());
        } catch (IOException x) {
            Logger.error("High Score file could not be opened: '{}'", highScoreFile);
        }
    }

    @Override
    public void updateHighScore() {
        if (highScoreFile == null) {
            Logger.error("High Score file could not be opened: game variant not set?");
            return;
        }
        var oldHighScore = Score.fromFile(highScoreFile);
        if (highScore.points() > oldHighScore.points()) {
            try {
                highScore.save(highScoreFile, "High Score updated at %s".formatted(LocalTime.now()));
            } catch (IOException x) {
                Logger.error("High Score file could not be saved: '{}'", highScoreFile);
            }
        }
    }

    @Override
    public void resetScore() {
        score.reset();
    }

    @Override
    public void saveHighScore() {
        if (highScoreFile == null) {
            Logger.error("High Score file could not be opened: game variant not set?");
            return;
        }
        try {
            new Score().save(highScoreFile, "High Score, %s".formatted(LocalDateTime.now()));
        } catch (IOException x) {
            Logger.error("High Score could not be saved to file '{}'", highScoreFile);
            Logger.error(x);
        }
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
