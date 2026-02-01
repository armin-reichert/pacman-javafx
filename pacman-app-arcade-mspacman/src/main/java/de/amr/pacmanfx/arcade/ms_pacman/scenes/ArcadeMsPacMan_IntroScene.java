/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState;
import de.amr.pacmanfx.event.CreditAddedEvent;
import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.AnimationManager;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Resources;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.sound.SoundID;

import java.util.Collections;
import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.ArcadePalette.ARCADE_RED;
import static de.amr.pacmanfx.ui.ArcadePalette.ARCADE_WHITE;

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

    public final StateMachine<ArcadeMsPacMan_IntroScene> sceneController;

    private Marquee marquee;
    private Pac msPacMan;
    private List<Ghost> ghosts;
    private byte presentedGhostCharacter;
    private int numTicksBeforeRising;

    public ArcadeMsPacMan_IntroScene() {
        sceneController = new StateMachine<>(this, List.of(SceneState.values()));
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
        ui.voicePlayer().play(GameUI_Resources.VOICE_EXPLAIN_GAME_START);

        game.hud().credit(true).score(true).levelCounter(true).livesCounter(false).show();

        actionBindings.registerAllBindingsFrom(ArcadePacMan_UIConfig.DEFAULT_BINDINGS);
        actionBindings.registerAllBindingsFrom(GameUI.SCENE_TESTS_BINDINGS);

        marquee = new Marquee(60, 88, 132, 60, 96, 6, 16);
        marquee.setBulbOffColor(ARCADE_RED);
        marquee.setBulbOnColor(ARCADE_WHITE);

        final UIConfig uiConfig = ui.currentConfig();

        msPacMan = ArcadeMsPacMan_GameModel.createMsPacMan();
        msPacMan.setAnimationManager(uiConfig.createPacAnimations());
        msPacMan.selectAnimation(Pac.AnimationID.PAC_MUNCHING);

        ghosts = List.of(
            uiConfig.createGhostWithAnimations(RED_GHOST_SHADOW),
            uiConfig.createGhostWithAnimations(PINK_GHOST_SPEEDY),
            uiConfig.createGhostWithAnimations(CYAN_GHOST_BASHFUL),
            uiConfig.createGhostWithAnimations(ORANGE_GHOST_POKEY)
        );

        presentedGhostCharacter = RED_GHOST_SHADOW;
        numTicksBeforeRising = 0;

        sceneController.restart(SceneState.STARTING);
    }

    @Override
    protected void doEnd(Game game) {
        ui.voicePlayer().stop();
    }

    @Override
    public void update(Game game) {
        sceneController.update();
    }

    @Override
    public void onCreditAdded(CreditAddedEvent e) {
        ui.soundManager().play(SoundID.COIN_INSERTED);
    }

    // Scene controller FSM

    public enum SceneState implements StateMachine.State<ArcadeMsPacMan_IntroScene> {

        STARTING {
            @Override
            public void onEnter(ArcadeMsPacMan_IntroScene scene) {
                scene.marquee.timer().restartIndefinitely();
                scene.msPacMan.setPosition(TS * 31, TS * 20);
                scene.msPacMan.setMoveDir(Direction.LEFT);
                scene.msPacMan.setSpeed(ACTOR_SPEED);
                scene.msPacMan.setVisible(true);
                scene.msPacMan.playAnimation(Pac.AnimationID.PAC_MUNCHING);
                for (Ghost ghost : scene.ghosts) {
                    ghost.setPosition(TS * 33.5f, TS * 20);
                    ghost.setMoveDir(Direction.LEFT);
                    ghost.setWishDir(Direction.LEFT);
                    ghost.setSpeed(ACTOR_SPEED);
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setVisible(true);
                    ghost.playAnimation(Ghost.AnimationID.GHOST_NORMAL);
                }
                scene.presentedGhostCharacter = RED_GHOST_SHADOW;
            }

            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                scene.marquee.timer().doTick();
                if (sceneTimer.atSecond(1)) {
                    scene.sceneController.enterState(GHOSTS_MARCHING_IN);
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
                        scene.sceneController.enterState(MS_PACMAN_MARCHING_IN);
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
                    scene.sceneController.enterState(READY_TO_PLAY);
                }
            }
        },

        READY_TO_PLAY {
            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                final Game game = scene.gameContext().currentGame();
                scene.marquee.timer().doTick();
                if (sceneTimer.atSecond(2.0) && !game.canStartNewGame()) {
                    game.control().enterState(GameState.STARTING_GAME_OR_LEVEL); // demo level
                } else if (sceneTimer.atSecond(5)) {
                    game.control().enterState(GameState.SETTING_OPTIONS_FOR_START);
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