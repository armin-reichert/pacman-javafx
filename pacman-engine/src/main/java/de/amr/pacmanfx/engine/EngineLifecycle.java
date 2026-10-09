package de.amr.pacmanfx.engine;

public interface EngineLifecycle {

    void startGame();

    void suspendGame();

    void newGameSession();

    void terminate();

    void enterGameVariant(String variantName);

    void exitGameVariant(String variantName);
}
