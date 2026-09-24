/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.introscene;

import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateMachine;
import de.amr.basics.math.Direction;
import de.amr.basics.timer.Pulse;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.model.GhostPersonality;

import java.util.List;

public class IntroSceneController extends StateMachine<ArcadePacMan_IntroScene> {

    // State STARTING
    public static final int TICK_TITLE_VISIBLE           = 3;
    public static final int TICK_START_PRESENTING_GHOSTS = 60;

    // State PRESENTING_GHOSTS
    public static final int TICK_GHOST_SPRITE_VISIBLE    =   0;
    public static final int TICK_GHOST_CHARACTER_VISIBLE =  60;
    public static final int TICK_GHOST_NICKNAME_VISIBLE  =  90;
    public static final int TICK_GHOST_PRESENT_NEXT      = 120;
    public static final int TICK_GHOST_PRESENTATION_END  = 150;

    // State SHOWING_POINTS
    public static final int TICK_SHOW_POINTS_DURATION = 60;

    // State CHASING_PAC_MAN
    public static final float CHASING_SPEED = 1.1f;
    public static final float GHOST_FRIGHTENED_SPEED = 0.5f;

    public static final int TICK_PAC_MAN_APPEARS = 60;
    public static final int TICK_PAC_MAN_REACHES_ENERGIZER = 230;
    public static final int TICK_PAC_MAN_MOVES_AGAIN = TICK_PAC_MAN_REACHES_ENERGIZER + 4;
    public static final int TICK_CHASING_PAC_MAN_END = TICK_PAC_MAN_REACHES_ENERGIZER + 8;

    // State CHASING_GHOSTS
    public static final int GHOST_EATING_TICKS = 50;

    public static final int TICK_CHASING_GHOSTS_END = 270;

    // READY_TO_PLAY
    public static final int TICK_START_DEMO_LEVEL = 60;

    public IntroSceneController() {
        super(List.of(SceneState.values()));
        for (var state : SceneState.values()) {
            state.controller = this;
        }
    }

    public enum SceneState implements State<ArcadePacMan_IntroScene> {

        STARTING {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                scene.initState();
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (timer.tickCount() == TICK_TITLE_VISIBLE) {
                    scene.view.titleTextView.show();
                } else if (timer.tickCount() == TICK_START_PRESENTING_GHOSTS) {
                    controller.enterState(scene, PRESENTING_GHOSTS);
                }
            }
        },

        PRESENTING_GHOSTS {
            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                final int t = (int) timer.tickCount();
                if (t > TICK_GHOST_PRESENTATION_END) {
                    return;
                }
                switch (t) {
                    case TICK_GHOST_SPRITE_VISIBLE    -> scene.view.ghostImageViews[scene.ghostIndex].show();
                    case TICK_GHOST_CHARACTER_VISIBLE -> scene.view.ghostCharacterDisplays[scene.ghostIndex].show();
                    case TICK_GHOST_NICKNAME_VISIBLE  -> scene.view.ghostNicknameDisplays[scene.ghostIndex].show();
                    case TICK_GHOST_PRESENT_NEXT      -> presentNextGhost(scene);
                    case TICK_GHOST_PRESENTATION_END  -> controller.enterState(scene, SHOWING_POINTS);
                }
            }

            private void presentNextGhost(ArcadePacMan_IntroScene scene) {
                if (scene.ghostIndex < 3) {
                    scene.ghostIndex += 1;
                    timer.resetToIndefiniteDuration();
                }
            }
        },

        SHOWING_POINTS {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                scene.view.pulse.stopAndReset();
                scene.view.createAndShowPointsEnergizer();
                scene.view.pellet.show();
                scene.view.text10.show();
                scene.view.text10Pts.show();
                scene.view.text50.show();
                scene.view.text50Pts.show();
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (timer.tickCount() == TICK_SHOW_POINTS_DURATION) {
                    controller.enterState(scene, CHASING_PAC_MAN);
                }
                updateEnergizers(scene);
            }
        },

        CHASING_PAC_MAN {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                timer.restartTicks(TICK_CHASING_PAC_MAN_END);
                scene.view.pacMan.hide();
                scene.view.copyrightText.show();
                scene.view.createAndShowTargetEnergizer();
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                final GameSystems systems = scene.game().playConfig().systems();

                final long tick = timer.tickCount();
                if (tick == TICK_PAC_MAN_APPEARS) {
                    scene.startChasingPacMan(scene.game());
                }
                else if (tick == TICK_PAC_MAN_REACHES_ENERGIZER) {
                    scene.turnCardsStopPacMan(scene.game());
                    scene.view.removeTargetEnergizer();
                }
                else if (tick == TICK_PAC_MAN_MOVES_AGAIN) {
                    scene.turnCardsRestartPacMan(systems);
                }
                else if (tick == TICK_CHASING_PAC_MAN_END) {
                    controller.enterState(scene, CHASING_GHOSTS);
                    return;
                }
                updateEnergizers(scene);
                scene.chasePacMan(tick);
            }
        },

        CHASING_GHOSTS {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                final GameSystems systems = scene.game().playConfig().systems();

                timer.restartTicks(TICK_CHASING_GHOSTS_END);

                scene.lastGhostEatenTick = timer.tickCount();
                scene.numGhostsEaten = 0;

                systems.navigator().setMoveDir(scene.view.pacMan, Direction.RIGHT);
                systems.navigator().setSpeed(scene.view.pacMan, CHASING_SPEED);
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                final long tick = timer.tickCount();
                if (tick == TICK_CHASING_GHOSTS_END) {
                    scene.view.pacMan.hide();
                    controller.enterState(scene, WAIT_FOR_DEMO_LEVEL);
                } else {
                    updateEnergizers(scene);
                    scene.chaseGhosts(scene.game(), tick);
                }
            }
        },

        WAIT_FOR_DEMO_LEVEL {
            @Override
            public void onEnter(ArcadePacMan_IntroScene context) {
                timer.restartTicks(TICK_START_DEMO_LEVEL);
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                final GameContext game = scene.game();

                scene.view.pulse.triggerPulse();
                updateEnergizers(scene);

                if (timer.tickCount() == TICK_START_DEMO_LEVEL) {
                    scene.view.ghosts[GhostPersonality.ORANGE_GHOST_POKEY.ordinal()].hide();
                    scene.gameFlow().enterGameState(game, CommonGameStateID.GAME_OR_LEVEL_STARTING);
                }
            }
        };

        void updateEnergizers(ArcadePacMan_IntroScene scene) {
            if (scene.view.pulse.state() == Pulse.State.ON) {
                if (scene.view.pointsEnergizer != null) {
                    scene.view.pointsEnergizer.show();
                }
                if (scene.view.targetEnergizer != null) {
                    scene.view.targetEnergizer.show();
                }
            }
            else {
                if (scene.view.pointsEnergizer != null) {
                    scene.view.pointsEnergizer.hide();
                }
                if (scene.view.targetEnergizer != null) {
                    scene.view.targetEnergizer.hide();
                }
            }
        }

        IntroSceneController controller;

        final TickTimer timer = new TickTimer("Timer-" + name());

        @Override
        public TickTimer timer() {
            return timer;
        }
    }
}
