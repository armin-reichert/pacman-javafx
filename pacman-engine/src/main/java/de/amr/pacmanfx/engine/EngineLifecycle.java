package de.amr.pacmanfx.engine;

public interface EngineLifecycle {

    void startSession();

    void endSession();

    void createNewSession();

    void terminate();
}
