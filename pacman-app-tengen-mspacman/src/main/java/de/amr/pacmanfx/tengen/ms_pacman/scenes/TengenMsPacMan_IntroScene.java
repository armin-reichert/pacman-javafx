/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SceneRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.HUDRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.ACTION_ENTER_START_SCREEN;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Properties.PROPERTY_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.*;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createGhost;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createMsPacMan;

public class TengenMsPacMan_IntroScene extends GameScene2D {

    // Anchor point for everything
    private static final int MARQUEE_X = 60, MARQUEE_Y = 64;
    private static final int ACTOR_Y = MARQUEE_Y + 72;
    private static final int GHOST_STOP_X = MARQUEE_X - 18;
    private static final int MS_PAC_MAN_STOP_X = MARQUEE_X + 62;
    private static final float SPEED = 2.2f; //TODO check exact speed

    private final StateMachine<SceneState, TengenMsPacMan_IntroScene> sceneController;

    private ActorRenderer actorRenderer;

    private TengenMsPacMan_SpriteSheet spriteSheet;
    private Color[] ghostColors;

    private Marquee marquee;
    private Actor presentsText;
    private Pac msPacMan;
    private List<Ghost> ghosts;
    private int ghostIndex;
    private int waitBeforeRising;
    private boolean dark;

    public TengenMsPacMan_IntroScene(GameUI ui) {
        super(ui);
        sceneController = new StateMachine<>(List.of(SceneState.values())) {
            @Override
            public TengenMsPacMan_IntroScene context() {
                return TengenMsPacMan_IntroScene.this;
            }
        };
    }

    @Override
    public void createRenderers(Canvas canvas) {
        super.createRenderers(canvas);

        final GameUI_Config uiConfig = ui.currentConfig();
        sceneRenderer = configureRenderer(new TengenMsPacMan_SceneRenderer(canvas, uiConfig));
        actorRenderer  = configureRenderer(uiConfig.createActorRenderer(canvas));
    }

    @Override
    protected HUDRenderer hudRenderer() {
        return null;
    }

