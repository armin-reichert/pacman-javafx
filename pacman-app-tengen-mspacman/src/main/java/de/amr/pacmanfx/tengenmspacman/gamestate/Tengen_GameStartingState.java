/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.SessionKey;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.actor.pac.PacState;
import de.amr.pacmanfx.core.event.gameplay.GameStartedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.tengenmspacman.entities.pac.comp.PacBoosterComp;
import de.amr.pacmanfx.tengenmspacman.model.BoosterMode;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.gameOptionValues;

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
        final int startLevelNumber = gameOptionValues(game.session()).startLevelNumber();
        level = game.play().buildNormalLevel(game, startLevelNumber);

        final boolean boosterInitiallyEnabled = gameOptionValues(game.session()).boosterMode() == BoosterMode.BOOSTER_ALWAYS_ON;
        gameOptionValues(game.session()).setBoosterEnabled(boosterInitiallyEnabled);

        //TODO Hack. Should be done by entity update system
        final Pac pac = level.entitySet().pac();
        pac.state().setEnumValue(PacState.SLEEPING);
        pac.assertComponent(PacBoosterComp.class).setBoosterEnabled(boosterInitiallyEnabled);

        game.session().hud().creditDisplay().hide();
        game.session().hud().livesCounter().show();
        game.session().hud().levelCounter().show();
        game.session().hud().gameScore().show();
        game.session().hud().highScore().show();
        game.session().store(SessionKey.HUD_VISIBLE, true);

        game.session().hud().highScore().data().setEnabled(true);

        game.runtime().eventManager().publishEvent(new LevelCreatedEvent(game, level));
        game.runtime().eventManager().publishEvent(new GameStartedEvent(game));
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        final Pac pac = level.entitySet().pac();

        if (stateTick == 0) {
            game.runtime().systems().pacAnimation().update(pac, game.runtime().gameRules());
            lockGhosts(game, level.entitySet(), true);
        }

        if (stateTick == TICK_START_LEVEL) {
            game.play().startLevel(game, level);
        }
        else if (stateTick == TICK_SHOW_GUYS) {
            level.entitySet().showPacAndGhosts();
        }
        else if (stateTick == TICK_START_PLAYING) {
            lockPacAndGhosts(game, level.entitySet(), false);
            game.coinMechanism().consumeCoin();
            game.session().store(SessionKey.GAME_RUNNING, true);
            game.flow().enterGameState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
