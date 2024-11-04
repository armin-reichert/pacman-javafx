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
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.ms_pacman.MsPacManArcadeGame;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.scene.ms_pacman.ClapperboardAnimation;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfiguration.NES_RESOLUTION_X;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfiguration.NES_RESOLUTION_Y;

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
    private MediaPlayer music;
    private SceneController sceneController;
    private ClapperboardAnimation clapAnimation;

    @Override
    public void bindGameActions() {
        bind(context -> context.gameController().terminateCurrentState(), context.joypadInput().key(NES.Joypad.START));
    }

    @Override
    public void doInit() {
        context.setScoreVisible(false);

        pacMan = new Pac();
        msPacMan = new Pac();

        var spriteSheet = (TengenMsPacManGameSpriteSheet) context.currentGameSceneConfig().spriteSheet();
        msPacMan.setAnimations(new PacAnimations(spriteSheet));
        pacMan.setAnimations(new PacAnimations(spriteSheet));

        clapAnimation = new ClapperboardAnimation("2", "THE CHASE");
        clapAnimation.start();

        music =context.sound().createPlayer(context.gameVariant(), context.assets(), "intermission.2",1.0, false);

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
        return new Vector2f(NES_RESOLUTION_X, NES_RESOLUTION_Y);
    }

    @Override
    public void drawSceneContent(GameRenderer renderer) {
        TengenMsPacManGameRenderer r = (TengenMsPacManGameRenderer) renderer;
        String assetPrefix = GameAssets2D.assetPrefix(context.gameVariant());
        Color color = r.assets().color(assetPrefix + ".color.clapperboard"); //TODO check
        r.drawClapperBoard(r.scaledArcadeFont(TS), color, clapAnimation, t(3), t(10));
        r.drawAnimatedEntity(msPacMan);
        r.drawAnimatedEntity(pacMan);
        r.drawLevelCounter(context, size());
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
                music.play();
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