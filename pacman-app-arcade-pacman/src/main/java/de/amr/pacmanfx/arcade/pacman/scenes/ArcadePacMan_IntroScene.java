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
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.GameVariantConfig;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.model.actors.GhostState.EATEN;
import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;

/**
 * The ghosts are presented one by one, then Pac-Man is chased by the ghosts, turns the cards and hunts the ghosts himself.
 */
public class ArcadePacMan_IntroScene extends GameScene2D {

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

    public ArcadePacMan_IntroScene(Game game) {
        super(game);

        flow = new StateMachine<>(this, List.of(SceneState.values()));
    }

    @Override
    public void onActivate() {
        final Arcade_Actions actions = game().extensions()
            .get(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        actionBindings().registerAllBindings(actions.gameStartActionBindings()); // insert coin + start game actions
        actionBindings().registerAllBindings(game().actions().sceneTestActions().bindings()); // actions for starting tests

        flow.restartState(SceneState.STARTING);
    }

    @Override
    public void onDeactivate() {
        blinking.stop();
        game().ui().sounds().stopAndDisposeVoice();
        actionBindings().dispose();
    }

    @Override
    public void onTick(long tick) {
        flow.update();
    }

    private void initScene() {
        final GameVariantConfig uiConfig = game().currentVariantConfig();
        final SpriteAnimationContainer spriteAnimations = game().ui().sprites().animations();

        blinking = new Pulse(10, Pulse.State.ON);

        pacMan = ArcadePacMan_ActorFactory.createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations(spriteAnimations));

        ghosts[0] = uiConfig.createAnimatedGhost(spriteAnimations, GameModel.RED_GHOST_SHADOW);
        ghosts[1] = uiConfig.createAnimatedGhost(spriteAnimations, GameModel.PINK_GHOST_SPEEDY);
        ghosts[2] = uiConfig.createAnimatedGhost(spriteAnimations, GameModel.CYAN_GHOST_BASHFUL);
        ghosts[3] = uiConfig.createAnimatedGhost(spriteAnimations, GameModel.ORANGE_GHOST_POKEY);

        Arrays.fill(ghostImageVisible, false);
        Arrays.fill(ghostNicknameVisible, false);
        Arrays.fill(ghostCharacterVisible, false);

        titleVisible = false;
        ghostIndex = 0;
        lastGhostEatenTick = 0;
        numGhostsEaten = 0;

        game().ui().sounds().playVoice(GlobalAssets.Voice.EXPLAIN_GAME_START.media());
    }

    private void startChasingPacMan() {
        blinking.start();
        pacMan.setPosition(WorldMap.TS * 28, WorldMap.TS * 20);
        pacMan.setMoveDir(Direction.LEFT);
        pacMan.setSpeed(CHASING_SPEED);
        pacMan.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
        pacMan.animations().playSelected();
        pacMan.show();
        for (Ghost ghost : ghosts) {
            ghost.setState(GhostState.HUNTING_PAC);
            ghost.setMoveDir(Direction.LEFT);
            ghost.setWishDir(Direction.LEFT);
            ghost.setSpeed(CHASING_SPEED);
            ghost.setPosition(pacMan.x() + 16 * ghost.personality() + 18, pacMan.y());
            ghost.show();
            ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
            ghost.animations().playSelected();
        }
    }

    private void chasePacMan(long tick) {
        blinking.triggerPulse();
        pacMan.move();
        for (Ghost ghost : ghosts) {
            ghost.move();
        }

        // "shaking" effect
        final long tick_0_to_5 = tick % 6;
        final Ghost pinkGhost = ghosts[GameModel.PINK_GHOST_SPEEDY];
        final Ghost cyanGhost = ghosts[GameModel.CYAN_GHOST_BASHFUL];
        if (tick_0_to_5 == 2) {
            pinkGhost.setX(pinkGhost.x() + 0.5);
            cyanGhost.setX(cyanGhost.x() - 0.5);
        }
        else if (tick_0_to_5 == 5) {
            pinkGhost.setX(pinkGhost.x() - 0.5);
            cyanGhost.setX(cyanGhost.x() + 0.5);
        }
    }

