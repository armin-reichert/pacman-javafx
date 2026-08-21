/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.ecs.systems.SpriteAnimController;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.Clapperboard;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.entities.clapperboard.TengenMsPacMan_ClapperboardStateSystem;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.JoypadButton;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantUIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantUIConfig.NES_SCREEN_WIDTH;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 */
public class TengenMsPacMan_CutScene2 extends GameScene {

    public static final int TICK_EXPIRES = 1380;

    private static final int UPPER_LANE = WorldMap.TS * 8;
    private static final int LOWER_LANE = WorldMap.TS * 22;
    private static final int MIDDLE_LANE = WorldMap.TS * 10;
    private static final int LEFT_BORDER = WorldMap.TS;
    private static final int RIGHT_BORDER = WorldMap.TS * 30;
    public static final int TICK_CLAP = 2;

    private Clapperboard clapperboard;
    private Pac pacMan;
    private Pac msPacMan;

    public TengenMsPacMan_CutScene2(GameAppContext appContext) {
        super(appContext);
        rendering2D().unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        rendering2D().unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
    }

    public Clapperboard clapperboard() {
        return clapperboard;
    }

    public Pac pacMan() {
        return pacMan;
    }

    public Pac msPacMan() {
        return msPacMan;
    }

    @Override
    public void onActivate() {

        // Quit cut scene when "START" button on "joypad" is pressed
        final Joypad joypad = app().input().joypad();

        final var bindingsMap = actionBindingsSupport().bindingsMap();
        bindingsMap.bindActionToKeyCombination(
            app().commonActions().gameFlowActions().actionLetGameStateExpire(),
            joypad.keyForButton(JoypadButton.START));

        createActors();
    }

    @Override
    public void onDeactivate() {
        stopMusic();
    }

    @Override
    public void onTick(GameContext game) {
        final long tick = game().state().timer().tickCount();

        if (tick == TICK_CLAP) {
            clapperboard.show();
            clapperboard.pos().set(3 * WorldMap.TS, 10 * WorldMap.TS);
            TengenMsPacMan_ClapperboardStateSystem.startFlapAnimation(clapperboard);
            playMusic();
        }
        else if (tick == TICK_EXPIRES) {
            game().state().triggerTimeout();
        }

        TengenMsPacMan_ClapperboardStateSystem.update(clapperboard);
        playCutScene(game, tick);
    }

    private void playMusic() {
        app().ui().sounds().play(PacManGameSoundID.INTERMISSION_2);
    }

    private void stopMusic() {
        app().ui().sounds().stop(PacManGameSoundID.INTERMISSION_2);
    }

    private void createActors() {
        final var actorFactory = TengenMsPacMan_ActorFactory.instance();
        final GameVariantRenderConfig renderConfig = app().gameVariants().currentGameVariant().uiConfig().renderConfig();
        final SpriteAnimationContainer animationContainer = app().ui().sprites().animations();

        clapperboard = new Clapperboard("2", "THE CHASE");

        msPacMan = actorFactory.createMsPacMan();
        msPacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animationContainer));

        pacMan = actorFactory.createPacMan();
        pacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animationContainer));
    }

    private void playCutScene(GameContext game, long tick) {
        final WorldNavigationSystem navigator = game.variant().systems().worldNavigator();
        final SpriteAnimController animSystem = game.variant().systems().spriteAnimController();

        letActorsMove(game);

            if (tick == 270) {
                msPacMan.pos().set(LEFT_BORDER, UPPER_LANE);
                msPacMan.show();

                navigator.setMoveDirSpeed(msPacMan, 2.0f);
                navigator.setMoveDir(msPacMan, Direction.RIGHT);

                animSystem.select(msPacMan, CommonSpriteAnimationID.PAC_MUNCHING);
                animSystem.playSelected(msPacMan);
            }
            else if (tick == 320) {
                pacMan.pos().set(LEFT_BORDER, UPPER_LANE);
                pacMan.show();

                navigator.setMoveDirSpeed(pacMan, 2.0f);
                navigator.setMoveDir(pacMan, Direction.RIGHT);

                animSystem.select(pacMan,TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
                animSystem.playSelected(pacMan);
            }
            else if (tick == 520) {
                pacMan.pos().set(RIGHT_BORDER, LOWER_LANE);
                navigator.setMoveDir(pacMan, Direction.LEFT);
                navigator.setMoveDirSpeed(pacMan, 2.0f);
            }
            else if (tick == 570) {
                msPacMan.pos().set(RIGHT_BORDER, LOWER_LANE);
                navigator.setMoveDir(msPacMan, Direction.LEFT);
                navigator.setMoveDirSpeed(msPacMan, 2.0f);
            }
            else if (tick == 780) {
                msPacMan.pos().set(LEFT_BORDER, MIDDLE_LANE);
                navigator.setMoveDir(msPacMan, Direction.RIGHT);
                navigator.setMoveDirSpeed(msPacMan, 2.0f);
            }
            else if (tick == 830) {
                pacMan.pos().set(LEFT_BORDER, MIDDLE_LANE);
                navigator.setMoveDir(pacMan, Direction.RIGHT);
                navigator.setMoveDirSpeed(pacMan, 2.0f);
            }
            else if (tick == 1040) {
                pacMan.pos().set(RIGHT_BORDER, UPPER_LANE);
                navigator.setMoveDir(pacMan, Direction.LEFT);
                navigator.setMoveDirSpeed(pacMan, 4.0f); //TODO correct?
            }
            else if (tick == 1055) {
                msPacMan.pos().set(RIGHT_BORDER, UPPER_LANE);
                navigator.setMoveDir(msPacMan, Direction.LEFT);
                navigator.setMoveDirSpeed(msPacMan, 4.0f);
            }
            else if (tick == 105) {
                msPacMan.pos().set(LEFT_BORDER, LOWER_LANE);
                navigator.setMoveDir(msPacMan, Direction.RIGHT);
                navigator.setMoveDirSpeed(msPacMan, 4.0f);
            }
            else if (tick == 1120) {
                pacMan.pos().set(LEFT_BORDER, LOWER_LANE);
                navigator.setMoveDir(pacMan, Direction.RIGHT);
                navigator.setMoveDirSpeed(pacMan, 4.0f);
            }
    }

    private void letActorsMove(GameContext game) {
        final MovementSystem motor = game.variant().systems().motor();
        motor.move(pacMan);
        motor.move(msPacMan);
    }
}