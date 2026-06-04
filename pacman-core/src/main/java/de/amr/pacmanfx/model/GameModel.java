/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.flow.GameControlFlow;
import de.amr.pacmanfx.model.actors.ActorSpeedControl;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import de.amr.pacmanfx.model.level.LevelCounter;
import de.amr.pacmanfx.model.lives.PacManLives;
import de.amr.pacmanfx.model.world.GateKeeper;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import de.amr.pacmanfx.score.PersistentScore;
import de.amr.pacmanfx.score.Score;

import java.util.Optional;

/**
 * Common interface for all Pac‑Man game models.
 */
public interface GameModel extends GameCheats {

    // Components

    GameControlFlow flow();

    GameRules rules();

    CoinMechanism coinMechanism();

    PacManLives lives();

    ActorSpeedControl actorSpeedControl();

    GateKeeper gateKeeper();

    HeadsUpDisplay hud();

    LevelCounter levelCounter();

    Score score();

    PersistentScore highScore();

    WorldMapSelector mapSelector();

    // Lifecycle

    SimulationStep simulationStep();

    void init();

    void prepareNewGame();

    boolean canStartNewGame();

    boolean canContinueOnGameOver();

    void onGameOver(GameLevel level);

    boolean isPlaying();

    void setPlaying(boolean playing);

    void activateNextBonus(GameLevel level);

    // Level related

    GameLevel createLevel(int levelNumber, boolean demoLevel);

    void buildNormalLevel(int levelNumber);

    void buildDemoLevel();

    Optional<GameLevel> optGameLevel();

    void startLevel();

    void makeReadyForPlaying(GameLevel level);

    void showLevelMessage(GameLevel level, GameLevelMessageType type);

    void startDemoLevel(long tick);

    boolean isDemoLevelRunning();

    void startNextLevel();

    void onStartLevelPlaying(GameLevel level);

    void doLevelPlaying(GameLevel level);

    void onLevelCompleted(GameLevel level);

    // Actor related

    CollisionStrategy DEFAULT_COLLISION_STRATEGY = CollisionStrategy.SAME_TILE;

    CollisionStrategy collisionStrategy();

    void setCollisionStrategy(CollisionStrategy collisionStrategy);

    void eatPellet(GameLevel level, Vector2i tile);

    boolean hasPacManBeenKilled();

    void doPacManDying(GameLevel level, Pac pac, long tick);

    boolean hasGhostBeenKilled();

    void onEatGhost(GameLevel level, Ghost eatenGhost);
}
