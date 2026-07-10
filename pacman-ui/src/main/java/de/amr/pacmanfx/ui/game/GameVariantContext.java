/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.GameCheats;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.simulation.GamePlay;

import static java.util.Objects.requireNonNull;

/**
 * Context for the currently running game variant.
 */
public class GameVariantContext implements GameContext {

    private final CoinMechanism coinMechanism;

    private final GameVariant gameVariant;

    private final GameEventManager eventManager;

    public GameVariantContext(CoinMechanism coinMechanism, GameVariant gameVariant) {
        this.coinMechanism = requireNonNull(coinMechanism);
        this.gameVariant = requireNonNull(gameVariant);
        this.eventManager = new GameEventManagerImpl();
        //TODO rethink this
        model().hudState().creditProperty().bind(coinMechanism().numCoinsProperty());
    }

    @Override
    public GameCheats cheats() {
        return gameVariant.cheats();
    }

    @Override
    public CoinMechanism coinMechanism() {
        return coinMechanism;
    }

    @Override
    public GameEventManager eventManager() {
        return eventManager;
    }

    @Override
    public GameModel model() {
        return gameVariant.gameModel();
    }

    @Override
    public GameFlow flow() {
        return gameVariant.gameFlow();
    }

    @Override
    public GamePlay gamePlay() {
        return gameVariant.gamePlay();
    }
}
