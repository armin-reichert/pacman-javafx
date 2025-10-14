/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import org.tinylog.Logger;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.Direction.LEFT;
import static java.util.Objects.requireNonNull;

/**
 * From the Pac-Man dossier:
 * <p>
 * Commonly referred to as the ghost house or monster pen, this cordoned-off area in the center of the maze is the
 * domain of the four ghosts and off-limits to Pac-Man.
 * </p>
 * <p>
 * Whenever a level is completed or a life is lost, the ghosts are returned to their starting positions in and around
 * the ghost house before play continues—Blinky is always located just above and outside, while the other three are
 * placed inside: Inky on the left, Pinky in the middle, and Clyde on the right.
 * The pink door on top is used by the ghosts to enter or exit the house. Once a ghost leaves, however, it cannot
 * reenter unless it is first captured by Pac-Man—then the disembodied eyes can return home to be revived.
 * Since Blinky is already on the outside after a level is completed or a life is lost, the only time he can get
 * inside the ghost house is after Pac-Man captures him, and he immediately turns around to leave once revived.
 * That's about all there is to know about Blinky's behavior in terms of the ghost house, but determining when the
 * other three ghosts leave home is an involved process based on several variables and conditions.
 * The rest of this section will deal with them exclusively. Accordingly, any mention of "the ghosts" below refers t
 * o Pinky, Inky, and Clyde, but not Blinky.
 * </p>
 * <p>
 * The first control used to evaluate when the ghosts leave home is a personal counter each ghost retains for
 * tracking the number of dots Pac-Man eats. Each ghost's "dot counter" is reset to zero when a level begins and can
 * only be active when inside the ghost house, but only one ghost's counter can be active at any given time regardless
 * of how many ghosts are inside. The order of preference for choosing which ghost's counter to activate is:
 * Pinky, then Inky, and then Clyde. For every dot Pac-Man eats, the preferred ghost in the house (if any) gets its
 * dot counter increased by one. Each ghost also has a "dot limit" associated with his counter, per level.
 * If the preferred ghost reaches or exceeds his dot limit, it immediately exits the house and its dot counter is
 * deactivated (but not reset). The most-preferred ghost still waiting inside the house (if any) activates its timer
 * at this point and begins counting dots.
 * </p>
 * <p>
 * Pinky's dot limit is always set to zero, causing him to leave home immediately when every level begins.
 * For the first level, Inky has a limit of 30 dots, and Clyde has a limit of 60. This results in Pinky exiting
 * immediately which, in turn, activates Inky's dot counter. His counter must then reach or exceed 30 dots before
 * he can leave the house. Once Inky starts to leave, Clyde's counter (which is still at zero) is activated and
 * starts counting dots. When his counter reaches or exceeds 60, he may exit. On the second level, Inky's dot limit
 * is changed from 30 to zero, while Clyde's is changed from 60 to 50. Inky will exit the house as soon as the level
 * begins from now on. Starting at level three, all the ghosts have a dot limit of zero for the remainder of the game
 * and will leave the ghost house immediately at the start of every level.
 * </p>
 * <p>
 * Whenever a life is lost, the system disables (but does not reset) the ghosts' individual dot counters and uses
 * a global dot counter instead. This counter is enabled and reset to zero after a life is lost, counting the number
 * of dots eaten from that point forward. The three ghosts inside the house must wait for this special counter to
 * tell them when to leave. Pinky is released when the counter value is equal to 7 and Inky is released when it
 * equals 17. The only way to deactivate the counter is for Clyde to be inside the house when the counter equals 32;
 * otherwise, it will keep counting dots even after the ghost house is empty. If Clyde is present at the
 * appropriate time, the global counter is reset to zero and deactivated, and the ghosts' personal dot limits are
 * re-enabled and used as before for determining when to leave the house (including Clyde who is still in the house
 * at this time).
 * </p>
 * <p>
 * If dot counters were the only control, Pac-Man could simply stop eating dots early on and keep the ghosts
 * trapped inside the house forever. Consequently, a separate timer control was implemented to handle this case by
 * tracking the amount of time elapsed since Pac-Man has last eaten a dot. This timer is always running but gets
 * reset to zero each time a dot is eaten. Anytime Pac-Man avoids eating dots long enough for the timer to reach
 * its limit, the most-preferred ghost waiting in the ghost house (if any) is forced to leave immediately, and
 * the timer is reset to zero. The same order of preference described above is used by this control as well.
 * The game begins with an initial timer limit of four seconds, but lowers to it to three seconds starting with
 * level five.
 * </p>
 * <p>
 * The more astute reader may have already noticed there is subtle flaw in this system resulting in a way to
 * keep Pinky, Inky, and Clyde inside the ghost house for a very long time after eating them.
 * The trick involves having to sacrifice a life in order to reset and enable the global dot counter,
 * and then making sure Clyde exits the house before that counter is equal to 32.
 * This is accomplished by avoiding eating dots and waiting for the timer limit to force Clyde out.
 * Once Clyde is moving for the exit, start eating dots again until at least 32 dots have been consumed since
 * the life was lost. Now head for an energizer and gobble up some ghosts. Blinky will leave the house immediately
 * as usual, but the other three ghosts will remain "stuck" inside as long as Pac-Man continues eating dots with
 * sufficient frequency as not to trigger the control timer. Why does this happen?
 * The key lies in how the global dot counter works—it cannot be deactivated if Clyde is outside the house when
 * the counter has a value of 32. By letting the timer force Clyde out before 32 dots are eaten, the global dot
 * counter will keep counting dots instead of deactivating when it reaches 32. Now when the ghosts are eaten by
 * Pac-Man and return home, they will still be using the global dot counter to determine when to leave.
 * As previously described, however, this counter's logic only checks for three values: 7, 17, and 32, and
 * once those numbers are exceeded, the counter has no way to release the ghosts associated with them.
 * The only control left to release the ghosts is the timer which can be easily avoided by eating a dot every
 * so often to reset it.
 * </p>
 * </pre>
 *
 * @author Armin Reichert
 */
