/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.game.Game;
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

    public Camera3DActions(Game game) {

        actionNextPerspective = new GameAction(game, "perspective_next") {
            @Override
            protected void doAction() {
                final PerspectiveID nextID = game.ui().viewModel().d3.cameraPerspectiveIdProperty.get().next();
                game.ui().viewModel().d3.cameraPerspectiveIdProperty.set(nextID);
                game.ui().shortMessage(translatedPerspectiveMessage(game, nextID));
            }

            @Override
            public boolean isEnabled() {
                return is3DPlaySceneActive(game);
            }
        };

        actionPreviousPerspective = new GameAction(game, "perspective_previous") {
            @Override
            protected void doAction() {
                final PerspectiveID prevID = game.ui().viewModel().d3.cameraPerspectiveIdProperty.get().prev();
                game.ui().viewModel().d3.cameraPerspectiveIdProperty.set(prevID);
                game.ui().shortMessage(translatedPerspectiveMessage(game, prevID));
            }
            @Override
            public boolean isEnabled() {
                return is3DPlaySceneActive(game);
            }
        };

        actionToggleDrawMode = new GameAction(game, "toggle_draw_mode") {
            @Override
            protected void doAction() {
                Ufx.toggleProperty(game.ui().viewModel().d3.drawModeProperty, DrawMode.LINE, DrawMode.FILL);
            }
            @Override
            public boolean isEnabled() {
                return is3DPlaySceneActive(game);
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

    private boolean is3DPlaySceneActive(Game game) {
        return game.ui().gameScenes().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D);
    }

    private String translatedPerspectiveMessage(Game game, PerspectiveID perspectiveID) {
        final TranslationManager translations = game.ui().translations();
        return translations.translate(
            "camera_perspective",
            translations.translate("perspective_id_" + perspectiveID.name())
        );
    }
}
