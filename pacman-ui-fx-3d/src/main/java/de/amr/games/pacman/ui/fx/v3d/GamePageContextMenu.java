package de.amr.games.pacman.ui.fx.v3d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.ui.fx.GameSceneContext;
import de.amr.games.pacman.ui.fx.scene2d.PlayScene2D;
import de.amr.games.pacman.ui.fx.v3d.scene3d.Perspective;
import de.amr.games.pacman.ui.fx.v3d.scene3d.PlayScene3D;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui.fx.PacManGames2dUI.PY_USE_AUTOPILOT;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.PY_3D_PERSPECTIVE;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.PY_PIP_ON;

/**
 * @author Armin Reichert
 */
public class GamePageContextMenu extends ContextMenu {

    // "Kornblumenblau, sind die Augen der Frauen beim Weine..."
    private static final Color TITLE_ITEM_COLOR = Color.CORNFLOWERBLUE;
    private static final Font TITLE_ITEM_FONT = Font.font("Dialog", FontWeight.BLACK, 14);

    private CheckMenuItem autopilotItem;
    private CheckMenuItem immunityItem;
    private CheckMenuItem pipItem;
    private ToggleGroup perspectivesToggleGroup;

    public GamePageContextMenu(GameSceneContext sceneContext) {
        checkNotNull(sceneContext);
        if (sceneContext.currentGameScene().isEmpty()) {
            return;
        }
        var actionHandler = (ActionHandler3D) sceneContext.actionHandler();
        getItems().clear();
        getItems().add(createTitleItem(sceneContext.tt("scene_display")));
        if (sceneContext.currentGameScene().get() instanceof PlayScene2D) {
            var item = new MenuItem(sceneContext.tt("use_3D_scene"));
            item.setOnAction(e -> actionHandler.toggle2D3D());
            getItems().add(item);
        } else if (sceneContext.currentGameScene().get() instanceof PlayScene3D) {
            var item = new MenuItem(sceneContext.tt("use_2D_scene"));
            item.setOnAction(e -> actionHandler.toggle2D3D());
            getItems().add(item);
            pipItem = new CheckMenuItem(sceneContext.tt("pip"));
            pipItem.setOnAction(e -> actionHandler.togglePipVisible());
            getItems().add(pipItem);
            getItems().add(createTitleItem(sceneContext.tt("select_perspective")));
            perspectivesToggleGroup = new ToggleGroup();
            for (var p : Perspective.values()) {
                var rmi = new RadioMenuItem(sceneContext.tt(p.name()));
                rmi.setUserData(p);
                rmi.setToggleGroup(perspectivesToggleGroup);
                getItems().add(rmi);
            }
            perspectivesToggleGroup.selectedToggleProperty().addListener((py, ov, nv) -> {
                if (nv != null) {
                    // Note: These are the user data of the radio menu item!
                    PY_3D_PERSPECTIVE.set((Perspective) nv.getUserData());
                }
            });
        }

        getItems().add(createTitleItem(sceneContext.tt("pacman")));
        autopilotItem = new CheckMenuItem(sceneContext.tt("autopilot"));
        autopilotItem.setOnAction(e -> actionHandler.toggleAutopilot());
        getItems().add(autopilotItem);
        immunityItem = new CheckMenuItem(sceneContext.tt("immunity"));
        immunityItem.setOnAction(e -> actionHandler.toggleImmunity());
        getItems().add(immunityItem);
    }

    private MenuItem createTitleItem(String title) {
        var text = new Text(title);
        text.setFont(TITLE_ITEM_FONT);
        text.setFill(TITLE_ITEM_COLOR);
        return new CustomMenuItem(text);
    }

    public void updateState() {
        if (perspectivesToggleGroup != null) {
            for (var toggle : perspectivesToggleGroup.getToggles()) {
                toggle.setSelected(PY_3D_PERSPECTIVE.get().equals(toggle.getUserData()));
            }
        }
        if (pipItem != null) {
            pipItem.setSelected(PY_PIP_ON.get());
        }
        if (autopilotItem != null) {
            autopilotItem.setSelected(PY_USE_AUTOPILOT.get());
        }
        if (immunityItem != null) {
            immunityItem.setSelected(GameController.it().isPacImmune());
        }
    }
}