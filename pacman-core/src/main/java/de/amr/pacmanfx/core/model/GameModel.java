/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model;

import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.LevelCounter;
import de.amr.pacmanfx.core.model.world.ArcadeHouseGateKeeper;
import de.amr.pacmanfx.core.model.world.WorldMapSelector;
import de.amr.pacmanfx.core.rules.GameRules;
import de.amr.pacmanfx.core.score.PropertyFileScore;
import de.amr.pacmanfx.core.score.Score;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Base class of all Pac-Man game model classes.
 */
public abstract class GameModel {

    // Data

    private final BooleanProperty playing = new SimpleBooleanProperty(false);

    protected final Score score;

    protected PropertyFileScore highScore;

    protected GameLevel level;

    protected LevelCounter levelCounter;

    protected WorldMapSelector mapSelector;

    protected final ArcadeHouseGateKeeper gateKeeper;

    protected final IntegerProperty initialLifeCount = new SimpleIntegerProperty();

    protected final IntegerProperty lifeCount = new SimpleIntegerProperty();

    // Constructor

    protected GameModel() {
        score = new Score();
        gateKeeper = new ArcadeHouseGateKeeper();
    }

    /* -------------------------------------------------------------------------
     * API
     * ---------------------------------------------------------------------- */

    public abstract void init();

    public int initialLifeCount() {
        return initialLifeCount.get();
    }

    public void setInitialLifeCount(int count) {
        initialLifeCount.set(count);
    }

    public void setLifeCount(int numLives) {
        lifeCountProperty().set(numLives);
    }

    public int lifeCount() {
        return lifeCountProperty().get();
    }

    public void addLives(int n) {
        lifeCountProperty().set(lifeCount() + n);
    }

    public IntegerProperty lifeCountProperty() {
        return lifeCount;
    }

    public ArcadeHouseGateKeeper gateKeeper() {
        return gateKeeper;
    }

    public PropertyFileScore highScore() {
        return highScore;
    }

    public LevelCounter levelCounter() {
        return levelCounter;
    }

    public Score score() {
        return score;
    }

    public void setHighScore(PropertyFileScore score) {
        highScore = requireNonNull(score);
    }

    public WorldMapSelector mapSelector() {
        return mapSelector;
    }

    public abstract GameRules rules();

    public boolean isPlaying() {
        return playing.get();
    }

    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    public void setLevel(GameLevel level) {
        this.level = level;
    }

    public Optional<GameLevel> optLevel() {
        return Optional.ofNullable(level);
    }

    public GameLevel assertLevel() {
        return optLevel().orElseThrow();
    }
}
