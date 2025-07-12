/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel.createGhost;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel.createMsPacMan;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.ui.GameUI.GLOBAL_ACTION_BINDINGS;
import static de.amr.pacmanfx.ui.GameUI.theUI;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 */
public class ArcadeMsPacMan_IntroScene extends GameScene2D {

    private static final int TITLE_X          = TS * 10;
    private static final int TITLE_Y          = TS * 8;
    private static final int TOP_Y            = TS * 11;
    private static final int STOP_X_GHOST     = TS * 6 - 4;
    private static final int STOP_X_MS_PACMAN = TS * 15 + 2;

    private static final float ACTOR_SPEED = 1.11f;

    private static final String TITLE = "\"MS PAC-MAN\"";
    private static final String[] GHOST_NAMES = { "BLINKY", "PINKY", "INKY", "SUE" };
    private static final Color[] GHOST_COLORS = { ARCADE_RED, ARCADE_PINK, ARCADE_CYAN, ARCADE_ORANGE };

    private final StateMachine<SceneState, ArcadeMsPacMan_IntroScene> sceneController;

    private MidwayCopyright copyright;
    private Marquee marquee;
    private Pac msPacMan;
    private Ghost[] ghosts;
    private byte presentedGhostCharacter;
    private int numTicksBeforeRising;

    public ArcadeMsPacMan_IntroScene(GameContext gameContext) {
        super(gameContext);
        sceneController = new StateMachine<>(SceneState.values()) {
            @Override
            public ArcadeMsPacMan_IntroScene context() {
                return ArcadeMsPacMan_IntroScene.this;
            }
        };
    }

    @Override
    public void doInit() {
        gameContext.theGame().hud().showCredit(true);
        gameContext.theGame().hud().showScore(true);
        gameContext.theGame().hud().showLevelCounter(true);
        gameContext.theGame().hud().showLivesCounter(false);

        actionBindings.bind(ACTION_ARCADE_INSERT_COIN, GLOBAL_ACTION_BINDINGS);
        actionBindings.bind(ACTION_ARCADE_START_GAME, GLOBAL_ACTION_BINDINGS);
        actionBindings.bind(ACTION_TEST_CUT_SCENES, GLOBAL_ACTION_BINDINGS);
        actionBindings.bind(ACTION_TEST_LEVELS_BONI, GLOBAL_ACTION_BINDINGS);
        actionBindings.bind(ACTION_TEST_LEVELS_TEASERS, GLOBAL_ACTION_BINDINGS);

        copyright = new MidwayCopyright();
        copyright.setPosition(TS * 6, TS * 28);
        copyright.setColor(ARCADE_RED);
        copyright.setFont(scaledArcadeFont8());
        copyright.show();

        marquee = new Marquee(132, 60, 96, 6, 16);
        marquee.setPosition(60, 88);
        marquee.setBulbOffColor(ARCADE_RED);
        marquee.setBulbOnColor(ARCADE_WHITE);
        marquee.show();

        msPacMan = createMsPacMan(null);
        ghosts = new Ghost[] {
            createGhost(null, RED_GHOST_SHADOW),
            createGhost(null, PINK_GHOST_SPEEDY),
            createGhost(null, CYAN_GHOST_BASHFUL),
            createGhost(null, ORANGE_GHOST_POKEY)
        };
        presentedGhostCharacter = RED_GHOST_SHADOW;
        numTicksBeforeRising = 0;

        msPacMan.setAnimations(theUI().configuration().createPacAnimations(msPacMan));
        msPacMan.selectAnimation(ANIM_PAC_MUNCHING);
        for (Ghost ghost : ghosts) {
            ghost.setAnimations(theUI().configuration().createGhostAnimations(ghost));
            ghost.selectAnimation(ANIM_GHOST_NORMAL);
        }
        sceneController.restart(SceneState.STARTING);
    }

    @Override
    protected void doEnd() {
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
        theUI().theSound().play(SoundID.COIN_INSERTED);
    }

