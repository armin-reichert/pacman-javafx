/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameController.GameState;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.actors.MsPacMan;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.GameUI_Config;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions.ACTION_ENTER_START_SCREEN;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions.ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;

public class TengenMsPacMan_IntroScene extends GameScene2D {

    // Anchor point for everything
    public static final int MARQUEE_X = 60, MARQUEE_Y = 64;
    public static final int ACTOR_Y = MARQUEE_Y + 72;
    public static final int GHOST_STOP_X = MARQUEE_X - 18;
    public static final int MS_PAC_MAN_STOP_X = MARQUEE_X + 62;
    public static final float SPEED = 2.2f; //TODO check exact speed

    public final StateMachine<TengenMsPacMan_IntroScene> sceneController;

    public TengenMsPacMan_SpriteSheet spriteSheet;

    public Color[] ghostColors;

    public Marquee marquee;
    public Actor presentsText;
    public Pac msPacMan;
    public List<Ghost> ghosts;
    public int ghostIndex;
    private int waitBeforeRising;
    public boolean dark;

    public TengenMsPacMan_IntroScene() {
        sceneController = new StateMachine<>(this, List.of(SceneState.values()));
    }

    @Override
    public void doInit(Game game) {
        game.hud().hide();

        final GameUI_Config uiConfig = ui.currentConfig();
        spriteSheet = (TengenMsPacMan_SpriteSheet) uiConfig.spriteSheet();

        actionBindings.useAnyBinding(ACTION_ENTER_START_SCREEN,             TengenMsPacMan_UIConfig.ACTION_BINDINGS);
        actionBindings.useAnyBinding(ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY, TengenMsPacMan_UIConfig.ACTION_BINDINGS);

        ghostColors = Stream.of(RED_GHOST_SHADOW, PINK_GHOST_SPEEDY, CYAN_GHOST_BASHFUL, ORANGE_GHOST_POKEY)
                .map(personality -> uiConfig.assets().color("ghost.%d.color.normal.dress".formatted(personality)))
                .toArray(Color[]::new);

        marquee = new Marquee();
        marquee.setPosition(MARQUEE_X, MARQUEE_Y);
        marquee.scalingProperty().bind(scalingProperty());

        presentsText = new Actor();
        presentsText.setPosition(9 * TS, MARQUEE_Y - TS);

        sceneController.restart(SceneState.WAITING_FOR_START);
    }

    @Override
    protected void doEnd(Game game) {}

    @Override
    public void update(Game game) {
        if (!ui.clock().isPaused()) {
            sceneController.update();
        }
    }

    @Override
    public Vector2i unscaledSize() { return NES_SIZE_PX; }

    public enum SceneState implements StateMachine.State<TengenMsPacMan_IntroScene> {

        WAITING_FOR_START {

            @Override
            public void onEnter(TengenMsPacMan_IntroScene scene) {
                timer.restartTicks(TickTimer.INDEFINITE);
                scene.dark = false;
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene scene) {
                if (timer.atSecond(7.8)) {
                    scene.dark = true;
                } else if (timer.atSecond(9)) {
                    scene.dark = false;
                    scene.sceneController.enterState(SHOWING_MARQUEE);
                }
            }
        },

        SHOWING_MARQUEE {
            @Override
            public void onEnter(TengenMsPacMan_IntroScene scene) {
                timer.restartTicks(TickTimer.INDEFINITE);

                GameUI_Config uiConfig = scene.ui.currentConfig();

                scene.msPacMan = new MsPacMan();
                scene.msPacMan.setAnimationManager(scene.ui.currentConfig().createPacAnimations());
                scene.msPacMan.playAnimation(Pac.AnimationID.PAC_MUNCHING);

                scene.msPacMan.setPosition(TS * 33, ACTOR_Y);
                scene.msPacMan.setMoveDir(Direction.LEFT);
                scene.msPacMan.setSpeed(SPEED);
                scene.msPacMan.setVisible(true);

                scene.ghosts = List.of(
                    uiConfig.createGhostWithAnimations(RED_GHOST_SHADOW),
                    uiConfig.createGhostWithAnimations(CYAN_GHOST_BASHFUL),
                    uiConfig.createGhostWithAnimations(PINK_GHOST_SPEEDY),
                    uiConfig.createGhostWithAnimations(ORANGE_GHOST_POKEY)
                );
                for (Ghost ghost : scene.ghosts) {
                    ghost.setPosition(TS * 33, ACTOR_Y);
                    ghost.setMoveDir(Direction.LEFT);
                    ghost.setWishDir(Direction.LEFT);
                    ghost.setSpeed(SPEED);
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setVisible(true);
                    ghost.playAnimation();
                }
                scene.ghostIndex = 0;
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene scene) {
                scene.marquee.update(scene.context().currentGame().control().state().timer().tickCount());
                if (timer.atSecond(1)) {
                    scene.sceneController.enterState(GHOSTS_MARCHING_IN);
                }
            }
        },

