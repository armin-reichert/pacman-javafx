/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.ghost;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.GhostState;
import de.amr.pacmanfx.core.model.component.common.Position;
import de.amr.pacmanfx.core.model.systems.common.RandomWorldMovementSystem;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;
import de.amr.pacmanfx.core.rules.ActorSpeedRules;
import org.tinylog.Logger;

import static de.amr.basics.math.Direction.DOWN;
import static java.util.Objects.requireNonNull;

public class GhostStateMachine {

    public void update(GameContext gameContext, Ghost ghost) {
        final ActorSpeedRules speedRules = gameContext.model().rules().actorSpeedRules();
        final float speed = speedRules.ghostSpeed(gameContext, ghost);
        switch (ghost.state()) {
            case LOCKED         -> updateStateLocked(gameContext, ghost, speed);
            case LEAVING_HOUSE  -> updateStateLeavingHouse(gameContext, ghost, speed);
            case HUNTING_PAC    -> updateStateHuntingPac(gameContext, ghost, speed);
            case FRIGHTENED     -> updateStateFrightened(gameContext, ghost, speed);
            case EATEN          -> updateStateEaten();
            case RETURNING_HOME -> updateStateReturningToHouse(gameContext, ghost, speed);
            case ENTERING_HOUSE -> updateStateEnteringHouse(gameContext, ghost, speed);
        }
    }

    /**
     * Changes the state of the ghost.
     *
     * @param newState the new state
     */
    public void setState(Ghost ghost, GhostState newState) {
        requireNonNull(newState);
        if (ghost.state() == newState) {
            Logger.debug("{} is already in state {}", ghost.name(), newState);
        }
        ghost.setState(newState);

        // Execute "onEntry" action for the new state
        switch (newState) {
            case LOCKED, HUNTING_PAC -> ghost.animations.select(CommonAnimationID.GHOST_NORMAL);
            case ENTERING_HOUSE, RETURNING_HOME -> ghost.animations.select(CommonAnimationID.GHOST_EYES);
            case FRIGHTENED -> {
                ghost.animations.select(CommonAnimationID.GHOST_FRIGHTENED);
                ghost.animations.playSelected();
            }
            case EATEN -> {}
        }
    }

    // --- LOCKED ---

    private void updateStateLocked(GameContext gameContext, Ghost ghost, float speed) {
        final GhostHouseAccessSystem ghostHouseAccessSystem = gameContext.systems().ghostHouseAccessSystem;
        ghostHouseAccessSystem.stayInHouse(gameContext, ghost, speed);
    }

    // --- LEAVING_HOUSE ---

    private void updateStateLeavingHouse(GameContext gameContext, Ghost ghost, float speed) {
        final GhostHouseAccessSystem ghostHouseAccessSystem = gameContext.systems().ghostHouseAccessSystem;
        ghostHouseAccessSystem.leaveHouse(gameContext, ghost, speed);
    }

    // --- HUNTING_PAC ---

    /**
     * In each game level there are 8 alternating (scattering vs. chasing) hunting phases of different duration. The first
     * hunting phase is always a "scatter" phase where the ghosts retreat to their maze corners. After some time they
     * start chasing Pac-Man according to their character ("Shadow", "Speedy", "Bashful", "Pokey"). The last hunting phase
     * is an "infinite" chasing phase.
     * <p>
     */
    private void updateStateHuntingPac(GameContext gameContext, Ghost ghost, float speed) {
        // The specific hunting behavior is defined by the game variant. For example, in Ms. Pac-Man,
        // the red and pink ghosts are not chasing Pac-Man during the first scatter phase, but roam the maze randomly.
        final GhostHuntingStrategy strategy = gameContext.systems().ghostHuntingStrategy(ghost.personality());
        strategy.hunt(gameContext, ghost, speed);
    }

    // --- FRIGHTENED ---

    /**
     * <p>
     * A frightened ghost has a blue color and starts flashing blue/white shortly (how long exactly?) before Pac-Man loses
     * his power. Speed is about half of the normal speed. Reversing the move direction is not allowed in ghost state either.
     * </p><p>
     * Frightened ghosts choose a "random" direction when they enter a new tile. If the chosen direction
     * can be taken, it is stored and taken as soon as possible. Otherwise, the remaining directions are checked in
     * clockwise order.
     * </p>
     *
     * @see <a href="https://www.youtube.com/watch?v=eFP0_rkjwlY">YouTube: How Frightened Ghosts Decide Where to Go</a>
     */
    private void updateStateFrightened(GameContext gameContext, Ghost ghost, float speed) {
        final RandomWorldMovementSystem randomWorldMovementSystem = gameContext.systems().randomWorldMovementSystem;
        randomWorldMovementSystem.roam(gameContext, ghost, speed);
        ghost.playFrightenedAnimation(gameContext);
    }

    // --- EATEN ---

    /**
     * After a ghost is eaten by Pac-Man, he is displayed for a short time as the number of points earned for eating him.
     * The value doubles for each ghost eaten using the power of the same energizer.
     */
    private void updateStateEaten() {
    }

    // --- RETURNING_TO_HOUSE ---

    /**
     * After the short time being displayed by his value, the eaten ghost is displayed by his eyes only and returns
     * to the ghost house to be revived. Hallelujah!
     */
    private void updateStateReturningToHouse(GameContext gameContext, Ghost ghost, float speed) {
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;

        final Position position = ghost.position();
        final Vector2f houseEntry = ghost.house().entryPosition();
        //TODO
        final Vector2f positionVec =  position.asVector2f();
        if (positionVec.roughlyEquals(houseEntry, speed, 0)) {
            position.set(houseEntry.x(), houseEntry.y());
            worldMovementSystem.setMoveDir(ghost, DOWN);
            worldMovementSystem.setWishDir(ghost, DOWN);
            setState(ghost, GhostState.ENTERING_HOUSE);
        } else {
            //TODO use system method
            ghost.worldMovement().setTargetTile(ghost.house().leftDoorTile());
            worldMovementSystem.setSpeed(ghost, speed);
            worldMovementSystem.navigateTowardsTarget(ghost, gameContext);
            worldMovementSystem.tryMovingOrTeleporting(ghost, gameContext);
        }
    }

    // --- ENTERING_HOUSE ---

    private void updateStateEnteringHouse(GameContext gameContext, Ghost ghost, float speed) {
        final GhostHouseAccessSystem ghostHouseAccessSystem = gameContext.systems().ghostHouseAccessSystem;
        ghostHouseAccessSystem.enterHouse(gameContext, ghost, speed);
    }
}
