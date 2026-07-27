/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateMachine;
import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.GhostState;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.systems.common.MovementSystem;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;

import java.util.List;

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_RED;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 */
public class ArcadeMsPacMan_IntroScene extends AbstractGameScene2D {

    public static final int TITLE_X          = WorldMap.TS * 10;
    public static final int TITLE_Y          = WorldMap.TS * 8;
    public static final int TOP_Y            = WorldMap.TS * 11;
    public static final int STOP_X_GHOST     = WorldMap.TS * 6 - WorldMap.HTS;
    public static final int STOP_X_MS_PACMAN = WorldMap.TS * 15 + 2;

    private static final float ACTOR_SPEED = 1.11f;

    private final StateMachine<ArcadeMsPacMan_IntroScene> sceneFlow;

    // Public for access by renderer
    public Marquee marquee;
    public Pac msPacMan;
    public List<Ghost> ghosts;
    public byte presentedGhostPersonality;

    private int numTicksBeforeRising;

    public ArcadeMsPacMan_IntroScene(GameAppContext appContext) {
        super(appContext);
        sceneFlow = new StateMachine<>(List.of(SceneState.values()));
    }

    @Override
    public void onActivate() {
        final Arcade_Actions actions = appContext().getExtensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        actionBindings().registerAllBindings(actions.gameStartActionBindings());
        actionBindings().registerAllBindings(appContext().commonActions().sceneTestActions().bindings());

        sceneFlow.restartState(this, SceneState.STARTING);
    }

    @Override
    public void onDeactivate() {
        appContext().ui().sounds().voice().stop();
        actionBindings().dispose();
    }

    @Override
    public void onTick(GameContext gameContext) {
        sceneFlow.update(this);
    }

    private void initScene() {
        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer container = appContext().ui().sprites().animations();
        final WorldMovementSystem worldMovementSystem = gameContext().systems().worldMovementSystem;

        marquee = new Marquee(60, 88, 132, 60, 96, 6, 16);
        marquee.setBulbOffColor(ARCADE_RED);
        marquee.setBulbOnColor(ARCADE_WHITE);
        marquee.timer().restartIndefinitely();

        msPacMan = ArcadeMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.position().set(WorldMap.TS * 31, WorldMap.TS * 20);
        worldMovementSystem.setMoveDir(msPacMan, Direction.LEFT);
        worldMovementSystem.setSpeed(msPacMan, ACTOR_SPEED);
        msPacMan.visibility().show();
        msPacMan.animations = renderConfig.createPacAnimations(container);
        msPacMan.animations.select(CommonAnimationID.PAC_MUNCHING);
        msPacMan.animations.playSelected();

        ghosts = List.of(
            renderConfig.createAnimatedGhost(gameContext(), container, GameModel.RED_GHOST_SHADOW),
            renderConfig.createAnimatedGhost(gameContext(), container, GameModel.PINK_GHOST_SPEEDY),
            renderConfig.createAnimatedGhost(gameContext(), container, GameModel.CYAN_GHOST_BASHFUL),
            renderConfig.createAnimatedGhost(gameContext(), container, GameModel.ORANGE_GHOST_POKEY)
        );

        for (Ghost ghost : ghosts) {
            ghost.position().set(WorldMap.TS * 33.5f, WorldMap.TS * 20);
            worldMovementSystem.setMoveDir(ghost, Direction.LEFT);
            worldMovementSystem.setWishDir(ghost, Direction.LEFT);
            worldMovementSystem.setSpeed(ghost, ACTOR_SPEED);
            ghost.setState(GhostState.HUNTING_PAC);
            ghost.visibility().show();
            ghost.animations.select(CommonAnimationID.GHOST_NORMAL);
            ghost.animations.playSelected();
        }

        presentedGhostPersonality = GameModel.RED_GHOST_SHADOW;
        numTicksBeforeRising = 0;

        appContext().ui().sounds().voice().playAfterSec(1, GlobalAssets.VoiceID.EXPLAIN_GAME_START.media());
    }

