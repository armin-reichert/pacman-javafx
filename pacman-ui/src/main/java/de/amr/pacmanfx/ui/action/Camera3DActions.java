/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.engine.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.ui.EngineContext;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.DrawMode;

import java.util.Set;

import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.combine;

public class Camera3DActions {

    private final GameAction<EngineContext> actionPreviousPerspective;
    private final GameAction<EngineContext> actionNextPerspective;
    private final GameAction<EngineContext> actionToggleDrawMode;

    private final Set<ActionKeyBinding<EngineContext>> bindings;

    public Camera3DActions() {

        actionNextPerspective = new GameAction<>("perspective_next") {
            @Override
            public void execute(EngineContext context) {
                final GameUI ui = context.ui();
                final var perspectiveIDProperty = ui.viewModel().common3DSettings().cameraPerspectiveIDProperty();
                final PerspectiveID perspectiveID = perspectiveIDProperty.get().next();
                perspectiveIDProperty.set(perspectiveID);
                ui.showMessage(translatedPerspectiveMessage(context, perspectiveID));
            }

            @Override
            public boolean isEnabled(EngineContext context) {
                return is3DPlaySceneActive(context);
            }
        };

        actionPreviousPerspective = new GameAction<>("perspective_previous") {
            @Override
            public void execute(EngineContext context) {
                final GameUI ui = context.ui();
                final var perspectiveIDProperty = ui.viewModel().common3DSettings().cameraPerspectiveIDProperty();
                final PerspectiveID prevID = perspectiveIDProperty.get().prev();
                perspectiveIDProperty.set(prevID);
                ui.showMessage(translatedPerspectiveMessage(context, prevID));
            }

            @Override
            public boolean isEnabled(EngineContext context) {
                return is3DPlaySceneActive(context);
            }
        };

        actionToggleDrawMode = new GameAction<>("toggle_draw_mode") {
            @Override
            public void execute(EngineContext context) {
                final var drawModeProperty = context.ui().viewModel().common3DSettings().drawModeProperty();
                Ufx.toggleProperty(drawModeProperty, DrawMode.LINE, DrawMode.FILL);
            }

            @Override
            public boolean isEnabled(EngineContext context) {
                return is3DPlaySceneActive(context);
            }
        };

        bindings = Set.of(
            new ActionKeyBinding<>(actionPreviousPerspective(), combine().alt().key(KeyCode.LEFT)),
            new ActionKeyBinding<>(actionNextPerspective(),     combine().alt().key(KeyCode.RIGHT)),
            new ActionKeyBinding<>(actionToggleDrawMode(),      combine().alt().key(KeyCode.W))
        );
    }

    public GameAction<EngineContext> actionPreviousPerspective() {
        return actionPreviousPerspective;
    }

    public GameAction<EngineContext> actionNextPerspective() {
        return actionNextPerspective;
    }

    public GameAction<EngineContext> actionToggleDrawMode() {
        return actionToggleDrawMode;
    }

    public Set<ActionKeyBinding<EngineContext>> bindings() {
        return bindings;
    }

    private boolean is3DPlaySceneActive(EngineContext context) {
        return context.gameSceneManager().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D);
    }

    private String translatedPerspectiveMessage(EngineContext context, PerspectiveID perspectiveID) {
        return context.translationManager().translate(
            "camera_perspective",
            context.translationManager().translate("perspective_id_" + perspectiveID.name())
        );
    }
}
