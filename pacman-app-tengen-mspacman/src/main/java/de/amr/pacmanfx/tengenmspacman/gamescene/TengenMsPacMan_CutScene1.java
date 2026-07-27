/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.systems.common.MovementSystem;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.JoypadButton;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import java.util.List;

import static de.amr.basics.spriteanim.SpriteAnimationAccess.singleSpriteAnimation;
import static de.amr.pacmanfx.core.model.world.WorldMap.tilesPx;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantConfig.NES_SCREEN_WIDTH;

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

    public TengenMsPacMan_CutScene1(GameAppContext appContext) {
        super(appContext);
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
        final WorldMovementSystem worldMovementSystem = gameContext().systems().navigator;

        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer spriteAnimations = appContext().ui().sprites().animations();
        final var spriteSheet = TengenMsPacMan_SpriteSheet.instance();

        // Quit cut scene when "START" button on "joypad" is pressed
        final Joypad joypad = input().joypad();
        actionBindings().bindActionToKeyCombination(appContext().commonActions().gameFlowActions().actionLetGameStateExpire(),
            joypad.keyForButton(JoypadButton.START));

        clapperboard = new Clapperboard(1, "THEY MEET");
        clapperboard.position().set(3 * WorldMap.TS, 10 * WorldMap.TS);
        clapperboard.visibility().show();
        clapperboard.startAnimation();

        msPacMan = TengenMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.animations = renderConfig.createPacAnimations(spriteAnimations);
        msPacMan.position().set(RIGHT_BORDER, LOWER_LANE);
        worldMovementSystem.setMoveDir(msPacMan, Direction.LEFT);
        worldMovementSystem.setSpeed(msPacMan, 0);

        pacMan = TengenMsPacMan_ActorFactory.createPacMan();
        pacMan.animations = renderConfig.createPacAnimations(spriteAnimations);
        pacMan.position().set(LEFT_BORDER, UPPER_LANE);
        worldMovementSystem.setMoveDir(pacMan, Direction.RIGHT);
        worldMovementSystem.setSpeed(pacMan, 0);

        inky = renderConfig.createAnimatedGhost(gameContext(), spriteAnimations, GameModel.CYAN_GHOST_BASHFUL);
        worldMovementSystem.setMoveDir(inky, Direction.RIGHT);
        worldMovementSystem.setWishDir(inky, Direction.RIGHT);
        inky.position().set(LEFT_BORDER, UPPER_LANE);
        worldMovementSystem.setSpeed(inky, 0);

        pinky = renderConfig.createAnimatedGhost(gameContext(), spriteAnimations, GameModel.PINK_GHOST_SPEEDY);
        worldMovementSystem.setMoveDir(pinky, Direction.LEFT);
        worldMovementSystem.setWishDir(pinky, Direction.LEFT);
        pinky.position().set(RIGHT_BORDER, LOWER_LANE);
        worldMovementSystem.setSpeed(pinky, 0);

        heart = new Actor();
        heart.animations = singleSpriteAnimation(spriteSheet.findSprite(SpriteID.HEART));

        collided = false;

        appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_1);
    }

    @Override
    public void onDeactivate() {
        appContext().ui().sounds().stop(PacManGameSoundID.INTERMISSION_1);
    }

    @Override
    public void onTick(GameContext gameContext) {
        final MovementSystem motor = gameContext.systems().motor;
        final WorldMovementSystem navigator = gameContext.systems().navigator;

        clapperboard.tick();

        List.of(pacMan, msPacMan, inky, pinky).forEach(motor::moveAccelerated);

        if (collided) {
            if (inky.position().y > MIDDLE_LANE) {
                inky.position().setY(MIDDLE_LANE);
            }
            if (pinky.position().y > MIDDLE_LANE) {
                pinky.position().setY(MIDDLE_LANE);
            }
        }

        final long gameStateTick = gameState().timer().tickCount();
        if (gameStateTick <= TICK_EXPIRES) {
            switch ((int) gameStateTick) {
                case 130 -> {
                    navigator.setSpeed(pacMan, SPEED_CHASING);
                    pacMan.animations.select(TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
                    pacMan.animations.playSelected();
                    pacMan.visibility().show();

                    navigator.setSpeed(msPacMan, SPEED_CHASING);
                    msPacMan.animations.select(CommonAnimationID.PAC_MUNCHING);
                    msPacMan.animations.playSelected();
                    msPacMan.visibility().show();
                }
                case 160 -> {
                    navigator.setSpeed(inky, SPEED_CHASING);
                    inky.animations.select(CommonAnimationID.GHOST_NORMAL);
                    inky.animations.playSelected();
                    inky.visibility().show();

                    navigator.setSpeed(pinky, SPEED_CHASING);
                    pinky.animations.select(CommonAnimationID.GHOST_NORMAL);
                    pinky.animations.playSelected();
                    pinky.visibility().show();
                }
                case 400 -> {
                    msPacMan.position().set(LEFT_BORDER, MIDDLE_LANE);
                    navigator.setMoveDir(msPacMan, Direction.RIGHT);

                    pacMan.position().set(RIGHT_BORDER, MIDDLE_LANE);
                    navigator.setMoveDir(pacMan, Direction.LEFT);

                    pinky.position().set(msPacMan.position().x - WorldMap.TS * 11, msPacMan.position().y);
                    navigator.setMoveDir(pinky, Direction.RIGHT);
                    navigator.setWishDir(pinky, Direction.RIGHT);

                    inky.position().set(pacMan.position().x + WorldMap.TS * 11, pacMan.position().y);
                    navigator.setMoveDir(inky, Direction.LEFT);
                    navigator.setWishDir(inky, Direction.LEFT);
                }
                case 454 -> List.of(pacMan, msPacMan).forEach(pac -> {
                    navigator.setMoveDir(pac, Direction.UP);
                    navigator.setSpeed(pac, SPEED_RISING);
                });
                case 498 -> {
                    collided = true;

                    navigator.setMoveDir(inky, Direction.RIGHT);
                    navigator.setWishDir(inky, Direction.RIGHT);
                    navigator.setSpeed(inky, SPEED_AFTER_COLLISION);
                    inky.movement().velY -= 2.0f;
                    inky.movement().setAcceleration(0, 0.4f);

                    navigator.setMoveDir(pinky, Direction.LEFT);
                    navigator.setWishDir(pinky, Direction.LEFT);
                    navigator.setSpeed(pinky, SPEED_AFTER_COLLISION);
                    pinky.movement().velY -= 2.0f;
                    pinky.movement().setAcceleration(0, 0.4f);
                }
                case 530 -> {
                    inky.visibility().hide();
                    pinky.visibility().hide();
                    navigator.setSpeed(pacMan, 0);
                    navigator.setMoveDir(pacMan, Direction.LEFT);
                    navigator.setSpeed(msPacMan, 0);
                    navigator.setMoveDir(msPacMan, Direction.RIGHT);
                }
                case 545 -> {
                    pacMan.animations.resetSelected();
                    msPacMan.animations.resetSelected();
                }
                case 560 -> {
                    heart.position().set(0.5f * (pacMan.position().x + msPacMan.position().x), pacMan.position().y - tilesPx(2));
                    heart.visibility().show();
                }
                case 760 -> {
                    pacMan.visibility().hide();
                    msPacMan.visibility().hide();
                    heart.visibility().hide();
                }
                case 775 -> gameState().triggerTimeout();
            }
        }
    }
}