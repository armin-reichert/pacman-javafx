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
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.PacManGames.theUI;

public class MiniGameView extends VBox {

    private final DoubleProperty aspectProperty = new SimpleDoubleProperty(28.0/36.0);
    private final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(Color.BLACK);
    private final BooleanProperty debugPy = new SimpleBooleanProperty(false);
    private final DoubleProperty canvasHeightProperty = new SimpleDoubleProperty(400);
    private final FloatProperty scalingPy = new SimpleFloatProperty(1.0f);
    private final ObjectProperty<Vector2f> worldSizePy = new SimpleObjectProperty<>(Vector2f.of(28*TS,36*TS));

    private final Canvas canvas;
    private final HBox canvasContainer;
    private long drawCount;
    private GameRenderer gr;

    public MiniGameView() {
        canvas = new Canvas();
        // Keep canvas at current aspect which depends on the current game level's world size
        canvas.heightProperty().bind(canvasHeightProperty());
        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> aspectProperty.get() * canvas.getHeight(),
            aspectProperty, canvas.heightProperty()
        ));

        // The VBox fills the complete parent container height (why?), so we put the canvas
        // into an HBox that does not grow in height and provides some padding around the canvas
        canvasContainer = new HBox(canvas);
        canvasContainer.setPadding(new Insets(0, 10, 0, 10));
        canvasContainer.backgroundProperty().bind(backgroundColorProperty().map(Background::fill));
        VBox.setVgrow(canvasContainer, Priority.NEVER);
        getChildren().add(canvasContainer);

        scalingPy.bind(Bindings.createFloatBinding(
            () -> (float) canvas.getHeight() / worldSizePy.get().y(),
            canvas.heightProperty(), worldSizePy
        ));
        aspectProperty.bind(worldSizePy.map(size -> size.x() / size.y()));

        worldSizePy.set(Vector2f.of(28*TS,36*TS));
    }

    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColorPy;
    }

    public BooleanProperty debugProperty() {
        return debugPy;
    }

    public DoubleProperty canvasHeightProperty() {
        return canvasHeightProperty;
    }

    public void onLevelCreated(GameLevel gameLevel) {
        worldSizePy.set(gameLevel.worldSizePx());
        gr = theUI().configuration().createGameRenderer(canvas);
        gr.applyRenderingHints(gameLevel);
        gr.setScaling(scalingPy.floatValue());
        moveDownIntoScreen();
    }

    public void onLevelCompleted(GameLevel level) {
        moveUpOffScreen();
    }

    private void moveDownIntoScreen() {
        var transition = new TranslateTransition(Duration.seconds(1), this);
        transition.setToY(0);
        transition.setByY(10);
        transition.setDelay(Duration.seconds(2));
        transition.setInterpolator(Interpolator.EASE_OUT);
        transition.play();
    }

    private void moveUpOffScreen() {
        var transition = new TranslateTransition(Duration.seconds(2), this);
        transition.setToY(-canvasContainer.getHeight());
        transition.setByY(10);
        transition.setDelay(Duration.seconds(2));
        transition.setInterpolator(Interpolator.EASE_IN);
        transition.play();
    }

    public void draw(GameLevel gameLevel) {
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        float scaling = scalingPy.floatValue();
        ctx.setFill(backgroundColorProperty().get());
        ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (gr == null) {
            Logger.warn("Cannot draw game scene without game renderer");
            return;
        }
        drawGameScene(gameLevel, scaling);
        if (debugProperty().get()) {
            drawInfo(scaling);
        }
        drawCount += 1;
    }

    private void drawGameScene(GameLevel gameLevel, float scaling) {
        gr.setScaling(scaling);
        gr.drawLevel(gameLevel, backgroundColorProperty().get(), false, gameLevel.blinking().isOn());
        var actors = new ArrayList<Actor>();
        gameLevel.bonus().map(Bonus::actor).ifPresent(actors::add);
        actors.add(gameLevel.pac());
        Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW).map(gameLevel::ghost).forEach(actors::add);
        actors.forEach(gr::drawActor);
    }

    private void drawInfo(float scaling) {
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.save();
        ctx.setFill(Color.WHITE);
        ctx.setFont(Font.font(14 * scaling));
        ctx.setTextAlign(TextAlignment.CENTER);
        ctx.fillText("scaling: %.2f, draw calls: %d".formatted(scaling, drawCount), canvas.getWidth() * 0.5, 16 * scaling);
        ctx.restore();
    }
}