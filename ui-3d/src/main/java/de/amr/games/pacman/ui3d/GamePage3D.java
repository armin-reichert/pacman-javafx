/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.page.GamePage;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;

import static de.amr.games.pacman.ui2d.PacManGames2dUI.PY_IMMUNITY;
import static de.amr.games.pacman.ui3d.PacManGames3dUI.*;

/**
 * @author Armin Reichert
 */
public class GamePage3D extends GamePage {

    public GamePage3D(GameContext context, Scene parentScene) {
        super(context, parentScene);
    }

    @Override
    public void onContextMenuRequested(ContextMenuEvent event) {
        if (contextMenu != null) {
            contextMenu.hide();
        }
        if (!context.isCurrentGameSceneRegisteredAs(GameSceneID.PLAY_SCENE)
            && !context.isCurrentGameSceneRegisteredAs(GameSceneID.PLAY_SCENE_3D)) {
            return;
        }
        boolean isPlayScene3D = context.isCurrentGameSceneRegisteredAs(GameSceneID.PLAY_SCENE_3D);
        var actionHandler = context.actionHandler();
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
        miAutopilot.selectedProperty().bindBidirectional(PY_AUTOPILOT);
        contextMenu.getItems().add(miAutopilot);

        var miImmunity = new CheckMenuItem(context.tt("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        contextMenu.getItems().add(miImmunity);

        contextMenu.getItems().add(new SeparatorMenuItem());
        if (context.game().variant() == GameVariant.PACMAN_XXL) {
            var miOpenMapEditor = new MenuItem(context.tt("open_editor"));
            contextMenu.getItems().add(miOpenMapEditor);
            miOpenMapEditor.setOnAction(e -> context.actionHandler().openMapEditor());
        }

        var miQuit = new MenuItem(context.tt("quit"));
        miQuit.setOnAction(e -> quit());
        contextMenu.getItems().add(miQuit);

        contextMenu.requestFocus();
        contextMenu.show(rootPane(), event.getScreenX(), event.getScreenY());
    }
}