    private void turnCardsStopPacMan() {
        pacMan.setSpeed(0);
        pacMan.animations().stopSelected();
        for (Ghost ghost : ghosts) {
            ghost.setState(FRIGHTENED);
            ghost.setMoveDir(Direction.RIGHT);
            ghost.setWishDir(Direction.RIGHT);
            ghost.setSpeed(GHOST_FRIGHTENED_SPEED);
        }
    }

    private void turnCardsRestartPacMan() {
        pacMan.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
        pacMan.animations().playSelected();
        pacMan.setSpeed(CHASING_SPEED);
    }

    private void chaseGhosts(long tick) {
        blinking.triggerPulse();
        pacMan.move();
        for (Ghost ghost : ghosts) { ghost.move(); }
        edibleGhost().ifPresent(victim -> eatGhostAndStopChasing(victim, tick));
        if (tick == lastGhostEatenTick + GHOST_EATING_TICKS) {
            continueChasing();
        }
    }

    private Optional<Ghost> edibleGhost() {
        return Stream.of(ghosts)
            .filter(ghost -> ghost.state() == FRIGHTENED)
            .filter(ghost -> CollisionStrategy.SAME_TILE.collide(ghost, pacMan))
            .findFirst();
    }

    private void eatGhostAndStopChasing(Ghost victim, long tick) {
        victim.setState(EATEN);
        victim.animations().selectAndSetFrame(ArcadePacMan_AnimationID.GHOST_POINTS, numGhostsEaten++);
        pacMan.hide();
        pacMan.setSpeed(0);
        for (Ghost ghost : ghosts) {
            ghost.setSpeed(0);
            ghost.animations().stopSelected();
        }
        lastGhostEatenTick = tick;
    }

    private void continueChasing() {
        pacMan.show();
        pacMan.setSpeed(CHASING_SPEED);
        for (Ghost ghost : ghosts) {
            if (ghost.state() == EATEN) {
                ghost.hide();
            } else {
                ghost.show();
                ghost.setSpeed(GHOST_FRIGHTENED_SPEED);
                ghost.animations().select(ArcadePacMan_AnimationID.GHOST_FRIGHTENED);
                ghost.animations().playSelected();
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
                    scene.flow.enterState(PRESENTING_GHOSTS);
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
                    case TICK_GHOST_PRESENTATION_END  -> scene.flow.enterState(SHOWING_POINTS);
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
                    scene.flow.enterState(CHASING_PAC_MAN);
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
                final long tick = timer.tickCount();
                if (tick == TICK_PAC_MAN_APPEARS) {
                    scene.startChasingPacMan();
                }
                else if (tick == TICK_PAC_MAN_REACHES_ENERGIZER) {
                    scene.turnCardsStopPacMan();
                }
                else if (tick == TICK_PAC_MAN_MOVES_AGAIN) {
                    scene.turnCardsRestartPacMan();
                }
                else if (tick == TICK_CHASING_PAC_MAN_END) {
                    scene.flow.enterState(CHASING_GHOSTS);
                    return;
                }
                scene.chasePacMan(tick);
            }
        },

        CHASING_GHOSTS {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                timer.restartTicks(TICK_CHASING_GHOSTS_END);
                scene.lastGhostEatenTick = timer.tickCount();
                scene.pacMan.setMoveDir(Direction.RIGHT);
                scene.pacMan.setSpeed(CHASING_SPEED);
                scene.numGhostsEaten = 0;
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                final long tick = timer.tickCount();
                if (tick == TICK_CHASING_GHOSTS_END) {
                    scene.pacMan.hide();
                    scene.flow.enterState(WAIT_FOR_DEMO_LEVEL);
                } else {
                    scene.chaseGhosts(tick);
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
                if (timer.tickCount() == TICK_START_DEMO_LEVEL) {
                    scene.ghosts[GameModel.ORANGE_GHOST_POKEY].hide();
                    scene.gameContext().flow().enterState(GameStateID.GAME_OR_LEVEL_STARTING);
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