/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengenmspacman.model.actor.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.rendering.SpriteID;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.input.JoypadButton;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.basics.spriteanim.SingleSpriteAnimationSet;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_LET_GAME_STATE_EXPIRE;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them.
 */
public class TengenMsPacMan_CutScene1 extends GameScene2D {

    public static final Vector2i SIZE = new Vector2i(NES_SCREEN_WIDTH, NES_SCREEN_HEIGHT);

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

    public TengenMsPacMan_CutScene1(GameUI ui) {
        super(ui);
    }

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
    public void onSceneStart() {
        final UIConfig uiConfig = ui.currentConfig();
        final var spriteSheet = TengenMsPacMan_SpriteSheet.instance();

        actionBindings.add(
            ACTION_LET_GAME_STATE_EXPIRE,
            Input.instance().joypad.keyForButton(JoypadButton.START));

        clapperboard = new Clapperboard(1, "THEY MEET");
        clapperboard.setPosition(3 * TS, 10 * TS);
        clapperboard.show();
        clapperboard.startAnimation();

        msPacMan = TengenMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAnimations(uiConfig.createPacAnimations());
        msPacMan.setMoveDir(Direction.LEFT);
        msPacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
        msPacMan.setSpeed(0);

        pacMan = TengenMsPacMan_ActorFactory.createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations());
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.setPosition(LEFT_BORDER, UPPER_LANE);
        pacMan.setSpeed(0);

        inky = uiConfig.createGhostWithAnimations(CYAN_GHOST_BASHFUL);
        inky.setMoveDir(Direction.RIGHT);
        inky.setWishDir(Direction.RIGHT);
        inky.setPosition(LEFT_BORDER, UPPER_LANE);
        inky.setSpeed(0);

        pinky = uiConfig.createGhostWithAnimations(PINK_GHOST_SPEEDY);
        pinky.setMoveDir(Direction.LEFT);
        pinky.setWishDir(Direction.LEFT);
        pinky.setPosition(RIGHT_BORDER, LOWER_LANE);
        pinky.setSpeed(0);

        heart = new Actor();
        heart.setAnimations(new SingleSpriteAnimationSet(spriteSheet.sprite(SpriteID.HEART)));

        collided = false;

        ui.soundManager().play(SoundID.INTERMISSION_1);
    }

    @Override
    public void onSceneEnd() {
        ui.soundManager().stop(SoundID.INTERMISSION_1);
    }

    @Override
    public void onTick(long tick) {
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

        final long gameStateTick = gameContext().game().flow().state().timer().tickCount();
        if (gameStateTick <= TICK_EXPIRES) {
            switch ((int) gameStateTick) {
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
                case 775 -> gameContext().game().flow().state().expire();
            }
        }
    }

    @Override
    public Vector2i unscaledSceneSize() { return SIZE; }
}