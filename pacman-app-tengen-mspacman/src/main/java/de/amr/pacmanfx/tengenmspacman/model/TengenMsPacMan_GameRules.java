package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.actors.Bonus;

import java.util.Map;
import java.util.OptionalInt;

public class TengenMsPacMan_GameRules implements GameRules {

    public static final int FIRST_LEVEL = 1;
    public static final int LAST_LEVEL = 32;

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
}
