/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameController.GameState;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.model.actors.MsPacMan;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_IntroScene_Renderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui._2d.HUD_Renderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.ACTION_ENTER_START_SCREEN;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.ui._2d.GameScene2D_Renderer.configureRendererForGameScene;

public class TengenMsPacMan_IntroScene extends GameScene2D {

    // Anchor point for everything
    public static final int MARQUEE_X = 60, MARQUEE_Y = 64;
    public static final int ACTOR_Y = MARQUEE_Y + 72;
    public static final int GHOST_STOP_X = MARQUEE_X - 18;
    public static final int MS_PAC_MAN_STOP_X = MARQUEE_X + 62;
    public static final float SPEED = 2.2f; //TODO check exact speed

    public final StateMachine<SceneState, TengenMsPacMan_IntroScene> sceneController;

    public TengenMsPacMan_SpriteSheet spriteSheet;

    private TengenMsPacMan_IntroScene_Renderer sceneRenderer;

    public Color[] ghostColors;

    public Marquee marquee;
    public Actor presentsText;
    public Pac msPacMan;
    public List<Ghost> ghosts;
    public int ghostIndex;
    private int waitBeforeRising;
    public boolean dark;

    public TengenMsPacMan_IntroScene(GameUI ui) {
        super(ui);
        sceneController = new StateMachine<>();
        sceneController.setContext(this);
        sceneController.addStates(SceneState.values());
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        final GameUI_Config uiConfig = ui.currentConfig();

        sceneRenderer = configureRendererForGameScene(
            new TengenMsPacMan_IntroScene_Renderer(this, canvas, (TengenMsPacMan_SpriteSheet) uiConfig.spriteSheet()), this);
    }

    @Override
    public HUD_Renderer hudRenderer() {
        return null;
    }

    @Override
    public TengenMsPacMan_IntroScene_Renderer sceneRenderer() {
        return sceneRenderer;
    }

    @Override
    public void doInit() {
        final GameUI_Config uiConfig = ui.currentConfig();
        spriteSheet = (TengenMsPacMan_SpriteSheet) uiConfig.spriteSheet();

        var tengenActionBindings = ui.<TengenMsPacMan_UIConfig>currentConfig().tengenActionBindings();
        actionBindings.bind(ACTION_ENTER_START_SCREEN, tengenActionBindings);
        actionBindings.bind(ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY, tengenActionBindings);

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
    protected void doEnd() {}

    @Override
    public void update() {
        if (!ui.clock().isPaused()) {
            sceneController.update();
        }
    }

    @Override
    public Vector2i unscaledSize() { return NES_SIZE_PX; }

    public enum SceneState implements FsmState<TengenMsPacMan_IntroScene> {

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
                    scene.sceneController.changeState(SHOWING_MARQUEE);
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
                scene.msPacMan.playAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);

                scene.msPacMan.setPosition(TS * 33, ACTOR_Y);
                scene.msPacMan.setMoveDir(Direction.LEFT);
                scene.msPacMan.setSpeed(SPEED);
                scene.msPacMan.setVisible(true);

                scene.ghosts = List.of(
                    uiConfig.createAnimatedGhost(RED_GHOST_SHADOW),
                    uiConfig.createAnimatedGhost(CYAN_GHOST_BASHFUL),
                    uiConfig.createAnimatedGhost(PINK_GHOST_SPEEDY),
                    uiConfig.createAnimatedGhost(ORANGE_GHOST_POKEY)
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
                    scene.sceneController.changeState(GHOSTS_MARCHING_IN);
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
                        scene.sceneController.changeState(MS_PACMAN_MARCHING_IN);
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
                    if (game.allOptionsHaveDefaultValue()) {
                        game.setCanStartNewGame(false); // TODO check this
                        game.control().restart(GameState.STARTING_GAME_OR_LEVEL);
                    } else {
                        game.control().changeState(GameState.SETTING_OPTIONS_FOR_START);
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