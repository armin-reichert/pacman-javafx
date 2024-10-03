/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

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
import de.amr.games.pacman.ui2d.GameAction2D;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.util.Keyboard;
import de.amr.games.pacman.ui2d.variant.ms_pacman.MsPacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.variant.ms_pacman.MsPacManGamePacAnimations;
import de.amr.games.pacman.ui2d.variant.tengen.TengenMsPacManGameSpriteSheet;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.BitSet;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.GameAction.calledAction;

/**
 * Intro scene of the Tengen Ms. Pac-Man game.
 *
 * @author Armin Reichert
 */
public class TengenMsPacManIntroScene extends GameScene2D {

    static final Color[]  SHADES_OF_BLUE = { Color.DARKBLUE, Color.BLUE, Color.LIGHTBLUE }; //TODO check exact colors
    static final Color    YELLOWISH = Color.web("e8d020");
    static final float    SPEED = 1.1f * 2; //TODO check exact speed
    static final int      TOP_Y = TS * 11 + 1;
    static final int      STOP_X_GHOST = TS * 6 - 4;
    static final int      STOP_X_MS_PAC_MAN = TS * 15 + 2;
    static final Vector2i TITLE_POSITION = v2i(TS * 10, TS * 8);
    static final int      NUM_BULBS = 96;
    static final int      DISTANCE_BETWEEN_ACTIVE_BULBS = 16;

    private enum SceneState implements FsmState<TengenMsPacManIntroScene> {

        TENGEN_PRESENTS {
            @Override
            public void onEnter(TengenMsPacManIntroScene intro) {
                intro.tengenPresentsY = 36 * TS;
            }

            @Override
            public void onUpdate(TengenMsPacManIntroScene intro) {
                long t = timer.currentTick();
                if (0 <= t && t < 120) {
                    if (t % 4 == 0 && intro.tengenPresentsY > 16 * TS) { // TODO check y position
                        intro.tengenPresentsY -= TS;
                    }
                }
                else if (t == 120) {
                    intro.ghosts[0].setPosition(t(31), t(30)); //TODO check y position
                    intro.ghosts[0].setMoveAndWishDir(Direction.LEFT);
                    intro.ghosts[0].setSpeed(3); // TODO check speed
                    intro.ghosts[0].setVisible(true);
                }
                else if (120 < t && t < 360) {
                    intro.ghosts[0].move();
                }
                else if (360 <= t && t < 480) {
                    if (t % 4 == 0) {
                        intro.tengenPresentsY += TS;
                    }
                }
                else if (t == 480) {
                    intro.sceneController.changeState(WAITING_FOR_START);
                }
            }
        },

        WAITING_FOR_START {
            @Override
            public void onUpdate(TengenMsPacManIntroScene intro) {
                if (Keyboard.pressed(KeyCode.SPACE)) {
                    intro.sceneController.changeState(SHOWING_MARQUEE);
                }
            }
        },

