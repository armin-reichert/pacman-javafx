/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model;

import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.LevelCounter;
import de.amr.pacmanfx.core.model.lives.PacManLives;
import de.amr.pacmanfx.core.model.lives.PacManLivesImpl;
import de.amr.pacmanfx.core.model.world.GateKeeper;
import de.amr.pacmanfx.core.model.world.WorldMapSelector;
import de.amr.pacmanfx.core.score.PropertyFileScore;
import de.amr.pacmanfx.core.score.Score;
import de.amr.pacmanfx.core.simulation.HuntingStepResult;
import de.amr.pacmanfx.core.steering.Steering;
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

    protected GameLevel level;

    protected final PacManLives lives;

    protected Steering automaticSteering;

    protected HuntingStepResult huntingStepResult;

    // Constructor

    protected AbstractGameModel() {
        score = new Score();
        lives = new PacManLivesImpl();
    }

    /* -------------------------------------------------------------------------
     * GameModel interface implementation
     * ---------------------------------------------------------------------- */

    @Override
    public Steering automaticSteering() {
        return automaticSteering;
    }

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
    public void setHighScore(PropertyFileScore score) {
        highScore = requireNonNull(score);
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
        this.level = level;
    }

    @Override
    public Optional<GameLevel> optLevel() {
        return Optional.ofNullable(level);
    }

    @Override
    public GameLevel assertLevel() {
        return optLevel().orElseThrow();
    }
}
