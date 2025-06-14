/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.PacManGames_UIConfig;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.media.MediaPlayer;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel.createMsPacMan;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel.createPacMan;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_PacAnimationMap.PAC_MAN_MUNCHING;
import static de.amr.pacmanfx.arcade.ms_pacman.SpriteID.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.ui.PacManGames_Env.theSound;
import static de.amr.pacmanfx.ui.PacManGames_Env.theUI;

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
    private Clapperboard clapperboard;
    private SpriteAnimation storkAnimation;

    @Override
    public void doInit() {
        var spriteSheet = (ArcadeMsPacMan_SpriteSheet) theUI().configuration().spriteSheet();
        theGame().setScoreVisible(true);

        pacMan = createPacMan();
        msPacMan = createMsPacMan();
        stork = new Actor();
        bag = new Actor();

        PacManGames_UIConfig config = theUI().configuration();
        msPacMan.setAnimations(config.createPacAnimations(msPacMan));
        pacMan.setAnimations(config.createPacAnimations(pacMan));

        storkAnimation =  SpriteAnimation.createAnimation().ofSprites(spriteSheet.spriteSeq(STORK)).frameTicks(8).endless();
        storkAnimation.play();

        clapperboard = new Clapperboard(spriteSheet, "3", "JUNIOR");
        clapperboard.setPosition(tiles_to_px(3), tiles_to_px(10));
        clapperboard.setFont(arcadeFont8());
        clapperboard.startAnimation();

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
        @SuppressWarnings("unchecked") SpriteSheet<SpriteID> spriteSheet = (SpriteSheet<SpriteID>) theUI().configuration().spriteSheet();
        if (gr() instanceof ArcadeMsPacMan_GameRenderer r) { // TODO handle case of VectorGraphicsGameRenderer!
            r.drawClapperBoard(clapperboard);
        }
        gr().drawActor(msPacMan);
        gr().drawActor(pacMan);
        gr().drawActorSprite(stork, (Sprite) storkAnimation.currentSprite());
        gr().drawActorSprite(bag, bagOpen ? spriteSheet.sprite(JUNIOR_PAC) : spriteSheet.sprite(BLUE_BAG));
        gr().drawLevelCounter(theGame().levelCounter(), sizeInPx());
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
        clapperboard.tick();
        if (sceneTimer.atSecond(1)) {
            music.play();
        } else if (sceneTimer.atSecond(3)) {
            enterStateDeliverJunior();
        }
    }

    private void enterStateDeliverJunior() {
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.setPosition(TS * 3, LANE_Y - 4);
        pacMan.selectAnimation(PAC_MAN_MUNCHING);
        pacMan.show();

        msPacMan.setMoveDir(Direction.RIGHT);
        msPacMan.setPosition(TS * 5, LANE_Y - 4);
        msPacMan.selectAnimation(ANIM_PAC_MUNCHING);
        msPacMan.show();

        stork.setPosition(TS * 30, TS * 12);
        stork.setVelocity(-0.8f, 0);
        stork.show();

        bag.setPosition(stork.x() - 14, stork.y() + 3);
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
        if (!bagOpen && bag.y() > LANE_Y) {
            ++numBagBounces;
            if (numBagBounces < 3) {
                bag.setVelocity(-0.2f, -1f / numBagBounces);
                bag.setY(LANE_Y);
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