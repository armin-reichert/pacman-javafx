/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.world.World;
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

        addInfo("Game Scene", () -> context.currentGameScene().isPresent()
            ? context.currentGameScene().get().getClass().getSimpleName() : InfoText.NO_INFO);
        addInfo("Game State", () -> "%s".formatted(context.gameState()));
        addInfo("", () -> "Running:   %s%s".formatted(context.gameState().timer().tick(),
            context.gameState().timer().isStopped() ? " (STOPPED)" : ""));
        addInfo("", () -> "Remaining: %s".formatted(ticksToString(context.gameState().timer().remaining())));
        addInfo("Game Level", () -> context.game().level().isPresent()
            ? context.game().levelNumber() : InfoText.NO_INFO);
        addInfo("World Map", () -> {
            if (context.game().world() != null) {
                String url =  context.game().world().map().url().toString();
                return url.substring(url.lastIndexOf("/") + 1);
            }
            return InfoText.NO_INFO;
        });
        addInfo("Lives", () -> context.game().level().isPresent() ? context.game().lives() : InfoText.NO_INFO);

        addInfo("Hunting Phase", this::fmtHuntingPhase);
        addInfo("", this::fmtHuntingTicksRunning);
        addInfo("", this::fmtHuntingTicksRemaining);

        addInfo("Pac-Man speed", ifLevelExists(this::fmtPacSpeed));
        addInfo("- empowered", ifLevelExists(this::fmtPacSpeedPowered));
        addInfo("Pellets", this::fmtPelletCount);
        addInfo("Ghost speed", ifLevelExists(this::fmtGhostSpeed));
        addInfo("- frightened", ifLevelExists(this::fmtGhostSpeedFrightened));
        addInfo("- in tunnel", ifLevelExists(this::fmtGhostSpeedTunnel));
        addInfo("Frightened time", ifLevelExists(this::fmtPacPowerSeconds));
        addInfo("Maze flashings", ifLevelExists(this::fmtNumFlashes));
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