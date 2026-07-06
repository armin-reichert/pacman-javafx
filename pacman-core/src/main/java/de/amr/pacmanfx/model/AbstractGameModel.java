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
import de.amr.pacmanfx.score.PersistentScore;
import de.amr.pacmanfx.score.Score;
import de.amr.pacmanfx.simulation.HuntingStepResult;
import de.amr.pacmanfx.steering.Steering;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.File;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Base implementation of the {@link GameModel} interface.
 */
public abstract class AbstractGameModel implements GameModel {

    private final BooleanProperty playing = new SimpleBooleanProperty(false);

    protected final Score score;

    protected GateKeeper gateKeeper;

    protected HUDState hud;

    protected PersistentScore highScore;

    protected GameLevel currentLevel;

    protected PacManLives lives;

    protected LevelCounter levelCounter;

    protected WorldMapSelector mapSelector;

    protected Steering automaticSteering;

    protected GameRules rules;

    protected HuntingStepResult huntingStepResult;

    // Constructor

    protected AbstractGameModel() {
        score = new Score();
        lives = new PacManLivesImpl();
        hud = new HUDState();
    }

    public void setRules(GameRules rules) {
        this.rules = rules;
    }

    public void setMapSelector(WorldMapSelector mapSelector) {
        this.mapSelector =  requireNonNull(mapSelector);
    }

    @Override
    public HuntingStepResult huntingStepResult() {
        return huntingStepResult;
    }

    @Override
    public void setHuntingStepResult(HuntingStepResult huntingStepResult) {
        this.huntingStepResult = huntingStepResult;
    }

    /* -------------------------------------------------------------------------
     * GameModel interface implementation
     * ---------------------------------------------------------------------- */

    @Override
    public PacManLives lives() {
        return lives;
    }

    @Override
    public GateKeeper gateKeeper() {
        return gateKeeper;
    }

    @Override
    public HUDState hudState() {
        return hud;
    }

    @Override
    public LevelCounter levelCounter() {
        return levelCounter;
    }

    @Override
    public Score score() {
        return score;
    }

    @Override
    public PersistentScore highScore() {
        return highScore;
    }

    @Override
    public WorldMapSelector mapSelector() {
        return mapSelector;
    }

    @Override
    public GameRules rules() {
        return rules;
    }

    // Lifecycle

    @Override
    public boolean isPlaying() {
        return playing.get();
    }

    @Override
    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    // Level related

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

    public void setHighScoreFile(File file) {
        highScore = new PersistentScore(file);
    }
}
