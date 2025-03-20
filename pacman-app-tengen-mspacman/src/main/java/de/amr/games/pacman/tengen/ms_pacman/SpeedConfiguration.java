/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.model.GameException;
import de.amr.games.pacman.model.GameLevel;

import static de.amr.games.pacman.lib.Globals.inClosedRange;
import static de.amr.games.pacman.model.GameModel.*;

/**
 *  Got this information from @RussianManSMWC.
 *
 *  @see <a href="https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly/blob/main/Data/PlayerAndGhostSpeeds.asm">here</a>
 */
public interface SpeedConfiguration {

    static float speedUnitsToPixels(float units) {
        return units / 32f;
    }

    static float pacBoosterSpeedDelta() {
        return 0.5f;
    }

    static float pacDifficultySpeedDelta(Difficulty difficulty) {
        return speedUnitsToPixels(switch (difficulty) {
            case EASY -> -4;
            case NORMAL -> 0;
            case HARD -> 12;
            case CRAZY -> 24;
        });
    }

    static float ghostDifficultySpeedDelta(Difficulty difficulty) {
        return speedUnitsToPixels(switch (difficulty) {
            case EASY -> -8;
            case NORMAL -> 0;
            case HARD -> 16;
            case CRAZY -> 32;
        });
    }

    static float ghostIDSpeedDelta(byte ghostID) {
        return speedUnitsToPixels(switch (ghostID) {
            case RED_GHOST -> 3;
            case ORANGE_GHOST -> 2;
            case CYAN_GHOST -> 1;
            case PINK_GHOST -> 0;
            default -> throw GameException.invalidGhostID(ghostID);
        });
    }

    /**
     * @RussianManSMWC on Discord:
     * <p>
     * By the way, there's an additional quirk regarding ghosts' speed.
     * On normal difficulty ONLY and in levels 5 and above, the ghosts become slightly faster if there are few dots remain.
     * if there are 31 or less dots, the speed is increased. the base increase value is 2, which is further increased by 1 for every 8 dots eaten.
     * (I should note it's in subunits. it if was times 2, that would've been crazy)
     * </p>
     */
    static float ghostSpeedIncreaseByFoodRemaining(TengenMsPacMan_GameModel game) {
        GameLevel level = game.level().orElseThrow();
        byte units = 0;
        if (game.difficulty() == Difficulty.NORMAL && level.number() >= 5) {
            int dotsLeft = level.uneatenFoodCount();
            if (dotsLeft <= 7) {
                units = 5;
            } else if (dotsLeft <= 15) {
                units = 4;
            } else if (dotsLeft <= 23) {
                units = 3;
            } else if (dotsLeft <= 31) {
                units = 2;
            }
        }
        return speedUnitsToPixels(units);
    }

    static float pacBaseSpeedInLevel(int levelNumber) {
        int units = 0;
        if      (inClosedRange(levelNumber, 1, 4))   { units = 0x20; }
        else if (inClosedRange(levelNumber, 5, 12))  { units = 0x24; }
        else if (inClosedRange(levelNumber, 13, 16)) { units = 0x28; }
        else if (inClosedRange(levelNumber, 17, 20)) { units = 0x27; }
        else if (inClosedRange(levelNumber, 21, 24)) { units = 0x26; }
        else if (inClosedRange(levelNumber, 25, 28)) { units = 0x25; }
        else if (levelNumber >= 29)            { units = 0x24; }

        return speedUnitsToPixels(units);
    }

    // TODO: do they all have the same base speed? Unclear from disassembly data.
    static float ghostBaseSpeedInLevel(int levelNumber) {
        int units = 0x20; // default: 32
        if      (inClosedRange(levelNumber, 1, 4))  { units = 0x18; }
        else if (inClosedRange(levelNumber, 5, 12)) { units = 0x20 + (levelNumber - 5); } // 0x20-0x27
        else if (levelNumber >= 13)           { units = 0x28;}

        return speedUnitsToPixels(units);
    }
}