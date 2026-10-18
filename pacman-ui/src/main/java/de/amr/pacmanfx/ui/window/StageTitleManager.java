/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.window;

import de.amr.basics.Named;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.pacmanfx.app.runtime.DefaultGameVariantRuntime;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.GameViewManager;
import de.amr.pacmanfx.ui.views.editor.EditorView;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;

import java.util.Optional;
import java.util.function.Supplier;

import static javafx.beans.binding.Bindings.createStringBinding;

public class StageTitleManager {

    private final BooleanProperty connected = new SimpleBooleanProperty(false);

    private final StringBinding currentBinding;

    private final StringProperty stageTitleProperty;

    public StageTitleManager(EngineUIContext engine, StringProperty stageTitleProperty) {
        this.stageTitleProperty = stageTitleProperty;
        currentBinding = createStageTitleBinding(engine);
        stageTitleProperty.bind(currentBinding);
        // Triggers title update
        connected.set(true);
    }

    public void update(GameViewManager viewManager, Named viewID) {
        if (viewID == GameViewID.START_PAGES || viewID == GameViewID.GAMEPLAY) {
            stageTitleProperty.bind(currentBinding);
        }
        else if (viewID == GameViewID.EDITOR) {
            viewManager.optView(GameViewID.EDITOR, EditorView.class).ifPresent(editorView -> {
                stageTitleProperty.unbind();
                editorView.optTitleSupplier().ifPresent(titleSupplier -> stageTitleProperty.set(titleSupplier.get()));
            });
        }
    }

    // Private area

    private StringBinding createStageTitleBinding(EngineUIContext engine) {
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
            connected,
            engine.gameVariantManager().selectedVariantNameProperty(),
            engine.gameSceneManager().currentGameSceneProperty(),
            engine.clock().updatesDisabledProperty(),
            engine.ui().viewModel().debugModeOnProperty(),
            engine.ui().viewModel().common3DSettings().view3DEnabledProperty(),
            engine.viewManager().currentViewIDProperty()
        );
    }

    private Optional<String> optCurrentViewTitle(EngineUIContext engine) {
        final GameView currentView = engine.viewManager().optCurrentView().orElse(null);
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
        final String gameVariantName = engine.gameVariantManager().currentVariantName();
        if (gameVariantName == null) {
            return "";
        }

        final String viewModeKey = engine.translationManager().translate(is3D ?
            "view_mode.3d" : "view_mode.2d");

        // In game-variant specific resource bundles, there should be two entries with placeholder
        // app.title = Game Variant Name {0}
        // app.title = Game Variant Name {0} (paused)

        final DefaultGameVariantRuntime runtime = (DefaultGameVariantRuntime) engine.gameVariantManager().currentRuntime();
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
