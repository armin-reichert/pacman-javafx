/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.world.House;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.*;

/**
 * There are 4 ghosts with different "personalities".
 * 
 * @author Armin Reichert
 */
public class Ghost extends Creature implements AnimationDirector {

	public static final String ANIM_GHOST_NORMAL     = "normal";
	public static final String ANIM_GHOST_FRIGHTENED = "frightened";
	public static final String ANIM_GHOST_EYES       = "eyes";
	public static final String ANIM_GHOST_FLASHING   = "flashing";
	public static final String ANIM_GHOST_NUMBER     = "number";
	public static final String ANIM_BLINKY_DAMAGED   = "damaged";
	public static final String ANIM_BLINKY_STRETCHED = "stretched";
	public static final String ANIM_BLINKY_PATCHED   = "patched";
	public static final String ANIM_BLINKY_NAKED     = "naked";

	private final byte id;
	private GhostState state;
	private byte killedIndex;
	private Pac pac;
	private Consumer<Ghost> fnHuntingBehavior;
	private Consumer<Ghost> fnFrightenedBehavior;
	private Predicate<Direction> fnIsSteeringAllowed;
	private World world;
	private House house;
	private Vector2f revivalPosition;
	private float speedReturningToHouse;
	private float speedInsideHouse;
	private Animations animations;

	public Ghost(byte id, String name) {
		super(name);
		checkGhostID(id);
		this.id = id;
		reset();
	}

	@Override
	public String toString() {
		return "Ghost{" +
			"id=" + id +
			", state=" + state +
			", killedIndex=" + killedIndex +
			'}';
	}

	/**
	 * The ghost ID. One of {@link GameModel#RED_GHOST}, {@link GameModel#PINK_GHOST}, {@link GameModel#CYAN_GHOST},
	 * {@link GameModel#ORANGE_GHOST}.
	 */
	public byte id() {
		return id;
	}

	public void setAnimations(Animations animations) {
		this.animations = animations;
	}

	@Override
	public Optional<Animations> animations() {
		return Optional.ofNullable(animations);
	}

	@Override
	public void reset() {
		super.reset();
		setKilledIndex(-1);
	}

	@Override
	public World world() {
		return world;
	}

	public void setWorld(World world) {
		checkNotNull(world);
		this.world = world;
	}

	public void setHouse(House house) {
		this.house = house;
	}

	public boolean insideHouse(House house) {
		return house.contains(tile());
	}

	public void setPac(Pac pac) {
		checkNotNull(pac);
		this.pac = pac;
	}

	public void setRevivalPosition(Vector2f revivalPosition) {
		checkNotNull(revivalPosition);
		this.revivalPosition = revivalPosition;
	}

	public void setSpeedReturningToHouse(float pixelsPerTick) {
		this.speedReturningToHouse = pixelsPerTick;
	}

	public void setSpeedInsideHouse(float pixelsPerTick) {
		this.speedInsideHouse = pixelsPerTick;
	}

	public void setFnIsSteeringAllowed(Predicate<Direction> fnIsSteeringAllowed) {
		checkNotNull(fnIsSteeringAllowed);
		this.fnIsSteeringAllowed = fnIsSteeringAllowed;
	}

	/**
	 * @return Index <code>(0,1,2,3)</code> telling when this ghost was killed during Pac-Man power phase. If not killed,
	 *         value is -1.
	 */
	public byte killedIndex() {
		return killedIndex;
	}

	public void setKilledIndex(int index) {
		if (index < -1 || index > 3) {
			throw new IllegalArgumentException("Killed index must be one of -1, 0, 1, 2, 3, but is: " + index);
		}
		this.killedIndex = (byte) index;
	}

	private boolean killable() {
		return pac.powerTimer().isRunning() && killedIndex == -1;
	}

	public void setFnHuntingBehavior(Consumer<Ghost> fnHuntingBehavior) {
		checkNotNull(fnHuntingBehavior);
		this.fnHuntingBehavior = fnHuntingBehavior;
	}

