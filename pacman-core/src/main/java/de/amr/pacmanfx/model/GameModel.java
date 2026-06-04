/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.flow.GameControlFlow;
import de.amr.pacmanfx.model.actors.*;
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
public interface GameModel {

    // Components

    GameControlFlow flow();

    GameRules rules();

    GameCheats cheats();

    CoinMechanism coinMechanism();

    PacManLives lives();

    GateKeeper gateKeeper();

    ActorSpeedControl actorSpeedControl();

    HeadsUpDisplay hud();

    LevelCounter levelCounter();

    Score score();

    PersistentScore highScore();

    WorldMapSelector mapSelector();

    // Lifecycle

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

    void setLevel(GameLevel level);

    Optional<GameLevel> optGameLevel();

    void startLevel();

    void prepareLevelForPlaying(GameLevel level);

    void showLevelMessage(GameLevel level, GameLevelMessageType type);

    void startDemoLevel(long tick);

    boolean isDemoLevelRunning();

    void startNextLevel();

    void onStartLevelPlaying(GameLevel level);

    void doLevelPlaying(GameLevel level);

    void onLevelCompleted(GameLevel level);

    // Actor related

    CollisionStrategy collisionStrategy();

    void setCollisionStrategy(CollisionStrategy collisionStrategy);

    Boolean isCollisionDoubleChecked();

    void eatPellet(GameLevel level, Vector2i tile);

    void eatEnergizer(GameLevel level, Vector2i tile);

    void eatBonus(GameLevel level, Bonus bonus);

    //TODO remove tick parameter, introduce new game state instead
    void doPacManDying(GameLevel level, Pac pac, long tick);

    void onEatGhost(GameLevel level, Ghost eatenGhost);

    boolean isPacSafeInDemoLevel(GameLevel demoLevel);
}
