/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.PacManGames2dUI;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
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

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_CANVAS_BG_COLOR;

/**
 * @author Armin Reichert
 */
public class PictureInPictureView implements GameEventListener {

    public final DoubleProperty heightPy = new SimpleDoubleProperty(GameModel.ARCADE_MAP_SIZE_Y) {
        @Override
        protected void invalidated() {
            updateScaling();
        }
    };

    public final BooleanProperty visiblePy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            if (get()) {
                updateScaling();
            }
        }
    };

    private final GameContext context;
    private final HBox container;
    private final Canvas canvas = new Canvas();
    private final HBox canvasContainer;
    private final PlayScene2D gameScene;
    private GameWorldRenderer renderer;
    private double aspect = 0.777;

    public PictureInPictureView(GameContext context) {
        this.context = context;

        gameScene = new PlayScene2D();
        gameScene.setGameContext(context);
        gameScene.backgroundColorPy.bind(PY_CANVAS_BG_COLOR);

        context.gameVariantProperty().addListener((py, ov, nv) -> {
            if (nv != null) {
                renderer = PacManGames2dUI.createRenderer(nv, context.assets());
                renderer.setCanvas(canvas);
            }
        });

        canvas.heightProperty().bind(heightPy);
        canvas.widthProperty().bind(heightPy.multiply(aspect));

        canvasContainer = new HBox(canvas);
        canvasContainer.visibleProperty().bind(visiblePy);
        canvasContainer.backgroundProperty().bind(PY_CANVAS_BG_COLOR.map(Ufx::coloredBackground));
        canvasContainer.setPadding(new Insets(5, 10, 5, 10));

        container = new HBox(canvasContainer);
    }

    public DoubleProperty opacityProperty() {
        return canvasContainer.opacityProperty();
    }

    public void setVisible(boolean visible) {
        visiblePy.set(visible);
    }

    public Node node() {
        return container;
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        Vector2i worldSize = context.worldSizeTilesOrDefault();
        aspect = (double) worldSize.x() / worldSize.y();
        if (context.game().world() != null) {
            int mapNumber = context.game().mapNumberByLevelNumber(context.game().levelNumber());
            renderer.selectMap(context.game().world().map(), mapNumber, context.spriteSheet());
        }
    }

    public void draw() {
        if (visiblePy.get()) {
            gameScene.draw(renderer);
        }
    }

    private void updateScaling() {
        double referenceHeight = context.worldSizeTilesOrDefault().y() * TS;
        gameScene.scalingPy.set(heightPy.get() / referenceHeight);
    }
}