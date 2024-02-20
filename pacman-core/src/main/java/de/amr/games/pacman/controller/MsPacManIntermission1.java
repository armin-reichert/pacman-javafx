/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.*;

import static de.amr.games.pacman.lib.Globals.TS;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 * 
 * @author Armin Reichert
 */
public class MsPacManIntermission1 {

	public static final byte STATE_FLAP = 0;
	public static final byte STATE_CHASED_BY_GHOSTS = 1;
	public static final byte STATE_COMING_TOGETHER = 2;
	public static final byte STATE_IN_HEAVEN = 3;

	public static final int UPPER_LANE_Y = TS * 12;
	public static final int MIDDLE_LANE_Y = TS * 18;
	public static final int LOWER_LANE_Y = TS * 24;

	public static final float SPEED_PAC_CHASING = 1.125f;
	public static final float SPEED_PAC_RISING = 0.75f;
	public static final float SPEED_GHOST_AFTER_COLLISION = 0.3f;
	public static final float SPEED_GHOST_CHASING = 1.25f;

	public final Pac pacMan;
	public final Pac msPac;
	public final Ghost pinky;
	public final Ghost inky;
	public final Entity heart;

	private byte state;
	private final TickTimer stateTimer = new TickTimer("MsPacManIntermission1");

	public void changeState(byte state, long ticks) {
		this.state = state;
		stateTimer.reset(ticks);
		stateTimer.start();
	}

	public MsPacManIntermission1() {
		pacMan = new Pac("Pac-Man");
		inky = new Ghost(GameModel.CYAN_GHOST, "Inky");
		msPac = new Pac("Ms. Pac-Man");
		pinky = new Ghost(GameModel.PINK_GHOST, "Pinky");
		heart = new Entity();
	}

	public void tick() {
		switch (state) {
		case STATE_FLAP:
			updateStateFlap();
			break;
		case STATE_CHASED_BY_GHOSTS:
			updateStateChasedByGhosts();
			break;
		case STATE_COMING_TOGETHER:
			updateStateComingTogether();
			break;
		case STATE_IN_HEAVEN:
			if (stateTimer.hasExpired()) {
				GameController.it().terminateCurrentState();
				return;
			}
			break;
		default:
			throw new IllegalStateException("Illegal state: " + state);
		}
		stateTimer.advance();
	}

	private void updateStateFlap() {
		if (stateTimer.atSecond(1)) {
			GameController.it().publishGameEvent(GameEventType.INTERMISSION_STARTED);
		} else if (stateTimer.hasExpired()) {
			enterStateChasedByGhosts();
		}
	}

	private void enterStateChasedByGhosts() {
		pacMan.setMoveDir(Direction.RIGHT);
		pacMan.setPosition(TS * (-2), UPPER_LANE_Y);
		pacMan.setPixelSpeed(SPEED_PAC_CHASING);
		pacMan.selectAnimation(Pac.ANIM_HUSBAND_MUNCHING);
		pacMan.startAnimation();
		pacMan.show();

		inky.setMoveAndWishDir(Direction.RIGHT);
		inky.setPosition(pacMan.position().minus(TS * 6, 0));
		inky.setPixelSpeed(SPEED_GHOST_CHASING);
		inky.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
		inky.startAnimation();
		inky.show();

		msPac.setMoveDir(Direction.LEFT);
		msPac.setPosition(TS * 30, LOWER_LANE_Y);
		msPac.setPixelSpeed(SPEED_PAC_CHASING);
		msPac.selectAnimation(Pac.ANIM_MUNCHING);
		msPac.startAnimation();
		msPac.show();

		pinky.setMoveAndWishDir(Direction.LEFT);
		pinky.setPosition(msPac.position().plus(TS * 6, 0));
		pinky.setPixelSpeed(SPEED_GHOST_CHASING);
		pinky.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
		pinky.startAnimation();
		pinky.show();

		changeState(STATE_CHASED_BY_GHOSTS, TickTimer.INDEFINITE);
	}

	private void updateStateChasedByGhosts() {
		if (inky.posX() > TS * 30) {
			enterStateComingTogether();
		} else {
			pacMan.move();
			msPac.move();
			inky.move();
			pinky.move();
		}
	}

	private void enterStateComingTogether() {
		msPac.setPosition(TS * (-3), MIDDLE_LANE_Y);
		msPac.setMoveDir(Direction.RIGHT);
		pinky.setPosition(msPac.position().minus(TS * 5, 0));
		pinky.setMoveAndWishDir(Direction.RIGHT);
		pacMan.setPosition(TS * 31, MIDDLE_LANE_Y);
		pacMan.setMoveDir(Direction.LEFT);
		inky.setPosition(pacMan.position().plus(TS * 5, 0));
		inky.setMoveAndWishDir(Direction.LEFT);
		changeState(STATE_COMING_TOGETHER, TickTimer.INDEFINITE);
	}

	private void updateStateComingTogether() {
		// Pac-Man and Ms. Pac-Man reach end position?
		if (pacMan.moveDir() == Direction.UP && pacMan.posY() < UPPER_LANE_Y) {
			enterStateInHeaven();
		}

		// Pac-Man and Ms. Pac-Man meet?
		else if (pacMan.moveDir() == Direction.LEFT && pacMan.posX() - msPac.posX() < TS * (2)) {
			pacMan.setMoveDir(Direction.UP);
			pacMan.setPixelSpeed(SPEED_PAC_RISING);
			msPac.setMoveDir(Direction.UP);
			msPac.setPixelSpeed(SPEED_PAC_RISING);
		}

		// Inky and Pinky collide?
		else if (inky.moveDir() == Direction.LEFT && inky.posX() - pinky.posX() < TS * (2)) {
			inky.setMoveAndWishDir(Direction.RIGHT);
			inky.setPixelSpeed(SPEED_GHOST_AFTER_COLLISION);
			inky.setVelocity(inky.velocity().minus(0, 2.0f));
			inky.setAcceleration(0, 0.4f);

			pinky.setMoveAndWishDir(Direction.LEFT);
			pinky.setPixelSpeed(SPEED_GHOST_AFTER_COLLISION);
			pinky.setVelocity(pinky.velocity().minus(0, 2.0f));
			pinky.setAcceleration(0, 0.4f);
		}

		else {
			pacMan.move();
			msPac.move();
			inky.move();
			pinky.move();
			if (inky.posY() > MIDDLE_LANE_Y) {
				inky.setPosition(inky.posX(), MIDDLE_LANE_Y);
				inky.setAcceleration(Vector2f.ZERO);
			}
			if (pinky.posY() > MIDDLE_LANE_Y) {
				pinky.setPosition(pinky.posX(), MIDDLE_LANE_Y);
				pinky.setAcceleration(Vector2f.ZERO);
			}
		}
	}

	private void enterStateInHeaven() {
		pacMan.setPixelSpeed(0);
		pacMan.setMoveDir(Direction.LEFT);
		pacMan.stopAnimation();
		pacMan.resetAnimation();

		msPac.setPixelSpeed(0);
		msPac.setMoveDir(Direction.RIGHT);
		msPac.stopAnimation();
		msPac.resetAnimation();

		inky.setPixelSpeed(0);
		inky.hide();

		pinky.setPixelSpeed(0);
		pinky.hide();

		heart.setPosition((pacMan.posX() + msPac.posX()) / 2, pacMan.posY() - TS * (2));
		heart.show();

		changeState(STATE_IN_HEAVEN, 3 * 60);
	}
}