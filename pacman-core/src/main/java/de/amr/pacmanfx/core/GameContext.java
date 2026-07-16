/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.event.GameEventManager;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.simulation.FrameContext;
import de.amr.pacmanfx.core.simulation.GamePlay;
import de.amr.pacmanfx.core.state.GameState;

/**
 * Facade giving access to non UI related classes.
 */
public interface GameContext {

    CoinMechanism coinMechanism();

    GamePlay gamePlay();

    GameFlowController flow();

    GameEventManager eventManager();

    default GameState state() {
        return flow().state();
    }

    GameCheats cheats();

    GameModel model();

    GameLevel level();

    FrameContext thisFrame();

    void newFrame(long tick);
}
