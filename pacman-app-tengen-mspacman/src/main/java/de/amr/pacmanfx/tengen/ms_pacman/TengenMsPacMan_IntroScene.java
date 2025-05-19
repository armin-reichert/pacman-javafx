/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.fsm.FiniteStateMachine;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.BitSet;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_ANY_PAC_MUNCHING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_GameModel.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_SpriteSheet.MS_PAC_MAN_TITLE_SPRITE;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.*;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;
import static de.amr.pacmanfx.uilib.input.Keyboard.naked;

/**
 * @author Armin Reichert
 */
public class TengenMsPacMan_IntroScene extends GameScene2D {

    // Anchor point for everything
    private static final int MARQUEE_X = 60, MARQUEE_Y = 64;
    private static final int ACTOR_Y = MARQUEE_Y + 72;
    private static final int GHOST_STOP_X = MARQUEE_X - 18;
    private static final int MS_PAC_MAN_STOP_X = MARQUEE_X + 62;
    private static final int NUM_BULBS = 96;
    private static final float SPEED = 2.2f; //TODO check exact speed

    private final FiniteStateMachine<SceneState, TengenMsPacMan_IntroScene> sceneController;

    private long marqueeTick;
    private final BitSet marqueeState = new BitSet(NUM_BULBS);
    private Pac msPacMan;
    private Ghost[] ghosts;
    private int ghostIndex;
    private int waitBeforeRising;
    private boolean dark;

    public TengenMsPacMan_IntroScene() {
        sceneController = new FiniteStateMachine<>(SceneState.values()) {
            @Override
            public TengenMsPacMan_IntroScene context() {
                return TengenMsPacMan_IntroScene.this;
            }
        };
    }

    @Override
    public void bindActions() {
        bind(TengenMsPacMan_GameAction.START_GAME, theJoypad().key(JoypadButton.START));
        bind(TengenMsPacMan_GameAction.TOGGLE_JOYPAD_BINDINGS_DISPLAYED, naked(KeyCode.SPACE));
    }

    @Override
    public void doInit() {
        theGame().scoreManager().setScoreVisible(false);
        sceneController.restart(SceneState.WAITING_FOR_START);
    }

    @Override
    protected void doEnd() {
        theSound().stopVoice();
    }

    @Override
    public void update() {
        if (!theClock().isPaused()) {
            sceneController.update();
        }
    }

    @Override
    public Vector2f sizeInPx() {
        return NES_SIZE.toVector2f();
    }

