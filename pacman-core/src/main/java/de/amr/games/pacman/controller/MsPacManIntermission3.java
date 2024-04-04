/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Pac;

import static de.amr.games.pacman.controller.GameController.publishGameEvent;
import static de.amr.games.pacman.lib.Globals.TS;

/**
 * Intermission scene 3: "Junior".
 *
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle. The stork drops the
 * bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and finally opens up to reveal a tiny Pac-Man.
 * (Played after rounds 9, 13, and 17)
 *
 * @author Armin Reichert
 */
public class MsPacManIntermission3 {

    public static final byte STATE_FLAP = 0;
    public static final byte STATE_DELIVER_JUNIOR = 1;
    public static final byte STATE_STORK_LEAVES_SCENE = 2;

    public static final int LANE_Y = TS * 24;

    public final Pac pacMan;
    public final Pac msPacMan;
    public final Entity stork;
    public final Entity bag;

    public boolean bagOpen;
    public int numBagBounces;

    private byte state;
    private final TickTimer stateTimer = new TickTimer("MsPacManIntermission3");

    public void changeState(byte state, long ticks) {
        this.state = state;
        stateTimer.reset(ticks);
        stateTimer.start();
    }

    public MsPacManIntermission3() {
        pacMan = new Pac("Pac-Man");
        msPacMan = new Pac("Ms. Pac-Man");
        stork = new Entity();
        bag = new Entity();
    }

    public void tick() {
        switch (state) {
            case STATE_FLAP:
                updateStateFlap();
                break;
            case STATE_DELIVER_JUNIOR:
                updateStateDeliverJunior();
                break;
            case STATE_STORK_LEAVES_SCENE:
                updateStateStorkLeavesScene();
                break;
            default:
                throw new IllegalStateException("Illegal state: " + state);
        }
        stateTimer.advance();
    }

    private void updateStateFlap() {
        if (stateTimer.atSecond(1)) {
            publishGameEvent(GameController.it().game(), GameEventType.INTERMISSION_STARTED);
        } else if (stateTimer.atSecond(3)) {
            enterStateDeliverJunior();
        }
    }

    private void enterStateDeliverJunior() {
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.setPosition(TS * 3, LANE_Y - 4);
        pacMan.selectAnimation(Pac.ANIM_HUSBAND_MUNCHING);
        pacMan.show();

        msPacMan.setMoveDir(Direction.RIGHT);
        msPacMan.setPosition(TS * 5, LANE_Y - 4);
        msPacMan.selectAnimation(Pac.ANIM_MUNCHING);
        msPacMan.show();

        stork.setPosition(TS * 30, TS * 12);
        stork.setVelocity(-0.8f, 0);
        stork.show();

        bag.setPosition(stork.position().plus(-14, 3));
        bag.setVelocity(stork.velocity());
        bag.setAcceleration(Vector2f.ZERO);
        bag.show();
        bagOpen = false;
        numBagBounces = 0;

        changeState(STATE_DELIVER_JUNIOR, TickTimer.INDEFINITE);
    }

    private void updateStateDeliverJunior() {
        stork.move();
        bag.move();

        // release bag from storks beak?
        if (stork.tile().x() == 20) {
            bag.setAcceleration(0, 0.04f); // gravity
            stork.setVelocity(-1, 0);
        }

        // (closed) bag reaches ground for first time?
        if (!bagOpen && bag.posY() > LANE_Y) {
            ++numBagBounces;
            if (numBagBounces < 3) {
                bag.setVelocity(-0.2f, -1f / numBagBounces);
                bag.setPosY(LANE_Y);
            } else {
                bagOpen = true;
                bag.setVelocity(Vector2f.ZERO);
                changeState(STATE_STORK_LEAVES_SCENE, 3 * 60);
            }
        }
    }

    private void updateStateStorkLeavesScene() {
        stork.move();
        if (stateTimer.hasExpired()) {
            GameController.it().terminateCurrentState();
        }
    }
}