/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.lib.Vector2f;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.VBox;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.optGameLevel;
import static de.amr.pacmanfx.ui.PacManGames.theUI;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;
import static java.util.Objects.requireNonNull;

/**
 * Picture-in-Picture view. Adapts its aspect ratio to the current game world. Height can be changed via dashboard.
 * <br>
 * TODO: For large maps we need a camera inside this view or something else
 */
public class PictureInPictureView extends VBox {

    private final Canvas canvas = new Canvas();
    private GameScene2D scene2D;

    public PictureInPictureView() {
        canvas.heightProperty().bind(PY_PIP_HEIGHT);
        canvas.getGraphicsContext2D().setImageSmoothing(false);
        getChildren().add(canvas);
        setPadding(new Insets(5, 15, 5, 15));
        visibleProperty().addListener((py,ov,nv) -> recomputeLayout());
        canvas.heightProperty().addListener((py,ov,nv) -> recomputeLayout());
    }

    public void setScene2D(GameScene2D scene2D) {
        this.scene2D = requireNonNull(scene2D);
        this.scene2D.setGameRenderer((SpriteGameRenderer) theUI().configuration().createRenderer(canvas));
        this.scene2D.backgroundColorProperty().bind(PY_CANVAS_BG_COLOR);
        recomputeLayout();
    }

    public void draw() {
        if (scene2D != null && isVisible() && optGameLevel().isPresent()) {
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