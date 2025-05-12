/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HuntingPhase;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import javafx.scene.paint.Color;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.timer.TickTimer.ticksToString;
import static de.amr.pacmanfx.ui.PacManGamesEnvironment.THE_UI_CONFIGS;
import static de.amr.pacmanfx.uilib.Ufx.formatColorHex;

/**
 * @author Armin Reichert
 */
public class InfoBoxGameInfo extends InfoBox {

    public void init() {

        addLabeledValue("Game Scene", ifGameScenePresent(gameScene -> gameScene.getClass().getSimpleName()));
        addLabeledValue("Game State", () -> "%s".formatted(gameState()));
        addLabeledValue("State Timer", this::stateTimerInfo);
        addLabeledValue("Level Number", ifLevelPresent(level -> "%d".formatted(level.number())));
        addLabeledValue("Demo Level", ifLevelPresent(level -> level.isDemoLevel() ? "Yes" : "No"));
        addLabeledValue("World Map", ifLevelPresent(level -> {
            URL url = level.worldMap().url();
            if (url == null) {
                return InfoText.NO_INFO;
            }
            String urlString = URLDecoder.decode(url.toExternalForm(), StandardCharsets.UTF_8);
            return urlString.substring(urlString.lastIndexOf("/") + 1);
        }));
        addLabeledValue("Fill/Stroke/Pellet", ifLevelPresent(level -> {
            WorldMap worldMap = level.worldMap();
            if (worldMap.hasConfigValue("nesColorScheme")) {
                // Tengen Ms. Pac-Man
                var nesColors = (NES_ColorScheme) worldMap.getConfigValue("nesColorScheme");
                Color fillColor = Color.web(nesColors.fillColor());
                Color strokeColor = Color.web(nesColors.strokeColor());
                Color pelletColor = Color.web(nesColors.pelletColor());
                return "%s / %s / %s".formatted(formatColorHex(fillColor), formatColorHex(strokeColor), formatColorHex(pelletColor));
            } else if (worldMap.hasConfigValue("colorMap")) {
                // Pac-Man XXL game
                Map<String, String> colorMap = worldMap.getConfigValue("colorMap");
                Color fillColor = Color.web(colorMap.get("fill"));
                Color strokeColor = Color.web(colorMap.get("stroke"));
                Color pelletColor = Color.web(colorMap.get("pellet"));
                return "%s / %s / %s".formatted(formatColorHex(fillColor), formatColorHex(strokeColor), formatColorHex(pelletColor));
            } else if (worldMap.hasConfigValue("colorMapIndex")) {
                // Arcade games
                WorldMapColorScheme coloring = THE_UI_CONFIGS.current().worldMapColorScheme(worldMap);
                return "%s / %s / %s".formatted(formatColorHex(coloring.fill()), formatColorHex(coloring.stroke()), formatColorHex(coloring.pellet()));
            } else {
                return InfoText.NO_INFO;
            }
        }));

        addLabeledValue("Lives",           ifLevelPresent(level -> "%d".formatted(game().lifeCount())));

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
        TickTimer t = gameState().timer();
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
        HuntingTimer huntingTimer = level.huntingTimer();
        return "%s #%d%s".formatted(
            huntingTimer.phase().name(),
            huntingTimer.phase() == HuntingPhase.CHASING
                ? huntingTimer.currentChasingPhaseIndex().orElse(42)
                : huntingTimer.currentScatterPhaseIndex().orElse(42),
            huntingTimer.isStopped() ? " STOPPED" : "");
    }

    private String fmtHuntingTicksRunning(GameLevel level) {
        return "Running:   %d".formatted(level.huntingTimer().tickCount());
    }

    private String fmtHuntingTicksRemaining(GameLevel level) {
        return "Remaining: %s".formatted(ticksToString(level.huntingTimer().remainingTicks()));
    }

    private String fmtPelletCount(GameLevel level) {
        return "%d of %d (%d energizers)".formatted(
                level.uneatenFoodCount(),
                level.totalFoodCount(),
                level.energizerTiles().count()
        );
    }

    private String fmtGhostAttackSpeed(GameLevel level) {
        // use Pinky because Blinky could be in Elroy mode
        Ghost pinky = level.ghost(PINK_GHOST_ID);
        return (pinky != null)
            ? "%.4f px/s".formatted(level.speedControl().ghostAttackSpeed(level, pinky) * 60)
            : InfoText.NO_INFO;
    }

    private String fmtGhostSpeedFrightened(GameLevel level) {
        Ghost blinky = level.ghost(RED_GHOST_ID);
        return (blinky != null)
            ? "%.4f px/s".formatted(level.speedControl().ghostFrightenedSpeed(level, blinky) * 60)
            : InfoText.NO_INFO;
    }

    private String fmtGhostSpeedTunnel(GameLevel level) {
        Ghost blinky = level.ghost(RED_GHOST_ID);
        return (blinky != null)
            ? "%.4f px/s".formatted(level.speedControl().ghostTunnelSpeed(level, blinky) * 60)
            : InfoText.NO_INFO;
    }

    private String fmtPacNormalSpeed(GameLevel level) {
        return "%.4f px/s".formatted(level.speedControl().pacNormalSpeed(level) * 60);
    }

    private String fmtPacSpeedPowered(GameLevel level) {
        return "%.4f px/s".formatted(level.speedControl().pacPowerSpeed(level) * 60);
    }

    private String fmtPacPowerTime(GameLevel level) {
        return "%.2f sec (%d ticks)".formatted(
            level.game().pacPowerTicks(level) / 60f,
            level.game().pacPowerTicks(level));
    }

    private String fmtNumFlashes(GameLevel level) {
        return "%d".formatted(level.data().numFlashes());
    }
}