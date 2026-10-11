/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.window;

import de.amr.basics.ui.assets.TranslationManager;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.app.runtime.DefaultGameVariantRuntime;
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import de.amr.pacmanfx.ui.views.GameViewID;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

public class GameWindow {

    public static final int MIN_STAGE_WIDTH  = 280;
    public static final int MIN_STAGE_HEIGHT = 360;

    private final BooleanProperty connected = new SimpleBooleanProperty(false);

    private StringBinding titleBinding;

    private final Stage stage;

    private final GameMainScene mainScene;

    public GameWindow(Stage stage, int width, int height) {
        this.stage = requireNonNull(stage);

        mainScene = new GameMainScene(width, height);

        stage.setScene(mainScene);
        stage.setMinWidth(MIN_STAGE_WIDTH);
        stage.setMinHeight(MIN_STAGE_HEIGHT);
    }

    public void connectEngine(EngineUIContext engineContext) {
        mainScene.setEngineContext(engineContext);

        titleBinding = createStageTitleBinding(engineContext);
        stage.titleProperty().bind(titleBinding);

        //TODO Without this, the title is not changed when returning from the editor. Why?
        engineContext.ui().viewManager().currentViewIDProperty().addListener(
            (_, _, viewID) -> updateStageTitleBinding(engineContext.ui(), viewID));

        engineContext.gameVariantManager().addVariantListener((_, _, _) -> updateStageIcon(engineContext));

        // Triggers title update
        connected.set(true);
    }

    public void show(EngineUIContext engineContext) {
        updateStageIcon(engineContext);
        stage.centerOnScreen();
        stage.show();
    }

    public Stage stage() {
        return stage;
    }

    public GameMainScene mainScene() {
        return mainScene;
    }

    public void setFullScreen(boolean value) {
        stage.setFullScreen(value);
    }

    // Private area

    private StringBinding createStageTitleBinding(EngineUIContext engineContext) {
        return createStringBinding(
            () -> switch (engineContext.ui().viewManager().currentViewID()) {
                case null -> ""; // happens initially, don't mind
                case START_PAGES, GAMEPLAY -> optCurrentViewTitle(engineContext.ui()).orElse(titleForCurrentGameScene(engineContext));
                // Editor has its own title supplier → use it directly
                case EDITOR -> optCurrentViewTitle(engineContext.ui()).orElse(("Map Editor"));
            },
            connected,
            engineContext.gameVariantManager().selectedVariantNameProperty(),
            engineContext.gameSceneManager().currentGameSceneProperty(),
            engineContext.clock().updatesDisabledProperty(),
            engineContext.ui().viewModel().debugModeOnProperty(),
            engineContext.ui().viewModel().common3DSettings().view3DEnabledProperty(),
            engineContext.ui().viewManager().currentViewIDProperty()
        );
    }

    private Optional<String> optCurrentViewTitle(GameUI ui) {
        return ui.viewManager().optCurrentView().isEmpty()
            ? Optional.of("No View present")
            : ui.viewManager().assertCurrentView().optTitleSupplier().map(Supplier::get);
    }

    private void updateStageTitleBinding(GameUI ui, GameViewID viewID) {
        switch (viewID) {
            case START_PAGES, GAMEPLAY -> stage.titleProperty().bind(titleBinding);
            case EDITOR -> ui.viewManager().optEditorView().ifPresent(editorView -> {
                stage.titleProperty().unbind();
                editorView.optTitleSupplier().ifPresent(titleSupplier -> stage.setTitle(titleSupplier.get()));
            });
        }
    }

    private void updateStageIcon(EngineUIContext engineContext) {
        final DefaultGameVariantRuntime runtime = (DefaultGameVariantRuntime) engineContext.gameVariantManager().currentRuntime();
        final Image icon = runtime.uiConfig().assets().image("app_icon");
        if (icon != null) {
            stage.getIcons().setAll(icon);
        } else {
            Logger.error("Could not access stage icon");
        }
    }

    private String titleForCurrentGameScene(EngineUIContext engineContext) {
        final GameScene gameScene = engineContext.gameSceneManager().optCurrentGameScene().orElse(null);
        final GameViewModel viewModel = engineContext.ui().viewModel();

        final boolean debug  = viewModel.debugModeOnProperty().get();
        final boolean is3D   = viewModel.common3DSettings().view3DEnabledProperty().get();
        final boolean paused = engineContext.clock().getUpdatesDisabled();

        final String normalTitle = stageTitle(engineContext, paused, is3D);
        return (gameScene == null || !debug)
            ? normalTitle
            : "%s [%s]".formatted(normalTitle, gameScene.getClass().getSimpleName());
    }

    private String stageTitle(EngineUIContext engineContext, boolean paused, boolean is3D) {
        final String gameVariantName = engineContext.gameVariantManager().currentVariantName();
        if (gameVariantName == null) {
            return "";
        }

        final String viewModeKey = engineContext.translationManager().translate(is3D ?
            "view_mode.3d" : "view_mode.2d");

        // In game-variant specific resource bundles, there should be two entries with placeholder
        // app.title = Game Variant Name {0}
        // app.title = Game Variant Name {0} (paused)

        final DefaultGameVariantRuntime runtime = (DefaultGameVariantRuntime) engineContext.gameVariantManager().currentRuntime();
        final TranslationManager variantTranslationManager = runtime.uiConfig().translations();
        final String titleKey = paused ? "app.title.paused" : "app.title";
        if (variantTranslationManager.textBundle() != null
            && variantTranslationManager.textBundle().containsKey(titleKey)) {
            return variantTranslationManager.translate(titleKey, viewModeKey);
        } else {
            return "Unspecified Pac-Man Game Variant";
        }
    }
}
