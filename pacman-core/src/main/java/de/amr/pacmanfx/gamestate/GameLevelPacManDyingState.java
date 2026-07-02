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

    public record Timing(
        int hideGhostsTick,
        int animationStartTick,
        int hidePacTick,
        int pacDeadTick
    ) {}

    private final Timing timings;

    public GameLevelPacManDyingState(Timing timings) {
        super(GameStateID.GAME_LEVEL_PACMAN_DYING);
        this.timings = timings;
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel gameModel = context.model();
        final GameLevel level = context.model().optGameLevel().orElseThrow();

        block(); // UI triggers time-out

        gameModel.gateKeeper().resetCounterAndSetEnabled(true);

        level.huntingTimer().stop();

        final Pac pac = level.entities().pac();
        pac.animations().stopSelected();
        pac.powerTimer().stop();
        pac.powerTimer().reset(0);
        pac.setSpeed(0);
        pac.setDead(true);
        Logger.info("Pac power timer stopped and reset to zero.");

        level.entities().ghosts().forEach(ghost -> ghost.onPacKilled(level));

        context.flow().publishGameEvent(new StopAllSoundsEvent(context));
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        final GameLevel level = gameModel.optGameLevel().orElseThrow();
        final Pac pac = level.entities().pac();
        final long tick = timer().tickCount();

        if (timer().hasExpired()) {
            if (level.isDemoLevel()) {
                gameContext.flow().enterState(GameStateID.GAME_OVER);
            } else {
                gameModel.lives().add(-1);
                gameContext.flow().enterState(gameModel.lives().count() == 0
                    ? GameStateID.GAME_OVER : GameStateID.GAME_OR_LEVEL_STARTING);
            }
        }
        else if (tick == timings.hideGhostsTick()) {
            level.entities().ghosts().forEach(Ghost::hide);
            pac.animations().select(ArcadePacMan_AnimationID.PAC_DYING);
            pac.animations().resetSelected();
        }
        else if (tick == timings.animationStartTick()) {
            pac.animations().playSelected();
            gameContext.flow().publishGameEvent(new PacDyingEvent(gameContext, pac));
        }
        else if (tick == timings.hidePacTick()) {
            pac.hide();
            level.optBonus().ifPresent(Bonus::setInactive); //TODO check this
        }
        else if (tick == timings.pacDeadTick()) {
            gameContext.flow().publishGameEvent(new PacDeadEvent(gameContext, pac));
        }
        else {
            level.heartbeat().triggerPulse();
            pac.update(gameContext, level);
        }
    }
}
