package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.actors.Bonus;

import java.util.Map;
import java.util.OptionalInt;

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
        final int pelletsEaten = level.worldMap().foodLayer().eatenFoodCount();
        return pelletsEaten == 70 || pelletsEaten == 170;
    }

    @Override
    public int pointsForBonus(Bonus bonus) {
        return 0;
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
    public OptionalInt cutSceneNumberAfterLevel(int levelNumber) {
        Integer cutSceneNumber = CUT_SCENE_NUMBER_AFTER_LEVEL_NUMBER.get(levelNumber);
        return cutSceneNumber != null
            ? OptionalInt.of(cutSceneNumber)
            : OptionalInt.empty();
    }
}
