/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.arcade.ms_pacman.entities.Heart;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.Clapperboard;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.clapperboard.system.ClapperboardStateSystem;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.game.GameVariant;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 */
public class ArcadeMsPacMan_CutScene1 extends GameScene {

    static final int UPPER_Y  = WorldMap.TS * 12;
    static final int MIDDLE_Y = WorldMap.TS * 18;
    static final int LOWER_Y  = WorldMap.TS * 24;

    static final float SPEED_PAC_CHASING = 1.125f;
    static final float SPEED_GHOST_CHASING = 1.25f;
    static final float SPEED_RISING = 0.75f;
    static final float SPEED_GHOST_AFTER_COLLISION = 0.3f;

    // Public for access by renderer
    private Pac pacMan;
    private Pac msPacMan;
    private Ghost inky;
    private Ghost pinky;
    private GameEntity heart;
    private Clapperboard clapperboard;

    public ArcadeMsPacMan_CutScene1(GameAppContext app) {
        super(app);
        components().setComp(CanvasRenderingComp.class, new CanvasRenderingComp());
    }

    @Override
    public void onActivate() {
        initScene();
        setState(SceneState.CLAPPERBOARD, 120);
    }

    @Override
    public void onTick(GameContext game) {
        final GameSystems sys = game.variant().systems();

        switch (sceneState) {
            case CLAPPERBOARD -> updateStateClapperboard(sys);
            case CHASED_BY_GHOSTS -> updateStateChasedByGhosts(sys);
            case COMING_TOGETHER -> updateStateComingTogether(sys);
            case IN_HEAVEN -> updateStateInHeaven();
            default -> throw new IllegalStateException("Illegal scene state: " + sceneState);
        }
        sceneTimer.doTick();
    }

    public Stream<GameEntity> entitiesInRenderOrder() {
        return Stream.of(clapperboard, msPacMan, pacMan, inky, pinky, heart);
    }

