/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.window;

import de.amr.basics.ui.assets.TranslationManager;
import de.amr.pacmanfx.core.GameVariantRuntime;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.GameVariantUIConfig;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.ui.views.GameViewManager;
import javafx.beans.binding.StringBinding;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

/**
 * The stage title depends on lots of different things: the currently selected game variant, the view mode,
 * if debug mode is active and more. This class handles all these cases.
 */
public class StageTitleManager {

    private final StringBinding startAndPlayViewBinding;

    private final Stage stage;

    public StageTitleManager(EngineUIContext engine, Stage stage) {
        this.stage = requireNonNull(stage);
        startAndPlayViewBinding = createStartAndPlayViewTitleBinding(engine);
        stage.titleProperty().bind(startAndPlayViewBinding);
    }

    public void update(EngineUIContext engine, GameView view) {
        final GameViewManager viewManager = engine.viewManager();
        if (view == viewManager.startPagesView() || view == viewManager.gamePlayView()) {
            stage.titleProperty().bind(startAndPlayViewBinding);
        }
        else if (view == viewManager.editorView()) {
            viewManager.editorView().optTitleSupplier().ifPresent(supplier -> {
                stage.titleProperty().unbind();
                stage.setTitle(supplier.get());
            });
        }
    }

    // Private area

    private StringBinding createStartAndPlayViewTitleBinding(EngineUIContext engine) {
        return createStringBinding(
            () -> {
                final GameViewManager viewManager = engine.viewManager();
                if (viewManager.isStartPagesViewSelected() || viewManager.isGamePlayViewSelected()) {
                    return optCurrentViewTitle(engine).orElse(titleForCurrentGameScene(engine));
                }
                if (viewManager.isEditorViewSelected()) {
                    return optCurrentViewTitle(engine).orElse(("Map Editor"));
                }
                return "";
            },
            engine.gameVariantManager().currentVariantIDProperty(),
            engine.gameSceneManager().currentGameSceneProperty(),
            engine.clock().updatesDisabledProperty(),
            engine.ui().viewModel().debugModeOnProperty(),
            engine.ui().viewModel().common3DSettings().view3DEnabledProperty(),
            engine.viewManager().currentViewProperty()
        );
    }

    private Optional<String> optCurrentViewTitle(EngineUIContext engine) {
        final GameView currentView = engine.viewManager().currentView();
        return currentView == null ? Optional.empty() : currentView.optTitleSupplier().map(Supplier::get);
    }

    private String titleForCurrentGameScene(EngineUIContext engine) {
        final GameScene gameScene = engine.gameSceneManager().optCurrentGameScene().orElse(null);
        final GameViewModel viewModel = engine.ui().viewModel();

        final boolean debug  = viewModel.debugModeOnProperty().get();
        final boolean is3D   = viewModel.common3DSettings().view3DEnabledProperty().get();
        final boolean paused = engine.clock().areUpdatesDisabled();

        final String normalTitle = stageTitle(engine, paused, is3D);
        return (gameScene == null || !debug)
            ? normalTitle
            : "%s [%s]".formatted(normalTitle, gameScene.getClass().getSimpleName());
    }

    private String stageTitle(EngineUIContext engine, boolean paused, boolean is3D) {
        final String gameVariantName = engine.gameVariantManager().currentVariantID();
        if (gameVariantName == null) {
            return "";
        }

        final String viewModeKey = engine.translationManager().translate(is3D ? "view_mode.3d" : "view_mode.2d");

        // In game-variant specific resource bundles, there should be two entries with placeholder
        // app.title = Game Variant Name {0}
        // app.title = Game Variant Name {0} (paused)

        final GameVariantRuntime runtime = engine.gameVariantManager().currentRuntime();
        final GameVariantUIConfig uiConfig = engine.uiConfig(runtime);
        final TranslationManager translationManager = uiConfig.translationManager();
        final String titleKey = paused ? "app.title.paused" : "app.title";
        if (translationManager.textBundle() != null
            && translationManager.textBundle().containsKey(titleKey)) {
            return translationManager.translate(titleKey, viewModeKey);
        } else {
            return "Unspecified Pac-Man Game Variant";
        }
    }
}
