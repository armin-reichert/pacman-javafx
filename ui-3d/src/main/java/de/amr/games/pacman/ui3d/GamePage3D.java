/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

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
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.PacManGames2dUI.PY_IMMUNITY;
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
            displayedScene.scalingPy.bind(heightProperty().divide(DEFAULT_CANVAS_HEIGHT_UNSCALED));
            widthProperty().bind(heightProperty().multiply(0.777));
            opacityProperty().bind(PY_PIP_OPACITY_PERCENTAGE.divide(100.0));
        }

        public void draw() {
            if (isVisible()) {
                displayedScene.draw();
            }
        }
    }

    private final BorderPane dashboardLayer;
    private final Dashboard dashboard;
    private final PictureInPictureView pip;
    private ContextMenu contextMenu;

    public GamePage3D(GameSceneContext context) {
        super(context);

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
        PY_3D_PIP_ON.addListener((py, ov, nv) -> updateDashboardLayer());
        dashboard.visibleProperty().addListener((py, ov, nv) -> updateDashboardLayer());

        updateDashboardLayer();
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
            miPictureInPicture.selectedProperty().bindBidirectional(PY_3D_PIP_ON);
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

    public Dashboard dashboard() {
        return dashboard;
    }

    public void hideContextMenu() {
        if (contextMenu != null) {
            contextMenu.hide();
        }
    }

    private MenuItem menuTitleItem(String titleText) {
        var text = new Text(titleText);
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
                adaptCanvasSizeToCurrentWorld();
            }
        }
        hideContextMenu();
        updateDashboardLayer();
    }

    private void updateDashboardLayer() {
        dashboardLayer.setVisible(dashboard.isVisible() || PY_3D_PIP_ON.get());
        layout.requestFocus();
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