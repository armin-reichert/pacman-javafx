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
import de.amr.pacmanfx.model.world.WorldMap;
import org.tinylog.Logger;

public final class EntityCollisionResolver {

    private EntityCollisionResolver() {}

    public static void evaluateCollisions(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        final GameLevel level = gameModel.optGameLevel().orElseThrow();
        final Pac pac = level.entities().pac();

        evalFoodFound(gameContext.huntingStepResult(), gameContext, level, pac);
        if (gameContext.huntingStepResult().foodFound()) {
            gameContext.flow().publishGameEvent(
                new PacEatsFoodEvent(gameContext, pac, gameContext.huntingStepResult().energizerFound(), false));
        }

        evalBonusFound(gameContext.huntingStepResult(), gameContext, gameModel, level);

        evalPacKilled(gameContext.huntingStepResult(), gameModel, level, pac);
        if (gameContext.huntingStepResult().pacKilled()) {
            EntityCollisionResolver.fixPacPositionIfKilledInsidePortal(level, pac);
        }
        else {
            evalGhostsKilled(gameContext, level);
        }
    }

    private static void evalFoodFound(HuntingStepResult result, GameContext gameContext, GameLevel level, Pac pac) {

        if (!result.foodFound()) {
            pac.continueStarving();
            return;
        }

        pac.endStarving();

        final GameModel gameModel = gameContext.model();
        final Vector2i foodTile = result.foodFoundTile();

        level.worldMap().foodLayer().markFoodEatenAt(foodTile);
        if (result.energizerFound()) {
            gameModel.eatEnergizer(gameContext, level, foodTile);
        } else {
            gameModel.eatPellet(gameContext, level, foodTile);
        }

        if (gameContext.rules().isBonusAwarded(level)) {
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

    private static void evalGhostsKilled(GameContext gameContext, GameLevel level) {
        if (gameContext.huntingStepResult().detectedPacGhostCollision()) {
            // Frightened ghosts get killed when colliding with Pac
            gameContext.huntingStepResult().ghostsCollidingWithPac().stream()
                .filter(ghost -> ghost.state() == GhostState.FRIGHTENED)
                .forEach(gameContext.huntingStepResult().ghostsKilled()::add);
            // More than one ghost might have been killed in this step
            gameContext.huntingStepResult().ghostsKilled().forEach(ghost -> gameContext.model().onEatGhost(gameContext, level, ghost));
        }
    }

    // If collision happened while teleporting (horizontally), move collided actors into visible world
    public static void fixPacPositionIfKilledInsidePortal(GameLevel level, Pac pac) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        terrain.hPortalContainingTile(pac.computeTile()).ifPresent(hPortal -> {
            if (pac.moveDir() == Direction.LEFT) {
                pac.setX(hPortal.rightBorderEntryTile().x() * WorldMap.TS + WorldMap.HTS);
            } else if (pac.moveDir() == Direction.RIGHT) {
                pac.setX(hPortal.leftBorderEntryTile().x() * WorldMap.TS - WorldMap.HTS);
            }
            // Not sure if colliding ghosts should also be moved back to visible area
            Logger.info("Detected collision while teleporting, moved Pac-Man back into world");
        });
    }

}
