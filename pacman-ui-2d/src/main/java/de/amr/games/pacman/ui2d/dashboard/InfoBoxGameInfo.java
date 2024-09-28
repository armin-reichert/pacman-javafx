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

        addTextRow("Game Scene", ifGameScene(gameScene -> gameScene.getClass().getSimpleName()));
        addTextRow("Game State", () -> "%s".formatted(context.gameState()));

        addTextRow("State Timer",
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

        addTextRow("Level Number",ifLevel(level -> "%d".formatted(context.game().levelNumber())));

        addTextRow("World Map",ifWorld(world -> {
            String url = world.map().url().toString();
            return url.substring(url.lastIndexOf("/") + 1);
        }));

        addTextRow("Lives",           ifLevel(level -> "%d".formatted(context.game().lives())));

        addTextRow("Hunting Phase",   ifLevel(this::fmtHuntingPhase));
        addTextRow("",                ifLevel(this::fmtHuntingTicksRunning));
        addTextRow("",                ifLevel(this::fmtHuntingTicksRemaining));

        addTextRow("Pac-Man speed",   ifLevel(this::fmtPacSpeed));
        addTextRow("- empowered",     ifLevel(this::fmtPacSpeedPowered));
        addTextRow("Pellets",         ifWorld(this::fmtPelletCount));
        addTextRow("Ghost speed",     ifLevel(this::fmtGhostSpeed));
        addTextRow("- frightened",    ifLevel(this::fmtGhostSpeedFrightened));
        addTextRow("- in tunnel",     ifLevel(this::fmtGhostSpeedTunnel));
        addTextRow("Frightened time", ifLevel(this::fmtPacPowerSeconds));
        addTextRow("Maze flashings",  ifLevel(this::fmtNumFlashes));
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