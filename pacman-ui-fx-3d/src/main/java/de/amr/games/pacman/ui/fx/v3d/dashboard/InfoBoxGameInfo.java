/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
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

    private String fmtHuntingPhase(GameModel game) {
        var huntingTimer = game.huntingTimer();
        return "%s #%d%s".formatted(game.currentHuntingPhaseName(),
            game.scatterPhase().isPresent() ? game.scatterPhase().get() : game.chasingPhase().orElse(42),
            huntingTimer.isStopped() ? " STOPPED" : "");
    }

    private String fmtHuntingTicksRunning(GameModel game) {
        return "Running:   %d".formatted(game.huntingTimer().tick());
    }

    private String fmtHuntingTicksRemaining(GameModel game) {
        return "Remaining: %s".formatted(ticksToString(game.huntingTimer().remaining()));
    }

    private String fmtPelletCount(GameModel game) {
        var world = game.world();
        return String.format("%d of %d (%d energizers)", world.uneatenFoodCount(),
            world.totalFoodCount(), world.energizerTiles().count());
    }

    private String fmtGhostSpeed(GameModel game) {
        return fmtSpeed(game.level().get().ghostSpeedPercentage());
    }

    private String fmtGhostSpeedFrightened(GameModel game) {
        return fmtSpeed(game.level().get().ghostSpeedFrightenedPercentage());
    }

    private String fmtGhostSpeedTunnel(GameModel game) {
        return fmtSpeed(game.level().get().ghostSpeedTunnelPercentage());
    }

    private String fmtPacSpeed(GameModel game) {
        return fmtSpeed(game.level().get().pacSpeedPercentage());
    }

    private String fmtPacSpeedPowered(GameModel game) {
        return fmtSpeed(game.level().get().pacSpeedPoweredPercentage());
    }

    private String fmtPacPowerSeconds(GameModel game) {
        return "%d sec".formatted(game.level().get().pacPowerSeconds());
    }

    private String fmtNumFlashes(GameModel game) {
        return "%d".formatted(game.level().get().numFlashes());
    }
}