    @Override
    public void drawSceneContent() {
        final Font font = arcadeFontScaledTS();
        gr.fillCanvas(backgroundColor());
        gr.drawScores(theGame().scoreManager(), nesPaletteColor(0x20), font);
        TengenMsPacMan_Renderer2D r = (TengenMsPacMan_Renderer2D) gr;
        r.drawSceneBorderLines();
        TickTimer timer = sceneController.state().timer;
        long t = timer.tickCount();
        switch (sceneController.state()) {
            case WAITING_FOR_START -> {
                if (!dark) {
                    r.fillTextAtScaledPosition("TENGEN PRESENTS", r.shadeOfBlue(t), font, 9 * TS, MARQUEE_Y - TS);
                    r.drawSpriteScaled(MS_PAC_MAN_TITLE_SPRITE, 6 * TS, MARQUEE_Y);
                    if (t % 60 < 30) {
                        r.fillTextAtScaledPosition("PRESS START", nesPaletteColor(0x20), font, 11 * TS, MARQUEE_Y + 9 * TS);
                    }
                    r.fillTextAtScaledPosition("MS PAC-MAN TM NAMCO LTD", nesPaletteColor(0x25), font, 6 * TS, MARQUEE_Y + 15 * TS);
                    r.fillTextAtScaledPosition("©1990 TENGEN INC",        nesPaletteColor(0x25), font, 8 * TS, MARQUEE_Y + 16 * TS);
                    r.fillTextAtScaledPosition("ALL RIGHTS RESERVED",     nesPaletteColor(0x25), font, 7 * TS, MARQUEE_Y + 17 * TS);
                }
            }
            case SHOWING_MARQUEE -> {
                drawMarquee();
                r.fillTextAtScaledPosition("\"MS PAC-MAN\"", nesPaletteColor(0x28), font, MARQUEE_X + 20, MARQUEE_Y - 18);
            }
            case GHOSTS_MARCHING_IN -> {
                drawMarquee();
                r.fillTextAtScaledPosition("\"MS PAC-MAN\"", nesPaletteColor(0x28), font, MARQUEE_X + 20, MARQUEE_Y - 18);
                if (ghostIndex == 0) {
                    r.fillTextAtScaledPosition("WITH", nesPaletteColor(0x20), font, MARQUEE_X + 12, MARQUEE_Y + 23);
                }
                Ghost currentGhost = ghosts[ghostIndex];
                Color ghostColor = theAssets().color("tengen.ghost.%d.color.normal.dress".formatted(currentGhost.personality()));
                r.fillTextAtScaledPosition(currentGhost.name().toUpperCase(), ghostColor, font, MARQUEE_X + 44, MARQUEE_Y + 41);
                for (Ghost ghost : ghosts) { r.drawActor(ghost); }
            }
            case MS_PACMAN_MARCHING_IN -> {
                drawMarquee();
                r.fillTextAtScaledPosition("\"MS PAC-MAN\"", nesPaletteColor(0x28), font, MARQUEE_X + 20, MARQUEE_Y - 18);
                r.fillTextAtScaledPosition("STARRING", nesPaletteColor(0x20), font, MARQUEE_X + 12, MARQUEE_Y + 22);
                r.fillTextAtScaledPosition("MS PAC-MAN", nesPaletteColor(0x28), font, MARQUEE_X + 28, MARQUEE_Y + 38);
                for (Ghost ghost : ghosts) { r.drawActor(ghost); }
                r.drawActor(msPacMan);
            }
        }

        if (PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED.get()) {
            r.drawJoypadKeyBinding(theJoypad().currentKeyBinding());
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
            gr.ctx().setFill(marqueeState.get(i) ? nesPaletteColor(0x20) : nesPaletteColor(0x15));
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
        gr.ctx().fillRect(scaled(x), scaled(y), scaled(2), scaled(2));
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

                scene.ghosts = new Ghost[] {
                    createRedGhost(),
                    createCyanGhost(),
                    createPinkGhost(),
                    createOrangeGhost()
                };
                for (Ghost ghost : scene.ghosts) {
                    ghost.setPosition(TS * 33, ACTOR_Y);
                    ghost.setMoveAndWishDir(Direction.LEFT);
                    ghost.setSpeed(SPEED);
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setVisible(true);
                }
                scene.ghostIndex = 0;

                var spriteSheet = (TengenMsPacMan_SpriteSheet) theUIConfig().current().spriteSheet();
                scene.msPacMan.setAnimations(new TengenMsPacMan_PacAnimationSet(spriteSheet));
                scene.msPacMan.selectAnimation(ANIM_ANY_PAC_MUNCHING);
                scene.msPacMan.startAnimation();

                for (Ghost ghost : scene.ghosts) {
                    ghost.setAnimations(new TengenMsPacMan_GhostAnimationSet(spriteSheet, ghost.personality()));
                    ghost.selectAnimation(ANIM_GHOST_NORMAL);
                    ghost.startAnimation();
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
                Ghost ghost = scene.ghosts[scene.ghostIndex];
                Logger.debug("Tick {}: {} marching in", theClock().tickCount(), ghost.name());
                if (ghost.moveDir() == Direction.LEFT) {
                    if (ghost.posX() <= GHOST_STOP_X) {
                        ghost.setPosX(GHOST_STOP_X);
                        ghost.setMoveAndWishDir(Direction.UP);
                        scene.waitBeforeRising = 2;
                    } else {
                        ghost.move();
                        Logger.debug("{} moves {} x={}", ghost.name(), ghost.moveDir(), ghost.posX());
                    }
                }
                else if (ghost.moveDir() == Direction.UP) {
                    int endPositionY = MARQUEE_Y + scene.ghostIndex * 16;
                    if (scene.waitBeforeRising > 0) {
                        scene.waitBeforeRising--;
                    }
                    else if (ghost.posY() <= endPositionY) {
                        ghost.setSpeed(0);
                        ghost.setMoveAndWishDir(Direction.RIGHT);
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
                Logger.debug("Tick {}: {} marching in", theClock().tickCount(), scene.msPacMan.name());
                scene.msPacMan.move();
                if (scene.msPacMan.posX() <= MS_PAC_MAN_STOP_X) {
                    scene.msPacMan.setSpeed(0);
                    scene.msPacMan.resetAnimation();
                }
                if (timer.atSecond(8)) {
                    // start demo level or show options
                    var tengenGame = (TengenMsPacMan_GameModel) theGame();
                    if (tengenGame.optionsAreInitial()) {
                        tengenGame.setCanStartNewGame(false); // TODO check this
                        theGameController().restart(GameState.STARTING_GAME);
                    } else {
                        theGameController().changeState(GameState.SETTING_OPTIONS);
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