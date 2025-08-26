/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_ActorRenderer;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_HUDRenderer;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;

import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel.createMsPacMan;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel.createPacMan;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_PacAnimationManager.PAC_MAN_MUNCHING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 */
public class ArcadeMsPacMan_CutScene2 extends GameScene2D {

    private static final String MUSIC_ID = "audio.intermission.2";

    private static final int UPPER_LANE_Y  = TS * 12;
    private static final int MIDDLE_LANE_Y = TS * 18;
    private static final int LOWER_LANE_Y  = TS * 24;

    private Pac pacMan;
    private Pac msPacMan;

    private Clapperboard clapperboard;

    private ArcadeMsPacMan_HUDRenderer hudRenderer;
    private ArcadeMsPacMan_ActorRenderer actorSpriteRenderer;

    public ArcadeMsPacMan_CutScene2(GameUI ui) {
        super(ui);
    }
    
    @Override
    public void doInit() {
        GameUI_Config uiConfig = ui.currentConfig();

        hudRenderer = (ArcadeMsPacMan_HUDRenderer) uiConfig.createHUDRenderer(canvas);
        actorSpriteRenderer = (ArcadeMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas);

        bindRendererProperties(hudRenderer, actorSpriteRenderer);

        context().game().hud().scoreVisible(true).levelCounterVisible(true).livesCounterVisible(false);

        pacMan = createPacMan();
        msPacMan = createMsPacMan();

        msPacMan.setAnimations(ui.currentConfig().createPacAnimations(msPacMan));
        pacMan.setAnimations(ui.currentConfig().createPacAnimations(pacMan));

        clapperboard = new Clapperboard("2", "THE CHASE");
        clapperboard.setPosition(TS(3), TS(10));
        clapperboard.setFont(actorSpriteRenderer.arcadeFontTS());
        clapperboard.startAnimation();

        setSceneState(STATE_CLAPPERBOARD, 120);
    }

    @Override
    protected void doEnd() {
        ui.soundManager().stop(MUSIC_ID);
    }

    @Override
    public void update() {
        switch (sceneState) {
            case STATE_CLAPPERBOARD -> updateStateClapperboard();
            case STATE_CHASING -> updateStateChasing();
            default -> throw new IllegalStateException("Illegal scene state: " + sceneState);
        }
        sceneTimer.doTick();
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawHUD() {
        if (hudRenderer != null) {
            hudRenderer.drawHUD(context(), context().game().hud(), sizeInPx());
        }
    }

    @Override
    public void drawSceneContent() {
        if (actorSpriteRenderer != null) {
            Stream.of(clapperboard, msPacMan, pacMan).forEach(actor -> actorSpriteRenderer.drawActor(actor));
        }
    }

    // Scene controller state machine

    private static final byte STATE_CLAPPERBOARD = 0;
    private static final byte STATE_CHASING = 1;

    private byte sceneState;
    private final TickTimer sceneTimer = new TickTimer("MsPacMan_CutScene2");

    private void setSceneState(byte state, long ticks) {
        sceneState = state;
        sceneTimer.reset(ticks);
        sceneTimer.start();
    }

    private void updateStateClapperboard() {
        clapperboard.tick();
        if (sceneTimer.hasExpired()) {
            ui.soundManager().play(MUSIC_ID);
            enterStateChasing();
        }
    }

    private void enterStateChasing() {
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.playAnimation(PAC_MAN_MUNCHING);

        msPacMan.setMoveDir(Direction.RIGHT);
        msPacMan.playAnimation(ANIM_PAC_MUNCHING);

        setSceneState(STATE_CHASING, TickTimer.INDEFINITE);
    }

    private void updateStateChasing() {
        if (sceneTimer.atSecond(4.5)) {
            pacMan.setPosition(TS * (-2), UPPER_LANE_Y);
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setSpeed(2.0f);
            pacMan.show();

            msPacMan.setPosition(TS * (-8), UPPER_LANE_Y);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setSpeed(2.0f);
            msPacMan.show();
        }
        else if (sceneTimer.atSecond(9)) {
            pacMan.setPosition(TS * 36, LOWER_LANE_Y);
            pacMan.setMoveDir(Direction.LEFT);
            pacMan.setSpeed(2.0f);

            msPacMan.setPosition(TS * 30, LOWER_LANE_Y);
            msPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setSpeed(2.0f);
        }
        else if (sceneTimer.atSecond(13.5)) {
            pacMan.setPosition(TS * (-2), MIDDLE_LANE_Y);
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setSpeed(2.0f);

            msPacMan.setPosition(TS * (-8), MIDDLE_LANE_Y);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setSpeed(2.0f);
        }
        else if (sceneTimer.atSecond(17.5)) {
            pacMan.setPosition(TS * 42, UPPER_LANE_Y);
            pacMan.setMoveDir(Direction.LEFT);
            pacMan.setSpeed(4.0f);

            msPacMan.setPosition(TS * 30, UPPER_LANE_Y);
            msPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setSpeed(4.0f);
        }
        else if (sceneTimer.atSecond(18.5)) {
            pacMan.setPosition(TS * (-2), LOWER_LANE_Y);
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setSpeed(4.0f);

            msPacMan.setPosition(TS * (-14), LOWER_LANE_Y);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setSpeed(4.0f);
        }
        else if (sceneTimer.atSecond(23)) {
            context().gameController().letCurrentGameStateExpire();
        }
        else {
            pacMan.move();
            msPacMan.move();
        }
    }
}