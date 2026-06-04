package de.amr.pacmanfx.simulation;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.PacEatsFoodEvent;
import de.amr.pacmanfx.flow.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.TerrainLayer;
import org.tinylog.Logger;

import static de.amr.pacmanfx.core.Globals.HTS;
import static de.amr.pacmanfx.core.Globals.TS;

public class HuntingLogic {

    public static GameStateID computeNextState(GameContext context, GameLevel level) {
        if (context.gameModel().rules().isLevelCompleted(level)) {
            return GameStateID.GAME_LEVEL_COMPLETE;
        }
        else if (context.huntingResult().pacKilled()) {
            return GameStateID.GAME_LEVEL_PACMAN_DYING;
        }
        else if (context.huntingResult().hasGhostBeenKilled()) {
            return GameStateID.GAME_LEVEL_EATING_GHOST;
        }
        return GameStateID.GAME_LEVEL_PLAYING;
    }

    public static void evaluate(GameContext context) {
        final HuntingStepResult result = context.huntingResult();
        final GameModel game = context.gameModel();
        final GameLevel level = game.optGameLevel().orElseThrow();
        final Pac pac = level.entities().pac();

        evalFoodFound(result, game, level, pac);
        evalBonusFound(result, game, level);
        evalPacKilled(result, game, level, pac);
        if (!result.pacKilled()) {
            evalGhostsKilled(result, game, level);
        }
    }

    private static void evalFoodFound(HuntingStepResult result, GameModel game, GameLevel level, Pac pac) {
        if (!result.foodFound()) {
            pac.continueStarving();
            return;
        }

        pac.endStarving();

        final Vector2i foodTile = result.foodFoundTile();

        level.worldMap().foodLayer().markFoodEatenAt(foodTile);
        if (result.energizerFound()) {
            game.eatEnergizer(level, foodTile);
        } else {
            game.eatPellet(level, foodTile);
        }

        if (game.rules().isBonusAwarded(level)) {
            game.activateNextBonus(level);
        }
    }

    private static void evalBonusFound(HuntingStepResult result, GameModel game, GameLevel level) {
        if (result.foundEdibleBonus()) {
            game.eatBonus(level, result.edibleBonus());
        }
    }

    private static void evalPacKilled(HuntingStepResult result, GameModel game, GameLevel level, Pac pac) {
        if (level.isDemoLevel() && game.isPacSafeInDemoLevel(level) || pac.isImmune()) {
            return;
        }
        result.ghostsCollidingWithPac().stream()
            .filter(ghost -> ghost.state() == GhostState.HUNTING_PAC)
            .findFirst().ifPresent(pacKiller -> result.setPacKilled(true));
    }

    private static void evalGhostsKilled(HuntingStepResult result, GameModel game, GameLevel level) {
        if (result.detectedPacGhostCollision()) {
            // Frightened ghosts get killed when colliding with Pac
            result.ghostsCollidingWithPac().stream()
                .filter(ghost -> ghost.state() == GhostState.FRIGHTENED)
                .forEach(result.ghostsKilled()::add);
            // More than one ghost might have been killed in this step
            result.ghostsKilled().forEach(ghost -> game.onEatGhost(level, ghost));
        }
    }

}
