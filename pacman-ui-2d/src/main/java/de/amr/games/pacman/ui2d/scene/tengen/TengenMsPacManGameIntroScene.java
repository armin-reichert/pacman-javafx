/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.fsm.FiniteStateMachine;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui2d.scene.ms_pacman.MsPacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.scene.ms_pacman.MsPacManGamePacAnimations;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.BitSet;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.scene.tengen.TengenMsPacManGameRenderer.*;

/**
 * Intro scene of the Tengen Ms. Pac-Man game.
 *
 * @author Armin Reichert
 */
public class TengenMsPacManGameIntroScene extends GameScene2D {

    static final float    SPEED = 2.2f; //TODO check exact speed
    static final int      TOP_Y = TS * 11 + 1;
    static final int      STOP_X_GHOST = TS * 5 + 2;
    static final int      STOP_X_MS_PAC_MAN = TS * 15 + 2;
    static final Vector2i TITLE_POSITION = v2i(TS * 10, TS * 8);
    static final int      NUM_BULBS = 96;
    static final int      DISTANCE_BETWEEN_ACTIVE_BULBS = 16;

    private final FiniteStateMachine<SceneState, TengenMsPacManGameIntroScene> sceneController;

    private Pac msPacMan;
    private Ghost[] ghosts;
    private Color[] ghostColors;
    private TickTimer marqueeTimer;
    private int ghostIndex;
    private int waitBeforeRising;

    public TengenMsPacManGameIntroScene() {
        sceneController = new FiniteStateMachine<>(SceneState.values()) {
            @Override
            public TengenMsPacManGameIntroScene context() {
                return TengenMsPacManGameIntroScene.this;
            }
        };
    }

    @Override
    public void init() {
        context.setScoreVisible(false);

        msPacMan = new Pac();
        ghosts = new Ghost[] { Ghost.blinky(), Ghost.inky(), Ghost.pinky(), Ghost.sue() };
        ghostColors = new Color[] {
            context.assets().color("tengen.ghost.0.color.normal.dress"),
            context.assets().color("tengen.ghost.2.color.normal.dress"),
            context.assets().color("tengen.ghost.1.color.normal.dress"),
            context.assets().color("tengen.ghost.3.color.normal.dress"),
        };
        marqueeTimer = new TickTimer("marquee-timer");
        ghostIndex = 0;
        waitBeforeRising = 0;

        msPacMan.setAnimations(new MsPacManGamePacAnimations(context.spriteSheet()));
        msPacMan.selectAnimation(Pac.ANIM_MUNCHING);

        for (Ghost ghost : ghosts) {
            ghost.setAnimations(new MsPacManGameGhostAnimations(context.spriteSheet(), ghost.id()));
            ghost.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
        }
        sceneController.restart(SceneState.WAITING_FOR_START);
    }

    @Override
    public void end() {
        context.sounds().stopVoice();
    }

    @Override
    public void update() {
        sceneController.update();
    }

    @Override
    public void drawSceneContent(GameWorldRenderer renderer) {
        TickTimer timer = sceneController.state().timer();
        long t = timer.currentTick();
        Font font = renderer.scaledArcadeFont(TS);
        BitSet marqueeState = computeMarqueeState(marqueeTimer.currentTick());
        switch (sceneController.state()) {
            case WAITING_FOR_START -> {
                // Loop over 3 different shades of blue, 16 frames each
                renderer.drawText("TENGEN PRESENTS", shadeOfBlue(t, 16), font, 6 * TS, 11 * TS);
                // Draw Tengen logo without image smoothing
                renderer.ctx().save();
                renderer.ctx().setImageSmoothing(false);
                renderer.drawSpriteScaled(context.spriteSheet(), TengenMsPacManGameSpriteSheet.MS_PAC_MAN_TITLE, 3 * TS, 12 * TS);
                renderer.ctx().restore();
                // Blink effect TODO: check exact rate
                if (t % 60 < 30) {
                    renderer.drawText("PRESS SPACE", Color.WHITE, font, 8 * TS, 21 * TS);
                }
                Font copyrightFont = renderer.scaledArcadeFont(7.5);
                Color copyrightColor = TENGEN_PINK;
                renderer.drawText("MS PAC-MAN TM NAMCO LTD", copyrightColor, copyrightFont, 3 * TS, 27 * TS);
                renderer.drawText("©1990 TENGEN INC",        copyrightColor, copyrightFont, 5 * TS, 28 * TS);
                renderer.drawText("ALL RIGHTS RESERVED",     copyrightColor, copyrightFont, 4 * TS, 29 * TS);
            }
            case SHOWING_MARQUEE -> drawMarquee(renderer, font, marqueeState);
            case GHOSTS_MARCHING_IN -> {
                drawMarquee(renderer, font, marqueeState);
                if (ghostIndex == 0) {
                    renderer.drawText("WITH", Color.WHITE, font, TITLE_POSITION.x() - TS, TOP_Y + 20);
                }
                String ghostName = ghosts[ghostIndex].name().toUpperCase();
                renderer.drawText(ghostName, ghostColors[ghostIndex], font, TITLE_POSITION.x() + t(3), TOP_Y + 38);
                for (Ghost ghost : ghosts) { renderer.drawAnimatedEntity(ghost); }
                renderer.drawAnimatedEntity(msPacMan);
            }
            case MS_PACMAN_MARCHING_IN -> {
                drawMarquee(renderer, font, marqueeState);
                renderer.drawText("STARRING",   Color.WHITE, font, TITLE_POSITION.x() - TS, TOP_Y + 20);
                renderer.drawText("MS PAC-MAN", TENGEN_YELLOW, font, TITLE_POSITION.x() + TS, TOP_Y + 38);
                for (Ghost ghost : ghosts) { renderer.drawAnimatedEntity(ghost); }
                renderer.drawAnimatedEntity(msPacMan);
            }
        }
    }

