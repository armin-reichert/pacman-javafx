/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
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

    static final int UPPER_Y  = TS * 12;
    static final int MIDDLE_Y = TS * 18;
    static final int LOWER_Y  = TS * 24;

    public Pac pacMan;
    public Pac msPacMan;
    public Clapperboard clapperboard;

    public ArcadeMsPacMan_CutScene2(Game game) {
        super(game);
    }

    @Override
    public void onActivate() {
        initScene();
        setSceneState(SceneState.CLAPPERBOARD, 120);
    }

    @Override
    public void onTick(long tick) {
        switch (state) {
            case SceneState.CLAPPERBOARD -> updateStateClapperboard();
            case SceneState.CHASING -> updateStateChasing();
            default -> throw new IllegalStateException("Illegal scene state: " + state);
        }
        sceneTimer.doTick();
    }

    private void initScene() {
        final UIConfig uiConfig = game().currentUIConfig();
        final SpriteAnimationContainer spriteAnimations = game().ui().sprites().animations();
        pacMan = ArcadePacMan_GameModel.createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations(spriteAnimations));
        msPacMan = ArcadeMsPacMan_GameModel.createMsPacMan();
        msPacMan.setAnimations(uiConfig.createPacAnimations(spriteAnimations));
        clapperboard = new Clapperboard("2", "THE CHASE");
        clapperboard.setPosition(TS(3), TS(10));
        clapperboard.startAnimation();
    }

    // Scene controller state machine

    private enum SceneState { CLAPPERBOARD, CHASING }

    private SceneState state;
    private final TickTimer sceneTimer = new TickTimer("Timer-MsPacMan_CutScene2");

    private void setSceneState(SceneState state, long ticks) {
        this.state = state;
        sceneTimer.reset(ticks);
        sceneTimer.start();
    }

    private void updateStateClapperboard() {
        clapperboard.tick();
        if (sceneTimer.hasExpired()) {
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
        if (sceneTimer.atSecond(4.5)) {
            pacMan.setPosition(TS * (-2), UPPER_Y);
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setSpeed(2.0f);
            pacMan.show();

            msPacMan.setPosition(TS * (-8), UPPER_Y);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setSpeed(2.0f);
            msPacMan.show();
        }
        else if (sceneTimer.atSecond(9)) {
            pacMan.setPosition(TS * 36, LOWER_Y);
            pacMan.setMoveDir(Direction.LEFT);
            pacMan.setSpeed(2.0f);

            msPacMan.setPosition(TS * 30, LOWER_Y);
            msPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setSpeed(2.0f);
        }
        else if (sceneTimer.atSecond(13.5)) {
            pacMan.setPosition(TS * (-2), MIDDLE_Y);
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setSpeed(2.0f);

            msPacMan.setPosition(TS * (-8), MIDDLE_Y);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setSpeed(2.0f);
        }
        else if (sceneTimer.atSecond(17.5)) {
            pacMan.setPosition(TS * 42, UPPER_Y);
            pacMan.setMoveDir(Direction.LEFT);
            pacMan.setSpeed(4.0f);

            msPacMan.setPosition(TS * 30, UPPER_Y);
            msPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setSpeed(4.0f);
        }
        else if (sceneTimer.atSecond(18.5)) {
            pacMan.setPosition(TS * (-2), LOWER_Y);
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setSpeed(4.0f);

            msPacMan.setPosition(TS * (-14), LOWER_Y);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setSpeed(4.0f);
        }
        else if (sceneTimer.atSecond(23)) {
            gameState().expire();
        }
        else {
            pacMan.move();
            msPacMan.move();
        }
    }
}