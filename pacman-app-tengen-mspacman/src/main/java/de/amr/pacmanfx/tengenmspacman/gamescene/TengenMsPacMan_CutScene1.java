/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.Clapperboard;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.game.GameVariant;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.entities.Heart;
import de.amr.pacmanfx.tengenmspacman.entities.clapperboard.TengenMsPacMan_ClapperboardStateSystem;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.ui.input.JoypadButton;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import java.util.List;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them.
 */
public class TengenMsPacMan_CutScene1 extends GameScene {

    public static final int TICK_CLAP = 2;
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
    private Heart heart;
    private Pac pacMan;
    private Pac msPacMan;
    private Ghost inky;
    private Ghost pinky;

    private boolean collided;

    public TengenMsPacMan_CutScene1(GameAppContext app) {
        super(app);
        setComp(CanvasRenderingComp.class, new CanvasRenderingComp());
        reqCanvasRendering().unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        reqCanvasRendering().unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
    }

    public Clapperboard clapperboard() {
        return clapperboard;
    }

    public Heart heart() {
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
        // Quit cut scene when "START" button on "joypad" is pressed
        final GameAction quitAction = app().commonActions().gameFlowActions().actionLetGameStateExpire();

        final var bindingsMap = actionBindingsSupport().bindingsMap();
        bindingsMap.bindActionToKeyCombination(quitAction, app().input().joypad().keyForButton(JoypadButton.START));

        createActors();
    }

    @Override
    public void onDeactivate() {
        stopMusic();
    }

    @Override
    public void onTick(GameContext game) {
        final int tick = (int) game.state().timer().tickCount();
        switch (tick) {
            case TICK_CLAP -> {
                getReady(game.variant().systems().navigator());
                clapperboard.show();
                TengenMsPacMan_ClapperboardStateSystem.startFlapAnimation(clapperboard);
                playMusic();
            }
            case TICK_EXPIRES -> {
                game.state().triggerTimeout();
                return;
            }
        }

        TengenMsPacMan_ClapperboardStateSystem.update(clapperboard);
        playCutScene(game, tick);
    }

