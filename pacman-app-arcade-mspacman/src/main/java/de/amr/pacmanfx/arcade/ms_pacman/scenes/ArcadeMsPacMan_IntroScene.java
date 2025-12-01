/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.model.actors.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_HUDRenderer;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_IntroScene_Renderer;
import de.amr.pacmanfx.arcade.pacman.ArcadeActions;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.action.TestActions;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;

import java.util.Collections;
import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.api.ArcadePalette.ARCADE_RED;
import static de.amr.pacmanfx.ui.api.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.ui.input.Keyboard.bare;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 */
public class ArcadeMsPacMan_IntroScene extends GameScene2D {

    public static final int TITLE_X          = TS * 10;
    public static final int TITLE_Y          = TS * 8;
    public static final int TOP_Y            = TS * 11;
    public static final int STOP_X_GHOST     = TS * 6 - 4;
    public static final int STOP_X_MS_PACMAN = TS * 15 + 2;

    private static final float ACTOR_SPEED = 1.11f;

    public final StateMachine<SceneState, ArcadeMsPacMan_IntroScene> sceneController;

    private ArcadeMsPacMan_HUDRenderer hudRenderer;
    private ArcadeMsPacMan_IntroScene_Renderer sceneRenderer;

    private Marquee marquee;
    private Pac msPacMan;
    private List<Ghost> ghosts;
    private byte presentedGhostCharacter;
    private int numTicksBeforeRising;

    public ArcadeMsPacMan_IntroScene(GameUI ui) {
        super(ui);
        sceneController = new StateMachine<>();
        sceneController.setContext(this);
        sceneController.addStates(SceneState.values());
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        final GameUI_Config uiConfig = ui.currentConfig();

        hudRenderer = configureRenderer(
            (ArcadeMsPacMan_HUDRenderer) uiConfig.createHUDRenderer(canvas));

        sceneRenderer = configureRenderer(
            new ArcadeMsPacMan_IntroScene_Renderer(this, canvas));
    }

    @Override
    public ArcadeMsPacMan_HUDRenderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public ArcadeMsPacMan_IntroScene_Renderer sceneRenderer() {
        return sceneRenderer;
    }

    public Marquee marquee() {
        return marquee;
    }

    public Pac msPacMan() {
        return msPacMan;
    }

    public List<Ghost> ghosts() {
        return Collections.unmodifiableList(ghosts);
    }

    public byte presentedGhostCharacter() {
        return presentedGhostCharacter;
    }

    @Override
    public void doInit(Game game) {
        game.hud().creditVisible(true).scoreVisible(true).levelCounterVisible(true).livesCounterVisible(false);

        actionBindings.addKeyCombination(ArcadeActions.ACTION_INSERT_COIN, bare(KeyCode.DIGIT5));
        actionBindings.addKeyCombination(ArcadeActions.ACTION_INSERT_COIN, bare(KeyCode.NUMPAD5));
        actionBindings.addKeyCombination(ArcadeActions.ACTION_START_GAME, bare(KeyCode.DIGIT1));
        actionBindings.addKeyCombination(ArcadeActions.ACTION_START_GAME, bare(KeyCode.NUMPAD1));

        actionBindings.bind(TestActions.ACTION_CUT_SCENES_TEST, ui.actionBindings());
        actionBindings.bind(TestActions.ACTION_SHORT_LEVEL_TEST, ui.actionBindings());
        actionBindings.bind(TestActions.ACTION_MEDIUM_LEVEL_TEST, ui.actionBindings());

        marquee = new Marquee(60, 88, 132, 60, 96, 6, 16);
        marquee.setBulbOffColor(ARCADE_RED);
        marquee.setBulbOnColor(ARCADE_WHITE);

        final GameUI_Config uiConfig = ui.currentConfig();

        msPacMan = ArcadeMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAnimationManager(uiConfig.createPacAnimations());
        msPacMan.selectAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);

        ghosts = List.of(
            uiConfig.createAnimatedGhost(RED_GHOST_SHADOW),
            uiConfig.createAnimatedGhost(PINK_GHOST_SPEEDY),
            uiConfig.createAnimatedGhost(CYAN_GHOST_BASHFUL),
            uiConfig.createAnimatedGhost(ORANGE_GHOST_POKEY)
        );

        presentedGhostCharacter = RED_GHOST_SHADOW;
        numTicksBeforeRising = 0;

        sceneController.restart(SceneState.STARTING);
    }

    @Override
    protected void doEnd(Game game) {
    }

    @Override
    public void update(Game game) {
        sceneController.update();
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        ui.soundManager().play(SoundID.COIN_INSERTED);
    }

    // Scene controller FSM

    public enum SceneState implements FsmState<ArcadeMsPacMan_IntroScene> {

        STARTING {
            @Override
            public void onEnter(ArcadeMsPacMan_IntroScene scene) {
                scene.marquee.timer().restartIndefinitely();
                scene.msPacMan.setPosition(TS * 31, TS * 20);
                scene.msPacMan.setMoveDir(Direction.LEFT);
                scene.msPacMan.setSpeed(ACTOR_SPEED);
                scene.msPacMan.setVisible(true);
                scene.msPacMan.playAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
                for (Ghost ghost : scene.ghosts) {
                    ghost.setPosition(TS * 33.5f, TS * 20);
                    ghost.setMoveDir(Direction.LEFT);
                    ghost.setWishDir(Direction.LEFT);
                    ghost.setSpeed(ACTOR_SPEED);
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setVisible(true);
                    ghost.playAnimation(CommonAnimationID.ANIM_GHOST_NORMAL);
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
                        ghost.optAnimationManager().ifPresent(am -> {
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
                    scene.msPacMan.optAnimationManager().ifPresent(AnimationManager::reset);
                    scene.sceneController.changeState(READY_TO_PLAY);
                }
            }
        },

        READY_TO_PLAY {
            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                final Game game = scene.context().currentGame();
                scene.marquee.timer().doTick();
                if (sceneTimer.atSecond(2.0) && !scene.context().currentGame().canStartNewGame()) {
                    game.control().changeState(GameState.STARTING_GAME_OR_LEVEL); // demo level
                } else if (sceneTimer.atSecond(5)) {
                    game.control().changeState(GameState.SETTING_OPTIONS_FOR_START);
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