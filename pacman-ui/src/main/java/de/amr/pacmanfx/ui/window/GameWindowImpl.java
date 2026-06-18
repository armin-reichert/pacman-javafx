/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.window;

import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.widgets.startbutton.GameStartButton;
import javafx.beans.binding.StringBinding;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

public class GameWindowImpl implements GameWindow {

    private Game game;

    private StringBinding titleBinding;

    private final Stage stage;
    private final GameMainScene mainScene;

    public GameWindowImpl(Stage stage, int width, int height) {
        this.stage = requireNonNull(stage);

        mainScene = new GameMainScene(width, height);
        mainScene.getStylesheets().add(GameUI_Constants.STYLE_SHEET_PATH);
        mainScene.getStylesheets().add(
            GameStartButton.class
                .getResource("/de/amr/pacmanfx/uilib/widgets/startbutton/game-start-button.css")
                .toExternalForm()
        );

        stage.setScene(mainScene);
        stage.setMinWidth(GameUI_Constants.MIN_STAGE_WIDTH);
        stage.setMinHeight(GameUI_Constants.MIN_STAGE_HEIGHT);
    }

    @Override
    public void connect(Game game) {
        if (this.game != null) {
            Logger.warn("Game view already connect to game!");
            return;
        }
        this.game = requireNonNull(game);

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
                    .orElse(titleForCurrentGameScene());
            },
            game.gameVariantNameProperty(),
            game.clock().updatesDisabledProperty(),
            game.ui().views().currentViewIDProperty(),
            game.ui().gameScenes().currentGameSceneProperty(),
            game.ui().settings().debugModeOnProperty(),
            game.ui().settings().d3().view3DEnabledProperty()
        );

        game.ui().views().currentViewIDProperty().addListener((_, _, viewID) -> updateStageTitle(viewID));
    }

    @Override
    public void show() {
        prepareStageForDisplay();
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

    private void updateStageTitle(GameViewID viewID) {
        switch (viewID) {
            case START_PAGES, GAMEPLAY -> stage.titleProperty().bind(titleBinding);
            case EDITOR -> game.ui().views().optEditorView().ifPresent(editorView -> {
                stage.titleProperty().unbind();
                editorView.optTitleSupplier().ifPresent(titleSupplier -> stage.setTitle(titleSupplier.get()));
            });
        }
    }

    private void prepareStageForDisplay() {
        stage.titleProperty().bind(titleBinding);

        updateStageIcon();
        game.gameVariantNameProperty().addListener((_, _, _) -> updateStageIcon());
    }

    private void updateStageIcon() {
        final Image icon = game.currentUIConfig().assets().image("app_icon");
        if (icon != null) {
            game.ui().window().stage().getIcons().setAll(icon);
        } else {
            Logger.error("Could not access stage icon");
        }
    }

    private String titleForCurrentGameScene() {
        final AbstractGameScene gameScene = game.ui().gameScenes().optCurrentGameScene().orElse(null);

        final boolean debug = game.ui().settings().debugModeOnProperty().get();
        final boolean is3D = game.ui().settings().d3().view3DEnabledProperty().get();
        final boolean paused = game.clock().getUpdatesDisabled();

        final String normalTitle = stageTitle(paused, is3D);
        return (gameScene == null || !debug)
            ? normalTitle
            : "%s [%s]".formatted(normalTitle, gameScene.getClass().getSimpleName());
    }

    private String stageTitle(boolean paused, boolean is3D) {
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