	public void setFnFrightenedBehavior(Consumer<Ghost> fnFrightenedBehavior) {
		checkNotNull(fnFrightenedBehavior);
		this.fnFrightenedBehavior = fnFrightenedBehavior;
	}

	@Override
	public boolean canAccessTile(Vector2i tile) {
		checkTileNotNull(tile);
		var currentTile = tile();
		for (var dir : Direction.values()) {
			if (tile.equals(currentTile.plus(dir.vector())) && !fnIsSteeringAllowed.test(dir)) {
				Logger.trace("Ghost {} cannot access tile {} because he cannot move {} at tile {}",
					name(), tile, dir, currentTile);
				return false;
			}
		}
		if (house.door().occupies(tile)) {
			return is(ENTERING_HOUSE, LEAVING_HOUSE);
		}
		return super.canAccessTile(tile);
	}

	@Override
	public boolean canReverse() {
		return isNewTileEntered() && is(HUNTING_PAC, FRIGHTENED);
	}

	// Here begins the state machine part

	/** The current state of this ghost. */
	public GhostState state() {
		return state;
	}

	/**
	 * @param alternatives ghost states to be checked
	 * @return <code>true</code> if this ghost is in any of the given states. If no alternatives are given, returns
	 *         <code>false</code>
	 */
	public boolean is(GhostState... alternatives) {
		return oneOf(state, alternatives);
	}

	public void setState(GhostState state) {
		checkNotNull(state);
		this.state = state;
		switch (state) {
			case LOCKED, HUNTING_PAC, LEAVING_HOUSE -> selectAnimation(ANIM_GHOST_NORMAL);
			case EATEN               -> selectAnimation(ANIM_GHOST_NUMBER, killedIndex);
			case RETURNING_TO_HOUSE  -> selectAnimation(ANIM_GHOST_EYES);
			case FRIGHTENED          -> updateFrightenedAnimation();
			case ENTERING_HOUSE      -> {}
		}
	}

	/**
	 * Executes a single simulation step for this ghost in the current game level.
	 */
	public void updateState() {
		switch (state) {
			case LOCKED             -> updateStateLocked();
			case LEAVING_HOUSE      -> updateStateLeavingHouse();
			case HUNTING_PAC        -> updateStateHuntingPac();
			case FRIGHTENED         -> updateStateFrightened();
			case EATEN              -> updateStateEaten();
			case RETURNING_TO_HOUSE -> updateStateReturningToHouse();
			case ENTERING_HOUSE     -> updateStateEnteringHouse();
		}
	}

	// --- LOCKED ---

