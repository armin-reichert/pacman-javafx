/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.event.gameplay.GameStartedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.GameSession;

public class Arcade_GameStartingState extends GameState {

    static final int TICK_NEW_GAME_START_LEVEL = 2;
    static final int TICK_NEW_GAME_SHOW_GUYS = 60;
    static final int TICK_NEW_GAME_START_PLAYING = 240;

    public Arcade_GameStartingState() {
        super(CommonGameStateID.GAME_STARTING);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameLevel newLevel = game.variant().gamePlay().buildNormalLevel(game, 1);
        game.eventManager().publishGameEvent(new LevelCreatedEvent(newLevel));

        game.session().hud().hideCredit().showLivesCounter();
        ScoreSystem.enableScore(game.session().highScore(), true);

        game.eventManager().publishGameEvent(new GameStartedEvent(game));
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSystems systems = game.variant().systems();
        final GameSession session = game.session();
        final GameLevel level = session.level();
        final long tick = timer().tickCount();

        systems.entityUpdater().updateSessionHUDEntities(game);

        if (tick == TICK_NEW_GAME_START_LEVEL) {
            game.variant().gamePlay().startLevel(game, level);
        }
        else if (tick == TICK_NEW_GAME_SHOW_GUYS) {
            level.entities().pac().show();
            level.entities().ghosts().forEach(GameEntity::show);
        }
        else if (tick == TICK_NEW_GAME_START_PLAYING) {
            session.setGameRunning(true);
            game.variant().gameFlow().enterGameState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }

    @Override
    public void onExit(GameContext game) {
        game.coinMechanism().consumeCoin();
    }
}
