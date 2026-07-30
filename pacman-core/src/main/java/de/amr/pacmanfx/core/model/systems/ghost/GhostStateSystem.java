/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.ghost;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.ActorAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.component.ghost.GhostState;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.ghost.GhostStateComponent;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
import de.amr.pacmanfx.core.model.systems.pac.PacPowerSystem;
import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class GhostStateSystem {

    public void update(GameContext gameContext, Ghost ghost) {
        requireNonNull(gameContext);
        requireNonNull(ghost);

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
        
        ghost.requireComponent(GhostStateComponent.class).setState(newState);

        initAnimation(ghost, gameContext.systems().spriteAnim());
    }
    
    // --- LOCKED ---

    private void updateStateLocked(GameContext gameContext, Ghost ghost, float speed) {
        final GameSystems sys = gameContext.systems();

        sys.ghostHouseAccess().stayInHouse(gameContext, ghost, speed);

        if (isThreatenedByPac(gameContext, ghost)) {
            playFrightenedAnimation(gameContext, ghost);
        } else {
            sys.spriteAnim().select(ghost, ActorAnimationID.GHOST_NORMAL);
        }
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
        playFrightenedAnimation(gameContext, ghost);
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
        final GameSystems sys = gameContext.systems();

        boolean leftHouse = sys.ghostHouseAccess().leaveHouse(gameContext, ghost, speed);
        boolean threatened =  isThreatenedByPac(gameContext, ghost);

        if (leftHouse) {
            changeState(gameContext, ghost,
                threatened ? GhostState.FRIGHTENED : GhostState.HUNTING_PAC);
        }
        else {
            if (threatened) {
                playFrightenedAnimation(gameContext, ghost);
            } else {
                sys.spriteAnim().select(ghost, ActorAnimationID.GHOST_NORMAL);
            }
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

    private boolean isThreatenedByPac(GameContext gameContext, Ghost ghost) {
        final PacPowerSystem pacPowerSystem = gameContext.systems().pacPower();
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();
        return pacPowerSystem.isPowerActive(pac) && !level.isInGhostKilledChain(ghost);
    }

    //TODO move into animation system class

    private void initAnimation(Ghost ghost, SpriteAnimSystem animSystem) {
        switch (ghost.state()) {
            case LOCKED, HUNTING_PAC -> {
                animSystem.select(ghost, ActorAnimationID.GHOST_NORMAL);
                animSystem.playSelected(ghost);
            }
            case ENTERING_HOUSE, RETURNING_HOME -> {
                animSystem.select(ghost, ActorAnimationID.GHOST_EYES);
                animSystem.playSelected(ghost);
            }
            case FRIGHTENED -> {
                animSystem.select(ghost, ActorAnimationID.GHOST_FRIGHTENED);
                animSystem.playSelected(ghost);
            }
            case EATEN -> {}
        }
    }

    private void playFrightenedAnimation(GameContext gameContext, Ghost ghost) {
        final GameSystems sys = gameContext.systems();
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();

        if (sys.pacPower().isPowerStartingFading(level, pac)) {
            sys.spriteAnim().select(ghost, ActorAnimationID.GHOST_FLASHING);
            sys.spriteAnim().playSelected(ghost);
        }
        else if (!sys.pacPower().isPowerFading(level, pac)) {
            sys.spriteAnim().select(ghost, ActorAnimationID.GHOST_FRIGHTENED);
            sys.spriteAnim().playSelected(ghost);
        }
    }

}
