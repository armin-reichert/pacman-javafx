/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.event.gameplay.GameStartedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameFlowController;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.GameSession;

public class Arcade_GameStartingState extends GameState {

    static final int TICK_START_LEVEL = 2;
    static final int TICK_SHOW_GUYS = 60;
    static final int TICK_START_PLAYING = 240;

    private GameFlowController gameFlow;
    private GamePlay gamePlay;
    private GameSystems systems;
    private GameSession session;
    private GameLevel level;
    private Pac pac;

    public Arcade_GameStartingState() {
        super(CommonGameStateID.GAME_STARTING);
    }

    @Override
    public void onEnter(GameContext game) {
        gameFlow = game.variant().gameFlow();
        gamePlay = game.variant().gamePlay();
        systems = game.variant().systems();
        session = game.session();

        // Build new level
        level = game.variant().gamePlay().buildNormalLevel(game, 1);
        pac = level.entities().pac();

        game.eventManager().publishGameEvent(new LevelCreatedEvent(level));

        final HUD hud = session.hud();
        hud.hideCredit();
        hud.livesCounter().show();
        hud.levelCounter().show();
        hud.gameScore().show();
        hud.highScore().show();
        hud.show();

        ScoreSystem.enableScore(hud.highScore(), true);

        game.eventManager().publishGameEvent(new GameStartedEvent(game));
    }

    @Override
    public void onUpdate(GameContext game) {
        final long tick = timer().tickCount();

        if (tick < TICK_START_PLAYING) {
            systems.worldNavigator().setDisabled(pac, true);
            systems.pacAnimation().setDisabled(pac, true);
            for (Ghost ghost : level.entities().ghosts()) {
                systems.worldNavigator().setDisabled(ghost, true);
                systems.ghostAnimation().setDisabled(ghost, true);
            }
        }

        if (tick == TICK_START_LEVEL) {
            gamePlay.startLevel(game, level);
        }
        else if (tick == TICK_SHOW_GUYS) {
            level.entities().pac().show();
            level.entities().ghosts().forEach(GameEntity::show);
        }
        else if (tick == TICK_START_PLAYING) {

            // Now, actors start moving and animating
            systems.worldNavigator().setDisabled(pac, false);
            systems.pacAnimation().setDisabled(pac, false);
            for (Ghost ghost : level.entities().ghosts()) {
                systems.worldNavigator().setDisabled(ghost, false);
                systems.ghostAnimation().setDisabled(ghost, false);
            }

            game.coinMechanism().consumeCoin();
            session.setGameRunning(true);

            gameFlow.enterGameState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
