package de.amr.pacmanfx.simulation;

import de.amr.pacmanfx.core.GameContext;

public class Simulation {

    public static SimulationStep doHuntingStep(GameContext gameContext, long tick) {
        final SimulationStep step = gameContext.simulationStep();
        step.init(tick);
        step.simulate(gameContext.gameModel().optGameLevel().orElseThrow());
        return step;
    }
}
