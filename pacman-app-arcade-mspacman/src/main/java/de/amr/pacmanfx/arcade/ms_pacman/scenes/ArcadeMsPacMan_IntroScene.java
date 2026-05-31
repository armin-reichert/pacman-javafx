/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateMachine;
import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationSet;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameState;
import de.amr.pacmanfx.event.CreditAddedEvent;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.gamescene.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;

import java.util.List;

import static de.amr.pacmanfx.core.Globals.*;
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

    public ArcadeMsPacMan_IntroScene(GameUI ui) {
        super(ui);

        final var gameEventHandler = new GameScene.DefaultGameEventHandler(this) {
            @Override
            public void onCreditAdded(CreditAddedEvent e) {
                services().currentSoundEffects().ifPresent(GameSoundEffects::playCoinInsertedSound);
            }
        };
        setGameEventHandler(gameEventHandler);

        sceneController = new StateMachine<>(this, List.of(SceneState.values()));
    }

    @Override
    public void onActivate(UIConfig uiConfig) {
        final SpriteAnimationSet spriteAnimationSet = ui.access().sprites().animationSet();

        ui.access().sounds().playVoice(GameUI_Constants.VOICE_EXPLAIN_GAME_START);

        actionBindings.registerAllBindings(ArcadePacMan_UIConfig.GAME_START_ACTION_BINDINGS);
        actionBindings.registerAllBindings(GameUI_Constants.SCENE_TESTS_BINDINGS);

        marquee = new Marquee(60, 88, 132, 60, 96, 6, 16);
        marquee.setBulbOffColor(ARCADE_RED);
        marquee.setBulbOnColor(ARCADE_WHITE);

        msPacMan = ArcadeMsPacMan_GameModel.createMsPacMan();
        msPacMan.setAnimations(uiConfig.createPacAnimations(spriteAnimationSet));
        msPacMan.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);

        ghosts = List.of(
            uiConfig.createGhostWithAnimations(spriteAnimationSet, RED_GHOST_SHADOW),
            uiConfig.createGhostWithAnimations(spriteAnimationSet, PINK_GHOST_SPEEDY),
            uiConfig.createGhostWithAnimations(spriteAnimationSet, CYAN_GHOST_BASHFUL),
            uiConfig.createGhostWithAnimations(spriteAnimationSet, ORANGE_GHOST_POKEY)
        );

        presentedGhostPersonality = RED_GHOST_SHADOW;
        numTicksBeforeRising = 0;

        sceneController.restartState(SceneState.STARTING);
    }

    @Override
    public void onDeactivate() {
        ui.access().sounds().stopAndDisposeVoice();
    }

    @Override
    public void onTick(GameClock clock) {
        sceneController.update();
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
                scene.msPacMan.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
                scene.msPacMan.animations().playSelected();
                for (Ghost ghost : scene.ghosts) {
                    ghost.setPosition(TS * 33.5f, TS * 20);
                    ghost.setMoveDir(Direction.LEFT);
                    ghost.setWishDir(Direction.LEFT);
                    ghost.setSpeed(ACTOR_SPEED);
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setVisible(true);
                    ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
                    ghost.animations().playSelected();
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
                        ghost.animations().stopSelected();
                        ghost.animations().resetSelected();
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
                    scene.msPacMan.animations().resetSelected();
                    scene.sceneController.enterState(READY_TO_PLAY);
                }
            }
        },

        READY_TO_PLAY {
            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                final Game game = scene.services().currentGame();
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