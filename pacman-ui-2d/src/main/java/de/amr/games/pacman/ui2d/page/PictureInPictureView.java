/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.GameParameters;
import de.amr.games.pacman.ui2d.scene.PlayScene2D;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.HBox;

/**
 * @author Armin Reichert
 */
public class PictureInPictureView {

    public final DoubleProperty heightPy = new SimpleDoubleProperty(GameModel.ARCADE_MAP_SIZE_Y);
    public final DoubleProperty aspectPy = new SimpleDoubleProperty(0.77) {
        @Override
        protected void invalidated() {
            gameScene.init(); // updates renderer scaling
        }
    };

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
    private final PlayScene2D gameScene;

    public PictureInPictureView(GameContext context) {
        this.context = context;

        Canvas canvas = new Canvas();
        canvas.heightProperty().bind(heightPy);
        canvas.widthProperty().bind(heightPy.multiply(aspectPy));

        gameScene = new PlayScene2D();
        gameScene.setContext(context);
        gameScene.setCanvas(canvas);

        HBox pane = new HBox(canvas);
        pane.opacityProperty().bind(opacityPy);
        pane.visibleProperty().bind(visiblePy);
        pane.backgroundProperty().bind(GameParameters.PY_CANVAS_COLOR.map(Ufx::coloredBackground));
        pane.setPadding(new Insets(5,10,5,10));

        layout.getChildren().add(pane);
    }

    public void setVisible(boolean visible) {
        visiblePy.set(visible);
    }

    public Node node() {
        return layout;
    }

    public void draw() {
        if (visiblePy.get()) {
            GameWorld world = context.game().world();
            if (world != null) {
                double numCols = world.map().terrain().numCols();
                double numRows = world.map().terrain().numRows();
                aspectPy.set(numCols / numRows);
                gameScene.scalingPy.set(GameModel.ARCADE_MAP_TILES_Y / numRows);
            }
            gameScene.draw();
        }
    }
}