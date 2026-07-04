/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.model.GameCheats;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.simulation.HuntingStepResult;

public interface GameContext {

    CoinMechanism coinMechanism();

    GameFlow flow();

    default GameState state() {
        return flow().state();
    }

    GameCheats cheats();

    GameModel model();

    HuntingStepResult huntingStepResult();

    void setHuntingStepResult(HuntingStepResult result);
}