    private void initScene() {
        final var actorFactory = new ArcadeMsPacMan_ActorFactory();
        final GameVariant variant = app().gameVariants().currentGameVariant();
        final GameVariantRenderConfig renderConfig = variant.uiConfig().renderConfig();
        final SpriteAnimContainer animContainer    = variant.spriteAnimContainer();
        final ActorSpriteAnimController animController  = variant.config().systems().actorSpriteAnimController();

        pacMan = actorFactory.createPacMan();
        pacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));

        msPacMan = actorFactory.createMsPacMan();
        msPacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));

        inky = renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.CYAN_GHOST_BASHFUL);

        pinky = renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.PINK_GHOST_SPEEDY);

        heart = new Heart();

        clapperboard = new Clapperboard("1", "THEY MEET");
        clapperboard.pos().set(tilesPx(3), tilesPx(10));

        ClapperboardStateSystem.startFlapAnimation(clapperboard);
    }

    // Scene controller state machine

    private enum SceneState {CLAPPERBOARD, CHASED_BY_GHOSTS, COMING_TOGETHER, IN_HEAVEN}

    private SceneState sceneState;
    private final TickTimer sceneTimer = new TickTimer("Timer-MsPacMan_CutScene1");

    private void setState(SceneState state, long ticks) {
        sceneState = state;
        sceneTimer.reset(ticks);
        sceneTimer.start();
    }

    private void updateStateClapperboard(GameSystems systems) {
        ClapperboardStateSystem.update(clapperboard);
        if (sceneTimer.atSecond(1)) {
            soundManager().play(PacManGameSoundID.INTERMISSION_1);
        } else if (sceneTimer.hasExpired()) {
            enterStateChasedByGhosts(systems);
        }
    }

    private void enterStateChasedByGhosts(GameSystems systems) {
        pacMan.pos().set(WorldMap.TS * (-2), UPPER_Y);
        pacMan.show();

        systems.navigator().setMoveDir(pacMan, Direction.RIGHT);
        systems.navigator().setMoveDirSpeed(pacMan, SPEED_PAC_CHASING);

        systems.actorSpriteAnimController().select(pacMan, CommonSpriteAnimationID.MR_PAC_MAN_MUNCHING);
        systems.actorSpriteAnimController().playSelected(pacMan);

        inky.pos().set(pacMan.pos().x() - 6 * WorldMap.TS, pacMan.pos().y());
        inky.show();

        systems.navigator().setMoveDirSpeed(inky, SPEED_GHOST_CHASING);
        systems.navigator().setMoveDir(inky, Direction.RIGHT);
        systems.navigator().setWishDir(inky, Direction.RIGHT);

        systems.actorSpriteAnimController().select(inky, CommonSpriteAnimationID.GHOST_NORMAL);
        systems.actorSpriteAnimController().playSelected(inky);

        msPacMan.pos().set(WorldMap.TS * 30, LOWER_Y);
        msPacMan.show();

        systems.navigator().setMoveDir(msPacMan, Direction.LEFT);
        systems.navigator().setMoveDirSpeed(msPacMan, SPEED_PAC_CHASING);

        systems.actorSpriteAnimController().select(msPacMan, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
        systems.actorSpriteAnimController().playSelected(msPacMan);

        pinky.pos().set(msPacMan.pos().x() + 6 * WorldMap.TS, msPacMan.pos().y());
        pinky.show();

        systems.navigator().setMoveDir(pinky, Direction.LEFT);
        systems.navigator().setWishDir(pinky, Direction.LEFT);
        systems.navigator().setMoveDirSpeed(pinky, SPEED_GHOST_CHASING);

        systems.actorSpriteAnimController().select(pinky, CommonSpriteAnimationID.GHOST_NORMAL);
        systems.actorSpriteAnimController().playSelected(pinky);

        setState(SceneState.CHASED_BY_GHOSTS, TickTimer.INDEFINITE);
    }

    private void updateStateChasedByGhosts(GameSystems sys) {
        if (inky.pos().x() > WorldMap.TS * 30) {
            enterStateComingTogether(sys);
        }
        else {
            List.of(pacMan, msPacMan, inky, pinky).forEach(sys.motor()::move);
        }
    }

    private void enterStateComingTogether(GameSystems systems) {
        msPacMan.pos().set(WorldMap.TS * (-3), MIDDLE_Y);
        systems.navigator().setMoveDir(msPacMan, Direction.RIGHT);

        pinky.pos().set(msPacMan.pos().x() - 5 * WorldMap.TS, msPacMan.pos().y());
        systems.navigator().setMoveDir(pinky, Direction.RIGHT);
        systems.navigator().setWishDir(pinky, Direction.RIGHT);

        pacMan.pos().set(WorldMap.TS * 31, MIDDLE_Y);
        systems.navigator().setMoveDir(pacMan, Direction.LEFT);

        inky.pos().set(pacMan.pos().x() + 5 * WorldMap.TS, pacMan.pos().y());
        systems.navigator().setMoveDir(inky, Direction.LEFT);
        systems.navigator().setWishDir(inky, Direction.LEFT);

        setState(SceneState.COMING_TOGETHER, TickTimer.INDEFINITE);
    }

    private void updateStateComingTogether(GameSystems systems) {
        // Pac-Man and Ms. Pac-Man reach end position?
        if (pacMan.worldNavigation().moveDir() == Direction.UP && pacMan.pos().y() < UPPER_Y) {
            enterStateInHeaven(systems);
        }

        // Pac-Man and Ms. Pac-Man meet?
        else if (pacMan.worldNavigation().moveDir() == Direction.LEFT && pacMan.pos().x() - msPacMan.pos().x() < WorldMap.TS * 2) {
            systems.navigator().setMoveDir(pacMan, Direction.UP);
            systems.navigator().setMoveDirSpeed(pacMan, SPEED_RISING);
            systems.navigator().setMoveDir(msPacMan, Direction.UP);
            systems.navigator().setMoveDirSpeed(msPacMan, SPEED_RISING);
        }

        // Inky and Pinky collide?
        else if (inky.worldNavigation().moveDir() == Direction.LEFT && inky.pos().x() - pinky.pos().x() < WorldMap.TS * 2) {
            systems.navigator().setMoveDir(inky, Direction.RIGHT);
            systems.navigator().setWishDir(inky, Direction.RIGHT);
            systems.navigator().setMoveDirSpeed(inky, SPEED_GHOST_AFTER_COLLISION);

            systems.motor().setVelocityY(inky, inky.movement().velocityY() - 2.0f);
            systems.motor().setAcceleration(inky, 0, 0.4f);

            systems.navigator().setMoveDir(pinky, Direction.LEFT);
            systems.navigator().setWishDir(pinky, Direction.LEFT);
            systems.navigator().setMoveDirSpeed(pinky, SPEED_GHOST_AFTER_COLLISION);

            systems.motor().setVelocityY(pinky, pinky.movement().velocityY() - 2.0f);
            systems.motor().setAcceleration(pinky, 0, 0.4f);
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
        systems.navigator().setMoveDirSpeed(pacMan, 0);
        systems.navigator().setMoveDir(pacMan, Direction.LEFT);

        systems.actorSpriteAnimController().stopSelected(pacMan);
        systems.actorSpriteAnimController().resetSelected(pacMan);

        systems.navigator().setMoveDirSpeed(msPacMan, 0);
        systems.navigator().setMoveDir(msPacMan, Direction.RIGHT);

        systems.actorSpriteAnimController().stopSelected(msPacMan);
        systems.actorSpriteAnimController().resetSelected(msPacMan);

        inky.hide();
        pinky.hide();

        heart.pos().set((pacMan.pos().x() + msPacMan.pos().x()) * 0.5f, pacMan.pos().y() - WorldMap.TS * 2);
        heart.show();

        setState(SceneState.IN_HEAVEN, 3L * GameConstants.SIMULATION_FPS);
    }

    private void updateStateInHeaven() {
        if (sceneTimer.hasExpired()) {
            game().state().triggerTimeout();
        }
    }
}