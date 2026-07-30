/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateMachine;
import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.basics.timer.Pulse;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.GhostState;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnim;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
import de.amr.pacmanfx.core.model.systems.common.MovementSystem;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.rules.CollisionStrategy;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.core.model.actors.GhostState.EATEN;
import static de.amr.pacmanfx.core.model.actors.GhostState.FRIGHTENED;

/**
 * The ghosts are presented one by one, then Pac-Man is chased by the ghosts, turns the cards and hunts the ghosts himself.
 */
public class ArcadePacMan_IntroScene extends AbstractGameScene2D {

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
        final Arcade_Actions actions = appContext().getExtensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        actionBindings().registerAllBindings(actions.gameStartActionBindings()); // insert coin + start game actions
        actionBindings().registerAllBindings(appContext().commonActions().sceneTestActions().bindings()); // actions for starting tests

        flow.restartState(this, SceneState.STARTING);
    }

    @Override
    public void onDeactivate() {
        blinking.stop();
        appContext().ui().sounds().voice().stop();
        actionBindings().dispose();
    }

    @Override
    public void onTick(GameContext gameContext) {
        flow.update(this);
    }

    private void initScene() {
        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer spriteAnimations = appContext().ui().sprites().animations();

        blinking = new Pulse(10, Pulse.State.ON);

        final var factory = ArcadePacMan_ActorFactory.instance();

        pacMan = factory.createPacMan();
        pacMan.assertComponent(SpriteAnim.class).setAnimations(renderConfig.createPacAnimations(spriteAnimations));

        ghosts[0] = renderConfig.createAnimatedGhost(gameContext(), spriteAnimations, GhostPersonality.RED_GHOST_SHADOW);
        ghosts[1] = renderConfig.createAnimatedGhost(gameContext(), spriteAnimations, GhostPersonality.PINK_GHOST_SPEEDY);
        ghosts[2] = renderConfig.createAnimatedGhost(gameContext(), spriteAnimations, GhostPersonality.CYAN_GHOST_BASHFUL);
        ghosts[3] = renderConfig.createAnimatedGhost(gameContext(), spriteAnimations, GhostPersonality.ORANGE_GHOST_POKEY);

        Arrays.fill(ghostImageVisible, false);
        Arrays.fill(ghostNicknameVisible, false);
        Arrays.fill(ghostCharacterVisible, false);

        titleVisible = false;
        ghostIndex = 0;
        lastGhostEatenTick = 0;
        numGhostsEaten = 0;

        appContext().ui().sounds().voice().playAfterSec(1, GlobalAssets.VoiceID.EXPLAIN_GAME_START.media());
    }

    private void startChasingPacMan(GameContext gameContext) {
        final GameSystems sys = gameContext.systems();

        blinking.start();

        pacMan.position().set(WorldMap.TS * 28, WorldMap.TS * 20);
        pacMan.show();

        sys.navigator().setMoveDir(pacMan, Direction.LEFT);
        sys.navigator().setSpeed(pacMan, CHASING_SPEED);

        sys.spriteAnim().select(pacMan, CommonAnimationID.PAC_MUNCHING);
        sys.spriteAnim().playSelected(pacMan);

        for (Ghost ghost : ghosts) {
            ghost.position().set(pacMan.position().x + 16 * ghost.personality().ordinal() + 18, pacMan.position().y);
            ghost.show();

            sys.navigator().setMoveDir(ghost, Direction.LEFT);
            sys.navigator().setWishDir(ghost, Direction.LEFT);
            sys.navigator().setSpeed(ghost, CHASING_SPEED);

            sys.spriteAnim().select(pacMan, CommonAnimationID.PAC_MUNCHING);
            sys.spriteAnim().playSelected(pacMan);

            sys.ghostState().changeState(gameContext, ghost, GhostState.HUNTING_PAC);
        }
    }

    private void chasePacMan(long tick) {
        final MovementSystem motor = gameContext().systems().motor();
        blinking.triggerPulse();
        motor.moveAccelerated(pacMan);
        for (Ghost ghost : ghosts) {
            motor.moveAccelerated(ghost);
        }

        // "shaking" effect
        final long tick_0_to_5 = tick % 6;
        final Ghost pinkGhost = ghosts[GhostPersonality.PINK_GHOST_SPEEDY.ordinal()];
        final Ghost cyanGhost = ghosts[GhostPersonality.CYAN_GHOST_BASHFUL.ordinal()];
        if (tick_0_to_5 == 2) {
            pinkGhost.position().setX(pinkGhost.position().x + 0.5);
            cyanGhost.position().setX(cyanGhost.position().x - 0.5);
        }
        else if (tick_0_to_5 == 5) {
            pinkGhost.position().setX(pinkGhost.position().x - 0.5);
            cyanGhost.position().setX(cyanGhost.position().x + 0.5);
        }
    }

    private void turnCardsStopPacMan(GameContext gameContext) {
        final GameSystems sys = gameContext.systems();

        sys.navigator().setSpeed(pacMan, 0);
        sys.spriteAnim().stopSelected(pacMan);

        for (Ghost ghost : ghosts) {
            sys.navigator().setMoveDir(ghost, Direction.RIGHT);
            sys.navigator().setWishDir(ghost, Direction.RIGHT);
            sys.navigator().setSpeed(ghost, GHOST_FRIGHTENED_SPEED);

            sys.ghostState().changeState(gameContext, ghost, FRIGHTENED);
        }
    }

    private void turnCardsRestartPacMan(GameSystems sys) {
        sys.navigator().setSpeed(pacMan, CHASING_SPEED);

        sys.spriteAnim().select(pacMan, CommonAnimationID.PAC_MUNCHING);
        sys.spriteAnim().playSelected(pacMan);
    }

    private void chaseGhosts(GameContext gameContext, long tick) {
        final GameSystems sys = gameContext.systems();

        blinking.triggerPulse();

        sys.motor().moveAccelerated(pacMan);

        for (Ghost ghost : ghosts) { sys.motor().moveAccelerated(ghost); }

        edibleGhost().ifPresent(victim -> eatGhostAndStopChasing(gameContext, victim, tick));

        if (tick == lastGhostEatenTick + GHOST_EATING_TICKS) {
            continueChasing(sys);
        }
    }

    private Optional<Ghost> edibleGhost() {
        return Stream.of(ghosts)
            .filter(ghost -> ghost.state() == FRIGHTENED)
            .filter(ghost -> CollisionStrategy.SAME_TILE.collide(ghost, pacMan))
            .findFirst();
    }

    private void eatGhostAndStopChasing(GameContext gameContext, Ghost victim, long tick) {
        final GameSystems sys = gameContext.systems();

        sys.ghostState().changeState(gameContext, victim, EATEN);
        sys.spriteAnim().selectAndSetFrame(victim, CommonAnimationID.GHOST_POINTS, numGhostsEaten++);

        pacMan.hide();
        sys.navigator().setSpeed(pacMan, 0);

        for (Ghost ghost : ghosts) {
            sys.navigator().setSpeed(ghost, 0);
            sys.spriteAnim().stopSelected(ghost);
        }

        lastGhostEatenTick = tick;
    }

    private void continueChasing(GameSystems sys) {
        pacMan.show();
        sys.navigator().setSpeed(pacMan, CHASING_SPEED);

        for (Ghost ghost : ghosts) {
            if (ghost.state() == EATEN) {
                ghost.hide();
            } else {
                ghost.show();
                sys.navigator().setSpeed(ghost, GHOST_FRIGHTENED_SPEED);
                sys.spriteAnim().select(ghost, CommonAnimationID.GHOST_FRIGHTENED);
                sys.spriteAnim().playSelected(ghost);
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
                final GameSystems sys = scene.gameContext().systems();

                final long tick = timer.tickCount();
                if (tick == TICK_PAC_MAN_APPEARS) {
                    scene.startChasingPacMan(scene.gameContext());
                }
                else if (tick == TICK_PAC_MAN_REACHES_ENERGIZER) {
                    scene.turnCardsStopPacMan(scene.gameContext());
                }
                else if (tick == TICK_PAC_MAN_MOVES_AGAIN) {
                    scene.turnCardsRestartPacMan(sys);
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
                final GameSystems sys = scene.gameContext().systems();

                timer.restartTicks(TICK_CHASING_GHOSTS_END);

                scene.lastGhostEatenTick = timer.tickCount();
                scene.numGhostsEaten = 0;

                sys.navigator().setMoveDir(scene.pacMan, Direction.RIGHT);
                sys.navigator().setSpeed(scene.pacMan, CHASING_SPEED);
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                final long tick = timer.tickCount();
                if (tick == TICK_CHASING_GHOSTS_END) {
                    scene.pacMan.hide();
                    scene.flow.enterState(scene, WAIT_FOR_DEMO_LEVEL);
                } else {
                    scene.chaseGhosts(scene.gameContext(), tick);
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
                final GameContext gameContext = scene.gameContext();

                if (timer.tickCount() == TICK_START_DEMO_LEVEL) {
                    scene.ghosts[GhostPersonality.ORANGE_GHOST_POKEY.ordinal()].hide();
                    scene.gameFlow().enterState(gameContext, GameStateID.GAME_OR_LEVEL_STARTING);
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