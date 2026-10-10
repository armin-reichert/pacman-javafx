/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.basics.ui.assets.ArcadeColor;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.engine.config.GameVariantUIConfig;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.engine.GameSceneEmbedding;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneView2D;
import de.amr.pacmanfx.ui.gamescene.playscene.PlayScene3D;
import de.amr.pacmanfx.ui.viewmodel.Game2DSettingsVM;
import de.amr.pacmanfx.uilib.widgets.decorationpane.DecorationPaneBorderConfig;
import de.amr.pacmanfx.uilib.widgets.decorationpane.DecorationPaneConfig;
import de.amr.pacmanfx.uilib.widgets.decorationpane.FramedGameSceneContainer;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.binding.FloatExpression;
import javafx.beans.value.ObservableValue;
import javafx.scene.SubScene;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class GameScenePane extends StackPane {

    public static final float MAX_GAME_SCENE_SCALING = 5;

    //TODO use FX controls + CSS
    public static final DecorationPaneConfig FRAMED_CONTAINER_CONFIG = new DecorationPaneConfig(
        0.85f, 0.93f, 0.5f, // scaling x,y, min
        20, 20, // padding x,y
        new DecorationPaneBorderConfig(26, 10, 5, 55.0, ArcadeColor.WHITE.color())
    );

    public static final Background DEBUG_BACKGROUND = Ufx.paintBackground(Color.TEAL);
    public static final Border DEBUG_BORDER = Ufx.border(Color.LIGHTGREEN, 1);

    public static final Background PLAIN_CONTAINER_BACKGROUND = Background.fill(Color.rgb(10, 10, 80));

    private final FramedGameSceneContainer framedContainer;
    private final PlainGameSceneContainer plainContainer;

    public GameScenePane() {
        framedContainer = new FramedGameSceneContainer(FRAMED_CONTAINER_CONFIG);
        plainContainer = new PlainGameSceneContainer();
    }

    public void resizeTo(double width, double height) {
        framedContainer.stretchTo(width, height);
    }

    public void installKeyBindings() {
        framedContainer.installBindings();
    }

    public void uninstallKeyBindings() {
        framedContainer.uninstallBindings();
    }

    public void setDebugMode(boolean debug) {
        setBackground(debug ? DEBUG_BACKGROUND : null);
        setBorder(debug ? DEBUG_BORDER : null);
    }

    public void embedGameScene(GameUI ui, GameVariantUIConfig uiConfig, AbstractGameScene gameScene) {
        requireNonNull(ui);
        requireNonNull(uiConfig);
        requireNonNull(gameScene);

        final DoubleExpression availableWidth = ui.window().mainScene().widthProperty();
        final DoubleExpression availableHeight = ui.window().mainScene().heightProperty();
        final Game2DSettingsVM settingsViewModel = ui.viewModel().common2DSettings();
        final GameSceneEmbedding embedding = uiConfig.gameSceneConfig().embedding(gameScene);
        switch (embedding) {
            case PLAIN_2D, DECORATED_2D -> embedGameScene2D(availableWidth, availableHeight, gameScene, settingsViewModel, embedding);
            case SUBSCENE -> {
                if (gameScene instanceof PlayScene3D playScene3D) {
                    embedGameSceneProvidingSubScene(availableWidth, availableHeight, playScene3D.subScene());
                }
            }
        }
        Logger.info("Game scene {} EMBEDDED into play view!", gameScene.getClass().getSimpleName());
    }

    private void embedGameScene2D(DoubleExpression availableWidth, DoubleExpression availableHeight, AbstractGameScene gameScene, Game2DSettingsVM settingsViewModel, GameSceneEmbedding embedding) {
        if (embedding == GameSceneEmbedding.DECORATED_2D) {
            embedDecoratedGameScene2D(availableWidth, availableHeight, gameScene, settingsViewModel);
        }
        else if (embedding == GameSceneEmbedding.PLAIN_2D) {
            embedPlainGameScene2D(availableHeight, gameScene);
        }
        else {
            Logger.error("Illegal embedding: " + embedding);
        }
    }

    private void embedDecoratedGameScene2D(DoubleExpression availableWidth, DoubleExpression availableHeight, AbstractGameScene gameScene, Game2DSettingsVM settingsViewModel) {
        final GameSceneView2D view2D = gameScene.assertComponent(GameSceneView2D.class);
        final Color bgColor = settingsViewModel.canvasBackgroundColorProperty().get();
        final ObservableValue<Background> containerBackground = settingsViewModel.canvasBackgroundColorProperty().map(Ufx::paintBackground);

        framedContainer.newRenderingSurface(); //TODO check if creating a new canvas is needed
        framedContainer.backgroundProperty().bind(containerBackground);

        // Set unscaled decoration pane size to game scene (=world map) size
        framedContainer.unscaledWidthProperty().bind(view2D.unscaledWidthProperty());
        framedContainer.unscaledHeightProperty().bind(view2D.unscaledHeightProperty());

        // Limit scaling
        framedContainer.renderingSurface().scalingProperty().bind(framedContainer.scalingProperty().map(
            scaling -> Math.min(scaling.doubleValue(), MAX_GAME_SCENE_SCALING)));

        framedContainer.renderingSurface().fill(bgColor);
        framedContainer.stretchTo(availableWidth.doubleValue(), availableHeight.doubleValue());

        view2D.setRenderingSurface(framedContainer.renderingSurface());

        getChildren().setAll(framedContainer);
    }

    private void embedPlainGameScene2D(DoubleExpression availableHeight, AbstractGameScene gameScene) {
        final GameSceneView2D view2D = gameScene.assertComponent(GameSceneView2D.class);
        final FloatExpression unscaledWidth = view2D.unscaledWidthProperty();
        final FloatExpression unscaledHeight = view2D.unscaledHeightProperty();
        final DoubleExpression scaling = availableHeight.divide(unscaledHeight);

        view2D.setRenderingSurface(plainContainer.renderingSurface());
        view2D.renderingSurface().canvas().heightProperty().bind(availableHeight);
        view2D.renderingSurface().canvas().widthProperty().bind(scaling.multiply(unscaledWidth));
        view2D.renderingSurface().scalingProperty().bind(scaling);

        plainContainer.setBackground(PLAIN_CONTAINER_BACKGROUND);

        getChildren().setAll(plainContainer);
        plainContainer.getChildren().setAll(plainContainer.renderingSurface().canvas());

        Logger.info("After embedding: canvas w={} h={} scaling={}",
            view2D.renderingSurface().canvas().getWidth(),
            view2D.renderingSurface().canvas().getHeight(),
            view2D.renderingSurface().scaling());
    }

    private void embedGameSceneProvidingSubScene(DoubleExpression availableWidth, DoubleExpression availableHeight, SubScene subScene) {
        subScene.widthProperty().bind(availableWidth);
        subScene.heightProperty().bind(availableHeight);
        getChildren().setAll(subScene);
    }
}
