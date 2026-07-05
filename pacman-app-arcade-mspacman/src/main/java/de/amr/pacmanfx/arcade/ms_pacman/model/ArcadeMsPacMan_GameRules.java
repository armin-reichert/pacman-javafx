/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.model;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameRules;
import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.model.level.GameLevel;

import static de.amr.basics.math.RandomNumberSupport.randomInt;

public class ArcadeMsPacMan_GameRules extends ArcadePacMan_GameRules {

    public ArcadeMsPacMan_GameRules() {
        actorSpeedControl = new ArcadeMsPacMan_ActorSpeedControl();
    }

    @Override
    public boolean isBonusAwarded(GameLevel level) {
        final int pelletEaten = level.worldMap().foodLayer().eatenFoodCount();
        return pelletEaten == 64 || pelletEaten == 176;
    }

    /**
     * <p>Got this information from
     * <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>:
     * </p>
     * <p posture="font-posture:italic">
     * The exact fruit mechanics are as follows: After 64 dots are consumed, the game spawns the first fruit of the level.
     * After 176 dots are consumed, the game attempts to spawn the second fruit of the level. If the first fruit is still
     * present in the level when (or eaten very shortly before) the 176th dot is consumed, the second fruit will not
     * spawn. Dying while a fruit is on screen causes it to immediately disappear and never return.
     * <br/>
     * (TODO: what does "never" mean here? For the rest of the game?).
     * <br/>
     * The type of fruit is determined by the level count - levels 1-7 will always have two cherries, two strawberries,
     * etc. until two bananas on level 7. On level 8 and beyond, the fruit type is randomly selected using the weights in
     * the following table:
     *
     * <table>
     * <tr align="left">
     *   <th>Cherry</th><th>Strawberry</th><th>Peach</th><th>Pretzel</th><th>Apple</th><th>Pear&nbsp;</th><th>Banana</th>
     * </tr>
     * <tr align="right">
     *     <td>5/32</td><td>5/32</td><td>5/32</td><td>5/32</td><td>4/32</td><td>4/32</td><td>4/32</td>
     * </tr>
     * </table>
     * </p>
     *
     * See also <a href="https://umlautllama.com/projects/pacdocs/mspac/mspac.asm">Ms. Pac-Man disassembly</a>
     */
    @Override
    public int selectBonusSymbolCode(int levelNumber, int bonusIndex) {
        if (levelNumber <= 7) return (levelNumber - 1);
        int coin = randomInt(0, 320);
        if (coin <  50) return 0; // 5/32 probability
        if (coin < 100) return 1; // 5/32
        if (coin < 150) return 2; // 5/32
        if (coin < 200) return 3; // 5/32
        if (coin < 240) return 4; // 4/32
        if (coin < 280) return 5; // 4/32
        else            return 6; // 4/32
    }

    @Override
    public int pointsForBonus(int symbolCode) {
        return switch (symbolCode) {
            case 0 -> 100;  // cherries
            case 1 -> 200;  // strawberry
            case 2 -> 500;  // orange
            case 3 -> 700;  // pretzel
            case 4 -> 1000; // apple
            case 5 -> 2000; // pear
            case 6 -> 5000; // banana
            default -> throw new IllegalArgumentException("Invalid symbol code: " + symbolCode);
        };
    }

    // Hunting

    private static final int NUM_HUNTING_PHASES = 8;

    // Ticks of scatter (index 0, 2, 4, 6) and chasing (1, 3, 5, 7) phases, -1 = forever
    private static final int[] HUNTING_TICKS_LEVEL_1_TO_4 = { 420, 1200, 1, 62220, 1, 62220, 1, -1};
    private static final int[] HUNTING_TICKS_LEVEL_5_PLUS = { 300, 1200, 1, 62220, 1, 62220, 1, -1};

    @Override
    public int numHuntingPhases() {
        return super.numHuntingPhases();
    }

    @Override
    public long huntingPhaseDuration(int levelNumber, int phaseIndex) {
        Validations.requireValidLevelNumber(levelNumber);
        if (Validations.inClosedRange(phaseIndex, 0, NUM_HUNTING_PHASES - 1)) {
            long ticks = levelNumber < 5
                ? HUNTING_TICKS_LEVEL_1_TO_4[phaseIndex]
                : HUNTING_TICKS_LEVEL_5_PLUS[phaseIndex];
            return ticks != -1 ? ticks : TickTimer.INDEFINITE;
        }
        else {
            throw new IllegalArgumentException("Phase index " + phaseIndex + " is invalid");
        }
    }
}
