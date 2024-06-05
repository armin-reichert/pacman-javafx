/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui2d.scene.GameSceneContext;

import static de.amr.games.pacman.lib.TickTimer.ticksToString;

/**
 * Game related settings.
 *
 * @author Armin Reichert
 */
public class InfoBoxGameInfo extends InfoBox {

    public InfoBoxGameInfo(String title) {
        super(title);
    }

    public void init(GameSceneContext context) {
            this.context = context;

        infoText("Game Scene", () -> context.currentGameScene().isPresent()
            ? context.currentGameScene().get().getClass().getSimpleName() : InfoText.NO_INFO);
        infoText("Game State", () -> "%s".formatted(context.gameState()));
        infoText("", () -> "Running:   %s%s".formatted(context.gameState().timer().tick(),
            context.gameState().timer().isStopped() ? " (STOPPED)" : ""));
        infoText("", () -> "Remaining: %s".formatted(ticksToString(context.gameState().timer().remaining())));
        infoText("Game Level", () -> context.game().level().isPresent()
            ? context.game().levelNumber() : InfoText.NO_INFO);
        infoText("World Map", () -> {
            if (context.game().world() != null) {
                String url =  context.game().world().map().url().toString();
                return url.substring(url.lastIndexOf("/") + 1);
            }
            return InfoText.NO_INFO;
        });
        infoText("Lives", () -> context.game().level().isPresent() ? context.game().lives() : InfoText.NO_INFO);

        infoText("Hunting Phase", this::fmtHuntingPhase);
        infoText("", this::fmtHuntingTicksRunning);
        infoText("", this::fmtHuntingTicksRemaining);

        infoText("Pac-Man speed", ifLevelExists(this::fmtPacSpeed));
        infoText("- empowered", ifLevelExists(this::fmtPacSpeedPowered));
        infoText("Pellets", this::fmtPelletCount);
        infoText("Ghost speed", ifLevelExists(this::fmtGhostSpeed));
        infoText("- frightened", ifLevelExists(this::fmtGhostSpeedFrightened));
        infoText("- in tunnel", ifLevelExists(this::fmtGhostSpeedTunnel));
        infoText("Frightened time", ifLevelExists(this::fmtPacPowerSeconds));
        infoText("Maze flashings", ifLevelExists(this::fmtNumFlashes));
    }

    private String fmtHuntingPhase() {
        if (context.game().level().isPresent()) {
            var huntingTimer = context.game().huntingTimer();
            return "%s #%d%s".formatted(context.game().currentHuntingPhaseName(),
                context.game().scatterPhase().isPresent()
                    ? context.game().scatterPhase().get()
                    : context.game().chasingPhase().orElse(42),
                huntingTimer.isStopped() ? " STOPPED" : "");
        }
        return InfoText.NO_INFO;
    }

    private String fmtHuntingTicksRunning() {
        if (context.game().level().isPresent()) {
            return "Running:   %d".formatted(context.game().huntingTimer().tick());
        }
        return InfoText.NO_INFO;
    }

    private String fmtHuntingTicksRemaining() {
        if (context.game().level().isPresent()) {
            return "Remaining: %s".formatted(ticksToString(context.game().huntingTimer().remaining()));
        }
        return InfoText.NO_INFO;
    }

    private String fmtPelletCount() {
        if (context.game().world() != null) {
            World world = context.game().world();
            return String.format("%d of %d (%d energizers)", world.uneatenFoodCount(),
                world.totalFoodCount(), world.energizerTiles().count());
        }
        return InfoText.NO_INFO;
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