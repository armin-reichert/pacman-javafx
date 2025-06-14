/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.PacManGames_UIConfig;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.uilib.animation.SingleSpriteActor;
import javafx.scene.media.MediaPlayer;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel.*;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_PacAnimationMap.PAC_MAN_MUNCHING;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.ui.PacManGames_Env.theSound;
import static de.amr.pacmanfx.ui.PacManGames_Env.theUI;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 *
 * @author Armin Reichert
 */
public class ArcadeMsPacMan_CutScene1 extends GameScene2D {

    private static final int UPPER_LANE_Y  = TS * 12;
    private static final int MIDDLE_LANE_Y = TS * 18;
    private static final int LOWER_LANE_Y  = TS * 24;

    private static final float SPEED_PAC_CHASING = 1.125f;
    private static final float SPEED_PAC_RISING = 0.75f;
    private static final float SPEED_GHOST_AFTER_COLLISION = 0.3f;
    private static final float SPEED_GHOST_CHASING = 1.25f;

    private Pac pacMan;
    private Pac msPacMan;
    private Ghost inky;
    private Ghost pinky;
    private SingleSpriteActor heart;
    private Clapperboard clapperboard;
    private MediaPlayer music;

    @Override
    public void doInit() {
        var spriteSheet = (ArcadeMsPacMan_SpriteSheet) theUI().configuration().spriteSheet();

        theGame().setScoreVisible(true);

        pacMan = createPacMan();
        msPacMan = createMsPacMan();
        inky = createCyanGhost();
        pinky = createPinkGhost();
        heart = new SingleSpriteActor(spriteSheet.sprite(SpriteID.HEART));

        final PacManGames_UIConfig config = theUI().configuration();
        msPacMan.setAnimations(config.createPacAnimations(msPacMan));
        pacMan.setAnimations(config.createPacAnimations(pacMan));
        inky.setAnimations(config.createGhostAnimations(inky));
        pinky.setAnimations(config.createGhostAnimations(pinky));

        clapperboard = new Clapperboard(spriteSheet, "1", "THEY MEET");
        clapperboard.setPosition(tiles_to_px(3), tiles_to_px(10));
        clapperboard.setFont(arcadeFont8());
        clapperboard.startAnimation();
        music = theSound().createSound("intermission.1");

        setState(STATE_CLAPPERBOARD, 120);
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        switch (sceneState) {
            case STATE_CLAPPERBOARD -> updateStateClapperboard();
            case STATE_CHASED_BY_GHOSTS -> updateStateChasedByGhosts();
            case STATE_COMING_TOGETHER -> updateStateComingTogether();
            case STATE_IN_HEAVEN -> updateStateInHeaven();
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
        gr().drawActor(clapperboard);
        gr().drawActor(msPacMan);
        gr().drawActor(pacMan);
        gr().drawActor(inky);
        gr().drawActor(pinky);
        gr().drawActor(heart);
        gr().drawLevelCounter(theGame().levelCounter(), sizeInPx());
    }

    // Scene controller state machine

    private static final byte STATE_CLAPPERBOARD = 0;
    private static final byte STATE_CHASED_BY_GHOSTS = 1;
    private static final byte STATE_COMING_TOGETHER = 2;
    private static final byte STATE_IN_HEAVEN = 3;

    private byte sceneState;
    private final TickTimer sceneTimer = new TickTimer("MsPacMan_CutScene1");

    private void setState(byte state, long ticks) {
        sceneState = state;
        sceneTimer.reset(ticks);
        sceneTimer.start();
    }

    private void updateStateClapperboard() {
        clapperboard.tick();
        if (sceneTimer.atSecond(1)) {
            music.play();
        } else if (sceneTimer.hasExpired()) {
            enterStateChasedByGhosts();
        }
    }

    private void enterStateChasedByGhosts() {
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.setPosition(TS * (-2), UPPER_LANE_Y);
        pacMan.setSpeed(SPEED_PAC_CHASING);
        pacMan.playAnimation(PAC_MAN_MUNCHING);
        pacMan.show();

        inky.setMoveAndWishDir(Direction.RIGHT);
        inky.setPosition(pacMan.x() - 6 * TS, pacMan.y());
        inky.setSpeed(SPEED_GHOST_CHASING);
        inky.playAnimation(ANIM_GHOST_NORMAL);
        inky.show();

        msPacMan.setMoveDir(Direction.LEFT);
        msPacMan.setPosition(TS * 30, LOWER_LANE_Y);
        msPacMan.setSpeed(SPEED_PAC_CHASING);
        msPacMan.playAnimation(ANIM_PAC_MUNCHING);
        msPacMan.show();

        pinky.setMoveAndWishDir(Direction.LEFT);
        pinky.setPosition(msPacMan.x() + 6 * TS, msPacMan.y());
        pinky.setSpeed(SPEED_GHOST_CHASING);
        pinky.playAnimation(ANIM_GHOST_NORMAL);
        pinky.show();

        setState(STATE_CHASED_BY_GHOSTS, TickTimer.INDEFINITE);
    }

    private void updateStateChasedByGhosts() {
        if (inky.x() > TS * 30) {
            enterStateComingTogether();
        }
        else {
            pacMan.move();
            msPacMan.move();
            inky.move();
            pinky.move();
        }
    }

    private void enterStateComingTogether() {
        msPacMan.setPosition(TS * (-3), MIDDLE_LANE_Y);
        msPacMan.setMoveDir(Direction.RIGHT);

        pinky.setPosition(msPacMan.x() - 5 * TS, msPacMan.y());
        pinky.setMoveAndWishDir(Direction.RIGHT);

        pacMan.setPosition(TS * 31, MIDDLE_LANE_Y);
        pacMan.setMoveDir(Direction.LEFT);

        inky.setPosition(pacMan.x() + 5 * TS, pacMan.y());
        inky.setMoveAndWishDir(Direction.LEFT);

        setState(STATE_COMING_TOGETHER, TickTimer.INDEFINITE);
    }

    private void updateStateComingTogether() {
        // Pac-Man and Ms. Pac-Man reach end position?
        if (pacMan.moveDir() == Direction.UP && pacMan.y() < UPPER_LANE_Y) {
            enterStateInHeaven();
        }

        // Pac-Man and Ms. Pac-Man meet?
        else if (pacMan.moveDir() == Direction.LEFT && pacMan.x() - msPacMan.x() < TS * 2) {
            pacMan.setMoveDir(Direction.UP);
            pacMan.setSpeed(SPEED_PAC_RISING);
            msPacMan.setMoveDir(Direction.UP);
            msPacMan.setSpeed(SPEED_PAC_RISING);
        }

        // Inky and Pinky collide?
        else if (inky.moveDir() == Direction.LEFT && inky.x() - pinky.x() < TS * 2) {
            inky.setMoveAndWishDir(Direction.RIGHT);
            inky.setSpeed(SPEED_GHOST_AFTER_COLLISION);
            inky.setVelocity(inky.velocity().minus(0, 2.0f));
            inky.setAcceleration(0, 0.4f);

            pinky.setMoveAndWishDir(Direction.LEFT);
            pinky.setSpeed(SPEED_GHOST_AFTER_COLLISION);
            pinky.setVelocity(pinky.velocity().minus(0, 2.0f));
            pinky.setAcceleration(0, 0.4f);
        }

        else {
            pacMan.move();
            msPacMan.move();
            inky.move();
            pinky.move();

            // Collision with ground?
            if (inky.y() > MIDDLE_LANE_Y) {
                inky.setY(MIDDLE_LANE_Y);
                inky.setAcceleration(Vector2f.ZERO);
            }
            if (pinky.y() > MIDDLE_LANE_Y) {
                pinky.setY(MIDDLE_LANE_Y);
                pinky.setAcceleration(Vector2f.ZERO);
            }
        }
    }

    private void enterStateInHeaven() {
        pacMan.setSpeed(0);
        pacMan.setMoveDir(Direction.LEFT);
        pacMan.stopAnimation();
        pacMan.resetAnimation();

        msPacMan.setSpeed(0);
        msPacMan.setMoveDir(Direction.RIGHT);
        msPacMan.stopAnimation();
        msPacMan.resetAnimation();

        inky.hide();
        pinky.hide();

        heart.setPosition((pacMan.x() + msPacMan.x()) * 0.5f, pacMan.y() - TS * 2);
        heart.show();

        setState(STATE_IN_HEAVEN, 3 * NUM_TICKS_PER_SEC);
    }

    private void updateStateInHeaven() {
        if (sceneTimer.hasExpired()) {
            theGameController().letCurrentGameStateExpire();
        }
    }
}