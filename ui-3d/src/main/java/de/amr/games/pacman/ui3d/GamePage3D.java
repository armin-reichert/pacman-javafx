/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.ui2d.page.GamePage;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui2d.scene.GameSceneContext;
import de.amr.games.pacman.ui3d.scene.Perspective;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;

import static de.amr.games.pacman.ui2d.PacManGames2dUI.PY_IMMUNITY;
import static de.amr.games.pacman.ui3d.PacManGames3dUI.*;

/**
 * @author Armin Reichert
 */
public class GamePage3D extends GamePage {

    public GamePage3D(GameSceneContext context) {
        super(context);
    }

    @Override
    public void onContextMenuRequested(ContextMenuEvent event) {
        if (contextMenu != null) {
            contextMenu.hide();
        }
        if (!context.isCurrentGameScene(PLAY_SCENE) && !context.isCurrentGameScene(PLAY_SCENE_3D)) {
            return;
        }
        boolean isPlayScene3D = context.isCurrentGameScene(PLAY_SCENE_3D);
        var actionHandler = (ActionHandler3D) context.actionHandler();
        contextMenu = new ContextMenu();

        contextMenu.getItems().add(menuTitleItem(context.tt("scene_display")));
        String titleKey = isPlayScene3D ? "use_2D_scene" : "use_3D_scene";
        var item = new MenuItem(context.tt(titleKey));
        item.setOnAction(e -> actionHandler.toggle2D3D());
        contextMenu.getItems().add(item);

        if (isPlayScene3D) {
            var miPictureInPicture = new CheckMenuItem(context.tt("pip"));
            miPictureInPicture.selectedProperty().bindBidirectional(PY_PIP_ON);
            contextMenu.getItems().add(miPictureInPicture);

            contextMenu.getItems().add(menuTitleItem(context.tt("select_perspective")));
            var radioGroup = new ToggleGroup();
            for (var perspective : Perspective.values()) {
                var miPerspective = new RadioMenuItem(context.tt(perspective.name()));
                contextMenu.getItems().add(miPerspective);
                // keep global property in sync with selection
                miPerspective.selectedProperty().addListener((py, ov, selected) -> {
                    if (selected) {
                        PY_3D_PERSPECTIVE.set(perspective);
                    }
                });
                // keep selection in sync with global property value
                PY_3D_PERSPECTIVE.addListener((py, ov, newPerspective) -> miPerspective.setSelected(newPerspective == perspective));
                miPerspective.setSelected(perspective == PY_3D_PERSPECTIVE.get()); // == is allowed for enum comparison
                miPerspective.setToggleGroup(radioGroup);
            }
        }

        // Common items
        contextMenu.getItems().add(menuTitleItem(context.tt("pacman")));

        var miAutopilot = new CheckMenuItem(context.tt("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_USE_AUTOPILOT);
        contextMenu.getItems().add(miAutopilot);

        var miImmunity = new CheckMenuItem(context.tt("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        contextMenu.getItems().add(miImmunity);

        contextMenu.requestFocus();
        contextMenu.show(rootPane(), event.getScreenX(), event.getScreenY());
    }

    @Override
    public void onGameSceneChanged(GameScene newGameScene) {
        if (context.isCurrentGameScene(PLAY_SCENE_3D)) {
            layout().replaceCanvasLayer(newGameScene.root());
        } else {
            layout().restoreCanvasLayer();
            if (newGameScene instanceof GameScene2D scene2D) {
                scene2D.clearCanvas();
                adaptCanvasSizeToCurrentWorld();
            }
        }
        updateDashboardLayer();
        hideContextMenu();
    }
}