/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d;

import de.amr.games.pacman.ui.GameAction;
import de.amr.games.pacman.ui._3d.scene3d.Perspective;
import javafx.scene.shape.DrawMode;

import static de.amr.games.pacman.ui.Globals.THE_UI;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.PY_PIP_ON;
import static de.amr.games.pacman.ui._3d.GlobalProperties3d.PY_3D_DRAW_MODE;
import static de.amr.games.pacman.ui._3d.GlobalProperties3d.PY_3D_PERSPECTIVE;
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
            THE_UI.showFlashMessage(THE_UI.assets().localizedText("camera_perspective", THE_UI.assets().localizedText(next.name())));
        }
    },

    PREV_PERSPECTIVE {
        @Override
        public void execute() {
            Perspective.Name prev = PY_3D_PERSPECTIVE.get().prev();
            PY_3D_PERSPECTIVE.set(prev);
            THE_UI.showFlashMessage(THE_UI.assets().localizedText("camera_perspective", THE_UI.assets().localizedText(prev.name())));
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
            THE_UI.togglePlayScene2D3D();
        }
    },

    TOGGLE_PIP_VISIBILITY {
        @Override
        public void execute() {
            toggle(PY_PIP_ON);
            if (!THE_UI.configurations().currentGameSceneIsPlayScene3D()) {
                THE_UI.showFlashMessage(THE_UI.assets().localizedText(PY_PIP_ON.get() ? "pip_on" : "pip_off"));
            }
        }
    }
}