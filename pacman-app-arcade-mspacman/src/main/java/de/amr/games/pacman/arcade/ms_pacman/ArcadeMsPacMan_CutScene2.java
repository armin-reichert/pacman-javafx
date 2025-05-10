/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.scene.media.MediaPlayer;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacMan_GameModel.createMsPacMan;
import static de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacMan_GameModel.createPacMan;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui.GameAssets.ARCADE_WHITE;
import static de.amr.games.pacman.ui.Globals.THE_SOUND;
import static de.amr.games.pacman.ui.Globals.THE_UI_CONFIGS;

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
    private ClapperboardAnimation clapperboardAnimation;

    @Override
    public void doInit() {
        game().scoreManager().setScoreVisible(true);

        pacMan = createPacMan();
        msPacMan = createMsPacMan();

        ArcadeMsPacMan_SpriteSheet spriteSheet = THE_UI_CONFIGS.current().spriteSheet();
        msPacMan.setAnimations(new ArcadeMsPacMan_PacAnimations(spriteSheet));
        pacMan.setAnimations(new ArcadeMsPacMan_PacAnimations(spriteSheet));

        clapperboardAnimation = new ClapperboardAnimation("2", "THE CHASE");
        clapperboardAnimation.start();

        music = THE_SOUND.createSound("intermission.2");

        setSceneState(STATE_CLAPPERBOARD, 120);
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
        gr.drawScores(game().scoreManager(), ARCADE_WHITE, arcadeFontScaledTS());
        if (gr instanceof ArcadeMsPacMan_GameRenderer r) {
            r.drawClapperBoard(clapperboardAnimation, tiles_to_px(3), tiles_to_px(10), arcadeFontScaledTS());
        }
        gr.drawAnimatedActor(msPacMan);
        gr.drawAnimatedActor(pacMan);
        gr.drawLevelCounter(game().levelCounter(), sizeInPx());
    }

    // Scene controller state machine

    private static final byte STATE_CLAPPERBOARD = 0;
    private static final byte STATE_CHASING = 1;

    private byte sceneState;
    private final TickTimer sceneTimer = new TickTimer("MsPacManCutScene2");

    private void setSceneState(byte state, long ticks) {
        this.sceneState = state;
        sceneTimer.reset(ticks);
        sceneTimer.start();
    }

    private void updateSceneState() {
        switch (sceneState) {
            case STATE_CLAPPERBOARD -> updateStateClapperboard();
            case STATE_CHASING -> updateStateChasing();
            default -> throw new IllegalStateException("Illegal state: " + sceneState);
        }
        sceneTimer.doTick();
    }

    private void updateStateClapperboard() {
        clapperboardAnimation.tick();
        if (sceneTimer.hasExpired()) {
            music.play();
            enterStateChasing();
        }
    }

    private void enterStateChasing() {
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.selectAnimation(ArcadeMsPacMan_PacAnimations.ANIM_PAC_MAN_MUNCHING);
        pacMan.startAnimation();
        msPacMan.setMoveDir(Direction.RIGHT);
        msPacMan.selectAnimation(PacAnimations.ANIM_MUNCHING);
        msPacMan.startAnimation();

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
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setSpeed(2.0f);
            msPacMan.setPosition(TS * (-8), MIDDLE_LANE_Y);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setSpeed(2.0f);
            pacMan.setPosition(TS * (-2), MIDDLE_LANE_Y);
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
            THE_GAME_CONTROLLER.letCurrentStateExpire();
        }
        else {
            pacMan.move();
            msPacMan.move();
        }
    }
}