    @Override
    public void doInit() {
        final GameUI_Config uiConfig = ui.currentConfig();
        spriteSheet = (TengenMsPacMan_SpriteSheet) uiConfig.spriteSheet();

        var tengenActionBindings = ui.<TengenMsPacMan_UIConfig>currentConfig().actionBindings();
        actionBindings.assign(ACTION_ENTER_START_SCREEN, tengenActionBindings);
        actionBindings.assign(ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY, tengenActionBindings);

        ghostColors = Stream.of(RED_GHOST_SHADOW, PINK_GHOST_SPEEDY, CYAN_GHOST_BASHFUL, ORANGE_GHOST_POKEY)
                .map(personality -> uiConfig.assets().color("ghost.%d.color.normal.dress".formatted(personality)))
                .toArray(Color[]::new);

        marquee = new Marquee();
        marquee.setPosition(MARQUEE_X, MARQUEE_Y);
        marquee.scalingProperty().bind(scaling);

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
    public Vector2f sizeInPx() { return NES_SIZE_PX; }

    @Override
    public void drawSceneContent() {
        long tick = sceneController.state().timer().tickCount();
        sceneRenderer.ctx().setFont(sceneRenderer.arcadeFontTS());
        sceneRenderer.ctx().setImageSmoothing(false);
        switch (sceneController.state()) {
            case WAITING_FOR_START -> {
                if (!dark) {
                    boolean showPressStart = tick % 60 < 30;
                    sceneRenderer.fillText("TENGEN PRESENTS", blueShadedColor(tick), presentsText.x(), presentsText.y());
                    sceneRenderer.drawSprite(spriteSheet.sprite(SpriteID.LARGE_MS_PAC_MAN_TEXT), 6 * TS, MARQUEE_Y, true);
                    if (showPressStart) sceneRenderer.fillText("PRESS START", nesColor(0x20), 11 * TS, MARQUEE_Y + 9 * TS);
                    sceneRenderer.fillText("MS PAC-MAN TM NAMCO LTD", nesColor(0x25), 6 * TS, MARQUEE_Y + 15 * TS);
                    sceneRenderer.fillText("©1990 TENGEN INC",        nesColor(0x25), 8 * TS, MARQUEE_Y + 16 * TS);
                    sceneRenderer.fillText("ALL RIGHTS RESERVED",     nesColor(0x25), 7 * TS, MARQUEE_Y + 17 * TS);
                }
            }
            case SHOWING_MARQUEE -> {
                marquee.draw(sceneRenderer.ctx());
                sceneRenderer.fillText("\"MS PAC-MAN\"", nesColor(0x28), MARQUEE_X + 20, MARQUEE_Y - 18);
            }
            case GHOSTS_MARCHING_IN -> {
                marquee.draw(sceneRenderer.ctx());
                sceneRenderer.fillText("\"MS PAC-MAN\"", nesColor(0x28), MARQUEE_X + 20, MARQUEE_Y - 18);
                if (ghostIndex == 0) {
                    sceneRenderer.fillText("WITH", nesColor(0x20), MARQUEE_X + 12, MARQUEE_Y + 23);
                }
                Ghost currentGhost = ghosts.get(ghostIndex);
                Color ghostColor = ghostColors[currentGhost.id().personality()];
                sceneRenderer.fillText(currentGhost.name().toUpperCase(), ghostColor, MARQUEE_X + 44, MARQUEE_Y + 41);
                ghosts.forEach(ghost -> actorRenderer.drawActor(ghost));
            }
            case MS_PACMAN_MARCHING_IN -> {
                marquee.draw(sceneRenderer.ctx());
                sceneRenderer.fillText("\"MS PAC-MAN\"", nesColor(0x28), MARQUEE_X + 20, MARQUEE_Y - 18);
                sceneRenderer.fillText("STARRING", nesColor(0x20), MARQUEE_X + 12, MARQUEE_Y + 22);
                sceneRenderer.fillText("MS PAC-MAN", nesColor(0x28), MARQUEE_X + 28, MARQUEE_Y + 38);
                ghosts.forEach(ghost -> actorRenderer.drawActor(ghost));
                actorRenderer.drawActor(msPacMan);
            }
        }

        if (PROPERTY_JOYPAD_BINDINGS_DISPLAYED.get() && sceneRenderer instanceof TengenMsPacMan_SceneRenderer tengenSceneRenderer) {
            tengenSceneRenderer.drawJoypadKeyBinding(ui.joypad().currentKeyBinding());
        }
    }

    private enum SceneState implements FsmState<TengenMsPacMan_IntroScene> {

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

                scene.msPacMan = createMsPacMan();
                scene.msPacMan.setPosition(TS * 33, ACTOR_Y);
                scene.msPacMan.setMoveDir(Direction.LEFT);
                scene.msPacMan.setSpeed(SPEED);
                scene.msPacMan.setVisible(true);

                scene.ghosts = List.of(
                    createGhost(RED_GHOST_SHADOW),
                    createGhost(CYAN_GHOST_BASHFUL),
                    createGhost(PINK_GHOST_SPEEDY),
                    createGhost(ORANGE_GHOST_POKEY)
                );
                for (Ghost ghost : scene.ghosts) {
                    ghost.setPosition(TS * 33, ACTOR_Y);
                    ghost.setMoveDir(Direction.LEFT);
                    ghost.setWishDir(Direction.LEFT);
                    ghost.setSpeed(SPEED);
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setVisible(true);
                }
                scene.ghostIndex = 0;
                scene.msPacMan.setAnimations(scene.ui.currentConfig().createPacAnimations(scene.msPacMan));
                scene.msPacMan.playAnimation(ANIM_PAC_MUNCHING);
                for (Ghost ghost : scene.ghosts) {
                    ghost.setAnimations(scene.ui.currentConfig().createGhostAnimations(ghost));
                    ghost.playAnimation(ANIM_GHOST_NORMAL);
                }
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene scene) {
                scene.marquee.update(scene.context().gameState().timer().tickCount());
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
                scene.marquee.update(scene.context().gameState().timer().tickCount());
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
                scene.marquee.update(scene.context().gameState().timer().tickCount());
                Logger.debug("Tick {}: {} marching in", scene.ui.clock().tickCount(), scene.msPacMan.name());
                scene.msPacMan.move();
                if (scene.msPacMan.x() <= MS_PAC_MAN_STOP_X) {
                    scene.msPacMan.setSpeed(0);
                    scene.msPacMan.animations().ifPresent(AnimationManager::reset);
                }
                if (timer.atSecond(8)) {
                    // start demo level or show options
                    var game = scene.context().<TengenMsPacMan_GameModel>game();
                    if (game.optionsAreInitial()) {
                        game.setCanStartNewGame(false); // TODO check this
                        scene.context().gameController().restart(GamePlayState.STARTING_GAME_OR_LEVEL);
                    } else {
                        scene.context().gameController().changeGameState(GamePlayState.SETTING_OPTIONS_FOR_START);
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