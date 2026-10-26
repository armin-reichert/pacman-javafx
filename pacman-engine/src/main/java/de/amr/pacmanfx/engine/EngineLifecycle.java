package de.amr.pacmanfx.engine;

public interface EngineLifecycle {

    void startGame();

    void suspendGame();

    void createNewSession();

    void terminate();

    void enterGameVariant(String variantName);

    void exitGameVariant(String variantName);
}
