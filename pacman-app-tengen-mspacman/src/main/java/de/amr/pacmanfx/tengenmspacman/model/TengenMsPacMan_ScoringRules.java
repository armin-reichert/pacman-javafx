/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.core.rules.ScoringRules;
import de.amr.pacmanfx.core.model.level.GameLevel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.EnumMap;

import static de.amr.pacmanfx.core.rules.ScoringRules.crossedScoreLine;

public class TengenMsPacMan_ScoringRules implements ScoringRules {

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

    private final ObjectProperty<MapCategory> mapCategory = new SimpleObjectProperty<>(MapCategory.ARCADE);

    public MapCategory mapCategory() {
        return mapCategory.get();
    }

    public ObjectProperty<MapCategory> mapCategoryProperty() {
        return mapCategory;
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
    public int pointsForBonus(int symbolCode) {
        final BonusSymbol symbol = BonusSymbol.values()[symbolCode];
        return BONUS_POINTS.get(symbol);
    }

    @Override
    public boolean isBonusAwarded(GameLevel level) {
        int eatenFoodCount = level.worldMap().foodLayer().eatenFoodCount();
        return eatenFoodCount == 64 || eatenFoodCount == 176;
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
        if (mapCategory() == MapCategory.ARCADE) {
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
}
