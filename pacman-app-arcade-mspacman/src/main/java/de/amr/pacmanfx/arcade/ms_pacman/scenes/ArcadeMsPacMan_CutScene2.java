/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationSet;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.model.actors.ArcadeMsPacMan_AnimationID;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import static de.amr.pacmanfx.model.world.WorldMap.TS;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 */
public class ArcadeMsPacMan_CutScene2 extends GameScene2D {

    private static final int UPPER_LANE_Y  = TS * 12;
    private static final int MIDDLE_LANE_Y = TS * 18;
    private static final int LOWER_LANE_Y  = TS * 24;

    private Pac pacMan;
    private Pac msPacMan;

    private Clapperboard clapperboard;

    public ArcadeMsPacMan_CutScene2(Game game) {
        super(game);
    }

    public Pac pacMan() {
        return pacMan;
    }

    public Pac msPacMan() {
        return msPacMan;
    }

    public Clapperboard clapperboard() {
        return clapperboard;
    }

    @Override
    public void onActivate() {
        final UIConfig uiConfig = game().currentUIConfig();
        final SpriteAnimationSet spriteAnimations = game().ui().sprites().animationSet();
        pacMan = ArcadePacMan_GameModel.createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations(spriteAnimations));
        msPacMan = ArcadeMsPacMan_GameModel.createMsPacMan();
        msPacMan.setAnimations(uiConfig.createPacAnimations(spriteAnimations));
        clapperboard = new Clapperboard("2", "THE CHASE");
        clapperboard.setPosition(TS(3), TS(10));
        clapperboard.startAnimation();
        setSceneState(SceneState.CLAPPERBOARD, 120);
    }

    @Override
    public void onTick(long tick) {
        switch (state) {
            case SceneState.CLAPPERBOARD -> updateStateClapperboard();
            case SceneState.CHASING -> updateStateChasing();
            default -> throw new IllegalStateException("Illegal scene state: " + state);
        }
        timer.doTick();
    }

    // Scene controller state machine

    private enum SceneState { CLAPPERBOARD, CHASING }

    private SceneState state;
    private final TickTimer timer = new TickTimer("MsPacMan_CutScene2");

    private void setSceneState(SceneState state, long ticks) {
        this.state = state;
        timer.reset(ticks);
        timer.start();
    }

    private void updateStateClapperboard() {
        clapperboard.tick();
        if (timer.hasExpired()) {
            game().ui().sounds().play(PacManGameSoundID.INTERMISSION_2);
            enterStateChasing();
        }
    }

    private void enterStateChasing() {
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.animations().select(ArcadeMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
        pacMan.animations().playSelected();

        msPacMan.setMoveDir(Direction.RIGHT);
        msPacMan.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
        msPacMan.animations().playSelected();

        setSceneState(SceneState.CHASING, TickTimer.INDEFINITE);
    }

    private void updateStateChasing() {
        if (timer.atSecond(4.5)) {
            pacMan.setPosition(TS * (-2), UPPER_LANE_Y);
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setSpeed(2.0f);
            pacMan.show();

            msPacMan.setPosition(TS * (-8), UPPER_LANE_Y);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setSpeed(2.0f);
            msPacMan.show();
        }
        else if (timer.atSecond(9)) {
            pacMan.setPosition(TS * 36, LOWER_LANE_Y);
            pacMan.setMoveDir(Direction.LEFT);
            pacMan.setSpeed(2.0f);

            msPacMan.setPosition(TS * 30, LOWER_LANE_Y);
            msPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setSpeed(2.0f);
        }
        else if (timer.atSecond(13.5)) {
            pacMan.setPosition(TS * (-2), MIDDLE_LANE_Y);
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setSpeed(2.0f);

            msPacMan.setPosition(TS * (-8), MIDDLE_LANE_Y);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setSpeed(2.0f);
        }
        else if (timer.atSecond(17.5)) {
            pacMan.setPosition(TS * 42, UPPER_LANE_Y);
            pacMan.setMoveDir(Direction.LEFT);
            pacMan.setSpeed(4.0f);

            msPacMan.setPosition(TS * 30, UPPER_LANE_Y);
            msPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setSpeed(4.0f);
        }
        else if (timer.atSecond(18.5)) {
            pacMan.setPosition(TS * (-2), LOWER_LANE_Y);
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setSpeed(4.0f);

            msPacMan.setPosition(TS * (-14), LOWER_LANE_Y);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setSpeed(4.0f);
        }
        else if (timer.atSecond(23)) {
            gameState().expire();
        }
        else {
            pacMan.move();
            msPacMan.move();
        }
    }
}