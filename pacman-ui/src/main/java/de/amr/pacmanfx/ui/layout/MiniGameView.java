/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._2d.GameRenderer;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;

public class MiniGameView extends VBox {

    private static final Vector2f ARCADE_SIZE = Vector2f.of(28, 36);

    private final FloatProperty aspectProperty                  = new SimpleFloatProperty(ARCADE_SIZE.x() / ARCADE_SIZE.y());
    private final ObjectProperty<Color> backgroundColorProperty = new SimpleObjectProperty<>(Color.BLACK);
    private final BooleanProperty debugProperty                 = new SimpleBooleanProperty(false);
    private final DoubleProperty canvasHeightProperty           = new SimpleDoubleProperty(400);
    private final FloatProperty scalingProperty                 = new SimpleFloatProperty(1.0f);
    private final ObjectProperty<Vector2f> worldSizeProperty    = new SimpleObjectProperty<>(ARCADE_SIZE.scaled(TS));

    private final Canvas canvas;
    private final HBox canvasContainer;
    private GameRenderer gr;
    private long drawCallCount;

    private final Animation moveIntoScreenAnimation;

    public MiniGameView() {
        canvas = new Canvas();
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

        scalingProperty.bind(Bindings.createFloatBinding(
            () -> (float) canvas.getHeight() / worldSizeProperty.get().y(),
            canvas.heightProperty(), worldSizeProperty
        ));
        aspectProperty.bind(worldSizeProperty.map(size -> size.x() / size.y()));

        moveIntoScreenAnimation = createMoveIntoScreenAnimation();
    }

    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColorProperty;
    }

    public BooleanProperty debugProperty() {
        return debugProperty;
    }

    public DoubleProperty canvasHeightProperty() {
        return canvasHeightProperty;
    }

    public void onLevelCreated(GameUI ui, GameLevel gameLevel) {
        worldSizeProperty.set(gameLevel.worldSizePx());
        gr = ui.theConfiguration().createGameRenderer(canvas);
        gr.applyRenderingHints(gameLevel);
        gr.setScaling(scalingProperty.floatValue());
        moveIntoScreenAnimation.play();
    }

    public void onLevelCompleted() {
        Animation moveOffAnimation = createMoveOffScreenAnimation();
        moveOffAnimation.play();
    }

    private Animation createMoveIntoScreenAnimation() {
        var transition = new TranslateTransition(Duration.seconds(1), this);
        transition.setToY(0);
        transition.setByY(10);
        transition.setDelay(Duration.seconds(1));
        transition.setInterpolator(Interpolator.EASE_OUT);
        return transition;
    }

    private Animation createMoveOffScreenAnimation() {
        var transition = new TranslateTransition(Duration.seconds(2), this);
        transition.setToY(-canvasContainer.getHeight());
        transition.setByY(10);
        transition.setDelay(Duration.seconds(2));
        transition.setInterpolator(Interpolator.EASE_IN);
        return transition;
    }

    public void draw(GameUI ui, GameLevel gameLevel) {
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        float scaling = scalingProperty.floatValue();
        ctx.setFill(backgroundColorProperty().get());
        ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (gr == null) {
            Logger.warn("Cannot draw game scene without game renderer");
            return;
        }
        if (gameLevel == null) {
            Logger.warn("No game level to draw in mini game view");
            return;
        }
        gr.setScaling(scaling);
        gr.drawLevel(ui.theGameContext(),
            gameLevel,
            backgroundColorProperty().get(),
            false,
            gameLevel.blinking().isOn(),
            ui.theGameClock().tickCount());
        gameLevel.bonus().ifPresent(gr::drawActor);
        gr.drawActor(gameLevel.pac());
        Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW)
                .map(gameLevel::ghost)
                .forEach(gr::drawActor);
        if (debugProperty().get()) {
            ctx.save();
            ctx.setTextAlign(TextAlignment.CENTER);
            ctx.setFill(Color.WHITE);
            ctx.setFont(Font.font(14 * scaling));
            ctx.fillText("scaling: %.2f, draw calls: %d".formatted(scaling, drawCallCount), canvas.getWidth() * 0.5, 16 * scaling);
            ctx.restore();
        }
        drawCallCount += 1;
    }
}