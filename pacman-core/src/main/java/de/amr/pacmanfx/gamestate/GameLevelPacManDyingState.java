/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.PacDeadEvent;
import de.amr.pacmanfx.event.PacDyingEvent;
import de.amr.pacmanfx.event.StopAllSoundsEvent;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import org.tinylog.Logger;

public class GameLevelPacManDyingState extends GameState {

    public static class Timing {
        public static final short TICK_PACMAN_DYING_HIDE_GHOSTS = 60;
        public static final short TICK_PACMAN_DYING_START_ANIMATION = 90;
        public static final short TICK_PACMAN_DYING_HIDE_PAC = 190;
        public static final short TICK_PACMAN_DYING_PAC_DEAD = 210;
    }

    public GameLevelPacManDyingState() {
        super(GameStateID.GAME_LEVEL_PACMAN_DYING);
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel game = context.gameModel();
        final GameLevel level = context.gameModel().optGameLevel().orElseThrow();

        lock(); // UI triggers time-out

        game.gateKeeper().resetCounterAndSetEnabled(true);

        level.huntingTimer().stop();

        final Pac pac = level.entities().pac();
        pac.animations().stopSelected();
        pac.powerTimer().stop();
        pac.powerTimer().reset(0);
        pac.setSpeed(0);
        pac.setDead(true);
        Logger.info("Pac power timer stopped and reset to zero.");

        level.entities().ghosts().forEach(ghost -> ghost.onPacKilled(level));

        context.gameFlow().publishGameEvent(new StopAllSoundsEvent(context));
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameModel gameModel = gameContext.gameModel();
        final GameLevel level = gameModel.optGameLevel().orElseThrow();
        final Pac pac = level.entities().pac();
        final long tick = timer().tickCount();

        if (timer().hasExpired()) {
            if (level.isDemoLevel()) {
                gameContext.gameFlow().enterState(GameStateID.GAME_OVER);
            } else {
                gameModel.lives().add(-1);
                gameContext.gameFlow().enterState(gameModel.lives().count() == 0
                    ? GameStateID.GAME_OVER
                    : GameStateID.GAME_OR_LEVEL_STARTING);
            }
        }
        else if (tick == Timing.TICK_PACMAN_DYING_HIDE_GHOSTS) {
            level.entities().ghosts().forEach(Ghost::hide);
            pac.animations().select(ArcadePacMan_AnimationID.PAC_DYING);
            pac.animations().resetSelected();
        }
        else if (tick == Timing.TICK_PACMAN_DYING_START_ANIMATION) {
            pac.animations().playSelected();
            gameContext.gameFlow().publishGameEvent(new PacDyingEvent(gameContext, pac));
        }
        else if (tick == Timing.TICK_PACMAN_DYING_HIDE_PAC) {
            pac.hide();
            level.optBonus().ifPresent(Bonus::setInactive); //TODO check this
        }
        else if (tick == Timing.TICK_PACMAN_DYING_PAC_DEAD) {
            gameContext.gameFlow().publishGameEvent(new PacDeadEvent(gameContext, pac));
        }
        else {
            level.heartbeat().triggerPulse();
            pac.update(gameContext, level);
        }
    }
}
