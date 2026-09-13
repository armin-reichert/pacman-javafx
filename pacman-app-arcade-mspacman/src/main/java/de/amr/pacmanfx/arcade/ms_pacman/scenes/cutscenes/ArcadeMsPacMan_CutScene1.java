/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.scenes.cutscenes;

import de.amr.basics.math.Direction;
import de.amr.basics.timer.TickTimer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.ms_pacman.entities.Heart;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.Clapperboard;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.clapperboard.system.ClapperboardStateSystem;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 */
public class ArcadeMsPacMan_CutScene1 extends GameScene {

    static final int UPPER_Y  = TS * 12;
    static final int MIDDLE_Y = TS * 18;
    static final int LOWER_Y  = TS * 24;

    static final float SPEED_PAC_CHASING = 1.125f;
    static final float SPEED_GHOST_CHASING = 1.25f;
    static final float SPEED_RISING = 0.75f;
    static final float SPEED_GHOST_AFTER_COLLISION = 0.3f;

    private Pac pacMan;
    private Pac msPacMan;
    private Ghost inky;
    private Ghost pinky;
    private GameEntity heart;
    private Clapperboard clapperboard;

    private final ClapperboardStateSystem clapperboardSystem = new ClapperboardStateSystem();

    public ArcadeMsPacMan_CutScene1(GameAppContext app) {
        super(app);
        setComp(GameSceneCanvasRenderingComp.class, new GameSceneCanvasRenderingComp());
    }

    @Override
    public void onActivate() {
        initScene();
        setState(SceneState.CLAPPERBOARD, 120);
    }

    @Override
    public void onTick(GameContext game) {
        final GameSystems systems = game.variantPlayConfig().systems();

        switch (sceneState) {
            case CLAPPERBOARD -> updateStateClapperboard(systems);
            case CHASED_BY_GHOSTS -> updateStateChasedByGhosts(systems);
            case COMING_TOGETHER -> updateStateComingTogether(systems);
            case IN_HEAVEN -> updateStateInHeaven();
            default -> throw new IllegalStateException("Illegal scene state: " + sceneState);
        }
        sceneTimer.doTick();
    }

    @Override
    public Stream<Renderable> renderables() {
        return Ufx.streamOf(clapperboard, msPacMan, pacMan, inky, pinky, heart);
    }

