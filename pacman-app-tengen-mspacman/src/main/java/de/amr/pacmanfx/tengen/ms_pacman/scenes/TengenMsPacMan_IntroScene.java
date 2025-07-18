/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.BitSet;
import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesPaletteColor;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createGhost;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createMsPacMan;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer.blueShadedColor;

public class TengenMsPacMan_IntroScene extends GameScene2D {

    // Anchor point for everything
    private static final int MARQUEE_X = 60, MARQUEE_Y = 64;
    private static final int ACTOR_Y = MARQUEE_Y + 72;
    private static final int GHOST_STOP_X = MARQUEE_X - 18;
    private static final int MS_PAC_MAN_STOP_X = MARQUEE_X + 62;
    private static final int NUM_BULBS = 96;
    private static final float SPEED = 2.2f; //TODO check exact speed

    private final StateMachine<SceneState, TengenMsPacMan_IntroScene> sceneController;

    private TengenMsPacMan_SpriteSheet spriteSheet;
    private long marqueeTick;
    private final BitSet marqueeState = new BitSet(NUM_BULBS);
    private Actor presentsText;
    private Pac msPacMan;
    private List<Ghost> ghosts;
    private int ghostIndex;
    private int waitBeforeRising;
    private boolean dark;

    public TengenMsPacMan_IntroScene(GameUI ui) {
        super(ui);
        sceneController = new StateMachine<>(SceneState.values()) {
            @Override
            public TengenMsPacMan_IntroScene context() {
                return TengenMsPacMan_IntroScene.this;
            }
        };
    }

    @Override
    public void doInit() {
        gameContext().theGame().theHUD().showScore(false);
        gameContext().theGame().theHUD().showLevelCounter(false);
        gameContext().theGame().theHUD().showLivesCounter(false);

        spriteSheet = (TengenMsPacMan_SpriteSheet) ui.theConfiguration().spriteSheet();

        var config = ui.<TengenMsPacMan_UIConfig>theConfiguration();
        actionBindings.bind(config.ACTION_ENTER_START_SCREEN, config.TENGEN_MS_PACMAN_ACTION_BINDINGS);
        actionBindings.bind(config.ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAYED, config.TENGEN_MS_PACMAN_ACTION_BINDINGS);

        presentsText = new Actor(null);
        presentsText.setPosition(9 * TS, MARQUEE_Y - TS);
        sceneController.restart(SceneState.WAITING_FOR_START);
    }

    @Override
    protected void doEnd() {
    }

    @Override
    public void update() {
        if (!ui.theGameClock().isPaused()) {
            sceneController.update();
        }
    }

    @Override
    public Vector2f sizeInPx() { return NES_SIZE_PX; }

    @SuppressWarnings("unchecked")
    @Override
    public TengenMsPacMan_GameRenderer renderer() {
        return (TengenMsPacMan_GameRenderer) gameRenderer;
    }

    @Override
    public void drawSceneContent() {
        ctx().setFont(scaledArcadeFont8());
        renderer().drawVerticalSceneBorders();
        TickTimer timer = sceneController.state().timer;
        long tick = timer.tickCount();
        switch (sceneController.state()) {
            case WAITING_FOR_START -> {
                if (!dark) {
                    renderer().fillTextAtScaledPosition("TENGEN PRESENTS", blueShadedColor(tick), presentsText.x(), presentsText.y());
                    renderer().drawSpriteScaled(spriteSheet.sprite(SpriteID.TITLE_TEXT), 6 * TS, MARQUEE_Y);
                    if (tick % 60 < 30) {
                        renderer().fillTextAtScaledPosition("PRESS START", nesPaletteColor(0x20), 11 * TS, MARQUEE_Y + 9 * TS);
                    }
                    renderer().fillTextAtScaledPosition("MS PAC-MAN TM NAMCO LTD", nesPaletteColor(0x25), 6 * TS, MARQUEE_Y + 15 * TS);
                    renderer().fillTextAtScaledPosition("©1990 TENGEN INC",        nesPaletteColor(0x25), 8 * TS, MARQUEE_Y + 16 * TS);
                    renderer().fillTextAtScaledPosition("ALL RIGHTS RESERVED",     nesPaletteColor(0x25), 7 * TS, MARQUEE_Y + 17 * TS);
                }
            }
            case SHOWING_MARQUEE -> {
                drawMarquee();
                renderer().fillTextAtScaledPosition("\"MS PAC-MAN\"", nesPaletteColor(0x28), MARQUEE_X + 20, MARQUEE_Y - 18);
            }
            case GHOSTS_MARCHING_IN -> {
                drawMarquee();
                renderer().fillTextAtScaledPosition("\"MS PAC-MAN\"", nesPaletteColor(0x28), MARQUEE_X + 20, MARQUEE_Y - 18);
                if (ghostIndex == 0) {
                    renderer().fillTextAtScaledPosition("WITH", nesPaletteColor(0x20), MARQUEE_X + 12, MARQUEE_Y + 23);
                }
                Ghost currentGhost = ghosts.get(ghostIndex);
                Color ghostColor = ui.theAssets().color("tengen.ghost.%d.color.normal.dress".formatted(currentGhost.personality()));
                renderer().fillTextAtScaledPosition(currentGhost.name().toUpperCase(), ghostColor, MARQUEE_X + 44, MARQUEE_Y + 41);
                renderer().drawActors(ghosts);
            }
            case MS_PACMAN_MARCHING_IN -> {
                drawMarquee();
                renderer().fillTextAtScaledPosition("\"MS PAC-MAN\"", nesPaletteColor(0x28), MARQUEE_X + 20, MARQUEE_Y - 18);
                renderer().fillTextAtScaledPosition("STARRING", nesPaletteColor(0x20), MARQUEE_X + 12, MARQUEE_Y + 22);
                renderer().fillTextAtScaledPosition("MS PAC-MAN", nesPaletteColor(0x28), MARQUEE_X + 28, MARQUEE_Y + 38);
                renderer().drawActors(ghosts);
                renderer().drawActor(msPacMan);
            }
        }

        var config = (TengenMsPacMan_UIConfig) ui.theConfiguration();
        if (config.propertyJoypadBindingsDisplayed.get()) {
            renderer().drawJoypadKeyBinding(ui.theJoypad().currentKeyBinding());
        }
    }

