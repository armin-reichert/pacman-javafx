package de.amr.pacmanfx.simulation;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.PacEatsFoodEvent;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.TerrainLayer;
import org.tinylog.Logger;

import static de.amr.pacmanfx.core.Globals.HTS;
import static de.amr.pacmanfx.core.Globals.TS;

public final class HuntingResolver {

    private HuntingResolver() {}

    public static void evaluate(GameContext gameContext) {
        final HuntingStepResult result = gameContext.huntingResult();
        final GameModel game = gameContext.gameModel();
        final GameLevel level = game.optGameLevel().orElseThrow();
        final Pac pac = level.entities().pac();

        evalFoodFound(result, gameContext, level, pac);
        if (gameContext.huntingResult().foodFound()) {
            gameContext.gameFlow().publishGameEvent(
                new PacEatsFoodEvent(gameContext, pac, gameContext.huntingResult().energizerFound(), false));
        }

        evalBonusFound(result, gameContext, game, level);

        evalPacKilled(result, game, level, pac);
        if (result.pacKilled()) {
            HuntingResolver.fixPacPositionIfKilledInsidePortal(level, pac);
        }
        else {
            evalGhostsKilled(result, gameContext, game, level);
        }
    }

    private static void evalFoodFound(HuntingStepResult result, GameContext gameContext, GameLevel level, Pac pac) {

        if (!result.foodFound()) {
            pac.continueStarving();
            return;
        }

        pac.endStarving();

        final GameModel gameModel = gameContext.gameModel();
        final Vector2i foodTile = result.foodFoundTile();

        level.worldMap().foodLayer().markFoodEatenAt(foodTile);
        if (result.energizerFound()) {
            gameModel.eatEnergizer(gameContext, level, foodTile);
        } else {
            gameModel.eatPellet(gameContext, level, foodTile);
        }

        if (gameContext.gameRules().isBonusAwarded(level)) {
            gameModel.activateNextBonus(gameContext, level);
        }
    }

    private static void evalBonusFound(HuntingStepResult result, GameContext gameContext, GameModel game, GameLevel level) {
        if (result.foundEdibleBonus()) {
            game.eatBonus(gameContext, level, result.edibleBonus());
        }
    }

    private static void evalPacKilled(HuntingStepResult result, GameModel game, GameLevel level, Pac pac) {
        if (level.isDemoLevel() && game.isPacSafeInDemoLevel(level) || pac.isImmune()) {
            return;
        }
        result.ghostsCollidingWithPac().stream()
            .filter(ghost -> ghost.state() == GhostState.HUNTING_PAC)
            .findFirst().ifPresent(_ -> result.setPacKilled(true));
    }

    private static void evalGhostsKilled(HuntingStepResult result, GameContext gameContext, GameModel game, GameLevel level) {
        if (result.detectedPacGhostCollision()) {
            // Frightened ghosts get killed when colliding with Pac
            result.ghostsCollidingWithPac().stream()
                .filter(ghost -> ghost.state() == GhostState.FRIGHTENED)
                .forEach(result.ghostsKilled()::add);
            // More than one ghost might have been killed in this step
            result.ghostsKilled().forEach(ghost -> game.onEatGhost(gameContext, level, ghost));
        }
    }

    // If collision happened while teleporting (horizontally), move collided actors into visible world
    public static void fixPacPositionIfKilledInsidePortal(GameLevel level, Pac pac) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        terrain.hPortalContainingTile(pac.computeTile()).ifPresent(hPortal -> {
            if (pac.moveDir() == Direction.LEFT) {
                pac.setX(hPortal.rightBorderEntryTile().x() * TS + HTS);
            } else if (pac.moveDir() == Direction.RIGHT) {
                pac.setX(hPortal.leftBorderEntryTile().x() * TS - HTS);
            }
            // Not sure if colliding ghosts should also be moved back to visible area
            Logger.info("Detected collision while teleporting, moved Pac-Man back into world");
        });
    }

}
