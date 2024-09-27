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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.VBox;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_CANVAS_BG_COLOR;

/**
 * @author Armin Reichert
 */
public class PictureInPictureView extends VBox implements GameEventListener {

    public final DoubleProperty canvasHeightPy = new SimpleDoubleProperty(GameModel.ARCADE_MAP_SIZE_Y);

    private final GameContext context;
    private final PlayScene2D playScene2D;
    private GameWorldRenderer renderer;
    private double aspect = 0.777;

    public PictureInPictureView(GameContext context) {
        this.context = context;

        backgroundProperty().bind(PY_CANVAS_BG_COLOR.map(Ufx::coloredBackground));
        setPadding(new Insets(5, 10, 5, 10));

        playScene2D = new PlayScene2D();
        playScene2D.backgroundColorPy.bind(PY_CANVAS_BG_COLOR);
        playScene2D.scalingPy.bind(canvasHeightPy.divide(context.worldSizeTilesOrDefault().y() * TS));
        playScene2D.setGameContext(context);

        var canvas = new Canvas();
        //TODO check when renderer must be (re)created
        context.gameVariantProperty().addListener((py, ov, nv) -> {
            if (nv != null) {
                renderer = PacManGames2dUI.createRenderer(nv, context.assets());
                renderer.setCanvas(canvas);
            }
        });
        canvas.heightProperty().bind(canvasHeightPy);
        canvas.widthProperty().bind(canvasHeightPy.multiply(aspect));

        getChildren().add(canvas);
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        Vector2i worldSize = context.worldSizeTilesOrDefault();
        aspect = (double) worldSize.x() / worldSize.y();
        context.attachRendererToCurrentMap(renderer);
    }

    public void draw() {
        if (isVisible()) {
            playScene2D.draw(renderer);
        }
    }
}