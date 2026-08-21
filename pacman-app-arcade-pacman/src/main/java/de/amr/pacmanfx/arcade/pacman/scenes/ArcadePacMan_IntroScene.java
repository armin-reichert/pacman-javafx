/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateMachine;
import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimContainer;
import de.amr.basics.timer.Pulse;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.ecs.systems.SpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.ghost.system.GhostAnimationSelectionSystem;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.rules.CollisionStrategy;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.core.entities.ghost.comp.GhostState.EATEN;
import static de.amr.pacmanfx.core.entities.ghost.comp.GhostState.FRIGHTENED;

/**
 * The ghosts are presented one by one, then Pac-Man is chased by the ghosts, turns the cards and hunts the ghosts himself.
 */
public class ArcadePacMan_IntroScene extends GameScene {

    public static final int NUM_GHOSTS = 4;

    // State STARTING
    public static final int TICK_TITLE_VISIBLE           = 3;
    public static final int TICK_START_PRESENTING_GHOSTS = 60;

    // State PRESENTING_GHOSTS
    public static final int TICK_GHOST_SPRITE_VISIBLE    =   0;
    public static final int TICK_GHOST_CHARACTER_VISIBLE =  60;
    public static final int TICK_GHOST_NICKNAME_VISIBLE  =  90;
    public static final int TICK_GHOST_PRESENT_NEXT      = 120;
    public static final int TICK_GHOST_PRESENTATION_END  = 150;

    // State SHOWING_POINTS
    public static final int TICK_SHOW_POINTS_DURATION = 60;

    // State CHASING_PAC_MAN
    public static final float CHASING_SPEED = 1.1f;
    public static final float GHOST_FRIGHTENED_SPEED = 0.5f;

    public static final int TICK_PAC_MAN_APPEARS = 60;
    public static final int TICK_PAC_MAN_REACHES_ENERGIZER = 230;
    public static final int TICK_PAC_MAN_MOVES_AGAIN = TICK_PAC_MAN_REACHES_ENERGIZER + 4;
    public static final int TICK_CHASING_PAC_MAN_END = TICK_PAC_MAN_REACHES_ENERGIZER + 8;

    // State CHASING_GHOSTS
    public static final int GHOST_EATING_TICKS = 50;

    public static final int TICK_CHASING_GHOSTS_END = 270;

    // READY_TO_PLAY
    public static final int TICK_START_DEMO_LEVEL = 60;

    // public access for renderer
    public final StateMachine<ArcadePacMan_IntroScene> flow;
    public boolean titleVisible;
    public Pulse blinking;
    public Pac pacMan;
    public final Ghost[] ghosts = new Ghost[NUM_GHOSTS];
    public final boolean[] ghostImageVisible = new boolean[NUM_GHOSTS];
    public final boolean[] ghostNicknameVisible = new boolean[NUM_GHOSTS];
    public final boolean[] ghostCharacterVisible = new boolean[NUM_GHOSTS];

    private int numGhostsEaten;
    private int ghostIndex;
    private long lastGhostEatenTick;

    public ArcadePacMan_IntroScene(GameAppContext appContext) {
        super(appContext);
        flow = new StateMachine<>(List.of(SceneState.values()));
    }

    @Override
    public void onActivate() {
        final Arcade_Actions actions = app().currentGameVariantUIConfig()
            .getExtensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        final var bindingsMap = actionBindingsSupport().bindingsMap();
        bindingsMap.registerAllBindings(actions.gameStartActionBindings()); // insert coin + start game actions
        bindingsMap.registerAllBindings(app().commonActions().sceneTestActions().bindings()); // actions for starting tests

        flow.restartState(this, SceneState.STARTING);
    }

    @Override
    public void onDeactivate() {
        blinking.stop();
        app().ui().sounds().voice().stop();
    }

    @Override
    public void onTick(GameContext game) {
        flow.update(this);
    }

