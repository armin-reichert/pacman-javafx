/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.lib.Vector2f;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.VBox;
import org.tinylog.Logger;

import static de.amr.games.pacman.Globals.assertNotNull;
import static de.amr.games.pacman.ui.Globals.THE_UI;
import static de.amr.games.pacman.ui.Globals.PY_CANVAS_BG_COLOR;
import static de.amr.games.pacman.ui.Globals.PY_PIP_HEIGHT;

/**
 * Picture-in-Picture view. Adapts its aspect ratio to the current game world. Height can be changed via dashboard.
 * <p>
 * TODO: fixme: should not depend on specific game
 * TODO: For large maps we need a camera inside this view or something alike
 * </p>
 */
public class PictureInPictureView extends VBox {

    private final Canvas canvas;
    private GameScene2D scene2D;

    public PictureInPictureView() {
        setPadding(new Insets(5, 15, 5, 15));
        canvas = new Canvas();
        canvas.heightProperty().bind(PY_PIP_HEIGHT);
        canvas.heightProperty().addListener((py,ov,nv) -> recomputeLayout());
        getChildren().add(canvas);
        visibleProperty().addListener((py,ov,nv) -> recomputeLayout());
    }

    public void setScene2D(GameScene2D scene2D) {
        this.scene2D = assertNotNull(scene2D);
        GameRenderer renderer = THE_UI.configurations().current().createRenderer(canvas);
        scene2D.setGameRenderer(renderer);
        scene2D.backgroundColorProperty().bind(PY_CANVAS_BG_COLOR);
        recomputeLayout();
    }

    public void draw() {
        if (scene2D != null && isVisible()) {
            scene2D.draw();
        }
    }

    private void recomputeLayout() {
        if (scene2D != null) {
            Vector2f size = scene2D.sizeInPx();
            double aspectRatio = size.x() / size.y();
            canvas.setWidth(aspectRatio * canvas.getHeight());
            scene2D.setScaling(canvas.getHeight() / size.y());
            Logger.debug("Layout recomputed, w={0.00} h={0.00} aspect={0.00}, scene size (px)={}",
                getWidth(), getHeight(), aspectRatio, size);
        }
    }
}