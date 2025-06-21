/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.ui._2d.GameRenderer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
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

public class MiniGameView {

    private final DoubleProperty aspectPy = new SimpleDoubleProperty(28.0/36.0);
    private final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(Color.BLACK);
    private final BooleanProperty debugPy = new SimpleBooleanProperty(false);
    private final DoubleProperty heightPy = new SimpleDoubleProperty(400);
    private final DoubleProperty opacityPy = new SimpleDoubleProperty(1);
    private final FloatProperty scalingPy = new SimpleFloatProperty(1.0f);
    private final BooleanProperty visiblePy = new SimpleBooleanProperty(false);
    private final ObjectProperty<Vector2f> worldSizePy = new SimpleObjectProperty<>(Vector2f.of(28*TS,36*TS));

    private final VBox root = new VBox();
    private final Canvas canvas = new Canvas();
    private final GraphicsContext ctx = canvas.getGraphicsContext2D();
    private long drawCount;
    private GameRenderer gr;

    public MiniGameView() {
        // The VBox fills the complete parent container height (why?), so we put the canvas
        // into an HBox that does not grow in height and provides some padding around the canvas
        HBox canvasContainer = new HBox(canvas);
        canvasContainer.setPadding(new Insets(0, 10, 0, 10));
        canvasContainer.backgroundProperty().bind(backgroundColorProperty().map(Background::fill));
        VBox.setVgrow(canvasContainer, Priority.NEVER);

        // Keep canvas at current aspect which depends on the current game level's world size
        canvas.heightProperty().bind(heightProperty());
        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> aspectPy.doubleValue() * canvas.getHeight(),
            canvas.heightProperty(), aspectPy
        ));

        root.getChildren().add(canvasContainer);
        root.opacityProperty().bind(opacityProperty());
        root.visibleProperty().bind(visibleProperty());

        scalingPy.bind(Bindings.createFloatBinding(
            () -> (float)canvas.getHeight() / worldSizePy.get().y(),
            canvas.heightProperty(), worldSizePy
        ));
        aspectPy.bind(worldSizePy.map(size -> size.x() / size.y()));

        worldSizePy.set(Vector2f.of(28*TS,36*TS));
    }

    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColorPy;
    }

    public BooleanProperty debugProperty() {
        return debugPy;
    }

    public DoubleProperty heightProperty() {
        return heightPy;
    }

    public DoubleProperty opacityProperty() {
        return opacityPy;
    }

    public BooleanProperty visibleProperty() {
        return visiblePy;
    }

    public void draw() {
        drawCount += 1;
        ctx.setFill(backgroundColorProperty().get());
        ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (optGameLevel().isPresent() && theUI().currentGameSceneIsPlayScene3D()) {
            drawGameScene(gr);
        }
        if (debugProperty().get()) {
            drawInfo();
        }
    }

    public void onLevelCreated(GameLevel gameLevel) {
        worldSizePy.set(gameLevel.worldSizePx());
        gr = theUI().configuration().createGameRenderer(canvas);
        gr.applyRenderingHints(gameLevel);
        gr.setScaling(scalingPy.floatValue());
    }

    private void drawGameScene(GameRenderer gr) {
        gr.setScaling(scalingPy.floatValue());
        gr.drawLevel(theGameLevel(), Color.BLACK, false, false);
        var actors = new ArrayList<Actor>();
        theGameLevel().bonus().map(Bonus::actor).ifPresent(actors::add);
        actors.add(theGameLevel().pac());
        Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW)
            .map(theGameLevel()::ghost).forEach(actors::add);
        actors.forEach(gr::drawActor);
    }

    private void drawInfo() {
        double scaling = scalingPy.floatValue();
        ctx.save();
        ctx.setFill(Color.WHITE);
        ctx.setFont(Font.font(14 * scaling));
        ctx.setTextAlign(TextAlignment.CENTER);
        ctx.fillText("scaling: %.2f, draws: %d".formatted(scaling, drawCount), canvas.getWidth() * 0.5, 16 * scaling);
        ctx.restore();
    }

    public Pane root() {
        return root;
    }
}
