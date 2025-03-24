/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui.UIGlobals.THE_GAME_CONTEXT;
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

    static final int TOP_Y        = TS * 11 + 1;
    static final int TITLE_X      = TS * 10;
    static final int TITLE_Y      = TS * 8;
    static final int STOP_X_GHOST = TS * 6 - 4;
    static final int STOP_X_MSPAC = TS * 15 + 2;

    static final int BULB_COUNT = 96;
    static final int ACTIVE_BULBS_DIST = 16;
    static final int MARQUEE_XMIN = 60, MARQUEE_XMAX = 192, MARQUEE_YMIN = 88, MARQUEE_YMAX = 148;

    static final Color COLOR_CYAN   = Color.web(Arcade.Palette.CYAN);
    static final Color COLOR_ORANGE = Color.web(Arcade.Palette.ORANGE);
    static final Color COLOR_PINK   = Color.web(Arcade.Palette.PINK);
    static final Color COLOR_RED    = Color.web(Arcade.Palette.RED);
    static final Color COLOR_WHITE  = Color.web(Arcade.Palette.WHITE);
    static final Color COLOR_YELLOW = Color.web(Arcade.Palette.YELLOW);

    static final Color COLOR_BULB_ON  = COLOR_WHITE;
    static final Color COLOR_BULB_OFF = COLOR_RED;

    static final Color[] COLOR_GHOST = { COLOR_RED, COLOR_PINK, COLOR_CYAN, COLOR_ORANGE };

    private final FiniteStateMachine<SceneState, IntroScene> sceneController;

    private Pac msPacMan;
    private Ghost[] ghosts;
    private TickTimer marqueeTimer;
    private int ghostID;
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
        bind(GameActions2D.INSERT_COIN, THE_GAME_CONTEXT.arcadeKeys().key(Arcade.Button.COIN));
        bind(GameActions2D.START_GAME, THE_GAME_CONTEXT.arcadeKeys().key(Arcade.Button.START));
        bindTestActions(this);
    }

    @Override
    public void doInit() {
        THE_GAME_CONTEXT.setScoreVisible(true);

        msPacMan = new Pac();
        ghosts = new Ghost[] {
            ArcadeMsPacMan_GameModel.blinky(),
            ArcadeMsPacMan_GameModel.pinky(),
            ArcadeMsPacMan_GameModel.inky(),
            ArcadeMsPacMan_GameModel.sue()
        };
        marqueeTimer = new TickTimer("marquee-timer");
        ghostID = 0;
        waitBeforeRising = 0;

        ArcadeMsPacMan_SpriteSheet spriteSheet = (ArcadeMsPacMan_SpriteSheet) THE_GAME_CONTEXT.gameConfiguration().spriteSheet();
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
        THE_GAME_CONTEXT.sound().stopVoice();
    }

    @Override
    public void update() {
        sceneController.update();
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        THE_GAME_CONTEXT.sound().playInsertCoinSound();
    }

    @Override
    public void drawSceneContent() {
        final Font font = gr.scaledArcadeFont(TS);
        drawMarquee();
        gr.drawText("\"MS PAC-MAN\"", COLOR_ORANGE, font, TITLE_X, TITLE_Y);
        if (sceneController.state() == SceneState.GHOSTS_MARCHING_IN) {
            String ghostName = ghosts[ghostID].name().toUpperCase();
            double dx = ghostName.length() < 4 ? tiles2Px(1) : 0;
            if (ghostID == GameModel.RED_GHOST_ID) {
                gr.drawText("WITH", COLOR_WHITE, font, TITLE_X, TOP_Y + tiles2Px(3));
            }
            gr.drawText(ghostName, COLOR_GHOST[ghostID], font, TITLE_X + tiles2Px(3) + dx, TOP_Y + tiles2Px(6));
        }
        else if (sceneController.state() == SceneState.MS_PACMAN_MARCHING_IN || sceneController.state() == SceneState.READY_TO_PLAY) {
            gr.drawText("STARRING", COLOR_WHITE, font, TITLE_X, TOP_Y + tiles2Px(3));
            gr.drawText("MS PAC-MAN", COLOR_YELLOW, font, TITLE_X, TOP_Y + tiles2Px(6));
        }
        for (Ghost ghost : ghosts) {
            gr.drawAnimatedActor(ghost);
        }
        gr.drawAnimatedActor(msPacMan);
        // might be PacManXXL renderer!
        if (gr instanceof ArcadeMsPacMan_GameRenderer r) {
            r.drawMsPacManMidwayCopyright(tiles2Px(6), tiles2Px(28), COLOR_RED, font);
        }
        gr.drawText("CREDIT %2d".formatted(THE_GAME_CONTROLLER.credit), COLOR_WHITE, font, 2 * TS, sizeInPx().y() - 2);
        gr.drawLevelCounter(THE_GAME_CONTEXT, sizeInPx().x() - 4 * TS, sizeInPx().y() - 2 * TS);
    }

    /**
     * 6 of the 96 light bulbs are lightning each frame, shifting counter-clockwise every tick.
     * <p>
     * The bulbs on the left border however are switched off every second frame. This is
     * probably a bug in the original Arcade game.
     * </p>
     */
    private void drawMarquee() {
        int t = (int) (marqueeTimer.tickCount() % BULB_COUNT);
        for (int i = 0; i < BULB_COUNT; ++i) { drawBulb(i, false); }
        for (int b = 0; b < 6; ++b) { drawBulb((t + b * ACTIVE_BULBS_DIST) % BULB_COUNT, true); }
        for (int i = 81; i < BULB_COUNT; i += 2) { drawBulb(i, false); } // simulates bug
    }

    private void drawBulb(int i, boolean on) {
        int x, y;
        if (i <= 33)      { x = MARQUEE_XMIN + 4 * i; y = MARQUEE_YMAX; } // lower edge left-to-right
        else if (i <= 48) { x = MARQUEE_XMAX;         y = 4 * (70 - i); } // right edge bottom-to-top
        else if (i <= 81) { x = 4 * (96 - i);         y = MARQUEE_YMIN; } // upper edge right-to-left
        else              { x = MARQUEE_XMIN;         y = 4 * (i - 59); } // left edge top-to-bottom
        gr.ctx().setFill(on ? COLOR_BULB_ON : COLOR_BULB_OFF);
        gr.ctx().fillRect(scaled(x), scaled(y), scaled(2), scaled(2));
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
                intro.ghostID = 0;
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
                    if (intro.ghostID == 3) {
                        intro.sceneController.changeState(MS_PACMAN_MARCHING_IN);
                    } else {
                        ++intro.ghostID;
                    }
                }
            }

            boolean letGhostMarchIn(IntroScene intro) {
                Ghost ghost = intro.ghosts[intro.ghostID];
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
                    int endPositionY = TOP_Y + intro.ghostID * 16;
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
                if (intro.msPacMan.posX() <= STOP_X_MSPAC) {
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
                if (timer.atSecond(2.0) && !THE_GAME_CONTEXT.game().canStartNewGame()) {
                    THE_GAME_CONTROLLER.changeState(GameState.STARTING_GAME); // demo level
                } else if (timer.atSecond(5)) {
                    THE_GAME_CONTROLLER.changeState(GameState.SETTING_OPTIONS);
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