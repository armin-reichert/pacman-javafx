/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model;

import de.amr.pacmanfx.core.model.entities.levelCounter.LevelCounter;
import de.amr.pacmanfx.core.model.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.rules.GameRules;
import de.amr.pacmanfx.core.model.score.PropertyFileScore;
import de.amr.pacmanfx.core.model.score.Score;
import de.amr.pacmanfx.core.model.world.house.ArcadeHouseGateKeeper;
import de.amr.pacmanfx.core.model.world.map.WorldMapSelector;
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

    //TODO do not store entity in model!
    protected final LevelCounter levelCounter;

    protected WorldMapSelector mapSelector;

    protected final ArcadeHouseGateKeeper gateKeeper;

    protected final IntegerProperty initialLifeCount = new SimpleIntegerProperty();

    // Constructor

    protected GameModel() {
        score = new Score();
        levelCounter = new LevelCounter();
        gateKeeper = new ArcadeHouseGateKeeper();

        LevelCounterSystem.enable(levelCounter, true);
        LevelCounterSystem.setCapacity(levelCounter, 7);
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
