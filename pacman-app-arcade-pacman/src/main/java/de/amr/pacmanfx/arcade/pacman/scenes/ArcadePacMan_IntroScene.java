/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState;
import de.amr.pacmanfx.event.CreditAddedEvent;
import de.amr.pacmanfx.lib.Pulse;
import de.amr.pacmanfx.lib.TickTimer;
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
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.sound.SoundID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.model.actors.GhostState.EATEN;
import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;

/**
 * The ghosts are presented one by one, then Pac-Man is chased by the ghosts, turns the cards and hunts the ghosts himself.
 */
public class ArcadePacMan_IntroScene extends GameScene2D {

    private static final float CHASING_SPEED = 1.1f;
    private static final float GHOST_FRIGHTENED_SPEED = 0.6f;

    private final StateMachine<ArcadePacMan_IntroScene> sceneController;

    private Pulse blinking;
    private Pac pacMan;
    private List<Ghost> ghosts;
    private boolean[] ghostImageVisible;
    private boolean[] ghostNicknameVisible;
    private boolean[] ghostCharacterVisible;
    private List<Ghost> victims;
    private boolean titleVisible;
    private int ghostIndex;
    private long ghostKilledTime;

    public ArcadePacMan_IntroScene() {
        sceneController = new StateMachine<>(this, List.of(SceneState.values()));
    }

    public boolean isTitleVisible() {
        return titleVisible;
    }

    public boolean isGhostImageVisible(byte personality) {
        return ghostImageVisible[personality];
    }

    public boolean isGhostCharacterVisible(byte personality) {
        return ghostCharacterVisible[personality];
    }

    public boolean isGhostNicknameVisible(byte personality) {
        return ghostNicknameVisible[personality];
    }

    public List<Ghost> ghosts() {
        return Collections.unmodifiableList(ghosts);
    }

    public Pac pacMan() {
        return pacMan;
    }

    public Pulse blinking() {
        return blinking;
    }

    @Override
    public void doInit(Game game) {
        final UIConfig uiConfig = ui.currentConfig();

        game.hud().credit(true).score(true).livesCounter(false).levelCounter(true).show();

        ui.voicePlayer().play(GameUI_Resources.VOICE_EXPLAIN_GAME_START);

        actionBindings.registerAllBindingsFrom(ArcadePacMan_UIConfig.DEFAULT_BINDINGS); // insert coin + start game actions
        actionBindings.registerAllBindingsFrom(GameUI.SCENE_TESTS_BINDINGS); // actions for starting tests

        blinking = new Pulse(10, Pulse.State.ON);

        pacMan = ArcadePacMan_GameModel.createPacMan();
        pacMan.setAnimationManager(uiConfig.createPacAnimations());
        pacMan.selectAnimation(Pac.AnimationID.PAC_MUNCHING);

        ghosts = List.of(
            uiConfig.createGhostWithAnimations(RED_GHOST_SHADOW),
            uiConfig.createGhostWithAnimations(PINK_GHOST_SPEEDY),
            uiConfig.createGhostWithAnimations(CYAN_GHOST_BASHFUL),
            uiConfig.createGhostWithAnimations(ORANGE_GHOST_POKEY)
        );

        ghostImageVisible     = new boolean[4];
        ghostNicknameVisible  = new boolean[4];
        ghostCharacterVisible = new boolean[4];

        victims = new ArrayList<>(4);
        titleVisible = false;
        ghostIndex = 0;
        ghostKilledTime = 0;

        sceneController.restart(SceneState.STARTING);
    }

    @Override
    protected void doEnd(Game game) {
        blinking.stop();
        ui.voicePlayer().stop();
    }

    @Override
    public void update(Game game) {
        sceneController.update();
    }

    @Override
    public void onCreditAdded(CreditAddedEvent e) {
        ui.soundManager().play(SoundID.COIN_INSERTED);
    }

    public SceneState state() {
        return (SceneState) sceneController.state();
    }

    public enum SceneState implements StateMachine.State<ArcadePacMan_IntroScene> {

        STARTING {
            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (timer.tickCount() == 3) {
                    scene.titleVisible = true;
                } else if (timer.atSecond(1)) {
                    scene.sceneController.enterState(PRESENTING_GHOSTS);
                }
            }
        },

        PRESENTING_GHOSTS {
            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (timer.tickCount() == 1) {
                    scene.ghostImageVisible[scene.ghostIndex] = true;
                } else if (timer.atSecond(1.0)) {
                    scene.ghostCharacterVisible[scene.ghostIndex] = true;
                } else if (timer.atSecond(1.5)) {
                    scene.ghostNicknameVisible[scene.ghostIndex] = true;
                } else if (timer.atSecond(2.0)) {
                    if (scene.ghostIndex < scene.ghosts.size() - 1) {
                        timer.resetIndefiniteTime();
                    }
                    scene.ghostIndex += 1;
                } else if (timer.atSecond(2.5)) {
                    scene.sceneController.enterState(SHOWING_POINTS);
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
                if (timer.atSecond(1)) {
                    scene.sceneController.enterState(CHASING_PAC);
                }
            }
        },

