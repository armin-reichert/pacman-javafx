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
import static de.amr.games.pacman.ui2d.page.Page.menuTitleItem;
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
        dashboardLayer.dashboard().addInfoBox(3, context.locText("infobox.3D_settings.title"), new InfoBox3D());
        backgroundProperty().bind(Bindings.createObjectBinding(
            () -> {
                if (PY_3D_DRAW_MODE.get() == DrawMode.LINE) {
                    return coloredBackground(Color.rgb(150, 150, 200));
                }
                Image wallpaper = context.assets().get(PY_NIGHT_MODE.get()
                    ? "wallpaper.night" : "wallpaper.day");
                return wallpaperBackground(wallpaper);
            }, PY_3D_DRAW_MODE, PY_NIGHT_MODE
        ));
    }

    @Override
    public void handleContextMenuRequest(ContextMenuEvent event) {
        contextMenu.getItems().clear();
        contextMenu.hide();

        boolean isPlayScene = context.currentGameSceneIs(GameSceneID.PLAY_SCENE)
            || context.currentGameSceneIs(GameSceneID.PLAY_SCENE_3D);
        if (!isPlayScene) {
            return;
        }

        contextMenu.getItems().add(menuTitleItem(context.locText("scene_display")));

        // Toggle 2D-3D
        boolean is3D = context.currentGameSceneIs(GameSceneID.PLAY_SCENE_3D);
        var item = new MenuItem(context.locText(is3D ? "use_2D_scene" : "use_3D_scene"));
        item.setOnAction(e -> context.actionHandler().toggle2D3D());
        contextMenu.getItems().add(item);

        if (is3D) {
            // Toggle picture-in-picture display
            var miPiP = new CheckMenuItem(context.locText("pip"));
            miPiP.selectedProperty().bindBidirectional(PY_PIP_ON);
            contextMenu.getItems().add(miPiP);

            contextMenu.getItems().add(menuTitleItem(context.locText("select_perspective")));

            // Camera perspective selection
            var perspectivesGroup = new ToggleGroup();
            for (var perspective : Perspective.values()) {
                var miPerspective = new RadioMenuItem(context.locText(perspective.name()));
                miPerspective.setToggleGroup(perspectivesGroup);
                // keep global property in sync with selection
                miPerspective.selectedProperty().addListener((py, ov, selected) -> {
                    if (selected) {
                        PY_3D_PERSPECTIVE.set(perspective);
                    }
                });
                // keep selection in sync with global property value
                PY_3D_PERSPECTIVE.addListener((py, ov, newPerspective) -> miPerspective.setSelected(newPerspective == perspective));
                miPerspective.setSelected(perspective == PY_3D_PERSPECTIVE.get()); // == is allowed for enum comparison
                contextMenu.getItems().add(miPerspective);
            }
        }

        // Common items
        contextMenu.getItems().add(menuTitleItem(context.locText("pacman")));

        var miAutopilot = new CheckMenuItem(context.locText("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_AUTOPILOT);
        contextMenu.getItems().add(miAutopilot);

        var miImmunity = new CheckMenuItem(context.locText("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        contextMenu.getItems().add(miImmunity);

        contextMenu.getItems().add(new SeparatorMenuItem());

        if (context.game().variant() == GameVariant.PACMAN_XXL) {
            var miOpenMapEditor = new MenuItem(context.locText("open_editor"));
            miOpenMapEditor.setOnAction(e -> context.actionHandler().openMapEditor());
            contextMenu.getItems().add(miOpenMapEditor);
        }

        var miQuit = new MenuItem(context.locText("quit"));
        miQuit.setOnAction(e -> quit());
        contextMenu.getItems().add(miQuit);

        contextMenu.show(this, event.getScreenX(), event.getScreenY());
        contextMenu.requestFocus();
    }

    @Override
    public void setGameScene(GameScene gameScene) {
        contextMenu.hide();
        if (gameScene instanceof PlayScene3D) {
            getChildren().set(0, gameScene.root());
        } else if (gameScene instanceof GameScene2D scene2D) {
            setGameScene2D(scene2D);
        } else {
            Logger.warn("Cannot embed game scene {}", gameScene);
        }
    }
}