    @Override
    public void drawSceneContent() {
        ctx().setFont(scaledArcadeFont8());
        gr().fillTextAtScaledPosition(TITLE, ARCADE_ORANGE, TITLE_X, TITLE_Y);
        gr().drawActor(marquee);
        for (Ghost ghost : ghosts) { gr().drawActor(ghost); }
        gr().drawActor(msPacMan);
        switch (sceneController.state()) {
            case GHOSTS_MARCHING_IN -> {
                String ghostName = GHOST_NAMES[presentedGhostCharacter];
                Color ghostColor = GHOST_COLORS[presentedGhostCharacter];
                if (presentedGhostCharacter == RED_GHOST_SHADOW) {
                    gr().fillTextAtScaledPosition("WITH", ARCADE_WHITE, TITLE_X, TOP_Y + tiles_to_px(3));
                }
                double x = TITLE_X + (ghostName.length() < 4 ? tiles_to_px(4) : tiles_to_px(3));
                double y = TOP_Y + tiles_to_px(6);
                gr().fillTextAtScaledPosition(ghostName, ghostColor, x, y);
            }
            case MS_PACMAN_MARCHING_IN, READY_TO_PLAY -> {
                gr().fillTextAtScaledPosition("STARRING", ARCADE_WHITE, TITLE_X, TOP_Y + tiles_to_px(3));
                gr().fillTextAtScaledPosition("MS PAC-MAN", ARCADE_YELLOW, TITLE_X, TOP_Y + tiles_to_px(6));
            }
        }
        gr().drawActor(copyright);
    }

    // Scene controller FSM

    private enum SceneState implements FsmState<ArcadeMsPacMan_IntroScene> {

        STARTING {
            @Override
            public void onEnter(ArcadeMsPacMan_IntroScene scene) {
                scene.marquee.timer().restartIndefinitely();
                scene.msPacMan.setPosition(TS * 31, TS * 20);
                scene.msPacMan.setMoveDir(Direction.LEFT);
                scene.msPacMan.setSpeed(ACTOR_SPEED);
                scene.msPacMan.setVisible(true);
                scene.msPacMan.playAnimation(ANIM_PAC_MUNCHING);
                for (Ghost ghost : scene.ghosts) {
                    ghost.setPosition(TS * 33.5f, TS * 20);
                    ghost.setMoveAndWishDir(Direction.LEFT);
                    ghost.setSpeed(ACTOR_SPEED);
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setVisible(true);
                    ghost.playAnimation(ANIM_GHOST_NORMAL);
                }
                scene.presentedGhostCharacter = RED_GHOST_SHADOW;
            }

            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                scene.marquee.timer().doTick();
                if (sceneTimer.atSecond(1)) {
                    scene.sceneController.changeState(GHOSTS_MARCHING_IN);
                }
            }
        },

        GHOSTS_MARCHING_IN {
            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                scene.marquee.timer().doTick();
                boolean atEndPosition = letGhostWalkIn(scene);
                if (atEndPosition) {
                    if (scene.presentedGhostCharacter == ORANGE_GHOST_POKEY) {
                        scene.sceneController.changeState(MS_PACMAN_MARCHING_IN);
                    } else {
                        ++scene.presentedGhostCharacter;
                    }
                }
            }

            boolean letGhostWalkIn(ArcadeMsPacMan_IntroScene scene) {
                Ghost ghost = scene.ghosts[scene.presentedGhostCharacter];
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
                    int endPositionY = TOP_Y + scene.presentedGhostCharacter * 16 + 1;
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
                scene.marquee.timer().doTick();
                scene.msPacMan.move();
                if (scene.msPacMan.x() <= STOP_X_MS_PACMAN) {
                    scene.msPacMan.setSpeed(0);
                    scene.msPacMan.resetAnimation();
                    scene.sceneController.changeState(READY_TO_PLAY);
                }
            }
        },

        READY_TO_PLAY {
            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                scene.marquee.timer().doTick();
                if (sceneTimer.atSecond(2.0) && !scene.gameContext.theGame().canStartNewGame()) {
                    scene.gameContext.theGameController().changeGameState(GameState.STARTING_GAME); // demo level
                } else if (sceneTimer.atSecond(5)) {
                    scene.gameContext.theGameController().changeGameState(GameState.SETTING_OPTIONS);
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