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

public class CommonPacManDyingState extends GameState {

    private final int hideGhostsTick;
    private final int animationStartTick;
    private final int hidePacTick;
    private final int pacDeadTick;

    public CommonPacManDyingState(
        int hideGhostsTick,
        int animationStartTick,
        int hidePacTick,
        int pacDeadTick)
    {
        super(GameStateID.GAME_LEVEL_PACMAN_DYING);
        this.hideGhostsTick = hideGhostsTick;
        this.animationStartTick = animationStartTick;
        this.hidePacTick = hidePacTick;
        this.pacDeadTick = pacDeadTick;
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel model = context.model();
        final GameLevel level = context.model().optLevel().orElseThrow();

        model.gateKeeper().resetCounterAndSetEnabled(true);

        level.huntingTimer().stop();

        level.entities().optBonus().ifPresent(Bonus::setInactive);

        final Pac pac = level.entities().pac();
        pac.animations().stopSelected();
        pac.powerTimer().stop();
        pac.powerTimer().reset(0);
        pac.setSpeed(0);
        pac.setDead(true);
        Logger.info("Pac power timer stopped and reset to zero.");

        level.entities().ghosts().forEach(ghost -> ghost.onPacKilled(level));

        context.eventManager().publishGameEvent(new StopAllSoundsEvent());
        waitForTimeout(); // UI triggers timeout
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameModel model = context.model();
        final GameLevel level = model.optLevel().orElseThrow();
        final Pac pac = level.entities().pac();
        final long tick = timer().tickCount();

        if (timer().hasExpired()) {
            if (level.isDemoLevel()) {
                context.flow().enterState(GameStateID.GAME_OVER);
            } else {
                model.lives().add(-1);
                context.flow().enterState(model.lives().count() == 0
                    ? GameStateID.GAME_OVER : GameStateID.GAME_OR_LEVEL_STARTING);
            }
        }
        else if (tick == hideGhostsTick) {
            level.entities().ghosts().forEach(Ghost::hide);
            pac.animations().select(ArcadePacMan_AnimationID.PAC_DYING);
            pac.animations().resetSelected();
        }
        else if (tick == animationStartTick) {
            pac.animations().playSelected();
            context.eventManager().publishGameEvent(new PacDyingEvent(pac));
        }
        else if (tick == hidePacTick) {
            pac.hide();
            level.optBonus().ifPresent(Bonus::setInactive); //TODO check this
        }
        else if (tick == pacDeadTick) {
            context.eventManager().publishGameEvent(new PacDeadEvent(pac));
        }
        else {
            level.heartbeat().triggerPulse();
            pac.update(level, context.eventManager());
        }
    }
}
