/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.components.SpriteAnimComp;
import de.amr.pacmanfx.core.ecs.systems.common.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.world.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.entities.ActorAnimationID;
import de.amr.pacmanfx.core.model.entities.ghost.Ghost;
import de.amr.pacmanfx.core.model.entities.pac.Pac;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.entities.clapperboard.Clapperboard;
import de.amr.pacmanfx.tengenmspacman.entities.clapperboard.ClapperboardStateSystem;
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

import static de.amr.basics.spriteanim.SpriteAnimationAccessor.singleSpriteAnimation;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
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
    private GameEntity heart;
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

    public GameEntity heart() {
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
        final WorldNavigationSystem navigator = gameContext().systems().worldNavigator();

        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer spriteAnimations = appContext().ui().sprites().animations();
        final var spriteSheet = TengenMsPacMan_SpriteSheet.instance();

        // Quit cut scene when "START" button on "joypad" is pressed
        final Joypad joypad = input().joypad();
        actionBindings().bindActionToKeyCombination(appContext().commonActions().gameFlowActions().actionLetGameStateExpire(),
            joypad.keyForButton(JoypadButton.START));

        clapperboard = new Clapperboard(1, "THEY MEET");
        clapperboard.show();
        clapperboard.pos().set(3 * WorldMap.TS, 10 * WorldMap.TS);

        ClapperboardStateSystem.startFlapAnimation(clapperboard);

        final var factory = TengenMsPacMan_ActorFactory.instance();

        msPacMan = factory.createMsPacMan();
        msPacMan.spriteAnimation().setAnimations(renderConfig.createPacAnimations(spriteAnimations));
        msPacMan.pos().set(RIGHT_BORDER, LOWER_LANE);
        navigator.setMoveDir(msPacMan, Direction.LEFT);
        navigator.setSpeed(msPacMan, 0);

        pacMan = factory.createPacMan();
        pacMan.spriteAnimation().setAnimations(renderConfig.createPacAnimations(spriteAnimations));
        pacMan.pos().set(LEFT_BORDER, UPPER_LANE);
        navigator.setMoveDir(pacMan, Direction.RIGHT);
        navigator.setSpeed(pacMan, 0);

        inky = renderConfig.createAnimatedGhost(gameContext(), spriteAnimations, GhostPersonality.CYAN_GHOST_BASHFUL);
        navigator.setMoveDir(inky, Direction.RIGHT);
        navigator.setWishDir(inky, Direction.RIGHT);
        inky.pos().set(LEFT_BORDER, UPPER_LANE);
        navigator.setSpeed(inky, 0);

        pinky = renderConfig.createAnimatedGhost(gameContext(), spriteAnimations, GhostPersonality.PINK_GHOST_SPEEDY);
        navigator.setMoveDir(pinky, Direction.LEFT);
        navigator.setWishDir(pinky, Direction.LEFT);
        pinky.pos().set(RIGHT_BORDER, LOWER_LANE);
        navigator.setSpeed(pinky, 0);

        heart = new GameEntity();
        final SpriteAnimComp heartAnimationComp = new SpriteAnimComp();
        heartAnimationComp.setAnimations(singleSpriteAnimation(spriteSheet.findSprite(SpriteID.HEART)));
        heart.setComponent(SpriteAnimComp.class, heartAnimationComp);

        collided = false;

        appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_1);
    }

    @Override
    public void onDeactivate() {
        appContext().ui().sounds().stop(PacManGameSoundID.INTERMISSION_1);
    }

    @Override
    public void onTick(GameContext gameContext) {
        final GameSystems sys = gameContext.systems();

        ClapperboardStateSystem.update(clapperboard);

        List.of(pacMan, msPacMan, inky, pinky).forEach(sys.motor()::move);

        if (collided) {
            if (inky.pos().y() > MIDDLE_LANE) {
                inky.pos().setY(MIDDLE_LANE);
            }
            if (pinky.pos().y() > MIDDLE_LANE) {
                pinky.pos().setY(MIDDLE_LANE);
            }
        }

        final long gameStateTick = gameState().timer().tickCount();
        if (gameStateTick <= TICK_EXPIRES) {
            switch ((int) gameStateTick) {
                case 130 -> {
                    pacMan.show();
                    sys.worldNavigator().setSpeed(pacMan, SPEED_CHASING);
                    sys.spriteAnim().select(pacMan, TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
                    sys.spriteAnim().playSelected(pacMan);

                    msPacMan.show();
                    sys.worldNavigator().setSpeed(msPacMan, SPEED_CHASING);
                    sys.spriteAnim().select(msPacMan, ActorAnimationID.PAC_MUNCHING);
                    sys.spriteAnim().playSelected(msPacMan);
                }
                case 160 -> {
                    inky.show();
                    sys.worldNavigator().setSpeed(inky, SPEED_CHASING);
                    sys.spriteAnim().select(inky, ActorAnimationID.GHOST_NORMAL);
                    sys.spriteAnim().playSelected(inky);

                    pinky.show();
                    sys.worldNavigator().setSpeed(pinky, SPEED_CHASING);
                    sys.spriteAnim().select(pinky, ActorAnimationID.GHOST_NORMAL);
                    sys.spriteAnim().playSelected(pinky);
                }
                case 400 -> {
                    msPacMan.pos().set(LEFT_BORDER, MIDDLE_LANE);
                    sys.worldNavigator().setMoveDir(msPacMan, Direction.RIGHT);

                    pacMan.pos().set(RIGHT_BORDER, MIDDLE_LANE);
                    sys.worldNavigator().setMoveDir(pacMan, Direction.LEFT);

                    pinky.pos().set(msPacMan.pos().x() - WorldMap.TS * 11, msPacMan.pos().y());
                    sys.worldNavigator().setMoveDir(pinky, Direction.RIGHT);
                    sys.worldNavigator().setWishDir(pinky, Direction.RIGHT);

                    inky.pos().set(pacMan.pos().x() + WorldMap.TS * 11, pacMan.pos().y());
                    sys.worldNavigator().setMoveDir(inky, Direction.LEFT);
                    sys.worldNavigator().setWishDir(inky, Direction.LEFT);
                }
                case 454 -> List.of(pacMan, msPacMan).forEach(pac -> {
                    sys.worldNavigator().setMoveDir(pac, Direction.UP);
                    sys.worldNavigator().setSpeed(pac, SPEED_RISING);
                });
                case 498 -> {
                    collided = true;

                    sys.worldNavigator().setMoveDir(inky, Direction.RIGHT);
                    sys.worldNavigator().setWishDir(inky, Direction.RIGHT);
                    sys.worldNavigator().setSpeed(inky, SPEED_AFTER_COLLISION);

                    inky.movement().setVelocityY(inky.movement().velocityY() - 2.0f);
                    inky.movement().setAcceleration(0, 0.4f);

                    sys.worldNavigator().setMoveDir(pinky, Direction.LEFT);
                    sys.worldNavigator().setWishDir(pinky, Direction.LEFT);
                    sys.worldNavigator().setSpeed(pinky, SPEED_AFTER_COLLISION);

                    pinky.movement().setVelocityY(pinky.movement().velocityY() - 2.0f);
                    pinky.movement().setAcceleration(0, 0.4f);
                }
                case 530 -> {
                    inky.hide();
                    pinky.hide();

                    sys.worldNavigator().setSpeed(pacMan, 0);
                    sys.worldNavigator().setMoveDir(pacMan, Direction.LEFT);
                    sys.worldNavigator().setSpeed(msPacMan, 0);
                    sys.worldNavigator().setMoveDir(msPacMan, Direction.RIGHT);
                }
                case 545 -> {
                    sys.spriteAnim().resetSelected(pacMan);
                    sys.spriteAnim().resetSelected(msPacMan);
                }
                case 560 -> {
                    heart.pos().set(0.5f * (pacMan.pos().x() + msPacMan.pos().x()), pacMan.pos().y() - tilesPx(2));
                    heart.show();
                }
                case 760 -> {
                    pacMan.hide();
                    msPacMan.hide();
                    heart.hide();
                }
                case 775 -> gameState().triggerTimeout();
            }
        }
    }
}