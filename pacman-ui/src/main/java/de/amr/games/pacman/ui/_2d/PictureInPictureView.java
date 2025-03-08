/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.uilib.Ufx;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.VBox;
import org.tinylog.Logger;

/**
 * Picture-in-Picture view. Adapts its aspect ratio to the current game world. Height can be changed via dashboard.
 * <p>
 * TODO: fixme: should not depend on specific game
 * TODO: For large maps we need a camera inside this view or something alike
 * </p>
 */
public class PictureInPictureView extends VBox {

    private final GameContext context;
    private final Canvas canvas = new Canvas();
    private GameScene2D scene2D;

    public PictureInPictureView(GameContext context) {
        this.context = Globals.assertNotNull(context);
        canvas.heightProperty().bind(GlobalProperties2d.PY_PIP_HEIGHT);
        canvas.heightProperty().addListener((py,ov,nv) -> recomputeLayout());
        getChildren().add(canvas);
        setPadding(new Insets(5, 15, 5, 15));
        backgroundProperty().bind(GlobalProperties2d.PY_CANVAS_BG_COLOR.map(Ufx::coloredBackground));
        opacityProperty().bind(GlobalProperties2d.PY_PIP_OPACITY_PERCENT.divide(100.0));
        visibleProperty().bind(Bindings.createObjectBinding(
            () -> GlobalProperties2d.PY_PIP_ON.get() && context.currentGameSceneHasID("PlayScene3D"),
            GlobalProperties2d.PY_PIP_ON, context.gameSceneProperty()
        ));
        visibleProperty().addListener((py,ov,visible) -> recomputeLayout());
    }

    public void setScene2D(GameScene2D scene2D) {
        this.scene2D = Globals.assertNotNull(scene2D);
        scene2D.setGameContext(context);
        scene2D.setGameRenderer(context.gameConfiguration().createRenderer(context.assets(), canvas));
        scene2D.backgroundColorProperty().bind(GlobalProperties2d.PY_CANVAS_BG_COLOR);
        scene2D.renderer().setWorldMap(context.level().map());
        recomputeLayout();
    }

    public void draw() {
        if (scene2D != null && isVisible()) {
            scene2D.draw();
        }
    }

    private void recomputeLayout() {
        Vector2f size = scene2D != null ? scene2D.size() : Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
        double canvasHeight = canvas.getHeight();
        double aspectRatio = size.x() / size.y();
        canvas.setWidth(aspectRatio * canvasHeight);
        if (scene2D != null) {
            scene2D.setScaling(canvasHeight / size.y());
        }
        layout();
        Logger.debug("Layout recomputed, w={0.00} h={0.00} aspect={0.00}, scene size (px)={}",
            getWidth(), getHeight(), aspectRatio, size);
    }
}