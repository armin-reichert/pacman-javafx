/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.PacManGames_Actions;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.arcade.ArcadePalette.*;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.ui.PacManGames_Env.theSound;
import static de.amr.pacmanfx.ui.PacManGames_Env.theUI;

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

    private static final String[] GHOST_NAMES = { "BLINKY", "PINKY", "INKY", "SUE" };
    private static final Color[] GHOST_COLORS = { ARCADE_RED, ARCADE_PINK, ARCADE_CYAN, ARCADE_ORANGE };

    private final StateMachine<SceneState, ArcadeMsPacMan_IntroScene> sceneController;

    private Pac msPacMan;
    private Ghost[] ghosts;
    private TickTimer marqueeTimer;
    private byte presented;
    private int numTicksBeforeRising;

    public ArcadeMsPacMan_IntroScene() {
        sceneController = new StateMachine<>(SceneState.values()) {
            @Override
            public ArcadeMsPacMan_IntroScene context() {
                return ArcadeMsPacMan_IntroScene.this;
            }
        };
    }

    @Override
    public void doInit() {
        theGame().scoreManager().setScoreVisible(true);

        bindToDefaultKeys(PacManGames_Actions.ARCADE_INSERT_COIN);
        bindToDefaultKeys(PacManGames_Actions.ARCADE_START_GAME);
        bindToDefaultKeys(PacManGames_Actions.TEST_CUT_SCENES);
        bindToDefaultKeys(PacManGames_Actions.TEST_LEVELS_BONI);
        bindToDefaultKeys(PacManGames_Actions.TEST_LEVELS_TEASERS);

        msPacMan = createMsPacMan();
        ghosts = new Ghost[] { createRedGhost(), createPinkGhost(), createCyanGhost(), createOrangeGhost() };
        marqueeTimer = new TickTimer("marquee-timer");
        presented = RED_GHOST_SHADOW;
        numTicksBeforeRising = 0;

        var spriteSheet = (ArcadeMsPacMan_SpriteSheet) theUI().configs().current().spriteSheet();
        msPacMan.setAnimations(new ArcadeMsPacMan_PacAnimationMap(spriteSheet));
        msPacMan.selectAnimation(ANIM_PAC_MUNCHING);
        for (Ghost ghost : ghosts) {
            ghost.setAnimations(new ArcadeMsPacMan_GhostAnimationMap(spriteSheet, ghost.personality()));
            ghost.selectAnimation(ANIM_GHOST_NORMAL);
        }
        sceneController.restart(SceneState.STARTING);
    }

    @Override
    protected void doEnd() {
        theSound().stopVoice();
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
        theSound().playInsertCoinSound();
    }

    @Override
    public void drawSceneContent() {
        drawMarquee();
        gr().fillText("\"MS PAC-MAN\"", ARCADE_ORANGE, normalArcadeFont(), TITLE_X, TITLE_Y);
        switch (sceneController.state()) {
            case GHOSTS_MARCHING_IN -> {
                String ghostName = GHOST_NAMES[presented];
                if (presented == RED_GHOST_SHADOW) {
                    gr().fillText("WITH", ARCADE_WHITE, normalArcadeFont(), TITLE_X, TOP_Y + tiles_to_px(3));
                }
                double x = TITLE_X + tiles_to_px(3);
                if (ghostName.length() < 4) x += TS;
                gr().fillText(ghostName, GHOST_COLORS[presented], normalArcadeFont(), x, TOP_Y + tiles_to_px(6));
            }
            case MS_PACMAN_MARCHING_IN, READY_TO_PLAY -> {
                gr().fillText("STARRING", ARCADE_WHITE, normalArcadeFont(), TITLE_X, TOP_Y + tiles_to_px(3));
                gr().fillText("MS PAC-MAN", ARCADE_YELLOW, normalArcadeFont(), TITLE_X, TOP_Y + tiles_to_px(6));
            }
        }
        for (Ghost ghost : ghosts) { gr().drawActor(ghost); }
        gr().drawActor(msPacMan);
        if (gr() instanceof ArcadeMsPacMan_GameRenderer r) { // might also be vector renderer!
            r.drawMsPacManCopyrightAtTile(ARCADE_RED, normalArcadeFont(), 6, 28);
        }
        gr().fillText("CREDIT %2d".formatted(theCoinMechanism().numCoins()), scoreColor(), normalArcadeFont(), 2 * TS, sizeInPx().y() - 2);
        gr().drawLevelCounter(theGame().levelCounter(), sizeInPx());
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
        gr().ctx().setFill(on ? COLOR_BULB_ON : COLOR_BULB_OFF);
        gr().ctx().fillRect(scaled(x), scaled(y), scaled(2), scaled(2));
    }

    // Scene controller FSM

    private enum SceneState implements FsmState<ArcadeMsPacMan_IntroScene> {

        STARTING {
            @Override
            public void onEnter(ArcadeMsPacMan_IntroScene scene) {
                scene.marqueeTimer.restartIndefinitely();
                scene.msPacMan.setPosition(TS * 31, TS * 20);
                scene.msPacMan.setMoveDir(Direction.LEFT);
                scene.msPacMan.setSpeed(SPEED);
                scene.msPacMan.setVisible(true);
                scene.msPacMan.playAnimation(ANIM_PAC_MUNCHING);
                for (Ghost ghost : scene.ghosts) {
                    ghost.setPosition(TS * 33.5f, TS * 20);
                    ghost.setMoveAndWishDir(Direction.LEFT);
                    ghost.setSpeed(SPEED);
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setVisible(true);
                    ghost.playAnimation(ANIM_GHOST_NORMAL);
                }
                scene.presented = RED_GHOST_SHADOW;
            }

            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                scene.marqueeTimer.doTick();
                if (sceneTimer.atSecond(1)) {
                    scene.sceneController.changeState(GHOSTS_MARCHING_IN);
                }
            }
        },

        GHOSTS_MARCHING_IN {
            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                scene.marqueeTimer.doTick();
                boolean atEndPosition = letGhostWalkIn(scene);
                if (atEndPosition) {
                    if (scene.presented == ORANGE_GHOST_POKEY) {
                        scene.sceneController.changeState(MS_PACMAN_MARCHING_IN);
                    } else {
                        ++scene.presented;
                    }
                }
            }

            boolean letGhostWalkIn(ArcadeMsPacMan_IntroScene scene) {
                Ghost ghost = scene.ghosts[scene.presented];
                if (ghost.moveDir() == Direction.LEFT) {
                    if (ghost.x() <= STOP_X_GHOST) {
                        ghost.setX(STOP_X_GHOST);
                        ghost.setMoveAndWishDir(Direction.UP);
                        scene.numTicksBeforeRising = 2;
                    } else {
                        ghost.move();
                    }
                }
                else if (ghost.moveDir() == Direction.UP) {
                    int endPositionY = TOP_Y + scene.presented * 16;
                    if (scene.numTicksBeforeRising > 0) {
                        scene.numTicksBeforeRising--;
                    }
                    else if (ghost.y() <= endPositionY) {
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
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                scene.marqueeTimer.doTick();
                scene.msPacMan.move();
                if (scene.msPacMan.x() <= STOP_X_MSPAC) {
                    scene.msPacMan.setSpeed(0);
                    scene.msPacMan.resetAnimation();
                    scene.sceneController.changeState(READY_TO_PLAY);
                }
            }
        },

        READY_TO_PLAY {
            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                scene.marqueeTimer.doTick();
                if (sceneTimer.atSecond(2.0) && !theGame().canStartNewGame()) {
                    theGameController().changeGameState(GameState.STARTING_GAME); // demo level
                } else if (sceneTimer.atSecond(5)) {
                    theGameController().changeGameState(GameState.SETTING_OPTIONS);
                }
            }
        };

        final TickTimer sceneTimer = new TickTimer("Timer-" + name());

        @Override
        public TickTimer timer() {
            return sceneTimer;
        }
    }
}