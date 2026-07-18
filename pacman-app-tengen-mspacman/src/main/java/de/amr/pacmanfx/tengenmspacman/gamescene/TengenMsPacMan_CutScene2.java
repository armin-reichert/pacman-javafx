/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.gameplay.FrameContext;
import de.amr.pacmanfx.core.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
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

        clapperboard = new Clapperboard(2, "THE CHASE");
        clapperboard.setPosition(3 * WorldMap.TS, 10 * WorldMap.TS);
        clapperboard.show();
        clapperboard.startAnimation();

        msPacMan = TengenMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAnimations(renderConfig.createPacAnimations(spriteAnimations));

        pacMan = TengenMsPacMan_ActorFactory.createPacMan();
        pacMan.setAnimations(renderConfig.createPacAnimations(spriteAnimations));

        appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_2);
    }

    @Override
    public void onDeactivate() {
        appContext().ui().sounds().stop(PacManGameSoundID.INTERMISSION_2);
    }

    @Override
    public void onTick(FrameContext frame) {
        final long gameStateTick = gameState().timer().tickCount();
        if (gameStateTick <= TICK_EXPIRES) {
            switch ((int) gameStateTick) {
                case 270 -> {
                    msPacMan.setPosition(LEFT_BORDER, UPPER_LANE);
                    msPacMan.setMoveDir(Direction.RIGHT);
                    msPacMan.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
                    msPacMan.animations().playSelected();
                    msPacMan.setSpeed(2.0f);
                    msPacMan.show();
                }
                case 320 -> {
                    pacMan.setMoveDir(Direction.RIGHT);
                    pacMan.animations().select(TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
                    pacMan.animations().playSelected();
                    pacMan.setPosition(LEFT_BORDER, UPPER_LANE);
                    pacMan.setMoveDir(Direction.RIGHT);
                    pacMan.setSpeed(2.0f);
                    pacMan.show();
                }
                case 520 -> {
                    pacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
                    pacMan.setMoveDir(Direction.LEFT);
                    pacMan.setSpeed(2.0f);
                }
                case 570 -> {
                    msPacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
                    msPacMan.setMoveDir(Direction.LEFT);
                    msPacMan.setSpeed(2.0f);
                }
                case 780 -> {
                    msPacMan.setPosition(LEFT_BORDER, MIDDLE_LANE);
                    msPacMan.setMoveDir(Direction.RIGHT);
                    msPacMan.setSpeed(2.0f);
                }
                case 830 -> {
                    pacMan.setPosition(LEFT_BORDER, MIDDLE_LANE);
                    pacMan.setMoveDir(Direction.RIGHT);
                    pacMan.setSpeed(2.0f);
                }
                case 1040 -> {
                    pacMan.setPosition(RIGHT_BORDER, UPPER_LANE);
                    pacMan.setMoveDir(Direction.LEFT);
                    pacMan.setSpeed(4.0f); //TODO correct?
                }
                case 1055 -> {
                    msPacMan.setPosition(RIGHT_BORDER, UPPER_LANE);
                    msPacMan.setMoveDir(Direction.LEFT);
                    msPacMan.setSpeed(4.0f);
                }
                case 1105 -> {
                    msPacMan.setPosition(LEFT_BORDER, LOWER_LANE);
                    msPacMan.setMoveDir(Direction.RIGHT);
                    msPacMan.setSpeed(4.0f);
                }
                case 1120 -> {
                    pacMan.setPosition(LEFT_BORDER, LOWER_LANE);
                    pacMan.setMoveDir(Direction.RIGHT);
                    pacMan.setSpeed(4.0f);
                }
                case 1380 -> gameState().triggerTimeout();
            }

            pacMan.move();
            msPacMan.move();
            clapperboard.tick();
        }
    }
}