/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.DrawMode;

import java.util.Set;

import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.combine;

public class Camera3DActions {

    private final GameAction actionPreviousPerspective;
    private final GameAction actionNextPerspective;
    private final GameAction actionToggleDrawMode;

    private final Set<ActionKeyBinding> bindings;

    public Camera3DActions(GameAppContext actionContext) {

        actionNextPerspective = new GameAction(actionContext, "perspective_next") {
            @Override
            protected void doAction() {
                final PerspectiveID nextID = actionContext.ui().viewModel().common3D.cameraPerspectiveIdProperty.get().next();
                actionContext.ui().viewModel().common3D.cameraPerspectiveIdProperty.set(nextID);
                actionContext.ui().shortMessage(translatedPerspectiveMessage(actionContext, nextID));
            }

            @Override
            public boolean isEnabled() {
                return is3DPlaySceneActive(actionContext);
            }
        };

        actionPreviousPerspective = new GameAction(actionContext, "perspective_previous") {
            @Override
            protected void doAction() {
                final PerspectiveID prevID = actionContext.ui().viewModel().common3D.cameraPerspectiveIdProperty.get().prev();
                actionContext.ui().viewModel().common3D.cameraPerspectiveIdProperty.set(prevID);
                actionContext.ui().shortMessage(translatedPerspectiveMessage(actionContext, prevID));
            }
            @Override
            public boolean isEnabled() {
                return is3DPlaySceneActive(actionContext);
            }
        };

        actionToggleDrawMode = new GameAction(actionContext, "toggle_draw_mode") {
            @Override
            protected void doAction() {
                Ufx.toggleProperty(actionContext.ui().viewModel().common3D.drawModeProperty, DrawMode.LINE, DrawMode.FILL);
            }
            @Override
            public boolean isEnabled() {
                return is3DPlaySceneActive(actionContext);
            }
        };

        bindings = Set.of(
            new ActionKeyBinding(actionPreviousPerspective, combine().alt().key(KeyCode.LEFT)),
            new ActionKeyBinding(actionNextPerspective,     combine().alt().key(KeyCode.RIGHT)),
            new ActionKeyBinding(actionToggleDrawMode,      combine().alt().key(KeyCode.W))
        );
    }

    public GameAction actionPreviousPerspective() {
        return actionPreviousPerspective;
    }

    public GameAction actionNextPerspective() {
        return actionNextPerspective;
    }

    public GameAction actionToggleDrawMode() {
        return actionToggleDrawMode;
    }

    public Set<ActionKeyBinding> bindings() {
        return bindings;
    }

    private boolean is3DPlaySceneActive(GameAppContext actionContext) {
        return actionContext.ui().gameScenes().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D);
    }

    private String translatedPerspectiveMessage(GameAppContext actionContext, PerspectiveID perspectiveID) {
        final TranslationManager translations = actionContext.ui().translations();
        return translations.translate(
            "camera_perspective",
            translations.translate("perspective_id_" + perspectiveID.name())
        );
    }
}
