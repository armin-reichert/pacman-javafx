/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.fsm.State;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.rules.CollisionStrategy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.FoodLayer;
import de.amr.pacmanfx.core.model.world.MapColorScheme;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.model.world.WorldMapConfigKey;
import de.amr.pacmanfx.core.rules.ActorSpeedRules;
import de.amr.pacmanfx.core.rules.HuntingPhase;
import de.amr.pacmanfx.core.rules.HuntingRules;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import javafx.scene.paint.Color;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static de.amr.basics.timer.TickTimer.secToTicks;
import static de.amr.pacmanfx.core.model.GameModel.CYAN_GHOST_BASHFUL;
import static de.amr.pacmanfx.uilib.Ufx.formatColorHex;

public class DS_GameInfo extends GameDashboardSection {

    public DS_GameInfo() {
        super(DashboardID.GAME_INFO);
    }

    @Override
    public void setGameAppContext(GameAppContext appContext) {

        addDynamicInfo("Game State",  () -> appContext.currentGameContext().state().name());

        addDynamicInfo("State Timer", () -> stateTimerInfo(appContext.currentGameContext().state()));

        addDynamicInfo("Game Scene", fnGameSceneInfo(appContext,
            gameScene -> gameScene.getClass().getSimpleName())
        );

        addDynamicInfo("Level Number", fnGameLevelInfo(appContext,
            level -> (level.isDemoLevel() ? "%d (Demo Level)" : "%d").formatted(level.number()))
        );

        addDynamicInfo("World Map", fnGameLevelInfo(appContext,
            level -> {
                final String url = level.worldMap().url();
                return url == null
                    ? NO_INFO
                    : URLDecoder.decode(url.substring(url.lastIndexOf("/") + 1), StandardCharsets.UTF_8);
            })
        );

        addDynamicInfo("Fill/Stroke/Pellet", fnGameLevelInfo(appContext,
            level -> {
                final WorldMap worldMap = level.worldMap();
                MapColorScheme colorScheme = null;
                if (worldMap.hasConfigValue(WorldMapConfigKey.COLOR_SCHEME)) {
                    colorScheme = worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME);
                }
                else if (worldMap.hasConfigValue(WorldMapConfigKey.COLOR_MAP_INDEX)) {
                    final GameVariantConfig variantConfig = appContext.variants().currentVariant().config();
                    colorScheme = variantConfig.renderConfig().colorScheme(worldMap, variantConfig.worldSettings());
                }
                if (colorScheme != null) {
                    return "%s / %s / %s".formatted(
                        formatColorHex(Color.valueOf(colorScheme.wallFill())),
                        formatColorHex(Color.valueOf(colorScheme.wallStroke())),
                        formatColorHex(Color.valueOf(colorScheme.pellet())));
                }
                return NO_INFO;
            })
        );

