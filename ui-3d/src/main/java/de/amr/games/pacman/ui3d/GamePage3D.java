/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.ui2d.PacManGames2dUI;
import de.amr.games.pacman.ui2d.page.GamePage;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameSceneContext;
import de.amr.games.pacman.ui2d.scene.PlayScene2D;
import de.amr.games.pacman.ui2d.util.Keyboard;
import de.amr.games.pacman.ui2d.util.Ufx;
import de.amr.games.pacman.ui3d.dashboard.Dashboard;
import de.amr.games.pacman.ui3d.scene.Perspective;
import de.amr.games.pacman.ui3d.scene.PlayScene3D;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.games.pacman.ui3d.PacManGames3dUI.*;

/**
 * @author Armin Reichert
 */
public class GamePage3D extends GamePage {

    private final BorderPane dashboardLayer;
    private final Dashboard dashboard;

    private final Canvas pipCanvas = new Canvas();
    private final PlayScene2D pip = new PlayScene2D();
    private ContextMenu contextMenu;

    public GamePage3D(GameSceneContext sceneContext, double width, double height) {
        super(sceneContext, width, height);

        dashboard = new Dashboard(sceneContext);
        initPip();

        dashboardLayer = new BorderPane();
        dashboardLayer.setLeft(dashboard);
        dashboardLayer.setRight(pip.canvas());
        getLayersContainer().getChildren().add(dashboardLayer);

        canvasLayer.setBackground(sceneContext.theme().background("wallpaper.background"));

        // data binding
        PY_3D_DRAW_MODE.addListener((py, ov, nv) -> updateBackground3D());
        PY_3D_NIGHT_MODE.addListener((py, ov, nv) -> updateBackground3D());
        PY_PIP_ON.addListener((py, ov, nv) -> updateTopLayer());
        dashboard.visibleProperty().addListener((py, ov, nv) -> updateTopLayer());

        updateTopLayer();
    }

    public Optional<ContextMenu> currentContextMenu() {
        return Optional.ofNullable(contextMenu);
    }

    public void showContextMenu(Node node, double x, double y) {
        if (context.currentGameScene().isEmpty()) {
            Logger.warn("No game scene exists when context menu was opened");
            return;
        }
        contextMenu = new ContextMenu();
        populateContextMenu(context.currentGameScene().get());
        contextMenu.show(node, x, y);
    }

    private void populateContextMenu(GameScene gameScene) {
        var actionHandler = (ActionHandler3D) context.actionHandler();
        contextMenu.getItems().add(titleItem(context.tt("scene_display")));
        if (gameScene instanceof PlayScene2D) {
            var item = new MenuItem(context.tt("use_3D_scene"));
            item.setOnAction(e -> actionHandler.toggle2D3D());
            contextMenu.getItems().add(item);
        } else if (gameScene instanceof PlayScene3D) {
            var item = new MenuItem(context.tt("use_2D_scene"));
            item.setOnAction(e -> actionHandler.toggle2D3D());
            contextMenu.getItems().add(item);
            var pipItem = new CheckMenuItem(context.tt("pip"));
            pipItem.selectedProperty().bindBidirectional(PY_PIP_ON);
            contextMenu.getItems().add(pipItem);
            contextMenu.getItems().add(titleItem(context.tt("select_perspective")));
            var toggleGroup = new ToggleGroup();
            for (var perspective : Perspective.values()) {
                var radioMenuItem = new RadioMenuItem(context.tt(perspective.name()));
                radioMenuItem.setSelected(perspective.equals(PY_3D_PERSPECTIVE.get()));
                radioMenuItem.setUserData(perspective);
                radioMenuItem.setToggleGroup(toggleGroup);
                contextMenu.getItems().add(radioMenuItem);
            }
            toggleGroup.selectedToggleProperty().addListener((py, ov, nv) -> {
                if (nv != null) {
                    // Note: These are the user data of the radio menu item!
                    PY_3D_PERSPECTIVE.set((Perspective) nv.getUserData());
                }
            });
            PY_3D_PERSPECTIVE.addListener((py, ov, nv) -> {
                for (var radioMenuItem : toggleGroup.getToggles()) {
                    boolean selected = radioMenuItem.getUserData().equals(PY_3D_PERSPECTIVE.get());
                    radioMenuItem.setSelected(selected);
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
    }

    private MenuItem titleItem(String title) {
        var text = new Text(title);
        text.setFont(Font.font("Dialog", FontWeight.BLACK, 14));
        text.setFill(Color.CORNFLOWERBLUE); // "Kornblumenblau, sind die Augen der Frauen beim Weine..."
        return new CustomMenuItem(text);
    }

    public void hideContextMenu() {
        if (contextMenu != null) {
            contextMenu.hide();
        }
    }

    private void initPip() {
        final double ASPECT_RATIO = 0.777;
        pip.setContext(context);
        pipCanvas.heightProperty().bind(PY_PIP_HEIGHT);
        pipCanvas.widthProperty().bind(Bindings.createDoubleBinding(() -> pipCanvas.getHeight() * ASPECT_RATIO, PY_PIP_HEIGHT));
        pipCanvas.opacityProperty().bind(PY_PIP_OPACITY_PERCENTAGE.divide(100.0));
        pip.scalingPy.bind(Bindings.createDoubleBinding(() -> pipCanvas.getHeight() / CANVAS_HEIGHT_UNSCALED, PY_PIP_HEIGHT));
        pip.setCanvas(pipCanvas);
        pip.setScoreVisible(true);
    }

    @Override
    public void onGameSceneChanged(GameScene newGameScene) {
        if (isCurrentGameScene3D()) {
            updateHelpButton();
            getLayersContainer().getChildren().set(0, newGameScene.root());
        } else {
            getLayersContainer().getChildren().set(0, canvasLayer);
            super.onGameSceneChanged(newGameScene);
        }
        hideContextMenu();
        updateTopLayer();
        updateBackground3D();
    }

    private void updateTopLayer() {
        dashboardLayer.setVisible(dashboard.isVisible() || PY_PIP_ON.get());
        getLayersContainer().requestFocus();
    }

    private void updateBackground3D() {
        if (isCurrentGameScene3D()) {
            if (PY_3D_DRAW_MODE.get() == DrawMode.LINE) {
                getLayersContainer().setBackground(Ufx.coloredBackground(Color.BLACK));
            } else {
                var wallpaperKey = PY_3D_NIGHT_MODE.get() ? "model3D.wallpaper.night" : "model3D.wallpaper";
                getLayersContainer().setBackground(context.theme().background(wallpaperKey));
            }
        }
    }

    private boolean isCurrentGameScene3D() {
        return context.currentGameScene().isPresent()
            && context.currentGameScene().get() instanceof PlayScene3D;
    }

    @Override
    protected boolean isCurrentGameScene2D() {
        return !isCurrentGameScene3D();
    }

    @Override
    public void render() {
        super.render();
        dashboard.update();
        pip.canvas().setVisible(PY_PIP_ON.get() && isCurrentGameScene3D());
        pip.draw();
    }

    @Override
    protected void handleKeyboardInput() {
        var actionHandler = (ActionHandler3D) context.actionHandler();
        if (Keyboard.pressed(KEY_TOGGLE_2D_3D)) {
            actionHandler.toggle2D3D();
        } else if (Keyboard.pressed(KEYS_TOGGLE_DASHBOARD)) {
            dashboard.toggleVisibility();
        } else if (Keyboard.pressed(KEY_TOGGLE_PIP_VIEW)) {
            actionHandler.togglePipVisible();
        } else {
            super.handleKeyboardInput();
        }
    }
}