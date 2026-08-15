/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.arcade.ms_pacman.entities.Heart;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.entities.Clapperboard;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.clapperboard.system.ClapperboardStateSystem;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import java.util.List;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 */
public class ArcadeMsPacMan_CutScene1 extends AbstractGameScene2D {

    static final int UPPER_Y  = WorldMap.TS * 12;
    static final int MIDDLE_Y = WorldMap.TS * 18;
    static final int LOWER_Y  = WorldMap.TS * 24;

    static final float SPEED_PAC_CHASING = 1.125f;
    static final float SPEED_GHOST_CHASING = 1.25f;
    static final float SPEED_RISING = 0.75f;
    static final float SPEED_GHOST_AFTER_COLLISION = 0.3f;

    // Public for access by renderer
    public Pac pacMan;
    public Pac msPacMan;
    public Ghost inky;
    public Ghost pinky;
    public GameEntity heart;
    public Clapperboard clapperboard;

    public ArcadeMsPacMan_CutScene1(GameAppContext appContext) {
        super(appContext);
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

    private void initScene() {
        final GameVariantRenderConfig renderConfig = app().gameVariants().currentGameVariant().uiConfig().renderConfig();
        final SpriteAnimationContainer spriteAnimations = app().ui().sprites().animations();
        final var factory = new ArcadeMsPacMan_ActorFactory();

        pacMan = factory.createPacMan();
        pacMan.spriteAnim().setAnimations(renderConfig.createPacAnimations(spriteAnimations));

        msPacMan = factory.createMsPacMan();
        msPacMan.spriteAnim().setAnimations(renderConfig.createPacAnimations(spriteAnimations));

        inky = renderConfig.createAnimatedGhost(game(), spriteAnimations, GhostPersonality.CYAN_GHOST_BASHFUL);

        pinky = renderConfig.createAnimatedGhost(game(), spriteAnimations, GhostPersonality.PINK_GHOST_SPEEDY);

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

    private void updateStateClapperboard(GameSystems sys) {
        ClapperboardStateSystem.update(clapperboard);
        if (sceneTimer.atSecond(1)) {
            app().ui().sounds().play(PacManGameSoundID.INTERMISSION_1);
        } else if (sceneTimer.hasExpired()) {
            enterStateChasedByGhosts(sys);
        }
    }

    private void enterStateChasedByGhosts(GameSystems sys) {
        pacMan.pos().set(WorldMap.TS * (-2), UPPER_Y);
        pacMan.show();

        sys.worldNavigator().setMoveDir(pacMan, Direction.RIGHT);
        sys.worldNavigator().setSpeed(pacMan, SPEED_PAC_CHASING);

        sys.spriteAnim().select(pacMan, CommonSpriteAnimationID.MR_PAC_MAN_MUNCHING);
        sys.spriteAnim().playSelected(pacMan);

        inky.pos().set(pacMan.pos().x() - 6 * WorldMap.TS, pacMan.pos().y());
        inky.show();

        sys.worldNavigator().setSpeed(inky, SPEED_GHOST_CHASING);
        sys.worldNavigator().setMoveDir(inky, Direction.RIGHT);
        sys.worldNavigator().setWishDir(inky, Direction.RIGHT);

        sys.spriteAnim().select(inky, CommonSpriteAnimationID.GHOST_NORMAL);
        sys.spriteAnim().playSelected(inky);

        msPacMan.pos().set(WorldMap.TS * 30, LOWER_Y);
        msPacMan.show();

        sys.worldNavigator().setMoveDir(msPacMan, Direction.LEFT);
        sys.worldNavigator().setSpeed(msPacMan, SPEED_PAC_CHASING);

        sys.spriteAnim().select(msPacMan, CommonSpriteAnimationID.PAC_MUNCHING);
        sys.spriteAnim().playSelected(msPacMan);

        pinky.pos().set(msPacMan.pos().x() + 6 * WorldMap.TS, msPacMan.pos().y());
        pinky.show();

        sys.worldNavigator().setMoveDir(pinky, Direction.LEFT);
        sys.worldNavigator().setWishDir(pinky, Direction.LEFT);
        sys.worldNavigator().setSpeed(pinky, SPEED_GHOST_CHASING);

        sys.spriteAnim().select(pinky, CommonSpriteAnimationID.GHOST_NORMAL);
        sys.spriteAnim().playSelected(pinky);

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

    private void enterStateComingTogether(GameSystems sys) {
        msPacMan.pos().set(WorldMap.TS * (-3), MIDDLE_Y);
        sys.worldNavigator().setMoveDir(msPacMan, Direction.RIGHT);

        pinky.pos().set(msPacMan.pos().x() - 5 * WorldMap.TS, msPacMan.pos().y());
        sys.worldNavigator().setMoveDir(pinky, Direction.RIGHT);
        sys.worldNavigator().setWishDir(pinky, Direction.RIGHT);

        pacMan.pos().set(WorldMap.TS * 31, MIDDLE_Y);
        sys.worldNavigator().setMoveDir(pacMan, Direction.LEFT);

        inky.pos().set(pacMan.pos().x() + 5 * WorldMap.TS, pacMan.pos().y());
        sys.worldNavigator().setMoveDir(inky, Direction.LEFT);
        sys.worldNavigator().setWishDir(inky, Direction.LEFT);

        setState(SceneState.COMING_TOGETHER, TickTimer.INDEFINITE);
    }

    private void updateStateComingTogether(GameSystems sys) {
        // Pac-Man and Ms. Pac-Man reach end position?
        if (pacMan.worldNavigation().moveDir() == Direction.UP && pacMan.pos().y() < UPPER_Y) {
            enterStateInHeaven(sys);
        }

        // Pac-Man and Ms. Pac-Man meet?
        else if (pacMan.worldNavigation().moveDir() == Direction.LEFT && pacMan.pos().x() - msPacMan.pos().x() < WorldMap.TS * 2) {
            sys.worldNavigator().setMoveDir(pacMan, Direction.UP);
            sys.worldNavigator().setSpeed(pacMan, SPEED_RISING);
            sys.worldNavigator().setMoveDir(msPacMan, Direction.UP);
            sys.worldNavigator().setSpeed(msPacMan, SPEED_RISING);
        }

        // Inky and Pinky collide?
        else if (inky.worldNavigation().moveDir() == Direction.LEFT && inky.pos().x() - pinky.pos().x() < WorldMap.TS * 2) {
            sys.worldNavigator().setMoveDir(inky, Direction.RIGHT);
            sys.worldNavigator().setWishDir(inky, Direction.RIGHT);
            sys.worldNavigator().setSpeed(inky, SPEED_GHOST_AFTER_COLLISION);

            sys.motor().setVelocityY(inky, inky.movement().velocityY() - 2.0f);
            sys.motor().setAcceleration(inky, 0, 0.4f);

            sys.worldNavigator().setMoveDir(pinky, Direction.LEFT);
            sys.worldNavigator().setWishDir(pinky, Direction.LEFT);
            sys.worldNavigator().setSpeed(pinky, SPEED_GHOST_AFTER_COLLISION);

            sys.motor().setVelocityY(pinky, pinky.movement().velocityY() - 2.0f);
            sys.motor().setAcceleration(pinky, 0, 0.4f);
        }

        else {
            List.of(pacMan, msPacMan, inky, pinky).forEach(sys.motor()::move);

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

    private void enterStateInHeaven(GameSystems sys) {
        sys.worldNavigator().setSpeed(pacMan, 0);
        sys.worldNavigator().setMoveDir(pacMan, Direction.LEFT);

        sys.spriteAnim().stopSelected(pacMan);
        sys.spriteAnim().resetSelected(pacMan);

        sys.worldNavigator().setSpeed(msPacMan, 0);
        sys.worldNavigator().setMoveDir(msPacMan, Direction.RIGHT);

        sys.spriteAnim().stopSelected(msPacMan);
        sys.spriteAnim().resetSelected(msPacMan);

        inky.hide();
        pinky.hide();

        heart.pos().set((pacMan.pos().x() + msPacMan.pos().x()) * 0.5f, pacMan.pos().y() - WorldMap.TS * 2);
        heart.show();

        setState(SceneState.IN_HEAVEN, 3L * GameConstants.SIMULATION_FPS);
    }

    private void updateStateInHeaven() {
        if (sceneTimer.hasExpired()) {
            gameState().triggerTimeout();
        }
    }
}