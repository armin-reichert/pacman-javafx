/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.uilib.rendering.*;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.api.GameUI_Config.SCENE_ID_PLAY_SCENE_3D;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.*;
import static java.util.Objects.requireNonNull;

public class MiniGameView extends VBox {

    public static final Vector2f ARCADE_SIZE = Vector2f.of(28*TS, 36*TS);

    public static final Duration SLIDE_IN_DURATION = Duration.seconds(1);
    public static final Duration SLIDE_OUT_DURATION = Duration.seconds(2);

    private final DoubleProperty scaling = new SimpleDoubleProperty(1.0f);
    private final ObjectProperty<Vector2f> worldSize = new SimpleObjectProperty<>(ARCADE_SIZE);

    private final HBox layout;
    private final Canvas canvas;

    private final GameUI ui;

    private final BaseCanvasRenderer canvasRenderer;
    private GameLevelRenderer gameLevelRenderer;
    private ActorRenderer actorRenderer;

    private long drawCallCount;

    private final TranslateTransition slideInAnimation;
    private final TranslateTransition slideOutAnimation;

    public MiniGameView(GameUI ui) {
        this.ui = requireNonNull(ui);

        canvas = new Canvas();
        canvas.heightProperty().bind(PROPERTY_MINI_VIEW_HEIGHT);
        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> {
                Vector2f size = worldSize.get();
                double aspect = size.x() / size.y();
                return aspect * canvas.getHeight();
            },
            worldSize, canvas.heightProperty()
        ));

        canvasRenderer = new BaseCanvasRenderer(canvas);

        // The VBox fills the complete parent container height (why?), so we put the canvas
        // into an HBox that does not grow in height and provides some padding around the canvas.
        layout = new HBox(canvas);
        layout.backgroundProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR.map(Background::fill));
        layout.setPadding(new Insets(0, 10, 0, 10));
        VBox.setVgrow(layout, Priority.NEVER);
        getChildren().add(layout);

        opacityProperty().bind(PROPERTY_MINI_VIEW_OPACITY_PERCENT.divide(100.0));
        scaling.bind(Bindings.createDoubleBinding(
            () -> canvas.getHeight() / worldSize.get().y(),
            canvas.heightProperty(), worldSize
        ));
        visibleProperty().bind(Bindings.createObjectBinding(
            () -> PROPERTY_MINI_VIEW_ON.get() && ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_3D),
            PROPERTY_MINI_VIEW_ON, PROPERTY_CURRENT_GAME_SCENE
        ));

        slideInAnimation = new TranslateTransition(SLIDE_IN_DURATION, this);
        slideInAnimation.setToY(0);
        slideInAnimation.setByY(10);
        slideInAnimation.setDelay(Duration.seconds(1));
        slideInAnimation.setInterpolator(Interpolator.EASE_OUT);

        slideOutAnimation = new TranslateTransition(SLIDE_OUT_DURATION, this);
        slideOutAnimation.setToY(-layout.getHeight());
        slideOutAnimation.setByY(10);
        slideOutAnimation.setDelay(Duration.seconds(2));
        slideOutAnimation.setInterpolator(Interpolator.EASE_IN);
    }

    public void onGameLevelCreated(GameLevel gameLevel) {
        worldSize.set(gameLevel.worldSizePx());
        /* TODO: The renderers cannot be created in the constructor because the game controller has not yet
            selected a game variant when the constructor is called, so no UI configuration is available! */

        gameLevelRenderer = ui.currentConfig().createGameLevelRenderer(canvas);
        gameLevelRenderer.scalingProperty().bind(scaling);
        gameLevelRenderer.backgroundColorProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);

        actorRenderer = ui.currentConfig().createActorRenderer(canvas);
        actorRenderer.scalingProperty().bind(scaling);
        actorRenderer.backgroundColorProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);
    }

    public void slideIn() {
        slideInAnimation.play();
    }

    public void slideOut() {
        slideOutAnimation.setToY(-layout.getHeight());
        slideOutAnimation.play();
    }

    public void draw() {
        drawCallCount += 1;

        if (!isVisible() || gameLevelRenderer == null) {
            return;
        }
        actorRenderer.clearCanvas();

        GameLevel gameLevel = ui.gameContext().gameLevel();
        if (gameLevel != null) {
            var info = RenderInfo.build(Map.of(
                RenderInfoProperties.MAZE_BRIGHT, false,
                RenderInfoProperties.MAZE_BLINKING, gameLevel.blinking().isOn(),
                RenderInfoProperties.MAZE_EMPTY, gameLevel.uneatenFoodCount() == 0,
                RenderInfoProperties.TICK, ui.clock().tickCount()
            ));
            gameLevelRenderer.applyLevelSettings(gameLevel, info);
            gameLevelRenderer.drawGameLevel(gameLevel, info);

            gameLevel.bonus().ifPresent(bonus -> actorRenderer.drawActor(bonus));
            actorRenderer.drawActor(gameLevel.pac());
            Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW)
                .map(gameLevel::ghost)
                .forEach(ghost -> actorRenderer.drawActor(ghost));
        }

        if (PROPERTY_DEBUG_INFO_VISIBLE.get()) {
            canvasRenderer.fillTextCentered(
                "scaling: %.2f, draw calls: %d".formatted(scaling.doubleValue(), drawCallCount),
                Color.WHITE, Font.font(14 * scaling.get()),
                0.5 * worldSize.get().x(), 16
            );
        }
    }
}