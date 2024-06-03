/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.ui2d.PacManGames2dUI;
import de.amr.games.pacman.ui2d.page.GamePage;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui2d.scene.GameSceneContext;
import de.amr.games.pacman.ui2d.scene.PlayScene2D;
import de.amr.games.pacman.ui2d.util.Keyboard;
import de.amr.games.pacman.ui2d.util.Ufx;
import de.amr.games.pacman.ui3d.dashboard.Dashboard;
import de.amr.games.pacman.ui3d.scene.Perspective;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import static de.amr.games.pacman.ui3d.PacManGames3dUI.*;

/**
 * @author Armin Reichert
 */
public class GamePage3D extends GamePage {

    private static class PictureInPictureView extends Canvas {

        private final PlayScene2D displayedScene = new PlayScene2D();

        public PictureInPictureView(GameSceneContext context) {
            displayedScene.setContext(context);
            displayedScene.setCanvas(this);
            displayedScene.setScoreVisible(true);
            displayedScene.scalingPy.bind(heightProperty().divide(CANVAS_HEIGHT_UNSCALED));
            widthProperty().bind(heightProperty().multiply(0.777));
            opacityProperty().bind(PY_PIP_OPACITY_PERCENTAGE.divide(100.0));
        }

        public void draw() {
            if (isVisible()) {
                displayedScene.draw();
            }
        }
    }

    private final Scene parentScene;
    private final BorderPane dashboardLayer;
    private final Dashboard dashboard;
    private final PictureInPictureView pip;
    private ContextMenu contextMenu;

    public GamePage3D(Scene parentScene, GameSceneContext context, double width, double height) {
        super(context, width, height);
        this.parentScene = parentScene;
        pip = new PictureInPictureView(context);
        dashboard = new Dashboard(context);

        dashboardLayer = new BorderPane();
        dashboardLayer.setLeft(dashboard);
        dashboardLayer.setRight(pip);

        layout.getChildren().add(dashboardLayer);
        layout.backgroundProperty().bind(Bindings.createObjectBinding(
            () -> {
                if (PY_3D_DRAW_MODE.get() == DrawMode.LINE) {
                    return Ufx.coloredBackground(Color.BLACK);
                } else {
                    var wallpaperKey = PY_3D_NIGHT_MODE.get() ? "model3D.wallpaper.night" : "model3D.wallpaper";
                    return this.context.theme().background(wallpaperKey);
                }
            },
            PY_3D_DRAW_MODE, PY_3D_NIGHT_MODE
        ));
        layout.getCanvasLayer().setBackground(context.theme().background("wallpaper.background"));

        // data binding
        pip.heightProperty().bind(PY_PIP_HEIGHT);
        PY_3D_PIP_ON.addListener((py, ov, nv) -> updateTopLayer());
        dashboard.visibleProperty().addListener((py, ov, nv) -> updateTopLayer());

        updateTopLayer();
    }

