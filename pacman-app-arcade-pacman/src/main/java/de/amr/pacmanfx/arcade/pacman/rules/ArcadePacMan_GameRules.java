/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rules;

import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ScoringRules;

import java.util.List;

public class ArcadePacMan_GameRules extends Arcade_GameRules {

    public ArcadePacMan_GameRules() {
        actorSpeedRules = new ArcadePacMan_ActorSpeedRules();
        scoringRules = new ArcadePacMan_ScoringRules();
    }

    /** Each level has a bonus symbol appearing twice during the level.
     * From level 13 on, the same symbol ("key") appears.
     * <p>Klingt komisch? Is aber so!</p>
     */
    @Override
    public List<Integer> bonusSymbols(int levelNumber) {
        final int symbol = bonusSymbolCode(levelNumber);
        return List.of(symbol, symbol);
    }

    private int bonusSymbolCode(int levelNumber) {
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
}
