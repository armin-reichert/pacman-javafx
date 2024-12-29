/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileEncoding;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.*;

/**
 * There are 4 ghosts with different "personalities".
 *
 * @author Armin Reichert
 */
public class Ghost extends Creature implements AnimatedActor2D {

    public static Ghost blinky() {
        var ghost = new Ghost(GameModel.RED_GHOST);
        ghost.setName("Blinky");
        return ghost;
    }

    public static Ghost pinky() {
        var ghost = new Ghost(GameModel.PINK_GHOST);
        ghost.setName("Pinky");
        return ghost;
    }

    public static Ghost inky() {
        var ghost = new Ghost(GameModel.CYAN_GHOST);
        ghost.setName("Inky");
        return ghost;
    }

    public static Ghost clyde() {
        var ghost = new Ghost(GameModel.ORANGE_GHOST);
        ghost.setName("Clyde");
        return ghost;
    }

    public static Ghost sue() {
        var ghost = new Ghost(GameModel.ORANGE_GHOST);
        ghost.setName("Sue");
        return ghost;
    }

    private final byte id;
    private String name;
    private GhostState state;
    private Vector2f revivalPosition;
    private ActorAnimations animations;
    private Consumer<Ghost> huntingBehaviour = ghost -> {};
    private List<Vector2i> specialTerrainTiles = List.of();

    /**
     * Constructs a ghost without associated world like the ones in the cut scenes.
     * @param id ghost ID
     */
    private Ghost(byte id) {
        this.id = assertLegalGhostID(id);
        corneringSpeedUp = -1.25f;
    }

    public void setHuntingBehaviour(Consumer<Ghost> huntingBehaviour) {
        this.huntingBehaviour = assertNotNull(huntingBehaviour);
    }

    @Override
    public String toString() {
        return "Ghost{" +
            "name='" + name + '\'' +
            ", state=" + state +
            ". tile=" + tile() +
            ", newTileEntered=" + newTileEntered +
            ", gotReverseCommand=" + gotReverseCommand +
            ", posX=" + posX +
            ", posY=" + posY +
            '}';
    }

