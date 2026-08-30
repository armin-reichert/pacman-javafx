/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.Clapperboard;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.clapperboard.system.ClapperboardStateSystem;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.game.GameVariant;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import java.util.List;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 */
public class ArcadeMsPacMan_CutScene2 extends GameScene {

    static final int UPPER_Y  = TS * 12;
    static final int MIDDLE_Y = TS * 18;
    static final int LOWER_Y  = TS * 24;

    public Pac pacMan;
    public Pac msPacMan;
    public Clapperboard clapperboard;

    public ArcadeMsPacMan_CutScene2(GameAppContext app) {
        super(app);
        components().setComp(CanvasRenderingComp.class, new CanvasRenderingComp());
    }

    @Override
    public void onActivate() {
        initScene();
        setSceneState(SceneState.CLAPPERBOARD, 120);
    }

    @Override
    public void onTick(GameContext game) {
        final GameSystems sys = game.variant().systems();

        switch (state) {
            case SceneState.CLAPPERBOARD -> updateStateClapperboard(sys);
            case SceneState.CHASING -> updateStateChasing(sys);
            default -> throw new IllegalStateException("Illegal scene state: " + state);
        }
        sceneTimer.doTick();
    }

    private void initScene() {
        final GameVariant variant = app().gameVariants().currentGameVariant();
        final GameVariantRenderConfig renderConfig = variant.uiConfig().renderConfig();
        final SpriteAnimContainer animContainer    = variant.spriteAnimContainer();
        final var actorFactory = new ArcadeMsPacMan_ActorFactory();

        pacMan = actorFactory.createPacMan();
        pacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));

        msPacMan = actorFactory.createMsPacMan();
        msPacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));

        clapperboard = new Clapperboard("2", "THE CHASE");
        clapperboard.pos().set(tilesPx(3), tilesPx(10));
        ClapperboardStateSystem.startFlapAnimation(clapperboard);
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

    private void updateStateClapperboard(GameSystems systems) {
        ClapperboardStateSystem.update(clapperboard);
        if (sceneTimer.hasExpired()) {
            soundManager().play(PacManGameSoundID.INTERMISSION_2);
            enterStateChasing(systems);
        }
    }

    private void enterStateChasing(GameSystems systems) {
        systems.navigator().setMoveDir(pacMan, Direction.RIGHT);

        systems.actorSpriteAnimController().select(pacMan, CommonSpriteAnimationID.MR_PAC_MAN_MUNCHING);
        systems.actorSpriteAnimController().playSelected(pacMan);

        systems.navigator().setMoveDir(msPacMan, Direction.RIGHT);

        systems.actorSpriteAnimController().select(msPacMan, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
        systems.actorSpriteAnimController().playSelected(msPacMan);

        setSceneState(SceneState.CHASING, TickTimer.INDEFINITE);
    }

    private void updateStateChasing(GameSystems systems) {
        if (sceneTimer.atSecond(4.5)) {
            pacMan.pos().set(TS * (-2), UPPER_Y);
            pacMan.show();
            systems.navigator().setMoveDir(pacMan, Direction.RIGHT);
            systems.navigator().setMoveDirSpeed(pacMan, 2.0f);

            msPacMan.pos().set(TS * (-8), UPPER_Y);
            msPacMan.show();
            systems.navigator().setMoveDir(msPacMan, Direction.RIGHT);
            systems.navigator().setMoveDirSpeed(msPacMan, 2.0f);
        }
        else if (sceneTimer.atSecond(9)) {
            pacMan.pos().set(TS * 36, LOWER_Y);
            systems.navigator().setMoveDir(pacMan, Direction.LEFT);
            systems.navigator().setMoveDirSpeed(pacMan, 2.0f);

            msPacMan.pos().set(TS * 30, LOWER_Y);
            systems.navigator().setMoveDir(msPacMan, Direction.LEFT);
            systems.navigator().setMoveDirSpeed(msPacMan, 2.0f);
        }
        else if (sceneTimer.atSecond(13.5)) {
            pacMan.pos().set(TS * (-2), MIDDLE_Y);
            systems.navigator().setMoveDir(pacMan, Direction.RIGHT);
            systems.navigator().setMoveDirSpeed(pacMan, 2.0f);

            msPacMan.pos().set(TS * (-8), MIDDLE_Y);
            systems.navigator().setMoveDir(msPacMan, Direction.RIGHT);
            systems.navigator().setMoveDirSpeed(msPacMan, 2.0f);
        }
        else if (sceneTimer.atSecond(17.5)) {
            pacMan.pos().set(TS * 42, UPPER_Y);
            systems.navigator().setMoveDir(pacMan, Direction.LEFT);
            systems.navigator().setMoveDirSpeed(pacMan, 4.0f);

            msPacMan.pos().set(TS * 30, UPPER_Y);
            systems.navigator().setMoveDir(msPacMan, Direction.LEFT);
            systems.navigator().setMoveDirSpeed(msPacMan, 4.0f);
        }
        else if (sceneTimer.atSecond(18.5)) {
            pacMan.pos().set(TS * (-2), LOWER_Y);
            systems.navigator().setMoveDir(pacMan, Direction.RIGHT);
            systems.navigator().setMoveDirSpeed(pacMan, 4.0f);

            msPacMan.pos().set(TS * (-14), LOWER_Y);
            systems.navigator().setMoveDir(msPacMan, Direction.RIGHT);
            systems.navigator().setMoveDirSpeed(msPacMan, 4.0f);
        }
        else if (sceneTimer.atSecond(23)) {
            game().state().triggerTimeout();
        }
        else {
            List.of(pacMan, msPacMan).forEach(systems.motor()::move);
        }
    }
}