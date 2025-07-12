/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx;

import de.amr.pacmanfx.controller.CoinMechanism;
import de.amr.pacmanfx.controller.GameController;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.SimulationStep;

import java.util.Optional;
import java.util.Random;

public interface GameContext {
    CoinMechanism       theCoinMechanism();
    GameModel           theGame();
    GameController      theGameController();
    GameEventManager    theGameEventManager();
    Optional<GameLevel> optGameLevel();
    GameLevel           theGameLevel();
    GameState           theGameState();
    Random              theRNG();
    SimulationStep      theSimulationStep();
}
