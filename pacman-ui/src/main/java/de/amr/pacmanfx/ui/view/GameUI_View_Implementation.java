/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.view;

import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.gamescene.GameScene;
import de.amr.pacmanfx.ui.subviews.GameUI_SubView;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.beans.binding.StringBinding;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.function.Supplier;

import static javafx.beans.binding.Bindings.createStringBinding;

public class GameUI_View_Implementation implements GameUI_View {

    private final Stage stage;
    private final GameUI_MainScene mainScene;
    private final StatusIconBox statusIconBox;
    private StringBinding stageTitleBinding;
    private Image icon;

    public GameUI_View_Implementation(Stage stage, GameUI_MainScene mainScene, StatusIconBox statusIconBox) {
        this.stage = stage;
        this.mainScene = mainScene;
        this.statusIconBox = statusIconBox;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    @Override
    public void connect(AppContext context) {
        stageTitleBinding = createStringBinding(
            () -> computeStageTitle(context),
            context.gameClock().updatesDisabledProperty(),
            context.gameContext().gameVariantNameProperty(),
            context.ui().subViews().selectedSubViewProperty(),
            context.ui().gameScenes().gameSceneProperty(),
            GameUI_Constants.PROPERTY_DEBUG_INFO_VISIBLE,
            GameUI_Constants.PROPERTY_3D_ENABLED
        );
        stage.titleProperty().bind(stageTitleBindingProperty());
    }

    @Override
    public void show() {
        if (icon != null) {
            stage.getIcons().setAll(icon);
        }
        stage.setScene(mainScene);
        stage.setMinWidth(GameUI_Constants.MIN_STAGE_WIDTH);
        stage.setMinHeight(GameUI_Constants.MIN_STAGE_HEIGHT);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void replaceSubView(GameUI_SubView subView) {
        mainScene.replaceSubView(subView);
    }

    @Override
    public Stage stage() {
        return stage;
    }

    @Override
    public GameUI_MainScene mainScene() {
        return mainScene;
    }

    @Override
    public StatusIconBox statusIconBox() {
        return statusIconBox;
    }

    public StringBinding stageTitleBindingProperty() {
        return stageTitleBinding;
    }

    private String computeStageTitle(AppContext context) {
        final GameUI_SubView view = context.ui().subViews().currentView();
        return view == null
            ? context.ui().translations().translate("view.missing") // Should never happen
            : view.optTitleSupplier().map(Supplier::get).orElse(titleForCurrentGameScene(context));
    }

    private String titleForCurrentGameScene(AppContext context) {
        final GameScene gameScene = context.ui().gameScenes().optCurrentGameScene().orElse(null);

        final boolean debug = GameUI_Constants.PROPERTY_DEBUG_INFO_VISIBLE.get();
        final boolean is3D = GameUI_Constants.PROPERTY_3D_ENABLED.get();
        final boolean paused = context.gameClock().getUpdatesDisabled();

        final String normalTitle = appTitle(context, paused, is3D);
        return (gameScene == null || !debug)
            ? normalTitle
            : "%s [%s]".formatted(normalTitle, gameScene.getClass().getSimpleName());
    }

    private String appTitle(AppContext context, boolean paused, boolean is3D) {
        final String gameVariantName = context.currentGameVariant();
        if (gameVariantName == null) {
            return "";
        }

        final String viewMode = context.ui().translations().translate(is3D ? "threeD" : "twoD");

        // In game-variant specific resource bundles, there should be two entries with placeholder
        // app.title = Game Variant Name {0}
        // app.title = Game Variant Name {0} (paused)

        final TranslationManager appSpecificTranslator = context.currentUIConfig().assets();
        final String appTitleKey = paused ? "app.title.paused" : "app.title";
        if (appSpecificTranslator.bundle() != null
            && appSpecificTranslator.bundle().containsKey(appTitleKey)) {
            return appSpecificTranslator.translate(appTitleKey, viewMode);
        } else {
            return "Unspecified Game";
        }
    }
}
