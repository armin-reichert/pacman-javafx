/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.GameParameters;
import de.amr.games.pacman.ui2d.scene.*;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.HBox;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class PictureInPictureView {

    public final DoubleProperty widthPy  = new SimpleDoubleProperty(28*8);
    public final DoubleProperty heightPy = new SimpleDoubleProperty(36*8);
    public final DoubleProperty opacityPy = new SimpleDoubleProperty(1);
    public final BooleanProperty visiblePy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            if (get()) {
                gameScene.init();
            }
        }
    };

    private final GameContext context;
    private final HBox layout = new HBox();
    private final Canvas canvas = new Canvas();
    private GameScene gameScene;

    public PictureInPictureView(GameContext context) {
        this.context = checkNotNull(context);
        canvas.widthProperty().bind(widthPy);
        canvas.heightProperty().bind(heightPy);
        widthPy.bind(heightPy.multiply(0.777));

        HBox pane = new HBox(canvas);
        pane.opacityProperty().bind(opacityPy);
        pane.visibleProperty().bind(visiblePy);
        pane.backgroundProperty().bind(GameParameters.PY_CANVAS_COLOR.map(Ufx::coloredBackground));
        pane.setPadding(new Insets(5,10,5,10));

        layout.getChildren().add(pane);

        setGameScene(new PlayScene2D());
    }

    public void setGameScene(GameScene gameScene) {
        this.gameScene = gameScene;
        if (gameScene instanceof GameScene2D gameScene2D) {
            gameScene2D.setContext(context);
            gameScene2D.setCanvas(canvas);
            gameScene2D.scalingPy.bind(heightPy.divide(36 * 8));
        }
    }

    public void setVisible(boolean visible) {
        visiblePy.set(visible);
    }

    public Node node() {
        return layout;
    }

    public void draw() {
        if (visiblePy.get()) {
            if (gameScene instanceof GameScene2D gameScene2D) {
                //gameScene2D.update();
                gameScene2D.draw();
            }
        }
    }
}