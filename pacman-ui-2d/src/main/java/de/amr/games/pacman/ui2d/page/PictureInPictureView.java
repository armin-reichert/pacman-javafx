/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.MsPacManArcadeGameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.pacman.PacManArcadeGameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.pacman_xxl.PacManXXLGameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.tengen.TengenMsPacManGameWorldRenderer;
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
import org.tinylog.Logger;

import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_CANVAS_COLOR;

/**
 * @author Armin Reichert
 */
public class PictureInPictureView implements GameEventListener {

    public final DoubleProperty heightPy = new SimpleDoubleProperty(GameModel.ARCADE_MAP_SIZE_Y) {
        @Override
        protected void invalidated() {
            rescale();
        }
    };

    private final DoubleProperty aspectPy = new SimpleDoubleProperty(0.77) {
        @Override
        protected void invalidated() {
            rescale();
        }
    };

    public final DoubleProperty opacityPy = new SimpleDoubleProperty(1);

    public final BooleanProperty visiblePy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            if (get()) {
                rescale();
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
        gameScene.setGameContext(context);
        gameScene.setCanvas(canvas);

        HBox pane = new HBox(canvas);
        pane.opacityProperty().bind(opacityPy);
        pane.visibleProperty().bind(visiblePy);
        pane.backgroundProperty().bind(PY_CANVAS_COLOR.map(Ufx::coloredBackground));
        pane.setPadding(new Insets(5,10,5,10));

        layout.getChildren().add(pane);
    }

    public void setVisible(boolean visible) {
        visiblePy.set(visible);
    }

    public Node node() {
        return layout;
    }

    public void setGameVariant(GameVariant variant) {
        GameWorldRenderer renderer = switch (variant) {
            case MS_PACMAN -> new MsPacManArcadeGameWorldRenderer(context.assets());
            case MS_PACMAN_TENGEN -> new TengenMsPacManGameWorldRenderer(context.assets());
            case PACMAN -> new PacManArcadeGameWorldRenderer(context.assets());
            case PACMAN_XXL -> new PacManXXLGameWorldRenderer(context.assets());
        };
        renderer.scalingProperty().bind(gameScene.scalingPy);
        renderer.backgroundColorProperty().bind(gameScene.backgroundColorPy);
        gameScene.setRenderer(renderer);
        gameScene.backgroundColorPy.bind(PY_CANVAS_COLOR);
    }

    public void draw() {
        if (visiblePy.get()) {
            gameScene.draw();
        }
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        if (context.game().world() != null) {
            TileMap terrain = context.game().world().map().terrain();
            aspectPy.set((double) terrain.numCols() / terrain.numRows());
        }
    }

    private void rescale() {
        double referenceHeight = context.game().world() != null
            ? context.game().world().map().terrain().numRows() * Globals.TS
            : GameModel.ARCADE_MAP_SIZE_Y;
        double scaling = heightPy.get() / referenceHeight;
        Logger.debug("PiP scaling: {}", scaling);
        gameScene.scalingPy.set(scaling);
        gameScene.init();
    }
}