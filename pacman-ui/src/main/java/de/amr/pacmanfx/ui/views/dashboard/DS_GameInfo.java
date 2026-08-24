/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.fsm.State;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.rules.*;
import de.amr.pacmanfx.core.model.world.map.FoodLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import javafx.scene.paint.Color;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static de.amr.basics.timer.TickTimer.secToTicks;
import static de.amr.basics.util.Ufx.formatColorHex;

public class DS_GameInfo extends GameDashboardSection {

    public DS_GameInfo() {
        super(DashboardID.GAME_INFO);
    }

    @Override
    public void setGameApp(GameAppContext app) {

        addDynamicInfo("Game State",  () -> app.game().state().name());

        addDynamicInfo("State Timer", () -> stateTimerInfo(app.game().state()));

        addDynamicInfo("Game Scene", fnGameSceneInfo(app,
            gameScene -> gameScene.getClass().getSimpleName())
        );

        addDynamicInfo("Level Number", fnLevelInfo(app,
            level -> (app.game().session().isAttractMode() ? "%d (Demo Level)" : "%d").formatted(level.number()))
        );

        addDynamicInfo("World Map", fnLevelInfo(app,
            level -> {
                final String url = level.worldMap().url();
                return url == null
                    ? NO_INFO
                    : URLDecoder.decode(url.substring(url.lastIndexOf("/") + 1), StandardCharsets.UTF_8);
            })
        );

        addDynamicInfo("Fill/Stroke/Pellet", fnLevelInfo(app,
            level -> {
                final WorldMap worldMap = level.worldMap();
                WorldMapColorScheme colorScheme = null;
                if (worldMap.hasConfigValue(WorldMapConfigKey.COLOR_SCHEME)) {
                    colorScheme = worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME);
                }
                else if (worldMap.hasConfigValue(WorldMapConfigKey.COLOR_MAP_INDEX)) {
                    final GameVariantUIConfig variantConfig = app.gameVariants().currentGameVariant().uiConfig();
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

        addDynamicInfo("Pac lives",      () -> app.game().session().numLives());
        addDynamicInfo("Hunting Phase",  fnLevelInfo(app, this::fmtHuntingPhase));
        addDynamicInfo("-Running",       fnLevelInfo(app, level -> fmtHuntingTicksRunning(level.huntingTimerStrategy())));
        addDynamicInfo("-Remaining",     fnLevelInfo(app, level -> fmtHuntingTicksRemaining(level.huntingTimerStrategy())));
        addDynamicInfo("Collision mode", fnRulesInfo(app, rules -> fmtCollisionMode(rules.actorCollisionRules().getCollisionStrategy())));
        addDynamicInfo("Pac-Man speed",  supplyLevelSpeedInfo(app, (level, rules) -> fmtPacNormalSpeed(app.game(), level, rules)));
        addDynamicInfo("- empowered",    supplyLevelSpeedInfo(app, (level, rules) -> fmtPacSpeedPowered(app.game(), level, rules)));
        addDynamicInfo("Power Duration", fnLevelInfo(app, level -> fmtPacPowerTime(rules(app), level)));
        addDynamicInfo("Pellets",        fnLevelInfo(app, this::fmtPelletCount));
        addDynamicInfo("Ghost speed",    supplyLevelSpeedInfo(app, this::fmtGhostAttackSpeed));
        addDynamicInfo("- frightened",   supplyLevelSpeedInfo(app, this::fmtGhostSpeedFrightened));
        addDynamicInfo("- in tunnel",    supplyLevelSpeedInfo(app, this::fmtGhostSpeedTunnel));
        addDynamicInfo("Maze flashes",   fnLevelInfo(app, level -> fmtNumFlashes(rules(app), level)));
    }

    private GameRules rules(GameAppContext app) {
        return app.game().variant().rules();
    }

    private Supplier<String> supplyLevelSpeedInfo(
        GameAppContext appContext,
        BiFunction<GameLevel, ActorSpeedRules, String> fnInfo) {
        return () -> {
            final GameContext game = appContext.game();
            final ActorSpeedRules speedRules = game.variant().rules().actorSpeedRules();
            return game.session().optLevel()
                .map(level -> fnInfo.apply(level, speedRules)).orElse(NO_INFO);
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
            case BOX_INTERSECTION -> "Box Intersection";
        };
    }

    private String fmtHuntingPhase(GameLevel level) {
        final HuntingTimerStrategy huntingRules = level.huntingTimerStrategy();
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

    private String fmtHuntingTicksRunning(HuntingTimerStrategy huntingRules) {
        return "%d".formatted(huntingRules.tickCount());
    }

    private String fmtHuntingTicksRemaining(HuntingTimerStrategy huntingRules) {
        return "%d".formatted(huntingRules.remainingTicksOfCurrentPhase());
    }

    private String fmtPelletCount(GameLevel level) {
        FoodLayer foodLayer = level.worldMap().foodLayer();
        return "%d of %d (%d energizers)".formatted(
            level.food().remainingFoodCount(),
            level.food().totalFoodCount(),
            foodLayer.energizerTiles().size()
        );
    }

    private String fmtGhostAttackSpeed(GameLevel level, ActorSpeedRules speedControl) {
        // do not use Blinky because he has varying attack speed (Cruise Elroy mode)
        final float speed = speedControl.ghostSpeedAttacking(level, level.entities().ghost(GhostPersonality.CYAN_GHOST_BASHFUL));
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

    private String fmtPacNormalSpeed(GameContext game, GameLevel level, ActorSpeedRules speedControl) {
        final float speed = speedControl.pacSpeed(game, level);
        return "%.4f px/s".formatted(speed * GameConstants.SIMULATION_FPS);
    }

    private String fmtPacSpeedPowered(GameContext game, GameLevel level, ActorSpeedRules speedControl) {
        final float speed = speedControl.pacSpeedWhenHasPower(game, level);
        return "%.4f px/s".formatted(speed * GameConstants.SIMULATION_FPS);
    }

    private String fmtPacPowerTime(GameRules rules, GameLevel level) {
        final double powerSec = rules.pacPowerSeconds(level.number());
        final long powerTicks = secToTicks(powerSec);
        return "%.2f sec (%d ticks)".formatted(powerTicks / (float) GameConstants.SIMULATION_FPS, powerTicks);
    }

    private String fmtNumFlashes(GameRules rules, GameLevel level) {
        return "%d".formatted(rules.numLevelFlashes(level.number()));
    }
}