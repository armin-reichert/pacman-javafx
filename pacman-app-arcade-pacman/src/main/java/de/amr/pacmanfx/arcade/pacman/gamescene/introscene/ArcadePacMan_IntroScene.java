/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.introscene;

import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateMachine;
import de.amr.basics.math.Direction;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.GhostPoints;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.ghost.system.GhostAnimationSystem;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.rules.CollisionStrategy;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.ui.VoiceID;
import de.amr.pacmanfx.ui.action.core.GameApp;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.core.entities.ghost.comp.GhostState.EATEN;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;

/**
 * The ghosts are presented one by one, then Pac-Man is chased by the ghosts, turns the cards and hunts the ghosts himself.
 */
public class ArcadePacMan_IntroScene extends AbstractGameScene {

    private static final int[] GHOST_POINTS = { 200, 400, 800, 1600 };

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

    private final StateMachine<ArcadePacMan_IntroScene> flow;
    private final IntroSceneView view;

    private int numGhostsEaten;
    private int ghostIndex;
    private long lastGhostEatenTick;

    public ArcadePacMan_IntroScene(GameApp app) {
        super(app);
        setComp(GameSceneCanvasRenderingComp.class, new GameSceneCanvasRenderingComp());
        flow = new StateMachine<>(List.of(SceneState.values()));
        view = new IntroSceneView();
    }

    @Override
    public void onActivate() {
        final Arcade_Actions actions = app.variantManager().currentRuntime()
            .extensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        final var bindingsMap = actionBindings().registry();
        bindingsMap.registerAllBindings(actions.gameStartActionBindings()); // insert coin + start game actions
        bindingsMap.registerAllBindings(app.commonActions().sceneTestActions().bindings()); // actions for starting tests

        flow.restartState(this, SceneState.STARTING);
    }

    @Override
    public void onDeactivate() {
        view.pulse.stop();
        soundManager().voice().stop();
    }

    @Override
    public void onTick(GameContext game) {
        flow.update(this);
    }

    @Override
    public Stream<Renderable> renderables() {
        return view.renderables();
    }

    private void initSceneState() {
        final GameVariantRuntime variant = app.variantManager().currentRuntime();
        final GameVariantRenderConfig renderConfig = variant.uiConfig().renderConfig();
        final SpriteAnimationContainer animContainer    = variant.spriteAnimContainer();
        final ActorSpriteAnimController animController  = variant.playConfig().systems().actorSpriteAnimController();
        final var actorFactory = ArcadePacMan_ActorFactory.instance();

        view.createPacMan(actorFactory, renderConfig, animContainer);
        view.createGhosts(renderConfig, animController, animContainer);
        view.initEntityVisibility();

        ghostIndex = 0;
        lastGhostEatenTick = 0;
        numGhostsEaten = 0;

        soundManager().voice().playAfterSec(1, VoiceID.START_HINT.media());
    }

    // Animation

    private void startChasingPacMan(GameContext game) {
        final GameSystems systems = game.playConfig().systems();
        final WorldNavigationSystem nav = systems.navigator();

        view.pulse.start();

        view.pacMan.pos().set(TS * 28, TS * 20);
        nav.setMoveDir(view.pacMan, Direction.LEFT);
        nav.setSpeed(view.pacMan, CHASING_SPEED);
        view.pacMan.show();

        for (Ghost ghost : view.ghosts) {
            ghost.pos().set(view.pacMan.pos().x() + 16 * ghost.personality().ordinal() + 18, view.pacMan.pos().y());
            nav.setMoveDir(ghost, Direction.LEFT);
            nav.setWishDir(ghost, Direction.LEFT);
            nav.setSpeed(ghost, CHASING_SPEED);
            systems.ghostState().setState(ghost, GhostState.HUNTING_PAC);
            ghost.show();
        }
    }

    private void chasePacMan(long tick) {
        final GameSystems systems = game().playConfig().systems();
        final MovementSystem motor = systems.motor();
        final GhostAnimationSystem ghostSpriteAnimationSystem = systems.ghostAnimation();

        view.pulse.triggerPulse();

        motor.move(view.pacMan);
        for (Ghost ghost : view.ghosts) {
            motor.move(ghost);
        }

        // "shaking" effect
        final long tick_0_to_5 = tick % 6;
        final Ghost pinkGhost = view.ghosts[GhostPersonality.PINK_GHOST_SPEEDY.ordinal()];
        final Ghost cyanGhost = view.ghosts[GhostPersonality.CYAN_GHOST_BASHFUL.ordinal()];
        if (tick_0_to_5 == 2) {
            pinkGhost.pos().setX(pinkGhost.pos().x() + 0.5);
            cyanGhost.pos().setX(cyanGhost.pos().x() - 0.5);
        }
        else if (tick_0_to_5 == 5) {
            pinkGhost.pos().setX(pinkGhost.pos().x() - 0.5);
            cyanGhost.pos().setX(cyanGhost.pos().x() + 0.5);
        }

        for (Ghost ghost : view.ghosts) {
            ghostSpriteAnimationSystem.update(ghost);
        }
    }

    private void turnCardsStopPacMan(GameContext game) {
        final GameSystems systems = game.playConfig().systems();
        final WorldNavigationSystem nav = systems.navigator();
        final ActorSpriteAnimController animController = systems.actorSpriteAnimController();

        nav.setSpeed(view.pacMan, 0);
        systems.actorSpriteAnimController().stopSelected(view.pacMan);

        for (Ghost ghost : view.ghosts) {
            nav.setMoveDir(ghost, Direction.RIGHT);
            nav.setWishDir(ghost, Direction.RIGHT);
            nav.setSpeed(ghost, GHOST_FRIGHTENED_SPEED);
            systems.ghostState().setState(ghost, GhostState.FRIGHTENED);
            animController.select(ghost, CommonSpriteAnimationID.GHOST_FRIGHTENED);
            animController.playSelected(ghost);
        }
    }

