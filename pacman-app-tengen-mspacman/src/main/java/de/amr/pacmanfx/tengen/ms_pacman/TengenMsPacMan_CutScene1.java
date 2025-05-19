/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.model.actors.*;
import javafx.scene.media.MediaPlayer;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_GameModel.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_SpriteSheet.HEART_SPRITE;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.*;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;

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

    static final int CLAP_TILE_X = TS * 3;
    static final int CLAP_TILE_Y = TS * 10;

    static final int UPPER_LANE  = TS * 8;
    static final int LOWER_LANE  = TS * 24;
    static final int MIDDLE_LANE = TS * 16;

    static final int LEFT_BORDER = TS;
    static final int RIGHT_BORDER = TS * (NES_TILES.x() - 2);

    static final float SPEED_CHASING = 2f;
    static final float SPEED_PAC_RISING = 1f;
    static final float SPEED_GHOST_AFTER_COLLISION = 0.5f;

    private MediaPlayer music;
    private Pac pacMan;
    private Pac msPacMan;
    private Ghost inky;
    private Ghost pinky;
    private Actor heart;
    private ClapperboardAnimation clapAnimation;
    private boolean collided;

    private int t;

    @Override
    public void bindActions() {
        bind(theGameController()::letCurrentGameStateExpire, theJoypad().key(JoypadButton.START));
    }

    @Override
    public void doInit() {
        t = -1;
        theGame().scoreManager().setScoreVisible(false);

        msPacMan = createMsPacMan();
        pacMan = createPacMan();
        inky = createCyanGhost();
        pinky = createPinkGhost();
        heart = new Actor();

        var spriteSheet = (TengenMsPacMan_SpriteSheet) theUIConfig().current().spriteSheet();
        msPacMan.setAnimations(new TengenMsPacMan_PacAnimations(spriteSheet));
        pacMan.setAnimations(new TengenMsPacMan_PacAnimations(spriteSheet));
        inky.setAnimations(new TengenMsPacMan_GhostAnimations(spriteSheet, inky.personality()));
        pinky.setAnimations(new TengenMsPacMan_GhostAnimations(spriteSheet, pinky.personality()));

        music = theSound().createSound("intermission.1");
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        t += 1;
        if (t == 0) {
            clapAnimation = new ClapperboardAnimation();
            clapAnimation.start();
            music.play();
        }
        else if (t == 130) {
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setPosition(LEFT_BORDER, UPPER_LANE);
            pacMan.setSpeed(SPEED_CHASING);
            pacMan.selectAnimation(TengenMsPacMan_PacAnimations.PAC_MAN_MUNCHING);
            pacMan.startAnimation();
            pacMan.show();

            msPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
            msPacMan.setSpeed(SPEED_CHASING);
            msPacMan.selectAnimation(CommonAnimationID.ANIM_ANY_PAC_MUNCHING);
            msPacMan.startAnimation();
            msPacMan.show();
        }
        else if (t == 160) {
            inky.setMoveAndWishDir(Direction.RIGHT);
            inky.setPosition(LEFT_BORDER, UPPER_LANE);
            inky.setSpeed(SPEED_CHASING);
            inky.selectAnimation(GhostAnimationID.ANIM_GHOST_NORMAL);
            inky.startAnimation();
            inky.show();

            pinky.setMoveAndWishDir(Direction.LEFT);
            pinky.setPosition(RIGHT_BORDER, LOWER_LANE);
            pinky.setSpeed(SPEED_CHASING);
            pinky.selectAnimation(GhostAnimationID.ANIM_GHOST_NORMAL);
            pinky.startAnimation();
            pinky.show();

            collided = false;
        }
        else if (t == 400) {
            msPacMan.setPosition(LEFT_BORDER, MIDDLE_LANE);
            msPacMan.setMoveDir(Direction.RIGHT);

            pacMan.setPosition(RIGHT_BORDER, MIDDLE_LANE);
            pacMan.setMoveDir(Direction.LEFT);

            pinky.setPosition(msPacMan.position().minus(TS * 11, 0));
            pinky.setMoveAndWishDir(Direction.RIGHT);

            inky.setPosition(pacMan.position().plus(TS * 11, 0));
            inky.setMoveAndWishDir(Direction.LEFT);
        }
        else if (t == 454) {
            pacMan.setMoveDir(Direction.UP);
            pacMan.setSpeed(SPEED_PAC_RISING);
            msPacMan.setMoveDir(Direction.UP);
            msPacMan.setSpeed(SPEED_PAC_RISING);
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
            heart.setPosition((pacMan.posX() + msPacMan.posX()) / 2, pacMan.posY() - TS * (2));
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
            if (inky.posY() > MIDDLE_LANE) {
                inky.setPosition(inky.posX(), MIDDLE_LANE);
            }
            if (pinky.posY() > MIDDLE_LANE) {
                pinky.setPosition(pinky.posX(), MIDDLE_LANE);
            }
        }

        clapAnimation.tick();
    }

    @Override
    public Vector2f sizeInPx() {
        return NES_SIZE.toVector2f();
    }

    @Override
    public void drawSceneContent() {
        gr.fillCanvas(backgroundColor());
        gr.drawScores(theGame().scoreManager(), nesPaletteColor(0x20), arcadeFontScaledTS());
        var r = (TengenMsPacMan_Renderer2D) gr;
        r.drawSceneBorderLines();
        r.drawClapperBoard(clapAnimation, "THEY MEET", 1, CLAP_TILE_X, CLAP_TILE_Y, arcadeFontScaledTS());
        r.drawActor(msPacMan);
        r.drawActor(pacMan);
        r.drawActor(inky);
        r.drawActor(pinky);
        r.drawActorSprite(heart, HEART_SPRITE);
        r.drawLevelCounter(theGame().levelCounter(), sizeInPx().minus(0, 3*TS));
    }
}