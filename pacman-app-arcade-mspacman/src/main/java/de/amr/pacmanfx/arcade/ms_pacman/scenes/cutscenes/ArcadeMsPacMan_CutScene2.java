/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes.cutscenes;

import de.amr.basics.math.Direction;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.Clapperboard;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.clapperboard.system.ClapperboardStateSystem;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import java.util.List;
import java.util.stream.Stream;

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

    private Pac pacMan;
    private Pac msPacMan;
    private Clapperboard clapperboard;

    private final ClapperboardStateSystem clapperboardSystem = new ClapperboardStateSystem();

    public ArcadeMsPacMan_CutScene2(GameAppContext app) {
        super(app);
        setComp(SceneCanvasRenderingComp.class, new SceneCanvasRenderingComp());
    }

    @Override
    public void onActivate() {
        initScene();
        setSceneState(SceneState.CLAPPERBOARD, 120);
    }

    @Override
    public void onTick(GameContext game) {
        final GameSystems systems = game.variantPlayConfig().systems();

        switch (state) {
            case SceneState.CLAPPERBOARD -> updateStateClapperboard(systems);
            case SceneState.CHASING -> updateStateChasing(systems);
            default -> throw new IllegalStateException("Illegal scene state: " + state);
        }
        sceneTimer.doTick();
    }

    public Stream<Renderable> renderables() {
        return Stream.of(clapperboard, msPacMan, pacMan);
    }

    private void initScene() {
        final GameVariantConfig variant = app().variantManager().currentVariantConfig();
        final GameVariantRenderConfig renderConfig = variant.uiConfig().renderConfig();
        final SpriteAnimContainer animContainer    = variant.spriteAnimContainer();
        final var actorFactory = new ArcadeMsPacMan_ActorFactory();

        pacMan = actorFactory.createPacMan();
        pacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));

        msPacMan = actorFactory.createMsPacMan();
        msPacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));

        clapperboard = new Clapperboard("2", "THE CHASE");
        clapperboard.pos().set(tilesPx(3), tilesPx(10));
        clapperboardSystem.startFlapAnimation(clapperboard);
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
        clapperboardSystem.update(clapperboard);
        if (sceneTimer.hasExpired()) {
            soundManager().play(PacManGameSoundID.INTERMISSION_2);
            enterStateChasing(systems);
        }
    }

    private void enterStateChasing(GameSystems systems) {
        final WorldNavigationSystem nav = systems.navigator();
        final ActorSpriteAnimController animController = systems.actorSpriteAnimController();

        nav.setMoveDir(pacMan, Direction.RIGHT);

        animController.select(pacMan, CommonSpriteAnimationID.MR_PAC_MAN_MUNCHING);
        animController.playSelected(pacMan);

        nav.setMoveDir(msPacMan, Direction.RIGHT);

        animController.select(msPacMan, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
        animController.playSelected(msPacMan);

        setSceneState(SceneState.CHASING, TickTimer.INDEFINITE);
    }

    private void updateStateChasing(GameSystems systems) {
        final MovementSystem motor = systems.motor();
        final WorldNavigationSystem nav = systems.navigator();
        
        if (sceneTimer.atSecond(4.5)) {
            pacMan.pos().set(TS * (-2), UPPER_Y);
            pacMan.show();
            nav.setMoveDir(pacMan, Direction.RIGHT);
            nav.setMoveDirSpeed(pacMan, 2.0f);

            msPacMan.pos().set(TS * (-8), UPPER_Y);
            msPacMan.show();
            nav.setMoveDir(msPacMan, Direction.RIGHT);
            nav.setMoveDirSpeed(msPacMan, 2.0f);
        }
        else if (sceneTimer.atSecond(9)) {
            pacMan.pos().set(TS * 36, LOWER_Y);
            nav.setMoveDir(pacMan, Direction.LEFT);
            nav.setMoveDirSpeed(pacMan, 2.0f);

            msPacMan.pos().set(TS * 30, LOWER_Y);
            nav.setMoveDir(msPacMan, Direction.LEFT);
            nav.setMoveDirSpeed(msPacMan, 2.0f);
        }
        else if (sceneTimer.atSecond(13.5)) {
            pacMan.pos().set(TS * (-2), MIDDLE_Y);
            nav.setMoveDir(pacMan, Direction.RIGHT);
            nav.setMoveDirSpeed(pacMan, 2.0f);

            msPacMan.pos().set(TS * (-8), MIDDLE_Y);
            nav.setMoveDir(msPacMan, Direction.RIGHT);
            nav.setMoveDirSpeed(msPacMan, 2.0f);
        }
        else if (sceneTimer.atSecond(17.5)) {
            pacMan.pos().set(TS * 42, UPPER_Y);
            nav.setMoveDir(pacMan, Direction.LEFT);
            nav.setMoveDirSpeed(pacMan, 4.0f);

            msPacMan.pos().set(TS * 30, UPPER_Y);
            nav.setMoveDir(msPacMan, Direction.LEFT);
            nav.setMoveDirSpeed(msPacMan, 4.0f);
        }
        else if (sceneTimer.atSecond(18.5)) {
            pacMan.pos().set(TS * (-2), LOWER_Y);
            nav.setMoveDir(pacMan, Direction.RIGHT);
            nav.setMoveDirSpeed(pacMan, 4.0f);

            msPacMan.pos().set(TS * (-14), LOWER_Y);
            nav.setMoveDir(msPacMan, Direction.RIGHT);
            nav.setMoveDirSpeed(msPacMan, 4.0f);
        }
        else if (sceneTimer.atSecond(23)) {
            game().state().triggerTimeout();
        }
        else {
            List.of(pacMan, msPacMan).forEach(motor::move);
        }
    }
}