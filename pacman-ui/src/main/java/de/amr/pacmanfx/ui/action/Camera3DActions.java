/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.engine.runtime.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.ui.GameEngineUIContext;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.DrawMode;

import java.util.Set;

import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.combine;

public class Camera3DActions {

    private final GameAction<GameEngineUIContext> actionPreviousPerspective;
    private final GameAction<GameEngineUIContext> actionNextPerspective;
    private final GameAction<GameEngineUIContext> actionToggleDrawMode;

    private final Set<ActionKeyBinding> bindings;

    public Camera3DActions() {

        actionNextPerspective = new GameAction<>("perspective_next") {
            @Override
            public void execute(GameEngineUIContext context) {
                final GameUI ui = context.ui();
                final var perspectiveIDProperty = ui.viewModel().common3DSettings().cameraPerspectiveIDProperty();
                final PerspectiveID perspectiveID = perspectiveIDProperty.get().next();
                perspectiveIDProperty.set(perspectiveID);
                ui.shortMessage(translatedPerspectiveMessage(context, perspectiveID));
            }

            @Override
            public boolean isEnabled(GameEngineUIContext context) {
                return is3DPlaySceneActive(context);
            }
        };

        actionPreviousPerspective = new GameAction<>("perspective_previous") {
            @Override
            public void execute(GameEngineUIContext context) {
                final GameUI ui = context.ui();
                final var perspectiveIDProperty = ui.viewModel().common3DSettings().cameraPerspectiveIDProperty();
                final PerspectiveID prevID = perspectiveIDProperty.get().prev();
                perspectiveIDProperty.set(prevID);
                ui.shortMessage(translatedPerspectiveMessage(context, prevID));
            }

            @Override
            public boolean isEnabled(GameEngineUIContext context) {
                return is3DPlaySceneActive(context);
            }
        };

        actionToggleDrawMode = new GameAction<>("toggle_draw_mode") {
            @Override
            public void execute(GameEngineUIContext context) {
                final var drawModeProperty = context.ui().viewModel().common3DSettings().drawModeProperty();
                Ufx.toggleProperty(drawModeProperty, DrawMode.LINE, DrawMode.FILL);
            }

            @Override
            public boolean isEnabled(GameEngineUIContext context) {
                return is3DPlaySceneActive(context);
            }
        };

        bindings = Set.of(
            new ActionKeyBinding(actionPreviousPerspective, combine().alt().key(KeyCode.LEFT)),
            new ActionKeyBinding(actionNextPerspective,     combine().alt().key(KeyCode.RIGHT)),
            new ActionKeyBinding(actionToggleDrawMode,      combine().alt().key(KeyCode.W))
        );
    }

    public GameAction<GameEngineUIContext> actionPreviousPerspective() {
        return actionPreviousPerspective;
    }

    public GameAction<GameEngineUIContext> actionNextPerspective() {
        return actionNextPerspective;
    }

    public GameAction<GameEngineUIContext> actionToggleDrawMode() {
        return actionToggleDrawMode;
    }

    public Set<ActionKeyBinding> bindings() {
        return bindings;
    }

    private boolean is3DPlaySceneActive(GameEngineUIContext context) {
        return context.gameSceneManager().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D);
    }

    private String translatedPerspectiveMessage(GameEngineUIContext context, PerspectiveID perspectiveID) {
        return context.translationManager().translate(
            "camera_perspective",
            context.translationManager().translate("perspective_id_" + perspectiveID.name())
        );
    }
}
