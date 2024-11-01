/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.fsm.FiniteStateMachine;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.ms_pacman_tengen.TengenMsPacManGame;
import de.amr.games.pacman.ui2d.GameActions2D;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.util.NES_Controller;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.BitSet;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameRenderer.*;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfiguration.NES_RESOLUTION_Y;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfiguration.NES_RESOLUTION_X;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSpriteSheet.MS_PAC_MAN_TITLE_SPRITE;

/**
 * @author Armin Reichert
 */
public class IntroScene extends GameScene2D {

    // Anchor point for everything
    static final int MARQUEE_TOP_X = 60, MARQUEE_TOP_Y = 64;

    static final int ACTOR_Y = MARQUEE_TOP_Y + 72;
    static final int GHOST_STOP_X = MARQUEE_TOP_X - 18;
    static final int MS_PAC_MAN_STOP_X = MARQUEE_TOP_X + 62;
    static final int NUM_BULBS = 96;
    static final float SPEED = 2.2f; //TODO check exact speed

    private final FiniteStateMachine<SceneState, IntroScene> sceneController;

    private Pac msPacMan;
    private Ghost[] ghosts;
    private int ghostIndex;
    private int waitBeforeRising;

    public IntroScene() {
        sceneController = new FiniteStateMachine<>(SceneState.values()) {
            @Override
            public IntroScene context() {
                return IntroScene.this;
            }
        };
    }

    @Override
    public void defineGameActionKeyBindings() {
        bindAction(GameActions2D.START_GAME, NES_Controller.DEFAULT_CONTROLLER.start());
    }