public class GateKeeper {
    private static final byte NO_LIMIT = -1;
    private static final byte[] GLOBAL_LIMITS = new byte[] {NO_LIMIT, 7, 17, NO_LIMIT};

    private final Game game;
    private House house;
    private BiConsumer<GameLevel, Ghost> ghostReleasedAction =
        (gameLevel, ghost) -> Logger.info("Game Level #{}: {} released", gameLevel.number(), ghost.name());
    
    private final byte[] limitsByGhost = new byte[4];
    private int          pacStarvingLimit;
    private final int[]  countersByGhost = new int[4];
    private int          globalCounter;
    private boolean      globalCounterEnabled;

    public GateKeeper(Game game) {
        this.game = requireNonNull(game);
    }
    
    public void setHouse(House house) {
        this.house = house;
    }

    public void setOnGhostReleased(BiConsumer<GameLevel, Ghost> ghostReleasedAction) {
        this.ghostReleasedAction = ghostReleasedAction;
    }

    public void reset() {
        Arrays.fill(limitsByGhost, (byte)0);
        pacStarvingLimit = 0;
        Arrays.fill(countersByGhost, 0);
        globalCounter = 0;
        globalCounterEnabled = false;
    }

    public void setLevelNumber(int levelNumber) {
        Arrays.fill(limitsByGhost, (byte) 0);
        if (levelNumber == 1) {
            limitsByGhost[CYAN_GHOST_BASHFUL]   = 30;
            limitsByGhost[ORANGE_GHOST_POKEY] = 60;
        } else if (levelNumber == 2) {
            limitsByGhost[ORANGE_GHOST_POKEY] = 50;
        }
        pacStarvingLimit = levelNumber < 5 ? 240 : 180; // 4 sec : 3 sec
        Arrays.fill(countersByGhost, 0);
        globalCounter = 0;
        globalCounterEnabled = false;
    }

