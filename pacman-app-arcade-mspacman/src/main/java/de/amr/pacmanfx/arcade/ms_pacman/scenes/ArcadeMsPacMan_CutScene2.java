/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.model.actors.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_CutScene2_Renderer;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_HUDRenderer;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;

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

    private ArcadeMsPacMan_HUDRenderer hudRenderer;
    private ArcadeMsPacMan_CutScene2_Renderer sceneRenderer;

    public ArcadeMsPacMan_CutScene2(GameUI ui) {
        super(ui);
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        final GameUI_Config uiConfig = ui.currentConfig();

        hudRenderer = configureRenderer(
            (ArcadeMsPacMan_HUDRenderer) uiConfig.createHUDRenderer(canvas));

        sceneRenderer = configureRenderer(
            new ArcadeMsPacMan_CutScene2_Renderer(this, canvas));
    }

    @Override
    public ArcadeMsPacMan_HUDRenderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public ArcadeMsPacMan_CutScene2_Renderer sceneRenderer() {
        return sceneRenderer;
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
    public void doInit() {
        context().currentGame().hud().scoreVisible(true).levelCounterVisible(true).livesCounterVisible(false);

        final GameUI_Config uiConfig = ui.currentConfig();

        pacMan = ArcadeMsPacMan_ActorFactory.createPacMan();
        pacMan.setAnimationManager(uiConfig.createPacAnimations());

        msPacMan = ArcadeMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAnimationManager(uiConfig.createPacAnimations());

        clapperboard = new Clapperboard("2", "THE CHASE");
        clapperboard.setPosition(TS(3), TS(10));
        clapperboard.startAnimation();

        setSceneState(SceneState.CLAPPERBOARD, 120);
    }

    @Override
    protected void doEnd() {
    }

    @Override
    public void update() {
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
            ui.soundManager().play(SoundID.INTERMISSION_2);
            enterStateChasing();
        }
    }

    private void enterStateChasing() {
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.playAnimation(ArcadeMsPacMan_UIConfig.AnimationID.PAC_MAN_MUNCHING);

        msPacMan.setMoveDir(Direction.RIGHT);
        msPacMan.playAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);

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
            context().currentGame().terminateCurrentGameState();
        }
        else {
            pacMan.move();
            msPacMan.move();
        }
    }
}