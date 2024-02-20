/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.lib.*;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;

/**
 * Controls the intro scene of the Pac-Man game.
 * <p>
 * The ghosts are presented one after another, then Pac-Man is chased by the ghosts, turns the card and hunts the ghosts
 * himself.
 * 
 * @author Armin Reichert
 */
public class PacManIntro extends Fsm<PacManIntro.State, PacManIntro> {

	public static class GhostInfo {
		public Ghost ghost;
		public String character;
		public boolean pictureVisible;
		public boolean nicknameVisible;
		public boolean characterVisible;

		public GhostInfo(byte id, String nickname, String character) {
			ghost = new Ghost(id, nickname);
			this.character = character;
		}
	}

	public enum State implements FsmState<PacManIntro> {

		START {
			@Override
			public void onUpdate(PacManIntro ctx) {
				if (timer.tick() == 2) {
					ctx.creditVisible = true;
				} else if (timer.tick() == 3) {
					ctx.titleVisible = true;
				} else if (timer.atSecond(1)) {
					ctx.changeState(State.PRESENTING_GHOSTS);
				}
			}
		},

		PRESENTING_GHOSTS {
			@Override
			public void onUpdate(PacManIntro ctx) {
				if (timer.atSecond(0)) {
					ctx.ghostInfo[ctx.ghostIndex].pictureVisible = true;
				} else if (timer.atSecond(1.0)) {
					ctx.ghostInfo[ctx.ghostIndex].characterVisible = true;
				} else if (timer.atSecond(1.5)) {
					ctx.ghostInfo[ctx.ghostIndex].nicknameVisible = true;
				} else if (timer.atSecond(2.0)) {
					if (++ctx.ghostIndex < 4) {
						timer.resetIndefinitely();
					}
				} else if (timer.atSecond(2.5)) {
					ctx.changeState(State.SHOWING_POINTS);
				}
			}
		},

		SHOWING_POINTS {
			@Override
			public void onEnter(PacManIntro ctx) {
				ctx.blinking.stop();
			}

			@Override
			public void onUpdate(PacManIntro ctx) {
				if (timer.atSecond(1)) {
					ctx.changeState(State.CHASING_PAC);
				}
			}
		},

		CHASING_PAC {
			@Override
			public void onEnter(PacManIntro ctx) {
				timer.restartIndefinitely();
				ctx.pacMan.setPosition(TS * 36, TS * 20);
				ctx.pacMan.setMoveDir(Direction.LEFT);
				ctx.pacMan.setPixelSpeed(ctx.chaseSpeed);
				ctx.pacMan.show();
				ctx.pacMan.selectAnimation(Pac.ANIM_MUNCHING);
				ctx.pacMan.startAnimation();
				ctx.ghosts().forEach(ghost -> {
					ghost.setState(GhostState.HUNTING_PAC);
					ghost.setPosition(ctx.pacMan.position().plus(16 * (ghost.id() + 1), 0));
					ghost.setMoveAndWishDir(Direction.LEFT);
					ghost.setPixelSpeed(ctx.chaseSpeed);
					ghost.show();
					ghost.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
					ghost.startAnimation();
				});
			}

			@Override
			public void onUpdate(PacManIntro ctx) {
				if (timer.atSecond(1)) {
					ctx.blinking.start();
				}
				// Pac-Man reaches the energizer at the left and turns
				if (ctx.pacMan.posX() <= TS * ctx.leftTileX) {
					ctx.changeState(State.CHASING_GHOSTS);
				}
				// Ghosts already reverse direction before Pac-Man eats the energizer and turns!
				else if (ctx.pacMan.posX() <= TS * ctx.leftTileX + HTS) {
					ctx.ghosts().forEach(ghost -> {
						ghost.setState(GhostState.FRIGHTENED);
						ghost.selectAnimation(Ghost.ANIM_GHOST_FRIGHTENED);
						ghost.setMoveAndWishDir(Direction.RIGHT);
						ghost.setPixelSpeed(0.6f);
						ghost.move();
					});
					ctx.pacMan.move();
				}
				else { // keep moving
					ctx.blinking.tick();
					ctx.pacMan.move();
					ctx.ghosts().forEach(Ghost::move);
				}
			}
		},

		CHASING_GHOSTS {
			@Override
			public void onEnter(PacManIntro ctx) {
				timer.restartIndefinitely();
				ctx.ghostKilledTime = timer.tick();
				ctx.pacMan.setMoveDir(Direction.RIGHT);
				ctx.pacMan.setPixelSpeed(ctx.chaseSpeed);
			}

			@Override
			public void onUpdate(PacManIntro ctx) {
				if (ctx.ghosts().allMatch(ghost -> ghost.is(GhostState.EATEN))) {
					ctx.pacMan.hide();
					ctx.changeState(READY_TO_PLAY);
					return;
				}
				var nextVictim = ctx.ghosts()//
						.filter(ctx.pacMan::sameTile)//
						.filter(ghost -> ghost.is(GhostState.FRIGHTENED))//
						.findFirst();
				nextVictim.ifPresent(victim -> {
					victim.setKilledIndex(victim.id());
					ctx.ghostKilledTime = timer.tick();
					victim.setState(GhostState.EATEN);
					ctx.pacMan.hide();
					ctx.pacMan.setPixelSpeed(0);
					ctx.ghosts().forEach(ghost -> {
						ghost.setPixelSpeed(0);
						ghost.stopAnimation();
					});
				});

				// After ??? sec, Pac-Man and the surviving ghosts get visible again and move on
				if (timer.tick() - ctx.ghostKilledTime == timer.secToTicks(0.9)) {
					ctx.pacMan.show();
					ctx.pacMan.setPixelSpeed(ctx.chaseSpeed);
					ctx.ghosts().forEach(ghost -> {
						if (!ghost.is(GhostState.EATEN)) {
							ghost.show();
							ghost.setPixelSpeed(0.6f);
							ghost.startAnimation();
						} else {
							ghost.hide();
						}
					});
				}
				ctx.pacMan.move();
				ctx.ghosts().forEach(Ghost::move);
				ctx.blinking.tick();
			}
		},

		READY_TO_PLAY {
			@Override
			public void onUpdate(PacManIntro ctx) {
				if (timer.atSecond(0.75)) {
					ctx.ghostInfo[3].ghost.hide();
					if (!GameController.it().hasCredit()) {
						GameController.it().changeState(GameState.READY);
					}
				}
				else if (timer.atSecond(5)) {
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

	public float chaseSpeed = 1.1f;
	public int leftTileX = 4;
	public Pulse blinking = new Pulse(10, true);
	public Pac pacMan = new Pac("Pac-Man");
	public GhostInfo[] ghostInfo = {
		new GhostInfo(GameModel.RED_GHOST,   "BLINKY","SHADOW"),
		new GhostInfo(GameModel.PINK_GHOST,  "PINKY", "SPEEDY"),
		new GhostInfo(GameModel.CYAN_GHOST,  "INKY",  "BASHFUL"),
		new GhostInfo(GameModel.ORANGE_GHOST,"CLYDE", "POKEY")
	};
	public boolean creditVisible = false;
	public boolean titleVisible = false;
	public int ghostIndex;
	public long ghostKilledTime;

	public Ghost ghost(int id) {
		return ghostInfo[id].ghost;
	}

	public Stream<Ghost> ghosts() {
		return Stream.of(ghostInfo).map(info -> info.ghost);
	}

	public PacManIntro() {
		super(State.values());
	}

	@Override
	public PacManIntro context() {
		return this;
	}
}