/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Animations;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import javafx.scene.media.MediaPlayer;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel.createMsPacMan;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel.createPacMan;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.ui.GameAssets.ARCADE_WHITE;
import static de.amr.pacmanfx.ui.PacManGamesEnv.theSound;
import static de.amr.pacmanfx.ui.PacManGamesEnv.theUIConfig;

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
public class ArcadeMsPacMan_CutScene3 extends GameScene2D {

    private static final int LANE_Y = TS * 24;

    private Pac pacMan;
    private Pac msPacMan;
    private Actor stork;
    private Actor bag;
    private boolean bagOpen;
    private int numBagBounces;

    private MediaPlayer music;
    private ClapperboardAnimation clapperboardAnimation;
    private SpriteAnimation storkAnimation;

    @Override
    public void doInit() {
        theGame().scoreManager().setScoreVisible(true);

        pacMan = createPacMan();
        msPacMan = createMsPacMan();
        stork = new Actor();
        bag = new Actor();

        ArcadeMsPacMan_SpriteSheet spriteSheet = theUIConfig().current().spriteSheet();
        msPacMan.setAnimations(new ArcadeMsPacMan_PacAnimations(spriteSheet));
        pacMan.setAnimations(new ArcadeMsPacMan_PacAnimations(spriteSheet));

        storkAnimation = spriteSheet.createStorkFlyingAnimation();
        storkAnimation.play();

        clapperboardAnimation = new ClapperboardAnimation("3", "JUNIOR");
        clapperboardAnimation.start();

        music = theSound().createSound("intermission.3");
        setSceneState(STATE_CLAPPERBOARD, TickTimer.INDEFINITE);
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        switch (sceneState) {
            case STATE_CLAPPERBOARD -> updateStateClapperboard();
            case STATE_DELIVER_JUNIOR -> updateStateDeliverJunior();
            case STATE_STORK_LEAVES_SCENE -> updateStateStorkLeavesScene();
            default -> throw new IllegalStateException("Illegal scene state: " + sceneState);
        }
        sceneTimer.doTick();
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        gr.drawScores(theGame().scoreManager(), ARCADE_WHITE, arcadeFontScaledTS());
        if (gr instanceof ArcadeMsPacMan_GameRenderer r) {
            // Note: in Ms. Pac-Man XXL another renderer is used!
            r.drawClapperBoard(clapperboardAnimation, tiles_to_px(3), tiles_to_px(10), arcadeFontScaledTS());
        }
        gr.drawActor(msPacMan);
        gr.drawActor(pacMan);
        gr.drawActorSprite(stork, storkAnimation.currentSprite());
        gr.drawActorSprite(bag, bagOpen ? ArcadeMsPacMan_SpriteSheet.JUNIOR_PAC_SPRITE : ArcadeMsPacMan_SpriteSheet.BLUE_BAG_SPRITE);
        gr.drawLevelCounter(theGame().levelCounter(), sizeInPx());
    }

    // Scene controller state machine

    private static final byte STATE_CLAPPERBOARD = 0;
    private static final byte STATE_DELIVER_JUNIOR = 1;
    private static final byte STATE_STORK_LEAVES_SCENE = 2;

    private byte sceneState;
    private final TickTimer sceneTimer = new TickTimer("MsPacMan_CutScene3");

    private void setSceneState(byte state, long ticks) {
        sceneState = state;
        sceneTimer.reset(ticks);
        sceneTimer.start();
    }

    private void updateStateClapperboard() {
        clapperboardAnimation.tick();
        if (sceneTimer.atSecond(1)) {
            music.play();
        } else if (sceneTimer.atSecond(3)) {
            enterStateDeliverJunior();
        }
    }

    private void enterStateDeliverJunior() {
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.setPosition(TS * 3, LANE_Y - 4);
        pacMan.selectAnimation(ArcadeMsPacMan_PacAnimations.PAC_MAN_MUNCHING);
        pacMan.show();

        msPacMan.setMoveDir(Direction.RIGHT);
        msPacMan.setPosition(TS * 5, LANE_Y - 4);
        msPacMan.selectAnimation(Animations.ANY_PAC_MUNCHING);
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

        setSceneState(STATE_DELIVER_JUNIOR, TickTimer.INDEFINITE);
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
                setSceneState(STATE_STORK_LEAVES_SCENE, 3 * 60);
            }
        }
    }

    private void updateStateStorkLeavesScene() {
        stork.move();
        if (sceneTimer.hasExpired()) {
            theGameController().letCurrentGameStateExpire();
        }
    }
}