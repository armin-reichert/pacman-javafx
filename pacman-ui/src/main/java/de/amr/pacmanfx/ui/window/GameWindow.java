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
import javafx.stage.Stage;

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
    private EngineUIContext engineContext;

    public GameWindow(Stage stage, int width, int height) {
        this.stage = requireNonNull(stage);

        mainScene = new GameMainScene(width, height);

        stage.setScene(mainScene);
        stage.setMinWidth(MIN_STAGE_WIDTH);
        stage.setMinHeight(MIN_STAGE_HEIGHT);
    }

    public void setEngineContext(EngineUIContext engineContext) {
        this.engineContext = requireNonNull(engineContext);

        mainScene.setEngineContext(engineContext);

        titleBinding = createStageTitleBinding();
        stage.titleProperty().bind(titleBinding);

        // Triggers title update
        connected.set(true);
    }

    public void show() {
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

    public void updateStageTitleBinding(GameViewManager viewManager, Named viewID) {
        if (viewID == GameViewID.START_PAGES || viewID == GameViewID.GAMEPLAY) {
            stage.titleProperty().bind(titleBinding);
        }
        else if (viewID == GameViewID.EDITOR) {
            viewManager.optView(GameViewID.EDITOR, EditorView.class).ifPresent(editorView -> {
                stage.titleProperty().unbind();
                editorView.optTitleSupplier().ifPresent(titleSupplier -> stage.setTitle(titleSupplier.get()));
            });
        }
    }

    // Private area

    private StringBinding createStageTitleBinding() {
        return createStringBinding(
            () -> {
                final Named id = engineContext.viewManager().currentViewID();
                if (id == GameViewID.START_PAGES || id == GameViewID.GAMEPLAY) {
                    return optCurrentViewTitle().orElse(titleForCurrentGameScene());
                }
                if (id == GameViewID.EDITOR) {
                    return optCurrentViewTitle().orElse(("Map Editor"));
                }
                return "";
            },
            connected,
            engineContext.gameVariantManager().selectedVariantNameProperty(),
            engineContext.gameSceneManager().currentGameSceneProperty(),
            engineContext.clock().updatesDisabledProperty(),
            engineContext.ui().viewModel().debugModeOnProperty(),
            engineContext.ui().viewModel().common3DSettings().view3DEnabledProperty(),
            engineContext.viewManager().currentViewIDProperty()
        );
    }

    private Optional<String> optCurrentViewTitle() {
        final GameView currentView = engineContext.viewManager().optCurrentView().orElse(null);
        return currentView == null ? Optional.empty() : currentView.optTitleSupplier().map(Supplier::get);
    }

    private String titleForCurrentGameScene() {
        final GameScene gameScene = engineContext.gameSceneManager().optCurrentGameScene().orElse(null);
        final GameViewModel viewModel = engineContext.ui().viewModel();

        final boolean debug  = viewModel.debugModeOnProperty().get();
        final boolean is3D   = viewModel.common3DSettings().view3DEnabledProperty().get();
        final boolean paused = engineContext.clock().areUpdatesDisabled();

        final String normalTitle = stageTitle(paused, is3D);
        return (gameScene == null || !debug)
            ? normalTitle
            : "%s [%s]".formatted(normalTitle, gameScene.getClass().getSimpleName());
    }

    private String stageTitle(boolean paused, boolean is3D) {
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
