/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameSounds;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.variant.ms_pacman.ClapperboardAnimation;
import de.amr.games.pacman.ui2d.variant.ms_pacman.MsPacManGamePacAnimations;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.ui2d.GameAssets2D.PALETTE_PALE;

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

    static final int LANE_Y = TS * 24;

    private class SceneController {

        static final byte STATE_FLAP = 0;
        static final byte STATE_DELIVER_JUNIOR = 1;
        static final byte STATE_STORK_LEAVES_SCENE = 2;

        byte state;
        final TickTimer stateTimer = new TickTimer("MsPacManCutScene3");

        void setState(byte state, long ticks) {
            this.state = state;
            stateTimer.reset(ticks);
            stateTimer.start();
        }

        void tick() {
            switch (state) {
                case STATE_FLAP -> updateStateFlap();
                case STATE_DELIVER_JUNIOR -> updateStateDeliverJunior();
                case STATE_STORK_LEAVES_SCENE -> updateStateStorkLeavesScene();
                default -> throw new IllegalStateException("Illegal state: " + state);
            }
            stateTimer.tick();
        }

        void updateStateFlap() {
            clapAnimation.tick();
            if (stateTimer.atSecond(1)) {
                startMusic();
            } else if (stateTimer.atSecond(3)) {
                enterStateDeliverJunior();
            }
        }

        void enterStateDeliverJunior() {
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

            setState(STATE_DELIVER_JUNIOR, TickTimer.INDEFINITE);
        }

        void updateStateDeliverJunior() {
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
                    setState(STATE_STORK_LEAVES_SCENE, 3 * 60);
                }
            }
        }

        void updateStateStorkLeavesScene() {
            stork.move();
            if (stateTimer.hasExpired()) {
                context.gameController().terminateCurrentState();
            }
        }
    }

    private SceneController sceneController;

    private Pac pacMan;
    private Pac msPacMan;
    private Entity stork;
    private Entity bag;
    private boolean bagOpen;
    private int numBagBounces;

    private GameSpriteSheet spriteSheet;
    private ClapperboardAnimation clapAnimation;
    private SpriteAnimation storkAnimation;

    private void startMusic() {
        int number  = context.gameState() == GameState.INTERMISSION_TEST
            ? GameState.INTERMISSION_TEST.getProperty("intermissionTestNumber")
            : context.game().intermissionNumber(context.game().levelNumber());
        GameSounds.playIntermissionSound(number);
    }

    @Override
    public boolean isCreditVisible() {
        return !context.game().hasCredit();
    }

    @Override
    public void init() {
        super.init();
        context.setScoreVisible(true);

        pacMan = new Pac();
        msPacMan = new Pac();
        stork = new Entity();
        bag = new Entity();

        spriteSheet = context.spriteSheet();
        //TODO use Tengen sprite sheet
        if (context.game().variant() == GameVariant.MS_PACMAN_TENGEN) {
            spriteSheet = context.spriteSheet(GameVariant.MS_PACMAN);
        }

        msPacMan.setAnimations(new MsPacManGamePacAnimations(spriteSheet));
        pacMan.setAnimations(new MsPacManGamePacAnimations(spriteSheet));

        storkAnimation = spriteSheet.createStorkFlyingAnimation();
        storkAnimation.start();

        clapAnimation = new ClapperboardAnimation("3", "JUNIOR");
        clapAnimation.start();

        sceneController = new SceneController();
        sceneController.setState(SceneController.STATE_FLAP, TickTimer.INDEFINITE);
    }

    @Override
    public void update() {
        sceneController.tick();
    }

    @Override
    public void drawSceneContent(GameWorldRenderer renderer) {
        renderer.drawClapperBoard(g,
            spriteSheet,
            context.assets().font("font.arcade", scaled(8)),
            PALETTE_PALE,
            clapAnimation, t(3), t(10));
        renderer.drawAnimatedEntity(g, msPacMan);
        renderer.drawAnimatedEntity(g, pacMan);
        renderer.drawEntitySprite(g, stork, spriteSheet, storkAnimation.currentSprite());
        renderer.drawEntitySprite(g, bag, spriteSheet,
            bagOpen ? spriteSheet.juniorPacSprite() : spriteSheet.blueBagSprite());
    }
}