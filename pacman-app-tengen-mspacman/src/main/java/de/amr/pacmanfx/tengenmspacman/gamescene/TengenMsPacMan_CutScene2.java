/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.common.GameSystems;
import de.amr.pacmanfx.core.model.entities.ActorAnimationID;
import de.amr.pacmanfx.core.model.entities.pac.Pac;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.entities.clapperboard.Clapperboard;
import de.amr.pacmanfx.tengenmspacman.entities.clapperboard.ClapperboardStateSystem;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.JoypadButton;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantConfig.NES_SCREEN_WIDTH;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 */
public class TengenMsPacMan_CutScene2 extends AbstractGameScene2D {

    public static final int TICK_EXPIRES = 1380;

    private static final int UPPER_LANE = WorldMap.TS * 8;
    private static final int LOWER_LANE = WorldMap.TS * 22;
    private static final int MIDDLE_LANE = WorldMap.TS * 10;
    private static final int LEFT_BORDER = WorldMap.TS;
    private static final int RIGHT_BORDER = WorldMap.TS * 30;

    private Clapperboard clapperboard;
    private Pac pacMan;
    private Pac msPacMan;

    public TengenMsPacMan_CutScene2(GameAppContext appContext) {
        super(appContext);
        unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
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
        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer spriteAnimations = appContext().ui().sprites().animations();

        // Quit cut scene when "START" button on "joypad" is pressed
        final Joypad joypad = input().joypad();
        actionBindings().bindActionToKeyCombination(appContext().commonActions().gameFlowActions().actionLetGameStateExpire(),
            joypad.keyForButton(JoypadButton.START));

        clapperboard = new Clapperboard("2", "THE CHASE");
        clapperboard.pos().set(3 * WorldMap.TS, 10 * WorldMap.TS);

        final var factory = TengenMsPacMan_ActorFactory.instance();

        msPacMan = factory.createMsPacMan();
        msPacMan.spriteAnim().setAnimations(renderConfig.createPacAnimations(spriteAnimations));

        pacMan = factory.createPacMan();
        pacMan.spriteAnim().setAnimations(renderConfig.createPacAnimations(spriteAnimations));

        appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_2);
    }

    @Override
    public void onDeactivate() {
        appContext().ui().sounds().stop(PacManGameSoundID.INTERMISSION_2);
    }

    @Override
    public void onTick(GameContext gameContext) {
        final GameSystems sys = gameContext.systems();

        final long gameStateTick = gameState().timer().tickCount();

        if (gameStateTick <= TICK_EXPIRES) {
            switch ((int) gameStateTick) {
                case 0 -> {
                    clapperboard.show();
                    ClapperboardStateSystem.startFlapAnimation(clapperboard);
                }
                case 270 -> {
                    msPacMan.pos().set(LEFT_BORDER, UPPER_LANE);
                    msPacMan.show();

                    sys.worldNavigator().setSpeed(msPacMan, 2.0f);
                    sys.worldNavigator().setMoveDir(msPacMan, Direction.RIGHT);

                    sys.spriteAnim().select(msPacMan, ActorAnimationID.PAC_MUNCHING);
                    sys.spriteAnim().playSelected(msPacMan);
                }
                case 320 -> {
                    pacMan.pos().set(LEFT_BORDER, UPPER_LANE);
                    pacMan.show();

                    sys.worldNavigator().setSpeed(pacMan, 2.0f);
                    sys.worldNavigator().setMoveDir(pacMan, Direction.RIGHT);

                    sys.spriteAnim().select(pacMan,TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
                    sys.spriteAnim().playSelected(pacMan);
                }
                case 520 -> {
                    pacMan.pos().set(RIGHT_BORDER, LOWER_LANE);
                    sys.worldNavigator().setMoveDir(pacMan, Direction.LEFT);
                    sys.worldNavigator().setSpeed(pacMan, 2.0f);
                }
                case 570 -> {
                    msPacMan.pos().set(RIGHT_BORDER, LOWER_LANE);
                    sys.worldNavigator().setMoveDir(msPacMan, Direction.LEFT);
                    sys.worldNavigator().setSpeed(msPacMan, 2.0f);
                }
                case 780 -> {
                    msPacMan.pos().set(LEFT_BORDER, MIDDLE_LANE);
                    sys.worldNavigator().setMoveDir(msPacMan, Direction.RIGHT);
                    sys.worldNavigator().setSpeed(msPacMan, 2.0f);
                }
                case 830 -> {
                    pacMan.pos().set(LEFT_BORDER, MIDDLE_LANE);
                    sys.worldNavigator().setMoveDir(pacMan, Direction.RIGHT);
                    sys.worldNavigator().setSpeed(pacMan, 2.0f);
                }
                case 1040 -> {
                    pacMan.pos().set(RIGHT_BORDER, UPPER_LANE);
                    sys.worldNavigator().setMoveDir(pacMan, Direction.LEFT);
                    sys.worldNavigator().setSpeed(pacMan, 4.0f); //TODO correct?
                }
                case 1055 -> {
                    msPacMan.pos().set(RIGHT_BORDER, UPPER_LANE);
                    sys.worldNavigator().setMoveDir(msPacMan, Direction.LEFT);
                    sys.worldNavigator().setSpeed(msPacMan, 4.0f);
                }
                case 1105 -> {
                    msPacMan.pos().set(LEFT_BORDER, LOWER_LANE);
                    sys.worldNavigator().setMoveDir(msPacMan, Direction.RIGHT);
                    sys.worldNavigator().setSpeed(msPacMan, 4.0f);
                }
                case 1120 -> {
                    pacMan.pos().set(LEFT_BORDER, LOWER_LANE);
                    sys.worldNavigator().setMoveDir(pacMan, Direction.RIGHT);
                    sys.worldNavigator().setSpeed(pacMan, 4.0f);
                }
                case 1380 -> gameState().triggerTimeout();
            }

            sys.motor().move(pacMan);
            sys.motor().move(msPacMan);

            ClapperboardStateSystem.update(clapperboard);
        }
    }
}