    @Override
    public void doInit() {
        context.plugIn_NES_Controller();

        TengenMsPacManGameSpriteSheet spriteSheet = (TengenMsPacManGameSpriteSheet) context.currentGameSceneConfig().spriteSheet();
        context.setScoreVisible(false);

        ghostIndex = 0;
        waitBeforeRising = 0;

        msPacMan = new Pac();
        msPacMan.setAnimations(new PacAnimations(spriteSheet));
        msPacMan.selectAnimation(GameModel.ANIM_PAC_MUNCHING);

        ghosts = new Ghost[] { Ghost.blinky(), Ghost.inky(), Ghost.pinky(), Ghost.sue() };
        for (Ghost ghost : ghosts) {
            ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id()));
            ghost.selectAnimation(GameModel.ANIM_GHOST_NORMAL);
        }

        sceneController.restart(SceneState.WAITING_FOR_START);
    }

    @Override
    protected void doEnd() {
        context.sound().stopVoice(); // TODO check if needed
        context.plugOut_NES_Controller();
    }

    @Override
    public void update() {
        sceneController.update();
    }

    @Override
    public Vector2f size() {
        return new Vector2f(NES_RESOLUTION_X, NES_RESOLUTION_Y);
    }

    @Override
    public void drawSceneContent(GameRenderer renderer) {
        renderer.ctx().save();
        renderer.ctx().setImageSmoothing(false);
        TickTimer timer = sceneController.state().timer();
        long t = timer.currentTick();
        Font font = renderer.scaledArcadeFont(8);
        switch (sceneController.state()) {

            case WAITING_FOR_START -> {
                renderer.drawText("TENGEN PRESENTS", shadeOfBlue(t), font, 9 * TS, MARQUEE_TOP_Y - TS);
                renderer.drawSpriteScaled(MS_PAC_MAN_TITLE_SPRITE, 6 * TS, MARQUEE_TOP_Y);
                if (t % 60 < 30) {
                    renderer.drawText("PRESS [START]", Color.WHITE, font, 11 * TS, MARQUEE_TOP_Y + 9 * TS);
                }
                renderer.drawText("MS PAC-MAN TM NAMCO LTD", TENGEN_PINK, font, 6 * TS, MARQUEE_TOP_Y + 13 * TS);
                renderer.drawText("©1990 TENGEN INC",        TENGEN_PINK, font, 8 * TS, MARQUEE_TOP_Y + 14 * TS);
                renderer.drawText("ALL RIGHTS RESERVED",     TENGEN_PINK, font, 7 * TS, MARQUEE_TOP_Y + 15 * TS);
            }

            case SHOWING_MARQUEE -> {
                drawTitle(renderer, font);
                drawMarquee(renderer, t);
            }

            case GHOSTS_MARCHING_IN -> {
                drawTitle(renderer, font);
                drawMarquee(renderer, t);
                if (ghostIndex == 0) {
                    renderer.drawText("WITH", Color.WHITE, font, MARQUEE_TOP_X + 12, MARQUEE_TOP_Y + 23);
                }
                String ghostName = ghosts[ghostIndex].name().toUpperCase();
                Color ghostColor = context.assets().color("tengen.ghost.%d.color.normal.dress".formatted(ghosts[ghostIndex].id()));
                renderer.drawText(ghostName, ghostColor, font, MARQUEE_TOP_X + 44, MARQUEE_TOP_Y + 41);
                for (Ghost ghost : ghosts) { renderer.drawAnimatedEntity(ghost); }
            }

            case MS_PACMAN_MARCHING_IN -> {
                drawTitle(renderer, font);
                drawMarquee(renderer, t);
                renderer.drawText("STARRING", Color.WHITE, font, MARQUEE_TOP_X + 12, MARQUEE_TOP_Y + 22);
                renderer.drawText("MS PAC-MAN", TENGEN_YELLOW, font, MARQUEE_TOP_X + 44, MARQUEE_TOP_Y + 38);
                for (Ghost ghost : ghosts) { renderer.drawAnimatedEntity(ghost); }
                renderer.drawAnimatedEntity(msPacMan);
            }
        }
        renderer.ctx().restore();
    }

    private void drawTitle(GameRenderer renderer, Font font) {
        renderer.drawText("\"MS PAC-MAN\"", TENGEN_YELLOW, font, MARQUEE_TOP_X + 20, MARQUEE_TOP_Y - 18);
    }

    /**
     * 6 of the 96 bulbs are switched on per frame, shifting counter-clockwise every tick.
     * The bulbs on the left border however are switched off every second frame. Bug in original game?
     *
     * @param t clock tick
     * @return bit set indicating which bulbs are switched on
     */
    private BitSet marqueeState(long t) {
        var state = new BitSet(NUM_BULBS);
        for (int b = 0; b < 6; ++b) {
            state.set((int) (b * 16 + t) % NUM_BULBS);
        }
        // Simulate bug on left border
        for (int i = 81; i < NUM_BULBS; i += 2) {
            state.clear(i);
        }
        return state;
    }

    private void drawMarquee(GameRenderer renderer, long t) {
        BitSet bulbOn = marqueeState(t);
        double xMin = MARQUEE_TOP_X, xMax = xMin + 132, yMin = MARQUEE_TOP_Y, yMax = yMin + 60;
        GraphicsContext g = renderer.ctx();
        for (int i = 0; i < NUM_BULBS; ++i) {
            g.setFill(bulbOn.get(i) ? Color.WHITE : TENGEN_MARQUEE_COLOR);
            // TODO This is too cryptic
            if (i <= 33) {
                // lower border left-to-right
                drawBulb(g, xMin + 4 * i, yMax);
            } else if (i <= 48) {
                // right border bottom-to-top
                drawBulb(g, xMax, yMax - 4 * (i - 33));
            } else if (i <= 81) {
                // upper border right-to-left
                drawBulb(g, xMax - 4 * (i - 48), yMin);
            } else {
                // left border top-to-bottom
                drawBulb(g, xMin, yMin + 4 * (i - 81));
            }
        }
    }

    private void drawBulb(GraphicsContext g, double x, double y) {
        g.fillRect(scaled(x), scaled(y), scaled(2), scaled(2));
    }

    private enum SceneState implements FsmState<IntroScene> {

        WAITING_FOR_START {
            @Override
            public void onUpdate(IntroScene intro) {
                if (timer().atSecond(8)) {
                    intro.sceneController.changeState(SHOWING_MARQUEE);
                }
            }
        },

        SHOWING_MARQUEE {
            @Override
            public void onEnter(IntroScene intro) {
                intro.msPacMan.setPosition(TS * 33, ACTOR_Y);
                intro.msPacMan.setMoveDir(Direction.LEFT);
                intro.msPacMan.setSpeed(SPEED);
                intro.msPacMan.setVisible(true);
                intro.msPacMan.selectAnimation(GameModel.ANIM_PAC_MUNCHING);
                intro.msPacMan.animations().ifPresent(Animations::startCurrentAnimation);
                for (Ghost ghost : intro.ghosts) {
                    ghost.setPosition(TS * 33, ACTOR_Y);
                    ghost.setMoveAndWishDir(Direction.LEFT);
                    ghost.setSpeed(SPEED);
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setVisible(true);
                    ghost.startAnimation();
                }
                intro.ghostIndex = 0;
            }

            @Override
            public void onUpdate(IntroScene intro) {
                if (timer.atSecond(1)) {
                    intro.sceneController.changeState(GHOSTS_MARCHING_IN);
                }
            }
        },

        GHOSTS_MARCHING_IN {

            @Override
            public void onUpdate(IntroScene intro) {
                boolean reachedEndPosition = letGhostMarchIn(intro);
                if (reachedEndPosition) {
                    if (intro.ghostIndex == 3) {
                        intro.sceneController.changeState(MS_PACMAN_MARCHING_IN);
                    } else {
                        ++intro.ghostIndex;
                    }
                }
            }

            boolean letGhostMarchIn(IntroScene intro) {
                Ghost ghost = intro.ghosts[intro.ghostIndex];
                if (ghost.moveDir() == Direction.LEFT) {
                    if (ghost.posX() <= GHOST_STOP_X) {
                        ghost.setPosX(GHOST_STOP_X);
                        ghost.setMoveAndWishDir(Direction.UP);
                        intro.waitBeforeRising = 2;
                    } else {
                        ghost.move();
                    }
                }
                else if (ghost.moveDir() == Direction.UP) {
                    int endPositionY = MARQUEE_TOP_Y + intro.ghostIndex * 16;
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
                    }
                }
                return false;
            }
        },

        MS_PACMAN_MARCHING_IN {
            @Override
            public void onUpdate(IntroScene intro) {
                intro.msPacMan.move();
                if (intro.msPacMan.posX() <= MS_PAC_MAN_STOP_X) {
                    intro.msPacMan.setSpeed(0);
                    intro.msPacMan.animations().ifPresent(Animations::resetCurrentAnimation); //TODO check in Tengen, seems not to work!
                }
                if (timer().atSecond(8)) {
                    TengenMsPacManGame game = (TengenMsPacManGame) intro.context.game();
                    game.setCanStartGame(false); // TODO check this
                    intro.context.gameController().changeState(GameState.STARTING_GAME);
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