/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui.scene.GameScene2D;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.model.actors.Animations.ANIM_MR_PACMAN_MUNCHING;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 *
 * @author Armin Reichert
 */
public class CutScene2 extends GameScene2D {

    static final int UPPER_LANE_Y = TS * 12;
    static final int MIDDLE_LANE_Y = TS * 18;
    static final int LOWER_LANE_Y = TS * 24;

    private Pac pacMan;
    private Pac msPacMan;

    private SceneController sceneController;
    private MediaPlayer music;
    private ClapperboardAnimation clapAnimation;

    @Override
    public void bindGameActions() {
    }

    @Override
    public void doInit() {
        context.setScoreVisible(context.gameVariant() != GameVariant.MS_PACMAN_TENGEN);

        pacMan = new Pac();
        msPacMan = new Pac();

        music = context.sound().makeSound("intermission.2", 1, false);

        var spriteSheet = (MsPacManGameSpriteSheet) context.currentGameConfig().spriteSheet();
        msPacMan.setAnimations(new PacAnimations(spriteSheet));
        pacMan.setAnimations(new PacAnimations(spriteSheet));

        clapAnimation = new ClapperboardAnimation("2", "THE CHASE");
        clapAnimation.start();

        sceneController = new SceneController();
        sceneController.setState(SceneController.STATE_FLAP, 120);
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
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        MsPacManGameRenderer r = (MsPacManGameRenderer) gr;
        String assetKeyPrefix = context.currentGameConfig().assetKeyPrefix();
        Color color = context.assets().color(assetKeyPrefix + ".color.clapperboard");
        r.drawClapperBoard(r.scaledArcadeFont(TS), color, clapAnimation, t(3), t(10));
        r.drawAnimatedEntity(msPacMan);
        r.drawAnimatedEntity(pacMan);
        r.drawLevelCounter(context, size().x() - 4 * TS, size().y() - 2 * TS);
    }

    private class SceneController {

        static final byte STATE_FLAP = 0;
        static final byte STATE_CHASING = 1;

        byte state;
        final TickTimer stateTimer = new TickTimer("MsPacManCutScene2");

        void setState(byte state, long ticks) {
            this.state = state;
            stateTimer.reset(ticks);
            stateTimer.start();
        }

        void tick() {
            switch (state) {
                case STATE_FLAP -> updateStateFlap();
                case STATE_CHASING -> updateStateChasing();
                default -> throw new IllegalStateException("Illegal state: " + state);
            }
            stateTimer.doTick();
        }

        void updateStateFlap() {
            clapAnimation.tick();
            if (stateTimer.hasExpired()) {
                music.play();
                enterStateChasing();
            }
        }

        void enterStateChasing() {
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.selectAnimation(ANIM_MR_PACMAN_MUNCHING);
            pacMan.optAnimations().ifPresent(Animations::startCurrentAnimation);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.selectAnimation(Animations.ANIM_PAC_MUNCHING);
            msPacMan.optAnimations().ifPresent(Animations::startCurrentAnimation);

            setState(STATE_CHASING, TickTimer.INDEFINITE);
        }

        void updateStateChasing() {
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
                context.gameController().terminateCurrentState();
            }
            else {
                pacMan.move();
                msPacMan.move();
            }
        }
    }
}