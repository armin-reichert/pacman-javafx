/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.VBox;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.*;

/**
 * Picture-in-Picture view. Adapts its aspect ratio to the current game world. Height can be changed via dashboard.
 * <p>
 * TODO: For large maps we need a camera inside this view or something alike
 * </p>
 *
 * @author Armin Reichert
 */
public class PictureInPictureView extends VBox implements GameEventListener {

    private final GameContext context;
    private final Canvas canvas;
    private final PlayScene2D playScene2D;
    private GameRenderer renderer;

    public PictureInPictureView(GameContext context) {
        this.context = checkNotNull(context);

        canvas = new Canvas();
        canvas.heightProperty().bind(PY_PIP_HEIGHT);
        canvas.heightProperty().addListener((py,ov,nv) -> recomputeLayout());

        playScene2D = new PlayScene2D();
        playScene2D.setGameContext(context);
        playScene2D.backgroundColorPy.bind(PY_CANVAS_BG_COLOR);

        getChildren().add(canvas);

        setPadding(new Insets(5, 15, 5, 15));
        backgroundProperty().bind(PY_CANVAS_BG_COLOR.map(Ufx::coloredBackground));
        opacityProperty().bind(PY_PIP_OPACITY_PERCENT.divide(100.0));
        visibleProperty().bind(Bindings.createObjectBinding(
            () -> PY_PIP_ON.get() && context.currentGameSceneHasID("PlayScene3D"),
            PY_PIP_ON, context.gameSceneProperty()
        ));
        visibleProperty().addListener((py,ov,visible) -> {
            if (visible) {
                recomputeLayout();
            }
        });

        context.gameVariantProperty().addListener((py,ov,variant) -> {
            renderer = context.currentGameSceneConfig().renderer().copy();
            renderer.setCanvas(canvas);
            renderer.setRendererFor(context.game());
        });
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        renderer.setRendererFor(context.game());
        recomputeLayout();
    }

    public void draw() {
        if (isVisible()) {
            playScene2D.draw(renderer);
        }
    }

    private double worldAspectRatio() {
        Vector2i worldTiles = context.worldSizeTilesOrDefault();
        return (double) worldTiles.x() / (double) worldTiles.y();
    }

    private double standardHeight() {
        Vector2i worldTiles = context.worldSizeTilesOrDefault();
        return worldTiles.y() * TS;
    }

    private void recomputeLayout() {
        double canvasHeight = canvas.getHeight();
        double aspectRatio = worldAspectRatio();
        canvas.setWidth(aspectRatio * canvasHeight);
        playScene2D.scalingPy.set(canvasHeight / standardHeight());
        layout();
        Logger.info("Layout recomputed, w={0.00} h={0.00} aspect={0.00}, world size (tiles)={}",
            getWidth(), getHeight(), aspectRatio, context.worldSizeTilesOrDefault());
    }
}