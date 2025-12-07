/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState;
import de.amr.pacmanfx.arcade.pacman.model.actors.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_HUD_Renderer;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_IntroScene_Renderer;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.canvas.Canvas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.model.actors.GhostState.EATEN;
import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;

/**
 * The ghosts are presented one by one, Pac-Man is chased by the ghosts, turns the cards and hunts the ghosts himself.
 */
public class ArcadePacMan_IntroScene extends GameScene2D {

    private static final float CHASING_SPEED = 1.1f;
    private static final float GHOST_FRIGHTENED_SPEED = 0.6f;

    private final StateMachine<ArcadePacMan_IntroScene> sceneController;

    private ArcadePacMan_IntroScene_Renderer sceneRenderer;
    private ArcadePacMan_HUD_Renderer hudRenderer;

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

    public ArcadePacMan_IntroScene(GameUI ui) {
        super(ui);
        sceneController = new StateMachine<>();
        sceneController.setContext(this);
        sceneController.addStates(SceneState.values());
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        final GameUI_Config uiConfig = ui.currentConfig();

        hudRenderer = configureRenderer(
            (ArcadePacMan_HUD_Renderer) uiConfig.createHUDRenderer(canvas));

        sceneRenderer = configureRenderer(
            new ArcadePacMan_IntroScene_Renderer(this, canvas, (ArcadePacMan_SpriteSheet) uiConfig.spriteSheet()));
    }

    @Override
    public ArcadePacMan_HUD_Renderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public ArcadePacMan_IntroScene_Renderer sceneRenderer() {
        return sceneRenderer;
    }

    public boolean titleVisible() {
        return titleVisible;
    }

    public boolean ghostImageVisible(byte personality) {
        return ghostImageVisible[personality];
    }

    public boolean ghostCharacterVisible(byte personality) {
        return ghostCharacterVisible[personality];
    }

    public boolean ghostNicknameVisible(byte personality) {
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
        final GameUI_Config uiConfig = ui.currentConfig();

        ui.soundManager().playVoiceAfterSec(1, SoundID.VOICE_EXPLAIN_GAME_START);

        game.hud().creditVisible(true).scoreVisible(true).livesCounterVisible(false).levelCounterVisible(true);

        actionBindings.useAll(ArcadePacMan_UIConfig.DEFAULT_BINDINGS);
        actionBindings.useAll(GameUI.SCENE_TESTS_BINDINGS);

        blinking = new Pulse(10, Pulse.State.ON);

        pacMan = ArcadePacMan_ActorFactory.createPacMan();
        pacMan.setAnimationManager(uiConfig.createPacAnimations());
        pacMan.selectAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);

        ghosts = List.of(
            uiConfig.createAnimatedGhost(RED_GHOST_SHADOW),
            uiConfig.createAnimatedGhost(PINK_GHOST_SPEEDY),
            uiConfig.createAnimatedGhost(CYAN_GHOST_BASHFUL),
            uiConfig.createAnimatedGhost(ORANGE_GHOST_POKEY)
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
    }

    @Override
    public void update(Game game) {
        sceneController.update();
    }

    @Override
    public void onCreditAdded(GameEvent e) {
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
                    scene.sceneController.enterState(SHOWING_PELLET_POINTS);
                }
            }
        },

        SHOWING_PELLET_POINTS {
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
                scene.pacMan.playAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
                scene.ghosts.forEach(ghost -> {
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setPosition(scene.pacMan.x() + 16 * (ghost.personality() + 1), scene.pacMan.y());
                    ghost.setMoveDir(Direction.LEFT);
                    ghost.setWishDir(Direction.LEFT);
                    ghost.setSpeed(CHASING_SPEED);
                    ghost.show();
                    ghost.playAnimation(CommonAnimationID.ANIM_GHOST_NORMAL);
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
                    scene.pacMan.playAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
                    scene.pacMan.setSpeed(CHASING_SPEED);
                } else if (timer.tickCount() == 240) {
                    scene.sceneController.enterState(CHASING_GHOSTS);
                }
                scene.blinking.tick();
                scene.pacMan.move();
                scene.ghosts.forEach(Ghost::move);

                // wobbling
                float delta = 0.5f;
                final Ghost pinky = scene.ghosts.get(PINK_GHOST_SPEEDY);
                final Ghost inky = scene.ghosts.get(CYAN_GHOST_BASHFUL);
                int frame = (int) (timer.tickCount() % 6);
                if (frame == 2) {
                    pinky.setX(pinky.x() + delta);
                    inky.setX(inky.x() - delta);
                } else if (frame == 5) {
                    pinky.setX(pinky.x() - delta);
                    inky.setX(inky.x() + delta);
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
                        victim.selectAnimationAt(CommonAnimationID.ANIM_GHOST_NUMBER, scene.victims.size() - 1);
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
                            ghost.playAnimation(CommonAnimationID.ANIM_GHOST_FRIGHTENED);
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
                final Game game = scene.context().currentGame();
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