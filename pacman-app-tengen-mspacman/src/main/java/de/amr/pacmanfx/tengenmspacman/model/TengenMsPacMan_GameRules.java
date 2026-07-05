/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.level.GameLevel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.EnumMap;
import java.util.Map;
import java.util.OptionalInt;

import static de.amr.basics.math.RandomNumberSupport.randomInt;

// TODO: In Tengen Ms. Pac-Man, the game rules vary depending on the map category!
//       Should I use different rules instances which are switched on map category change or do I make the rules instance
//       mutable and store the current map category?
public class TengenMsPacMan_GameRules implements GameRules {

    // See https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly/blob/main/Data/PowerPelletTimes.asm
    // Hex value divided by 16 gives the duration in seconds
    public static final byte[] POWER_PELLET_TIMES = {
        0x60, 0x50, 0x40, 0x30, 0x20, 0x50, 0x20, 0x1C, // levels 1-8
        0x18, 0x40, 0x20, 0x1C, 0x18, 0x20, 0x1C, 0x18, // levels 9-16
        0x00, 0x18, 0x20                                // levels 17, 18, then 19+
    };

    public static final EnumMap<BonusSymbol, Integer> BONUS_POINTS = new EnumMap<>(BonusSymbol.class);
    static {
        BONUS_POINTS.put(BonusSymbol.CHERRY,       100);
        BONUS_POINTS.put(BonusSymbol.STRAWBERRY,   200);
        BONUS_POINTS.put(BonusSymbol.ORANGE,       500);
        BONUS_POINTS.put(BonusSymbol.PRETZEL,      700);
        BONUS_POINTS.put(BonusSymbol.APPLE,       1000);
        BONUS_POINTS.put(BonusSymbol.PEAR,        2000);
        BONUS_POINTS.put(BonusSymbol.BANANA,      5000); // Note!
        BONUS_POINTS.put(BonusSymbol.MILK,        3000); // Note!
        BONUS_POINTS.put(BonusSymbol.ICE_CREAM,   4000); // Note!
        BONUS_POINTS.put(BonusSymbol.HIGH_HEELS,  6000);
        BONUS_POINTS.put(BonusSymbol.STAR,        7000);
        BONUS_POINTS.put(BonusSymbol.HAND,        8000);
        BONUS_POINTS.put(BonusSymbol.RING,        9000);
        BONUS_POINTS.put(BonusSymbol.FLOWER,     10000);
    }

    public static final int FIRST_LEVEL = 1;
    public static final int LAST_LEVEL_NUMBER = 32;

    private static final Map<Integer, Integer> CUT_SCENE_NUMBER_AFTER_LEVEL_NUMBER = Map.of(
        2, 1,
        5, 2,
        9, 3,
        13, 3,
        17, 3,
        LAST_LEVEL_NUMBER, 4
    );

    private final BooleanProperty collisionDoubleChecked = new SimpleBooleanProperty(true);

    private final ObjectProperty<CollisionStrategy> collisionStrategy = new SimpleObjectProperty<>(CollisionStrategy.CENTER_DISTANCE);

    public BooleanProperty collisionDoubleCheckedProperty() {
        return collisionDoubleChecked;
    }

    public ObjectProperty<CollisionStrategy> collisionStrategyProperty() {
        return collisionStrategy;
    }

    private final TengenMsPacMan_ActorSpeedControl actorSpeedControl = new TengenMsPacMan_ActorSpeedControl();

    private MapCategory currentMapCategory = MapCategory.ARCADE;

    public TengenMsPacMan_GameRules() {}

    public void setCurrentMapCategory(MapCategory currentMapCategory) {
        this.currentMapCategory = currentMapCategory;
    }

    public MapCategory currentMapCategory() {
        return currentMapCategory;
    }

    @Override
    public TengenMsPacMan_ActorSpeedControl actorSpeedControl() {
        return actorSpeedControl;
    }

    @Override
    public boolean isLevelCompleted(GameLevel level) {
        return level.worldMap().foodLayer().remainingFoodCount() == 0;
    }