    /**
     * @param gameLevel the game level
     * @param prisoner the ghost to possibly get released
     * @return description why ghost has been released or {@code null} if ghost is not released
     */
    public String checkReleaseOf(GameLevel gameLevel, Ghost prisoner) {
        byte personality = prisoner.personality();
        if (personality == RED_GHOST_SHADOW) {
            return "Red ghost gets released unconditionally";
        }
        // check individual dot counter first (if enabled)
        if (!globalCounterEnabled && countersByGhost[personality] >= limitsByGhost[personality]) {
            return String.format("%s's individual dot counter reached limit (%d)", prisoner.name(), limitsByGhost[personality]);
        }
        // check global dot counter
        if (globalCounterEnabled && GLOBAL_LIMITS[personality] != NO_LIMIT && globalCounter >= GLOBAL_LIMITS[personality]) {
            return String.format("Global dot counter reached limit (%d)", GLOBAL_LIMITS[personality]);
        }
        // check Pac-Man starving ticks
        if (gameLevel.pac().starvingTime() >= pacStarvingLimit) {
            gameLevel.pac().endStarving();
            return String.format("%s reached starving limit (%d ticks)", gameLevel.pac().name(), pacStarvingLimit);
        }
        return null;
    }

    public void resetCounterAndSetEnabled(boolean enabled) {
        globalCounter = 0;
        globalCounterEnabled = enabled;
        Logger.info("Global dot counter set to 0 and {}", enabled ? "enabled" : "disabled");
    }

    public void registerFoodEaten(GameLevel gameLevel) {
        if (globalCounterEnabled) {
            if (gameLevel.ghost(ORANGE_GHOST_POKEY).inAnyOfStates(GhostState.LOCKED) && globalCounter == 32) {
                Logger.info("{} inside house when global counter reached 32", gameLevel.ghost(ORANGE_GHOST_POKEY).name());
                resetCounterAndSetEnabled(false);
            } else {
                globalCounter++;
                Logger.trace("Global dot counter = {}", globalCounter);
            }
        } else {
            requireNonNull(house);
            gameLevel.ghosts(GhostState.LOCKED).filter(house::isVisitedBy).findFirst().ifPresent(ghost -> {
                countersByGhost[ghost.personality()]++;
                Logger.trace("{} dot counter = {}", ghost.name(), countersByGhost[ghost.personality()]);
            });
        }
    }

    public void unlockGhosts(GameLevel gameLevel) {
        requireNonNull(house);
        Ghost blinky = gameLevel.ghost(RED_GHOST_SHADOW);
        if (blinky.inAnyOfStates(GhostState.LOCKED)) {
            if (house.isVisitedBy(blinky)) {
                blinky.setMoveDir(Direction.UP);
                blinky.setWishDir(Direction.UP);
                blinky.setState(GhostState.LEAVING_HOUSE);
            } else {
                blinky.setMoveDir(LEFT);
                blinky.setWishDir(LEFT);
                blinky.setState(GhostState.HUNTING_PAC);
            }
        }
        Stream.of(PINK_GHOST_SPEEDY, CYAN_GHOST_BASHFUL, ORANGE_GHOST_POKEY)
            .map(gameLevel::ghost)
            .filter(ghost -> ghost.inAnyOfStates(GhostState.LOCKED))
            .findFirst().ifPresent(prisoner -> {
                String reason = checkReleaseOf(gameLevel, prisoner);
                if (reason != null) {
                    game.simulationStepResults().releasedGhost = prisoner;
                    game.simulationStepResults().ghostReleaseInfo = reason;
                    prisoner.setMoveDir(Direction.UP);
                    prisoner.setWishDir(Direction.UP);
                    prisoner.setState(GhostState.LEAVING_HOUSE);
                    ghostReleasedAction.accept(gameLevel, prisoner);
                }
        });
    }
}