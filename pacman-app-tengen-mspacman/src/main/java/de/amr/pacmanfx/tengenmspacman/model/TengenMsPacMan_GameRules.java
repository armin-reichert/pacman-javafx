/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.core.model.GameRules;
import de.amr.pacmanfx.core.model.actors.CollisionStrategy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

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

    private final TengenMsPacMan_ActorSpeedSettings actorSpeedControl = new TengenMsPacMan_ActorSpeedSettings();

    private final TengenMsPacMan_ScoringRules scoringRules = new TengenMsPacMan_ScoringRules();

    private final ObjectProperty<MapCategory> mapCategory = new SimpleObjectProperty<>(MapCategory.ARCADE);

    public void setMapCategory(MapCategory category) {
        mapCategory.set(category);
    }

    public MapCategory mapCategory() {
        return mapCategory.get();
    }

    public ObjectProperty<MapCategory> mapCategoryProperty() {
        return mapCategory;
    }

    public TengenMsPacMan_GameRules() {
        scoringRules.mapCategoryProperty().bind(mapCategory);
    }

    @Override
    public TengenMsPacMan_ActorSpeedSettings actorSpeedControl() {
        return actorSpeedControl;
    }

    @Override
    public TengenMsPacMan_ScoringRules scoringRules() {
        return scoringRules;
    }

    @Override
    public boolean isLevelCompleted(GameLevel level) {
        return level.worldMap().foodLayer().remainingFoodCount() == 0;
    }

    @Override
    public int lastLevelNumber() {
        return LAST_LEVEL_NUMBER;
    }


    // TODO: I have no idea yet how Tengen Ms. Pac-Man exactly implemented this.
    //       What I know is that the "strange" maps use an extended set of bonus symbols.
    @Override
    public int selectBonusSymbolCode(int levelNumber, int bonusIndex) {
        final int lastSymbolCode = mapCategory() == MapCategory.STRANGE
            ? BonusSymbol.FLOWER.ordinal()
            : BonusSymbol.BANANA.ordinal();

        return levelNumber - 1 <= lastSymbolCode ? levelNumber - 1 : randomInt(0, lastSymbolCode + 1);
    }


    @Override
    public float eatenBonusDisplaySeconds() {
        return 2;
    }

    @Override
    public OptionalInt cutSceneAfterLevel(int levelNumber) {
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
