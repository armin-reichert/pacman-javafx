/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.AnimationManager;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.paint.Color;

import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel.createGhost;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel.createMsPacMan;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.ui.CommonGameActions.*;
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

    private MidwayCopyright midwayCopyright;
    private Marquee marquee;
    private Pac msPacMan;
    private List<Ghost> ghosts;
    private byte presentedGhostCharacter;
    private int numTicksBeforeRising;

    public ArcadeMsPacMan_IntroScene(GameUI ui) {
        super(ui);
        sceneController = new StateMachine<>(List.of(SceneState.values())) {
            @Override
            public ArcadeMsPacMan_IntroScene context() {
                return ArcadeMsPacMan_IntroScene.this;
            }
        };
    }

    @Override
    public void doInit() {
        setHudRenderer(ui.currentConfig().createHUDRenderer(canvas, scaling));
        gameContext().game().hudData().credit(true).score(true).levelCounter(true).livesCounter(false);

        actionBindings.assign(ACTION_ARCADE_INSERT_COIN, ui.actionBindings());
        actionBindings.assign(ACTION_ARCADE_START_GAME, ui.actionBindings());
        actionBindings.assign(ACTION_TEST_CUT_SCENES, ui.actionBindings());
        actionBindings.assign(ACTION_TEST_LEVELS_SHORT, ui.actionBindings());
        actionBindings.assign(ACTION_TEST_LEVELS_MEDIUM, ui.actionBindings());

        midwayCopyright = new MidwayCopyright(ui.currentConfig().assets().image("logo.midway"));
        midwayCopyright.setPosition(TS * 6, TS * 28);
        midwayCopyright.show();

        marquee = new Marquee(132, 60, 96, 6, 16);
        marquee.setPosition(60, 88);
        marquee.setBulbOffColor(ARCADE_RED);
        marquee.setBulbOnColor(ARCADE_WHITE);
        marquee.show();

        msPacMan = createMsPacMan();
        ghosts = List.of(
            createGhost(RED_GHOST_SHADOW),
            createGhost(PINK_GHOST_SPEEDY),
            createGhost(CYAN_GHOST_BASHFUL),
            createGhost(ORANGE_GHOST_POKEY)
        );
        presentedGhostCharacter = RED_GHOST_SHADOW;
        numTicksBeforeRising = 0;

        msPacMan.setAnimations(ui.currentConfig().createPacAnimations(msPacMan));
        msPacMan.selectAnimation(ANIM_PAC_MUNCHING);
        for (Ghost ghost : ghosts) {
            AnimationManager animations = ui.currentConfig().createGhostAnimations(ghost);
            ghost.setAnimations(animations);
            animations.select(ANIM_GHOST_NORMAL);
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
        ui.soundManager().play(SoundID.COIN_INSERTED);
    }

    @Override
    public void drawSceneContent() {
        gameRenderer.ctx().setFont(scaledArcadeFont8());
        gameRenderer.fillText(TITLE, ARCADE_ORANGE, TITLE_X, TITLE_Y);
        gameRenderer.drawActor(marquee);
        ghosts.forEach(ghost -> gameRenderer.drawActor(ghost));
        gameRenderer.drawActor(msPacMan);
        switch (sceneController.state()) {
            case GHOSTS_MARCHING_IN -> {
                String ghostName = GHOST_NAMES[presentedGhostCharacter];
                Color ghostColor = GHOST_COLORS[presentedGhostCharacter];
                if (presentedGhostCharacter == RED_GHOST_SHADOW) {
                    gameRenderer.fillText("WITH", ARCADE_WHITE, TITLE_X, TOP_Y + TS(3));
                }
                double x = TITLE_X + (ghostName.length() < 4 ? TS(4) : TS(3));
                double y = TOP_Y + TS(6);
                gameRenderer.fillText(ghostName, ghostColor, x, y);
            }
            case MS_PACMAN_MARCHING_IN, READY_TO_PLAY -> {
                gameRenderer.fillText("STARRING", ARCADE_WHITE, TITLE_X, TOP_Y + TS(3));
                gameRenderer.fillText("MS PAC-MAN", ARCADE_YELLOW, TITLE_X, TOP_Y + TS(6));
            }
        }
        gameRenderer.drawActor(midwayCopyright);
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
                    ghost.setMoveDir(Direction.LEFT);
                    ghost.setWishDir(Direction.LEFT);
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
                Ghost ghost = scene.ghosts.get(scene.presentedGhostCharacter);
                if (ghost.moveDir() == Direction.LEFT) {
                    if (ghost.x() <= STOP_X_GHOST) {
                        ghost.setX(STOP_X_GHOST);
                        ghost.setMoveDir(Direction.UP);
                        ghost.setWishDir(Direction.UP);
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
                        ghost.animations().ifPresent(am -> {
                            am.stop();
                            am.reset();
                        });
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
                    scene.msPacMan.animations().ifPresent(AnimationManager::reset);
                    scene.sceneController.changeState(READY_TO_PLAY);
                }
            }
        },

        READY_TO_PLAY {
            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                scene.marquee.timer().doTick();
                if (sceneTimer.atSecond(2.0) && !scene.gameContext().game().canStartNewGame()) {
                    scene.gameContext().gameController().changeGameState(GamePlayState.STARTING_GAME); // demo level
                } else if (sceneTimer.atSecond(5)) {
                    scene.gameContext().gameController().changeGameState(GamePlayState.SETTING_OPTIONS_FOR_START);
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