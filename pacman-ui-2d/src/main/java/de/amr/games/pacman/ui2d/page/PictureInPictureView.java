/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.ui.GameScene2D;
import de.amr.games.pacman.ui.PlayScene2D;
import de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManGameTengenRenderer;
import de.amr.games.pacman.ui.Ufx;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.VBox;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.TS;
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

    private static class TengenPlayScene extends de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.PlayScene2D {

        private final Canvas canvas;

        public TengenPlayScene(Canvas canvas) {
            this.canvas = canvas;
            viewPortHeightProperty().bind(canvas.heightProperty());
            viewPortWidthProperty().bind(canvas.widthProperty());
        }

        @Override
        public void draw() {
            setScaling(canvas.getHeight() / (size().y() + 3 * TS));
            var r = (MsPacManGameTengenRenderer) gr;
            r.setScaling(scaling());
            r.clearCanvas();
            context.game().level().ifPresent(level -> {
                r.ctx().save();
                r.ctx().translate(scaled(TS), 0);
                drawSceneContent();
                r.ctx().restore();
            });
        }

        public MsPacManGameTengenRenderer renderer() {
            return (MsPacManGameTengenRenderer) gr;
        }
    }

    private final Canvas canvas = new Canvas();
    private final GameContext context;
    private GameScene2D scene2D;

    public PictureInPictureView(GameContext context) {
        this.context = context;
        canvas.heightProperty().bind(PY_PIP_HEIGHT);
        canvas.heightProperty().addListener((py,ov,nv) -> recomputeLayout());
        getChildren().add(canvas);
        setPadding(new Insets(5, 15, 5, 15));
        backgroundProperty().bind(PY_CANVAS_BG_COLOR.map(Ufx::coloredBackground));
        opacityProperty().bind(PY_PIP_OPACITY_PERCENT.divide(100.0));
        visibleProperty().bind(Bindings.createObjectBinding(
            () -> PY_PIP_ON.get() && context.currentGameSceneHasID("PlayScene3D"),
            PY_PIP_ON, context.gameSceneProperty()
        ));
        visibleProperty().addListener((py,ov,visible) -> recomputeLayout());
    }

    private void createScene() {
        scene2D = context.gameVariant() == GameVariant.MS_PACMAN_TENGEN ? new TengenPlayScene(canvas) : new PlayScene2D();
        scene2D.setGameContext(context);
        scene2D.setGameRenderer(context.currentGameSceneConfig().createRenderer(canvas));
        scene2D.backgroundColorProperty().bind(PY_CANVAS_BG_COLOR);
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        createScene();
        recomputeLayout();
        if (scene2D instanceof TengenPlayScene tengenScene) {
            tengenScene.renderer().setWorldMap(context.level().world().map());
        }
    }

    public void draw() {
        if (scene2D != null && isVisible()) {
            scene2D.draw();
        }
    }

    private void recomputeLayout() {
        Vector2f sceneSize = scene2D.size();
        double canvasHeight = canvas.getHeight();
        double aspectRatio = sceneSize.x() / sceneSize.y();
        canvas.setWidth(aspectRatio * canvasHeight);
        scene2D.setScaling(canvasHeight / sceneSize.y());
        layout();
        Logger.debug("Layout recomputed, w={0.00} h={0.00} aspect={0.00}, scene size (px)={}",
            getWidth(), getHeight(), aspectRatio, sceneSize);
    }
}