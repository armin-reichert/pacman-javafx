package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.actors.Bonus;

import java.util.Map;
import java.util.OptionalInt;

public class ArcadeGameRules implements GameRules {

    private static final Map<Integer, Integer> CUT_SCENE_NUMBER_AFTER_LEVEL_NUMBER = Map.of(
         2, 1, // after level number 2, play cut scene number 1
         5, 2,
         9, 3,
        13, 3,
        17, 3
    );

    @Override
    public int pointsForPellet() {
        return 10;
    }

    @Override
    public int pointsForEnergizer() {
        return 50;
    }

    @Override
    public int pointsForBonus(Bonus bonus) {
        return 0;
    }

    @Override
    public boolean isExtraLifeAwarded(int oldScore, int newScore) {
        return crossedScoreLine(oldScore, newScore, 10_000);
    }

    @Override
    public OptionalInt cutSceneNumberAfterLevel(int levelNumber) {
        Integer cutSceneNumber = CUT_SCENE_NUMBER_AFTER_LEVEL_NUMBER.get(levelNumber);
        return cutSceneNumber != null
            ? OptionalInt.of(cutSceneNumber)
            : OptionalInt.empty();
    }
}
