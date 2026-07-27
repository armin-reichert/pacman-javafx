/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.component.EntityComponent;
import de.amr.pacmanfx.core.model.component.Position;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.MovementSystem;
import de.amr.pacmanfx.core.model.systems.PacPowerSystem;
import de.amr.pacmanfx.core.model.systems.WorldMovementSystem;
import de.amr.pacmanfx.core.model.world.House;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.rules.ActorSpeedRules;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.Collection;

import static de.amr.basics.math.Direction.*;
import static de.amr.pacmanfx.core.Validations.differsAtMost;
import static de.amr.pacmanfx.core.Validations.stateIsOneOf;
import static java.util.Objects.requireNonNull;

public class GhostStateMachine implements EntityComponent {

    public static final GhostState DEFAULT_STATE = GhostState.LOCKED;

    private ObjectProperty<GhostState> state;

    @Override
    public void reset() {}
    
    public void update(GameContext gameContext, Ghost ghost) {
        final ActorSpeedRules speedRules = gameContext.model().rules().actorSpeedRules();
        final float speed = speedRules.ghostSpeed(gameContext, ghost);
        switch (state()) {
            case LOCKED         -> updateStateLocked(gameContext, ghost, speed);
            case LEAVING_HOUSE  -> updateStateLeavingHouse(gameContext, ghost, speed);
            case HUNTING_PAC    -> updateStateHuntingPac(gameContext, ghost, speed);
            case FRIGHTENED     -> updateStateFrightened(gameContext, ghost, speed);
            case EATEN          -> updateStateEaten();
            case RETURNING_HOME -> updateStateReturningToHouse(gameContext, ghost, speed);
            case ENTERING_HOUSE -> updateStateEnteringHouse(gameContext, ghost, speed);
        }
    }

    public ObjectProperty<GhostState> stateProperty() {
        if (state == null) {
            state = new SimpleObjectProperty<>(DEFAULT_STATE);
        }
        return state;
    }

    /**
     * The current state of ghost ghost.
     */
    public GhostState state() {
        return state != null ? stateProperty().get() : DEFAULT_STATE;
    }

    /**
     * @param states ghost states to be checked
     * @return <code>true</code> if ghost ghost is in any of the given states.
     * If no alternatives are given, an exception is thrown.
     * <code>false</code>
     */
    public boolean inAnyOfStates(Collection<GhostState> states) {
        return state != null && stateIsOneOf(state(), states);
    }

