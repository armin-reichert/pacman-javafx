/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.lib.worldmap.FoodLayer;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HuntingPhase;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import javafx.scene.paint.Color;

import java.util.Map;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.timer.TickTimer.secToTicks;
import static de.amr.pacmanfx.lib.timer.TickTimer.ticksToString;
import static de.amr.pacmanfx.ui.api.GameUI_Config.CONFIG_KEY_COLOR_MAP;
import static de.amr.pacmanfx.ui.api.GameUI_Config.CONFIG_KEY_COLOR_MAP_INDEX;
import static de.amr.pacmanfx.uilib.Ufx.formatColorHex;

/**
 * @author Armin Reichert
 */
public class InfoBoxGameInfo extends InfoBox {

    public InfoBoxGameInfo(GameUI ui) {
        super(ui);
    }

    @Override
    public void init(GameUI ui) {
        addDynamicLabeledValue("Game State", () -> "%s".formatted(ui.context().currentGame().state().name()));
        addDynamicLabeledValue("State Timer", this::stateTimerInfo);
        addDynamicLabeledValue("Game Scene", ifGameScenePresent(gameScene -> gameScene.getClass().getSimpleName()));

        addDynamicLabeledValue("Level Number", ifGameLevelPresent(level ->
            (level.isDemoLevel() ? "%d (Demo Level)" : "%d").formatted(level.number())));
        addDynamicLabeledValue("World Map", ifGameLevelPresent(level -> {
            String url = level.worldMap().url();
            return url == null ? NO_INFO : url.substring(url.lastIndexOf("/") + 1);
        }));
        addDynamicLabeledValue("Fill/Stroke/Pellet", ifGameLevelPresent(level -> {
            WorldMap worldMap = level.worldMap();
            //TODO create "plugin" mechanism for variant-specific info
            if (worldMap.hasConfigValue("nesColorScheme")) {
                // Tengen Ms. Pac-Man
                var nesColors = (NES_ColorScheme) worldMap.getConfigValue("nesColorScheme");
                Color fillColor = Color.web(nesColors.fillColorRGB());
                Color strokeColor = Color.web(nesColors.strokeColorRGB());
                Color pelletColor = Color.web(nesColors.pelletColorRGB());
                return "%s / %s / %s".formatted(formatColorHex(fillColor), formatColorHex(strokeColor), formatColorHex(pelletColor));
            } else if (worldMap.hasConfigValue(CONFIG_KEY_COLOR_MAP)) {
                // Pac-Man XXL game
                Map<String, String> colorMap = worldMap.getConfigValue(CONFIG_KEY_COLOR_MAP);
                Color fillColor = Color.web(colorMap.get("fill"));
                Color strokeColor = Color.web(colorMap.get("stroke"));
                Color pelletColor = Color.web(colorMap.get("pellet"));
                return "%s / %s / %s".formatted(formatColorHex(fillColor), formatColorHex(strokeColor), formatColorHex(pelletColor));
            } else if (worldMap.hasConfigValue(CONFIG_KEY_COLOR_MAP_INDEX)) {
                // Arcade games
                WorldMapColorScheme coloring = ui.currentConfig().colorScheme(worldMap);
                return "%s / %s / %s".formatted(formatColorHex(coloring.fill()), formatColorHex(coloring.stroke()), formatColorHex(coloring.pellet()));
            } else {
                return NO_INFO;
            }
        }));

        addDynamicLabeledValue("Hunting Phase",   ifGameLevelPresent(gameLevel -> fmtHuntingPhase()));
        addDynamicLabeledValue("",                ifGameLevelPresent(gameLevel -> fmtHuntingTicksRunning(gameLevel.huntingTimer())));
        addDynamicLabeledValue("",                ifGameLevelPresent(gameLevel -> fmtHuntingTicksRemaining(gameLevel.huntingTimer())));

        addDynamicLabeledValue("Collision mode",  ifGameLevelPresent(this::fmtCollisionMode));
        addDynamicLabeledValue("Pac-Man speed",   ifGameLevelPresent(this::fmtPacNormalSpeed));
        addDynamicLabeledValue("- empowered",     ifGameLevelPresent(this::fmtPacSpeedPowered));
        addDynamicLabeledValue("Power Duration",  ifGameLevelPresent(this::fmtPacPowerTime));
        addDynamicLabeledValue("Pellets",         ifGameLevelPresent(this::fmtPelletCount));
        addDynamicLabeledValue("Ghost speed",     ifGameLevelPresent(this::fmtGhostAttackSpeed));
        addDynamicLabeledValue("- frightened",    ifGameLevelPresent(this::fmtGhostSpeedFrightened));
        addDynamicLabeledValue("- in tunnel",     ifGameLevelPresent(this::fmtGhostSpeedTunnel));
        addDynamicLabeledValue("Maze flashings",  ifGameLevelPresent(this::fmtNumFlashes));
    }