        CHASING_PAC {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                timer.restartIndefinitely();
                scene.pacMan.setPosition(TS * 36, TS * 20);
                scene.pacMan.setMoveDir(Direction.LEFT);
                scene.pacMan.setSpeed(CHASING_SPEED);
                scene.pacMan.show();
                scene.pacMan.playAnimation(Pac.AnimationID.PAC_MUNCHING);
                scene.ghosts.forEach(ghost -> {
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setMoveDir(Direction.LEFT);
                    ghost.setWishDir(Direction.LEFT);
                    ghost.setSpeed(CHASING_SPEED);
                    ghost.setPosition(scene.pacMan.x() + 16 * (ghost.personality() + 1), scene.pacMan.y());
                    ghost.show();
                    ghost.playAnimation(Ghost.AnimationID.GHOST_NORMAL);
                });
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (timer.atSecond(1)) {
                    scene.blinking.start();
                }
                else if (timer.tickCount() == 232) {
                    // Pac-Man reaches the energizer at the left and stops
                    scene.pacMan.setSpeed(0);
                    scene.pacMan.stopAnimation();
                    // Ghosts get frightened and reverse direction
                    scene.ghosts.forEach(ghost -> {
                        ghost.setState(FRIGHTENED);
                        ghost.setMoveDir(Direction.RIGHT);
                        ghost.setWishDir(Direction.RIGHT);
                        ghost.setSpeed(GHOST_FRIGHTENED_SPEED);
                    });
                } else if (timer.tickCount() == 236) {
                    // Pac-Man moves again a bit
                    scene.pacMan.playAnimation(Pac.AnimationID.PAC_MUNCHING);
                    scene.pacMan.setSpeed(CHASING_SPEED);
                } else if (timer.tickCount() == 240) {
                    scene.sceneController.enterState(CHASING_GHOSTS);
                }
                scene.blinking.tick();
                scene.pacMan.move();
                scene.ghosts.forEach(Ghost::move);

                // shaking effect
                final long frame = timer.tickCount() % 6;
                final Ghost pinkGhost = scene.ghosts.get(PINK_GHOST_SPEEDY);
                final Ghost cyanGhost = scene.ghosts.get(CYAN_GHOST_BASHFUL);
                if (frame == 2) {
                    pinkGhost.setX(pinkGhost.x() + 0.5);
                    cyanGhost.setX(cyanGhost.x() - 0.5);
                } else if (frame == 5) {
                    pinkGhost.setX(pinkGhost.x() - 0.5);
                    cyanGhost.setX(cyanGhost.x() + 0.5);
                }
            }
        },

        CHASING_GHOSTS {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                timer.restartIndefinitely();
                scene.ghostKilledTime = timer.tickCount();
                scene.pacMan.setMoveDir(Direction.RIGHT);
                scene.pacMan.setSpeed(CHASING_SPEED);
                scene.victims.clear();
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (scene.ghosts.stream().allMatch(ghost -> ghost.inAnyOfStates(EATEN))) {
                    scene.pacMan.hide();
                    scene.sceneController.enterState(READY_TO_PLAY);
                    return;
                }

                scene.ghosts.stream()
                    .filter(ghost -> ghost.state() == FRIGHTENED)
                    .filter(ghost -> CollisionStrategy.SAME_TILE.collide(ghost, scene.pacMan))
                    .findFirst()
                    .ifPresent(victim -> {
                        scene.victims.add(victim);
                        scene.ghostKilledTime = timer.tickCount();
                        scene.pacMan.hide();
                        scene.pacMan.setSpeed(0);
                        scene.ghosts.forEach(ghost -> {
                            ghost.setSpeed(0);
                            ghost.stopAnimation();
                        });
                        victim.setState(EATEN);
                        victim.selectAnimationAt(Ghost.AnimationID.GHOST_POINTS, scene.victims.size() - 1);
                    });

                // After 50 ticks, Pac-Man and the surviving ghosts get visible again and move on
                if (timer.tickCount() == scene.ghostKilledTime + 50) {
                    scene.pacMan.show();
                    scene.pacMan.setSpeed(CHASING_SPEED);
                    scene.ghosts.forEach(ghost -> {
                        if (ghost.inAnyOfStates(EATEN)) {
                            ghost.hide();
                        } else {
                            ghost.show();
                            ghost.setSpeed(GHOST_FRIGHTENED_SPEED);
                            ghost.playAnimation(Ghost.AnimationID.GHOST_FRIGHTENED);
                        }
                    });
                }

                scene.pacMan.move();
                scene.ghosts.forEach(Ghost::move);
                scene.blinking.tick();
            }
        },

        READY_TO_PLAY {
            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                final Game game = scene.gameContext().currentGame();
                if (timer.atSecond(0.75)) {
                    scene.ghosts.get(ORANGE_GHOST_POKEY).hide();
                    if (!game.canStartNewGame()) {
                        game.control().enterState(GameState.STARTING_GAME_OR_LEVEL);
                    }
                } else if (timer.atSecond(5)) {
                    game.control().enterState(GameState.SETTING_OPTIONS_FOR_START);
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