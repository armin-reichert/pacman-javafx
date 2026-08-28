/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.event.gameplay.GameStartedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;

public class Tengen_GameStartingState extends AbstractGameState {

    //TODO check again in Mesen and adapt these values
    static final short TICK_SHOW_READY = 10;
    static final short TICK_SHOW_GUYS = 70;
    static final short TICK_START_PLAYING = 250;

    public Tengen_GameStartingState() {
        super(CommonGameStateID.GAME_STARTING);
    }

    @Override
    public void onEnterState(GameContext game) {
        final var tengenGamePlay = (TengenMsPacMan_GamePlay) gamePlay;

        final GameLevel level = tengenGamePlay.buildNormalLevel(game, tengenGamePlay.startLevelNumber(session));
        game.eventManager().publishGameEvent(new LevelCreatedEvent(level));

        showActors(level, false);

        ScoreSystem.enableScore(hud.highScore(), true);
        game.eventManager().publishGameEvent(new GameStartedEvent(game));
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameLevel level = session.level();
        final long tick = timer().tickCount();

        if (tick < TICK_SHOW_GUYS) {
            disableActors(level, true);
        }

        if (tick == TICK_SHOW_READY) {
            gamePlay.startLevel(game, level);
        }
        else if (tick == TICK_SHOW_GUYS) {
            showActors(level, true);
            disableActors(level, false);
        }
        else if (tick == TICK_START_PLAYING) {
            session.setGameRunning(true);
            flow.enterGameState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }

    private void showActors(GameLevel level, boolean visible) {
        level.entities().pac().visibility().set(visible);
        for (Ghost ghost : level.entities().ghosts()) {
            ghost.visibility().set(visible);
        }
    }

    private void disableActors(GameLevel level, boolean disabled) {
        systems.worldNavigator().setDisabled(level.entities().pac(), disabled);
        for (Ghost ghost : level.entities().ghosts()) {
            systems.worldNavigator().setDisabled(ghost, disabled);
        }
    }
}
