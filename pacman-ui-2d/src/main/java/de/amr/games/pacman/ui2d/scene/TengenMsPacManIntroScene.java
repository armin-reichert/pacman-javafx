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
import de.amr.games.pacman.model.GameModel;
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
import static de.amr.games.pacman.ui2d.GameAction.executeActionIfCalled;
import static de.amr.games.pacman.ui2d.GameAssets2D.*;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 *
 * @author Armin Reichert
 */
public class TengenMsPacManIntroScene extends GameScene2D {

    static final Color[] BLUE_SHADES = { Color.DARKBLUE, Color.BLUE, Color.LIGHTBLUE };
    static final float SPEED = 2.5f;
    static final int TOP_Y = TS * 11 + 1;
    static final int STOP_X_GHOST = TS * 6 - 4;
    static final int STOP_X_MS_PAC_MAN = TS * 15 + 2;
    static final Vector2i TITLE_POSITION = v2i(TS * 10, TS * 8);
    static final int NUM_BULBS = 96;
    static final int DISTANCE_BETWEEN_ACTIVE_BULBS = 16;

    private enum SceneState implements FsmState<TengenMsPacManIntroScene> {

        WAITING_FOR_START {
            @Override
            public void onUpdate(TengenMsPacManIntroScene intro) {
                if (Keyboard.pressed(KeyCode.SPACE)) {
                    intro.sceneController.changeState(STARTING);
                }
            }
        },

        STARTING {
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
    private TickTimer marqueeTimer;
    private int ghostIndex;
    private int waitBeforeRising;

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
        return sceneController.state() != SceneState.WAITING_FOR_START;
    }

    @Override
    public void init() {
        context.setScoreVisible(true);

        msPacMan = new Pac();
        ghosts = new Ghost[] { Ghost.red(), Ghost.cyan(), Ghost.pink(), Ghost.orange() };
        ghosts[0].setName("Blinky");
        ghosts[2].setName("Pinky");
        ghosts[1].setName("Inky");
        ghosts[3].setName("Sue");
        marqueeTimer = new TickTimer("marquee-timer");
        ghostIndex = 0;
        waitBeforeRising = 0;

        //TODO use Ms. Pac-Man animations also in Tengen for now
        GameSpriteSheet msPacManGameSpriteSheet = context.assets().get("ms_pacman.spritesheet");
        msPacMan.setAnimations(new MsPacManGamePacAnimations(msPacManGameSpriteSheet));
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
    public void handleInput() {
        executeActionIfCalled(context, GameAction2D.ADD_CREDIT, GameAction2D.START_GAME, GameAction2D.TEST_CUT_SCENES);
    }

    @Override
    public void drawSceneContent(GameWorldRenderer renderer) {
        TickTimer timer = sceneController.state().timer();
        Font font = renderer.scaledArcadeFont(TS);
        BitSet marqueeState = computeMarqueeState(marqueeTimer.currentTick());
        switch (sceneController.state()) {
            case WAITING_FOR_START -> {
                int index = (int)(timer.currentTick() % 60) / 20;
                Color color = BLUE_SHADES[index];
                renderer.drawText("TENGEN PRESENTS", color, font, 6*TS, 10*TS);
                renderer.drawSpriteScaled(context.spriteSheet(), TengenMsPacManGameSpriteSheet.MS_PAC_MAN_TITLE, 3*TS, 11*TS);
                if (timer.currentTick() % 60 < 30) {
                    renderer.drawText("PRESS START", Color.WHITE, font, 8 * TS, 20 * TS);
                }
                renderer.drawText("MS PAC-MAN TM NAMCO LTD", Color.web("#ff60b0"), font, 3*TS, 27*TS);
                renderer.drawText("©1990 TENGEN INC", Color.web("#ff60b0"), font, 5*TS, 28*TS);
                renderer.drawText("ALL RIGHTS RESERVED", Color.web("#ff60b0"), font, 4*TS, 29*TS);
            }
            case GHOSTS_MARCHING_IN -> {
                drawMarquee(renderer.ctx(), marqueeState);
                renderer.drawText("\"MS PAC-MAN\"", PALETTE_ORANGE, font, TITLE_POSITION.x(), TITLE_POSITION.y());
                if (ghostIndex == GameModel.RED_GHOST) {
                    renderer.drawText("WITH", PALETTE_PALE, font, TITLE_POSITION.x(), TOP_Y + t(3));
                }
                String ghostName = ghosts[ghostIndex].name().toUpperCase();
                Color color = context.assets().color("tengen.ghost.%d.color.normal.dress".formatted(ghostIndex));
                double dx = ghostName.length() < 4 ? t(1) : 0;
                renderer.drawText(ghostName, color, font, TITLE_POSITION.x() + t(3) + dx, TOP_Y + t(6));
                for (Ghost ghost : ghosts) {
                    renderer.drawAnimatedEntity(ghost);
                }
                renderer.drawAnimatedEntity(msPacMan);
            }
            case MS_PACMAN_MARCHING_IN, READY_TO_PLAY-> {
                drawMarquee(renderer.ctx(), marqueeState);
                renderer.drawText("\"MS PAC-MAN\"", PALETTE_ORANGE, font, TITLE_POSITION.x(), TITLE_POSITION.y());
                renderer.drawText("STARRING", PALETTE_PALE, font, TITLE_POSITION.x(), TOP_Y + t(3));
                renderer.drawText("MS PAC-MAN", PALETTE_YELLOW, font, TITLE_POSITION.x(), TOP_Y + t(6));
                for (Ghost ghost : ghosts) {
                    renderer.drawAnimatedEntity(ghost);
                }
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
    private void drawMarquee(GraphicsContext g, BitSet marqueeState) {
        double xMin = 60, xMax = 192, yMin = 88, yMax = 148;
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
        g.setFill(on ? PALETTE_PALE : PALETTE_RED);
        g.fillRect(scaled(x), scaled(y), scaled(2), scaled(2));
    }
}