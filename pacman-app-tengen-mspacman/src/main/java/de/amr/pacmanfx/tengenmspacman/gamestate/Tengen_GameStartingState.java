/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.event.gameplay.GameStartedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.tengenmspacman.entities.pac.comp.PacBoosterComp;
import de.amr.pacmanfx.tengenmspacman.model.BoosterMode;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.gameOptions;

public class Tengen_GameStartingState extends AbstractGameState {

    static final short TICK_START_LEVEL = 10;
    static final short TICK_SHOW_GUYS = 70;
    static final short TICK_START_PLAYING = 250;

    private GameLevel level;

    public Tengen_GameStartingState() {
        super(CommonGameStateID.GAME_STARTING);
    }

    @Override
    public void onEnterState(GameContext game) {
        final GameSession session = game.session();
        final int startLevelNumber = gameOptions(session).startLevelNumber();
        level = gamePlay.buildNormalLevel(game, startLevelNumber);

        final boolean boosterInitiallyEnabled = gameOptions(session).boosterMode() == BoosterMode.BOOSTER_ALWAYS_ON;
        gameOptions(session).setBoosterEnabled(boosterInitiallyEnabled);

        //TODO Hack. Should be done by entity update system
        final Pac pac = level.entities().pac();
        pac.state().setEnumValue(PacState.SLEEPING);
        pac.reqComp(PacBoosterComp.class).setBoosterEnabled(boosterInitiallyEnabled);

        hud.creditDisplay().hide();
        hud.livesCounter().show();
        hud.levelCounter().show();
        hud.gameScore().show();
        hud.highScore().show();
        hud.show();

        hud.highScore().data().setEnabled(true);

        game.eventManager().publishGameEvent(new LevelCreatedEvent(level));
        game.eventManager().publishGameEvent(new GameStartedEvent(game));
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        final Pac pac = level.entities().pac();

        if (stateTick == 0) {
            game.variant().systems().pacAnimation().update(pac, game.variant().rules());
            lockGhosts(level.entities(), true);
        }

        if (stateTick == TICK_START_LEVEL) {
            gamePlay.startLevel(game, level);
        }
        else if (stateTick == TICK_SHOW_GUYS) {
            showPacAndGhosts(level.entities());
        }
        else if (stateTick == TICK_START_PLAYING) {
            lockPacAndGhosts(level.entities(), false);
            game.coinMechanism().consumeCoin();
            session.setGameRunning(true);
            flow.enterGameState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
