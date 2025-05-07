/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.fsm.FiniteStateMachine;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.ActorAnimations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui.GameAssets.*;
import static de.amr.games.pacman.ui.Globals.THE_SOUND;
import static de.amr.games.pacman.ui.Globals.THE_UI_CONFIGS;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 *
 * @author Armin Reichert
 */
public class ArcadeMsPacMan_IntroScene extends GameScene2D {

    private static final float SPEED = 1.11f;

    private static final int TOP_Y        = TS * 11 + 1;
    private static final int TITLE_X      = TS * 10;
    private static final int TITLE_Y      = TS * 8;
    private static final int STOP_X_GHOST = TS * 6 - 4;
    private static final int STOP_X_MSPAC = TS * 15 + 2;

    private static final int BULB_COUNT = 96;
    private static final int ACTIVE_BULBS_DIST = 16;
    private static final RectArea MARQUEE = new RectArea(60, 88, 132, 60);

    private static final Color COLOR_BULB_ON  = ARCADE_WHITE;
    private static final Color COLOR_BULB_OFF = ARCADE_RED;

    private static final Color[] COLOR_GHOST = { ARCADE_RED, ARCADE_PINK, ARCADE_CYAN, ARCADE_ORANGE };

    private final FiniteStateMachine<SceneState, ArcadeMsPacMan_IntroScene> sceneController;

    private Pac msPacMan;
    private Ghost[] ghosts;
    private TickTimer marqueeTimer;
    private int ghostID;
    private int waitBeforeRising;

    public ArcadeMsPacMan_IntroScene() {
        sceneController = new FiniteStateMachine<>(SceneState.values()) {
            @Override
            public ArcadeMsPacMan_IntroScene context() {
                return ArcadeMsPacMan_IntroScene.this;
            }
        };
    }

    @Override
    public void bindActions() {
        bindArcadeInsertCoinAction();
        bindArcadeStartGameAction();
        bindStartTestsActions();
    }

    @Override
    public void doInit() {
        game().scoreManager().setScoreVisible(true);

        msPacMan = ArcadeMsPacMan_ActorFactory.createMsPacMan();
        ghosts = new Ghost[] {
            ArcadeMsPacMan_ActorFactory.createRedGhost(),
            ArcadeMsPacMan_ActorFactory.createPinkGhost(),
            ArcadeMsPacMan_ActorFactory.createCyanGhost(),
            ArcadeMsPacMan_ActorFactory.createOrangeGhost()
        };
        marqueeTimer = new TickTimer("marquee-timer");
        ghostID = 0;
        waitBeforeRising = 0;

        var spriteSheet = (ArcadeMsPacMan_SpriteSheet) THE_UI_CONFIGS.current().spriteSheet();
        msPacMan.setAnimations(new ArcadeMsPacMan_PacAnimations(spriteSheet));
        msPacMan.selectAnimation(ActorAnimations.ANIM_PAC_MUNCHING);
        for (Ghost ghost : ghosts) {
            ghost.setAnimations(new ArcadeMsPacMan_GhostAnimations(spriteSheet, ghost.id()));
            ghost.selectAnimation(ActorAnimations.ANIM_GHOST_NORMAL);
        }
        sceneController.restart(SceneState.STARTING);
    }

    @Override
    protected void doEnd() {
        THE_SOUND.stopVoice();
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
        THE_SOUND.playInsertCoinSound();
    }

    @Override
    public void drawSceneContent() {
        final SceneState state = sceneController.state();
        final Font font = arcadeFontScaledTS();
        gr.drawScores(game(), ARCADE_WHITE, font);
        drawMarquee();
        gr.fillTextAtScaledPosition("\"MS PAC-MAN\"", ARCADE_ORANGE, font, TITLE_X, TITLE_Y);
        if (state == SceneState.GHOSTS_MARCHING_IN) {
            String ghostName = ghosts[ghostID].name().toUpperCase();
            double dx = ghostName.length() < 4 ? tiles_to_px(1) : 0;
            if (ghostID == RED_GHOST_ID) {
                gr.fillTextAtScaledPosition("WITH", ARCADE_WHITE, font, TITLE_X, TOP_Y + tiles_to_px(3));
            }
            gr.fillTextAtScaledPosition(ghostName, COLOR_GHOST[ghostID], font, TITLE_X + tiles_to_px(3) + dx, TOP_Y + tiles_to_px(6));
        }
        else if (state == SceneState.MS_PACMAN_MARCHING_IN || state == SceneState.READY_TO_PLAY) {
            gr.fillTextAtScaledPosition("STARRING", ARCADE_WHITE, font, TITLE_X, TOP_Y + tiles_to_px(3));
            gr.fillTextAtScaledPosition("MS PAC-MAN", ARCADE_YELLOW, font, TITLE_X, TOP_Y + tiles_to_px(6));
        }
        for (Ghost ghost : ghosts) {
            gr.drawAnimatedActor(ghost);
        }
        gr.drawAnimatedActor(msPacMan);
        if (gr instanceof ArcadeMsPacMan_GameRenderer r) {
            // might be PacManXXL vector renderer!
            r.drawMidwayCopyright(6, 28, ARCADE_RED, font);
        }
        gr.fillTextAtScaledPosition("CREDIT %2d".formatted(THE_COIN_MECHANISM.numCoins()), ARCADE_WHITE, font, 2 * TS, sizeInPx().y() - 2);
        gr.drawLevelCounter(game().levelCounter(), sizeInPx());
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
        if (i <= 33)      { x = MARQUEE.x() + 4 * i; y = MARQUEE.yMax(); } // lower edge left-to-right
        else if (i <= 48) { x = MARQUEE.xMax();      y = 4 * (70 - i); }   // right edge bottom-to-top
        else if (i <= 81) { x = 4 * (96 - i);        y = MARQUEE.y(); }    // upper edge right-to-left
        else              { x = MARQUEE.x();         y = 4 * (i - 59); }   // left edge top-to-bottom
        gr.ctx().setFill(on ? COLOR_BULB_ON : COLOR_BULB_OFF);
        gr.ctx().fillRect(scaled(x), scaled(y), scaled(2), scaled(2));
    }

    // Scene controller FSM

    private enum SceneState implements FsmState<ArcadeMsPacMan_IntroScene> {

        STARTING {
            @Override
            public void onEnter(ArcadeMsPacMan_IntroScene intro) {
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
            public void onUpdate(ArcadeMsPacMan_IntroScene intro) {
                intro.marqueeTimer.doTick();
                if (timer.atSecond(1)) {
                    intro.sceneController.changeState(GHOSTS_MARCHING_IN);
                }
            }
        },

        GHOSTS_MARCHING_IN {

            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene intro) {
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

            boolean letGhostMarchIn(ArcadeMsPacMan_IntroScene intro) {
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
            public void onUpdate(ArcadeMsPacMan_IntroScene intro) {
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
            public void onUpdate(ArcadeMsPacMan_IntroScene intro) {
                intro.marqueeTimer.doTick();
                if (timer.atSecond(2.0) && !THE_GAME_CONTROLLER.game().canStartNewGame()) {
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