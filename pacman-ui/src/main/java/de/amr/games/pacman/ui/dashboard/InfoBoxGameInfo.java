/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.dashboard;

import de.amr.games.pacman.controller.HuntingTimer;
import de.amr.games.pacman.lib.nes.NES_ColorScheme;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.uilib.WorldMapColoring;
import javafx.scene.paint.Color;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static de.amr.games.pacman.lib.timer.TickTimer.ticksToString;
import static de.amr.games.pacman.uilib.Ufx.formatColorHex;

/**
 * @author Armin Reichert
 */
public class InfoBoxGameInfo extends InfoBox {

    public void init(GameContext context) {
        super.init(context);

        addLabeledValue("Game Scene", ifGameScenePresent(gameScene -> gameScene.getClass().getSimpleName()));
        addLabeledValue("Game State", () -> "%s".formatted(context.gameState()));
        addLabeledValue("State Timer", this::stateTimerInfo);
        addLabeledValue("Level Number", ifLevelPresent(level -> "%d".formatted(level.number)));
        addLabeledValue("Demo Level", ifLevelPresent(gameScene -> context.game().isDemoLevel() ? "Yes" : "No"));
        addLabeledValue("World Map", ifLevelPresent(level -> {
            URL url = level.world().map().url();
            if (url == null) {
                return InfoText.NO_INFO;
            }
            String urlString = URLDecoder.decode(url.toExternalForm(), StandardCharsets.UTF_8);
            return urlString.substring(urlString.lastIndexOf("/") + 1);
        }));
        addLabeledValue("Fill/Stroke/Pellet", ifLevelPresent(level -> {
            WorldMap worldMap = level.world().map();
            if (worldMap.hasConfigValue("nesColorScheme")) {
                // Tengen Ms. Pac-Man
                var nesColors = (NES_ColorScheme) worldMap.getConfigValue("nesColorScheme");
                Color fillColor = Color.valueOf(nesColors.fillColor());
                Color strokeColor = Color.valueOf(nesColors.strokeColor());
                Color pelletColor = Color.valueOf(nesColors.pelletColor());
                return "%s / %s / %s".formatted(formatColorHex(fillColor), formatColorHex(strokeColor), formatColorHex(pelletColor));
            } else if (worldMap.hasConfigValue("colorMap")) {
                // Pac-Man XXL game
                Map<String, String> colorMap = worldMap.getConfigValue("colorMap");
                Color fillColor = Color.valueOf(colorMap.get("fill"));
                Color strokeColor = Color.valueOf(colorMap.get("stroke"));
                Color pelletColor = Color.valueOf(colorMap.get("pellet"));
                return "%s / %s / %s".formatted(formatColorHex(fillColor), formatColorHex(strokeColor), formatColorHex(pelletColor));
            } else if (worldMap.hasConfigValue("colorMapIndex")) {
                // Arcade games
                WorldMapColoring coloring = context.gameConfiguration().worldMapColoring(worldMap);
                return "%s / %s / %s".formatted(formatColorHex(coloring.fill()), formatColorHex(coloring.stroke()), formatColorHex(coloring.pellet()));
            } else {
                return InfoText.NO_INFO;
            }
        }));

        addLabeledValue("Lives",           ifLevelPresent(level -> "%d".formatted(context.game().lives())));

        addLabeledValue("Hunting Phase",   ifLevelPresent(this::fmtHuntingPhase));
        addLabeledValue("",                ifLevelPresent(this::fmtHuntingTicksRunning));
        addLabeledValue("",                ifLevelPresent(this::fmtHuntingTicksRemaining));

        addLabeledValue("Pac-Man speed",   ifLevelPresent(this::fmtPacNormalSpeed));
        addLabeledValue("- empowered",     ifLevelPresent(this::fmtPacSpeedPowered));
        addLabeledValue("Power Duration",  ifLevelPresent(this::fmtPacPowerTime));
        addLabeledValue("Pellets",         ifLevelPresent(this::fmtPelletCount));
        addLabeledValue("Ghost speed",     ifLevelPresent(this::fmtGhostAttackSpeed));
        addLabeledValue("- frightened",    ifLevelPresent(this::fmtGhostSpeedFrightened));
        addLabeledValue("- in tunnel",     ifLevelPresent(this::fmtGhostSpeedTunnel));
        addLabeledValue("Maze flashings",  ifLevelPresent(this::fmtNumFlashes));
    }

    private String stateTimerInfo() {
        TickTimer t = context.gameState().timer();
        boolean indefinite = t.durationTicks() == TickTimer.INDEFINITE;
        if (t.isStopped()) {
            return "Stopped at tick %s of %s".formatted(t.tickCount(), indefinite ? "∞" : t.durationTicks());
        }
        if (indefinite) {
            return "Tick %s of ∞".formatted(t.tickCount());
        }
        return "Tick %d of %d. Remaining: %d".formatted(t.tickCount(), t.durationTicks(), t.remainingTicks());
    }

    private String fmtHuntingPhase(GameLevel level) {
        var game = context.game();
        HuntingTimer huntingTimer = game.huntingTimer();
        return "%s #%d%s".formatted(
            huntingTimer.phaseType().name(),
            huntingTimer.phaseType() == HuntingTimer.PhaseType.CHASING
                ? huntingTimer.currentChasingPhaseIndex().orElse(42)
                : huntingTimer.currentScatterPhaseIndex().orElse(42),
            huntingTimer.isStopped() ? " STOPPED" : "");
    }

    private String fmtHuntingTicksRunning(GameLevel level) {
        return "Running:   %d".formatted(context.game().huntingTimer().tickCount());
    }

    private String fmtHuntingTicksRemaining(GameLevel level) {
        return "Remaining: %s".formatted(ticksToString(context.game().huntingTimer().remainingTicks()));
    }

    private String fmtPelletCount(GameLevel level) {
        return "%d of %d (%d energizers)".formatted(
                level.world().uneatenFoodCount(),
                level.world().totalFoodCount(),
                level.world().energizerTiles().count()
        );
    }

    private String fmtGhostAttackSpeed(GameLevel level) {
        // use Pinky because Blinky could be in Elroy mode
        Ghost pinky = level.ghost(GameModel.PINK_GHOST);
        return (pinky != null) ? "%.4f px/s".formatted(context.game().ghostAttackSpeed(pinky) * 60) : InfoText.NO_INFO;
    }

    private String fmtGhostSpeedFrightened(GameLevel level) {
        Ghost blinky = level.ghost(GameModel.RED_GHOST);
        return (blinky != null) ? "%.4f px/s".formatted(context.game().ghostFrightenedSpeed(blinky) * 60) : InfoText.NO_INFO;
    }

    private String fmtGhostSpeedTunnel(GameLevel level) {
        Ghost blinky = level.ghost(GameModel.RED_GHOST);
        return (blinky != null) ? "%.4f px/s".formatted(context.game().ghostTunnelSpeed(blinky) * 60) : InfoText.NO_INFO;
    }

    private String fmtPacNormalSpeed(GameLevel level) {
        return "%.4f px/s".formatted(context.game().pacNormalSpeed() * 60);
    }

    private String fmtPacSpeedPowered(GameLevel level) {
        return "%.4f px/s".formatted(context.game().pacPowerSpeed() * 60);
    }

    private String fmtPacPowerTime(GameLevel unused) {
        return "%.2f sec (%d ticks)".formatted(context.game().pacPowerTicks() / 60f, context.game().pacPowerTicks());
    }

    private String fmtNumFlashes(GameLevel level) {
        return "%d".formatted(level.numFlashes());
    }
}