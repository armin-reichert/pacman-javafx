/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.ms_pacman.MsPacManArcadeGame;
import de.amr.games.pacman.model.pacman.PacManArcadeGame;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.model.pacman.PacManArcadeGame.ARCADE_MAP_SIZE;

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
    private ClapperboardAnimation clapAnimation;

    private void startMusic() {
        int number  = context.gameState() == GameState.TESTING_CUT_SCENES
            ? GameState.TESTING_CUT_SCENES.getProperty("intermissionTestNumber")
            : context.game().intermissionNumberAfterLevel();
        context.sounds().playIntermissionSound(number);
    }

    @Override
    public void init() {
        context.setScoreVisible(context.gameVariant() != GameVariant.MS_PACMAN_TENGEN);

        pacMan = new Pac();
        msPacMan = new Pac();

        MsPacManGameSpriteSheet spriteSheet = (MsPacManGameSpriteSheet) context.currentGameSceneConfig().spriteSheet();
        msPacMan.setAnimations(new PacAnimations(spriteSheet));
        pacMan.setAnimations(new PacAnimations(spriteSheet));

        clapAnimation = new ClapperboardAnimation("2", "THE CHASE");
        clapAnimation.start();

        sceneController = new SceneController();
        sceneController.setState(SceneController.STATE_FLAP, 120);
    }

    @Override
    public void end() {
    }

    @Override
    public void update() {
        sceneController.tick();
    }

    @Override
    public Vector2f size() {
        return ARCADE_MAP_SIZE;
    }

    @Override
    public void drawSceneContent(GameRenderer renderer) {
        MsPacManGameRenderer r = (MsPacManGameRenderer) renderer;
        String assetPrefix = GameAssets2D.assetPrefix(context.gameVariant());
        Color color = context.assets().color(assetPrefix + ".color.clapperboard");
        r.drawClapperBoard(renderer.scaledArcadeFont(TS), color, clapAnimation, t(3), t(10));
        renderer.drawAnimatedEntity(msPacMan);
        renderer.drawAnimatedEntity(pacMan);
        renderer.drawLevelCounter(context.game().levelNumber(), context.game().isDemoLevel(),
            context.game().levelCounter(), size());
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
            stateTimer.tick();
        }

        void updateStateFlap() {
            clapAnimation.tick();
            if (stateTimer.hasExpired()) {
                startMusic();
                enterStateChasing();
            }
        }

        void enterStateChasing() {
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.selectAnimation(MsPacManArcadeGame.ANIM_MR_PACMAN_MUNCHING);
            pacMan.animations().ifPresent(Animations::startCurrentAnimation);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.selectAnimation(GameModel.ANIM_PAC_MUNCHING);
            msPacMan.animations().ifPresent(Animations::startCurrentAnimation);

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