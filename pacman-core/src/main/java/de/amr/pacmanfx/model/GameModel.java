/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.actors.ActorSpeedControl;
import de.amr.pacmanfx.model.actors.Bonus;
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
public interface GameModel {

    // Components

    //TODO move into game context
    GameCheats cheats();

    PacManLives lives();

    GateKeeper gateKeeper();

    ActorSpeedControl actorSpeedControl();

    HUDState hud();

    LevelCounter levelCounter();

    Score score();

    PersistentScore highScore();

    WorldMapSelector mapSelector();

    // Lifecycle

    void init();

    void prepareNewGame();

    boolean canStartNewGame(GameContext gameContext);

    boolean canContinueOnGameOver();

    void onGameOver(GameContext gameContext, GameLevel level);

    boolean isPlaying();

    void setPlaying(boolean playing);

    // Level related

    GameLevel createLevel(GameContext gameContext, int levelNumber, boolean demoLevel);

    void buildNormalLevel(GameContext gameContext, int levelNumber);

    void buildDemoLevel(GameContext gameContext);

    void setLevel(GameLevel level);

    Optional<GameLevel> optGameLevel();

    void startLevel(GameContext gameContext);

    void startNextLevel(GameContext gameContext);

    void prepareLevelForPlaying(GameLevel level);

    void showLevelMessage(GameLevel level, GameLevelMessageType type);

    void startDemoLevel(GameContext gameContext, long tick);

    boolean isDemoLevelRunning();

    void onStartLevelPlaying(GameContext gameContext, GameLevel level);

    void onLevelCompleted(GameLevel level);

    // Actor related

    void eatPellet(GameContext gameContext, GameLevel level, Vector2i tile);

    void eatEnergizer(GameContext gameContext, GameLevel level, Vector2i tile);

    void activateNextBonus(GameContext gameContext, GameLevel level);

    void eatBonus(GameContext gameContext, GameLevel level, Bonus bonus);

    void onEatGhost(GameContext gameContext, GameLevel level, Ghost eatenGhost);

    void startPacPowerMode(GameContext gameContext, GameLevel level, Pac pac);

    void updatePacPowerMode(GameContext gameContext, GameLevel level, Pac pac);

    boolean isPacSafeInDemoLevel(GameLevel demoLevel);
}
