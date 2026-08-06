/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.system;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostStateComp;
import de.amr.pacmanfx.core.level.GameLevel;
import org.tinylog.Logger;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class GhostStateSystem {

    // Ghosts in these states are updated other ghost(s) are eaten and hunting is frozen
    public static final Set<GhostState> UPDATED_GHOST_STATES_WHILE_EATEN = Set.of(
        GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE);


    public void update(GameContext gameContext, GameLevel level, Ghost ghost) {
        requireNonNull(gameContext);
        requireNonNull(ghost);

        final GhostStateComp state = ghost.requireComponent(GhostStateComp.class);
        state.setThreatenedByPac(isGhostThreatenedByPac(level, ghost, level.entities().pac()));

        final float speed = gameContext.model().rules().actorSpeedRules().ghostSpeed(gameContext, ghost);
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

    public void changeState(GameContext gameContext, Ghost ghost, GhostState newState) {
        requireNonNull(gameContext);
        requireNonNull(ghost);
        requireNonNull(newState);

        if (ghost.state() == newState) {
            Logger.debug("{} is already in state {}", ghost.name(), newState);
            //TODO return from function?
        }
        
        ghost.requireComponent(GhostStateComp.class).setState(newState);

        //initAnimation(ghost, gameContext.systems().spriteAnim());
    }
    
    // --- LOCKED ---

    private void updateStateLocked(GameContext gameContext, Ghost ghost, float speed) {
        gameContext.systems().ghostHouseAccess().stayInHouse(gameContext, ghost, speed);
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
        final GameSystems sys = gameContext.systems();
        final GameLevel level = gameContext.assertLevel();
        // The specific hunting behavior is defined by the game variant. For example, in Ms. Pac-Man,
        // the red and pink ghosts are not chasing Pac-Man during the first scatter phase, but roam the maze randomly.
        gameContext.systems().ghostHuntingStrategy(ghost.personality()).hunt(level, ghost, speed, sys.ghostWorldMovementPolicy());
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
        gameContext.systems().roamingNavigator().roam(gameContext, ghost, speed);
    }

    // --- EATEN ---

    /**
     * After a ghost is eaten by Pac-Man, he is displayed for a short time as the number of points earned for eating him.
     * The value doubles for each ghost eaten using the power of the same energizer.
     */
    private void updateStateEaten() {
    }

    // --- LEAVING_HOUSE ---

    private void updateStateLeavingHouse(GameContext gameContext, Ghost ghost, float speed) {
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();

        boolean leftHouse = gameContext.systems().ghostHouseAccess().leaveHouse(gameContext, ghost, speed);
        boolean threatened = isGhostThreatenedByPac(level, ghost, pac);

        if (leftHouse) {
            changeState(gameContext, ghost, threatened ? GhostState.FRIGHTENED : GhostState.HUNTING_PAC);
        }
    }

    // --- RETURNING_TO_HOUSE ---

    /**
     * After the short time being displayed by his value, the eaten ghost is displayed by his eyes only and returns
     * to the ghost house to be revived. Hallelujah!
     */
    private void updateStateReturningToHouse(GameContext gameContext, Ghost ghost, float speed) {
        gameContext.systems().ghostHouseAccess().reachHouse(gameContext, ghost, speed);
    }

    // --- ENTERING_HOUSE ---

    private void updateStateEnteringHouse(GameContext gameContext, Ghost ghost, float speed) {
        gameContext.systems().ghostHouseAccess().enterHouse(gameContext, ghost, speed);
    }

    // helper

    private boolean isGhostThreatenedByPac(GameLevel level, Ghost ghost, Pac pac) {
        return pac.power().isActive() && !level.isInGhostKilledChain(ghost);
    }
}
