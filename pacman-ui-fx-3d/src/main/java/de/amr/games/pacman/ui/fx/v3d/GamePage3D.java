/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d;

import de.amr.games.pacman.ui.fx.GameScene;
import de.amr.games.pacman.ui.fx.GameSceneContext;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import de.amr.games.pacman.ui.fx.page.GamePage;
import de.amr.games.pacman.ui.fx.scene2d.PlayScene2D;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.v3d.dashboard.Dashboard;
import de.amr.games.pacman.ui.fx.v3d.scene3d.PlayScene3D;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dUI.*;

/**
 * @author Armin Reichert
 */
public class GamePage3D extends GamePage {

    private final BorderPane dashboardLayer;
    private final Dashboard dashboard;
    private final GamePageContextMenu contextMenu;

    private final DoubleProperty pipHeightPy = new SimpleDoubleProperty(this, "pipHeight", PIP_MIN_HEIGHT) {
        @Override
        protected void invalidated() {
              pip.setScaling(get() / CANVAS_HEIGHT_UNSCALED);
        }
    };

    private final Canvas pipCanvas = new Canvas();
    private final PlayScene2D pip = new PlayScene2D();

    public GamePage3D(Scene parentScene, GameSceneContext sceneContext, double width, double height) {
        super(sceneContext, width, height);

        dashboard = new Dashboard(sceneContext);
        contextMenu = new GamePageContextMenu(sceneContext, parentScene);
        initPip();

        dashboardLayer = new BorderPane();
        dashboardLayer.setLeft(dashboard);
        dashboardLayer.setRight(pip.canvas());
        getLayersContainer().getChildren().add(dashboardLayer);

        canvasLayer.setBackground(sceneContext.theme().background("wallpaper.background"));

        // data binding
        pipHeightPy.bind(PY_PIP_HEIGHT);
        PY_3D_DRAW_MODE.addListener((py, ov, nv) -> updateBackground3D());
        PY_3D_NIGHT_MODE.addListener((py, ov, nv) -> updateBackground3D());
        PY_PIP_ON.addListener((py, ov, nv) -> updateTopLayer());
        dashboard.visibleProperty().addListener((py, ov, nv) -> updateTopLayer());

        updateTopLayer();
    }

    private void initPip() {
        final double ASPECT_RATIO = (double) CANVAS_WIDTH_UNSCALED / CANVAS_HEIGHT_UNSCALED;
        pip.setContext(sceneContext);
        pipCanvas.heightProperty().bind(pipHeightPy);
        pipCanvas.widthProperty().bind(Bindings.createDoubleBinding(() -> pipHeightPy.get() * ASPECT_RATIO, pipHeightPy));
        pipCanvas.opacityProperty().bind(PY_PIP_OPACITY);
        pip.setCanvas(pipCanvas);
        pip.setScaling(pipHeightPy.get() / CANVAS_HEIGHT_UNSCALED);
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
        contextMenu.hide();
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
                getLayersContainer().setBackground(ResourceManager.coloredBackground(Color.BLACK));
            } else {
                var wallpaperKey = PY_3D_NIGHT_MODE.get() ? "model3D.wallpaper.night" : "model3D.wallpaper";
                getLayersContainer().setBackground(sceneContext.theme().background(wallpaperKey));
            }
        }
    }

    private boolean isCurrentGameScene3D() {
        return sceneContext.currentGameScene().isPresent()
            && sceneContext.currentGameScene().get() instanceof PlayScene3D;
    }

    @Override
    protected boolean isCurrentGameScene2D() {
        return !isCurrentGameScene3D();
    }

    @Override
    public void render() {
        super.render();
        contextMenu.updateState();
        dashboard.update();
        pip.canvas().setVisible(PY_PIP_ON.get() && isCurrentGameScene3D());
        pip.draw();
    }

    @Override
    protected void handleKeyboardInput() {
        var actionHandler = (ActionHandler3D) sceneContext.actionHandler();
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