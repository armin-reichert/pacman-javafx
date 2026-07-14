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
 * Base class of all Pac-Man game model classes.
 */
public abstract class GameModel {

    /**
     * The red ghost's character is aptly described as that of a shadow and is best-known as “Blinky”.
     * In Japan, his character is represented by the word oikake, which means “to run down or pursue”.
     * Blinky seems to always be the first of the ghosts to track Pac-Man down in the maze.
     * He is by far the most aggressive of the four and will doggedly pursue Pac-Man once behind him.
     */
    public static final byte RED_GHOST_SHADOW = 0;

    /**
     * Nicknamed “Pinky”, the pink ghost's character is described as one who is speedy.
     * In Japan, he is characterized as machibuse, meaning “to perform an ambush”, perhaps because Pinky always seems
     * to be able to get ahead of you and cut you off when you least expect it. He always moves at the same speed
     * as Inky and Clyde, however, which suggests speedy is a poor translation of the more appropriate machibuse.
     * Pinky and Blinky often seem to be working in concert to box Pac-Man in, leaving him with nowhere to run.
     */
    public static final byte PINK_GHOST_SPEEDY = 1;

    /**
     * The light-blue ghost is nicknamed “Inky” and his character is described as one who is bashful.
     * In Japan, he is portrayed as kimagure, meaning “a fickle, moody, or uneven temper”.
     * Perhaps not surprisingly, Inky is the least predictable of the ghosts. Sometimes he chases Pac-Man aggressively
     * like Blinky; other times he jumps ahead of Pac-Man as Pinky would. He might even wander off like Clyde on occasion!
     * In fact, Inky may be the most dangerous ghost of all due to his erratic behavior.
     * Bashful is not a very good translation of kimagure, and misleads the player to assume Inky will shy away
     * from Pac-Man when he gets close which is not always the case.
     */
    public static final byte CYAN_GHOST_BASHFUL = 2;

    /**
     * The orange ghost is nicknamed “Clyde” and is characterized as one who is pokey.
     * In Japan, his character is described as otoboke, meaning “pretending ignorance”, and his nickname is “Guzuta”,
     * meaning “one who lags behind”. In reality, Clyde moves at the same speed as Inky and Pinky so his character
     * description is a bit misleading. Clyde is the last ghost to leave the pen and tends to separate himself
     * from the other ghosts by shying away from Pac-Man and doing his own thing when he isn't patrolling his corner
     * of the maze. Although not nearly as dangerous as the other three ghosts, his behavior can seem unpredictable
     * at times and should still be considered a threat.
     */
    public static final byte ORANGE_GHOST_POKEY = 3;

    // Data

    private final BooleanProperty playing = new SimpleBooleanProperty(false);

    protected final Score score;

    protected PropertyFileScore highScore;

    protected GameLevel level;

    protected final PacManLives lives;

    protected Steering automaticSteering;

    protected HuntingStepResult huntingStepResult;

    // Constructor

    protected GameModel() {
        score = new Score();
        lives = new PacManLivesImpl();
    }

    /* -------------------------------------------------------------------------
     * API
     * ---------------------------------------------------------------------- */

    public Steering automaticSteering() {
        return automaticSteering;
    }

    public abstract void init();

    public PacManLives lives() {
        return lives;
    }

    public abstract GateKeeper gateKeeper();

    public abstract HUDState hudState();

    public PropertyFileScore highScore() {
        return highScore;
    }

    public HuntingStepResult huntingStepResult() {
        return huntingStepResult;
    }

    public void setHuntingStepResult(HuntingStepResult huntingStepResult) {
        this.huntingStepResult = huntingStepResult;
    }

    public void clearHuntingStepResult() {
        setHuntingStepResult(null);
    }

    public abstract LevelCounter levelCounter();

    public Score score() {
        return score;
    }

    public void setHighScore(PropertyFileScore score) {
        highScore = requireNonNull(score);
    }

    public abstract WorldMapSelector mapSelector();

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
