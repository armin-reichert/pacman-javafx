/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
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

        level.huntingTimerStrategy().stop();
        level.entities().ghosts().forEach(ghost -> ghost.state().setPacPower(true));
        systems.pacPower().start(e.pac(), durationTicks);

        Logger.info("Pac power started. Power ticks: {}", durationTicks);
    }

    @Override
    public void onPacPowerStartsFading(PacPowerStartsFadingEvent e) {
        final GameLevel level = game.session().level();
        level.entities().ghosts().forEach(ghost -> ghost.state().setPacPowerFading(true));

        Logger.info("Pac power started fading. Power ticks remaining: {}", e.pac().power().ticksRemaining());
    }

    @Override
    public void onPacPowerEnds(PacPowerEndsEvent e) {
        final GameLevel level = game.session().level();

        level.clearGhostKillChain();
        level.entities().ghosts().forEach(ghost -> {
            ghost.state().setPacPower(false);
            ghost.state().setPacPowerFading(false);
        });
        level.huntingTimerStrategy().start();

        Logger.info("Pac power ended, hunting resumed.");
    }
}
