/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.AbstractHuntingTimer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HuntingPhase;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Config;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.CYAN_GHOST_BASHFUL;
import static de.amr.pacmanfx.Globals.NUM_TICKS_PER_SEC;
import static de.amr.pacmanfx.lib.TickTimer.secToTicks;
import static de.amr.pacmanfx.uilib.Ufx.formatColorHex;

public class InfoBoxGameInfo extends InfoBox {

    public InfoBoxGameInfo(GameUI ui) {
        super(ui);
    }

    @Override
    public void init(GameUI ui) {
        addDynamicLabeledValue("Game State", () -> "%s".formatted(ui.context().currentGame().control().state().name()));
        addDynamicLabeledValue("State Timer", this::stateTimerInfo);
        addDynamicLabeledValue("Game Scene", ifGameScenePresent(gameScene -> gameScene.getClass().getSimpleName()));

        addDynamicLabeledValue("Level Number", ifGameLevel(level ->
            (level.isDemoLevel() ? "%d (Demo Level)" : "%d").formatted(level.number())));
        addDynamicLabeledValue("World Map", ifGameLevel(level -> {
            String url = level.worldMap().url();
            return url == null ? NO_INFO : url.substring(url.lastIndexOf("/") + 1);
        }));
        addDynamicLabeledValue("Fill/Stroke/Pellet", ifGameLevel(level -> {
            WorldMap worldMap = level.worldMap();
            //TODO create "plugin" mechanism for variant-specific info
            if (worldMap.hasConfigValue("nesColorScheme")) {
                // Tengen Ms. Pac-Man
                var nesColors = (NES_ColorScheme) worldMap.getConfigValue("nesColorScheme");
                Color fillColor = Color.valueOf(nesColors.fillColorRGB());
                Color strokeColor = Color.valueOf(nesColors.strokeColorRGB());
                Color pelletColor = Color.valueOf(nesColors.pelletColorRGB());
                return "%s / %s / %s".formatted(formatColorHex(fillColor), formatColorHex(strokeColor), formatColorHex(pelletColor));
            } else if (worldMap.hasConfigValue(GameUI_Config.ConfigKey.COLOR_SCHEME)) {
                // Pac-Man XXL game
                WorldMapColorScheme colorScheme = worldMap.getConfigValue(GameUI_Config.ConfigKey.COLOR_SCHEME);
                Color fillColor = Color.valueOf(colorScheme.wallFill());
                Color strokeColor = Color.valueOf(colorScheme.wallStroke());
                Color pelletColor = Color.valueOf(colorScheme.pellet());
                return "%s / %s / %s".formatted(formatColorHex(fillColor), formatColorHex(strokeColor), formatColorHex(pelletColor));
            } else if (worldMap.hasConfigValue(GameUI_Config.ConfigKey.COLOR_MAP_INDEX)) {
                // Arcade games
                WorldMapColorScheme coloring = ui.currentConfig().colorScheme(worldMap);
                return "%s / %s / %s".formatted(coloring.wallFill(), coloring.wallStroke(), coloring.pellet());
            } else {
                return NO_INFO;
            }
        }));

        addDynamicLabeledValue("Hunting Phase",   ifGameLevel(this::fmtHuntingPhase));
        addDynamicLabeledValue("-Running",        ifGameLevel(level -> fmtHuntingTicksRunning(level.huntingTimer())));
        addDynamicLabeledValue("-Remaining",      ifGameLevel(level -> fmtHuntingTicksRemaining(level.huntingTimer())));

        addDynamicLabeledValue("Collision mode",  ifGameLevel(this::fmtCollisionMode));
        addDynamicLabeledValue("Pac-Man speed",   ifGameLevel(this::fmtPacNormalSpeed));
        addDynamicLabeledValue("- empowered",     ifGameLevel(this::fmtPacSpeedPowered));
        addDynamicLabeledValue("Power Duration",  ifGameLevel(this::fmtPacPowerTime));
        addDynamicLabeledValue("Pellets",         ifGameLevel(this::fmtPelletCount));
        addDynamicLabeledValue("Ghost speed",     ifGameLevel(this::fmtGhostAttackSpeed));
        addDynamicLabeledValue("- frightened",    ifGameLevel(this::fmtGhostSpeedFrightened));
        addDynamicLabeledValue("- in tunnel",     ifGameLevel(this::fmtGhostSpeedTunnel));
        addDynamicLabeledValue("Maze flashings",  ifGameLevel(this::fmtNumFlashes));
    }

