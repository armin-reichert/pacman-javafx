/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.NavigationPoint;
import de.amr.games.pacman.lib.Pulse;
import de.amr.games.pacman.lib.RouteBasedSteering;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.games.pacman.event.GameEventManager.publishGameEvent;

/**
 * A bonus that tumbles through the world, starting at some portal, making one round around the ghost house and leaving
 * the maze at some portal at the other border.
 * 
 * <p>
 * That's however not exactly the original Ms. Pac-Man behaviour with predefined "fruit paths".
 * 
 * @author Armin Reichert
 */
public class MovingBonus extends Creature implements Bonus {

	private final Pulse jumpAnimation;
	private final RouteBasedSteering steering = new RouteBasedSteering();
	private final byte symbol;
	private final int points;
	private long eatenTimer;
	private byte state;

	public MovingBonus(byte symbol, int points) {
		super("MovingBonus-" + symbol);
		reset();
		this.symbol = symbol;
		this.points = points;
		jumpAnimation = new Pulse(10, false);
		canTeleport = false; // override default from Creature
		eatenTimer = 0;
		state = Bonus.STATE_INACTIVE;
	}

	@Override
	public boolean canReverse() {
		return false;
	}

	@Override
	public MovingBonus entity() {
		return this;
	}

	@Override
	public String toString() {
		return String.format("[MovingBonus state=%s symbol=%d value=%d eatenTimer=%d tile=%s]",
				state, symbol(), points, eatenTimer, tile());
	}

	@Override
	public byte state() {
		return state;
	}

	@Override
	public byte symbol() {
		return symbol;
	}

	@Override
	public int points() {
		return points;
	}

	@Override
	public void setInactive() {
		state = Bonus.STATE_INACTIVE;
		jumpAnimation.stop();
		hide();
		setSpeed(0);
	}

	@Override
	public void setEdible(long ticks) {
		state = Bonus.STATE_EDIBLE;
		jumpAnimation.restart();
		show();
		setSpeed(0.5f); // how fast in the original game?
		setTargetTile(null);
	}

	@Override
	public void setEaten(long ticks) {
		state = Bonus.STATE_EATEN;
		eatenTimer = ticks;
		jumpAnimation.stop();
		Logger.info("Bonus eaten: {}", this);
	}

	public void setRoute(List<NavigationPoint> route, boolean leftToRight) {
		centerOverTile(route.get(0).tile());
		setMoveAndWishDir(leftToRight ? Direction.RIGHT : Direction.LEFT);
		route.remove(0);
		steering.setRoute(route);
	}

	public float dy() {
		if (!jumpAnimation.isRunning()) {
			return 0;
		}
		return jumpAnimation.on() ? -3f : 3f;
	}

	@Override
	public void update(GameLevel level) {
		switch (state) {

		case STATE_INACTIVE:
			break; // nothing to do

		case STATE_EDIBLE: {
			if (sameTile(level.pac())) {
				level.game().scorePoints(points());
				setEaten(GameModel.BONUS_POINTS_SHOWN_TICKS);
				publishGameEvent(level.game(), GameEventType.BONUS_EATEN);
				return;
			}
			steering.steer(level, this);
			if (steering.isComplete()) {
				setInactive();
				Logger.trace("Bonus left world: {}", this);
				publishGameEvent(level.game(), GameEventType.BONUS_EXPIRED, tile());
			} else {
				navigateTowardsTarget();
				tryMoving();
				jumpAnimation.tick();
			}
			break;
		}

		case STATE_EATEN: {
			if (--eatenTimer == 0) {
				setInactive();
				Logger.trace("Bonus expired: {}", this);
				publishGameEvent(level.game(), GameEventType.BONUS_EXPIRED, tile());
			}
			break;
		}

		default:
			throw new IllegalStateException();
		}
	}
}