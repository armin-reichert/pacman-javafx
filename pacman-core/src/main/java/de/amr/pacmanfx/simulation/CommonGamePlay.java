/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.simulation;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.event.PacEatsFoodEvent;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.GateKeeper;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMap;
import org.tinylog.Logger;

public abstract class CommonGamePlay implements GamePlay {

    @Override
    public HuntingStepResult hunt(GameContext context, GameEventManager eventManager, GameLevel level) {
        final GameModel model = level.gameModel();
        final Pac pac = level.entities().pac();
        final GateKeeper gateKeeper = model.gateKeeper();
        final boolean doubleChecked = model.rules().collisionDoubleCheckedProperty().get();

        level.heartbeat().triggerPulse();
        level.huntingTimer().update(model.rules(), level.number());

        if (gateKeeper != null) {
            gateKeeper.unlockGhostIfPossible(level);
        }

        updatePacPowerMode(eventManager, level, pac);

        final EntityCollisionDetector collisionDetector = new EntityCollisionDetector();
        // If double-check active, do an additional collision check before Pac has moved
        level.entities().forEach(entity -> {
            if (entity != pac) {
                entity.update(context, level);
            }
        });
        if (doubleChecked) {
            collisionDetector.detectCollisions(level);
        }
        pac.update(context, level);

        final HuntingStepResult result = collisionDetector.detectCollisions(level);
        evaluateCollisions(result, context.eventManager(), level);

        return result;
    }

    private void evaluateCollisions(HuntingStepResult huntingStepResult, GameEventManager eventManager, GameLevel level) {
        final Pac pac = level.entities().pac();

        evalFoodFound(huntingStepResult, eventManager, level);
        if (huntingStepResult.foodFound()) {
            eventManager.publishGameEvent(new PacEatsFoodEvent(pac, huntingStepResult.energizerFound(), false));
        }

        evalBonusFound(huntingStepResult, eventManager, level);

        evalPacKilled(huntingStepResult, eventManager, level);
        if (huntingStepResult.pacKilled()) {
            fixPacPositionIfKilledInsidePortal(level);
        }
        else {
            evalGhostsKilled(huntingStepResult, eventManager, level);
        }
    }

    private void evalFoodFound(
        HuntingStepResult huntingStepResult,
        GameEventManager eventManager,
        GameLevel level
    ) {
        final Pac pac = level.entities().pac();
        final GameModel model = level.gameModel();
        final Vector2i foodTile = huntingStepResult.foodFoundTile();

        if (!huntingStepResult.foodFound()) {
            pac.continueStarving();
            return;
        }

        pac.endStarving();

        level.worldMap().foodLayer().markFoodEatenAt(foodTile);
        if (huntingStepResult.energizerFound()) {
            onEatEnergizer(eventManager, level, foodTile);
        } else {
            onEatPellet(eventManager, level, foodTile);
        }

        if (model.rules().isBonusAwarded(level)) {
            activateNextBonus(eventManager, level);
        }
    }

    private void evalBonusFound(
        HuntingStepResult huntingStepResult,
        GameEventManager eventManager,
        GameLevel level
    ) {
        if (huntingStepResult.foundEdibleBonus()) {
            onEatBonus(eventManager, level, huntingStepResult.edibleBonus());
        }
    }

    private void evalPacKilled(
        HuntingStepResult huntingStepResult,
        GameEventManager eventManager,
        GameLevel level
    ) {
        if (level.isDemoLevel() && isPacSafeInDemoLevel(level) || level.entities().pac().isImmune()) {
            return;
        }
        huntingStepResult.ghostsCollidingWithPac().stream()
            .filter(ghost -> ghost.state() == GhostState.HUNTING_PAC)
            .findFirst().ifPresent(_ -> huntingStepResult.setPacKilled(true));
    }

    private void evalGhostsKilled(
        HuntingStepResult huntingStepResult,
        GameEventManager eventManager,
        GameLevel level
    ) {
        if (huntingStepResult.detectedPacGhostCollision()) {
            // Frightened ghosts get killed when colliding with Pac
            huntingStepResult.ghostsCollidingWithPac().stream()
                .filter(ghost -> ghost.state() == GhostState.FRIGHTENED)
                .forEach(huntingStepResult.ghostsKilled()::add);
            // More than one ghost might have been killed in this step
            huntingStepResult.ghostsKilled().forEach(ghost -> onEatGhost(eventManager, level, ghost));
        }
    }

    // If collision happened while teleporting (horizontally), move collided actors into visible world
    private void fixPacPositionIfKilledInsidePortal(GameLevel level) {
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
