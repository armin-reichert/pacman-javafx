/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.page.GamePage;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui3d.dashboard.InfoBox3D;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import org.tinylog.Logger;

import static de.amr.games.pacman.ui2d.GameParameters.*;
import static de.amr.games.pacman.ui2d.util.Ufx.coloredBackground;
import static de.amr.games.pacman.ui2d.util.Ufx.wallpaperBackground;
import static de.amr.games.pacman.ui3d.GameParameters3D.PY_3D_DRAW_MODE;
import static de.amr.games.pacman.ui3d.GameParameters3D.PY_3D_PERSPECTIVE;

/**
 * @author Armin Reichert
 */
public class GamePage3D extends GamePage {

    public GamePage3D(GameContext context, Scene parentScene) {
        super(context, parentScene);
        dashboard().addInfoBox(3, context.tt("infobox.3D_settings.title"), new InfoBox3D());
        rootPane().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> {
                if (PY_3D_DRAW_MODE.get() == DrawMode.LINE) {
                    return coloredBackground(Color.rgb(150, 150, 200));
                }
                Image wallpaper = context.assets().get(PY_NIGHT_MODE.get() ? "wallpaper.night" : "wallpaper.day");
                return wallpaperBackground(wallpaper);
            },
            PY_3D_DRAW_MODE, PY_NIGHT_MODE
        ));
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

    @Override
    public void embedGameScene(GameScene gameScene) {
        hideContextMenu();
        if (gameScene instanceof PlayScene3D) {
            stackPane.getChildren().set(0, gameScene.root());
        } else if (gameScene instanceof GameScene2D scene2D) {
            embedGameScene2D(scene2D);
        } else {
            Logger.warn("Cannot embed game scene {}", gameScene);
        }
    }
}