    private String stateTimerInfo() {
        TickTimer t = ui.context().currentGame().control().state().timer();
        boolean indefinite = t.durationTicks() == TickTimer.INDEFINITE;
        if (t.isStopped()) {
            return "Stopped at tick %s of %s".formatted(t.tickCount(), indefinite ? "∞" : t.durationTicks());
        }
        if (indefinite) {
            return "Tick %s of ∞".formatted(t.tickCount());
        }
        return "Tick %d of %d. Remaining: %d".formatted(t.tickCount(), t.durationTicks(), t.remainingTicks());
    }

    private String fmtCollisionMode(GameLevel level) {
        CollisionStrategy collisionStrategy = level.game().collisionStrategy();
        return switch (collisionStrategy) {
            case SAME_TILE -> "Same Tile";
            case CENTER_DISTANCE -> "Distance-based";
        };
    }

    private String fmtHuntingPhase(GameLevel level) {
        AbstractHuntingTimer timer = level.huntingTimer();
        return "%s #%d%s (%s)".formatted(
            timer.phase().name(),
            timer.phase() == HuntingPhase.CHASING
                ? timer.currentChasingPhaseIndex().orElse(42)
                : timer.currentScatterPhaseIndex().orElse(42),
            timer.isStopped() ? " STOPPED" : "",
            formatDurationAsSeconds(timer.durationTicks())
        );
    }

    private String formatDurationAsSeconds(long duration) {
        if (duration == TickTimer.INDEFINITE) {
            return "indefinite";
        }
        return "%.2f sec".formatted(duration / (float) NUM_TICKS_PER_SEC);
    }

    private String fmtHuntingTicksRunning(AbstractHuntingTimer timer) {
        return "%d".formatted(timer.tickCount());
    }

    private String fmtHuntingTicksRemaining(AbstractHuntingTimer timer) {
        return "%d".formatted(timer.remainingTicksOfCurrentPhase());
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
        if (level.game() instanceof AbstractGameModel gameModel) {
            // do not use Blinky because he has varying attack speed (Cruise Elroy mode)
            return "%.4f px/s".formatted(gameModel.ghostSpeedAttacking(level, level.ghost(CYAN_GHOST_BASHFUL)) * NUM_TICKS_PER_SEC);
        }
        return NO_INFO;
    }

    private String fmtGhostSpeedFrightened(GameLevel level) {
        if (level.game() instanceof AbstractGameModel gameModel) {
            return "%.4f px/s".formatted(gameModel.ghostSpeedWhenFrightened(level) * NUM_TICKS_PER_SEC);
        }
        return NO_INFO;
    }

    private String fmtGhostSpeedTunnel(GameLevel level) {
        if (level.game() instanceof AbstractGameModel gameModel) {
            return "%.4f px/s".formatted(gameModel.ghostSpeedTunnel(level.number()) * NUM_TICKS_PER_SEC);
        }
        return NO_INFO;
    }

    private String fmtPacNormalSpeed(GameLevel level) {
        return "%.4f px/s".formatted(level.game().pacSpeed(level) * NUM_TICKS_PER_SEC);
    }

    private String fmtPacSpeedPowered(GameLevel level) {
        return "%.4f px/s".formatted(level.game().pacSpeedWhenHasPower(level) * NUM_TICKS_PER_SEC);
    }

    private String fmtPacPowerTime(GameLevel level) {
        double powerSec = level.pacPowerSeconds();
        long powerTicks = secToTicks(powerSec);
        return "%.2f sec (%d ticks)".formatted(powerTicks / (float) NUM_TICKS_PER_SEC, powerTicks);
    }

    private String fmtNumFlashes(GameLevel level) {
        return "%d".formatted(level.numFlashes());
    }
}