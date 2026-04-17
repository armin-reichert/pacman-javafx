/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameState;
import de.amr.pacmanfx.event.CreditAddedEvent;
import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Resources;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;

import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_RED;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;

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

    public Marquee marquee;
    public Pac msPacMan;
    public List<Ghost> ghosts;
    public byte presentedGhostPersonality;

    private int numTicksBeforeRising;

    public ArcadeMsPacMan_IntroScene() {
        sceneController = new StateMachine<>(this, List.of(SceneState.values()));
    }

    @Override
    public void onStart() {
        ui.voicePlayer().playVoice(GameUI_Resources.VOICE_EXPLAIN_GAME_START);

        actionBindings.bindAll(ArcadePacMan_UIConfig.GAME_START_ACTION_BINDINGS);
        actionBindings.bindAll(GameUI.SCENE_TESTS_BINDINGS);

        marquee = new Marquee(60, 88, 132, 60, 96, 6, 16);
        marquee.setBulbOffColor(ARCADE_RED);
        marquee.setBulbOnColor(ARCADE_WHITE);

        final UIConfig uiConfig = ui.currentConfig();

        msPacMan = ArcadeMsPacMan_GameModel.createMsPacMan();
        msPacMan.setAnimations(uiConfig.createPacAnimations(ui.spriteAnimationDriver()));
        msPacMan.selectAnimation(Pac.AnimationID.PAC_MUNCHING);

        ghosts = List.of(
            uiConfig.createGhostWithAnimations(ui.spriteAnimationDriver(), RED_GHOST_SHADOW),
            uiConfig.createGhostWithAnimations(ui.spriteAnimationDriver(), PINK_GHOST_SPEEDY),
            uiConfig.createGhostWithAnimations(ui.spriteAnimationDriver(), CYAN_GHOST_BASHFUL),
            uiConfig.createGhostWithAnimations(ui.spriteAnimationDriver(), ORANGE_GHOST_POKEY)
        );

        presentedGhostPersonality = RED_GHOST_SHADOW;
        numTicksBeforeRising = 0;

        sceneController.restartState(SceneState.STARTING);
    }

    @Override
    protected void onEnd() {
        ui.voicePlayer().stopVoice();
    }

    @Override
    protected void onTick(long tick) {
        sceneController.update();
    }

    @Override
    public void onCreditAdded(CreditAddedEvent e) {
        soundEffects().ifPresent(GameSoundEffects::playCoinInsertedSound);
    }

    // Scene controller FSM

    public enum SceneState implements State<ArcadeMsPacMan_IntroScene> {

        STARTING {
            @Override
            public void onEnter(ArcadeMsPacMan_IntroScene scene) {
                scene.marquee.timer().restartIndefinitely();
                scene.msPacMan.setPosition(TS * 31, TS * 20);
                scene.msPacMan.setMoveDir(Direction.LEFT);
                scene.msPacMan.setSpeed(ACTOR_SPEED);
                scene.msPacMan.setVisible(true);
                scene.msPacMan.selectAnimation(Pac.AnimationID.PAC_MUNCHING);
                scene.msPacMan.playAnimation();
                for (Ghost ghost : scene.ghosts) {
                    ghost.setPosition(TS * 33.5f, TS * 20);
                    ghost.setMoveDir(Direction.LEFT);
                    ghost.setWishDir(Direction.LEFT);
                    ghost.setSpeed(ACTOR_SPEED);
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setVisible(true);
                    ghost.selectAnimation(Ghost.AnimationID.GHOST_NORMAL);
                    ghost.playAnimation();
                }
                scene.presentedGhostPersonality = RED_GHOST_SHADOW;
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
                    if (scene.presentedGhostPersonality == ORANGE_GHOST_POKEY) {
                        scene.sceneController.enterState(MS_PACMAN_MARCHING_IN);
                    } else {
                        ++scene.presentedGhostPersonality;
                    }
                }
            }

            boolean letGhostWalkIn(ArcadeMsPacMan_IntroScene scene) {
                Ghost ghost = scene.ghosts.get(scene.presentedGhostPersonality);
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
                    int endPositionY = TOP_Y + scene.presentedGhostPersonality * 16 + 1;
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
                    scene.sceneController.enterState(READY_TO_PLAY);
                }
            }
        },

        READY_TO_PLAY {
            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                final Game game = scene.gameContext().game();
                scene.marquee.timer().doTick();
                if (sceneTimer.atSecond(2.0) && !game.canStartNewGame()) {
                    game.flow().enterState(Arcade_GameState.STARTING_GAME_OR_LEVEL); // demo level
                } else if (sceneTimer.atSecond(5)) {
                    game.flow().enterState(Arcade_GameState.PREPARING_GAME_START);
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