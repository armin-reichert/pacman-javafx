/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.ActorAnimations;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.scene.media.MediaPlayer;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.model.actors.ActorAnimations.ANIM_MR_PACMAN_MUNCHING;
import static de.amr.games.pacman.ui.Globals.THE_UI;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 *
 * @author Armin Reichert
 */
public class CutScene2 extends GameScene2D {

    private static final int UPPER_LANE_Y  = TS * 12;
    private static final int MIDDLE_LANE_Y = TS * 18;
    private static final int LOWER_LANE_Y  = TS * 24;

    private Pac pacMan;
    private Pac msPacMan;

    private MediaPlayer music;
    private ClapperboardAnimation clapAnimation;

    @Override
    public void bindGameActions() {
    }

    @Override
    public void doInit() {
        game().setScoreVisible(true);

        pacMan = new Pac();
        msPacMan = new Pac();

        music = THE_UI.sound().makeSound("intermission.2");

        var spriteSheet = (ArcadeMsPacMan_SpriteSheet) THE_UI.configurations().current().spriteSheet();
        msPacMan.setAnimations(new PacAnimations(spriteSheet));
        pacMan.setAnimations(new PacAnimations(spriteSheet));

        clapAnimation = new ClapperboardAnimation("2", "THE CHASE");
        clapAnimation.start();

        setSceneState(STATE_FLAP, 120);
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        updateSceneState();
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        if (gr instanceof ArcadeMsPacMan_GameRenderer r) {
            r.drawClapperBoard(clapAnimation, tiles2Px(3), tiles2Px(10));
        }
        gr.drawAnimatedActor(msPacMan);
        gr.drawAnimatedActor(pacMan);
        gr.drawLevelCounter(sizeInPx().x() - 4 * TS, sizeInPx().y() - 2 * TS);
    }

    // Scene controller state machine

    private static final byte STATE_FLAP = 0;
    private static final byte STATE_CHASING = 1;

    private byte state;
    private final TickTimer stateTimer = new TickTimer("MsPacManCutScene2");

    private void setSceneState(byte state, long ticks) {
        this.state = state;
        stateTimer.reset(ticks);
        stateTimer.start();
    }

    private void updateSceneState() {
        switch (state) {
            case STATE_FLAP -> updateStateFlap();
            case STATE_CHASING -> updateStateChasing();
            default -> throw new IllegalStateException("Illegal state: " + state);
        }
        stateTimer.doTick();
    }

    private void updateStateFlap() {
        clapAnimation.tick();
        if (stateTimer.hasExpired()) {
            music.play();
            enterStateChasing();
        }
    }

    private void enterStateChasing() {
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.selectAnimation(ANIM_MR_PACMAN_MUNCHING);
        pacMan.startAnimation();
        msPacMan.setMoveDir(Direction.RIGHT);
        msPacMan.selectAnimation(ActorAnimations.ANIM_PAC_MUNCHING);
        msPacMan.startAnimation();

        setSceneState(STATE_CHASING, TickTimer.INDEFINITE);
    }

    private void updateStateChasing() {
        if (stateTimer.atSecond(4.5)) {
            pacMan.setPosition(TS * (-2), UPPER_LANE_Y);
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setSpeed(2.0f);
            pacMan.show();
            msPacMan.setPosition(TS * (-8), UPPER_LANE_Y);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setSpeed(2.0f);
            msPacMan.show();
        }
        else if (stateTimer.atSecond(9)) {
            pacMan.setPosition(TS * 36, LOWER_LANE_Y);
            pacMan.setMoveDir(Direction.LEFT);
            pacMan.setSpeed(2.0f);
            msPacMan.setPosition(TS * 30, LOWER_LANE_Y);
            msPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setSpeed(2.0f);
        }
        else if (stateTimer.atSecond(13.5)) {
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setSpeed(2.0f);
            msPacMan.setPosition(TS * (-8), MIDDLE_LANE_Y);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setSpeed(2.0f);
            pacMan.setPosition(TS * (-2), MIDDLE_LANE_Y);
        }
        else if (stateTimer.atSecond(17.5)) {
            pacMan.setPosition(TS * 42, UPPER_LANE_Y);
            pacMan.setMoveDir(Direction.LEFT);
            pacMan.setSpeed(4.0f);
            msPacMan.setPosition(TS * 30, UPPER_LANE_Y);
            msPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setSpeed(4.0f);
        }
        else if (stateTimer.atSecond(18.5)) {
            pacMan.setPosition(TS * (-2), LOWER_LANE_Y);
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setSpeed(4.0f);
            msPacMan.setPosition(TS * (-14), LOWER_LANE_Y);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setSpeed(4.0f);
        }
        else if (stateTimer.atSecond(23)) {
            THE_GAME_CONTROLLER.terminateCurrentState();
        }
        else {
            pacMan.move();
            msPacMan.move();
        }
    }
}