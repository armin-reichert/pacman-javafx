/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.view;

import de.amr.pacmanfx.ui.game.GameConstants;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.GameScene;
import de.amr.pacmanfx.ui.subviews.SubView;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.beans.binding.StringBinding;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.function.Supplier;

import static javafx.beans.binding.Bindings.createStringBinding;

public class GameViewImplementation implements GameView {

    private final Stage stage;
    private final GameViewMainScene mainScene;
    private final StatusIconBox statusIconBox;
    private StringBinding stageTitleBinding;
    private Image icon;

    public GameViewImplementation(Stage stage, GameViewMainScene mainScene, StatusIconBox statusIconBox) {
        this.stage = stage;
        this.mainScene = mainScene;
        this.statusIconBox = statusIconBox;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    @Override
    public void setGame(Game game) {
        stageTitleBinding = createStringBinding(
            () -> computeStageTitle(game),
            game.clock().updatesDisabledProperty(),
            game.variantNameProperty(),
            game.ui().subViews().selectedSubViewProperty(),
            game.ui().gameScenes().gameSceneProperty(),
            GameConstants.PROPERTY_DEBUG_INFO_VISIBLE,
            GameConstants.PROPERTY_3D_ENABLED
        );
        stage.titleProperty().bind(stageTitleBindingProperty());
    }

    @Override
    public void show() {
        if (icon != null) {
            stage.getIcons().setAll(icon);
        }
        stage.setScene(mainScene);
        stage.setMinWidth(GameConstants.MIN_STAGE_WIDTH);
        stage.setMinHeight(GameConstants.MIN_STAGE_HEIGHT);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void replaceSubView(SubView subView) {
        mainScene.replaceSubView(subView);
    }

    @Override
    public Stage stage() {
        return stage;
    }

    @Override
    public GameViewMainScene mainScene() {
        return mainScene;
    }

    @Override
    public StatusIconBox statusIconBox() {
        return statusIconBox;
    }

    public StringBinding stageTitleBindingProperty() {
        return stageTitleBinding;
    }

    private String computeStageTitle(Game game) {
        final SubView view = game.ui().subViews().currentView();
        return view == null
            ? game.ui().translations().translate("view.missing") // Should never happen
            : view.optTitleSupplier().map(Supplier::get).orElse(titleForCurrentGameScene(game));
    }

    private String titleForCurrentGameScene(Game game) {
        final GameScene gameScene = game.ui().gameScenes().optCurrentGameScene().orElse(null);

        final boolean debug = GameConstants.PROPERTY_DEBUG_INFO_VISIBLE.get();
        final boolean is3D = GameConstants.PROPERTY_3D_ENABLED.get();
        final boolean paused = game.clock().getUpdatesDisabled();

        final String normalTitle = stageTitle(game, paused, is3D);
        return (gameScene == null || !debug)
            ? normalTitle
            : "%s [%s]".formatted(normalTitle, gameScene.getClass().getSimpleName());
    }

    private String stageTitle(Game game, boolean paused, boolean is3D) {
        final String gameVariantName = game.currentGameVariantName();
        if (gameVariantName == null) {
            return "";
        }

        final String viewMode = game.ui().translations().translate(is3D ? "threeD" : "twoD");

        // In game-variant specific resource bundles, there should be two entries with placeholder
        // app.title = Game Variant Name {0}
        // app.title = Game Variant Name {0} (paused)

        final TranslationManager appSpecificTranslator = game.currentUIConfig();
        final String appTitleKey = paused ? "app.title.paused" : "app.title";
        if (appSpecificTranslator.textBundle() != null
            && appSpecificTranslator.textBundle().containsKey(appTitleKey)) {
            return appSpecificTranslator.translate(appTitleKey, viewMode);
        } else {
            return "Unspecified Game";
        }
    }
}