    /**
     * 6 of the 96 bulbs are switched on per frame, shifting counter-clockwise every tick.
     * The bulbs on the left border however are switched off every second frame. Bug in original game?
     *
     * @return bit set indicating which bulbs are switched on
     */
    private BitSet computeMarqueeState(long tick) {
        var state = new BitSet(NUM_BULBS);
        for (int b = 0; b < 6; ++b) {
            state.set((b * DISTANCE_BETWEEN_ACTIVE_BULBS + (int) tick) % NUM_BULBS);
        }
        // Simulate bug on left border
        for (int i = 81; i < NUM_BULBS; i += 2) {
            state.clear(i);
        }
        return state;
    }

    // TODO This is too cryptic
    private void drawMarquee(GameWorldRenderer renderer, Font font, BitSet marqueeState) {
        renderer.drawText("\"MS PAC-MAN\"", TENGEN_YELLOW, font, TITLE_POSITION.x(), TITLE_POSITION.y());
        double xMin = 60, xMax = 192, yMin = 88, yMax = 148;
        GraphicsContext g = renderer.ctx();
        for (int i = 0; i < NUM_BULBS; ++i) {
            boolean on = marqueeState.get(i);
            g.setFill(on ? Color.WHITE : MARQUEE_COLOR);
            if (i <= 33) { // lower edge left-to-right
                drawBulb(g, xMin + 4 * i, yMax);
            } else if (i <= 48) { // right edge bottom-to-top
                drawBulb(g, xMax, 4 * (70 - i));
            } else if (i <= 81) { // upper edge right-to-left
                drawBulb(g, 4 * (96 - i), yMin);
            } else { // left edge top-to-bottom
                drawBulb(g, xMin, 4 * (i - 59));
            }
        }
    }

    private void drawBulb(GraphicsContext g, double x, double y) {
        g.fillRect(scaled(x), scaled(y), scaled(2), scaled(2));
    }

    private enum SceneState implements FsmState<TengenMsPacManGameIntroScene> {

        WAITING_FOR_START {
            @Override
            public void onUpdate(TengenMsPacManGameIntroScene intro) {
                if (intro.context.keyboard().pressed(KeyCode.SPACE)) {
                    intro.context.gameController().changeState(GameState.STARTING);
                } else if (timer().atSecond(8)) {
                    intro.sceneController.changeState(SHOWING_MARQUEE);
                }
            }
        },

        SHOWING_MARQUEE {
            @Override
            public void onEnter(TengenMsPacManGameIntroScene intro) {
                intro.marqueeTimer.restartIndefinitely();
                intro.msPacMan.setPosition(TS * 31, TS * 20);
                intro.msPacMan.setMoveDir(Direction.LEFT);
                intro.msPacMan.setSpeed(SPEED);
                intro.msPacMan.setVisible(true);
                intro.msPacMan.selectAnimation(Pac.ANIM_MUNCHING);
                intro.msPacMan.animations().ifPresent(Animations::startSelected);
                for (Ghost ghost : intro.ghosts) {
                    ghost.setPosition(TS * 33.5f, TS * 20);
                    ghost.setMoveAndWishDir(Direction.LEFT);
                    ghost.setSpeed(SPEED);
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setVisible(true);
                    ghost.startAnimation();
                }
                intro.ghostIndex = 0;
            }

            @Override
            public void onUpdate(TengenMsPacManGameIntroScene intro) {
                intro.marqueeTimer.tick();
                if (timer.atSecond(1)) {
                    intro.sceneController.changeState(GHOSTS_MARCHING_IN);
                }
            }
        },

        GHOSTS_MARCHING_IN {

            @Override
            public void onUpdate(TengenMsPacManGameIntroScene intro) {
                intro.marqueeTimer.tick();
                boolean reachedEndPosition = letGhostMarchIn(intro);
                if (reachedEndPosition) {
                    if (intro.ghostIndex == 3) {
                        intro.sceneController.changeState(MS_PACMAN_MARCHING_IN);
                    } else {
                        ++intro.ghostIndex;
                    }
                }
            }

            boolean letGhostMarchIn(TengenMsPacManGameIntroScene intro) {
                Ghost ghost = intro.ghosts[intro.ghostIndex];
                if (ghost.moveDir() == Direction.LEFT) {
                    if (ghost.posX() <= STOP_X_GHOST) {
                        ghost.setPosX(STOP_X_GHOST);
                        ghost.setMoveAndWishDir(Direction.UP);
                        intro.waitBeforeRising = 2;
                    } else {
                        ghost.move();
                    }
                }
                else if (ghost.moveDir() == Direction.UP) {
                    int endPositionY = TOP_Y + intro.ghostIndex * 16;
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
            public void onUpdate(TengenMsPacManGameIntroScene intro) {
                intro.marqueeTimer.tick();
                intro.msPacMan.move();
                if (intro.msPacMan.posX() <= STOP_X_MS_PAC_MAN) {
                    intro.msPacMan.setSpeed(0);
                    intro.msPacMan.animations().ifPresent(Animations::resetSelected); //TODO check in Tengen, seems not to work!
                }
                if (timer().atSecond(8)) {
                    // show demo level
                    intro.context.gameController().changeState(GameState.READY);
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