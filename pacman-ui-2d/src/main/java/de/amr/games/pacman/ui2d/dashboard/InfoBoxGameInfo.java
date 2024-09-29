/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.ui2d.GameContext;

import static de.amr.games.pacman.lib.timer.TickTimer.ticksToString;

/**
 * @author Armin Reichert
 */
public class InfoBoxGameInfo extends InfoBox {

    public void init(GameContext context) {
        super.init(context);

        labelAndComputedValue("Game Scene", ifGameScene(gameScene -> gameScene.getClass().getSimpleName()));
        labelAndComputedValue("Game State", () -> "%s".formatted(context.gameState()));

        labelAndComputedValue("State Timer",
            () -> {
                TickTimer timer = context.gameState().timer();
                boolean indefinite = timer.duration() == TickTimer.INDEFINITE;
                if (timer.isStopped()) {
                    return "Stopped at tick %s of %s".formatted(
                        timer.currentTick(), indefinite ? "∞" : timer.duration());
                }
                if (indefinite) {
                    return "Tick %s of ∞".formatted(timer.currentTick());
                }
                return "Tick %s (%s remaining) of %s ticks".formatted(
                    timer.currentTick(), timer.remaining(), timer.duration());
            });

        labelAndComputedValue("Level Number",ifLevel(level -> "%d".formatted(context.game().levelNumber())));

        labelAndComputedValue("World Map",ifWorld(world -> {
            String url = world.map().url().toString();
            return url.substring(url.lastIndexOf("/") + 1);
        }));

        labelAndComputedValue("Lives",           ifLevel(level -> "%d".formatted(context.game().lives())));

        labelAndComputedValue("Hunting Phase",   ifLevel(this::fmtHuntingPhase));
        labelAndComputedValue("",                ifLevel(this::fmtHuntingTicksRunning));
        labelAndComputedValue("",                ifLevel(this::fmtHuntingTicksRemaining));

        labelAndComputedValue("Pac-Man speed",   ifLevel(this::fmtPacSpeed));
        labelAndComputedValue("- empowered",     ifLevel(this::fmtPacSpeedPowered));
        labelAndComputedValue("Pellets",         ifWorld(this::fmtPelletCount));
        labelAndComputedValue("Ghost speed",     ifLevel(this::fmtGhostSpeed));
        labelAndComputedValue("- frightened",    ifLevel(this::fmtGhostSpeedFrightened));
        labelAndComputedValue("- in tunnel",     ifLevel(this::fmtGhostSpeedTunnel));
        labelAndComputedValue("Frightened time", ifLevel(this::fmtPacPowerSeconds));
        labelAndComputedValue("Maze flashings",  ifLevel(this::fmtNumFlashes));
    }

    private String fmtHuntingPhase(GameLevel level) {
        var game = context.game();
        var huntingTimer = game.huntingTimer();
        return "%s #%d%s".formatted(
            game.isChasingPhase(game.huntingPhaseIndex()) ? "Chasing" : "Scattering",
            game.scatterPhase().orElse(game.chasingPhase().orElse(42)),
            huntingTimer.isStopped() ? " STOPPED" : "");
    }

    private String fmtHuntingTicksRunning(GameLevel level) {
        return "Running:   %d".formatted(context.game().huntingTimer().currentTick());
    }

    private String fmtHuntingTicksRemaining(GameLevel level) {
        return "Remaining: %s".formatted(ticksToString(context.game().huntingTimer().remaining()));
    }

    private String fmtPelletCount(GameWorld world) {
        return "%d of %d (%d energizers)".formatted(world.uneatenFoodCount(), world.totalFoodCount(), world.energizerTiles().count());
    }

    private String fmtGhostSpeed(GameLevel level) {
        return fmtSpeed(level.ghostSpeedPct());
    }

    private String fmtGhostSpeedFrightened(GameLevel level) {
        return fmtSpeed(level.ghostSpeedFrightenedPct());
    }

    private String fmtGhostSpeedTunnel(GameLevel level) {
        return fmtSpeed(level.ghostSpeedTunnelPct());
    }

    private String fmtPacSpeed(GameLevel level) {
        return fmtSpeed(level.pacSpeedPercentage());
    }

    private String fmtPacSpeedPowered(GameLevel level) {
        return fmtSpeed(level.pacSpeedPoweredPercentage());
    }

    private String fmtPacPowerSeconds(GameLevel level) {
        return "%d sec".formatted(level.pacPowerSeconds());
    }

    private String fmtNumFlashes(GameLevel level) {
        return "%d".formatted(level.numFlashes());
    }
}