/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.media.MediaPlayer;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel.createMsPacMan;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel.createPacMan;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_PacAnimationMap.PAC_MAN_MUNCHING;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.ui.PacManGames.theSound;
import static de.amr.pacmanfx.ui.PacManGames.theUI;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 *
 * @author Armin Reichert
 */
public class ArcadeMsPacMan_CutScene2 extends GameScene2D {

    private static final int UPPER_LANE_Y  = TS * 12;
    private static final int MIDDLE_LANE_Y = TS * 18;
    private static final int LOWER_LANE_Y  = TS * 24;

    private Pac pacMan;
    private Pac msPacMan;

    private MediaPlayer music;
    private Clapperboard clapperboard;

    @Override
    public void doInit() {
        theGame().hud().showScore(true);
        theGame().hud().showLevelCounter(true);
        theGame().hud().showLivesCounter(false);

        pacMan = createPacMan();
        msPacMan = createMsPacMan();

        msPacMan.setAnimations(theUI().configuration().createPacAnimations(msPacMan));
        pacMan.setAnimations(theUI().configuration().createPacAnimations(pacMan));

        clapperboard = new Clapperboard("2", "THE CHASE");
        clapperboard.setPosition(tiles_to_px(3), tiles_to_px(10));
        clapperboard.setFont(scaledArcadeFont8());
        clapperboard.startAnimation();

        music = theSound().createSound("intermission.2");

        setSceneState(STATE_CLAPPERBOARD, 120);
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        switch (sceneState) {
            case STATE_CLAPPERBOARD -> updateStateClapperboard();
            case STATE_CHASING -> updateStateChasing();
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
    }

    // Scene controller state machine

    private static final byte STATE_CLAPPERBOARD = 0;
    private static final byte STATE_CHASING = 1;

    private byte sceneState;
    private final TickTimer sceneTimer = new TickTimer("MsPacMan_CutScene2");

    private void setSceneState(byte state, long ticks) {
        sceneState = state;
        sceneTimer.reset(ticks);
        sceneTimer.start();
    }

    private void updateStateClapperboard() {
        clapperboard.tick();
        if (sceneTimer.hasExpired()) {
            music.play();
            enterStateChasing();
        }
    }

    private void enterStateChasing() {
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.playAnimation(PAC_MAN_MUNCHING);

        msPacMan.setMoveDir(Direction.RIGHT);
        msPacMan.playAnimation(ANIM_PAC_MUNCHING);

        setSceneState(STATE_CHASING, TickTimer.INDEFINITE);
    }

    private void updateStateChasing() {
        if (sceneTimer.atSecond(4.5)) {
            pacMan.setPosition(TS * (-2), UPPER_LANE_Y);
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setSpeed(2.0f);
            pacMan.show();

            msPacMan.setPosition(TS * (-8), UPPER_LANE_Y);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setSpeed(2.0f);
            msPacMan.show();
        }
        else if (sceneTimer.atSecond(9)) {
            pacMan.setPosition(TS * 36, LOWER_LANE_Y);
            pacMan.setMoveDir(Direction.LEFT);
            pacMan.setSpeed(2.0f);

            msPacMan.setPosition(TS * 30, LOWER_LANE_Y);
            msPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setSpeed(2.0f);
        }
        else if (sceneTimer.atSecond(13.5)) {
            pacMan.setPosition(TS * (-2), MIDDLE_LANE_Y);
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setSpeed(2.0f);

            msPacMan.setPosition(TS * (-8), MIDDLE_LANE_Y);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setSpeed(2.0f);
        }
        else if (sceneTimer.atSecond(17.5)) {
            pacMan.setPosition(TS * 42, UPPER_LANE_Y);
            pacMan.setMoveDir(Direction.LEFT);
            pacMan.setSpeed(4.0f);

            msPacMan.setPosition(TS * 30, UPPER_LANE_Y);
            msPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setSpeed(4.0f);
        }
        else if (sceneTimer.atSecond(18.5)) {
            pacMan.setPosition(TS * (-2), LOWER_LANE_Y);
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setSpeed(4.0f);

            msPacMan.setPosition(TS * (-14), LOWER_LANE_Y);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setSpeed(4.0f);
        }
        else if (sceneTimer.atSecond(23)) {
            theGameController().letCurrentGameStateExpire();
        }
        else {
            pacMan.move();
            msPacMan.move();
        }
    }
}