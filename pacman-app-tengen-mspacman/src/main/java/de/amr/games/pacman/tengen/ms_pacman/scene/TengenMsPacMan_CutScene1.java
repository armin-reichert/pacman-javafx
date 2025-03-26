/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman.scene;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.nes.NES_JoypadButton;
import de.amr.games.pacman.model.actors.Actor2D;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameModel;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.*;
import de.amr.games.pacman.ui._2d.GameRenderer;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.model.actors.ActorAnimations.ANIM_GHOST_NORMAL;
import static de.amr.games.pacman.model.actors.ActorAnimations.ANIM_PAC_MUNCHING;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_TILES;
import static de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_SpriteSheet.HEART_SPRITE;
import static de.amr.games.pacman.ui.GameUI.THE_GAME_CONTEXT;
import static de.amr.games.pacman.ui.GameUI.THE_SOUND;

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
    private Pac mrPacMan;
    private Pac msPacMan;
    private Ghost inky;
    private Ghost pinky;
    private Actor2D heart;
    private ClapperboardAnimation clapAnimation;
    private boolean collided;

    private int t;

    @Override
    public void bindGameActions() {
        bind(THE_GAME_CONTROLLER::terminateCurrentState, THE_GAME_CONTEXT.joypadKeyBinding().key(NES_JoypadButton.BTN_START));
    }

    @Override
    public void doInit() {
        t = -1;
        THE_GAME_CONTEXT.setScoreVisible(false);

        mrPacMan = new Pac();
        msPacMan = new Pac();
        inky = TengenMsPacMan_GameModel.inky();
        pinky = TengenMsPacMan_GameModel.pinky();
        heart = new Actor2D();

        var spriteSheet = (TengenMsPacMan_SpriteSheet) THE_GAME_CONTEXT.currentUIConfig().spriteSheet();
        msPacMan.setAnimations(new TengenMsPacMan_PacAnimations(spriteSheet));
        mrPacMan.setAnimations(new TengenMsPacMan_PacAnimations(spriteSheet));
        inky.setAnimations(new TengenMsPacMan_GhostAnimations(spriteSheet, inky.id()));
        pinky.setAnimations(new TengenMsPacMan_GhostAnimations(spriteSheet, pinky.id()));

        music = THE_SOUND.makeSound("intermission.1");
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
            mrPacMan.setMoveDir(Direction.RIGHT);
            mrPacMan.setPosition(LEFT_BORDER, UPPER_LANE);
            mrPacMan.setSpeed(SPEED_CHASING);
            mrPacMan.selectAnimation("pacman_munching");
            mrPacMan.startAnimation();
            mrPacMan.show();

            msPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
            msPacMan.setSpeed(SPEED_CHASING);
            msPacMan.selectAnimation(ANIM_PAC_MUNCHING);
            msPacMan.startAnimation();
            msPacMan.show();
        }
        else if (t == 160) {
            inky.setMoveAndWishDir(Direction.RIGHT);
            inky.setPosition(LEFT_BORDER, UPPER_LANE);
            inky.setSpeed(SPEED_CHASING);
            inky.selectAnimation(ANIM_GHOST_NORMAL);
            inky.startAnimation();
            inky.show();

            pinky.setMoveAndWishDir(Direction.LEFT);
            pinky.setPosition(RIGHT_BORDER, LOWER_LANE);
            pinky.setSpeed(SPEED_CHASING);
            pinky.selectAnimation(ANIM_GHOST_NORMAL);
            pinky.startAnimation();
            pinky.show();

            collided = false;
        }
        else if (t == 400) {
            msPacMan.setPosition(LEFT_BORDER, MIDDLE_LANE);
            msPacMan.setMoveDir(Direction.RIGHT);

            mrPacMan.setPosition(RIGHT_BORDER, MIDDLE_LANE);
            mrPacMan.setMoveDir(Direction.LEFT);

            pinky.setPosition(msPacMan.position().minus(TS * 11, 0));
            pinky.setMoveAndWishDir(Direction.RIGHT);

            inky.setPosition(mrPacMan.position().plus(TS * 11, 0));
            inky.setMoveAndWishDir(Direction.LEFT);
        }
        else if (t == 454) {
            mrPacMan.setMoveDir(Direction.UP);
            mrPacMan.setSpeed(SPEED_PAC_RISING);
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
            mrPacMan.setSpeed(0);
            mrPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setSpeed(0);
            msPacMan.setMoveDir(Direction.RIGHT);
        }
        else if (t == 545) {
            mrPacMan.stopAnimation();
            mrPacMan.resetAnimation();
            msPacMan.stopAnimation();
            msPacMan.resetAnimation();
        }
        else if (t == 560) {
            heart.setPosition((mrPacMan.posX() + msPacMan.posX()) / 2, mrPacMan.posY() - TS * (2));
            heart.show();
        }
        else if (t == 760) {
            mrPacMan.hide();
            msPacMan.hide();
            heart.hide();
        }
        else if (t == 775) {
            THE_GAME_CONTROLLER.terminateCurrentState();
            return;
        }

        mrPacMan.move();
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
        var r = (TengenMsPacMan_Renderer2D) gr;
        r.drawSceneBorderLines();
        r.setLevelNumberBoxesVisible(false);
        r.drawClapperBoard(clapAnimation, "THEY MEET", 1, CLAP_TILE_X, CLAP_TILE_Y);
        r.drawAnimatedActor(msPacMan);
        r.drawAnimatedActor(mrPacMan);
        r.drawAnimatedActor(inky);
        r.drawAnimatedActor(pinky);
        r.drawActorSprite(heart, HEART_SPRITE);
        if (THE_GAME_CONTROLLER.game().level().isPresent()) {
            // avoid exception in cut scene test mode
            r.drawLevelCounter(sizeInPx().x() - 4 * TS, sizeInPx().y() - 3 * TS);
        }
    }

    @Override
    protected void drawDebugInfo() {
        gr.drawTileGrid(sizeInPx().x(), sizeInPx().y());
        gr.ctx().setFill(Color.WHITE);
        gr.ctx().setFont(GameRenderer.DEBUG_FONT);
        gr.ctx().fillText("Tick " + t, 20, 20);
    }
}