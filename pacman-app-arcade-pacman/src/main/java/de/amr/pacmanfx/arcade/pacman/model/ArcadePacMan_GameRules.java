/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.model.GameRules;
import de.amr.pacmanfx.core.model.actors.ActorSpeedSettings;
import de.amr.pacmanfx.core.model.actors.CollisionStrategy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Map;
import java.util.OptionalInt;

import static de.amr.pacmanfx.core.Validations.inClosedRange;
import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;

public class ArcadePacMan_GameRules implements GameRules {

    public static final LevelData[] LEVEL_DATA_TABLE = {
        /* 1*/ LevelData.of( 80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5),
        /* 2*/ LevelData.of( 90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5),
        /* 3*/ LevelData.of( 90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5),
        /* 4*/ LevelData.of( 90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5),
        /* 5*/ LevelData.of(100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5),
        /* 6*/ LevelData.of(100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5),
        /* 7*/ LevelData.of(100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
        /* 8*/ LevelData.of(100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5),
        /* 9*/ LevelData.of(100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3),
        /*10*/ LevelData.of(100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5),
        /*11*/ LevelData.of(100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5),
        /*12*/ LevelData.of(100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
        /*13*/ LevelData.of(100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3),
        /*14*/ LevelData.of(100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5),
        /*15*/ LevelData.of(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
        /*16*/ LevelData.of(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
        /*17*/ LevelData.of(100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0),
        /*18*/ LevelData.of(100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3),
        /*19*/ LevelData.of(100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
        /*20*/ LevelData.of(100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
        /*21*/ LevelData.of( 90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0),
    };

    public static LevelData levelData(int levelNumber) {
        requireValidLevelNumber(levelNumber);
        final int rowIndex = Math.min(levelNumber - 1, LEVEL_DATA_TABLE.length - 1);
        return LEVEL_DATA_TABLE[rowIndex];
    }

    private static final Map<Integer, Integer> CUT_SCENE_NUMBER_AFTER_LEVEL_NUMBER = Map.of(
         2, 1, // after level number 2, play cut scene number 1
         5, 2,
         9, 3,
        13, 3,
        17, 3
    );

    private final BooleanProperty collisionDoubleChecked = new SimpleBooleanProperty(true);

    private final ObjectProperty<CollisionStrategy> collisionStrategy = new SimpleObjectProperty<>(CollisionStrategy.SAME_TILE);

    public BooleanProperty collisionDoubleCheckedProperty() {
        return collisionDoubleChecked;
    }

    public ObjectProperty<CollisionStrategy> collisionStrategyProperty() {
        return collisionStrategy;
    }

    protected ActorSpeedSettings actorSpeedSettings = new Arcade_ActorSpeedSettings();

    public ArcadePacMan_GameRules() {}

    @Override
    public ActorSpeedSettings actorSpeedControl() {
        return actorSpeedSettings;
    }

    @Override
    public boolean isLevelCompleted(GameLevel level) {
        return level.worldMap().foodLayer().remainingFoodCount() == 0;
    }

    @Override
    public int lastLevelNumber() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int pointsForGhost(int killedBefore) {
        return switch (killedBefore) {
            case 0 -> 200;
            case 1 -> 400;
            case 2 -> 800;
            default -> 1600;
        };
    }

    @Override
    public int pointsForPellet() {
        return 10;
    }

    public int restingTicksForPellet() {
        return 1;
    }

    @Override
    public int pointsForEnergizer() {
        return 50;
    }

    @Override
    public int restingTicksForEnergizer() {
        return 3;
    }

    @Override
    public boolean isBonusAwarded(GameLevel level) {
        final int pelletsEaten = level.worldMap().foodLayer().eatenFoodCount();
        return pelletsEaten == 70 || pelletsEaten == 170;
    }

    @Override
    public int selectBonusSymbolCode(int levelNumber, int bonusIndex) {
        // Each level has a single bonus symbol appearing twice during the level.
        // From level 13 on, the same symbol (code=7, "key") appears.
        // Klingt komisch? Is aber so!
        return switch (levelNumber) {
            case 1 -> 0;      // cherries
            case 2 -> 1;      // strawberry
            case 3, 4 -> 2;   // peach
            case 5, 6 -> 3;   // apple
            case 7, 8 -> 4;   // grapes
            case 9, 10 -> 5;  // galaxian
            case 11, 12 -> 6; // bell
            default -> 7;     // key
        };
    }

    @Override
    public int pointsForBonus(int symbolCode) {
        return switch (symbolCode) {
            case 0 -> 100;  // cherries
            case 1 -> 300;  // strawberry
            case 2 -> 500;  // peach
            case 3 -> 700;  // apple
            case 4 -> 1000; // grapes
            case 5 -> 2000; // galaxian
            case 6 -> 3000; // bell
            case 7 -> 5000; // key
            default -> throw new IllegalArgumentException("Invalid symbol code: " + symbolCode);
        };
    }

    @Override
    public float eatenBonusDisplaySeconds() {
        return 2;
    }

    @Override
    public boolean isExtraLifeAwarded(int oldScore, int newScore) {
        return crossedScoreLine(oldScore, newScore, 10_000);
    }

    @Override
    public OptionalInt cutSceneAfterLevel(int levelNumber) {
        Integer cutSceneNumber = CUT_SCENE_NUMBER_AFTER_LEVEL_NUMBER.get(levelNumber);
        return cutSceneNumber != null
            ? OptionalInt.of(cutSceneNumber)
            : OptionalInt.empty();
    }

    @Override
    public int lastCutSceneNumber() {
        return 3;
    }

    // Hunting

    private static final int NUM_HUNTING_PHASES = 8;

    // Ticks of scatter (index 0, 2, 4, 6) and chasing (1, 3, 5, 7) phases, -1 = forever
    private static final int[] HUNTING_TICKS_SEQ_1 = { 420, 1200, 420, 1200, 300,  1200, 300, -1 };
    private static final int[] HUNTING_TICKS_SEQ_2 = { 420, 1200, 420, 1200, 300, 61980,   1, -1 };
    private static final int[] HUNTING_TICKS_SEQ_3 = { 300, 1200, 300, 1200, 300, 62262,   1, -1 };

    @Override
    public int numHuntingPhases() {
        return NUM_HUNTING_PHASES;
    }

    @Override
    public long huntingPhaseDuration(int levelNumber, int phaseIndex) {
        requireValidLevelNumber(levelNumber);
        if (!inClosedRange(phaseIndex, 0, NUM_HUNTING_PHASES - 1)) {
            throw new IllegalArgumentException("Phase index %d is invalid".formatted(phaseIndex));
        }
        final long ticks = switch (levelNumber) {
            case 1       -> HUNTING_TICKS_SEQ_1[phaseIndex];
            case 2, 3, 4 -> HUNTING_TICKS_SEQ_2[phaseIndex];
            default      -> HUNTING_TICKS_SEQ_3[phaseIndex];
        };
        return ticks != -1 ? ticks : TickTimer.INDEFINITE;
    }
}
