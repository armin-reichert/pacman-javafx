/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model;

import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.LevelCounter;
import de.amr.pacmanfx.model.lives.PacManLives;
import de.amr.pacmanfx.model.lives.PacManLivesImpl;
import de.amr.pacmanfx.model.world.GateKeeper;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import de.amr.pacmanfx.score.PropertyFileScore;
import de.amr.pacmanfx.score.Score;
import de.amr.pacmanfx.simulation.HuntingStepResult;
import de.amr.pacmanfx.steering.Steering;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Base implementation of the {@link GameModel} interface.
 */
public abstract class AbstractGameModel implements GameModel {

    private final BooleanProperty playing = new SimpleBooleanProperty(false);

    protected final Score score;

    protected PropertyFileScore highScore;

    protected GameLevel currentLevel;

    protected final PacManLives lives;

    protected Steering automaticSteering;

    protected HuntingStepResult huntingStepResult;

    // Constructor

    protected AbstractGameModel() {
        score = new Score();
        lives = new PacManLivesImpl();
    }

    public void setHighScore(PropertyFileScore score) {
        highScore = requireNonNull(score);
    }

    public abstract void setMapSelector(WorldMapSelector mapSelector);

    /* -------------------------------------------------------------------------
     * GameModel interface implementation
     * ---------------------------------------------------------------------- */

    @Override
    public PacManLives lives() {
        return lives;
    }

    @Override
    public abstract GateKeeper gateKeeper();

    @Override
    public abstract HUDState hudState();

    @Override
    public PropertyFileScore highScore() {
        return highScore;
    }

    @Override
    public HuntingStepResult huntingStepResult() {
        return huntingStepResult;
    }

    @Override
    public void setHuntingStepResult(HuntingStepResult huntingStepResult) {
        this.huntingStepResult = huntingStepResult;
    }

    @Override
    public abstract LevelCounter levelCounter();

    @Override
    public Score score() {
        return score;
    }

    @Override
    public abstract WorldMapSelector mapSelector();

    @Override
    public abstract GameRules rules();

    @Override
    public boolean isPlaying() {
        return playing.get();
    }

    @Override
    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    @Override
    public void setLevel(GameLevel level) {
        currentLevel = level;
    }

    @Override
    public Optional<GameLevel> optGameLevel() {
        return Optional.ofNullable(currentLevel);
    }

    @Override
    public GameLevel assertLevel() {
        return optGameLevel().orElseThrow();
    }
}
