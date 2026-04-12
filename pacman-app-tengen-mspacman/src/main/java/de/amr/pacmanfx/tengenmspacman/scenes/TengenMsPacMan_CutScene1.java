/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengenmspacman.rendering.SpriteID;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.animation.SingleSpriteNoAnimation;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.JOYPAD;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_PIXELS;
import static de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel.createMsPacMan;
import static de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel.createPacMan;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_LET_GAME_STATE_EXPIRE;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them.
 */
public class TengenMsPacMan_CutScene1 extends GameScene2D {

    public static final int TICK_EXPIRES = 775;

    private static final int UPPER_LANE   = TS * 8;
    private static final int LOWER_LANE   = TS * 24;
    private static final int MIDDLE_LANE  = TS * 16;
    private static final int LEFT_BORDER  = TS;
    private static final int RIGHT_BORDER = TS * 30;

    private static final float SPEED_CHASING = 2.0f;
    private static final float SPEED_RISING = 1.0f;
    private static final float SPEED_AFTER_COLLISION = 0.5f;

    private Clapperboard clapperboard;
    private Actor heart;
    private Pac pacMan;
    private Pac msPacMan;
    private Ghost inky;
    private Ghost pinky;

    private boolean collided;

    public TengenMsPacMan_CutScene1() {}

    public Clapperboard clapperboard() {
        return clapperboard;
    }

    public Actor heart() {
        return heart;
    }

    public Pac pacMan() {
        return pacMan;
    }

    public Pac msPacMan() {
        return msPacMan;
    }

    public Ghost inky() {
        return inky;
    }

    public Ghost pinky() {
        return pinky;
    }

    @Override
    public void doInit(Game game) {
        final UIConfig uiConfig = ui.currentConfig();
        final var spriteSheet = TengenMsPacMan_SpriteSheet.instance();

        actionBindings.bindActionToKeyCombination(ACTION_LET_GAME_STATE_EXPIRE, JOYPAD.key(JoypadButton.START));

        clapperboard = new Clapperboard(1, "THEY MEET");
        clapperboard.setPosition(3 * TS, 10 * TS);
        clapperboard.show();
        clapperboard.startAnimation();

        msPacMan = createMsPacMan();
        msPacMan.setAnimations(uiConfig.createPacAnimations(ui.spriteAnimationRegistry()));
        msPacMan.setMoveDir(Direction.LEFT);
        msPacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
        msPacMan.setSpeed(0);

        pacMan = createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations(ui.spriteAnimationRegistry()));
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.setPosition(LEFT_BORDER, UPPER_LANE);
        pacMan.setSpeed(0);

        inky = uiConfig.createGhostWithAnimations(ui.spriteAnimationRegistry(), CYAN_GHOST_BASHFUL);
        inky.setMoveDir(Direction.RIGHT);
        inky.setWishDir(Direction.RIGHT);
        inky.setPosition(LEFT_BORDER, UPPER_LANE);
        inky.setSpeed(0);

        pinky = uiConfig.createGhostWithAnimations(ui.spriteAnimationRegistry(), PINK_GHOST_SPEEDY);
        pinky.setMoveDir(Direction.LEFT);
        pinky.setWishDir(Direction.LEFT);
        pinky.setPosition(RIGHT_BORDER, LOWER_LANE);
        pinky.setSpeed(0);

        heart = new Actor();
        heart.setAnimations(new SingleSpriteNoAnimation(spriteSheet.sprite(SpriteID.HEART)));

        collided = false;

        ui.soundManager().play(SoundID.INTERMISSION_1);
    }

    @Override
    protected void doEnd(Game game) {
        ui.soundManager().stop(SoundID.INTERMISSION_1);
    }

    @Override
    public void update(Game game) {
        clapperboard.tick();

        pacMan.move();
        msPacMan.move();
        inky.move();
        pinky.move();

        if (collided) {
            if (inky.y() > MIDDLE_LANE) {
                inky.setY(MIDDLE_LANE);
            }
            if (pinky.y() > MIDDLE_LANE) {
                pinky.setY(MIDDLE_LANE);
            }
        }

        final long tick = game.control().state().timer().tickCount();
        if (tick <= TICK_EXPIRES) {
            final short eventTick = (short) tick;
            switch (eventTick) {
                case 130 -> {
                    pacMan.setSpeed(SPEED_CHASING);
                    pacMan.selectAnimation(TengenMsPacMan_AnimationID.ANIM_MR_PAC_MAN_MUNCHING);
                    pacMan.playAnimation();
                    pacMan.show();

                    msPacMan.setSpeed(SPEED_CHASING);
                    msPacMan.selectAnimation(Pac.AnimationID.PAC_MUNCHING);
                    msPacMan.playAnimation();
                    msPacMan.show();
                }
                case 160 -> {
                    inky.setSpeed(SPEED_CHASING);
                    inky.selectAnimation(Ghost.AnimationID.GHOST_NORMAL);
                    inky.playAnimation();
                    inky.show();

                    pinky.setSpeed(SPEED_CHASING);
                    pinky.selectAnimation(Ghost.AnimationID.GHOST_NORMAL);
                    pinky.playAnimation();
                    pinky.show();
                }
                case 400 -> {
                    msPacMan.setPosition(LEFT_BORDER, MIDDLE_LANE);
                    msPacMan.setMoveDir(Direction.RIGHT);

                    pacMan.setPosition(RIGHT_BORDER, MIDDLE_LANE);
                    pacMan.setMoveDir(Direction.LEFT);

                    pinky.setPosition(msPacMan.x() - TS * 11, msPacMan.y());
                    pinky.setMoveDir(Direction.RIGHT);
                    pinky.setWishDir(Direction.RIGHT);

                    inky.setPosition(pacMan.x() + TS * 11, pacMan.y());
                    inky.setMoveDir(Direction.LEFT);
                    inky.setWishDir(Direction.LEFT);
                }
                case 454 -> {
                    pacMan.setMoveDir(Direction.UP);
                    pacMan.setSpeed(SPEED_RISING);
                    msPacMan.setMoveDir(Direction.UP);
                    msPacMan.setSpeed(SPEED_RISING);
                }
                case 498 -> {
                    collided = true;

                    inky.setMoveDir(Direction.RIGHT);
                    inky.setWishDir(Direction.RIGHT);
                    inky.setSpeed(SPEED_AFTER_COLLISION);
                    inky.setVelocity(inky.velocity().minus(0, 2.0f));
                    inky.setAcceleration(0, 0.4f);

                    pinky.setMoveDir(Direction.LEFT);
                    pinky.setWishDir(Direction.LEFT);
                    pinky.setSpeed(SPEED_AFTER_COLLISION);
                    pinky.setVelocity(pinky.velocity().minus(0, 2.0f));
                    pinky.setAcceleration(0, 0.4f);
                }
                case 530 -> {
                    inky.hide();
                    pinky.hide();
                    pacMan.setSpeed(0);
                    pacMan.setMoveDir(Direction.LEFT);
                    msPacMan.setSpeed(0);
                    msPacMan.setMoveDir(Direction.RIGHT);
                }
                case 545 -> {
                    pacMan.resetAnimation();
                    msPacMan.resetAnimation();
                }
                case 560 -> {
                    heart.setPosition(0.5f * (pacMan.x() + msPacMan.x()), pacMan.y() - TS(2));
                    heart.show();
                }
                case 760 -> {
                    pacMan.hide();
                    msPacMan.hide();
                    heart.hide();
                }
                case 775 -> game.control().state().expire();
            }
        }
    }

    @Override
    public Vector2i unscaledSize() { return NES_SCREEN_PIXELS; }
}