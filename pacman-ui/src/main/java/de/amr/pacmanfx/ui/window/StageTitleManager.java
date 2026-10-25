/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.window;

import de.amr.basics.Named;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.pacmanfx.app.config.GameVariantUIConfig;
import de.amr.pacmanfx.core.GameVariantRuntime;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.ui.EngineContext;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.editor.EditorView;
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

    public StageTitleManager(EngineContext engine, Stage stage) {
        this.stage = requireNonNull(stage);
        startAndPlayViewBinding = createStartAndPlayViewTitleBinding(engine);
        stage.titleProperty().bind(startAndPlayViewBinding);
    }

    public void update(EngineContext engine, Named viewID) {
        if (viewID == GameViewID.START_PAGES || viewID == GameViewID.GAMEPLAY) {
            stage.titleProperty().bind(startAndPlayViewBinding);
        }
        else if (viewID == GameViewID.EDITOR) {
            engine.viewManager().optView(GameViewID.EDITOR, EditorView.class)
                .flatMap(EditorView::optTitleSupplier).ifPresent(supplier -> {
                stage.titleProperty().unbind();
                stage.setTitle(supplier.get());
            });
        }
    }

    // Private area

    private StringBinding createStartAndPlayViewTitleBinding(EngineContext engine) {
        return createStringBinding(
            () -> {
                final Named id = engine.viewManager().currentViewID();
                if (id == GameViewID.START_PAGES || id == GameViewID.GAMEPLAY) {
                    return optCurrentViewTitle(engine).orElse(titleForCurrentGameScene(engine));
                }
                if (id == GameViewID.EDITOR) {
                    return optCurrentViewTitle(engine).orElse(("Map Editor"));
                }
                return "";
            },
            engine.gameVariantManager().currentVariantIDProperty(),
            engine.gameSceneManager().currentGameSceneProperty(),
            engine.clock().updatesDisabledProperty(),
            engine.ui().viewModel().debugModeOnProperty(),
            engine.ui().viewModel().common3DSettings().view3DEnabledProperty(),
            engine.viewManager().currentViewIDProperty()
        );
    }

    private Optional<String> optCurrentViewTitle(EngineContext engine) {
        final GameView currentView = engine.viewManager().optCurrentView().orElse(null);
        return currentView == null ? Optional.empty() : currentView.optTitleSupplier().map(Supplier::get);
    }

    private String titleForCurrentGameScene(EngineContext engine) {
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

    private String stageTitle(EngineContext engine, boolean paused, boolean is3D) {
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
        final TranslationManager translationManager = uiConfig.translations();
        final String titleKey = paused ? "app.title.paused" : "app.title";
        if (translationManager.textBundle() != null
            && translationManager.textBundle().containsKey(titleKey)) {
            return translationManager.translate(titleKey, viewModeKey);
        } else {
            return "Unspecified Pac-Man Game Variant";
        }
    }
}
