/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.actors.Bonus;

import java.util.EnumMap;
import java.util.Map;
import java.util.OptionalInt;

public class TengenMsPacMan_GameRules implements GameRules {

    // See https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly/blob/main/Data/PowerPelletTimes.asm
    // Hex value divided by 16 gives the duration in seconds
    public static final byte[] POWER_PELLET_TIMES = {
        0x60, 0x50, 0x40, 0x30, 0x20, 0x50, 0x20, 0x1C, // levels 1-8
        0x18, 0x40, 0x20, 0x1C, 0x18, 0x20, 0x1C, 0x18, // levels 9-16
        0x00, 0x18, 0x20                                // levels 17, 18, then 19+
    };

    public static final PacBooster DEFAULT_PAC_BOOSTER = PacBooster.OFF;
    public static final Difficulty DEFAULT_DIFFICULTY = Difficulty.NORMAL;
    public static final MapCategory DEFAULT_MAP_CATEGORY = MapCategory.ARCADE;

    public static final int DEFAULT_START_LEVEL = 1;

    public static final EnumMap<BonusSymbol, Integer> BONUS_VALUES = new EnumMap<>(BonusSymbol.class);
    static {
        BONUS_VALUES.put(BonusSymbol.CHERRY,       100);
        BONUS_VALUES.put(BonusSymbol.STRAWBERRY,   200);
        BONUS_VALUES.put(BonusSymbol.ORANGE,       500);
        BONUS_VALUES.put(BonusSymbol.PRETZEL,      700);
        BONUS_VALUES.put(BonusSymbol.APPLE,       1000);
        BONUS_VALUES.put(BonusSymbol.PEAR,        2000);
        BONUS_VALUES.put(BonusSymbol.BANANA,      5000); // Note!
        BONUS_VALUES.put(BonusSymbol.MILK,        3000); // Note!
        BONUS_VALUES.put(BonusSymbol.ICE_CREAM,   4000); // Note!
        BONUS_VALUES.put(BonusSymbol.HIGH_HEELS,  6000);
        BONUS_VALUES.put(BonusSymbol.STAR,        7000);
        BONUS_VALUES.put(BonusSymbol.HAND,        8000);
        BONUS_VALUES.put(BonusSymbol.RING,        9000);
        BONUS_VALUES.put(BonusSymbol.FLOWER,     10000);
    }

    public static final int FIRST_LEVEL = 1;
    public static final int LAST_LEVEL = 32;

    public static final int DEFAULT_NUM_CONTINUES = 4;

    private static final Map<Integer, Integer> CUT_SCENE_NUMBER_AFTER_LEVEL_NUMBER = Map.of(
        2, 1,
        5, 2,
        9, 3,
        13, 3,
        17, 3,
        LAST_LEVEL, 4
    );

    private final TengenMsPacMan_GameModel gameModel;

    public TengenMsPacMan_GameRules(TengenMsPacMan_GameModel gameModel) {
        this.gameModel = gameModel;
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

    @Override
    public int pointsForBonus(Bonus bonus) {
        return 0;
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
        if (gameModel.mapCategory() == MapCategory.ARCADE) {
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
}
