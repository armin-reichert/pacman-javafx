package de.amr.pacmanfx.engine;

public interface EngineLifecycle {

    void startSession();

    void endSession();

    void restartSession();

    void createNewSession();

    void terminate();
}
