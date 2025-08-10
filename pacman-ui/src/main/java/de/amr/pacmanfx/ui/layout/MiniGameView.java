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
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.GameUI_Config.SCENE_ID_PLAY_SCENE_3D;
import static java.util.Objects.requireNonNull;

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

    private GameUI ui;
    private GameRenderer gr;
    private GameLevel gameLevel;
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

    public void setGameUI(GameUI ui) {
        this.ui = requireNonNull(ui);
        backgroundColorProperty().bind(GameUI.PROPERTY_CANVAS_BACKGROUND_COLOR);
        debugProperty().bind(GameUI.PROPERTY_DEBUG_INFO_VISIBLE);
        canvasHeightProperty().bind(GameUI.PROPERTY_MINI_VIEW_HEIGHT);
        opacityProperty().bind(GameUI.PROPERTY_MINI_VIEW_OPACITY_PERCENT.divide(100.0));
        visibleProperty().bind(Bindings.createObjectBinding(
            () -> GameUI.PROPERTY_MINI_VIEW_ON.get() && ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_3D),
            GameUI.PROPERTY_MINI_VIEW_ON, GameUI.PROPERTY_CURRENT_GAME_SCENE
        ));
    }

    public void setGameLevel(GameLevel gameLevel) {
        this.gameLevel = requireNonNull(gameLevel);
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

    public void draw() {
        drawCallCount += 1;
        if (gr == null) {
            return;
        }
        float scaling = scalingProperty.get();
        gr.setScaling(scaling);
        gr.ctx().setFill(backgroundColorProperty().get());
        gr.ctx().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (gameLevel != null) {
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
        }

        if (debugProperty().get()) {
            gr.ctx().save();
            gr.ctx().setTextAlign(TextAlignment.CENTER);
            gr.ctx().setFill(Color.WHITE);
            gr.ctx().setFont(Font.font(14 * scaling));
            gr.ctx().fillText("scaling: %.2f, draw calls: %d".formatted(scaling, drawCallCount), canvas.getWidth() * 0.5, 16 * scaling);
            gr.ctx().restore();
        }
    }
}