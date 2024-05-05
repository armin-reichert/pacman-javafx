/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.lib.*;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import org.tinylog.Logger;

import java.util.BitSet;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.GameModel.*;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 *
 * @author Armin Reichert
 */
public class MsPacManIntro extends FiniteStateMachine<MsPacManIntro.State, MsPacManIntro> {

    public final float speed = 1.1f;
    public final int stopY = TS * 11 + 1;
    public final int stopX = TS * 6 - 4;
    public final int stopMsPacX = TS * 15 + 2;
    public final Vector2i titlePosition = v2i(TS * 10, TS * 8);
    public final TickTimer marqueeTimer = new TickTimer("marquee-timer");
    public final int numBulbs = 96;
    public final int bulbOnDistance = 16;
    public int ticksUntilLifting = 0;
    public int ghostIndex = 0;

    public final Pac msPacMan;
    public final Ghost[] ghosts;

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
            public void onEnter(MsPacManIntro intro) {
                intro.marqueeTimer.restartIndefinitely();
                intro.msPacMan.setPosition(TS * 31, TS * 20);
                intro.msPacMan.setMoveDir(Direction.LEFT);
                intro.msPacMan.setSpeed(intro.speed);
                intro.msPacMan.selectAnimation(Pac.ANIM_MUNCHING);
                intro.msPacMan.animations().ifPresent(Animations::startSelected);
                for (var ghost : intro.ghosts) {
                    ghost.setPosition(TS * 33.5f, TS * 20);
                    ghost.setMoveAndWishDir(Direction.LEFT);
                    ghost.setSpeed(intro.speed);
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.startAnimation();
                }
                intro.ghostIndex = 0;
            }

            @Override
            public void onUpdate(MsPacManIntro intro) {
                intro.marqueeTimer.advance();
                if (timer.atSecond(1)) {
                    intro.changeState(State.GHOSTS_MARCHING_IN);
                }
            }
        },

        GHOSTS_MARCHING_IN {
            @Override
            public void onUpdate(MsPacManIntro intro) {
                intro.marqueeTimer.advance();
                var ghost = intro.ghosts[intro.ghostIndex];
                ghost.show();

                if (ghost.moveDir() == Direction.LEFT) {
                    if (ghost.posX() <= intro.stopX) {
                        ghost.setPosX(intro.stopX);
                        ghost.setMoveAndWishDir(Direction.UP);
                        intro.ticksUntilLifting = 2;
                    } else {
                        ghost.move();
                    }
                    return;
                }

                if (ghost.moveDir() == Direction.UP) {
                    if (intro.ticksUntilLifting > 0) {
                        intro.ticksUntilLifting -= 1;
                        Logger.trace("Ticks until lifting {}: {}", ghost.name(), intro.ticksUntilLifting);
                        return;
                    }
                    if (ghost.posY() <= intro.stopY + ghost.id() * 16) {
                        ghost.setSpeed(0);
                        ghost.stopAnimation();
                        ghost.resetAnimation();
                        if (intro.ghostIndex == 3) {
                            intro.changeState(State.MS_PACMAN_MARCHING_IN);
                        } else {
                            ++intro.ghostIndex;
                        }
                    } else {
                        ghost.move();
                    }
                }
            }
        },

        MS_PACMAN_MARCHING_IN {
            @Override
            public void onUpdate(MsPacManIntro intro) {
                intro.marqueeTimer.advance();
                intro.msPacMan.show();
                intro.msPacMan.move();
                if (intro.msPacMan.posX() <= intro.stopMsPacX) {
                    intro.msPacMan.setSpeed(0);
                    intro.msPacMan.animations().ifPresent(Animations::resetSelected);
                    intro.changeState(State.READY_TO_PLAY);
                }
            }
        },

        READY_TO_PLAY {
            @Override
            public void onUpdate(MsPacManIntro intro) {
                intro.marqueeTimer.advance();
                if (timer.atSecond(2.0) && !gameController().hasCredit()) {
                    gameController().changeState(GameState.READY);
                    // go into demo mode
                } else if (timer.atSecond(5)) {
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

    public MsPacManIntro() {
        super(State.values());
        msPacMan = new Pac();
        ghosts = new Ghost[] {
            new Ghost(RED_GHOST),
            new Ghost(PINK_GHOST),
            new Ghost(CYAN_GHOST),
            new Ghost(ORANGE_GHOST)
        };
        ghosts[RED_GHOST].setName("Blinky");
        ghosts[PINK_GHOST].setName("Pinky");
        ghosts[CYAN_GHOST].setName("Inky");
        ghosts[ORANGE_GHOST].setName("Sue");
    }

    @Override
    public MsPacManIntro context() {
        return this;
    }
}