    public byte id() {
        return id;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public Actor2D actor() {
        return this;
    }

    public void setAnimations(ActorAnimations animations) {
        this.animations = animations;
    }

    @Override
    public Optional<ActorAnimations> animations() {
        return Optional.ofNullable(animations);
    }

    public boolean insideHouse() {
        return world.isPartOfHouse(tile());
    }

    public void setRevivalPosition(Vector2f position) {
        assertNotNull(position);
        revivalPosition = position;
    }

    public void setSpecialTerrainTiles(List<Vector2i> tiles) {
        specialTerrainTiles = new ArrayList<>(tiles);
    }

    public List<Vector2i> specialTerrainTiles() {
        return specialTerrainTiles;
    }

    /**
     * Lets the ghost randomly roam through the world.
     *
     * @param speed the speed (in pixels/tick)
     */
    public void roam(float speed) {
        Vector2i currentTile = tile();
        if (!world.isPortalAt(currentTile) && (isNewTileEntered() || !moveInfo.moved)) {
            setWishDir(determineNextDirection(currentTile));
        }
        setSpeed(speed);
        tryMoving();
    }

    // try a random direction towards an accessible tile, do not turn back unless there is no other way
    private Direction determineNextDirection(Vector2i currentTile) {
        Direction dir = pseudoRandomDirection();
        int turns = 0;
        while (dir == moveDir.opposite() || !canAccessTile(currentTile.plus(dir.vector()))) {
            dir = dir.nextClockwise();
            if (++turns > 4) {
                return moveDir.opposite();  // avoid endless loop
            }
        }
        return dir;
    }

    private Direction pseudoRandomDirection() {
        int rnd = Globals.randomInt(0, 1000);
        if (rnd < 163)             return UP;
        if (rnd < 163 + 252)       return RIGHT;
        if (rnd < 163 + 252 + 285) return DOWN;
        return LEFT;
    }

    @Override
    public boolean canAccessTile(Vector2i tile) {
        assertTileNotNull(tile);
        assertNotNull(world);

        // hunting ghosts cannot move up at certain tiles in Pac-Man game
        if (state == HUNTING_PAC) {
            var currentTile = tile();
            if (specialTerrainTiles.contains(tile)
                    && world.map().terrain().get(tile) == TileEncoding.ONE_WAY_DOWN
                    && currentTile.equals(tile.plus(DOWN.vector()))) {
                Logger.info("Hunting {} cannot move up to special tile {}", name, tile);
                return false;
            }
        }
        if (world.isDoorAt(tile)) {
            return inState(ENTERING_HOUSE, LEAVING_HOUSE);
        }
        if (world.isInsideWorld(tile)) {
            return !world.isBlockedTile(tile);
        }
        return world.isPortalAt(tile);
    }

    @Override
    public boolean canReverse() {
        return newTileEntered && inState(HUNTING_PAC, FRIGHTENED);
    }

    // Here begins the state machine part

    /**
     * The current state of this ghost.
     */
    public GhostState state() {
        return state;
    }

    /**
     * @param stateAlternatives ghost states to be checked
     * @return <code>true</code> if this ghost is in any of the given states.
     * If no alternatives are given, an exception is thrown.
     * <code>false</code>
     */
    public boolean inState(GhostState... stateAlternatives) {
        if (stateAlternatives.length == 0) {
            throw new IllegalArgumentException("No states to check for ghost");
        }
        return oneOf(state, stateAlternatives);
    }

    /**
     * Changes the state of this ghost.
     *
     * @param state the new state
     */
    public void setState(GhostState state) {
        assertNotNull(state);
        if (this.state == state) {
            Logger.trace("{} is already in state {}", name, state);
        }
        this.state = state;
        // onEntry action:
        switch (state) {
            case LOCKED, HUNTING_PAC -> selectAnimation(ActorAnimations.ANIM_GHOST_NORMAL);
            case ENTERING_HOUSE, RETURNING_HOME -> selectAnimation(ActorAnimations.ANIM_GHOST_EYES);
            case FRIGHTENED -> selectAnimation(ActorAnimations.ANIM_GHOST_FRIGHTENED);
            case EATEN -> {}
        }
    }

    /**
     * Executes a single simulation step for this ghost in the current game level.
     *
     * @param game game variant
     */
    public void update(GameModel game) {
        assertNotNull(game);
        switch (state) {
            case LOCKED             -> updateStateLocked(game);
            case LEAVING_HOUSE      -> updateStateLeavingHouse(game);
            case HUNTING_PAC        -> updateStateHuntingPac(game);
            case FRIGHTENED         -> updateStateFrightened(game);
            case EATEN              -> updateStateEaten();
            case RETURNING_HOME     -> updateStateReturningToHouse(game);
            case ENTERING_HOUSE     -> updateStateEnteringHouse(game);
        }
    }

    public void eaten(int index) {
        setState(EATEN);
        selectAnimation(ActorAnimations.ANIM_GHOST_NUMBER, index);
    }

    // --- LOCKED ---

    /**
     * In locked state, ghosts inside the house are bouncing up and down. They become blue when Pac-Man gets power
     * and start blinking when Pac-Man's power starts fading. After that, they return to their normal color.
     */
    private void updateStateLocked(GameModel game) {
        GameLevel level = game.level().orElseThrow();
        if (insideHouse()) {
            float minY = world.houseCeilingY() + HTS, maxY = world().houseFloorY() - HTS;
            setSpeed(game.ghostSpeedInsideHouse(this));
            move();
            if (posY <= minY) {
                setMoveAndWishDir(DOWN);
            } else if (posY >= maxY) {
                setMoveAndWishDir(UP);
            }
            posY = clamp(posY, minY, maxY);
        } else {
            setSpeed(0);
        }
        if (level.powerTimer().isRunning() && !level.victims().contains(this)) {
            updateFrightenedAnimation(game);
        } else {
            selectAnimation(ActorAnimations.ANIM_GHOST_NORMAL);
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
    private void updateStateLeavingHouse(GameModel game) {
        GameLevel level = game.level().orElseThrow();
        float speedInsideHouse = game.ghostSpeedInsideHouse(this);
        Vector2f houseEntryPosition = world.houseEntryPosition();
        if (posY() <= houseEntryPosition.y()) {
            // has raised and is outside house
            setPosition(houseEntryPosition);
            setMoveAndWishDir(LEFT);
            newTileEntered = false; // force moving left until new tile is entered
            if (level.powerTimer().isRunning() && !level.victims().contains(this)) {
                setState(FRIGHTENED);
            } else {
                setState(HUNTING_PAC);
            }
            return;
        }
        // move inside house
        float centerX = posX + HTS;
        float houseCenterX = world.houseCenter().x();
        if (differsAtMost(0.5f * speedInsideHouse, centerX, houseCenterX)) {
            // align horizontally and raise
            setPosX(houseCenterX - HTS);
            setMoveAndWishDir(UP);
        } else {
            // move sidewards until center axis is reached
            setMoveAndWishDir(centerX < houseCenterX ? RIGHT : LEFT);
        }
        setSpeed(speedInsideHouse);
        move();
        if (level.powerTimer().isRunning() && !level.victims().contains(this)) {
            updateFrightenedAnimation(game);
        } else {
            selectAnimation(ActorAnimations.ANIM_GHOST_NORMAL);
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
    private void updateStateHuntingPac(GameModel game) {
        // The specific hunting behaviour is defined by the game variant. For example, in Ms. Pac-Man,
        // the red and pink ghosts are not chasing Pac-Man during the first scatter phase, but roam the maze randomly.
        huntingBehaviour.accept(this);
    }

    // --- FRIGHTENED ---

    /**
     * <p>
     * A frightened ghost has a blue color and starts flashing blue/white shortly (how long exactly?) before Pac-Man loses
     * his power. Speed is about half of the normal speed. Reversing the move direction is not allowed in this state either.
     * </p><p>
     * Frightened ghosts choose a "random" direction when they enter a new tile. If the chosen direction
     * can be taken, it is stored and taken as soon as possible. Otherwise, the remaining directions are checked in
     * clockwise order.
     * </p>
     *
     * @see <a href="https://www.youtube.com/watch?v=eFP0_rkjwlY">YouTube: How Frightened Ghosts Decide Where to Go</a>
     */
    private void updateStateFrightened(GameModel game) {
        float speed = world.isTunnel(tile()) ? game.ghostTunnelSpeed(this) : game.ghostFrightenedSpeed(this);
        roam(speed);
        updateFrightenedAnimation(game);
    }

    private void updateFrightenedAnimation(GameModel game) {
        if (game.isPowerFadingStarting()) {
            selectAnimation(ActorAnimations.ANIM_GHOST_FLASHING);
        } else if (!game.isPowerFading()) {
            selectAnimation(ActorAnimations.ANIM_GHOST_FRIGHTENED);
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
    private void updateStateReturningToHouse(GameModel game) {
        float speedReturningToHouse = game.ghostSpeedReturningToHouse(this);
        Vector2f houseEntry = world.houseEntryPosition();
        if (position().roughlyEquals(houseEntry, 0.5f * speedReturningToHouse, 0)) {
            setPosition(houseEntry);
            setMoveAndWishDir(DOWN);
            setState(ENTERING_HOUSE);
        } else {
            setSpeed(speedReturningToHouse);
            setTargetTile(world.houseLeftDoorTile());
            navigateTowardsTarget();
            tryMoving();
        }
    }

    // --- ENTERING_HOUSE ---

    /**
     * When an eaten ghost has arrived at the ghost house door, he falls down to the center of the house,
     * then moves up again (if the house center is his revival position), or moves sidewards towards his revival position.
     */
    private void updateStateEnteringHouse(GameModel game) {
        float speed = game.ghostSpeedReturningToHouse(this);
        if (position().roughlyEquals(revivalPosition, 0.5f * speed, 0.5f * speed)) {
            setPosition(revivalPosition);
            setMoveAndWishDir(UP);
            setState(LOCKED);
            return;
        }
        if (posY < revivalPosition.y()) {
            setMoveAndWishDir(DOWN);
        } else if (posX > revivalPosition.x()) {
            setMoveAndWishDir(LEFT);
        } else if (posX < revivalPosition.x()) {
            setMoveAndWishDir(RIGHT);
        }
        setSpeed(speed);
        move();
    }
}