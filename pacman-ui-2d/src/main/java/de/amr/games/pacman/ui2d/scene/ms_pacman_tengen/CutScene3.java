/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.ms_pacman.MsPacManArcadeGame;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManTengenGameSceneConfig.NES_RESOLUTION_X;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManTengenGameSceneConfig.NES_RESOLUTION_Y;

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
public class CutScene3 extends GameScene2D {

    static final int LANE_Y = TS * 24;

    private SceneController sceneController;
    private MediaPlayer music;
    private Pac mrPacMan;
    private Pac msPacMan;
    private Entity stork;
    private Entity bag;
    private boolean bagOpen;
    private int numBagBounces;

    private ClapperboardAnimation clapAnimation;
    private SpriteAnimation storkAnimation;

    @Override
    public void bindGameActions() {
        bind(context -> context.gameController().terminateCurrentState(), context.joypad().keyCombination(NES.Joypad.START));
    }

    @Override
    public void doInit() {
        context.setScoreVisible(false);

        mrPacMan = new Pac();
        msPacMan = new Pac();
        stork = new Entity();
        bag = new Entity();

        music = context.sound().makeSound("intermission.3",1.0, false);

        var spriteSheet = (MsPacManTengenGameSpriteSheet) context.currentGameSceneConfig().spriteSheet();
        mrPacMan.setAnimations(new PacAnimations(spriteSheet));
        msPacMan.setAnimations(new PacAnimations(spriteSheet));

        storkAnimation = spriteSheet.createStorkFlyingAnimation();
        storkAnimation.start();

        clapAnimation = new ClapperboardAnimation();
        clapAnimation.start();

        sceneController = new SceneController();
        sceneController.setState(SceneController.STATE_FLAP, TickTimer.INDEFINITE);
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        sceneController.tick();
    }

    @Override
    public Vector2f size() {
        return new Vector2f(NES_RESOLUTION_X, NES_RESOLUTION_Y);
    }

    @Override
    public void drawSceneContent(GameRenderer renderer) {
        MsPacManTengenGameRenderer r = (MsPacManTengenGameRenderer) renderer;
        String assetPrefix = GameAssets2D.assetPrefix(context.gameVariant());
        Color color = r.assets().color(assetPrefix + ".color.clapperboard");
        r.drawClapperBoard(clapAnimation, "JUNIOR", 3, r.scaledArcadeFont(TS), color, t(3), t(10));
        r.drawAnimatedEntity(msPacMan);
        r.drawAnimatedEntity(mrPacMan);
        r.drawStork(storkAnimation, stork, bag.acceleration().y() != 0);
        r.drawSprite(bag, bagOpen
            ? MsPacManTengenGameSpriteSheet.JUNIOR_PAC_SPRITE
            : MsPacManTengenGameSpriteSheet.BLUE_BAG_SPRITE);
        r.setLevelNumberBoxesVisible(false);
        if (context.game().level().isPresent()) {
            // avoid exception in cut scene test mode
            r.drawLevelCounter(context, size());
        }
    }

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
            stateTimer.doTick();
        }

        void updateStateFlap() {
            clapAnimation.tick();
            if (stateTimer.atSecond(0)) {
                music.play();
            } else if (!clapAnimation.isRunning()) {
                enterStateDeliverJunior();
            }
        }

        void enterStateDeliverJunior() {
            mrPacMan.setMoveDir(Direction.RIGHT);
            mrPacMan.setPosition(TS * 3, LANE_Y - 4);
            mrPacMan.selectAnimation(MsPacManArcadeGame.ANIM_MR_PACMAN_MUNCHING);
            mrPacMan.show();

            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setPosition(TS * 5, LANE_Y - 4);
            msPacMan.selectAnimation(GameModel.ANIM_PAC_MUNCHING);
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
}