        addDynamicInfo("Hunting Phase",  fnGameLevelInfo(appContext, this::fmtHuntingPhase));
        addDynamicInfo("-Running",       fnGameLevelInfo(appContext, level -> fmtHuntingTicksRunning(level.huntingRules())));
        addDynamicInfo("-Remaining",     fnGameLevelInfo(appContext, level -> fmtHuntingTicksRemaining(level.huntingRules())));
        addDynamicInfo("Collision mode", fnGameRulesInfo(appContext, rules -> fmtCollisionMode(rules.actorCollisionRules().getCollisionStrategy())));
        addDynamicInfo("Pac-Man speed",  supplyGameLevelSpeedInfo(appContext, this::fmtPacNormalSpeed));
        addDynamicInfo("- empowered",    supplyGameLevelSpeedInfo(appContext, this::fmtPacSpeedPowered));
        addDynamicInfo("Power Duration", fnGameLevelInfo(appContext, this::fmtPacPowerTime));
        addDynamicInfo("Pellets",        fnGameLevelInfo(appContext, this::fmtPelletCount));
        addDynamicInfo("Ghost speed",    supplyGameLevelSpeedInfo(appContext, this::fmtGhostAttackSpeed));
        addDynamicInfo("- frightened",   supplyGameLevelSpeedInfo(appContext, this::fmtGhostSpeedFrightened));
        addDynamicInfo("- in tunnel",    supplyGameLevelSpeedInfo(appContext, this::fmtGhostSpeedTunnel));
        addDynamicInfo("Maze flashes",   fnGameLevelInfo(appContext, this::fmtNumFlashes));
    }

    private Supplier<String> supplyGameLevelSpeedInfo(
        GameAppContext appContext,
        BiFunction<GameLevel, ActorSpeedRules, String> fnInfo) {
        return () -> {
            final GameModel model = appContext.currentGameContext().model();
            final ActorSpeedRules speedControl = model.rules().actorSpeedRules();
            return model.optLevel().map(level -> fnInfo.apply(level, speedControl)).orElse(NO_INFO);
        };
    }

    private String stateTimerInfo(State<?> gameState) {
        final TickTimer timer = gameState.timer();
        final boolean indefinite = timer.durationTicks() == TickTimer.INDEFINITE;
        if (timer.isStopped()) {
            return "Stopped at tick %s of %s".formatted(timer.tickCount(), indefinite ? "∞" : timer.durationTicks());
        }
        if (indefinite) {
            return "Tick %s of ∞".formatted(timer.tickCount());
        }
        return "Tick %d of %d. Remaining: %d".formatted(timer.tickCount(), timer.durationTicks(), timer.remainingTicks());
    }

    private String fmtCollisionMode(CollisionStrategy collisionStrategy ) {
        return switch (collisionStrategy) {
            case SAME_TILE -> "Same Tile";
            case CENTER_DISTANCE -> "Distance-based";
        };
    }

    private String fmtHuntingPhase(GameLevel level) {
        final HuntingRules huntingRules = level.huntingRules();
        return "%s #%d%s (%s)".formatted(
            huntingRules.currentHuntingPhase().name(),
            huntingRules.currentHuntingPhase() == HuntingPhase.CHASING
                ? huntingRules.currentChasingPhaseIndex().orElse(42)
                : huntingRules.currentScatterPhaseIndex().orElse(42),
            huntingRules.isStopped() ? " STOPPED" : "",
            formatDurationAsSeconds(huntingRules.durationTicks())
        );
    }

    private String formatDurationAsSeconds(long duration) {
        if (duration == TickTimer.INDEFINITE) {
            return "indefinite";
        }
        return "%.2f sec".formatted(duration / (float) GameConstants.SIMULATION_FPS);
    }

    private String fmtHuntingTicksRunning(HuntingRules huntingRules) {
        return "%d".formatted(huntingRules.tickCount());
    }

    private String fmtHuntingTicksRemaining(HuntingRules huntingRules) {
        return "%d".formatted(huntingRules.remainingTicksOfCurrentPhase());
    }

    private String fmtPelletCount(GameLevel level) {
        FoodLayer foodLayer = level.worldMap().foodLayer();
        return "%d of %d (%d energizers)".formatted(
                foodLayer.remainingFoodCount(),
                foodLayer.totalFoodCount(),
                foodLayer.energizerTiles().size()
        );
    }

    private String fmtGhostAttackSpeed(GameLevel level, ActorSpeedRules speedControl) {
        // do not use Blinky because he has varying attack speed (Cruise Elroy mode)
        final float speed = speedControl.ghostSpeedAttacking(level, level.ghost(CYAN_GHOST_BASHFUL));
        return "%.4f px/s".formatted(speed * GameConstants.SIMULATION_FPS);
    }

    private String fmtGhostSpeedFrightened(GameLevel level, ActorSpeedRules speedControl) {
        final float speed = speedControl.ghostSpeedFrightened(level);
        return "%.4f px/s".formatted(speed * GameConstants.SIMULATION_FPS);
    }

    private String fmtGhostSpeedTunnel(GameLevel level, ActorSpeedRules speedControl) {
        final float speed = speedControl.ghostSpeedTunnel(level.number());
        return "%.4f px/s".formatted(speed * GameConstants.SIMULATION_FPS);
    }

    private String fmtPacNormalSpeed(GameLevel level, ActorSpeedRules speedControl) {
        final float speed = speedControl.pacSpeed(level);
        return "%.4f px/s".formatted(speed * GameConstants.SIMULATION_FPS);
    }

    private String fmtPacSpeedPowered(GameLevel level, ActorSpeedRules speedControl) {
        final float speed = speedControl.pacSpeedWhenHasPower(level);
        return "%.4f px/s".formatted(speed * GameConstants.SIMULATION_FPS);
    }

    private String fmtPacPowerTime(GameLevel level) {
        double powerSec = level.pacPowerSeconds();
        long powerTicks = secToTicks(powerSec);
        return "%.2f sec (%d ticks)".formatted(powerTicks / (float) GameConstants.SIMULATION_FPS, powerTicks);
    }

    private String fmtNumFlashes(GameLevel level) {
        return "%d".formatted(level.numFlashes());
    }
}