/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.basics.ui.assets.ArcadeColor;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneEmbedding;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneRendering2DComp;
import de.amr.pacmanfx.ui.gamescene.d3.PlayScene3D;
import de.amr.pacmanfx.ui.viewmodel.Game2DSettingsVM;
import de.amr.pacmanfx.ui.window.GameMainScene;
import de.amr.pacmanfx.uilib.view2d.RenderingSurface;
import de.amr.pacmanfx.uilib.widgets.decorationpane.DecorationPaneBorderConfig;
import de.amr.pacmanfx.uilib.widgets.decorationpane.DecorationPaneConfig;
import de.amr.pacmanfx.uilib.widgets.decorationpane.FramedGameSceneContainer;
import javafx.beans.value.ObservableValue;
import javafx.scene.SubScene;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class GameScenePane extends BorderPane {

    public static final float MAX_GAME_SCENE_SCALING = 5;

    //TODO use FX controls + CSS
    public static final DecorationPaneConfig DECORATION_PANE_CONFIG = new DecorationPaneConfig(
        0.85f, 0.93f, 0.5f, // scaling x,y, min
        20, 20, // padding x,y
        new DecorationPaneBorderConfig(26, 10, 5, 55.0, ArcadeColor.WHITE.color())
    );

    public static final Background DEBUG_BACKGROUND = Ufx.paintBackground(Color.TEAL);
    public static final Border DEBUG_BORDER = Ufx.border(Color.LIGHTGREEN, 1);

    public static final Background PLAIN_CONTAINER_BACKGROUND = Background.fill(Color.rgb(10, 10, 80));

    private final FramedGameSceneContainer framedContainer;
    private final PlainGameSceneContainer plainContainer;
    private final SubSceneGameSceneContainer subSceneContainer;

    public GameScenePane() {
        framedContainer = new FramedGameSceneContainer(
            DECORATION_PANE_CONFIG,
            WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.x(),
            WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.y()
        );

        plainContainer = new PlainGameSceneContainer();

        subSceneContainer = new SubSceneGameSceneContainer();
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

    public void disembedGameScene(GameScene gameScene) {
        requireNonNull(gameScene);

        gameScene.deactivate();

        gameScene.optSubSceneFX().ifPresent(subSceneFX -> {
            subSceneFX.widthProperty().unbind();
            subSceneFX.heightProperty().unbind();
        });

        framedContainer.unscaledWidthProperty().unbind();
        framedContainer.unscaledHeightProperty().unbind();
        framedContainer.backgroundProperty().unbind();

        Logger.info("Game scene {} DISEMBEDDED from play view!", gameScene.getClass().getSimpleName());
    }

    public void embedGameScene(GameUI ui, GameVariantUIConfig uiConfig, GameScene gameScene) {
        requireNonNull(ui);
        requireNonNull(uiConfig);
        requireNonNull(gameScene);

        final GameMainScene mainScene = ui.window().mainScene();
        final Game2DSettingsVM settingsViewModel = ui.viewModel().common2DSettings();
        final GameSceneEmbedding embedding = uiConfig.gameSceneConfig().embedding(gameScene);
        switch (embedding) {
            case PLAIN_2D, DECORATED_2D -> embedGameScene2D(mainScene, gameScene, settingsViewModel, embedding);
            case SUBSCENE_2D -> embedGameScene2DWithSubSceneFX(mainScene, gameScene);
            case SUBSCENE_3D -> {
                if (gameScene instanceof PlayScene3D playScene3D) {
                    embedPlayScene3D(mainScene, playScene3D);
                }
            }
        }
        gameScene.activate();
        Logger.info("Game scene {} EMBEDDED into play view!", gameScene.getClass().getSimpleName());
    }

    private void embedGameScene2D(GameMainScene mainScene, GameScene gameScene, Game2DSettingsVM settingsViewModel, GameSceneEmbedding embedding) {
        if (!(gameScene instanceof AbstractGameScene abstractGameScene)) {
            Logger.error("Current game scene is not an AbstractGameScene");
            return;
        }
        if (embedding == GameSceneEmbedding.DECORATED_2D) {
            embedDecoratedGameScene2D(mainScene, abstractGameScene, settingsViewModel);
        }
        else if (embedding == GameSceneEmbedding.PLAIN_2D) {
            embedPlainGameScene2D(mainScene, abstractGameScene);
        }
        else {
            Logger.error("Illegal embedding: " + embedding);
        }
    }

    private void embedDecoratedGameScene2D(GameMainScene mainScene, AbstractGameScene gameScene, Game2DSettingsVM settingsViewModel) {
        final GameSceneRendering2DComp r2d = gameScene.reqComp(GameSceneRendering2DComp.class);
        final ObservableValue<Background> containerBackground = settingsViewModel.canvasBackgroundColorProperty().map(Ufx::paintBackground);

        framedContainer.newRenderingSurface(); //TODO check if creating a new canvas is needed
        framedContainer.backgroundProperty().bind(containerBackground);

        // Set unscaled decoration pane size to game scene (=world map) size
        framedContainer.unscaledWidthProperty().bind(r2d.unscaledWidthProperty());
        framedContainer.unscaledHeightProperty().bind(r2d.unscaledHeightProperty());

        // Limit scaling
        framedContainer.renderingSurface().scalingProperty().bind(framedContainer.scalingProperty().map(
            scaling -> Math.min(scaling.doubleValue(), MAX_GAME_SCENE_SCALING)));

        framedContainer.renderingSurface().clear();
        framedContainer.stretchTo(mainScene.getWidth(), mainScene.getHeight());

        r2d.setRenderingSurface(framedContainer.renderingSurface());
        setCenter(framedContainer);
    }

    private void embedPlainGameScene2D(GameMainScene mainScene, AbstractGameScene gameScene) {
        final GameSceneRendering2DComp r2d = gameScene.reqComp(GameSceneRendering2DComp.class);

        plainContainer.reset();
        plainContainer.setBackground(PLAIN_CONTAINER_BACKGROUND);

        final RenderingSurface surface = plainContainer.renderingSurface();

        surface.heightProperty().bind(mainScene.heightProperty());

        surface.widthProperty().bind(
            mainScene.heightProperty()
                .multiply(r2d.unscaledWidthProperty())
                .divide(r2d.unscaledHeightProperty()));

        surface.scalingProperty().bind(
            mainScene.heightProperty()
                .divide(r2d.unscaledHeightProperty()));

        r2d.setRenderingSurface(plainContainer.renderingSurface());
        setCenter(plainContainer);
    }

    private void embedPlayScene3D(GameMainScene mainScene, PlayScene3D playScene3D) {
        final SubScene subSceneFX = playScene3D.subScene();
        subSceneFX.widthProperty().bind(mainScene.widthProperty());
        subSceneFX.heightProperty().bind(mainScene.heightProperty());
        setCenter(subSceneFX);
    }

    private void embedGameScene2DWithSubSceneFX(GameMainScene mainScene, GameScene gameScene) {
        if (!(gameScene instanceof AbstractGameScene abstractGameScene)
            || !abstractGameScene.hasComp(GameSceneRendering2DComp.class)) {
            Logger.error("Cannot embed game scene");
            return;
        }
        final GameSceneRendering2DComp r2D = abstractGameScene.reqComp(GameSceneRendering2DComp.class);
        final RenderingSurface renderingSurface = subSceneContainer.renderingSurface();

        final double aspect = (double) r2D.unscaledWidth() / r2D.unscaledHeight();

        renderingSurface.scalingProperty().bind(subSceneContainer.subScene().heightProperty().divide(r2D.unscaledHeight()));

        r2D.setRenderingSurface(renderingSurface);

        subSceneContainer.subScene().heightProperty().bind(mainScene.heightProperty());
        subSceneContainer.subScene().widthProperty().bind(mainScene.heightProperty().multiply(aspect));

        subSceneContainer.root().setBackground(Background.fill(Color.BLACK));

        setCenter(subSceneContainer.subScene());
    }
}
