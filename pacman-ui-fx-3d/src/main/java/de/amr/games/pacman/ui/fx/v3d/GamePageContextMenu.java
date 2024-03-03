package de.amr.games.pacman.ui.fx.v3d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.ui.fx.GameScene;
import de.amr.games.pacman.ui.fx.GameSceneContext;
import de.amr.games.pacman.ui.fx.scene2d.PlayScene2D;
import de.amr.games.pacman.ui.fx.v3d.scene3d.Perspective;
import de.amr.games.pacman.ui.fx.v3d.scene3d.PlayScene3D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.PY_3D_PERSPECTIVE;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.PY_PIP_ON;

/**
 * @author Armin Reichert
 */
public class GamePageContextMenu extends ContextMenu {
    private final GameSceneContext sceneContext;
    private CheckMenuItem autopilotItem;
    private CheckMenuItem immunityItem;
    private CheckMenuItem pipItem;
    private ToggleGroup perspectivesToggleGroup;
    private final Font titleItemFont;

    public GamePageContextMenu(GameSceneContext sceneContext, Scene parentScene) {
        checkNotNull(sceneContext);
        checkNotNull(parentScene);
        this.sceneContext = sceneContext;
        titleItemFont = sceneContext.theme().font("font.handwriting", 18);
        parentScene.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            hide();
            if (e.getButton() == MouseButton.SECONDARY) {
                sceneContext.currentGameScene().ifPresent(gameScene -> {
                    if (gameScene == sceneContext.sceneConfig().get("play") ||
                        gameScene == sceneContext.sceneConfig().get("play3D")) {
                        rebuild(gameScene);
                        show(parentScene.getRoot(), e.getScreenX(), e.getScreenY());
                    }
                });
            }
        });
    }

    public void rebuild(GameScene gameScene) {
        var actionHandler = (ActionHandler3D) sceneContext.actionHandler();
        getItems().clear();
        getItems().add(createTitleItem(sceneContext.tt("scene_display")));
        if (gameScene instanceof PlayScene2D) {
            var item = new MenuItem(sceneContext.tt("use_3D_scene"));
            item.setOnAction(e -> actionHandler.toggle2D3D());
            getItems().add(item);
        } else if (gameScene instanceof PlayScene3D) {
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
        text.setFont(titleItemFont);
        text.setFill(sceneContext.theme().color("palette.orange"));
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
            autopilotItem.setSelected(GameController.it().isPacAutoControlled());
        }
        if (immunityItem != null) {
            immunityItem.setSelected(GameController.it().isPacImmune());
        }
    }
}