        SHOWING_MARQUEE {
            @Override
            public void onEnter(TengenMsPacManIntroScene intro) {
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
            public void onUpdate(TengenMsPacManIntroScene intro) {
                intro.marqueeTimer.tick();
                if (timer.atSecond(1)) {
                    intro.sceneController.changeState(GHOSTS_MARCHING_IN);
                }
            }
        },

        GHOSTS_MARCHING_IN {

            @Override
            public void onUpdate(TengenMsPacManIntroScene intro) {
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

            boolean letGhostMarchIn(TengenMsPacManIntroScene intro) {
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
            public void onUpdate(TengenMsPacManIntroScene intro) {
                intro.marqueeTimer.tick();
                intro.msPacMan.move();
                if (intro.msPacMan.posX() <= STOP_X_MS_PAC_MAN) {
                    intro.msPacMan.setSpeed(0);
                    intro.msPacMan.animations().ifPresent(Animations::resetSelected);
                    intro.sceneController.changeState(READY_TO_PLAY);
                }
            }
        },

        READY_TO_PLAY {

            @Override
            public void onUpdate(TengenMsPacManIntroScene intro) {
                intro.marqueeTimer.tick();
                if (timer.atSecond(2.0) && !intro.context.game().hasCredit()) {
                    intro.context.gameController().changeState(GameState.READY); // demo level
                } else if (timer.atSecond(5)) {
                    intro.context.gameController().changeState(GameState.CREDIT);
                }
            }
        };

        final TickTimer timer = new TickTimer("Timer-" + name());

        @Override
        public TickTimer timer() {
            return timer;
        }
    }

    private final FiniteStateMachine<SceneState, TengenMsPacManIntroScene> sceneController;

    private Pac msPacMan;
    private Ghost[] ghosts;
    private Color[] ghostColors;
    private TickTimer marqueeTimer;
    private int ghostIndex;
    private int waitBeforeRising;
    private int tengenPresentsY;

    public TengenMsPacManIntroScene() {
        sceneController = new FiniteStateMachine<>(SceneState.values()) {
            @Override
            public TengenMsPacManIntroScene context() {
                return TengenMsPacManIntroScene.this;
            }
        };
    }

    @Override
    public boolean isCreditVisible() {
        return false;
    }

    @Override
    public void init() {
        context.setScoreVisible(true);

        msPacMan = new Pac();
        ghosts = new Ghost[] { Ghost.red(), Ghost.cyan(), Ghost.pink(), Ghost.orange() };
        ghosts[0].setName("Blinky");
        ghosts[1].setName("Inky");
        ghosts[2].setName("Pinky");
        ghosts[3].setName("Sue");
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

    @Override
    public void handleInput() {
        calledAction(GameAction2D.ADD_CREDIT, GameAction2D.START_GAME, GameAction2D.TEST_CUT_SCENES)
            .ifPresent(action -> action.execute(context));
    }

    private static Color shadeOfBlue(long t) {
        int index = (int) (t % 48) /16;
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
                renderer.drawSpriteScaled(context.spriteSheet(), TengenMsPacManGameSpriteSheet.MS_PAC_MAN_TITLE, 3 * TS, 11 * TS);
                // Blink effect, 32 frames for each phase. TODO: check rate
                if (t % 64 < 32) {
                    renderer.drawText("PRESS START", Color.WHITE, font, 8 * TS, 20 * TS);
                }
                Color copyrightColor = Color.web("#ff60b0"); //TODO check this
                renderer.drawText("MS PAC-MAN TM NAMCO LTD", copyrightColor, font, 3 * TS, 27 * TS);
                renderer.drawText("©1990 TENGEN INC",        copyrightColor, font, 5 * TS, 28 * TS);
                renderer.drawText("ALL RIGHTS RESERVED",     copyrightColor, font, 4 * TS, 29 * TS);
            }
            case SHOWING_MARQUEE -> drawMarquee(renderer, font, marqueeState);
            case GHOSTS_MARCHING_IN -> {
                drawMarquee(renderer, font, marqueeState);
                if (ghostIndex == 0) {
                    renderer.drawText("WITH", Color.WHITE, font, TITLE_POSITION.x(), TOP_Y + 20);
                }
                String ghostName = ghosts[ghostIndex].name().toUpperCase();
                double indent = ghostName.length() < 4 ? TS : 0;
                renderer.drawText(ghostName, ghostColors[ghostIndex], font, TITLE_POSITION.x() + t(3) + indent, TOP_Y + 40);
                for (Ghost ghost : ghosts) { renderer.drawAnimatedEntity(ghost); }
                renderer.drawAnimatedEntity(msPacMan);
            }
            case MS_PACMAN_MARCHING_IN, READY_TO_PLAY -> {
                drawMarquee(renderer, font, marqueeState);
                renderer.drawText("STARRING",   Color.WHITE, font, TITLE_POSITION.x(), TOP_Y + 20);
                renderer.drawText("MS PAC-MAN", YELLOWISH,   font, TITLE_POSITION.x(), TOP_Y + 40);
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
            if (i <= 33) { // lower edge left-to-right
                drawBulb(g, xMin + 4 * i, yMax, on);
            } else if (i <= 48) { // right edge bottom-to-top
                drawBulb(g, xMax, 4 * (70 - i), on);
            } else if (i <= 81) { // upper edge right-to-left
                drawBulb(g, 4 * (96 - i), yMin, on);
            } else { // left edge top-to-bottom
                drawBulb(g, xMin, 4 * (i - 59), on);
            }
        }
    }

    private void drawBulb(GraphicsContext g, double x, double y, boolean on) {
        g.setFill(on ? Color.WHITE : Color.web("d84060"));
        g.fillRect(scaled(x), scaled(y), scaled(2), scaled(2));
    }
}