        GHOSTS_MARCHING_IN {
            @Override
            public void onEnter(TengenMsPacMan_IntroScene scene) {
                timer.restartTicks(TickTimer.INDEFINITE);
                scene.waitBeforeRising = 0;
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene scene) {
                long tick = scene.context().currentGame().control().state().timer().tickCount();
                scene.marquee.update(tick);
                boolean reachedEndPosition = letGhostMarchIn(scene);
                if (reachedEndPosition) {
                    if (scene.ghostIndex == 3) {
                        scene.sceneController.enterState(MS_PACMAN_MARCHING_IN);
                    } else {
                        ++scene.ghostIndex;
                    }
                }
            }

            boolean letGhostMarchIn(TengenMsPacMan_IntroScene scene) {
                Ghost ghost = scene.ghosts.get(scene.ghostIndex);
                Logger.debug("Tick {}: {} marching in", scene.ui.clock().tickCount(), ghost.name());
                if (ghost.moveDir() == Direction.LEFT) {
                    if (ghost.x() <= GHOST_STOP_X) {
                        ghost.setX(GHOST_STOP_X);
                        ghost.setMoveDir(Direction.UP);
                        ghost.setWishDir(Direction.UP);
                        scene.waitBeforeRising = 2;
                    } else {
                        ghost.move();
                        Logger.debug("{} moves {} x={}", ghost.name(), ghost.moveDir(), ghost.x());
                    }
                }
                else if (ghost.moveDir() == Direction.UP) {
                    int endPositionY = MARQUEE_Y + scene.ghostIndex * 16;
                    if (scene.waitBeforeRising > 0) {
                        scene.waitBeforeRising--;
                    }
                    else if (ghost.y() <= endPositionY) {
                        ghost.setSpeed(0);
                        ghost.setMoveDir(Direction.RIGHT);
                        ghost.setWishDir(Direction.RIGHT);
                        return true;
                    }
                    else {
                        ghost.move();
                        Logger.debug("{} moves {}", ghost.name(), ghost.moveDir());
                    }
                }
                return false;
            }
        },

        MS_PACMAN_MARCHING_IN {
            @Override
            public void onEnter(TengenMsPacMan_IntroScene scene) {
                timer.restartTicks(TickTimer.INDEFINITE);
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene scene) {
                long tick = scene.context().currentGame().control().state().timer().tickCount();
                scene.marquee.update(tick);
                Logger.debug("Tick {}: {} marching in", scene.ui.clock().tickCount(), scene.msPacMan.name());
                scene.msPacMan.move();
                if (scene.msPacMan.x() <= MS_PAC_MAN_STOP_X) {
                    scene.msPacMan.setSpeed(0);
                    scene.msPacMan.optAnimationManager().ifPresent(AnimationManager::reset);
                }
                if (timer.atSecond(8)) {
                    // start demo level or show options
                    TengenMsPacMan_GameModel game = scene.context().currentGame();
                    if (game.allOptionsDefault()) {
                        game.setCanStartNewGame(false); // TODO check this
                        game.control().restart(GameState.STARTING_GAME_OR_LEVEL);
                    } else {
                        game.control().enterState(GameState.SETTING_OPTIONS_FOR_START);
                    }
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