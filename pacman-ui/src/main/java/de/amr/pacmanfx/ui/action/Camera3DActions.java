/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.engine.runtime.PacManGamesEngineImpl;
import de.amr.pacmanfx.engine.runtime.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.runtime.action.GameAction;
import de.amr.pacmanfx.engine.runtime.PacManGamesEngine;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.DrawMode;

import java.util.Set;

import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.combine;

public class Camera3DActions {

    private final GameAction actionPreviousPerspective;
    private final GameAction actionNextPerspective;
    private final GameAction actionToggleDrawMode;

    private final Set<ActionKeyBinding> bindings;

    public Camera3DActions() {

        actionNextPerspective = new GameAction("perspective_next") {
            @Override
            public void execute(PacManGamesEngine engine) {
                if (!(engine instanceof PacManGamesEngineImpl engineImpl)) {
                    throw new IllegalArgumentException("Illegal engine " + engine);
                }
                final GameUI ui = engineImpl.ui();
                final var perspectiveIDProperty = ui.viewModel().common3DSettings().cameraPerspectiveIDProperty();
                final PerspectiveID perspectiveID = perspectiveIDProperty.get().next();
                perspectiveIDProperty.set(perspectiveID);
                ui.shortMessage(translatedPerspectiveMessage(engine, perspectiveID));
            }

            @Override
            public boolean isEnabled(PacManGamesEngine engine) {
                if (!(engine instanceof PacManGamesEngineImpl engineImpl)) {
                    throw new IllegalArgumentException("Illegal engine " + engine);
                }
                return is3DPlaySceneActive(engineImpl);
            }
        };

        actionPreviousPerspective = new GameAction("perspective_previous") {
            @Override
            public void execute(PacManGamesEngine engine) {
                if (!(engine instanceof PacManGamesEngineImpl engineImpl)) {
                    throw new IllegalArgumentException("Illegal engine " + engine);
                }
                final GameUI ui = engineImpl.ui();
                final var perspectiveIDProperty = ui.viewModel().common3DSettings().cameraPerspectiveIDProperty();
                final PerspectiveID prevID = perspectiveIDProperty.get().prev();
                perspectiveIDProperty.set(prevID);
                ui.shortMessage(translatedPerspectiveMessage(engine, prevID));
            }
            @Override
            public boolean isEnabled(PacManGamesEngine engine) {
                if (!(engine instanceof PacManGamesEngineImpl engineImpl)) {
                    throw new IllegalArgumentException("Illegal engine " + engine);
                }
                return is3DPlaySceneActive(engineImpl);
            }
        };

        actionToggleDrawMode = new GameAction("toggle_draw_mode") {
            @Override
            public void execute(PacManGamesEngine engine) {
                if (!(engine instanceof PacManGamesEngineImpl engineImpl)) {
                    throw new IllegalArgumentException("Illegal engine " + engine);
                }
                final var drawModeProperty = engineImpl.ui().viewModel().common3DSettings().drawModeProperty();
                Ufx.toggleProperty(drawModeProperty, DrawMode.LINE, DrawMode.FILL);
            }
            @Override
            public boolean isEnabled(PacManGamesEngine engine) {
                if (!(engine instanceof PacManGamesEngineImpl engineImpl)) {
                    throw new IllegalArgumentException("Illegal engine " + engine);
                }
                return is3DPlaySceneActive(engineImpl);
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

    private boolean is3DPlaySceneActive(PacManGamesEngineImpl engine) {
        return engine.gameSceneManager().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D);
    }

    private String translatedPerspectiveMessage(PacManGamesEngine app, PerspectiveID perspectiveID) {
        return app.translationManager().translate(
            "camera_perspective",
            app.translationManager().translate("perspective_id_" + perspectiveID.name())
        );
    }
}