    // Scene flow state machine

    public State<ArcadeMsPacMan_IntroScene> sceneState() {
        return sceneFlow.state();
    }

    public enum SceneState implements State<ArcadeMsPacMan_IntroScene> {

        STARTING {
            @Override
            public void onEnter(ArcadeMsPacMan_IntroScene scene) {
                scene.initScene();
            }

            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                scene.marquee.timer().doTick();
                if (timer.atSecond(1)) {
                    scene.sceneFlow.enterState(scene, GHOSTS_MARCHING_IN);
                }
            }
        },

        GHOSTS_MARCHING_IN {
            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                scene.marquee.timer().doTick();
                boolean atEndPosition = letGhostWalkIn(scene);
                if (atEndPosition) {
                    if (scene.presentedGhostPersonality == GameModel.ORANGE_GHOST_POKEY) {
                        scene.sceneFlow.enterState(scene, MS_PACMAN_MARCHING_IN);
                    } else {
                        ++scene.presentedGhostPersonality;
                    }
                }
            }

            boolean letGhostWalkIn(ArcadeMsPacMan_IntroScene scene) {
                final MovementSystem movementSystem = scene.gameContext().systems().movementSystem;
                final WorldMovementSystem worldMovementSystem = scene.gameContext().systems().worldMovementSystem;

                final Ghost ghost = scene.ghosts.get(scene.presentedGhostPersonality);
                if (ghost.worldMovement().moveDir() == Direction.LEFT) {
                    if (ghost.position().x <= STOP_X_GHOST) {
                        ghost.position().setX(STOP_X_GHOST);
                        worldMovementSystem.setMoveDir(ghost, Direction.UP);
                        worldMovementSystem.setWishDir(ghost, Direction.UP);
                        scene.numTicksBeforeRising = 2;
                    } else {
                        movementSystem.moveAccelerated(ghost);
                    }
                }
                else if (ghost.worldMovement().moveDir() == Direction.UP) {
                    int endPositionY = TOP_Y + scene.presentedGhostPersonality * 16 + 1;
                    if (scene.numTicksBeforeRising > 0) {
                        scene.numTicksBeforeRising--;
                    }
                    else if (ghost.position().y <= endPositionY) {
                        worldMovementSystem.setSpeed(ghost, 0);
                        ghost.animations.stopSelected();
                        ghost.animations.resetSelected();
                        return true;
                    }
                    else {
                        movementSystem.moveAccelerated(ghost);
                    }
                }
                return false;
            }
        },

        MS_PACMAN_MARCHING_IN {
            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                final MovementSystem movementSystem = scene.gameContext().systems().movementSystem;
                final WorldMovementSystem worldMovementSystem = scene.gameContext().systems().worldMovementSystem;

                scene.marquee.timer().doTick();
                movementSystem.moveAccelerated(scene.msPacMan);
                if (scene.msPacMan.position().x <= STOP_X_MS_PACMAN) {
                    worldMovementSystem.setSpeed(scene.msPacMan, 0);
                    scene.msPacMan.animations.resetSelected();
                    scene.sceneFlow.enterState(scene, READY_TO_PLAY);
                }
            }
        },

        READY_TO_PLAY {
            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                final GameContext gameContext = scene.appContext().currentGameContext();
                final boolean canPlay = !gameContext.coinMechanism().isEmpty();
                scene.marquee.timer().doTick();
                if (timer.atSecond(2.0) && !canPlay) {
                    scene.gameFlow().enterState(gameContext, GameStateID.GAME_OR_LEVEL_STARTING); // play demo level after 2 seconds
                }
                //TODO can this happen at all?
                else if (timer.atSecond(5)) {
                    scene.gameFlow().enterState(gameContext, GameStateID.GAME_PREPARATION);
                }
            }
        };

        final TickTimer timer = new TickTimer("Timer-" + name());

        @Override
        public TickTimer timer() {
            return timer;
        }
    }
}