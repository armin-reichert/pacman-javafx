/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.rendering.ClapperboardAnimation;
import de.amr.games.pacman.ui2d.rendering.MsPacManGamePacAnimations;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;

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
public class MsPacManCutScene3 extends GameScene2D {

    private class MsPacManCutScene3Controller {

        public static final byte STATE_FLAP = 0;
        public static final byte STATE_DELIVER_JUNIOR = 1;
        public static final byte STATE_STORK_LEAVES_SCENE = 2;

        private byte state;
        private final TickTimer stateTimer = new TickTimer("MsPacManIntermission3");

        public void changeState(byte state, long ticks) {
            this.state = state;
            stateTimer.reset(ticks);
            stateTimer.start();
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
            stateTimer.tick();
        }

        private void updateStateFlap() {
            if (stateTimer.atSecond(1)) {
                context.game().publishGameEvent(GameEventType.INTERMISSION_STARTED);
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

    public static final int LANE_Y = TS * 24;

    private Pac pacMan;
    private Pac msPacMan;
    private Entity stork;
    private Entity bag;
    private boolean bagOpen;
    private int numBagBounces;

    private MsPacManCutScene3Controller sceneController;
    private ClapperboardAnimation clapAnimation;
    private SpriteAnimation storkAnimation;
    private MsPacManGameSpriteSheet sheet;

    @Override
    public boolean isCreditVisible() {
        return !context.game().hasCredit();
    }

    @Override
    public void init() {
        super.init();
        context.setScoreVisible(true);

        sheet = (MsPacManGameSpriteSheet) context.spriteSheet(context.game().variant());

        pacMan = new Pac();
        msPacMan = new Pac();
        stork = new Entity();
        bag = new Entity();

        msPacMan.setAnimations(new MsPacManGamePacAnimations(msPacMan, sheet));
        pacMan.setAnimations(new MsPacManGamePacAnimations(pacMan, sheet));
        storkAnimation = sheet.createStorkFlyingAnimation();
        storkAnimation.start();
        clapAnimation = new ClapperboardAnimation("3", "JUNIOR");
        clapAnimation.start();

        sceneController = new MsPacManCutScene3Controller();
        sceneController.changeState(MsPacManCutScene3Controller.STATE_FLAP, TickTimer.INDEFINITE);
    }

    @Override
    public void update() {
        sceneController.tick();
        clapAnimation.tick();
    }

    @Override
    public void drawSceneContent() {
        spriteRenderer.drawClapperBoard(g,
            context.assets().font("font.arcade", s(8)),
            context.assets().color("palette.pale"),
            clapAnimation, t(3), t(10));
        spriteRenderer.drawPac(g, msPacMan);
        spriteRenderer.drawPac(g, pacMan);
        spriteRenderer.drawEntitySprite(g, stork, storkAnimation.currentSprite());
        spriteRenderer.drawEntitySprite(g, bag, bagOpen ? sheet.juniorPacSprite() : sheet.blueBagSprite());
        drawLevelCounter(g);
    }
}