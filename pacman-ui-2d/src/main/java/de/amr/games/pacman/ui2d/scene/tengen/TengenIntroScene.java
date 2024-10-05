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
import static de.amr.games.pacman.ui2d.scene.tengen.TengenGameWorldRenderer.PINKISH;
import static de.amr.games.pacman.ui2d.scene.tengen.TengenGameWorldRenderer.YELLOWISH;

/**
 * Intro scene of the Tengen Ms. Pac-Man game.
 *
 * @author Armin Reichert
 */
public class TengenIntroScene extends GameScene2D {

    //TODO check exact colors
    static final Color[]  SHADES_OF_BLUE = {
        Color.DARKBLUE, Color.BLUE, Color.LIGHTBLUE
    };
    static final float    SPEED = 2.2f; //TODO check exact speed
    static final int      TOP_Y = TS * 11 + 1;
    static final int      STOP_X_GHOST = TS * 6 - 4;
    static final int      STOP_X_MS_PAC_MAN = TS * 15 + 2;
    static final Vector2i TITLE_POSITION = v2i(TS * 10, TS * 8);
    static final int      NUM_BULBS = 96;
    static final int      DISTANCE_BETWEEN_ACTIVE_BULBS = 16;

    private enum SceneState implements FsmState<TengenIntroScene> {

        TENGEN_PRESENTS {
            @Override
            public void onEnter(TengenIntroScene intro) {
                intro.tengenPresentsY = 36 * TS;
            }

            @Override
            public void onUpdate(TengenIntroScene intro) {
                long t = timer.currentTick();
                if (0 <= t && t < 120) {
                    if (t % 4 == 0 && intro.tengenPresentsY > 16 * TS) { // TODO check y position
                        intro.tengenPresentsY -= TS;
                    }
                }
                else if (t == 120) {
                    intro.ghosts[0].setPosition(t(31), t(30)); //TODO check y position
                    intro.ghosts[0].setMoveAndWishDir(Direction.LEFT);
                    intro.ghosts[0].setSpeed(8); // TODO check speed
                    intro.ghosts[0].setVisible(true);
                }
                else if (120 < t && t < 240) {
                    intro.ghosts[0].move();
                }
                else if (240 <= t && t < 360) {
                    if (t % 4 == 0) { intro.tengenPresentsY += 2*TS; }
                }
                else if (t == 360) {
                    intro.sceneController.changeState(WAITING_FOR_START);
                }
            }
        },

        WAITING_FOR_START {
            @Override
            public void onUpdate(TengenIntroScene intro) {
                //TODO check this
                if (intro.context.keyboard().pressed(KeyCode.SPACE)) {
                    intro.sceneController.changeState(SHOWING_MARQUEE);
                }
            }
        },

        SHOWING_MARQUEE {
            @Override
            public void onEnter(TengenIntroScene intro) {
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
            public void onUpdate(TengenIntroScene intro) {
                intro.marqueeTimer.tick();
                if (timer.atSecond(1)) {
                    intro.sceneController.changeState(GHOSTS_MARCHING_IN);
                }
            }
        },

        GHOSTS_MARCHING_IN {

            @Override
            public void onUpdate(TengenIntroScene intro) {
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

            boolean letGhostMarchIn(TengenIntroScene intro) {
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
            public void onUpdate(TengenIntroScene intro) {
                intro.marqueeTimer.tick();
                intro.msPacMan.move();
                if (intro.msPacMan.posX() <= STOP_X_MS_PAC_MAN) {
                    intro.msPacMan.setSpeed(0);
                    intro.msPacMan.animations().ifPresent(Animations::resetSelected);
                }
                if (timer().atSecond(4)) {
                    intro.context.gameController().changeState(GameState.CREDIT); // The settings scene
                }
            }
        };

        final TickTimer timer = new TickTimer("Timer-" + name());

        @Override
        public TickTimer timer() {
            return timer;
        }
    }

    private final FiniteStateMachine<SceneState, TengenIntroScene> sceneController;

    private Pac msPacMan;
    private Ghost[] ghosts;
    private Color[] ghostColors;
    private TickTimer marqueeTimer;
    private int ghostIndex;
    private int waitBeforeRising;
    private int tengenPresentsY;

    public TengenIntroScene() {
        sceneController = new FiniteStateMachine<>(SceneState.values()) {
            @Override
            public TengenIntroScene context() {
                return TengenIntroScene.this;
            }
        };
    }

    @Override
    public boolean isCreditVisible() {
        return false;
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

        //TODO use Tengen sprites for Ms. Pac-Man and PacMan characters
        GameSpriteSheet msPacManGameSpriteSheet = context.assets().get("ms_pacman.spritesheet");
        msPacMan.setAnimations(new MsPacManGamePacAnimations(msPacManGameSpriteSheet));
        msPacMan.selectAnimation(Pac.ANIM_MUNCHING);

        for (Ghost ghost : ghosts) {
            ghost.setAnimations(new MsPacManGameGhostAnimations(context.spriteSheet(), ghost.id()));
            ghost.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
        }
        sceneController.restart(SceneState.TENGEN_PRESENTS);
    }

    @Override
    public void end() {
        context.sounds().stopVoice();
    }

    @Override
    public void update() {
        sceneController.update();
    }

    private static Color shadeOfBlue(long t) {
        int ticksPerColor = 16;
        int ticksPerAnimation = (ticksPerColor * SHADES_OF_BLUE.length);
        int index = (int) (t % ticksPerAnimation) / ticksPerColor;
        return SHADES_OF_BLUE[index];
    }

    @Override
    public void drawSceneContent(GameWorldRenderer renderer) {
        TickTimer timer = sceneController.state().timer();
        long t = timer.currentTick();
        Font font = renderer.scaledArcadeFont(TS);
        BitSet marqueeState = computeMarqueeState(marqueeTimer.currentTick());
        switch (sceneController.state()) {
            case TENGEN_PRESENTS -> {
                renderer.drawText("TENGEN PRESENTS", shadeOfBlue(t), font, 6 * TS, tengenPresentsY);
                renderer.drawAnimatedEntity(ghosts[0]);
            }
            case WAITING_FOR_START -> {
                // Loop over 3 different shades of blue, 16 frames each
                renderer.drawText("TENGEN PRESENTS", shadeOfBlue(t), font, 6 * TS, 10 * TS);
                renderer.drawSpriteScaled(context.spriteSheet(), TengenSpriteSheet.MS_PAC_MAN_TITLE, 3 * TS, 11 * TS);
                // Blink effect, 32 frames for each phase. TODO: check rate
                if (t % 96 < 48) {
                    renderer.drawText("PRESS SPACE", Color.WHITE, font, 8 * TS, 20 * TS);
                }
                Font copyrightFont = renderer.scaledArcadeFont(7.5);
                Color copyrightColor = PINKISH; //TODO check this
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
                renderer.drawText("MS PAC-MAN", YELLOWISH,   font, TITLE_POSITION.x() + TS, TOP_Y + 38);
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
        renderer.drawText("\"MS PAC-MAN\"", YELLOWISH, font, TITLE_POSITION.x(), TITLE_POSITION.y());
        double xMin = 60, xMax = 192, yMin = 88, yMax = 148;
        GraphicsContext g = renderer.ctx();
        for (int i = 0; i < NUM_BULBS; ++i) {
            boolean on = marqueeState.get(i);
            g.setFill(on ? Color.WHITE : Color.web("d84060"));
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
}