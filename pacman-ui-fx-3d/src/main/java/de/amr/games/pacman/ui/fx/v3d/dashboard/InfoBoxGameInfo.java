/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui.fx.util.Theme;

import static de.amr.games.pacman.lib.TickTimer.ticksToString;

/**
 * Game related settings.
 *
 * @author Armin Reichert
 */
public class InfoBoxGameInfo extends InfoBox {

    public InfoBoxGameInfo(Theme theme, String title) {
        super(theme, title);

        addInfo("Game Scene", () -> sceneContext.currentGameScene().isPresent()
            ? sceneContext.currentGameScene().get().getClass().getSimpleName() : "n/a");
        addInfo("Game State", () -> "%s".formatted(sceneContext.gameState()));
        addInfo("", () -> "Running:   %s%s".formatted(sceneContext.gameState().timer().tick(),
            sceneContext.gameState().timer().isStopped() ? " (STOPPED)" : ""));
        addInfo("", () -> "Remaining: %s".formatted(ticksToString(sceneContext.gameState().timer().remaining())));

        addInfo("Hunting Phase", ifLevelExists(this::fmtHuntingPhase));
        addInfo("", ifLevelExists(this::fmtHuntingTicksRunning));
        addInfo("", ifLevelExists(this::fmtHuntingTicksRemaining));

        addInfo("Pellets", ifLevelExists(this::fmtPelletCount));
        addInfo("Ghost speed", ifLevelExists(this::fmtGhostSpeed));
        addInfo("- frightened", ifLevelExists(this::fmtGhostSpeedFrightened));
        addInfo("- in tunnel", ifLevelExists(this::fmtGhostSpeedTunnel));
        addInfo("Pac-Man speed", ifLevelExists(this::fmtPacSpeed));
        addInfo("- empowered", ifLevelExists(this::fmtPacSpeedPowered));
        addInfo("Frightened time", ifLevelExists(this::fmtPacPowerSeconds));
        addInfo("Maze flashings", ifLevelExists(this::fmtNumFlashes));
    }

    private String fmtHuntingPhase(GameLevel level) {
        var huntingTimer = level.huntingTimer();
        return "%s #%d%s".formatted(level.currentHuntingPhaseName(),
            level.scatterPhase().isPresent() ? level.scatterPhase().get() : level.chasingPhase().orElse(42),
            huntingTimer.isStopped() ? " STOPPED" : "");
    }

    private String fmtHuntingTicksRunning(GameLevel level) {
        return "Running:   %d".formatted(level.huntingTimer().tick());
    }

    private String fmtHuntingTicksRemaining(GameLevel level) {
        return "Remaining: %s".formatted(ticksToString(level.huntingTimer().remaining()));
    }

    private String fmtPelletCount(GameLevel level) {
        var world = level.world();
        return String.format("%d of %d (%d energizers)", world.uneatenFoodCount(),
            world.totalFoodCount(), world.energizerTiles().count());
    }

    private String fmtGhostSpeed(GameLevel level) {
        return fmtSpeed(level.ghostSpeedPercentage);
    }

    private String fmtGhostSpeedFrightened(GameLevel level) {
        return fmtSpeed(level.ghostSpeedFrightenedPercentage);
    }

    private String fmtGhostSpeedTunnel(GameLevel level) {
        return fmtSpeed(level.ghostSpeedTunnelPercentage);
    }

    private String fmtPacSpeed(GameLevel level) {
        return fmtSpeed(level.pacSpeedPercentage);
    }

    private String fmtPacSpeedPowered(GameLevel level) {
        return fmtSpeed(level.pacSpeedPoweredPercentage);
    }

    private String fmtPacPowerSeconds(GameLevel level) {
        return "%d sec".formatted(level.pacPowerSeconds);
    }

    private String fmtNumFlashes(GameLevel level) {
        return "%d".formatted(level.numFlashes);
    }
}