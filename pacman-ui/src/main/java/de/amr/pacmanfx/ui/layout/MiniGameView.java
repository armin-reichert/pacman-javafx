package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.ui._2d.GameRenderer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.PacManGames.theUI;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;
import static de.amr.pacmanfx.ui.PacManGames_UI.PY_PIP_ON;

public class MiniGameView {

    private final DoubleProperty aspectPy = new SimpleDoubleProperty(28.0/36.0);
    private final VBox root = new VBox();
    private final Canvas canvas = new Canvas();
    private final GraphicsContext ctx = canvas.getGraphicsContext2D();
    private long drawCount;
    private float scaling;
    private GameRenderer gr;

    public MiniGameView() {
        HBox hBox = new HBox(canvas);
        hBox.setPadding(new Insets(10));
        hBox.backgroundProperty().bind(PY_CANVAS_BG_COLOR.map(Background::fill));
        VBox.setVgrow(hBox, Priority.NEVER);

        canvas.heightProperty().bind(PY_PIP_HEIGHT);
        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> aspectPy.doubleValue() * canvas.getHeight(),
            canvas.heightProperty(), aspectPy
        ));

        root.getChildren().add(hBox);
        root.opacityProperty().bind(PY_PIP_OPACITY_PERCENT.divide(100.0));
        root.visibleProperty().bind(Bindings.createObjectBinding(
            () -> PY_PIP_ON.get() && theUI().currentGameSceneIsPlayScene3D(),
            PY_PIP_ON, theUI().currentGameSceneProperty()
        ));
    }

    public void draw() {
        drawCount += 1;
        ctx.setFill(PY_CANVAS_BG_COLOR.get());
        ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (optGameLevel().isPresent() && theUI().currentGameSceneIsPlayScene3D()) {
            Vector2f worldSizePxSize = theGameLevel().worldSizePx();
            scaling = (float) (canvas.getHeight() / worldSizePxSize.y());
            aspectPy.set(worldSizePxSize.x() / worldSizePxSize.y());
            createGameRenderer(theGameLevel(), scaling);
            drawGameScene(gr);
        }
        if (PY_DEBUG_INFO_VISIBLE.get()) {
            drawInfo();
        }
    }

    private void createGameRenderer(GameLevel gameLevel, float scaling) {
        gr = theUI().configuration().createGameRenderer(canvas);
        gr.setScaling(scaling);
        gr.applyRenderingHints(gameLevel);
    }

    private void drawGameScene(GameRenderer gr) {
        gr.drawLevel(theGameLevel(), Color.BLACK, false, false);
        var actors = new ArrayList<Actor>();
        theGameLevel().bonus().map(Bonus::actor).ifPresent(actors::add);
        actors.add(theGameLevel().pac());
        Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW)
            .map(theGameLevel()::ghost).forEach(actors::add);
        actors.forEach(gr::drawActor);
    }

    private void drawInfo() {
        ctx.save();
        ctx.setFill(Color.WHITE);
        ctx.setFont(Font.font(14*scaling));
        ctx.setTextAlign(TextAlignment.CENTER);
        ctx.fillText("scaling: %.2f, draws: %d".formatted(scaling, drawCount), canvas.getWidth() * 0.5, 16*scaling);
        ctx.restore();
    }

    public Pane root() {
        return root;
    }
}
