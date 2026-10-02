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
import de.amr.pacmanfx.ui.gamescene.common.GameVariantGameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneRendering2DComp;
import de.amr.pacmanfx.ui.viewmodel.Game2DSettingsVM;
import de.amr.pacmanfx.ui.window.GameMainScene;
import de.amr.pacmanfx.uilib.view2d.RenderingSurface;
import de.amr.pacmanfx.uilib.widgets.decorationpane.DecorationPaneBorderConfig;
import de.amr.pacmanfx.uilib.widgets.decorationpane.DecorationPaneConfig;
import de.amr.pacmanfx.uilib.widgets.decorationpane.FramedGameSceneContainer;
import javafx.beans.value.ObservableValue;
import javafx.scene.SubScene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class GameScenePane extends BorderPane {

    //TODO use FX controls + CSS
    public static final DecorationPaneConfig DECORATION_PANE_CONFIG = new DecorationPaneConfig(
        0.85f, 0.93f, 0.5f, // scaling x,y, min
        20, 20, // padding x,y
        new DecorationPaneBorderConfig(26, 10, 5, 55.0, ArcadeColor.WHITE.color())
    );

    private final FramedGameSceneContainer framedContainer;

    private final PlainGameSceneContainer plainContainer;

    public GameScenePane() {
        framedContainer = new FramedGameSceneContainer(
            DECORATION_PANE_CONFIG,
            WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.x(),
            WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.y()
        );

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

    public void embedGameScene(GameUI ui, GameVariantUIConfig uiConfig, GameScene gameScene) {
        requireNonNull(ui);
        requireNonNull(uiConfig);
        requireNonNull(gameScene);

        final GameMainScene mainScene = ui.window().mainScene();
        if (gameScene.optSubSceneFX().isPresent()) {
            embedGameSceneWithSubSceneFX(mainScene, gameScene, gameScene.optSubSceneFX().get());
        } else {
            embedGameScene2D(mainScene, uiConfig.gameSceneConfig(), gameScene, ui.viewModel().common2DSettings());
        }
        gameScene.activate();
        Logger.info("Game scene {} EMBEDDED into play view!", gameScene.getClass().getSimpleName());
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

    // 3D scenes or 2D scenes with camera
    public void embedGameSceneWithSubSceneFX(GameMainScene mainScene, GameScene gameScene, SubScene subSceneFX) {
        // stretch sub scene to available space
        subSceneFX.widthProperty().bind(mainScene.widthProperty());
        subSceneFX.heightProperty().bind(mainScene.heightProperty());

        if (!(gameScene instanceof AbstractGameScene abstractGameScene)) {
            Logger.error("Current game scene is not an AbstractGameScene");
            return;
        }

        if (abstractGameScene.hasComp(GameSceneRendering2DComp.class)) {
            final GameSceneRendering2DComp r2D = abstractGameScene.reqComp(GameSceneRendering2DComp.class);
            r2D.setRenderingSurface(plainContainer.renderingSurface());
        }
//        setCenter(subSceneFX);
        setCenter(plainContainer);
    }

    // 2D scenes without camera which are shown at full size
    public void embedGameScene2D(
        GameMainScene mainScene,
        GameVariantGameSceneConfig gameSceneConfig,
        GameScene gameScene,
        Game2DSettingsVM settingsViewModel)
    {
        if (!(gameScene instanceof AbstractGameScene abstractGameScene)) {
            Logger.error("Current game scene is not an AbstractGameScene");
            return;
        }
        final GameSceneRendering2DComp r2d = abstractGameScene.reqComp(GameSceneRendering2DComp.class);
        final ObservableValue<Background> containerBackground = settingsViewModel.canvasBackgroundColorProperty().map(Ufx::paintBackground);

        final boolean decorated = gameSceneConfig.sceneDecorationRequested(gameScene);
        if (decorated) {
            framedContainer.newRenderingSurface(); //TODO check if creating a new canvas is needed
            framedContainer.backgroundProperty().bind(containerBackground);

            // Set unscaled decoration pane size to game scene (=world map) size
            framedContainer.unscaledWidthProperty().bind(r2d.unscaledWidthProperty());
            framedContainer.unscaledHeightProperty().bind(r2d.unscaledHeightProperty());

            // Limit scaling
            framedContainer.renderingSurface().scalingProperty().bind(framedContainer.scalingProperty().map(
                scaling -> Math.min(scaling.doubleValue(), GamePlayView.MAX_GAME_SCENE_SCALING)));

            framedContainer.renderingSurface().clear();
            framedContainer.stretchTo(mainScene.getWidth(), mainScene.getHeight());

            r2d.setRenderingSurface(framedContainer.renderingSurface());
            setCenter(framedContainer);
        }
        else {
            plainContainer.setBackground(Background.fill(Color.rgb(10, 10, 80)));

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
    }
}
