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
import static de.amr.games.pacman.model.actors.GhostState.EATEN;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;

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

	/**
	 * Intro is controlled by a FSM, here come the states.
	 */
	public enum State implements FsmState<PacManIntro> {

		START {
			@Override
			public void onUpdate(PacManIntro intro) {
				if (timer.tick() == 2) {
					intro.creditVisible = true;
				} else if (timer.tick() == 3) {
					intro.titleVisible = true;
				} else if (timer.atSecond(1)) {
					intro.changeState(State.PRESENTING_GHOSTS);
				}
			}
		},

		PRESENTING_GHOSTS {
			@Override
			public void onUpdate(PacManIntro intro) {
				if (timer.atSecond(0)) {
					intro.ghostInfo[intro.ghostIndex].pictureVisible = true;
				} else if (timer.atSecond(1.0)) {
					intro.ghostInfo[intro.ghostIndex].characterVisible = true;
				} else if (timer.atSecond(1.5)) {
					intro.ghostInfo[intro.ghostIndex].nicknameVisible = true;
				} else if (timer.atSecond(2.0)) {
					if (intro.ghostIndex < intro.ghostInfo.length - 1) {
						timer.resetIndefinitely();
					}
					intro.ghostIndex += 1;
				} else if (timer.atSecond(2.5)) {
					intro.changeState(State.SHOWING_POINTS);
				}
			}
		},

		SHOWING_POINTS {
			@Override
			public void onEnter(PacManIntro intro) {
				intro.blinking.stop();
			}

			@Override
			public void onUpdate(PacManIntro intro) {
				if (timer.atSecond(1)) {
					intro.changeState(State.CHASING_PAC);
				}
			}
		},

		CHASING_PAC {
			@Override
			public void onEnter(PacManIntro intro) {
				timer.restartIndefinitely();
				intro.pacMan.setPosition(TS * 36, TS * 20);
				intro.pacMan.setMoveDir(Direction.LEFT);
				intro.pacMan.setPixelSpeed(intro.chaseSpeed);
				intro.pacMan.show();
				intro.pacMan.selectAnimation(Pac.ANIM_MUNCHING);
				intro.pacMan.startAnimation();
				intro.ghosts().forEach(ghost -> {
					ghost.setState(GhostState.HUNTING_PAC);
					ghost.setPosition(intro.pacMan.position().plus(16 * (ghost.id() + 1), 0));
					ghost.setMoveAndWishDir(Direction.LEFT);
					ghost.setPixelSpeed(intro.chaseSpeed);
					ghost.show();
					ghost.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
					ghost.startAnimation();
				});
			}

			@Override
			public void onUpdate(PacManIntro intro) {
				if (timer.atSecond(1)) {
					intro.blinking.start();
				}
				// Pac-Man reaches the energizer at the left and turns
				if (intro.pacMan.posX() <= TS * intro.leftTileX) {
					intro.changeState(State.CHASING_GHOSTS);
				}
				// Ghosts already reverse direction before Pac-Man eats the energizer and turns!
				else if (intro.pacMan.posX() <= TS * intro.leftTileX + HTS) {
					intro.ghosts().forEach(ghost -> {
						ghost.setState(FRIGHTENED);
						ghost.selectAnimation(Ghost.ANIM_GHOST_FRIGHTENED);
						ghost.setMoveAndWishDir(Direction.RIGHT);
						ghost.setPixelSpeed(intro.ghostFrightenedSpeed);
						ghost.move();
					});
					intro.pacMan.move();
				}
				else { // keep moving
					intro.blinking.tick();
					intro.pacMan.move();
					intro.ghosts().forEach(Ghost::move);
				}
			}
		},

		CHASING_GHOSTS {
			@Override
			public void onEnter(PacManIntro intro) {
				timer.restartIndefinitely();
				intro.ghostKilledTime = timer.tick();
				intro.pacMan.setMoveDir(Direction.RIGHT);
				intro.pacMan.setPixelSpeed(intro.chaseSpeed);
			}

			@Override
			public void onUpdate(PacManIntro intro) {
				if (intro.ghosts().allMatch(ghost -> ghost.is(EATEN))) {
					intro.pacMan.hide();
					intro.changeState(READY_TO_PLAY);
					return;
				}

				intro.ghosts()
					.filter(ghost -> ghost.is(FRIGHTENED) && ghost.sameTile(intro.pacMan))
					.findFirst()
					.ifPresent(victim -> {
						//TODO(robustness) If killedIndex not set *before* changing state, animation frame index is invalid!
						victim.setKilledIndex(victim.id());
						victim.setState(EATEN);
						intro.ghostKilledTime = timer.tick();
						intro.pacMan.hide();
						intro.pacMan.setPixelSpeed(0);
						intro.ghosts().forEach(ghost -> {
							ghost.setPixelSpeed(0);
							ghost.stopAnimation();
						});
				});

				// After 50 ticks, Pac-Man and the surviving ghosts get visible again and move on
				if (timer.tick() == intro.ghostKilledTime + 50) {
					intro.pacMan.show();
					intro.pacMan.setPixelSpeed(intro.chaseSpeed);
					intro.ghosts().forEach(ghost -> {
						if (ghost.is(EATEN)) {
							ghost.hide();
						} else {
							ghost.show();
							ghost.setPixelSpeed(intro.ghostFrightenedSpeed);
							ghost.startAnimation();
						}
					});
				}

				intro.pacMan.move();
				intro.ghosts().forEach(Ghost::move);
				intro.blinking.tick();
			}
		},

		READY_TO_PLAY {
			@Override
			public void onUpdate(PacManIntro intro) {
				if (timer.atSecond(0.75)) {
					intro.ghostInfo[3].ghost.hide();
					if (!gameController().hasCredit()) {
						gameController().changeState(GameState.READY);
					}
				}
				else if (timer.atSecond(5)) {
					gameController().changeState(GameState.CREDIT);
				}
			}
		};

		final TickTimer timer = new TickTimer("Timer-" + name());

		@Override
		public TickTimer timer() {
			return timer;
		}

		GameController gameController() {
			return GameController.it();
		}
	}

	public float chaseSpeed = 1.1f;

	public float ghostFrightenedSpeed = 0.6f;
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