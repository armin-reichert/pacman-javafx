/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.gamescene.introscene;

import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateMachine;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.model.GhostPersonality;

import java.util.List;

class IntroSceneController extends StateMachine<ArcadeMsPacMan_IntroScene> {

    public IntroSceneController() {
        super(List.of(SceneState.values()));
        for (var state : SceneState.values()) {
            state.controller = this;
        }
    }

    public enum SceneState implements State<ArcadeMsPacMan_IntroScene> {

        STARTING {
            @Override
            public void onEnter(ArcadeMsPacMan_IntroScene scene) {
                scene.updateMarqueeText(this);
            }

            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                if (timer.atSecond(1)) {
                    controller.enterState(scene, GHOSTS_MARCHING_IN);
                }
            }
        },

        GHOSTS_MARCHING_IN {
            @Override
            public void onEnter(ArcadeMsPacMan_IntroScene scene) {
                scene.updateMarqueeText(this);
            }

            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                final boolean atEndPosition = scene.letGhostWalkIn(scene.view.ghosts().get(scene.ghostInSpotlight));
                if (atEndPosition) {
                    if (scene.ghostInSpotlight == GhostPersonality.ORANGE_GHOST_POKEY.ordinal()) {
                        controller.enterState(scene, MS_PACMAN_MARCHING_IN);
                    } else {
                        ++scene.ghostInSpotlight;
                        scene.updateMarqueeText(this);
                    }
                }
            }
        },

        MS_PACMAN_MARCHING_IN {
            @Override
            public void onEnter(ArcadeMsPacMan_IntroScene scene) {
                scene.updateMarqueeText(this);
            }

            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                final boolean atEndPosition = scene.letMsPacManWalkIn(scene.view.msPacMan());
                if (atEndPosition) {
                    controller.enterState(scene, READY_TO_PLAY);
                }
            }
        },

        READY_TO_PLAY {
            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                final GameContext game = scene.game();
                final boolean canPlay = !game.coinMechanism().isEmpty();
                if (timer.atSecond(2.0) && !canPlay) {
                    scene.gameFlow().enterGameState(game, CommonGameStateID.GAME_OR_LEVEL_STARTING); // play demo level after 2 seconds
                }
                //TODO can this happen at all?
                else if (timer.atSecond(5)) {
                    scene.gameFlow().enterGameState(game, CommonGameStateID.GAME_PREPARATION);
                }
            }
        };

        IntroSceneController controller;

        final TickTimer timer = new TickTimer("Timer-" + name());

        @Override
        public TickTimer timer() {
            return timer;
        }
    }
}
