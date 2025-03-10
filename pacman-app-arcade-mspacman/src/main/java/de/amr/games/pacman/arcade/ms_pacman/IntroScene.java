/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.fsm.FiniteStateMachine;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.ActorAnimations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui._2d.GameActions2D;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.BitSet;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui._2d.GameActions2D.bindTestActions;

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
    static final Vector2i TITLE_POSITION = vec_2i(TS * 10, TS * 8);
    static final int NUM_BULBS = 96;
    static final int DISTANCE_BETWEEN_ACTIVE_BULBS = 16;
    static final Color COLOR_BULB_ON = Color.valueOf(Arcade.Palette.WHITE);
    static final Color COLOR_BULB_OFF = Color.valueOf(Arcade.Palette.RED);

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
        bind(GameActions2D.INSERT_COIN, context.arcadeKeys().key(Arcade.Button.COIN));
        bind(GameActions2D.START_GAME, context.arcadeKeys().key(Arcade.Button.START));
        bindTestActions(this);
    }

    @Override
    public void doInit() {
        context.setScoreVisible(true);

        msPacMan = new Pac();
        ghosts = new Ghost[] {
            ArcadeMsPacMan_GameModel.blinky(),
            ArcadeMsPacMan_GameModel.pinky(),
            ArcadeMsPacMan_GameModel.inky(),
            ArcadeMsPacMan_GameModel.sue()
        };
        marqueeTimer = new TickTimer("marquee-timer");
        ghostIndex = 0;
        waitBeforeRising = 0;

        ArcadeMsPacMan_SpriteSheet spriteSheet = (ArcadeMsPacMan_SpriteSheet) context.gameConfiguration().spriteSheet();
        msPacMan.setAnimations(new PacAnimations(spriteSheet));
        msPacMan.selectAnimation(ActorAnimations.ANIM_PAC_MUNCHING);
        for (Ghost ghost : ghosts) {
            ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id()));
            ghost.selectAnimation(ActorAnimations.ANIM_GHOST_NORMAL);
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
    public void onCreditAdded(GameEvent e) {
        context.sound().playInsertCoinSound();
    }

    @Override
    public void drawSceneContent() {
        Font font = gr.scaledArcadeFont(TS);
        drawMarquee();
        gr.drawText("\"MS PAC-MAN\"", Color.valueOf(Arcade.Palette.ORANGE), font, TITLE_POSITION.x(), TITLE_POSITION.y());
        if (sceneController.state() == SceneState.GHOSTS_MARCHING_IN) {
            if (ghostIndex == GameModel.RED_GHOST) {
                gr.drawText("WITH", Color.valueOf(Arcade.Palette.WHITE), font, TITLE_POSITION.x(), TOP_Y + tiles2Px(3));
            }
            String ghostName = ghosts[ghostIndex].name().toUpperCase();
            Color color = switch (ghostIndex) {
                case GameModel.RED_GHOST -> Color.valueOf(Arcade.Palette.RED);
                case GameModel.PINK_GHOST -> Color.valueOf(Arcade.Palette.PINK);
                case GameModel.CYAN_GHOST -> Color.valueOf(Arcade.Palette.CYAN);
                case GameModel.ORANGE_GHOST -> Color.valueOf(Arcade.Palette.ORANGE);
                default -> throw new IllegalStateException("Illegal ghost index: " + ghostIndex);
            };
            double dx = ghostName.length() < 4 ? tiles2Px(1) : 0;
            gr.drawText(ghostName, color, font, TITLE_POSITION.x() + tiles2Px(3) + dx, TOP_Y + tiles2Px(6));
        } else if (sceneController.state() == SceneState.MS_PACMAN_MARCHING_IN || sceneController.state() == SceneState.READY_TO_PLAY) {
            gr.drawText("STARRING", Color.valueOf(Arcade.Palette.WHITE), font, TITLE_POSITION.x(), TOP_Y + tiles2Px(3));
            gr.drawText("MS PAC-MAN", Color.valueOf(Arcade.Palette.YELLOW), font, TITLE_POSITION.x(), TOP_Y + tiles2Px(6));
        }
        for (Ghost ghost : ghosts) {
            gr.drawAnimatedActor(ghost);
        }
        gr.drawAnimatedActor(msPacMan);
        // might be PacManXXL renderer
        if (gr instanceof ArcadeMsPacMan_GameRenderer r) {
            r.drawMsPacManMidwayCopyright(tiles2Px(6), tiles2Px(28), Color.valueOf(Arcade.Palette.RED), font);
        }
        gr.drawText("CREDIT %2d".formatted(context.gameController().credit), Color.valueOf(Arcade.Palette.WHITE), gr.scaledArcadeFont(TS), 2 * TS, size().y() - 2);
        gr.drawLevelCounter(context, size().x() - 4 * TS, size().y() - 2 * TS);
    }

    /**
     * 6 of the 96 light bulbs are lightning each frame, shifting counter-clockwise every tick.
     * <p>
     * The bulbs on the left border however are switched off every second frame. This is
     * probably a bug in the original Arcade game.
     * </p>
     */
    private void drawMarquee() {
        long tick = marqueeTimer.tickCount();
        var marqueeState = new BitSet(NUM_BULBS);
        for (int b = 0; b < 6; ++b) {
            marqueeState.set((b * DISTANCE_BETWEEN_ACTIVE_BULBS + (int) tick) % NUM_BULBS);
        }
        // Simulate bug on left border
        for (int i = 81; i < NUM_BULBS; i += 2) {
            marqueeState.clear(i);
        }
        final double xMin = 60, xMax = 192, yMin = 88, yMax = 148;
        final double size = scaled(2);
        for (int i = 0; i < NUM_BULBS; ++i) {
            boolean on = marqueeState.get(i);
            gr.ctx().setFill(on ? COLOR_BULB_ON : COLOR_BULB_OFF);
            if (i <= 33) {
                // lower edge left-to-right
                gr.ctx().fillRect(scaled(xMin + 4 * i), scaled(yMax), size, size);
            }
            else if (i <= 48) {
                // right edge bottom-to-top
                gr.ctx().fillRect(scaled(xMax), scaled(4 * (70 - i)), size, size);
            }
            else if (i <= 81) {
                // upper edge right-to-left
                gr.ctx().fillRect(scaled(4 * (96 - i)), scaled(yMin), size, size);
            }
            else {
                // left edge top-to-bottom
                gr.ctx().fillRect(scaled(xMin), scaled(4 * (i - 59)), size, size);
            }
        }
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
                intro.msPacMan.selectAnimation(ActorAnimations.ANIM_PAC_MUNCHING);
                intro.msPacMan.startAnimation();
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
                intro.marqueeTimer.doTick();
                if (timer.atSecond(1)) {
                    intro.sceneController.changeState(GHOSTS_MARCHING_IN);
                }
            }
        },

        GHOSTS_MARCHING_IN {

            @Override
            public void onUpdate(IntroScene intro) {
                intro.marqueeTimer.doTick();
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
                intro.marqueeTimer.doTick();
                intro.msPacMan.move();
                if (intro.msPacMan.posX() <= STOP_X_MS_PAC_MAN) {
                    intro.msPacMan.setSpeed(0);
                    intro.msPacMan.resetAnimation();
                    intro.sceneController.changeState(READY_TO_PLAY);
                }
            }
        },

        READY_TO_PLAY {

            @Override
            public void onUpdate(IntroScene intro) {
                intro.marqueeTimer.doTick();
                if (timer.atSecond(2.0) && !intro.context.game().canStartNewGame()) {
                    intro.context.gameController().changeState(GameState.STARTING_GAME); // demo level
                } else if (timer.atSecond(5)) {
                    intro.context.gameController().changeState(GameState.SETTING_OPTIONS);
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