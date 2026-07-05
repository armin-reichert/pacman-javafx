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

import static java.util.Objects.requireNonNull;

public final class EntityCollisionResolver {

    private final GameContext context;

    public EntityCollisionResolver(GameContext context) {
        this.context = requireNonNull(context);
    }

    public void evaluateCollisions(GameLevel level) {
        final Pac pac = level.entities().pac();

        evalFoodFound(level);
        if (context.huntingStepResult().foodFound()) {
            context.eventManager().publishGameEvent(
                new PacEatsFoodEvent(pac, context.huntingStepResult().energizerFound(), false));
        }

        evalBonusFound(level);

        evalPacKilled(level);
        if (context.huntingStepResult().pacKilled()) {
            fixPacPositionIfKilledInsidePortal(level);
        }
        else {
            evalGhostsKilled(level);
        }
    }

    private void evalFoodFound(GameLevel level) {
        final Pac pac = level.entities().pac();
        final GameModel model = context.model();
        final Vector2i foodTile = context.huntingStepResult().foodFoundTile();

        if (!context.huntingStepResult().foodFound()) {
            pac.continueStarving();
            return;
        }

        pac.endStarving();

        level.worldMap().foodLayer().markFoodEatenAt(foodTile);
        if (context.huntingStepResult().energizerFound()) {
            context.gamePlay().onEatEnergizer(context.eventManager(), model, level, foodTile);
        } else {
            context.gamePlay().onEatPellet(context.eventManager(), model, level, foodTile);
        }

        if (model.rules().isBonusAwarded(level)) {
            context.gamePlay().activateNextBonus(context.eventManager(), model, level);
        }
    }

    private void evalBonusFound(GameLevel level) {
        if (context.huntingStepResult().foundEdibleBonus()) {
            context.gamePlay().onEatBonus(context.eventManager(), level.gameModel(), level, context.huntingStepResult().edibleBonus());
        }
    }

    private void evalPacKilled(GameLevel level) {
        if (level.isDemoLevel() && context.gamePlay().isPacSafeInDemoLevel(level) || level.entities().pac().isImmune()) {
            return;
        }
        context.huntingStepResult().ghostsCollidingWithPac().stream()
            .filter(ghost -> ghost.state() == GhostState.HUNTING_PAC)
            .findFirst().ifPresent(_ -> context.huntingStepResult().setPacKilled(true));
    }

    private void evalGhostsKilled(GameLevel level) {
        if (context.huntingStepResult().detectedPacGhostCollision()) {
            // Frightened ghosts get killed when colliding with Pac
            context.huntingStepResult().ghostsCollidingWithPac().stream()
                .filter(ghost -> ghost.state() == GhostState.FRIGHTENED)
                .forEach(context.huntingStepResult().ghostsKilled()::add);
            // More than one ghost might have been killed in this step
            context.huntingStepResult().ghostsKilled().forEach(ghost ->
                context.gamePlay().onEatGhost(context.eventManager(), context.model(), level, ghost));
        }
    }

    // If collision happened while teleporting (horizontally), move collided actors into visible world
    public void fixPacPositionIfKilledInsidePortal(GameLevel level) {
        final Pac pac = level.entities().pac();
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
