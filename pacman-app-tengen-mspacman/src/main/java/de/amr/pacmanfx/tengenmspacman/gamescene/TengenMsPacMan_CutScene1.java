/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.spriteanim.SpriteAnimSystem;
import de.amr.pacmanfx.core.ecs.systems.world.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.entities.ActorAnimationID;
import de.amr.pacmanfx.core.model.entities.clapperboard.Clapperboard;
import de.amr.pacmanfx.core.model.entities.ghost.Ghost;
import de.amr.pacmanfx.core.model.entities.pac.Pac;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.entities.Heart;
import de.amr.pacmanfx.tengenmspacman.entities.clapperboard.TengenMsPacMan_ClapperboardStateSystem;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.input.JoypadButton;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import java.util.List;

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
        unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
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
        final GameAction quitAction = appContext().commonActions().gameFlowActions().actionLetGameStateExpire();
        actionBindings().bindActionToKeyCombination(quitAction, input().joypad().keyForButton(JoypadButton.START));
        createActors(gameContext());
    }

    @Override
    public void onDeactivate() {
        stopMusic();
    }

    @Override
    public void onTick(GameContext game) {
        final int tick = (int) gameState().timer().tickCount();
        switch (tick) {
            case TICK_CLAP -> {
                getReady(gameContext());
                clapperboard.show();
                TengenMsPacMan_ClapperboardStateSystem.startFlapAnimation(clapperboard);
                playMusic();
            }
            case TICK_EXPIRES -> {
                gameState().triggerTimeout();
                return;
            }
        }

        TengenMsPacMan_ClapperboardStateSystem.update(clapperboard);
        playCutScene(game, tick);
    }

    private void createActors(GameContext game) {
        final var actorFactory = TengenMsPacMan_ActorFactory.instance();
        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer animationContainer = appContext().ui().sprites().animations();

        clapperboard = new Clapperboard("1", "THEY MEET");
        msPacMan = actorFactory.createMsPacMan();
        msPacMan.spriteAnim().setAnimations(renderConfig.createPacAnimations(animationContainer));
        pacMan = actorFactory.createPacMan();
        pacMan.spriteAnim().setAnimations(renderConfig.createPacAnimations(animationContainer));
        inky = renderConfig.createAnimatedGhost(game, animationContainer, GhostPersonality.CYAN_GHOST_BASHFUL);
        pinky = renderConfig.createAnimatedGhost(game, animationContainer, GhostPersonality.PINK_GHOST_SPEEDY);
        heart = new Heart();
    }

    private void getReady(GameContext game) {
        final WorldNavigationSystem navigator = game.systems().worldNavigator();

        clapperboard.pos().set(3 * WorldMap.TS, 10 * WorldMap.TS);

        msPacMan.pos().set(RIGHT_BORDER, LOWER_LANE);
        navigator.setMoveDir(msPacMan, Direction.LEFT);
        navigator.setSpeed(msPacMan, 0);

        pacMan.pos().set(LEFT_BORDER, UPPER_LANE);
        navigator.setMoveDir(pacMan, Direction.RIGHT);
        navigator.setSpeed(pacMan, 0);

        inky.pos().set(LEFT_BORDER, UPPER_LANE);
        navigator.setMoveDir(inky, Direction.RIGHT);
        navigator.setWishDir(inky, Direction.RIGHT);
        navigator.setSpeed(inky, 0);

        pinky.pos().set(RIGHT_BORDER, LOWER_LANE);
        navigator.setMoveDir(pinky, Direction.LEFT);
        navigator.setWishDir(pinky, Direction.LEFT);
        navigator.setSpeed(pinky, 0);

        collided = false;
    }

    private void playMusic() {
        appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_1);
    }

    private void stopMusic() {
        appContext().ui().sounds().stop(PacManGameSoundID.INTERMISSION_1);
    }

    private void letActorsMove(GameContext game) {
        List.of(pacMan, msPacMan, inky, pinky).forEach(game.systems().motor()::move);
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
        final WorldNavigationSystem navigator = game.systems().worldNavigator();
        final SpriteAnimSystem animSystem = game.systems().spriteAnim();

        letActorsMove(game);

        if (tick == 130) {
            pacMan.show();
            navigator.setSpeed(pacMan, SPEED_CHASING);
            animSystem.select(pacMan, TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
            animSystem.playSelected(pacMan);

            msPacMan.show();
            navigator.setSpeed(msPacMan, SPEED_CHASING);
            animSystem.select(msPacMan, ActorAnimationID.PAC_MUNCHING);
            animSystem.playSelected(msPacMan);
        }
        else if (tick == 160) {
            inky.show();
            navigator.setSpeed(inky, SPEED_CHASING);
            animSystem.select(inky, ActorAnimationID.GHOST_NORMAL);
            animSystem.playSelected(inky);

            pinky.show();
            navigator.setSpeed(pinky, SPEED_CHASING);
            animSystem.select(pinky, ActorAnimationID.GHOST_NORMAL);
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
                navigator.setSpeed(pac, SPEED_RISING);
            });
        }
        else if (tick == 498) {
            collided = true;

            navigator.setMoveDir(inky, Direction.RIGHT);
            navigator.setWishDir(inky, Direction.RIGHT);
            navigator.setSpeed(inky, SPEED_AFTER_COLLISION);

            inky.movement().setVelocityY(inky.movement().velocityY() - 2.0f);
            inky.movement().setAcceleration(0, 0.4f);

            navigator.setMoveDir(pinky, Direction.LEFT);
            navigator.setWishDir(pinky, Direction.LEFT);
            navigator.setSpeed(pinky, SPEED_AFTER_COLLISION);

            pinky.movement().setVelocityY(pinky.movement().velocityY() - 2.0f);
            pinky.movement().setAcceleration(0, 0.4f);
        }
        else if (tick == 530) {
            inky.hide();
            pinky.hide();

            navigator.setSpeed(pacMan, 0);
            navigator.setMoveDir(pacMan, Direction.LEFT);
            navigator.setSpeed(msPacMan, 0);
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