    @Override
    public int lastLevelNumber() {
        return LAST_LEVEL_NUMBER;
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

    @Override
    public int pointsForEnergizer() {
        return 50;
    }

    @Override
    public boolean isBonusAwarded(GameLevel level) {
        int eatenFoodCount = level.worldMap().foodLayer().eatenFoodCount();
        return eatenFoodCount == 64 || eatenFoodCount == 176;
    }

    // TODO: I have no idea yet how Tengen Ms. Pac-Man exactly implemented this.
    //       What I know is that the "strange" maps use an extended set of bonus symbols.
    @Override
    public int selectBonusSymbolCode(int levelNumber, int bonusIndex) {

        final int lastSymbolCode = currentMapCategory == MapCategory.STRANGE
            ? BonusSymbol.FLOWER.ordinal()
            : BonusSymbol.BANANA.ordinal();

        return levelNumber - 1 <= lastSymbolCode ? levelNumber - 1 : randomInt(0, lastSymbolCode + 1);
    }

    @Override
    public int pointsForBonus(int symbolCode) {
        final BonusSymbol symbol = BonusSymbol.values()[symbolCode];
        return BONUS_POINTS.get(symbol);
    }

    @Override
    public float eatenBonusDisplaySeconds() {
        return 2;
    }

    /*
     * See https://tcrf.net/Ms._Pac-Man_(NES,_Tengen):
     *
     * Humorously, instead of adding a check to disable multiple extra lives,
     * the "Arcade" maze set sets the remaining 3 extra life scores to over 970,000 points,
     * a score normally unachievable without cheat codes, since all maze sets end after 32 stages.
     * This was most likely done to simulate the Arcade game only giving one extra life per game.
     */
    @Override
    public boolean isExtraLifeAwarded(int oldScore, int newScore) {
        if (currentMapCategory == MapCategory.ARCADE) {
            return crossedScoreLine(oldScore, newScore, 10_000)
                || crossedScoreLine(oldScore, newScore, 970_000)
                || crossedScoreLine(oldScore, newScore, 980_000)
                || crossedScoreLine(oldScore, newScore, 990_000);
        }
        else {
            return crossedScoreLine(oldScore, newScore, 10_000)
                || crossedScoreLine(oldScore, newScore, 50_000)
                || crossedScoreLine(oldScore, newScore, 100_000)
                || crossedScoreLine(oldScore, newScore, 300_000);
        }
    }

    @Override
    public OptionalInt cutSceneNumberAfterLevel(int levelNumber) {
        final Integer cutSceneNumber = CUT_SCENE_NUMBER_AFTER_LEVEL_NUMBER.get(levelNumber);
        return cutSceneNumber != null
            ? OptionalInt.of(cutSceneNumber)
            : OptionalInt.empty();
    }

    @Override
    public int lastCutSceneNumber() {
        return 4;
    }

    // Hunting

    private static final int NUM_HUNTING_PHASES = 8;

    private static final long[] HUNTING_TICKS_LEVEL_1_TO_4 = { 420, 1200, 1, 62220, 1, 62220, 1, TickTimer.INDEFINITE };
    private static final long[] HUNTING_TICKS_LEVEL_5_PLUS = { 300, 1200, 1, 62220, 1, 62220, 1, TickTimer.INDEFINITE };

    @Override
    public int numHuntingPhases() {
        return NUM_HUNTING_PHASES;
    }

    @Override
    public long huntingPhaseDuration(int levelNumber, int phaseIndex) {
        Validations.requireValidLevelNumber(levelNumber);
        if (Validations.inClosedRange(phaseIndex, 0, NUM_HUNTING_PHASES - 1)) {
            return levelNumber <= 4
                ? HUNTING_TICKS_LEVEL_1_TO_4[phaseIndex]
                : HUNTING_TICKS_LEVEL_5_PLUS[phaseIndex];        }
        else {
            throw new IllegalArgumentException("Phase index " + phaseIndex + " is invalid");
        }
    }
}
