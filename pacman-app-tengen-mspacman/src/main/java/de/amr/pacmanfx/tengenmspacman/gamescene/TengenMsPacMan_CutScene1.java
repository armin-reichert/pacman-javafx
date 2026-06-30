/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.rendering.SpriteID;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.GameVariant;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.JoypadButton;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import static de.amr.basics.spriteanim.SpriteAnimationAccessor.singleSpriteAnimation;
import static de.amr.pacmanfx.tengenmspacman.config.TengenMsPacManGameVariant.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.config.TengenMsPacManGameVariant.NES_SCREEN_WIDTH;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them.
 */
public class TengenMsPacMan_CutScene1 extends AbstractGameScene2D {

    public static final int TICK_EXPIRES = 775;

    private static final int UPPER_LANE   = WorldMap.TS * 8;
    private static final int LOWER_LANE   = WorldMap.TS * 24;
    private static final int MIDDLE_LANE  = WorldMap.TS * 16;
    private static final int LEFT_BORDER  = WorldMap.TS;
    private static final int RIGHT_BORDER = WorldMap.TS * 30;

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

    public TengenMsPacMan_CutScene1(Game game) {
        super(game);
        unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
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
    public void onActivate() {
        final GameVariant uiConfig = game().currentGameVariant();
        final SpriteAnimationContainer spriteAnimations = game().ui().sprites().animations();
        final var spriteSheet = TengenMsPacMan_SpriteSheet.instance();

        // Quit cut scene when "START" button on "joypad" is pressed
        final Joypad joypad = game().input().joypad();
        actionBindings().bindActionToKeyCombination(game().actions().gameFlowActions().actionLetGameStateExpire(),
            joypad.keyForButton(JoypadButton.START));

        clapperboard = new Clapperboard(1, "THEY MEET");
        clapperboard.setPosition(3 * WorldMap.TS, 10 * WorldMap.TS);
        clapperboard.show();
        clapperboard.startAnimation();

        msPacMan = TengenMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAnimations(uiConfig.createPacAnimations(spriteAnimations));
        msPacMan.setMoveDir(Direction.LEFT);
        msPacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
        msPacMan.setSpeed(0);

        pacMan = TengenMsPacMan_ActorFactory.createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations(spriteAnimations));
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.setPosition(LEFT_BORDER, UPPER_LANE);
        pacMan.setSpeed(0);

        inky = uiConfig.createAnimatedGhost(spriteAnimations, GameModel.CYAN_GHOST_BASHFUL);
        inky.setMoveDir(Direction.RIGHT);
        inky.setWishDir(Direction.RIGHT);
        inky.setPosition(LEFT_BORDER, UPPER_LANE);
        inky.setSpeed(0);

        pinky = uiConfig.createAnimatedGhost(spriteAnimations, GameModel.PINK_GHOST_SPEEDY);
        pinky.setMoveDir(Direction.LEFT);
        pinky.setWishDir(Direction.LEFT);
        pinky.setPosition(RIGHT_BORDER, LOWER_LANE);
        pinky.setSpeed(0);

        heart = new Actor();
        heart.setAnimations(singleSpriteAnimation(spriteSheet.sprite(SpriteID.HEART)));

        collided = false;

        game().ui().sounds().play(PacManGameSoundID.INTERMISSION_1);
    }

    @Override
    public void onDeactivate() {
        game().ui().sounds().stop(PacManGameSoundID.INTERMISSION_1);
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

        final long gameStateTick = gameState().timer().tickCount();
        if (gameStateTick <= TICK_EXPIRES) {
            switch ((int) gameStateTick) {
                case 130 -> {
                    pacMan.setSpeed(SPEED_CHASING);
                    pacMan.animations().select(TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
                    pacMan.animations().playSelected();
                    pacMan.show();

                    msPacMan.setSpeed(SPEED_CHASING);
                    msPacMan.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
                    msPacMan.animations().playSelected();
                    msPacMan.show();
                }
                case 160 -> {
                    inky.setSpeed(SPEED_CHASING);
                    inky.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
                    inky.animations().playSelected();
                    inky.show();

                    pinky.setSpeed(SPEED_CHASING);
                    pinky.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
                    pinky.animations().playSelected();
                    pinky.show();
                }
                case 400 -> {
                    msPacMan.setPosition(LEFT_BORDER, MIDDLE_LANE);
                    msPacMan.setMoveDir(Direction.RIGHT);

                    pacMan.setPosition(RIGHT_BORDER, MIDDLE_LANE);
                    pacMan.setMoveDir(Direction.LEFT);

                    pinky.setPosition(msPacMan.x() - WorldMap.TS * 11, msPacMan.y());
                    pinky.setMoveDir(Direction.RIGHT);
                    pinky.setWishDir(Direction.RIGHT);

                    inky.setPosition(pacMan.x() + WorldMap.TS * 11, pacMan.y());
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
                    inky.setVelY(inky.velY() - 2.0f);
                    inky.setAcceleration(0, 0.4f);

                    pinky.setMoveDir(Direction.LEFT);
                    pinky.setWishDir(Direction.LEFT);
                    pinky.setSpeed(SPEED_AFTER_COLLISION);
                    pinky.setVelY(pinky.velY() - 2.0f);
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
                    pacMan.animations().resetSelected();
                    msPacMan.animations().resetSelected();
                }
                case 560 -> {
                    heart.setPosition(0.5f * (pacMan.x() + msPacMan.x()), pacMan.y() - WorldMap.TS(2));
                    heart.show();
                }
                case 760 -> {
                    pacMan.hide();
                    msPacMan.hide();
                    heart.hide();
                }
                case 775 -> gameState().expire();
            }
        }
    }
}