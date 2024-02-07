/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.lib.*;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import org.tinylog.Logger;

import java.util.BitSet;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacManIntro extends Fsm<MsPacManIntro.State, MsPacManIntro> {

	public float          speed                = 1.1f;
	public int            stopY                = TS * 11 + 1;
	public int            stopX                = TS * 6 - 4;
	public int            stopMsPacX           = TS * 15 + 2;
	public int            ticksUntilLifting    = 0;
	public Vector2i       titlePosition        = v2i(TS * 10, TS * 8);
	public TickTimer      marqueeTimer         = new TickTimer("marquee-timer");
	public int            numBulbs             = 96;
	public int            bulbOnDistance       = 16;
	public Pac            msPacMan             = new Pac("Ms. Pac-Man");
	public Ghost[]        ghosts               = {
			new Ghost(GameModel.RED_GHOST,   "Blinky"),
			new Ghost(GameModel.PINK_GHOST,  "Pinky"),
			new Ghost(GameModel.CYAN_GHOST,  "Inky"),
			new Ghost(GameModel.ORANGE_GHOST,"Sue")


	};
	public int ghostIndex = 0;

	/**
	 * In the Arcade game, 6 of the 96 bulbs are switched-on every frame, shifting every tick. The bulbs in the leftmost
	 * column however are switched-off every second frame. Maybe a bug?
	 *
	 * @return bitset indicating which marquee bulbs are on
	 */
	public BitSet marqueeState() {
		var state = new BitSet(numBulbs);
		long t = marqueeTimer.tick();
		for (int b = 0; b < 6; ++b) {
			state.set((int) (b * bulbOnDistance + t) % numBulbs);
		}
		for (int i = 81; i < numBulbs; ++i) {
			if (isOdd(i)) {
				state.clear(i);
			}
		}
		return state;
	}

	public enum State implements FsmState<MsPacManIntro> {

		START {
			@Override
			public void onEnter(MsPacManIntro ctx) {
				ctx.marqueeTimer.restartIndefinitely();
				ctx.msPacMan.setPosition(TS * 31, TS * 20);
				ctx.msPacMan.setMoveDir(Direction.LEFT);
				ctx.msPacMan.setPixelSpeed(ctx.speed);
				ctx.msPacMan.selectAnimation(PacAnimations.MUNCHING);
				ctx.msPacMan.startAnimation();
				for (var ghost : ctx.ghosts) {
					ghost.setPosition(TS * 33.5f, TS * 20);
					ghost.setMoveAndWishDir(Direction.LEFT);
					ghost.setPixelSpeed(ctx.speed);
					ghost.enterStateHuntingPac();
					ghost.startAnimation();
				}
				ctx.ghostIndex = 0;
			}

			@Override
			public void onUpdate(MsPacManIntro ctx) {
				ctx.marqueeTimer.advance();
				if (timer.atSecond(1)) {
					ctx.changeState(State.GHOSTS_MARCHING_IN);
				}
			}
		},

		GHOSTS_MARCHING_IN {
			@Override
			public void onUpdate(MsPacManIntro ctx) {
				ctx.marqueeTimer.advance();
				var ghost = ctx.ghosts[ctx.ghostIndex];
				ghost.show();

				if (ghost.moveDir() == Direction.LEFT) {
					if (ghost.pos_x() <= ctx.stopX) {
						ghost.setPos_x(ctx.stopX);
						ghost.setMoveAndWishDir(Direction.UP);
						ctx.ticksUntilLifting = 2;
					} else {
						ghost.move();
					}
					return;
				}

				if (ghost.moveDir() == Direction.UP) {
					if (ctx.ticksUntilLifting > 0) {
						ctx.ticksUntilLifting -= 1;
						Logger.trace("Ticks until lifting {}: {}", ghost.name(), ctx.ticksUntilLifting);
						return;
					}
					if (ghost.pos_y() <= ctx.stopY + ghost.id() * 16) {
						ghost.setPixelSpeed(0);
						ghost.animations().ifPresent(ani -> {
							ani.stopSelected();
							ani.resetSelected();
						});
						if (ctx.ghostIndex == 3) {
							ctx.changeState(State.MS_PACMAN_MARCHING_IN);
						} else {
							++ctx.ghostIndex;
						}
					} else {
						ghost.move();
					}
				}
			}
		},

		MS_PACMAN_MARCHING_IN {
			@Override
			public void onUpdate(MsPacManIntro ctx) {
				ctx.marqueeTimer.advance();
				ctx.msPacMan.show();
				ctx.msPacMan.move();
				if (ctx.msPacMan.pos_x() <= ctx.stopMsPacX) {
					ctx.msPacMan.setPixelSpeed(0);
					ctx.msPacMan.animations().ifPresent(Animations::resetSelected);
					ctx.changeState(State.READY_TO_PLAY);
				}
			}
		},

		READY_TO_PLAY {
			@Override
			public void onUpdate(MsPacManIntro ctx) {
				ctx.marqueeTimer.advance();
				if (timer.atSecond(2.0) && !GameController.it().hasCredit()) {
					GameController.it().changeState(GameState.READY);
					// go into demo mode
				} else if (timer.atSecond(5)) {
					GameController.it().changeState(GameState.CREDIT);
				}
			}
		};

		final TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public TickTimer timer() {
			return timer;
		}
	}

	public MsPacManIntro() {
		super(State.values());
	}

	@Override
	public MsPacManIntro context() {
		return this;
	}
}