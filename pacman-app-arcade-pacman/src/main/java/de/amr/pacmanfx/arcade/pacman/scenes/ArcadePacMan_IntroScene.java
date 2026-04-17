/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameState;
import de.amr.pacmanfx.event.CreditAddedEvent;
import de.amr.pacmanfx.lib.Pulse;
import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Resources;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;

import java.util.Arrays;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
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

    // public access for renderer
    public final StateMachine<ArcadePacMan_IntroScene> flow;
    public boolean titleVisible;
    public Pulse blinking;
    public Pac pacMan;
    public final Ghost[] ghosts = new Ghost[NUM_GHOSTS];
    public final boolean[] ghostImageVisible = new boolean[NUM_GHOSTS];
    public final boolean[] ghostNicknameVisible = new boolean[NUM_GHOSTS];
    public final boolean[] ghostCharacterVisible = new boolean[NUM_GHOSTS];

    private int ghostsEaten;
    private int ghostIndex;
    private long lastGhostEatenTick;

    public ArcadePacMan_IntroScene() {
        flow = new StateMachine<>();
        flow.setContext(this);
        flow.addStates(SceneState.values());
    }

    @Override
    public void doInit(Game game) {
        final UIConfig uiConfig = ui.currentConfig();

        ui.voicePlayer().playVoice(GameUI_Resources.VOICE_EXPLAIN_GAME_START);

        actionBindings.bindAll(ArcadePacMan_UIConfig.GAME_START_ACTION_BINDINGS); // insert coin + start game actions
        actionBindings.bindAll(GameUI.SCENE_TESTS_BINDINGS); // actions for starting tests

        blinking = new Pulse(10, Pulse.State.ON);

        pacMan = ArcadePacMan_GameModel.createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations(ui.spriteAnimationDriver()));

        ghosts[0] = uiConfig.createGhostWithAnimations(ui.spriteAnimationDriver(), RED_GHOST_SHADOW);
        ghosts[1] = uiConfig.createGhostWithAnimations(ui.spriteAnimationDriver(), PINK_GHOST_SPEEDY);
        ghosts[2] = uiConfig.createGhostWithAnimations(ui.spriteAnimationDriver(), CYAN_GHOST_BASHFUL);
        ghosts[3] = uiConfig.createGhostWithAnimations(ui.spriteAnimationDriver(), ORANGE_GHOST_POKEY);

        Arrays.fill(ghostImageVisible, false);
        Arrays.fill(ghostNicknameVisible, false);
        Arrays.fill(ghostCharacterVisible, false);

        titleVisible = false;
        ghostIndex = 0;
        lastGhostEatenTick = 0;
        ghostsEaten = 0;

        flow.restartState(SceneState.STARTING);
    }

    @Override
    protected void doEnd(Game game) {
        blinking.stop();
        ui.voicePlayer().stopVoice();
    }

    @Override
    public void update(Game game) {
        flow.update();
    }

    @Override
    public void onCreditAdded(CreditAddedEvent e) {
        soundEffects().ifPresent(GameSoundEffects::playCoinInsertedSound);
    }

    public enum SceneState implements State<ArcadePacMan_IntroScene> {

        STARTING {
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
                if (timer.tickCount() == TICK_PAC_MAN_APPEARS) {
                    scene.blinking.start();
                    scene.pacMan.setPosition(TS * 28, TS * 20);
                    scene.pacMan.setMoveDir(Direction.LEFT);
                    scene.pacMan.setSpeed(CHASING_SPEED);
                    scene.pacMan.selectAnimation(Pac.AnimationID.PAC_MUNCHING);
                    scene.pacMan.playAnimation();
                    scene.pacMan.show();
                    for (Ghost ghost : scene.ghosts) {
                        ghost.setState(GhostState.HUNTING_PAC);
                        ghost.setMoveDir(Direction.LEFT);
                        ghost.setWishDir(Direction.LEFT);
                        ghost.setSpeed(CHASING_SPEED);
                        ghost.setPosition(scene.pacMan.x() + 16 * ghost.personality() + 18, scene.pacMan.y());
                        ghost.show();
                        ghost.selectAnimation(Ghost.AnimationID.GHOST_NORMAL);
                        ghost.playAnimation();
                    }
                }
                else if (timer.tickCount() == TICK_PAC_MAN_REACHES_ENERGIZER) {
                    // Pac-Man reaches the energizer at the left and stops
                    scene.pacMan.setSpeed(0);
                    scene.pacMan.stopAnimation();
                    // Ghosts get frightened and reverse direction
                    for (Ghost ghost : scene.ghosts) {
                        ghost.setState(FRIGHTENED);
                        ghost.setMoveDir(Direction.RIGHT);
                        ghost.setWishDir(Direction.RIGHT);
                        ghost.setSpeed(GHOST_FRIGHTENED_SPEED);
                    }
                }
                else if (timer.tickCount() == TICK_PAC_MAN_MOVES_AGAIN) {
                    // Pac-Man moves again a bit
                    scene.pacMan.selectAnimation(Pac.AnimationID.PAC_MUNCHING);
                    scene.pacMan.playAnimation();
                    scene.pacMan.setSpeed(CHASING_SPEED);
                }
                else if (timer.tickCount() == TICK_CHASING_PAC_MAN_END) {
                    scene.flow.enterState(CHASING_GHOSTS);
                    return;
                }

                chasePacMan(scene);
            }

            private void chasePacMan(ArcadePacMan_IntroScene scene) {
                scene.blinking.doTick();
                scene.pacMan.move();
                for (Ghost ghost : scene.ghosts) {
                    ghost.move();
                }

                // "shaking" effect
                final long tick_0_to_5 = timer.tickCount() % 6;
                final Ghost pinkGhost = scene.ghosts[PINK_GHOST_SPEEDY];
                final Ghost cyanGhost = scene.ghosts[CYAN_GHOST_BASHFUL];
                if (tick_0_to_5 == 2) {
                    pinkGhost.setX(pinkGhost.x() + 0.5);
                    cyanGhost.setX(cyanGhost.x() - 0.5);
                }
                else if (tick_0_to_5 == 5) {
                    pinkGhost.setX(pinkGhost.x() - 0.5);
                    cyanGhost.setX(cyanGhost.x() + 0.5);
                }
            }
        },

        CHASING_GHOSTS {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                timer.restartTicks(TICK_CHASING_GHOSTS_END);
                scene.lastGhostEatenTick = timer.tickCount();
                scene.pacMan.setMoveDir(Direction.RIGHT);
                scene.pacMan.setSpeed(CHASING_SPEED);
                scene.ghostsEaten = 0;
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (timer.tickCount() == TICK_CHASING_GHOSTS_END) {
                    scene.pacMan.hide();
                    scene.flow.enterState(READY_TO_PLAY);
                    return;
                }

                Stream.of(scene.ghosts)
                    .filter(ghost -> ghost.state() == FRIGHTENED)
                    .filter(ghost -> CollisionStrategy.SAME_TILE.collide(ghost, scene.pacMan))
                    .findFirst()
                    .ifPresent(victim -> {
                        victim.setState(EATEN);
                        victim.selectAnimationAndSetFrame(Ghost.AnimationID.GHOST_POINTS, scene.ghostsEaten);
                        scene.ghostsEaten++;
                        scene.lastGhostEatenTick = timer.tickCount();
                        scene.pacMan.hide();
                        scene.pacMan.setSpeed(0);
                        for (Ghost ghost : scene.ghosts) {
                            ghost.setSpeed(0);
                            ghost.stopAnimation();
                        }
                    });

                // After 50 ticks, Pac-Man and the surviving ghosts get visible again and move on
                if (timer.tickCount() == scene.lastGhostEatenTick + GHOST_EATING_TICKS) {
                    scene.pacMan.show();
                    scene.pacMan.setSpeed(CHASING_SPEED);
                    for (Ghost ghost : scene.ghosts) {
                        if (ghost.inAnyOfStates(EATEN)) {
                            ghost.hide();
                        } else {
                            ghost.show();
                            ghost.setSpeed(GHOST_FRIGHTENED_SPEED);
                            ghost.selectAnimation(Ghost.AnimationID.GHOST_FRIGHTENED);
                            ghost.playAnimation();
                        }
                    }
                }

                scene.pacMan.move();
                for (Ghost ghost : scene.ghosts) { ghost.move(); }
                scene.blinking.doTick();
            }
        },

        READY_TO_PLAY {
            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                final Game game = scene.gameContext().game();
                if (timer.atSecond(1)) {
                    scene.ghosts[ORANGE_GHOST_POKEY].hide();
                    if (!game.canStartNewGame()) {
                        game.flow().enterState(Arcade_GameState.STARTING_GAME_OR_LEVEL);
                    }
                } else if (timer.atSecond(5)) {
                    game.flow().enterState(Arcade_GameState.PREPARING_GAME_START);
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