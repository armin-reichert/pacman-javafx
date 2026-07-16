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
import de.amr.pacmanfx.core.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.GhostState;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.simulation.FrameContext;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.game.GameVariantConfig;
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

    public ArcadeMsPacMan_IntroScene(GameAppContext actionContext) {
        super(actionContext);
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
        appContext().ui().sounds().stopAndDisposeVoice();
        actionBindings().dispose();
    }

    @Override
    public void onTick(FrameContext frame) {
        sceneFlow.update(this);
    }

    private void initScene() {
        final GameVariantConfig gameVariantConfig = appContext().variants().currentVariant().config();
        final SpriteAnimationContainer spriteAnimations = appContext().ui().sprites().animations();

        marquee = new Marquee(60, 88, 132, 60, 96, 6, 16);
        marquee.setBulbOffColor(ARCADE_RED);
        marquee.setBulbOnColor(ARCADE_WHITE);
        marquee.timer().restartIndefinitely();

        msPacMan = ArcadeMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setPosition(WorldMap.TS * 31, WorldMap.TS * 20);
        msPacMan.setMoveDir(Direction.LEFT);
        msPacMan.setSpeed(ACTOR_SPEED);
        msPacMan.setVisible(true);
        msPacMan.setAnimations(gameVariantConfig.createPacAnimations(spriteAnimations));
        msPacMan.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
        msPacMan.animations().playSelected();

        ghosts = List.of(
            gameVariantConfig.createAnimatedGhost(spriteAnimations, GameModel.RED_GHOST_SHADOW),
            gameVariantConfig.createAnimatedGhost(spriteAnimations, GameModel.PINK_GHOST_SPEEDY),
            gameVariantConfig.createAnimatedGhost(spriteAnimations, GameModel.CYAN_GHOST_BASHFUL),
            gameVariantConfig.createAnimatedGhost(spriteAnimations, GameModel.ORANGE_GHOST_POKEY)
        );

        for (Ghost ghost : ghosts) {
            ghost.setPosition(WorldMap.TS * 33.5f, WorldMap.TS * 20);
            ghost.setMoveDir(Direction.LEFT);
            ghost.setWishDir(Direction.LEFT);
            ghost.setSpeed(ACTOR_SPEED);
            ghost.setState(GhostState.HUNTING_PAC);
            ghost.setVisible(true);
            ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
            ghost.animations().playSelected();
        }

        presentedGhostPersonality = GameModel.RED_GHOST_SHADOW;
        numTicksBeforeRising = 0;

        appContext().ui().sounds().playVoice(GlobalAssets.Voice.EXPLAIN_GAME_START.media());
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
                Ghost ghost = scene.ghosts.get(scene.presentedGhostPersonality);
                if (ghost.moveDir() == Direction.LEFT) {
                    if (ghost.x() <= STOP_X_GHOST) {
                        ghost.setX(STOP_X_GHOST);
                        ghost.setMoveDir(Direction.UP);
                        ghost.setWishDir(Direction.UP);
                        scene.numTicksBeforeRising = 2;
                    } else {
                        ghost.move();
                    }
                }
                else if (ghost.moveDir() == Direction.UP) {
                    int endPositionY = TOP_Y + scene.presentedGhostPersonality * 16 + 1;
                    if (scene.numTicksBeforeRising > 0) {
                        scene.numTicksBeforeRising--;
                    }
                    else if (ghost.y() <= endPositionY) {
                        ghost.setSpeed(0);
                        ghost.animations().stopSelected();
                        ghost.animations().resetSelected();
                        return true;
                    }
                    else {
                        ghost.move();
                    }
                }
                return false;
            }
        },

        MS_PACMAN_MARCHING_IN {
            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                scene.marquee.timer().doTick();
                scene.msPacMan.move();
                if (scene.msPacMan.x() <= STOP_X_MS_PACMAN) {
                    scene.msPacMan.setSpeed(0);
                    scene.msPacMan.animations().resetSelected();
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