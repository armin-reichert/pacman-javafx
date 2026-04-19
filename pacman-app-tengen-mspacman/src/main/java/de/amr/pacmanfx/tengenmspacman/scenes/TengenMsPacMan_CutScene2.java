/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.basics.fsm.State;
import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengenmspacman.model.actor.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.input.JoypadButton;
import de.amr.pacmanfx.ui.sound.SoundID;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_LET_GAME_STATE_EXPIRE;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 */
public class TengenMsPacMan_CutScene2 extends GameScene2D {

    public static final Vector2i SIZE = new Vector2i(NES_SCREEN_WIDTH, NES_SCREEN_HEIGHT);

    public static final int TICK_EXPIRES = 1380;

    private static final int UPPER_LANE = TS * 8;
    private static final int LOWER_LANE = TS * 22;
    private static final int MIDDLE_LANE = TS * 10;
    private static final int LEFT_BORDER = TS;
    private static final int RIGHT_BORDER = TS * 30;

    private Clapperboard clapperboard;
    private Pac pacMan;
    private Pac msPacMan;

    public TengenMsPacMan_CutScene2(GameUI ui) {
        super(ui);
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
    public void onSceneStart() {
        final UIConfig uiConfig = ui.currentConfig();

        actionBindings.add(
            ACTION_LET_GAME_STATE_EXPIRE,
            Input.instance().joypad.keyForButton(JoypadButton.START)
        );

        clapperboard = new Clapperboard(2, "THE CHASE");
        clapperboard.setPosition(3 * TS, 10 * TS);
        clapperboard.show();
        clapperboard.startAnimation();

        msPacMan = TengenMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAnimations(uiConfig.createPacAnimations());

        pacMan = TengenMsPacMan_ActorFactory.createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations());

        ui.soundManager().play(SoundID.INTERMISSION_2);
    }

    @Override
    public void onSceneEnd() {
        ui.soundManager().stop(SoundID.INTERMISSION_2);
    }

    @Override
    public void onTick(long tick) {
        final State<Game> gameState = gameContext().game().flow().state();
        final long gameStateTick = gameState.timer().tickCount();
        if (gameStateTick <= TICK_EXPIRES) {
            switch ((int) gameStateTick) {
                case 270 -> {
                    msPacMan.setPosition(LEFT_BORDER, UPPER_LANE);
                    msPacMan.setMoveDir(Direction.RIGHT);
                    msPacMan.selectAnimation(ArcadePacMan_AnimationID.PAC_MUNCHING);
                    msPacMan.playAnimation();
                    msPacMan.setSpeed(2.0f);
                    msPacMan.show();
                }
                case 320 -> {
                    pacMan.setMoveDir(Direction.RIGHT);
                    pacMan.selectAnimation(TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
                    pacMan.playAnimation();
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
                case 1380 -> gameState.expire();
            }

            pacMan.move();
            msPacMan.move();
            clapperboard.tick();
        }
    }

    @Override
    public Vector2i unscaledSceneSize() { return SIZE; }
}