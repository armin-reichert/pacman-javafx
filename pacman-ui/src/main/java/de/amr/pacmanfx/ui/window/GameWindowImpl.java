/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.window;

import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.beans.binding.StringBinding;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

public class GameWindowImpl implements GameWindow {

    public static final int MIN_STAGE_WIDTH  = 280;
    public static final int MIN_STAGE_HEIGHT = 360;

    private StringBinding titleBinding;

    private final Stage stage;
    private final GameMainScene mainScene;

    public GameWindowImpl(Stage stage, int width, int height) {
        this.stage = requireNonNull(stage);

        mainScene = new GameMainScene(width, height);

        stage.setScene(mainScene);
        stage.setMinWidth(MIN_STAGE_WIDTH);
        stage.setMinHeight(MIN_STAGE_HEIGHT);
    }

    @Override
    public void connect(Game game) {
        mainScene.connect(game);

        titleBinding = createStringBinding(
            () -> {
                var views = game.ui().views();
                var currentView = views.optCurrentView();

                if (currentView.isEmpty()) {
                    return game.ui().translations().translate("error.view_missing");
                }

                // Editor has its own title supplier → use it directly
                if (views.currentViewID() == GameViewID.EDITOR) {
                    return currentView.get().optTitleSupplier()
                        .map(Supplier::get)
                        .orElse("Editor");
                }

                // All other views use the normal title logic
                return currentView.get().optTitleSupplier()
                    .map(Supplier::get)
                    .orElse(titleForCurrentGameScene(game));
            },
            game.gameVariantNameProperty(),
            game.clock().updatesDisabledProperty(),
            game.ui().views().currentViewIDProperty(),
            game.ui().gameScenes().currentGameSceneProperty(),
            game.ui().settings().debugModeOnProperty,
            game.ui().settings().d3.view3DEnabledProperty
        );

        game.ui().views().currentViewIDProperty().addListener((_, _, viewID) -> updateStageTitleBinding(game, viewID));
    }

    @Override
    public void show(Game game) {
        prepareStageForDisplay(game);
        stage().centerOnScreen();
        stage().show();
    }

    @Override
    public Stage stage() {
        return stage;
    }

    @Override
    public GameMainScene mainScene() {
        return mainScene;
    }

    // Private area

    private void updateStageTitleBinding(Game game, GameViewID viewID) {
        switch (viewID) {
            case START_PAGES, GAMEPLAY -> stage.titleProperty().bind(titleBinding);
            case EDITOR -> game.ui().views().optEditorView().ifPresent(editorView -> {
                stage.titleProperty().unbind();
                editorView.optTitleSupplier().ifPresent(titleSupplier -> stage.setTitle(titleSupplier.get()));
            });
        }
    }

    private void prepareStageForDisplay(Game game) {
        stage.titleProperty().bind(titleBinding);

        updateStageIcon(game);
        game.gameVariantNameProperty().addListener((_, _, _) -> updateStageIcon(game));
    }

    private void updateStageIcon(Game game) {
        final Image icon = game.currentUIConfig().assets().image("app_icon");
        if (icon != null) {
            stage.getIcons().setAll(icon);
        } else {
            Logger.error("Could not access stage icon");
        }
    }

    private String titleForCurrentGameScene(Game game) {
        final AbstractGameScene gameScene = game.ui().gameScenes().optCurrentGameScene().orElse(null);

        final boolean debug = game.ui().settings().debugModeOnProperty.get();
        final boolean is3D = game.ui().settings().d3.view3DEnabledProperty.get();
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

        final String viewModeKey = game.ui().translations().translate(is3D ?
            "view_mode.3d" : "view_mode.2d");

        // In game-variant specific resource bundles, there should be two entries with placeholder
        // app.title = Game Variant Name {0}
        // app.title = Game Variant Name {0} (paused)

        final TranslationManager variantTranslations = game.currentUIConfig().translations();
        final String titleKey = paused ? "app.title.paused" : "app.title";
        if (variantTranslations.textBundle() != null
            && variantTranslations.textBundle().containsKey(titleKey)) {
            return variantTranslations.translate(titleKey, viewModeKey);
        } else {
            return "Unspecified Pac-Man Game Variant";
        }
    }
}