    private void initScene() {
        final GameVariantRenderConfig renderConfig = app().currentGameVariantUIConfig().renderConfig();
        final SpriteAnimContainer animationContainer = app().ui().spriteAnimManager().animContainer();
        final SpriteAnimController animController = app().game().variant().systems().spriteAnimController();

        blinking = new Pulse(10, Pulse.State.ON);

        final var factory = ArcadePacMan_ActorFactory.instance();

        pacMan = factory.createPacMan();
        pacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animationContainer));
        pacMan.spriteAnim().spriteAnimations().select(CommonSpriteAnimationID.PAC_MUNCHING);
        pacMan.spriteAnim().spriteAnimations().playSelected();

        ghosts[0] = renderConfig.createAnimatedGhost(animController, animationContainer, GhostPersonality.RED_GHOST_SHADOW);
        ghosts[1] = renderConfig.createAnimatedGhost(animController, animationContainer, GhostPersonality.PINK_GHOST_SPEEDY);
        ghosts[2] = renderConfig.createAnimatedGhost(animController, animationContainer, GhostPersonality.CYAN_GHOST_BASHFUL);
        ghosts[3] = renderConfig.createAnimatedGhost(animController, animationContainer, GhostPersonality.ORANGE_GHOST_POKEY);

        Arrays.fill(ghostImageVisible, false);
        Arrays.fill(ghostNicknameVisible, false);
        Arrays.fill(ghostCharacterVisible, false);

        titleVisible = false;
        ghostIndex = 0;
        lastGhostEatenTick = 0;
        numGhostsEaten = 0;

        app().ui().sounds().voice().playAfterSec(1, GlobalAssets.VoiceID.EXPLAIN_GAME_START.media());
    }

    private void startChasingPacMan(GameContext game) {
        final GameSystems systems = game.variant().systems();

        blinking.start();

        pacMan.pos().set(WorldMap.TS * 28, WorldMap.TS * 20);
        pacMan.show();

        systems.worldNavigator().setMoveDir(pacMan, Direction.LEFT);
        systems.worldNavigator().setMoveDirSpeed(pacMan, CHASING_SPEED);

        for (Ghost ghost : ghosts) {
            ghost.pos().set(pacMan.pos().x() + 16 * ghost.personality().ordinal() + 18, pacMan.pos().y());
            ghost.show();

            systems.worldNavigator().setMoveDir(ghost, Direction.LEFT);
            systems.worldNavigator().setWishDir(ghost, Direction.LEFT);
            systems.worldNavigator().setMoveDirSpeed(ghost, CHASING_SPEED);

            systems.ghostState().changeGhostState(ghost, GhostState.HUNTING_PAC);
        }
    }

    private void chasePacMan(long tick) {
        final GameSystems systems = game().variant().systems();
        final MovementSystem motor = systems.motor();
        final GhostAnimationSelectionSystem ghostSpriteAnimationSystem = systems.ghostSpriteAnimation();

        blinking.triggerPulse();
        motor.move(pacMan);
        for (Ghost ghost : ghosts) {
            motor.move(ghost);
        }

        // "shaking" effect
        final long tick_0_to_5 = tick % 6;
        final Ghost pinkGhost = ghosts[GhostPersonality.PINK_GHOST_SPEEDY.ordinal()];
        final Ghost cyanGhost = ghosts[GhostPersonality.CYAN_GHOST_BASHFUL.ordinal()];
        if (tick_0_to_5 == 2) {
            pinkGhost.pos().setX(pinkGhost.pos().x() + 0.5);
            cyanGhost.pos().setX(cyanGhost.pos().x() - 0.5);
        }
        else if (tick_0_to_5 == 5) {
            pinkGhost.pos().setX(pinkGhost.pos().x() - 0.5);
            cyanGhost.pos().setX(cyanGhost.pos().x() + 0.5);
        }

        for (Ghost ghost : ghosts) {
            ghostSpriteAnimationSystem.update(ghost, pacMan, systems.spriteAnimController());
        }
    }

    private void turnCardsStopPacMan(GameContext game) {
        final GameSystems systems = game.variant().systems();

        systems.worldNavigator().setMoveDirSpeed(pacMan, 0);
        systems.spriteAnimController().stopSelected(pacMan);

        for (Ghost ghost : ghosts) {
            systems.worldNavigator().setMoveDir(ghost, Direction.RIGHT);
            systems.worldNavigator().setWishDir(ghost, Direction.RIGHT);
            systems.worldNavigator().setMoveDirSpeed(ghost, GHOST_FRIGHTENED_SPEED);

            systems.ghostState().changeGhostState(ghost, FRIGHTENED);
            ghost.spriteAnimation().spriteAnimations().select(CommonSpriteAnimationID.GHOST_FRIGHTENED);
            ghost.spriteAnimation().spriteAnimations().playSelected();
        }
    }

    private void turnCardsRestartPacMan(GameSystems sys) {
        sys.worldNavigator().setMoveDirSpeed(pacMan, CHASING_SPEED);
        pacMan.spriteAnim().spriteAnimations().playSelected();
    }

    private void chaseGhosts(GameContext game, long tick) {
        final GameSystems systems = game.variant().systems();

        blinking.triggerPulse();
        systems.motor().move(pacMan);
        for (Ghost ghost : ghosts) { systems.motor().move(ghost); }
        edibleGhost().ifPresent(victim -> eatGhostAndStopChasing(game, victim, tick));
        if (tick == lastGhostEatenTick + GHOST_EATING_TICKS) {
            continueChasing(systems);
        }
    }

    private Optional<Ghost> edibleGhost() {
        return Stream.of(ghosts)
            .filter(ghost -> ghost.state().enumValue() == FRIGHTENED)
            .filter(ghost -> CollisionStrategy.SAME_TILE.collide(ghost, pacMan))
            .findFirst();
    }

    private void eatGhostAndStopChasing(GameContext game, Ghost victim, long tick) {
        final GameSystems systems = game.variant().systems();

        systems.ghostState().changeGhostState(victim, EATEN);
        systems.spriteAnimController().selectAndSetFrame(victim, CommonSpriteAnimationID.GHOST_POINTS, numGhostsEaten++);

        pacMan.hide();
        systems.worldNavigator().setMoveDirSpeed(pacMan, 0);

        for (Ghost ghost : ghosts) {
            systems.worldNavigator().setMoveDirSpeed(ghost, 0);
            systems.spriteAnimController().stopSelected(ghost);
        }

        lastGhostEatenTick = tick;
    }

    private void continueChasing(GameSystems sys) {
        pacMan.show();
        sys.worldNavigator().setMoveDirSpeed(pacMan, CHASING_SPEED);

        for (Ghost ghost : ghosts) {
            if (ghost.state().enumValue() == EATEN) {
                ghost.hide();
            } else {
                ghost.show();
                sys.worldNavigator().setMoveDirSpeed(ghost, GHOST_FRIGHTENED_SPEED);
                ghost.spriteAnimation().spriteAnimations().playSelected();
            }
        }
    }

    // Scene flow state machine

    public enum SceneState implements State<ArcadePacMan_IntroScene> {

        STARTING {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                scene.initScene();
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (timer.tickCount() == TICK_TITLE_VISIBLE) {
                    scene.titleVisible = true;
                } else if (timer.tickCount() == TICK_START_PRESENTING_GHOSTS) {
                    scene.flow.enterState(scene, PRESENTING_GHOSTS);
                }
            }
        },

        PRESENTING_GHOSTS {
            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (timer.tickCount() > TICK_GHOST_PRESENTATION_END) {
                    return;
                }
                switch ((int) timer.tickCount()) {
                    case TICK_GHOST_SPRITE_VISIBLE    -> scene.ghostImageVisible[scene.ghostIndex] = true;
                    case TICK_GHOST_CHARACTER_VISIBLE -> scene.ghostCharacterVisible[scene.ghostIndex] = true;
                    case TICK_GHOST_NICKNAME_VISIBLE  -> scene.ghostNicknameVisible[scene.ghostIndex] = true;
                    case TICK_GHOST_PRESENT_NEXT      -> presentNextGhost(scene);
                    case TICK_GHOST_PRESENTATION_END  -> scene.flow.enterState(scene, SHOWING_POINTS);
                }
            }

            private void presentNextGhost(ArcadePacMan_IntroScene scene) {
                if (scene.ghostIndex < NUM_GHOSTS - 1) {
                    scene.ghostIndex += 1;
                    timer.resetToIndefiniteDuration();
                }
            }
        },

        SHOWING_POINTS {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                scene.blinking.stop();
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (timer.tickCount() == TICK_SHOW_POINTS_DURATION) {
                    scene.flow.enterState(scene, CHASING_PAC_MAN);
                }
            }
        },

        CHASING_PAC_MAN {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                timer.restartTicks(TICK_CHASING_PAC_MAN_END);
                scene.pacMan.hide();
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                final GameSystems systems = scene.game().variant().systems();

                final long tick = timer.tickCount();
                if (tick == TICK_PAC_MAN_APPEARS) {
                    scene.startChasingPacMan(scene.game());
                }
                else if (tick == TICK_PAC_MAN_REACHES_ENERGIZER) {
                    scene.turnCardsStopPacMan(scene.game());
                }
                else if (tick == TICK_PAC_MAN_MOVES_AGAIN) {
                    scene.turnCardsRestartPacMan(systems);
                }
                else if (tick == TICK_CHASING_PAC_MAN_END) {
                    scene.flow.enterState(scene, CHASING_GHOSTS);
                    return;
                }
                scene.chasePacMan(tick);
            }
        },

        CHASING_GHOSTS {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                final GameSystems systems = scene.game().variant().systems();

                timer.restartTicks(TICK_CHASING_GHOSTS_END);

                scene.lastGhostEatenTick = timer.tickCount();
                scene.numGhostsEaten = 0;

                systems.worldNavigator().setMoveDir(scene.pacMan, Direction.RIGHT);
                systems.worldNavigator().setMoveDirSpeed(scene.pacMan, CHASING_SPEED);
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                final long tick = timer.tickCount();
                if (tick == TICK_CHASING_GHOSTS_END) {
                    scene.pacMan.hide();
                    scene.flow.enterState(scene, WAIT_FOR_DEMO_LEVEL);
                } else {
                    scene.chaseGhosts(scene.game(), tick);
                }
            }
        },

        WAIT_FOR_DEMO_LEVEL {
            @Override
            public void onEnter(ArcadePacMan_IntroScene context) {
                timer.restartTicks(TICK_START_DEMO_LEVEL);
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                final GameContext game = scene.game();

                if (timer.tickCount() == TICK_START_DEMO_LEVEL) {
                    scene.ghosts[GhostPersonality.ORANGE_GHOST_POKEY.ordinal()].hide();
                    scene.gameFlow().enterGameState(game, CommonGameStateID.GAME_OR_LEVEL_STARTING);
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