    /**
     * Changes the state of ghost ghost.
     *
     * @param newState the new state
     */
    public void setState(Ghost ghost, GhostState newState) {
        requireNonNull(newState);
        if (state() == newState) {
            Logger.debug("{} is already in state {}", ghost.name(), newState);
        }
        stateProperty().set(newState);

        // "onEntry" action:
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

    /**
     * In locked state, ghosts inside the house are bouncing up and down. They become blue when Pac-Man gets power
     * and start blinking when Pac-Man's power starts fading. After that, they return to their normal color.
     */
    private void updateStateLocked(GameContext gameContext, Ghost ghost, float speed) {
        final MovementSystem movementSystem = gameContext.systems().movementSystem;
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;

        final House house = ghost.house();
        final Position position = ghost.position();
        
        if (house.isVisitedBy(ghost)) {
            // locked inside house: jumping
            final float minY = (house.minTile().y() + 1) * WorldMap.TS + WorldMap.HTS;
            final float maxY = (house.maxTile().y() - 1) * WorldMap.TS - WorldMap.HTS;
            if (position.y <= minY) {
                worldMovementSystem.setMoveDir(ghost, DOWN);
                worldMovementSystem.setWishDir(ghost, DOWN);
            }
            else if (position.y >= maxY) {
                worldMovementSystem.setMoveDir(ghost, UP);
                worldMovementSystem.setWishDir(ghost, UP);
            }
            position.setY(Math.clamp(position.y, minY, maxY));
            worldMovementSystem.setSpeed(ghost, speed);
            movementSystem.moveAccelerated(ghost);
        }
        else {
            // locked outside of house: standing still
            worldMovementSystem.setSpeed(ghost, 0);
        }

        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();
        if (isThreatenedByPac(gameContext, ghost, pac)) {
            playFrightenedAnimation(gameContext, ghost, pac);
        } else {
            ghost.animations.select(CommonAnimationID.GHOST_NORMAL);
        }
    }

    // --- LEAVING_HOUSE ---

    /**
     * When a ghost leaves the house, he follows a specific route from his home/revival position to the house exit.
     * In the Arcade versions of Pac-Man and Ms.Pac-Man, the ghost first moves towards the vertical center of the house
     * and then raises up until he has passed the door on top of the house.
     * <p>
     * The ghost speed is slower than outside, but I do not know the exact value.
     */
    private void updateStateLeavingHouse(GameContext gameContext, Ghost ghost, float speed) {
        final MovementSystem movementSystem = gameContext.systems().movementSystem;
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();

        final Position position = ghost.position();
        final Vector2f houseEntryPosition = ghost.house().entryPosition();
        if (position.y <= houseEntryPosition.y()) {
            // outside at house entry
            position.setY(houseEntryPosition.y());
            worldMovementSystem.setMoveDir(ghost, LEFT);
            worldMovementSystem.setWishDir(ghost, LEFT);
            ghost.worldMovement().setNewTileEntered(false); // don't change direction until new tile is entered by moving
            setState(ghost, isThreatenedByPac(gameContext, ghost, pac) ? GhostState.FRIGHTENED : GhostState.HUNTING_PAC);
        }
        else {
            // still inside house
            final float centerX = position.x + WorldMap.HTS;
            final float houseCenterX = ghost.house().center().x();
            if (differsAtMost(0.5f * speed, centerX, houseCenterX)) {
                // align horizontally and raise
                position.setX(houseCenterX - WorldMap.HTS);
                worldMovementSystem.setMoveDir(ghost, UP);
                worldMovementSystem.setWishDir(ghost, UP);
            } else {
                // move sidewards until center axis is reached
                worldMovementSystem.setMoveDir(ghost, centerX < houseCenterX ? RIGHT : LEFT);
                worldMovementSystem.setWishDir(ghost, centerX < houseCenterX ? RIGHT : LEFT);
            }
            worldMovementSystem.setSpeed(ghost, speed);

            movementSystem.moveAccelerated(ghost);

            if (isThreatenedByPac(gameContext, ghost, pac)) {
                playFrightenedAnimation(gameContext, ghost, pac);
            } else {
                ghost.animations.select(CommonAnimationID.GHOST_NORMAL);
            }
        }
    }

    private boolean isThreatenedByPac(GameContext gameContext, Ghost ghost, Pac pac) {
        final PacPowerSystem pacPowerSystem = gameContext.systems().pacPowerSystem;
        final GameLevel level = gameContext.assertLevel();
        return pacPowerSystem.isPowerActive(pac) && !level.isInGhostKilledChain(ghost);
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
        ghost.hunt(gameContext, speed);
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
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
        final GameLevel level = gameContext.assertLevel();

        worldMovementSystem.setSpeed(ghost, speed);
        ghost.roam(gameContext);

        playFrightenedAnimation(gameContext, ghost, level.entities().pac());
    }

    private void playFrightenedAnimation(GameContext gameContext, Ghost ghost, Pac pac) {
        final GameLevel level = gameContext.assertLevel();
        final PacPowerSystem powerSystem = gameContext.systems().pacPowerSystem;
        if (powerSystem.isPowerStartingFading(level, pac)) {
            ghost.animations.select(CommonAnimationID.GHOST_FLASHING);
            ghost.animations.playSelected();
        }
        else if (!powerSystem.isPowerFading(level, pac)) {
            ghost.animations.select(CommonAnimationID.GHOST_FRIGHTENED);
            ghost.animations.playSelected();
        }
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

    /**
     * When an eaten ghost has arrived at the ghost house door, he falls down to the center of the house,
     * then moves up again (if the house center is his revival position), or moves sidewards towards his revival position.
     */
    private void updateStateEnteringHouse(GameContext gameContext, Ghost ghost, float speed) {
        final MovementSystem movementSystem = gameContext.systems().movementSystem;
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;

        final Position position = ghost.position();
        final Vector2f revivalPosition = WorldMap.halfTileRightOf(ghost.house().ghostRevivalTile(ghost.personality()));
        final Vector2f positionVec = position.asVector2f();
        if (positionVec.roughlyEquals(revivalPosition, 0.5f * speed, 0.5f * speed)) {
            position.set(revivalPosition.x(), revivalPosition.y());
            worldMovementSystem.setMoveDir(ghost, UP);
            worldMovementSystem.setWishDir(ghost, UP);
            setState(ghost, GhostState.LOCKED);
            return;
        }
        if (position.y < revivalPosition.y()) {
            worldMovementSystem.setMoveDir(ghost, DOWN);
            worldMovementSystem.setWishDir(ghost, DOWN);
        }
        else if (position.x > revivalPosition.x()) {
            worldMovementSystem.setMoveDir(ghost, LEFT);
            worldMovementSystem.setWishDir(ghost, LEFT);
        }
        else if (position.x < revivalPosition.x()) {
            worldMovementSystem.setMoveDir(ghost, RIGHT);
            worldMovementSystem.setWishDir(ghost, RIGHT);
        }
        worldMovementSystem.setSpeed(ghost, speed);

        movementSystem.moveAccelerated(ghost);
    }
}
