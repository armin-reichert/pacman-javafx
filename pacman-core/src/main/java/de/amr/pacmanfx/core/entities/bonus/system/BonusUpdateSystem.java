/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.bonus.system;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusStateComp;
import de.amr.pacmanfx.core.level.GameLevel;

public class BonusUpdateSystem {

    public void update(GameContext game, GameLevel level, Bonus bonus) {
        final GameSystems systems = game.variant().systems();

        final BonusStateComp state = bonus.state();
        switch (state.enumValue()) {
            case INACTIVE -> {}
            case EDIBLE -> {
                systems.bonusState().update(game, bonus);
                systems.bonusMoveAndJump().update(level, bonus, systems.motor());
            }
            case EATEN -> systems.bonusState().update(game, bonus);
        }
    }
}
