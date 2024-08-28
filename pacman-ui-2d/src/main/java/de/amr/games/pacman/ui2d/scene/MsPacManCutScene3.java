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
import de.amr.games.pacman.model.GameVariant;
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

    private static class Data {
        public static final int LANE_Y = TS * 24;

        public final Pac pacMan = new Pac();
        public final Pac msPacMan = new Pac();
        public final Entity stork = new Entity();
        public final Entity bag = new Entity();

        public boolean bagOpen;
        public int numBagBounces;
    }

    private static class MsPacManCutScene3Controller {

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

        public void tick(Data data) {
            switch (state) {
                case STATE_FLAP:
                    updateStateFlap(data);
                    break;
                case STATE_DELIVER_JUNIOR:
                    updateStateDeliverJunior(data);
                    break;
                case STATE_STORK_LEAVES_SCENE:
                    updateStateStorkLeavesScene(data);
                    break;
                default:
                    throw new IllegalStateException("Illegal state: " + state);
            }
            stateTimer.tick();
        }

        private void updateStateFlap(Data data) {
            if (stateTimer.atSecond(1)) {
                GameController.it().gameModel(GameVariant.MS_PACMAN).publishGameEvent(GameEventType.INTERMISSION_STARTED);
            } else if (stateTimer.atSecond(3)) {
                enterStateDeliverJunior(data);
            }
        }

        private void enterStateDeliverJunior(Data data) {
            data.pacMan.setMoveDir(Direction.RIGHT);
            data.pacMan.setPosition(TS * 3, Data.LANE_Y - 4);
            data.pacMan.selectAnimation(Pac.ANIM_HUSBAND_MUNCHING);
            data.pacMan.show();

            data.msPacMan.setMoveDir(Direction.RIGHT);
            data.msPacMan.setPosition(TS * 5, Data.LANE_Y - 4);
            data.msPacMan.selectAnimation(Pac.ANIM_MUNCHING);
            data.msPacMan.show();

            data.stork.setPosition(TS * 30, TS * 12);
            data.stork.setVelocity(-0.8f, 0);
            data.stork.show();

            data.bag.setPosition(data.stork.position().plus(-14, 3));
            data.bag.setVelocity(data.stork.velocity());
            data.bag.setAcceleration(Vector2f.ZERO);
            data.bag.show();
            data.bagOpen = false;
            data.numBagBounces = 0;

            changeState(STATE_DELIVER_JUNIOR, TickTimer.INDEFINITE);
        }

        private void updateStateDeliverJunior(Data data) {
            data.stork.move();
            data.bag.move();

            // release bag from storks beak?
            if (data.stork.tile().x() == 20) {
                data.bag.setAcceleration(0, 0.04f); // gravity
                data.stork.setVelocity(-1, 0);
            }

            // (closed) bag reaches ground for first time?
            if (!data.bagOpen && data.bag.posY() > Data.LANE_Y) {
                ++data.numBagBounces;
                if (data.numBagBounces < 3) {
                    data.bag.setVelocity(-0.2f, -1f / data.numBagBounces);
                    data.bag.setPosY(Data.LANE_Y);
                } else {
                    data.bagOpen = true;
                    data.bag.setVelocity(Vector2f.ZERO);
                    changeState(STATE_STORK_LEAVES_SCENE, 3 * 60);
                }
            }
        }

        private void updateStateStorkLeavesScene(Data data) {
            data.stork.move();
            if (stateTimer.hasExpired()) {
                GameController.it().terminateCurrentState();
            }
        }
    }

    private Data data;
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

        data = new Data();
        data.msPacMan.setAnimations(new MsPacManGamePacAnimations(data.msPacMan, sheet));
        data.pacMan.setAnimations(new MsPacManGamePacAnimations(data.pacMan, sheet));
        storkAnimation = sheet.createStorkFlyingAnimation();
        storkAnimation.start();
        clapAnimation = new ClapperboardAnimation("3", "JUNIOR");
        clapAnimation.start();

        sceneController = new MsPacManCutScene3Controller();
        sceneController.changeState(MsPacManCutScene3Controller.STATE_FLAP, TickTimer.INDEFINITE);
    }

    @Override
    public void update() {
        sceneController.tick(data);
        clapAnimation.tick();
    }

    @Override
    public void drawSceneContent() {
        spriteRenderer.drawClapperBoard(g,
            context.assets().font("font.arcade", s(8)),
            context.assets().color("palette.pale"),
            clapAnimation, t(3), t(10));
        spriteRenderer.drawPac(g, data.msPacMan);
        spriteRenderer.drawPac(g, data.pacMan);
        spriteRenderer.drawEntitySprite(g, data.stork, storkAnimation.currentSprite());
        spriteRenderer.drawEntitySprite(g, data.bag, data.bagOpen ? sheet.juniorPacSprite() : sheet.blueBagSprite());
        drawLevelCounter(g);
    }
}