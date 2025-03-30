/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d;

import de.amr.games.pacman.ui.GameAction;
import de.amr.games.pacman.ui._3d.scene3d.Perspective;
import javafx.scene.shape.DrawMode;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.ui.Globals.THE_UI;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.PY_PIP_ON;
import static de.amr.games.pacman.ui._3d.GlobalProperties3d.*;
import static de.amr.games.pacman.ui._3d.GlobalProperties3d.PY_3D_ENABLED;
import static de.amr.games.pacman.uilib.Ufx.toggle;

/**
 * @author Armin Reichert
 */
public enum GameActions3D implements GameAction {

    NEXT_PERSPECTIVE {
        @Override
        public void execute() {
            Perspective.Name next = PY_3D_PERSPECTIVE.get().next();
            PY_3D_PERSPECTIVE.set(next);
            THE_UI.showFlashMessage(THE_UI.assets().text("camera_perspective", THE_UI.assets().text(next.name())));
        }
    },

    PREV_PERSPECTIVE {
        @Override
        public void execute() {
            Perspective.Name prev = PY_3D_PERSPECTIVE.get().prev();
            PY_3D_PERSPECTIVE.set(prev);
            THE_UI.showFlashMessage(THE_UI.assets().text("camera_perspective", THE_UI.assets().text(prev.name())));
        }
    },

    TOGGLE_DRAW_MODE {
        @Override
        public void execute() {
            PY_3D_DRAW_MODE.set(PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
        }
    },

    TOGGLE_PLAY_SCENE_2D_3D {
        @Override
        public void execute() {
            THE_UI.currentGameScene().ifPresent(gameScene -> {
                toggle(PY_3D_ENABLED);
                if (THE_UI.configurations().currentGameSceneIsPlayScene2D()
                        || THE_UI.configurations().currentGameSceneIsPlayScene3D()) {
                    THE_UI.updateGameScene(true);
                    THE_GAME_CONTROLLER.update(); //TODO needed?
                }
                if (!THE_GAME_CONTROLLER.game().isPlaying()) {
                    THE_UI.showFlashMessage(THE_UI.assets().text(PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
                }
            });
        }
    },

    TOGGLE_PIP_VISIBILITY {
        @Override
        public void execute() {
            toggle(PY_PIP_ON);
            if (!THE_UI.configurations().currentGameSceneIsPlayScene3D()) {
                THE_UI.showFlashMessage(THE_UI.assets().text(PY_PIP_ON.get() ? "pip_on" : "pip_off"));
            }
        }
    }
}