    private void updateMarqueeState() {
        long t = sceneController.state().timer().tickCount();
        if (t % 4 == 0) {
            marqueeTick += 2;
            marqueeState.clear();
            for (int b = 0; b < 6; ++b) {
                marqueeState.set((int) (b * 16 + marqueeTick) % NUM_BULBS);
            }
        }
    }

    private void drawMarquee() {
        double xMin = MARQUEE_X, xMax = xMin + 132, yMin = MARQUEE_Y, yMax = yMin + 60;
        for (int i = 0; i < NUM_BULBS; ++i) {
            ctx().setFill(marqueeState.get(i) ? nesPaletteColor(0x20) : nesPaletteColor(0x15));
            if (i <= 33) { // lower border left-to-right
                drawBulb(xMin + 4 * i, yMax);
            } else if (i <= 48) { // right border bottom-to-top
                drawBulb(xMax, yMax - 4 * (i - 33));
            } else if (i <= 81) { // upper border right-to-left
                drawBulb(xMax - 4 * (i - 48), yMin);
            } else { // left border top-to-bottom
                drawBulb(xMin, yMin + 4 * (i - 81));
            }
        }
    }

    private void drawBulb(double x, double y) {
        ctx().fillRect(scaled(x), scaled(y), scaled(2), scaled(2));
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

                scene.msPacMan = createMsPacMan(null);
                scene.msPacMan.setPosition(TS * 33, ACTOR_Y);
                scene.msPacMan.setMoveDir(Direction.LEFT);
                scene.msPacMan.setSpeed(SPEED);
                scene.msPacMan.setVisible(true);

                scene.ghosts = List.of(
                    createGhost(null, RED_GHOST_SHADOW),
                    createGhost(null, CYAN_GHOST_BASHFUL),
                    createGhost(null, PINK_GHOST_SPEEDY),
                    createGhost(null, ORANGE_GHOST_POKEY)
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
                scene.msPacMan.setAnimations(scene.ui.theConfiguration().createPacAnimations(scene.msPacMan));
                scene.msPacMan.playAnimation(ANIM_PAC_MUNCHING);
                for (Ghost ghost : scene.ghosts) {
                    ghost.setAnimations(scene.ui.theConfiguration().createGhostAnimations(ghost));
                    ghost.playAnimation(ANIM_GHOST_NORMAL);
                }
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene scene) {
                scene.updateMarqueeState();
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
                scene.updateMarqueeState();
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
                Logger.debug("Tick {}: {} marching in", scene.ui.theGameClock().tickCount(), ghost.name());
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
                scene.updateMarqueeState();
                Logger.debug("Tick {}: {} marching in", scene.ui.theGameClock().tickCount(), scene.msPacMan.name());
                scene.msPacMan.move();
                if (scene.msPacMan.x() <= MS_PAC_MAN_STOP_X) {
                    scene.msPacMan.setSpeed(0);
                    scene.msPacMan.resetAnimation();
                }
                if (timer.atSecond(8)) {
                    // start demo level or show options
                    var game = scene.gameContext().<TengenMsPacMan_GameModel>theGame();
                    if (game.optionsAreInitial()) {
                        game.setCanStartNewGame(false); // TODO check this
                        scene.gameContext().theGameController().restart(GameState.STARTING_GAME);
                    } else {
                        scene.gameContext().theGameController().changeGameState(GameState.SETTING_OPTIONS_FOR_START);
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