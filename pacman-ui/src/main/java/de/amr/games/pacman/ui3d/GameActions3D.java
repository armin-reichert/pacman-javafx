/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.action.GameAction;
import de.amr.games.pacman.ui3d.scene3d.Perspective;
import javafx.scene.shape.DrawMode;

import static de.amr.games.pacman.ui2d.GlobalProperties2d.PY_PIP_ON;
import static de.amr.games.pacman.ui3d.GlobalProperties3d.PY_3D_DRAW_MODE;
import static de.amr.games.pacman.ui3d.GlobalProperties3d.PY_3D_PERSPECTIVE;
import static de.amr.games.pacman.uilib.Ufx.toggle;

/**
 * @author Armin Reichert
 */
public enum GameActions3D implements GameAction {

    NEXT_PERSPECTIVE {
        @Override
        public void execute(GameContext context) {
            Perspective.Name next = PY_3D_PERSPECTIVE.get().next();
            PY_3D_PERSPECTIVE.set(next);
            context.showFlashMessage(context.locText("camera_perspective", context.locText(next.name())));
        }
    },

    PREV_PERSPECTIVE {
        @Override
        public void execute(GameContext context) {
            Perspective.Name prev = PY_3D_PERSPECTIVE.get().prev();
            PY_3D_PERSPECTIVE.set(prev);
            context.showFlashMessage(context.locText("camera_perspective", context.locText(prev.name())));
        }
    },

    TOGGLE_DRAW_MODE {
        @Override
        public void execute(GameContext context) {
            PY_3D_DRAW_MODE.set(PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
        }
    },

    TOGGLE_PLAY_SCENE_2D_3D {
        @Override
        public void execute(GameContext context) {
            context.togglePlayScene2D3D();
        }
    },

    TOGGLE_PIP_VISIBILITY {
        @Override
        public void execute(GameContext context) {
            toggle(PY_PIP_ON);
            if (!context.currentGameSceneHasID("PlayScene3D")) {
                context.showFlashMessage(context.locText(PY_PIP_ON.get() ? "pip_on" : "pip_off"));
            }
        }
    }
}