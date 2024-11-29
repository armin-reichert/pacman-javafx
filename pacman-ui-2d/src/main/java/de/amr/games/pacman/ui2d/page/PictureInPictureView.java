/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManGameTengenRenderer;
import de.amr.games.pacman.ui2d.util.Ufx;
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
        if (context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
            de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.PlayScene2D playScene2D = new de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.PlayScene2D() {
                public void draw() {
                    // do this here because it should be run also when game is paused
                    updateScaling();
                    var r = (MsPacManGameTengenRenderer) gr;
                    r.setScaling(scaling());
                    r.clearCanvas();
                    context.game().level().ifPresent(level -> {
                        r.update(level.mapConfig());
                        r.ctx().save();
                        r.ctx().translate(scaled(TS), 0);
                        drawSceneContent();
                        r.ctx().restore();
                    });
                }
            };
            playScene2D.viewPortHeightProperty().bind(canvas.heightProperty());
            playScene2D.viewPortWidthProperty().bind(canvas.widthProperty());
            scene2D = playScene2D;
        } else {
            scene2D = new PlayScene2D();
        }
        scene2D.setGameContext(context);
        scene2D.setCanvas(canvas);
        scene2D.backgroundColorProperty().bind(PY_CANVAS_BG_COLOR);
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        createScene();
        recomputeLayout();
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