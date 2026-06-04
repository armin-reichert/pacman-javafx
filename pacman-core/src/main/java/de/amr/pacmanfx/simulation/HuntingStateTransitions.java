package de.amr.pacmanfx.simulation;

import de.amr.pacmanfx.flow.GameStateID;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.level.GameLevel;

public class HuntingStateTransitions {

    private HuntingStateTransitions() {}

    public static GameStateID computeNextState(HuntingStepResult result, GameRules rules, GameLevel level) {
        if (rules.isLevelCompleted(level)) {
            return GameStateID.GAME_LEVEL_COMPLETE;
        }
        else if (result.pacKilled()) {
            return GameStateID.GAME_LEVEL_PACMAN_DYING;
        }
        else if (result.hasGhostBeenKilled()) {
            return GameStateID.GAME_LEVEL_EATING_GHOST;
        }
        return GameStateID.GAME_LEVEL_PLAYING;
    }

}
