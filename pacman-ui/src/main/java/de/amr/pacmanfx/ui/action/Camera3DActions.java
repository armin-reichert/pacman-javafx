/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
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

    public Camera3DActions() {

        actionNextPerspective = new GameAction("perspective_next") {
            @Override
            public void execute(GameAppContext app) {
                final var perspectiveIDProperty = app.ui().viewModel().common3DSettings().cameraPerspectiveIDProperty();
                final PerspectiveID perspectiveID = perspectiveIDProperty.get().next();
                perspectiveIDProperty.set(perspectiveID);
                app.ui().shortMessage(translatedPerspectiveMessage(app, perspectiveID));
            }

            @Override
            public boolean isEnabled(GameAppContext app) {
                return is3DPlaySceneActive(app);
            }
        };

        actionPreviousPerspective = new GameAction("perspective_previous") {
            @Override
            public void execute(GameAppContext app) {
                final var perspectiveIDProperty = app.ui().viewModel().common3DSettings().cameraPerspectiveIDProperty();
                final PerspectiveID prevID = perspectiveIDProperty.get().prev();
                perspectiveIDProperty.set(prevID);
                app.ui().shortMessage(translatedPerspectiveMessage(app, prevID));
            }
            @Override
            public boolean isEnabled(GameAppContext app) {
                return is3DPlaySceneActive(app);
            }
        };

        actionToggleDrawMode = new GameAction("toggle_draw_mode") {
            @Override
            public void execute(GameAppContext app) {
                final var drawModeProperty = app.ui().viewModel().common3DSettings().drawModeProperty();
                Ufx.toggleProperty(drawModeProperty, DrawMode.LINE, DrawMode.FILL);
            }
            @Override
            public boolean isEnabled(GameAppContext app) {
                return is3DPlaySceneActive(app);
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

    private boolean is3DPlaySceneActive(GameAppContext app) {
        return app.ui().gameScenes().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D);
    }

    private String translatedPerspectiveMessage(GameAppContext app, PerspectiveID perspectiveID) {
        final TranslationManager translations = app.ui().translations();
        return translations.translate(
            "camera_perspective",
            translations.translate("perspective_id_" + perspectiveID.name())
        );
    }
}
