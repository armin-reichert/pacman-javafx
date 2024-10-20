/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.GlobalGameActions2D;
import de.amr.games.pacman.ui2d.page.GamePage;
import de.amr.games.pacman.ui3d.dashboard.InfoBox3D;
import de.amr.games.pacman.ui3d.scene.common.Perspective;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.ui2d.PacManGames2dApp.*;
import static de.amr.games.pacman.ui2d.page.Page.menuTitleItem;
import static de.amr.games.pacman.ui2d.util.Ufx.coloredBackground;
import static de.amr.games.pacman.ui3d.PacManGames3dApp.PY_3D_DRAW_MODE;
import static de.amr.games.pacman.ui3d.PacManGames3dApp.PY_3D_PERSPECTIVE;

/**
 * @author Armin Reichert
 */
public class GamePage3D extends GamePage {

    public GamePage3D(GameContext context, Scene parentScene) {
        super(context, parentScene);
        dashboardLayer.addEntry(3, context.locText("infobox.3D_settings.title"), new InfoBox3D());
        backgroundProperty().bind(Bindings.createObjectBinding(
            () -> {
                if (!context.currentGameSceneHasID("PlayScene3D")) {
                    return context.assets().get("wallpaper.background"); // little Pac-Man tapestry
                }
                return PY_3D_DRAW_MODE.get() == DrawMode.LINE
                    ? coloredBackground(Color.rgb(100, 100, 200))
                    : context.assets().get(PY_NIGHT_MODE.get() ? "wallpaper.night" : "wallpaper.day");
            }, PY_3D_DRAW_MODE, PY_NIGHT_MODE, context.gameSceneProperty()
        ));
    }

    @Override
    public void handleInput() {
        context.doFirstCalledActionElse(
            List.of(GlobalGameActions3D.TOGGLE_PIP_VISIBILITY, GlobalGameActions3D.TOGGLE_PLAY_SCENE_2D_3D),
            super::handleInput);
    }

    @Override
    public void handleContextMenuRequest(ContextMenuEvent event) {
        contextMenu.getItems().clear();
        contextMenu.hide();

        boolean isPlayScene = context.currentGameSceneHasID("PlayScene2D")
            || context.currentGameSceneHasID("PlayScene3D");
        if (!isPlayScene) {
            return;
        }

        contextMenu.getItems().add(menuTitleItem(context.locText("scene_display")));

        // Toggle 2D-3D
        boolean is3D = context.currentGameSceneHasID("PlayScene3D");
        var item = new MenuItem(context.locText(is3D ? "use_2D_scene" : "use_3D_scene"));
        item.setOnAction(e -> GlobalGameActions3D.TOGGLE_PLAY_SCENE_2D_3D.execute(context));
        contextMenu.getItems().add(item);

        // Toggle picture-in-picture display
        var miPiP = new CheckMenuItem(context.locText("pip"));
        miPiP.selectedProperty().bindBidirectional(PY_PIP_ON);
        contextMenu.getItems().add(miPiP);

        contextMenu.getItems().add(menuTitleItem(context.locText("select_perspective")));

        // Camera perspective selection
        var perspectivesGroup = new ToggleGroup();
        for (var perspective : Perspective.Name.values()) {
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

        // Common items
        contextMenu.getItems().add(menuTitleItem(context.locText("pacman")));

        var miAutopilot = new CheckMenuItem(context.locText("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_AUTOPILOT);
        contextMenu.getItems().add(miAutopilot);

        var miImmunity = new CheckMenuItem(context.locText("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        contextMenu.getItems().add(miImmunity);

        contextMenu.getItems().add(new SeparatorMenuItem());

        var miMuted = new CheckMenuItem(context.locText("muted"));
        miMuted.selectedProperty().bindBidirectional(context.sounds().mutedProperty());
        contextMenu.getItems().add(miMuted);

        if (context.gameVariant() == GameVariant.PACMAN_XXL || context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
            var miOpenMapEditor = new MenuItem(context.locText("open_editor"));
            miOpenMapEditor.setOnAction(e -> GlobalGameActions2D.OPEN_EDITOR.execute(context));
            contextMenu.getItems().add(miOpenMapEditor);
        }

        var miQuit = new MenuItem(context.locText("quit"));
        miQuit.setOnAction(e -> GlobalGameActions2D.SHOW_START_PAGE.execute(context));
        contextMenu.getItems().add(miQuit);

        contextMenu.show(this, event.getScreenX(), event.getScreenY());
        contextMenu.requestFocus();
    }
}