/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.event.GameEventManager;
import de.amr.pacmanfx.core.flow.GameFlow;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.simulation.GamePlay;
import de.amr.pacmanfx.core.simulation.GamePlayContext;
import de.amr.pacmanfx.core.state.GameState;

/**
 * Facade giving access to non UI related classes.
 */
public interface GameContext {

    CoinMechanism coinMechanism();

    GamePlay gamePlay();

    GameFlow flow();

    GameEventManager eventManager();

    default GameState state() {
        return flow().state();
    }

    GameCheats cheats();

    GameModel model();

    default GamePlayContext createPlayContext() {
        return new GamePlayContext(model().assertLevel(), model(), eventManager());
    }

    default GamePlayContext createPlayContextWithoutLevel() {
        return new GamePlayContext(null, model(), eventManager());
    }
}