    private void initScene() {
        final var actorFactory = new ArcadeMsPacMan_ActorFactory();
        final GameVariantConfig variant = app().variantManager().currentVariantConfig();
        final GameVariantRenderConfig renderConfig = variant.uiConfig().renderConfig();
        final SpriteAnimContainer animContainer = variant.spriteAnimContainer();
        final ActorSpriteAnimController animController = variant.playConfig().systems().actorSpriteAnimController();

        pacMan = actorFactory.createPacMan();
        pacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));

        msPacMan = actorFactory.createMsPacMan();
        msPacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));

        inky = renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.CYAN_GHOST_BASHFUL);

        pinky = renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.PINK_GHOST_SPEEDY);

        heart = new Heart();

        clapperboard = new Clapperboard("1", "THEY MEET");
        clapperboard.pos().set(tilesPx(3), tilesPx(10));

        clapperboardSystem.startFlapAnimation(clapperboard);
    }

    // Scene controller state machine

    private enum SceneState { CLAPPERBOARD, CHASED_BY_GHOSTS, COMING_TOGETHER, IN_HEAVEN }

    private SceneState sceneState;
    private final TickTimer sceneTimer = new TickTimer("Timer-MsPacMan_CutScene1");

    private void setState(SceneState state, long ticks) {
        sceneState = state;
        sceneTimer.reset(ticks);
        sceneTimer.start();
    }

    private void updateStateClapperboard(GameSystems systems) {
        clapperboardSystem.update(clapperboard);
        if (sceneTimer.atSecond(1)) {
            soundManager().play(PacManGameSoundID.INTERMISSION_1);
        } else if (sceneTimer.hasExpired()) {
            enterStateChasedByGhosts(systems);
        }
    }

    private void enterStateChasedByGhosts(GameSystems systems) {

        setState(SceneState.CHASED_BY_GHOSTS, TickTimer.INDEFINITE);

        final WorldNavigationSystem nav = systems.navigator();
        final ActorSpriteAnimController animController = systems.actorSpriteAnimController();

        // Pac-Man
        pacMan.pos().set(TS * (-2), UPPER_Y);
        pacMan.show();

        nav.setMoveDir(pacMan, Direction.RIGHT);
        nav.setMoveDirSpeed(pacMan, SPEED_PAC_CHASING);

        animController.select(pacMan, CommonSpriteAnimationID.MR_PAC_MAN_MUNCHING);
        animController.playSelected(pacMan);

        // Inky

        inky.pos().set(pacMan.pos().x() - 6 * TS, pacMan.pos().y());
        inky.show();

        nav.setMoveDirSpeed(inky, SPEED_GHOST_CHASING);
        nav.setMoveDir(inky, Direction.RIGHT);
        nav.setWishDir(inky, Direction.RIGHT);

        animController.select(inky, CommonSpriteAnimationID.GHOST_NORMAL);
        animController.playSelected(inky);

        // Ms. Pac-Man
        msPacMan.pos().set(TS * 30, LOWER_Y);
        msPacMan.show();

        nav.setMoveDir(msPacMan, Direction.LEFT);
        nav.setMoveDirSpeed(msPacMan, SPEED_PAC_CHASING);

        animController.select(msPacMan, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
        animController.playSelected(msPacMan);

        // Pinky

        pinky.pos().set(msPacMan.pos().x() + 6 * TS, msPacMan.pos().y());
        pinky.show();

        nav.setMoveDir(pinky, Direction.LEFT);
        nav.setWishDir(pinky, Direction.LEFT);
        nav.setMoveDirSpeed(pinky, SPEED_GHOST_CHASING);

        animController.select(pinky, CommonSpriteAnimationID.GHOST_NORMAL);
        animController.playSelected(pinky);
    }

    private void updateStateChasedByGhosts(GameSystems systems) {
        if (inky.pos().x() > TS * 30) {
            enterStateComingTogether(systems);
        }
        else {
            List.of(pacMan, msPacMan, inky, pinky).forEach(systems.motor()::move);
        }
    }

    private void enterStateComingTogether(GameSystems systems) {

        setState(SceneState.COMING_TOGETHER, TickTimer.INDEFINITE);

        final WorldNavigationSystem nav = systems.navigator();

        msPacMan.pos().set(TS * (-3), MIDDLE_Y);
        nav.setMoveDir(msPacMan, Direction.RIGHT);

        pinky.pos().set(msPacMan.pos().x() - 5 * TS, msPacMan.pos().y());
        nav.setMoveDir(pinky, Direction.RIGHT);
        nav.setWishDir(pinky, Direction.RIGHT);

        pacMan.pos().set(TS * 31, MIDDLE_Y);
        nav.setMoveDir(pacMan, Direction.LEFT);

        inky.pos().set(pacMan.pos().x() + 5 * TS, pacMan.pos().y());
        nav.setMoveDir(inky, Direction.LEFT);
        nav.setWishDir(inky, Direction.LEFT);

    }

    private void updateStateComingTogether(GameSystems systems) {
        final MovementSystem motor = systems.motor();
        final WorldNavigationSystem nav = systems.navigator();

        // Pac-Man and Ms. Pac-Man reach end position?
        if (pacMan.worldNavigation().moveDir() == Direction.UP && pacMan.pos().y() < UPPER_Y) {
            enterStateInHeaven(systems);
        }

        // Pac-Man and Ms. Pac-Man meet?
        else if (pacMan.worldNavigation().moveDir() == Direction.LEFT && pacMan.pos().x() - msPacMan.pos().x() < TS * 2) {
            nav.setMoveDir(pacMan, Direction.UP);
            nav.setMoveDirSpeed(pacMan, SPEED_RISING);
            nav.setMoveDir(msPacMan, Direction.UP);
            nav.setMoveDirSpeed(msPacMan, SPEED_RISING);
        }

        // Inky and Pinky collide?
        else if (inky.worldNavigation().moveDir() == Direction.LEFT && inky.pos().x() - pinky.pos().x() < TS * 2) {
            nav.setMoveDir(inky, Direction.RIGHT);
            nav.setWishDir(inky, Direction.RIGHT);
            nav.setMoveDirSpeed(inky, SPEED_GHOST_AFTER_COLLISION);

            motor.setVelocityY(inky, inky.movement().velocityY() - 2.0f);
            motor.setAcceleration(inky, 0, 0.4f);

            nav.setMoveDir(pinky, Direction.LEFT);
            nav.setWishDir(pinky, Direction.LEFT);
            nav.setMoveDirSpeed(pinky, SPEED_GHOST_AFTER_COLLISION);

            motor.setVelocityY(pinky, pinky.movement().velocityY() - 2.0f);
            motor.setAcceleration(pinky, 0, 0.4f);
        }

        else {
            List.of(pacMan, msPacMan, inky, pinky).forEach(systems.motor()::move);

            // Collision with ground?
            if (inky.pos().y() > MIDDLE_Y) {
                inky.pos().setY(MIDDLE_Y);
                inky.movement().setAcceleration(0, 0);
            }
            if (pinky.pos().y() > MIDDLE_Y) {
                pinky.pos().setY(MIDDLE_Y);
                pinky.movement().setAcceleration(0, 0);
            }
        }
    }

    private void enterStateInHeaven(GameSystems systems) {

        setState(SceneState.IN_HEAVEN, 3L * GameConstants.SIMULATION_FPS);

        final WorldNavigationSystem nav = systems.navigator();
        final ActorSpriteAnimController animController = systems.actorSpriteAnimController();

        nav.setMoveDirSpeed(pacMan, 0);
        nav.setMoveDir(pacMan, Direction.LEFT);

        animController.stopSelected(pacMan);
        animController.resetSelected(pacMan);

        nav.setMoveDirSpeed(msPacMan, 0);
        nav.setMoveDir(msPacMan, Direction.RIGHT);

        animController.stopSelected(msPacMan);
        animController.resetSelected(msPacMan);

        inky.hide();
        pinky.hide();

        heart.pos().set((pacMan.pos().x() + msPacMan.pos().x()) * 0.5f, pacMan.pos().y() - TS * 2);
        heart.show();
    }

    private void updateStateInHeaven() {
        if (sceneTimer.hasExpired()) {
            game().state().triggerTimeout();
        }
    }
}