/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameSceneContext;

import static de.amr.games.pacman.lib.TickTimer.ticksToString;

/**
 * @author Armin Reichert
 */
public class InfoBoxGameInfo extends InfoBox {

    public void init(GameSceneContext context) {
        this.context = context;

        infoText("Game Scene", ifGameScene(gameScene -> gameScene.getClass().getSimpleName()));
        infoText("Game State", () -> "%s".formatted(context.gameState()));

        infoText("",
            () -> "Running:   %s%s".formatted(
                context.gameState().timer().tick(),
                context.gameState().timer().isStopped() ? " (STOPPED)" : ""));

        infoText("",
            () -> "Remaining: %s".formatted(ticksToString(context.gameState().timer().remaining())));

        infoText("Level Number",ifLevel(level -> "%d".formatted(context.game().levelNumber())));

        infoText("World Map",ifWorld(world -> {
            String url = world.map().url().toString();
            return url.substring(url.lastIndexOf("/") + 1);
        }));

        infoText("Lives",           ifLevel(level -> "%d".formatted(context.game().lives())));

        infoText("Hunting Phase",   ifLevel(this::fmtHuntingPhase));
        infoText("",                ifLevel(this::fmtHuntingTicksRunning));
        infoText("",                ifLevel(this::fmtHuntingTicksRemaining));

        infoText("Pac-Man speed",   ifLevel(this::fmtPacSpeed));
        infoText("- empowered",     ifLevel(this::fmtPacSpeedPowered));
        infoText("Pellets",         ifWorld(this::fmtPelletCount));
        infoText("Ghost speed",     ifLevel(this::fmtGhostSpeed));
        infoText("- frightened",    ifLevel(this::fmtGhostSpeedFrightened));
        infoText("- in tunnel",     ifLevel(this::fmtGhostSpeedTunnel));
        infoText("Frightened time", ifLevel(this::fmtPacPowerSeconds));
        infoText("Maze flashings",  ifLevel(this::fmtNumFlashes));
    }

    private String fmtHuntingPhase(GameLevel level) {
        var game = context.game();
        var huntingTimer = game.huntingTimer();
        return "%s #%d%s".formatted(
            game.currentHuntingPhaseName(),
            game.scatterPhase().orElse(game.chasingPhase().orElse(42)),
            huntingTimer.isStopped() ? " STOPPED" : "");
    }

    private String fmtHuntingTicksRunning(GameLevel level) {
        return "Running:   %d".formatted(context.game().huntingTimer().tick());
    }

    private String fmtHuntingTicksRemaining(GameLevel level) {
        return "Remaining: %s".formatted(ticksToString(context.game().huntingTimer().remaining()));
    }

    private String fmtPelletCount(World world) {
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