    private String stateTimerInfo() {
        TickTimer t = ui.context().currentGame().state().timer();
        boolean indefinite = t.durationTicks() == TickTimer.INDEFINITE;
        if (t.isStopped()) {
            return "Stopped at tick %s of %s".formatted(t.tickCount(), indefinite ? "∞" : t.durationTicks());
        }
        if (indefinite) {
            return "Tick %s of ∞".formatted(t.tickCount());
        }
        return "Tick %d of %d. Remaining: %d".formatted(t.tickCount(), t.durationTicks(), t.remainingTicks());
    }

    private String fmtCollisionMode(GameLevel gameLevel) {
        CollisionStrategy collisionStrategy = gameLevel.game().collisionStrategy();
        return switch (collisionStrategy) {
            case SAME_TILE -> "Same Tile";
            case CENTER_DISTANCE -> "Distance-based";
        };
    }

    private String fmtHuntingPhase() {
        if (ui.context().currentGame().level() != null) {
            HuntingTimer huntingTimer = ui.context().currentGame().level().huntingTimer();
            return "%s #%d%s".formatted(
                huntingTimer.phase().name(),
                huntingTimer.phase() == HuntingPhase.CHASING
                    ? huntingTimer.currentChasingPhaseIndex().orElse(42)
                    : huntingTimer.currentScatterPhaseIndex().orElse(42),
                huntingTimer.isStopped() ? " STOPPED" : "");
        }
        return NO_INFO;
    }

    private String fmtHuntingTicksRunning(HuntingTimer huntingTimer) {
        return "Running:   %d".formatted(huntingTimer.tickCount());
    }

    private String fmtHuntingTicksRemaining(HuntingTimer huntingTimer) {
        return "Remaining: %s".formatted(ticksToString(huntingTimer.remainingTicks()));
    }

    private String fmtPelletCount(GameLevel level) {
        FoodLayer foodLayer = level.worldMap().foodLayer();
        return "%d of %d (%d energizers)".formatted(
                foodLayer.uneatenFoodCount(),
                foodLayer.totalFoodCount(),
                foodLayer.energizerTiles().size()
        );
    }

    private String fmtGhostAttackSpeed(GameLevel level) {
        // use Pinky because Blinky could be in Elroy mode
        Ghost pinky = level.ghost(PINK_GHOST_SPEEDY);
        return (pinky != null)
            ? "%.4f px/s".formatted(level.game().ghostSpeedWhenAttacking(level, pinky) * NUM_TICKS_PER_SEC)
            : NO_INFO;
    }

    private String fmtGhostSpeedFrightened(GameLevel level) {
        Ghost blinky = level.ghost(RED_GHOST_SHADOW);
        return (blinky != null)
            ? "%.4f px/s".formatted(level.game().ghostSpeedWhenFrightened(level, blinky) * NUM_TICKS_PER_SEC)
            : NO_INFO;
    }

    private String fmtGhostSpeedTunnel(GameLevel level) {
        Ghost blinky = level.ghost(RED_GHOST_SHADOW);
        return (blinky != null)
            ? "%.4f px/s".formatted(level.game().ghostSpeedInsideTunnel(level, blinky) * NUM_TICKS_PER_SEC)
            : NO_INFO;
    }

    private String fmtPacNormalSpeed(GameLevel level) {
        return "%.4f px/s".formatted(level.game().pacSpeed(level) * NUM_TICKS_PER_SEC);
    }

    private String fmtPacSpeedPowered(GameLevel level) {
        return "%.4f px/s".formatted(level.game().pacSpeedWhenHasPower(level) * NUM_TICKS_PER_SEC);
    }

    private String fmtPacPowerTime(GameLevel level) {
        double powerSec = level.game().pacPowerSeconds(level);
        long powerTicks = secToTicks(powerSec);
        return "%.2f sec (%d ticks)".formatted(powerTicks / (float) NUM_TICKS_PER_SEC, powerTicks);
    }

    private String fmtNumFlashes(GameLevel level) {
        return "%d".formatted(level.game().numFlashes(level));
    }
}