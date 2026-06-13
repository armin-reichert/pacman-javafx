/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.DrawMode;

import java.util.Set;

import static de.amr.pacmanfx.ui.input.Keyboard.alt;

public class Camera3DActions {

    private final GameAction actionPreviousPerspective;
    private final GameAction actionNextPerspective;
    private final GameAction actionToggleDrawMode;

    private final Set<ActionKeyBinding> bindings;

    public Camera3DActions(Game game) {

        actionNextPerspective = new GameAction(game, "perspective_next") {
            @Override
            protected void doAction() {
                final PerspectiveID nextID = game.ui().settings3D().cameraPerspectiveIdProperty().get().next();
                game.ui().settings3D().cameraPerspectiveIdProperty().set(nextID);

                final TranslationManager translations = game.ui().translations();
                final String msgKey = translations.translate(
                    "camera_perspective",
                    translations.translate("perspective_id_" + nextID.name())
                );
                game.shortMessage(msgKey);
            }
        };

        actionPreviousPerspective = new GameAction(game, "perspective_previous") {
            @Override
            protected void doAction() {
                final PerspectiveID prevID = game.ui().settings3D().cameraPerspectiveIdProperty().get().prev();
                game.ui().settings3D().cameraPerspectiveIdProperty().set(prevID);

                final TranslationManager translations = game.ui().translations();
                final String msgKey = translations.translate(
                    "camera_perspective",
                    translations.translate("perspective_id_" + prevID.name())
                );
                game.shortMessage(msgKey);
            }
        };

        actionToggleDrawMode = new GameAction(game, "toggle_draw_mode") {
            @Override
            protected void doAction() {
                Ufx.toggleProperty(game.ui().settings3D().drawModeProperty(), DrawMode.LINE, DrawMode.FILL);
            }
        };

        bindings = Set.of(
            new ActionKeyBinding(actionPreviousPerspective, alt(KeyCode.LEFT)),
            new ActionKeyBinding(actionNextPerspective,     alt(KeyCode.RIGHT)),
            new ActionKeyBinding(actionToggleDrawMode,      alt(KeyCode.W))
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
}
