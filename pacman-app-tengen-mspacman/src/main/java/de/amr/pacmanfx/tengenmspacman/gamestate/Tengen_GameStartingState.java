/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.event.gameplay.GameStartedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;

public class Tengen_GameStartingState extends AbstractGameState {

    static final short TICK_START_LEVEL = 10;
    static final short TICK_SHOW_GUYS = 70;
    static final short TICK_START_PLAYING = 250;

    private GameLevel level;
    private Pac pac;

    public Tengen_GameStartingState() {
        super(CommonGameStateID.GAME_STARTING);
    }

    @Override
    public void onEnterState(GameContext game) {
        level = gamePlay.buildNormalLevel(game, TengenMsPacMan_GamePlay.startLevelNumber(session));
        pac = level.entities().pac();

        game.eventManager().publishGameEvent(new LevelCreatedEvent(level));

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

            flow.enterGameState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
