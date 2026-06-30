/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.fsm.State;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.model.HuntingPhase;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.model.world.MapColorScheme;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapConfigKey;
import de.amr.pacmanfx.ui.game.Game;
import javafx.scene.paint.Color;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static de.amr.basics.timer.TickTimer.secToTicks;
import static de.amr.pacmanfx.core.GameClock.DEFAULT_TICKS_PER_SECOND;
import static de.amr.pacmanfx.model.GameModel.CYAN_GHOST_BASHFUL;
import static de.amr.pacmanfx.uilib.Ufx.formatColorHex;

public class DS_GameInfo extends GameDashboardSection {

    public DS_GameInfo() {
        super(DashboardID.GAME_INFO);
    }

    @Override
    public void connect(Game game) {
        addDynamicInfo("Game State",  () -> game.currentGameContext().state().name());
        addDynamicInfo("State Timer", () -> stateTimerInfo(game.currentGameContext().state()));
        addDynamicInfo("Game Scene", supplyGameSceneInfo(game, gameScene -> gameScene.getClass().getSimpleName()));

        addDynamicInfo("Level Number", supplyGameLevelInfo(game, level ->
            (level.isDemoLevel() ? "%d (Demo Level)" : "%d").formatted(level.number())));

        addDynamicInfo("World Map", supplyGameLevelInfo(game, level -> {
            final String url = level.worldMap().url();
            return url == null
                ? NO_INFO
                : URLDecoder.decode(url.substring(url.lastIndexOf("/") + 1), StandardCharsets.UTF_8);
        }));

        addDynamicInfo("Fill/Stroke/Pellet", supplyGameLevelInfo(game, level -> {
            final WorldMap worldMap = level.worldMap();
            MapColorScheme colorScheme = null;
            if (worldMap.hasConfigValue(WorldMapConfigKey.COLOR_SCHEME)) {
                colorScheme = worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME);
            }
            else if (worldMap.hasConfigValue(WorldMapConfigKey.COLOR_MAP_INDEX)) {
                colorScheme = game.currentGameVariant().colorScheme(worldMap);
            }
            if (colorScheme != null) {
                return "%s/%s/%s".formatted(
                    formatColorHex(Color.valueOf(colorScheme.wallFill())),
                    formatColorHex(Color.valueOf(colorScheme.wallStroke())),
                    formatColorHex(Color.valueOf(colorScheme.pellet())));
            }
            return NO_INFO;
        }));

        addDynamicInfo("Hunting Phase",  supplyGameLevelInfo(game, this::fmtHuntingPhase));
        addDynamicInfo("-Running",       supplyGameLevelInfo(game, level -> fmtHuntingTicksRunning(level.huntingTimer())));
        addDynamicInfo("-Remaining",     supplyGameLevelInfo(game, level -> fmtHuntingTicksRemaining(level.huntingTimer())));
        addDynamicInfo("Collision mode", supplyGameRulesInfo(game, rules -> fmtCollisionMode(rules.getCollisionStrategy())));
        addDynamicInfo("Pac-Man speed",  supplyGameLevelInfo(game, this::fmtPacNormalSpeed));
        addDynamicInfo("- empowered",    supplyGameLevelInfo(game, this::fmtPacSpeedPowered));
        addDynamicInfo("Power Duration", supplyGameLevelInfo(game, this::fmtPacPowerTime));
        addDynamicInfo("Pellets",        supplyGameLevelInfo(game, this::fmtPelletCount));
        addDynamicInfo("Ghost speed",    supplyGameLevelInfo(game, this::fmtGhostAttackSpeed));
        addDynamicInfo("- frightened",   supplyGameLevelInfo(game, this::fmtGhostSpeedFrightened));
        addDynamicInfo("- in tunnel",    supplyGameLevelInfo(game, this::fmtGhostSpeedTunnel));
        addDynamicInfo("Maze flashes",   supplyGameLevelInfo(game, this::fmtNumFlashes));
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
        HuntingTimer timer = level.huntingTimer();
        return "%s #%d%s (%s)".formatted(
            timer.currentHuntingPhase().name(),
            timer.currentHuntingPhase() == HuntingPhase.CHASING
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
        return "%.2f sec".formatted(duration / (float) DEFAULT_TICKS_PER_SECOND);
    }

    private String fmtHuntingTicksRunning(HuntingTimer timer) {
        return "%d".formatted(timer.tickCount());
    }

    private String fmtHuntingTicksRemaining(HuntingTimer timer) {
        return "%d".formatted(timer.remainingTicksOfCurrentPhase());
    }

    private String fmtPelletCount(GameLevel level) {
        FoodLayer foodLayer = level.worldMap().foodLayer();
        return "%d of %d (%d energizers)".formatted(
                foodLayer.remainingFoodCount(),
                foodLayer.totalFoodCount(),
                foodLayer.energizerTiles().size()
        );
    }

    private String fmtGhostAttackSpeed(GameLevel level) {
        // do not use Blinky because he has varying attack speed (Cruise Elroy mode)
        final float speed = level.gameModel().actorSpeedControl().ghostSpeedAttacking(level, level.ghost(CYAN_GHOST_BASHFUL));
        return "%.4f px/s".formatted(speed * DEFAULT_TICKS_PER_SECOND);
    }

    private String fmtGhostSpeedFrightened(GameLevel level) {
        final float speed = level.gameModel().actorSpeedControl().ghostSpeedFrightened(level);
        return "%.4f px/s".formatted(speed * DEFAULT_TICKS_PER_SECOND);
    }

    private String fmtGhostSpeedTunnel(GameLevel level) {
        final float speed = level.gameModel().actorSpeedControl().ghostSpeedTunnel(level.number());
        return "%.4f px/s".formatted(speed * DEFAULT_TICKS_PER_SECOND);
    }

    private String fmtPacNormalSpeed(GameLevel level) {
        final float speed = level.gameModel().actorSpeedControl().pacSpeed(level);
        return "%.4f px/s".formatted(speed * DEFAULT_TICKS_PER_SECOND);
    }

    private String fmtPacSpeedPowered(GameLevel level) {
        final float speed = level.gameModel().actorSpeedControl().pacSpeedWhenHasPower(level);
        return "%.4f px/s".formatted(speed * DEFAULT_TICKS_PER_SECOND);
    }

    private String fmtPacPowerTime(GameLevel level) {
        double powerSec = level.pacPowerSeconds();
        long powerTicks = secToTicks(powerSec);
        return "%.2f sec (%d ticks)".formatted(powerTicks / (float) DEFAULT_TICKS_PER_SECOND, powerTicks);
    }

    private String fmtNumFlashes(GameLevel level) {
        return "%d".formatted(level.numFlashes());
    }
}