/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.event.gameplay.GameStartedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelStartedEvent;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.session.GameSession;

public class ArcadeGameState_GameStarting extends GameState {

    static final int TICK_NEW_GAME_START_LEVEL = 2;
    static final int TICK_NEW_GAME_SHOW_GUYS = 60;
    static final int TICK_NEW_GAME_START_PLAYING = 240;

    public ArcadeGameState_GameStarting() {
        super(CommonGameStateID.GAME_STARTING);
    }

    @Override
    public void onEnter(GameContext game) {
        game.gamePlay().buildNormalLevel(game, 1, game.initialLifeCount());

        game.session().hud().hideCredit().showLivesCounter();
        ScoreSystem.enableScore(game.session().highScore(), true);

        game.eventManager().publishGameEvent(new GameStartedEvent(game));
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();
        final long tick = timer().tickCount();

        if (tick == TICK_NEW_GAME_START_LEVEL) {
            game.gamePlay().startLevel(game);
            // Note: This event is very important because it triggers the creation of the actor animations!
            game.eventManager().publishGameEvent(new LevelStartedEvent(level));
        }
        else if (tick == TICK_NEW_GAME_SHOW_GUYS) {
            level.entities().pac().show();
            level.entities().ghosts().forEach(GameEntity::show);
        }
        else if (tick == TICK_NEW_GAME_START_PLAYING) {
            session.setPlaying(true);
            session.gameFlow().enterState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }

    @Override
    public void onExit(GameContext game) {
        game.coinMechanism().consumeCoin();
    }
}
