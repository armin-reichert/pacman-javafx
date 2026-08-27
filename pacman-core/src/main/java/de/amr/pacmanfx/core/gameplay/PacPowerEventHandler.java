/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.event.base.DefaultGameEventListener;
import de.amr.pacmanfx.core.event.pac.PacPowerEndsEvent;
import de.amr.pacmanfx.core.event.pac.PacPowerStartsEvent;
import de.amr.pacmanfx.core.event.pac.PacPowerStartsFadingEvent;
import de.amr.pacmanfx.core.level.GameLevel;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class PacPowerEventHandler implements DefaultGameEventListener {

    private final GameContext game;

    public PacPowerEventHandler(GameContext game) {
        this.game = requireNonNull(game);
    }

    @Override
    public void onPacPowerStarts(PacPowerStartsEvent e) {
        final GameSystems systems = game.variant().systems();
        final long durationTicks = e.powerDurationTicks();
        final GameLevel level = game.session().level();

        Logger.info("Pac power started. Power ticks: {}", durationTicks);

        level.huntingTimerStrategy().stop();

        level.entities()
            .ghostsInState(GhostState.HUNTING_PAC)
            .forEach(ghost -> systems.ghostState().setVulnerable(ghost));

        systems.pacPower().start(e.pac(), durationTicks);
    }

    @Override
    public void onPacPowerEnds(PacPowerEndsEvent e) {
        final GameSystems systems = game.variant().systems();
        final GameLevel level = game.session().level();

        level.clearGhostKillChain();
        level.entities().ghosts().forEach(ghost -> systems.ghostState().setOutOfDanger(ghost));
        level.huntingTimerStrategy().start();

        Logger.info("Pac power ended, hunting resumed.");
    }

    @Override
    public void onPacPowerStartsFading(PacPowerStartsFadingEvent e) {
        Logger.info("Pac power started fading. Power ticks remaining: {}", e.pac().power().ticksRemaining());
    }
}
