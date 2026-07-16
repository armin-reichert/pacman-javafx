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

    public Camera3DActions(GameAppContext appContext) {

        actionNextPerspective = new GameAction(appContext, "perspective_next") {
            @Override
            protected void doAction() {
                final PerspectiveID nextID = appContext.ui().viewModel().common3D.cameraPerspectiveIdProperty.get().next();
                appContext.ui().viewModel().common3D.cameraPerspectiveIdProperty.set(nextID);
                appContext.ui().shortMessage(translatedPerspectiveMessage(appContext, nextID));
            }

            @Override
            public boolean isEnabled() {
                return is3DPlaySceneActive(appContext);
            }
        };

        actionPreviousPerspective = new GameAction(appContext, "perspective_previous") {
            @Override
            protected void doAction() {
                final PerspectiveID prevID = appContext.ui().viewModel().common3D.cameraPerspectiveIdProperty.get().prev();
                appContext.ui().viewModel().common3D.cameraPerspectiveIdProperty.set(prevID);
                appContext.ui().shortMessage(translatedPerspectiveMessage(appContext, prevID));
            }
            @Override
            public boolean isEnabled() {
                return is3DPlaySceneActive(appContext);
            }
        };

        actionToggleDrawMode = new GameAction(appContext, "toggle_draw_mode") {
            @Override
            protected void doAction() {
                Ufx.toggleProperty(appContext.ui().viewModel().common3D.drawModeProperty, DrawMode.LINE, DrawMode.FILL);
            }
            @Override
            public boolean isEnabled() {
                return is3DPlaySceneActive(appContext);
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

    private boolean is3DPlaySceneActive(GameAppContext appContext) {
        return appContext.ui().gameScenes().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D);
    }

    private String translatedPerspectiveMessage(GameAppContext appContext, PerspectiveID perspectiveID) {
        final TranslationManager translations = appContext.ui().translations();
        return translations.translate(
            "camera_perspective",
            translations.translate("perspective_id_" + perspectiveID.name())
        );
    }
}
