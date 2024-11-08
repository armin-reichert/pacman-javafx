/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.fsm.FiniteStateMachine;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameActions2D;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.BitSet;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.pacman.PacManArcadeGame.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui2d.GameActions2D.bindTestActions;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 *
 * @author Armin Reichert
 */
public class IntroScene extends GameScene2D {

    static final float SPEED = 1.1f;
    static final int TOP_Y = TS * 11 + 1;
    static final int STOP_X_GHOST = TS * 6 - 4;
    static final int STOP_X_MS_PAC_MAN = TS * 15 + 2;
    static final Vector2i TITLE_POSITION = v2i(TS * 10, TS * 8);
    static final int NUM_BULBS = 96;
    static final int DISTANCE_BETWEEN_ACTIVE_BULBS = 16;

    private final FiniteStateMachine<SceneState, IntroScene> sceneController;

    private Pac msPacMan;
    private Ghost[] ghosts;
    private TickTimer marqueeTimer;
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
    public void bindGameActions() {
        bind(GameActions2D.ADD_CREDIT, context.arcade().mapControlToKey(Arcade.Controls.COIN));
        bind(GameActions2D.START_GAME, context.arcade().mapControlToKey(Arcade.Controls.START));
        bindTestActions(this);
    }

    @Override
    public void doInit() {
        context.setScoreVisible(true);

        msPacMan = new Pac();
        ghosts = new Ghost[] { Ghost.blinky(), Ghost.pinky(), Ghost.inky(), Ghost.sue() };
        marqueeTimer = new TickTimer("marquee-timer");
        ghostIndex = 0;
        waitBeforeRising = 0;

        MsPacManGameSpriteSheet spriteSheet = (MsPacManGameSpriteSheet) context.currentGameSceneConfig().spriteSheet();
        msPacMan.setAnimations(new PacAnimations(spriteSheet));
        msPacMan.selectAnimation(GameModel.ANIM_PAC_MUNCHING);
        for (Ghost ghost : ghosts) {
            ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id()));
            ghost.selectAnimation(GameModel.ANIM_GHOST_NORMAL);
        }
        sceneController.restart(SceneState.STARTING);
    }

    @Override
    protected void doEnd() {
        context.sound().stopVoice();
    }

    @Override
    public void update() {
        sceneController.update();
    }

    @Override
    public Vector2f size() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent(GameRenderer renderer) {
        MsPacManGameRenderer r = (MsPacManGameRenderer) renderer;
        Font font = r.scaledArcadeFont(TS);
        BitSet marqueeState = computeMarqueeState(marqueeTimer.currentTick());
        drawMarquee(r.ctx(), marqueeState);
        r.drawText("\"MS PAC-MAN\"", Color.valueOf(Arcade.Palette.ORANGE), font, TITLE_POSITION.x(), TITLE_POSITION.y());
        if (sceneController.state() == SceneState.GHOSTS_MARCHING_IN) {
            if (ghostIndex == GameModel.RED_GHOST) {
                r.drawText("WITH", Color.valueOf(Arcade.Palette.WHITE), font, TITLE_POSITION.x(), TOP_Y + t(3));
            }
            String ghostName = ghosts[ghostIndex].name().toUpperCase();
            Color color = switch (ghostIndex) {
                case GameModel.RED_GHOST -> Color.valueOf(Arcade.Palette.RED);
                case GameModel.PINK_GHOST -> Color.valueOf(Arcade.Palette.PINK);
                case GameModel.CYAN_GHOST -> Color.valueOf(Arcade.Palette.CYAN);
                case GameModel.ORANGE_GHOST -> Color.valueOf(Arcade.Palette.ORANGE);
                default -> throw new IllegalStateException("Illegal ghost index: " + ghostIndex);
            };
            double dx = ghostName.length() < 4 ? t(1) : 0;
            r.drawText(ghostName, color, font, TITLE_POSITION.x() + t(3) + dx, TOP_Y + t(6));
        } else if (sceneController.state() == SceneState.MS_PACMAN_MARCHING_IN || sceneController.state() == SceneState.READY_TO_PLAY) {
            r.drawText("STARRING", Color.valueOf(Arcade.Palette.WHITE), font, TITLE_POSITION.x(), TOP_Y + t(3));
            r.drawText("MS PAC-MAN", Color.valueOf(Arcade.Palette.YELLOW), font, TITLE_POSITION.x(), TOP_Y + t(6));
        }
        for (Ghost ghost : ghosts) {
            r.drawAnimatedEntity(ghost);
        }
        r.drawAnimatedEntity(msPacMan);
        r.drawMsPacManMidwayCopyright(t(6), t(28), Color.valueOf(Arcade.Palette.RED), font);
        r.drawText("CREDIT %2d".formatted(context.gameController().coinControl().credit()), Color.valueOf(Arcade.Palette.WHITE), renderer.scaledArcadeFont(TS), 2 * TS, size().y() - 2);
        r.drawLevelCounter(context, size());
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
        g.setFill(on ? Color.valueOf(Arcade.Palette.WHITE) : Color.valueOf(Arcade.Palette.RED));
        g.fillRect(scaled(x), scaled(y), scaled(2), scaled(2));
    }

    // Scene controller FSM states

    private enum SceneState implements FsmState<IntroScene> {

        STARTING {
            @Override
            public void onEnter(IntroScene intro) {
                intro.marqueeTimer.restartIndefinitely();
                intro.msPacMan.setPosition(TS * 31, TS * 20);
                intro.msPacMan.setMoveDir(Direction.LEFT);
                intro.msPacMan.setSpeed(SPEED);
                intro.msPacMan.setVisible(true);
                intro.msPacMan.selectAnimation(GameModel.ANIM_PAC_MUNCHING);
                intro.msPacMan.animations().ifPresent(Animations::startCurrentAnimation);
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
            public void onUpdate(IntroScene intro) {
                intro.marqueeTimer.tick();
                if (timer.atSecond(1)) {
                    intro.sceneController.changeState(GHOSTS_MARCHING_IN);
                }
            }
        },

        GHOSTS_MARCHING_IN {

            @Override
            public void onUpdate(IntroScene intro) {
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

            boolean letGhostMarchIn(IntroScene intro) {
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
                        ghost.stopAnimation();
                        ghost.resetAnimation();
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
                intro.marqueeTimer.tick();
                intro.msPacMan.move();
                if (intro.msPacMan.posX() <= STOP_X_MS_PAC_MAN) {
                    intro.msPacMan.setSpeed(0);
                    intro.msPacMan.animations().ifPresent(Animations::resetCurrentAnimation);
                    intro.sceneController.changeState(READY_TO_PLAY);
                }
            }
        },

        READY_TO_PLAY {

            @Override
            public void onUpdate(IntroScene intro) {
                intro.marqueeTimer.tick();
                if (timer.atSecond(2.0) && !intro.context.game().canStartNewGame()) {
                    intro.context.gameController().changeState(GameState.STARTING_GAME); // demo level
                } else if (timer.atSecond(5)) {
                    intro.context.gameController().changeState(GameState.WAITING_FOR_START);
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