    private void turnCardsRestartPacMan(GameSystems systems) {
        systems.navigator().setSpeed(view.pacMan, CHASING_SPEED);
        systems.actorSpriteAnimController().playSelected(view.pacMan);
    }

    private void chaseGhosts(GameContext game, long tick) {
        final GameSystems systems = game.playConfig().systems();
        final MovementSystem motor = systems.motor();

        view.pulse.triggerPulse();

        motor.move(view.pacMan);
        for (Ghost ghost : view.ghosts) {
            motor.move(ghost);
        }

        findNextEdibleGhost().ifPresent(victim -> eatGhostAndStopChasing(game, victim, tick));
        if (tick == lastGhostEatenTick + GHOST_EATING_TICKS) {
            continueChasing(systems);
        }
    }

    private Optional<Ghost> findNextEdibleGhost() {
        return Stream.of(view.ghosts)
            .filter(ghost -> ghost.state().enumValue() != GhostState.EATEN)
            .filter(ghost -> CollisionStrategy.SAME_TILE.collide(ghost, view.pacMan))
            .findFirst();
    }

    private void eatGhostAndStopChasing(GameContext game, Ghost victim, long tick) {
        final GameSystems systems = game.playConfig().systems();

        victim.state().setEnumValue(GhostState.EATEN);
        victim.hide();

        view.pacMan.hide();
        systems.navigator().setSpeed(view.pacMan, 0);

        for (Ghost ghost : view.ghosts) {
            systems.navigator().setSpeed(ghost, 0);
            systems.actorSpriteAnimController().stopSelected(ghost);
        }

        view.points = new GhostPoints(GHOST_POINTS[numGhostsEaten]);
        view.points.pos().set(victim.pos().asVector2f());
        view.points.show();

        ++numGhostsEaten;
        lastGhostEatenTick = tick;
    }

    private void continueChasing(GameSystems systems) {
        view.pacMan.show();
        systems.navigator().setSpeed(view.pacMan, CHASING_SPEED);

        for (Ghost ghost : view.ghosts) {
            if (ghost.state().enumValue() == EATEN) {
                ghost.hide();
                view.points = null;
            } else {
                ghost.show();
                systems.navigator().setSpeed(ghost, GHOST_FRIGHTENED_SPEED);
                ghost.spriteAnimation().spriteAnimations().playSelected();
            }
        }
    }

    // Scene flow state machine

    public enum SceneState implements State<ArcadePacMan_IntroScene> {

        STARTING {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                scene.initSceneState();
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (timer.tickCount() == TICK_TITLE_VISIBLE) {
                    scene.view.titleText.show();
                } else if (timer.tickCount() == TICK_START_PRESENTING_GHOSTS) {
                    scene.flow.enterState(scene, PRESENTING_GHOSTS);
                }
            }
        },

        PRESENTING_GHOSTS {
            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                final int t = (int) timer.tickCount();
                if (t > TICK_GHOST_PRESENTATION_END) {
                    return;
                }
                switch (t) {
                    case TICK_GHOST_SPRITE_VISIBLE    -> scene.view.ghostImageDisplays[scene.ghostIndex].show();
                    case TICK_GHOST_CHARACTER_VISIBLE -> scene.view.ghostCharacterDisplays[scene.ghostIndex].show();
                    case TICK_GHOST_NICKNAME_VISIBLE  -> scene.view.ghostNicknameDisplays[scene.ghostIndex].show();
                    case TICK_GHOST_PRESENT_NEXT      -> presentNextGhost(scene);
                    case TICK_GHOST_PRESENTATION_END  -> scene.flow.enterState(scene, SHOWING_POINTS);
                }
            }

            private void presentNextGhost(ArcadePacMan_IntroScene scene) {
                if (scene.ghostIndex < 3) {
                    scene.ghostIndex += 1;
                    timer.resetToIndefiniteDuration();
                }
            }
        },

        SHOWING_POINTS {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                scene.view.pulse.stopAndReset();
                scene.view.energizer.show();
                scene.view.pellet.show();
                scene.view.text10.show();
                scene.view.text10Pts.show();
                scene.view.text50.show();
                scene.view.text50Pts.show();
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
                scene.view.pacMan.hide();
                scene.view.targetEnergizer.show();
                scene.view.copyrightText.show();
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                final GameSystems systems = scene.game().playConfig().systems();

                final long tick = timer.tickCount();
                if (tick == TICK_PAC_MAN_APPEARS) {
                    scene.startChasingPacMan(scene.game());
                }
                else if (tick == TICK_PAC_MAN_REACHES_ENERGIZER) {
                    scene.turnCardsStopPacMan(scene.game());
                    scene.view.targetEnergizer.hide();
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
                final GameSystems systems = scene.game().playConfig().systems();

                timer.restartTicks(TICK_CHASING_GHOSTS_END);

                scene.lastGhostEatenTick = timer.tickCount();
                scene.numGhostsEaten = 0;

                systems.navigator().setMoveDir(scene.view.pacMan, Direction.RIGHT);
                systems.navigator().setSpeed(scene.view.pacMan, CHASING_SPEED);
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                final long tick = timer.tickCount();
                if (tick == TICK_CHASING_GHOSTS_END) {
                    scene.view.pacMan.hide();
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

                scene.view.pulse.triggerPulse();

                if (timer.tickCount() == TICK_START_DEMO_LEVEL) {
                    scene.view.ghosts[GhostPersonality.ORANGE_GHOST_POKEY.ordinal()].hide();
                    scene.flow().enterGameState(game, CommonGameStateID.GAME_OR_LEVEL_STARTING);
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