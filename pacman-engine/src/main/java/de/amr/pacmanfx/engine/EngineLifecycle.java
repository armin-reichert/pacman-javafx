package de.amr.pacmanfx.engine;

import de.amr.pacmanfx.core.GameContext;

public interface EngineLifecycle {

    void startGame();

    void suspendGame();

    void newGameSession(GameContext game);

    void terminate();

    void enterGameVariant(String variantName);

    void exitGameVariant(String variantName);
}
