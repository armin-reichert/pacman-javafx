/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman.scene;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.fsm.FiniteStateMachine;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.nes.NES_JoypadButtonID;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameModel;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_GhostAnimations;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_PacAnimations;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_Renderer2D;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_SpriteSheet;
import de.amr.games.pacman.ui._2d.GameActions2D;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.BitSet;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.model.actors.ActorAnimations.ANIM_GHOST_NORMAL;
import static de.amr.games.pacman.model.actors.ActorAnimations.ANIM_PAC_MUNCHING;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameActions.TOGGLE_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_UIConfig.*;
import static de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_SpriteSheet.MS_PAC_MAN_TITLE_SPRITE;
import static de.amr.games.pacman.ui.Globals.*;

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
    public void bindGameActions() {
        bind(GameActions2D.START_GAME, THE_UI.keyboard().currentJoypadKeyBinding().key(NES_JoypadButtonID.START));
        bind(TOGGLE_JOYPAD_BINDINGS_DISPLAYED, THE_UI.keyboard().currentJoypadKeyBinding().key(NES_JoypadButtonID.SELECT));
    }

    @Override
    public void doInit() {
        THE_UI.setScoreVisible(false);
        THE_UI.keyboard().enableCurrentJoypad();
        sceneController.restart(SceneState.WAITING_FOR_START);
    }

    @Override
    protected void doEnd() {
        THE_UI.sound().stopVoice();
        THE_UI.keyboard().disableCurrentJoypad();
    }

    @Override
    public void update() {
        sceneController.update();
    }

    @Override
    public Vector2f sizeInPx() {
        return NES_SIZE.toVector2f();
    }

    @Override
    public void drawSceneContent() {
        TengenMsPacMan_Renderer2D r = (TengenMsPacMan_Renderer2D) gr;
        r.drawSceneBorderLines();
        TickTimer timer = sceneController.state().timer;
        long t = timer.tickCount();
        Font scaledFont = r.scaledArcadeFont(8);
        switch (sceneController.state()) {
            case WAITING_FOR_START -> {
                if (!dark) {
                    r.drawText("TENGEN PRESENTS", r.shadeOfBlue(t), scaledFont, 9 * TS, MARQUEE_Y - TS);
                    r.drawSpriteScaled(MS_PAC_MAN_TITLE_SPRITE, 6 * TS, MARQUEE_Y);
                    if (t % 60 < 30) {
                        r.drawText("PRESS START", nesPaletteColor(0x20), scaledFont, 11 * TS, MARQUEE_Y + 9 * TS);
                    }
                    r.drawText("MS PAC-MAN TM NAMCO LTD", nesPaletteColor(0x25), scaledFont, 6 * TS, MARQUEE_Y + 15 * TS);
                    r.drawText("©1990 TENGEN INC",        nesPaletteColor(0x25), scaledFont, 8 * TS, MARQUEE_Y + 16 * TS);
                    r.drawText("ALL RIGHTS RESERVED",     nesPaletteColor(0x25), scaledFont, 7 * TS, MARQUEE_Y + 17 * TS);
                }
            }
            case SHOWING_MARQUEE -> {
                drawMarquee();
                r.drawText("\"MS PAC-MAN\"", nesPaletteColor(0x28), scaledFont, MARQUEE_X + 20, MARQUEE_Y - 18);
            }
            case GHOSTS_MARCHING_IN -> {
                drawMarquee();
                r.drawText("\"MS PAC-MAN\"", nesPaletteColor(0x28), scaledFont, MARQUEE_X + 20, MARQUEE_Y - 18);
                if (ghostIndex == 0) {
                    r.drawText("WITH", nesPaletteColor(0x20), scaledFont, MARQUEE_X + 12, MARQUEE_Y + 23);
                }
                Ghost currentGhost = ghosts[ghostIndex];
                Color ghostColor = THE_UI.assets().color("tengen.ghost.%d.color.normal.dress".formatted(currentGhost.id()));
                r.drawText(currentGhost.name().toUpperCase(), ghostColor, scaledFont, MARQUEE_X + 44, MARQUEE_Y + 41);
                for (Ghost ghost : ghosts) { r.drawAnimatedActor(ghost); }
            }
            case MS_PACMAN_MARCHING_IN -> {
                drawMarquee();
                r.drawText("\"MS PAC-MAN\"", nesPaletteColor(0x28), scaledFont, MARQUEE_X + 20, MARQUEE_Y - 18);
                r.drawText("STARRING", nesPaletteColor(0x20), scaledFont, MARQUEE_X + 12, MARQUEE_Y + 22);
                r.drawText("MS PAC-MAN", nesPaletteColor(0x28), scaledFont, MARQUEE_X + 28, MARQUEE_Y + 38);
                for (Ghost ghost : ghosts) { r.drawAnimatedActor(ghost); }
                r.drawAnimatedActor(msPacMan);
            }
        }

        if (PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED.get()) {
            r.drawJoypadKeyBinding(THE_UI.keyboard().currentJoypadKeyBinding());
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
            public void onEnter(TengenMsPacMan_IntroScene intro) {
                timer.restartTicks(TickTimer.INDEFINITE);
                intro.dark = false;
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene intro) {
                if (timer.atSecond(7.8)) {
                    intro.dark = true;
                } else if (timer.atSecond(9)) {
                    intro.dark = false;
                    intro.sceneController.changeState(SHOWING_MARQUEE);
                }
            }
        },

        SHOWING_MARQUEE {
            @Override
            public void onEnter(TengenMsPacMan_IntroScene intro) {
                timer.restartTicks(TickTimer.INDEFINITE);

                intro.msPacMan = new Pac();
                intro.msPacMan.setName("Ms. Pac-Man");
                intro.msPacMan.setPosition(TS * 33, ACTOR_Y);
                intro.msPacMan.setMoveDir(Direction.LEFT);
                intro.msPacMan.setSpeed(SPEED);
                intro.msPacMan.setVisible(true);

                intro.ghosts = new Ghost[] {
                    TengenMsPacMan_GameModel.blinky(),
                    TengenMsPacMan_GameModel.inky(),
                    TengenMsPacMan_GameModel.pinky(),
                    TengenMsPacMan_GameModel.sue()
                };
                for (Ghost ghost : intro.ghosts) {
                    ghost.setPosition(TS * 33, ACTOR_Y);
                    ghost.setMoveAndWishDir(Direction.LEFT);
                    ghost.setSpeed(SPEED);
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setVisible(true);
                }
                intro.ghostIndex = 0;

                var spriteSheet = (TengenMsPacMan_SpriteSheet) THE_UI.configurations().current().spriteSheet();
                intro.msPacMan.setAnimations(new TengenMsPacMan_PacAnimations(spriteSheet));
                intro.msPacMan.selectAnimation(ANIM_PAC_MUNCHING);
                intro.msPacMan.startAnimation();

                for (Ghost ghost : intro.ghosts) {
                    ghost.setAnimations(new TengenMsPacMan_GhostAnimations(spriteSheet, ghost.id()));
                    ghost.selectAnimation(ANIM_GHOST_NORMAL);
                    ghost.startAnimation();
                }
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene intro) {
                intro.updateMarqueeState();
                if (timer.atSecond(1)) {
                    intro.sceneController.changeState(GHOSTS_MARCHING_IN);
                }
            }
        },

        GHOSTS_MARCHING_IN {
            @Override
            public void onEnter(TengenMsPacMan_IntroScene intro) {
                timer.restartTicks(TickTimer.INDEFINITE);
                intro.waitBeforeRising = 0;
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene intro) {
                intro.updateMarqueeState();
                boolean reachedEndPosition = letGhostMarchIn(intro);
                if (reachedEndPosition) {
                    if (intro.ghostIndex == 3) {
                        intro.sceneController.changeState(MS_PACMAN_MARCHING_IN);
                    } else {
                        ++intro.ghostIndex;
                    }
                }
            }

            boolean letGhostMarchIn(TengenMsPacMan_IntroScene intro) {
                Ghost ghost = intro.ghosts[intro.ghostIndex];
                Logger.debug("Tick {}: {} marching in", THE_UI.clock().tickCount(), ghost.name());
                if (ghost.moveDir() == Direction.LEFT) {
                    if (ghost.posX() <= GHOST_STOP_X) {
                        ghost.setPosX(GHOST_STOP_X);
                        ghost.setMoveAndWishDir(Direction.UP);
                        intro.waitBeforeRising = 2;
                    } else {
                        ghost.move();
                        Logger.debug("{} moves {} x={}", ghost.name(), ghost.moveDir(), ghost.posX());
                    }
                }
                else if (ghost.moveDir() == Direction.UP) {
                    int endPositionY = MARQUEE_Y + intro.ghostIndex * 16;
                    if (intro.waitBeforeRising > 0) {
                        intro.waitBeforeRising--;
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
            public void onEnter(TengenMsPacMan_IntroScene context) {
                timer.restartTicks(TickTimer.INDEFINITE);
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene intro) {
                intro.updateMarqueeState();
                Logger.debug("Tick {}: {} marching in", THE_UI.clock().tickCount(), intro.msPacMan.name());
                intro.msPacMan.move();
                if (intro.msPacMan.posX() <= MS_PAC_MAN_STOP_X) {
                    intro.msPacMan.setSpeed(0);
                    intro.msPacMan.resetAnimation();
                }
                if (timer.atSecond(7)) {
                    // start demo level or show options
                    TengenMsPacMan_GameModel game = THE_GAME_CONTROLLER.game();
                    if (game.hasDefaultOptionValues()) {
                        game.setCanStartNewGame(false); // TODO check this
                        THE_GAME_CONTROLLER.restart(GameState.STARTING_GAME);
                    } else {
                        THE_GAME_CONTROLLER.changeState(GameState.SETTING_OPTIONS);
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