    private void createActors() {
        final var actorFactory = TengenMsPacMan_ActorFactory.instance();
        final GameVariant variant = app().gameVariants().currentGameVariant();
        final GameVariantRenderConfig renderConfig = variant.uiConfig().renderConfig();
        final SpriteAnimContainer animContainer    = variant.spriteAnimContainer();
        final ActorSpriteAnimController animController  = variant.config().systems().actorSpriteAnimController();

        clapperboard = new Clapperboard("1", "THEY MEET");

        msPacMan = actorFactory.createMsPacMan();
        msPacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));

        pacMan = actorFactory.createPacMan();
        pacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));

        inky = renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.CYAN_GHOST_BASHFUL);

        pinky = renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.PINK_GHOST_SPEEDY);

        heart = new Heart();
    }

    private void getReady(WorldNavigationSystem navigator) {
        clapperboard.pos().set(3 * WorldMap.TS, 10 * WorldMap.TS);

        msPacMan.pos().set(RIGHT_BORDER, LOWER_LANE);
        navigator.setMoveDir(msPacMan, Direction.LEFT);
        navigator.setMoveDirSpeed(msPacMan, 0);

        pacMan.pos().set(LEFT_BORDER, UPPER_LANE);
        navigator.setMoveDir(pacMan, Direction.RIGHT);
        navigator.setMoveDirSpeed(pacMan, 0);

        inky.pos().set(LEFT_BORDER, UPPER_LANE);
        navigator.setMoveDir(inky, Direction.RIGHT);
        navigator.setWishDir(inky, Direction.RIGHT);
        navigator.setMoveDirSpeed(inky, 0);

        pinky.pos().set(RIGHT_BORDER, LOWER_LANE);
        navigator.setMoveDir(pinky, Direction.LEFT);
        navigator.setWishDir(pinky, Direction.LEFT);
        navigator.setMoveDirSpeed(pinky, 0);

        collided = false;
    }

    private void playMusic() {
        soundManager().play(PacManGameSoundID.INTERMISSION_1);
    }

    private void stopMusic() {
        soundManager().stop(PacManGameSoundID.INTERMISSION_1);
    }

    private void letActorsMove(GameContext game) {
        List.of(pacMan, msPacMan, inky, pinky).forEach(game.variant().systems().motor()::move);
        if (collided) {
            if (inky.pos().y() > MIDDLE_LANE) {
                inky.pos().setY(MIDDLE_LANE);
            }
            if (pinky.pos().y() > MIDDLE_LANE) {
                pinky.pos().setY(MIDDLE_LANE);
            }
        }
    }

    private void playCutScene(GameContext game, int tick) {
        final WorldNavigationSystem navigator = game.variant().systems().navigator();
        final ActorSpriteAnimController animSystem = game.variant().systems().actorSpriteAnimController();

        letActorsMove(game);

        if (tick == 130) {
            pacMan.show();
            navigator.setMoveDirSpeed(pacMan, SPEED_CHASING);
            animSystem.select(pacMan, TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
            animSystem.playSelected(pacMan);

            msPacMan.show();
            navigator.setMoveDirSpeed(msPacMan, SPEED_CHASING);
            animSystem.select(msPacMan, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
            animSystem.playSelected(msPacMan);
        }
        else if (tick == 160) {
            inky.show();
            navigator.setMoveDirSpeed(inky, SPEED_CHASING);
            animSystem.select(inky, CommonSpriteAnimationID.GHOST_NORMAL);
            animSystem.playSelected(inky);

            pinky.show();
            navigator.setMoveDirSpeed(pinky, SPEED_CHASING);
            animSystem.select(pinky, CommonSpriteAnimationID.GHOST_NORMAL);
            animSystem.playSelected(pinky);
        }
        else if (tick == 400) {
            msPacMan.pos().set(LEFT_BORDER, MIDDLE_LANE);
            navigator.setMoveDir(msPacMan, Direction.RIGHT);

            pacMan.pos().set(RIGHT_BORDER, MIDDLE_LANE);
            navigator.setMoveDir(pacMan, Direction.LEFT);

            pinky.pos().set(msPacMan.pos().x() - WorldMap.TS * 11, msPacMan.pos().y());
            navigator.setMoveDir(pinky, Direction.RIGHT);
            navigator.setWishDir(pinky, Direction.RIGHT);

            inky.pos().set(pacMan.pos().x() + WorldMap.TS * 11, pacMan.pos().y());
            navigator.setMoveDir(inky, Direction.LEFT);
            navigator.setWishDir(inky, Direction.LEFT);
        }
        else if (tick == 454) {
            List.of(pacMan, msPacMan).forEach(pac -> {
                navigator.setMoveDir(pac, Direction.UP);
                navigator.setMoveDirSpeed(pac, SPEED_RISING);
            });
        }
        else if (tick == 498) {
            collided = true;

            navigator.setMoveDir(inky, Direction.RIGHT);
            navigator.setWishDir(inky, Direction.RIGHT);
            navigator.setMoveDirSpeed(inky, SPEED_AFTER_COLLISION);

            inky.movement().setVelocityY(inky.movement().velocityY() - 2.0f);
            inky.movement().setAcceleration(0, 0.4f);

            navigator.setMoveDir(pinky, Direction.LEFT);
            navigator.setWishDir(pinky, Direction.LEFT);
            navigator.setMoveDirSpeed(pinky, SPEED_AFTER_COLLISION);

            pinky.movement().setVelocityY(pinky.movement().velocityY() - 2.0f);
            pinky.movement().setAcceleration(0, 0.4f);
        }
        else if (tick == 530) {
            inky.hide();
            pinky.hide();

            navigator.setMoveDirSpeed(pacMan, 0);
            navigator.setMoveDir(pacMan, Direction.LEFT);
            navigator.setMoveDirSpeed(msPacMan, 0);
            navigator.setMoveDir(msPacMan, Direction.RIGHT);
        }
        else if (tick == 545) {
            animSystem.resetSelected(pacMan);
            animSystem.resetSelected(msPacMan);
        }
        else if (tick == 560) {
            heart.pos().set(0.5f * (pacMan.pos().x() + msPacMan.pos().x()), pacMan.pos().y() - tilesPx(2));
            heart.show();
        }
        else if (tick == 760) {
            pacMan.hide();
            msPacMan.hide();
            heart.hide();
        }
    }
}