	/**
	 * In locked state, ghosts inside the house are bouncing up and down. They become blue when Pac-Man gets power
	 * and start blinking when Pac-Man's power starts fading. After that, they return to their normal color.
	 */
	private void updateStateLocked() {
		if (insideHouse(house)) {
			float minY = revivalPosition.y() - 4, maxY = revivalPosition.y() + 4;
			setPixelSpeed(speedInsideHouse);
			move();
			if (posY <= minY) {
				setMoveAndWishDir(DOWN);
			} else if (posY >= maxY) {
				setMoveAndWishDir(UP);
			}
			posY = clamp(posY, minY, maxY);
		} else {
			setPixelSpeed(0);
		}
		if (killable()) {
			updateFrightenedAnimation();
		} else {
			selectAnimation(ANIM_GHOST_NORMAL);
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
	private void updateStateLeavingHouse() {
		Vector2f houseEntryPosition = house.door().entryPosition();
		if (posY() <= houseEntryPosition.y()) {
			// has raised and is outside house
			setPosition(houseEntryPosition);
			setMoveAndWishDir(LEFT);
			newTileEntered = false; // force moving left until new tile is entered
			if (killable()) {
				setState(FRIGHTENED);
			} else {
				killedIndex = -1; // TODO check this
				setState(HUNTING_PAC);
			}
			return;
		}
		// move inside house
		float centerX = center().x();
		float houseCenterX = house.center().x();
		if (differsAtMost(0.5f * speedInsideHouse, centerX, houseCenterX)) {
			// align horizontally and raise
			setPosX(houseCenterX - HTS);
			setMoveAndWishDir(UP);
		} else {
			// move sidewards until center axis is reached
			setMoveAndWishDir(centerX < houseCenterX ? RIGHT : LEFT);
		}
		setPixelSpeed(speedInsideHouse);
		move();
		if (killable()) {
			updateFrightenedAnimation();
		} else {
			selectAnimation(ANIM_GHOST_NORMAL);
		}
	}

	// --- HUNTING_PAC ---

	/**
	 * In each game level there are 4 alternating (scattering vs. chasing) hunting phases of different duration. The first
	 * hunting phase is always a "scatter" phase where the ghosts retreat to their maze corners. After some time they
	 * start chasing Pac-Man according to their character ("Shadow", "Speedy", "Bashful", "Pokey"). The 4th hunting phase
	 * is an "infinite" chasing phase.
	 * <p>
	 */
	private void updateStateHuntingPac() {
		fnHuntingBehavior.accept(this);
	}

	// --- FRIGHTENED ---

	/**
	 * When frightened, a ghost moves randomly through the world, at each new tile he randomly decides where to move next.
	 * Reversing the move direction is not allowed in this state either.
	 * <p>
	 * A frightened ghost has a blue color and starts flashing blue/white shortly (how long exactly?) before Pac-Man loses
	 * his power. Speed is about half of the normal speed.
	 */
	private void updateStateFrightened() {
		fnFrightenedBehavior.accept(this);
		updateFrightenedAnimation();
	}

	private void updateFrightenedAnimation() {
		if (pac.isPowerStartingToFade()) {
			selectAnimation(ANIM_GHOST_FLASHING);
		} else if (!pac.isPowerFading()) {
			selectAnimation(ANIM_GHOST_FRIGHTENED);
		}
	}

	// --- EATEN ---

	/**
	 * After a ghost is eaten by Pac-Man, he is displayed for a short time as the number of points earned for eating him.
	 * The value doubles for each ghost eaten using the power of the same energizer.
	 */
	private void updateStateEaten() {
		// wait for timeout
	}

	// --- RETURNING_TO_HOUSE ---

	/**
	 * After the short time being displayed by his value, the eaten ghost is displayed by his eyes only and returns
	 * to the ghost house to be revived. Hallelujah!
	 */
	private void updateStateReturningToHouse() {
		Vector2f houseEntry = house.door().entryPosition();
		if (position().almostEquals(houseEntry, 0.5f * speedReturningToHouse, 0)) {
			setPosition(houseEntry);
			setState(ENTERING_HOUSE);
		} else {
			setPixelSpeed(speedReturningToHouse);
			setTargetTile(house.door().leftWing());
			navigateTowardsTarget();
			tryMoving();
		}
	}

	// --- ENTERING_HOUSE ---

	/**
	 * When an eaten ghost has arrived at the ghost house door, he falls down to the center of the house,
	 * then moves up again (if the house center is his revival position), or moves sidewards towards his revival position.
	 */
	private void updateStateEnteringHouse() {
		Vector2f entryPosition = house.door().entryPosition();
		Vector2f houseCenter = house.center();
		if (position().almostEquals(entryPosition,0.5f * velocity().length(), 0) && moveDir() != Direction.DOWN) {
			// if near entry, start falling
			setPosition(entryPosition);
			setMoveAndWishDir(Direction.DOWN);
		} else if (posY() >= houseCenter.y()) {
			// reached ground
			setPosY(houseCenter.y());
			if (revivalPosition.x() < houseCenter.x()) {
				setMoveAndWishDir(LEFT);
			} else if (revivalPosition.x() > houseCenter.x()) {
				setMoveAndWishDir(RIGHT);
			}
		}
		setPixelSpeed(speedReturningToHouse);
		move();
		if (differsAtMost(0.5 * speedReturningToHouse, posX(), revivalPosition.x())
			&& posY() >= revivalPosition.y()) {
			setPosition(revivalPosition);
			setMoveAndWishDir(UP);
			setState(LOCKED);
		}
	}
}