    @Override
    public void onContextMenuRequested(ContextMenuEvent event) {
        if (contextMenu != null) {
            contextMenu.hide();
        }
        if (!context.isCurrentGameScene(PLAY_SCENE) && !context.isCurrentGameScene(PLAY_SCENE_3D)) {
            return;
        }
        boolean isPlayScene2D = context.isCurrentGameScene(PLAY_SCENE);
        var actionHandler = (ActionHandler3D) context.actionHandler();
        contextMenu = new ContextMenu();
        if (isPlayScene2D) {
            contextMenu.getItems().add(titleItem(context.tt("scene_display")));
            var item = new MenuItem(context.tt("use_3D_scene"));
            item.setOnAction(e -> actionHandler.toggle2D3D());
            contextMenu.getItems().add(item);
        } else {
            contextMenu.getItems().add(titleItem(context.tt("scene_display")));
            var item = new MenuItem(context.tt("use_2D_scene"));
            item.setOnAction(e -> actionHandler.toggle2D3D());
            contextMenu.getItems().add(item);
            var pipItem = new CheckMenuItem(context.tt("pip"));
            pipItem.selectedProperty().bindBidirectional(PY_3D_PIP_ON);
            contextMenu.getItems().add(pipItem);
            contextMenu.getItems().add(titleItem(context.tt("select_perspective")));
            var toggleGroup = new ToggleGroup();
            for (var perspective : Perspective.values()) {
                var radio = new RadioMenuItem(context.tt(perspective.name()));
                radio.setSelected(perspective.equals(PY_3D_PERSPECTIVE.get()));
                radio.setUserData(perspective);
                radio.setToggleGroup(toggleGroup);
                contextMenu.getItems().add(radio);
            }
            toggleGroup.selectedToggleProperty().addListener((py, ov, radio) -> {
                if (radio != null) {
                    PY_3D_PERSPECTIVE.set((Perspective) radio.getUserData());
                }
            });
            PY_3D_PERSPECTIVE.addListener((py, ov, nv) -> {
                for (var radio : toggleGroup.getToggles()) {
                    radio.setSelected(radio.getUserData().equals(PY_3D_PERSPECTIVE.get()));
                }
            });
        }
        contextMenu.getItems().add(titleItem(context.tt("pacman")));
        var autopilotItem = new CheckMenuItem(context.tt("autopilot"));
        autopilotItem.selectedProperty().bindBidirectional(PY_USE_AUTOPILOT);
        contextMenu.getItems().add(autopilotItem);
        var immunityItem = new CheckMenuItem(context.tt("immunity"));
        immunityItem.selectedProperty().bindBidirectional(PacManGames2dUI.PY_IMMUNITY);
        contextMenu.getItems().add(immunityItem);

        contextMenu.requestFocus();
        contextMenu.show(parentScene.getRoot(), event.getScreenX(), event.getScreenY());
    }

    public Dashboard dashboard() {
        return dashboard;
    }

    public void hideContextMenu() {
        if (contextMenu != null) {
            contextMenu.hide();
        }
    }

    private MenuItem titleItem(String title) {
        var text = new Text(title);
        text.setFont(Font.font("Dialog", FontWeight.BLACK, 14));
        text.setFill(Color.CORNFLOWERBLUE); // "Kornblumenblau, sind die Augen der Frauen beim Weine..."
        return new CustomMenuItem(text);
    }

    @Override
    public void onGameSceneChanged(GameScene newGameScene) {
        if (isCurrentGameScene3D()) {
            layout().replaceCanvasLayer(newGameScene.root());
        } else {
            layout().restoreCanvasLayer();
            if (newGameScene instanceof GameScene2D scene2D) {
                scene2D.clearCanvas();
            }
        }
        hideContextMenu();
        updateTopLayer();
    }

    private void updateTopLayer() {
        dashboardLayer.setVisible(dashboard.isVisible() || PY_3D_PIP_ON.get());
        layout().requestFocus();
    }

    private boolean isCurrentGameScene3D() {
        return context.isCurrentGameScene(PLAY_SCENE_3D);
    }

    @Override
    protected boolean isCurrentGameScene2D() {
        return !isCurrentGameScene3D();
    }

    @Override
    public void render() {
        super.render();
        dashboard.update();
        pip.setVisible(PY_3D_PIP_ON.get() && isCurrentGameScene3D());
        pip.draw();
    }

    @Override
    public void handleKeyboardInput() {
        var actionHandler = (ActionHandler3D) context.actionHandler();
        if (Keyboard.pressed(KEY_SWITCH_EDITOR)) {
            actionHandler.enterMapEditor();
        } else if (Keyboard.pressed(KEY_TOGGLE_2D_3D)) {
            actionHandler.toggle2D3D();
        } else if (Keyboard.pressed(KEYS_TOGGLE_DASHBOARD)) {
            actionHandler.toggleDashboard();
        } else if (Keyboard.pressed(KEY_TOGGLE_PIP_VIEW)) {
            actionHandler.togglePipVisible();
        } else {
            super.handleKeyboardInput();
        }
    }
}