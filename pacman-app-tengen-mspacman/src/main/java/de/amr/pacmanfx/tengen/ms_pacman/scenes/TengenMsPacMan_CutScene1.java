/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.PacManGames_UIConfig;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.uilib.animation.SingleSpriteActor;
import javafx.scene.media.MediaPlayer;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.*;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_PacAnimationMap.ANIM_PAC_MAN_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE;
import static de.amr.pacmanfx.ui.PacManGames.*;
import static de.amr.pacmanfx.ui.PacManGames_UI.ACTION_LET_GAME_STATE_EXPIRE;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 *
 * @author Armin Reichert
 */
public class TengenMsPacMan_CutScene1 extends GameScene2D {

    private static final int UPPER_LANE   = TS * 8;
    private static final int LOWER_LANE   = TS * 24;
    private static final int MIDDLE_LANE  = TS * 16;
    private static final int LEFT_BORDER  = TS;
    private static final int RIGHT_BORDER = TS * 30;

    private static final float SPEED_CHASING = 2.0f;
    private static final float SPEED_RISING = 1.0f;
    private static final float SPEED_GHOST_AFTER_COLLISION = 0.5f;

    private MediaPlayer music;
    private Clapperboard clapperboard;
    private Pac pacMan;
    private Pac msPacMan;
    private Ghost inky;
    private Ghost pinky;
    private SingleSpriteActor heart;

    private boolean collided;
    private int t;

    @Override
    public void doInit() {
        t = -1;

        theGame().hud().hideScore();
        theGame().hud().showLevelCounter();
        theGame().hud().hideLivesCounter();

        bindActionToKeyCombination(ACTION_LET_GAME_STATE_EXPIRE, theJoypad().key(JoypadButton.START));
        music = theSound().createSound("intermission.1");

        PacManGames_UIConfig config = theUI().configuration();
        var spriteSheet = (TengenMsPacMan_SpriteSheet) config.spriteSheet();

        clapperboard = new Clapperboard(spriteSheet, 1, "THEY MEET");
        clapperboard.setPosition(3 * TS, 10 * TS);
        clapperboard.setFont(arcadeFont8());
        msPacMan = createMsPacMan();
        msPacMan.setAnimations(config.createPacAnimations(msPacMan));
        pacMan = createPacMan();
        pacMan.setAnimations(config.createPacAnimations(pacMan));
        inky = createGhost(CYAN_GHOST_BASHFUL);
        inky.setAnimations(config.createGhostAnimations(inky));
        pinky = createGhost(PINK_GHOST_SPEEDY);
        pinky.setAnimations(config.createGhostAnimations(pinky));
        heart = new SingleSpriteActor(spriteSheet.sprite(SpriteID.HEART));
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        t += 1;
        if (t == 0) {
            clapperboard.show();
            clapperboard.startAnimation();
            music.play();
        }
        else if (t == 130) {
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setPosition(LEFT_BORDER, UPPER_LANE);
            pacMan.setSpeed(SPEED_CHASING);
            pacMan.playAnimation(ANIM_PAC_MAN_MUNCHING);
            pacMan.show();

            msPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
            msPacMan.setSpeed(SPEED_CHASING);
            msPacMan.playAnimation(ANIM_PAC_MUNCHING);
            msPacMan.show();
        }
        else if (t == 160) {
            inky.setMoveAndWishDir(Direction.RIGHT);
            inky.setPosition(LEFT_BORDER, UPPER_LANE);
            inky.setSpeed(SPEED_CHASING);
            inky.playAnimation(ANIM_GHOST_NORMAL);
            inky.show();

            pinky.setMoveAndWishDir(Direction.LEFT);
            pinky.setPosition(RIGHT_BORDER, LOWER_LANE);
            pinky.setSpeed(SPEED_CHASING);
            pinky.playAnimation(ANIM_GHOST_NORMAL);
            pinky.show();

            collided = false;
        }
        else if (t == 400) {
            msPacMan.setPosition(LEFT_BORDER, MIDDLE_LANE);
            msPacMan.setMoveDir(Direction.RIGHT);

            pacMan.setPosition(RIGHT_BORDER, MIDDLE_LANE);
            pacMan.setMoveDir(Direction.LEFT);

            pinky.setPosition(msPacMan.x() - TS * 11, msPacMan.y());
            pinky.setMoveAndWishDir(Direction.RIGHT);

            inky.setPosition(pacMan.x() + TS * 11, pacMan.y());
            inky.setMoveAndWishDir(Direction.LEFT);
        }
        else if (t == 454) {
            pacMan.setMoveDir(Direction.UP);
            pacMan.setSpeed(SPEED_RISING);
            msPacMan.setMoveDir(Direction.UP);
            msPacMan.setSpeed(SPEED_RISING);
        }
        else if (t == 498) {
            collided = true;

            inky.setMoveAndWishDir(Direction.RIGHT);
            inky.setSpeed(SPEED_GHOST_AFTER_COLLISION);
            inky.setVelocity(inky.velocity().minus(0, 2.0f));
            inky.setAcceleration(0, 0.4f);

            pinky.setMoveAndWishDir(Direction.LEFT);
            pinky.setSpeed(SPEED_GHOST_AFTER_COLLISION);
            pinky.setVelocity(pinky.velocity().minus(0, 2.0f));
            pinky.setAcceleration(0, 0.4f);
        }
        else if (t == 530) {
            inky.hide();
            pinky.hide();
            pacMan.setSpeed(0);
            pacMan.setMoveDir(Direction.LEFT);
            msPacMan.setSpeed(0);
            msPacMan.setMoveDir(Direction.RIGHT);
        }
        else if (t == 545) {
            pacMan.stopAnimation();
            pacMan.resetAnimation();
            msPacMan.stopAnimation();
            msPacMan.resetAnimation();
        }
        else if (t == 560) {
            heart.setPosition((pacMan.x() + msPacMan.x()) / 2, pacMan.y() - TS * (2));
            heart.show();
        }
        else if (t == 760) {
            pacMan.hide();
            msPacMan.hide();
            heart.hide();
        }
        else if (t == 775) {
            theGameController().letCurrentGameStateExpire();
            return;
        }

        pacMan.move();
        msPacMan.move();
        inky.move();
        pinky.move();

        if (collided) {
            if (inky.y() > MIDDLE_LANE) {
                inky.setPosition(inky.x(), MIDDLE_LANE);
            }
            if (pinky.y() > MIDDLE_LANE) {
                pinky.setPosition(pinky.x(), MIDDLE_LANE);
            }
        }

        clapperboard.tick();
    }

    @Override
    public Vector2f sizeInPx() {
        return NES_SIZE.toVector2f();
    }

    @Override
    public TengenMsPacMan_GameRenderer gr() {
        return (TengenMsPacMan_GameRenderer) gameRenderer;
    }

    @Override
    public void drawSceneContent() {
        gr().drawVerticalSceneBorders();
        gr().drawActor(clapperboard);
        gr().drawActor(msPacMan);
        gr().drawActor(pacMan);
        gr().drawActor(inky);
        gr().drawActor(pinky);
        gr().drawActor(heart);
    }
}