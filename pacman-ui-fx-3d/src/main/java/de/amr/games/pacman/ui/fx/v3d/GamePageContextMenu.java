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

    private final GameSceneContext sceneContext;
    private CheckMenuItem autopilotItem;
    private CheckMenuItem immunityItem;
    private CheckMenuItem pipItem;
    private ToggleGroup perspectivesToggleGroup;

    public GamePageContextMenu(GameSceneContext sceneContext, Scene parentScene) {
        checkNotNull(sceneContext);
        checkNotNull(parentScene);
        this.sceneContext = sceneContext;
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
        gameScene.configureContextMenu(gameScene,sceneContext,getItems());

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