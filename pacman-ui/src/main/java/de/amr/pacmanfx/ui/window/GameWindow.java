/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.window;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
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

    public void setActionContext(GameAppContext actionContext) {
        mainScene.setGameActionContext(actionContext);

        titleBinding = createStageTitleBinding(actionContext);
        stage.titleProperty().bind(titleBinding);

        //TODO Without this, the title is not changed when returning from the editor. Why?
        actionContext.ui().views().currentViewIDProperty().addListener(
            (_, _, viewID) -> updateStageTitleBinding(actionContext.ui(), viewID));

        actionContext.variants().addVariantNameListener((_, _, _) -> updateStageIcon(actionContext));

        // Triggers title update
        connected.set(true);
    }

    public void show(GameAppContext actionContext) {
        updateStageIcon(actionContext);
        stage.centerOnScreen();
        stage.show();
    }

    public Stage stage() {
        return stage;
    }

    public GameMainScene mainScene() {
        return mainScene;
    }

    // Private area

    private StringBinding createStageTitleBinding(GameAppContext actionContext) {
        final GameUI ui = actionContext.ui();
        return createStringBinding(
            () -> switch (ui.views().currentViewID()) {
                case null -> ""; // happens initially, don't mind
                case START_PAGES, GAMEPLAY -> optCurrentViewTitle(ui).orElse(titleForCurrentGameScene(actionContext));
                // Editor has its own title supplier → use it directly
                case EDITOR -> optCurrentViewTitle(ui).orElse(("Map Editor"));
            },
            connected,
            actionContext.variants().variantNameProperty(),
            actionContext.clock().updatesDisabledProperty(),
            ui.viewModel().debugModeOnProperty,
            ui.viewModel().common3D.view3DEnabledProperty,
            ui.views().currentViewIDProperty(),
            ui.gameScenes().currentGameSceneProperty()
        );
    }

    private Optional<String> optCurrentViewTitle(GameUI ui) {
        return ui.views().optCurrentView().isEmpty()
            ? Optional.of("No View present")
            : ui.views().assertCurrentView().optTitleSupplier().map(Supplier::get);
    }

    private void updateStageTitleBinding(GameUI ui, GameViewID viewID) {
        switch (viewID) {
            case START_PAGES, GAMEPLAY -> stage.titleProperty().bind(titleBinding);
            case EDITOR -> ui.views().optEditorView().ifPresent(editorView -> {
                stage.titleProperty().unbind();
                editorView.optTitleSupplier().ifPresent(titleSupplier -> stage.setTitle(titleSupplier.get()));
            });
        }
    }

    private void updateStageIcon(GameAppContext actionContext) {
        final Image icon = actionContext.variants().currentVariant().config().assets().image("app_icon");
        if (icon != null) {
            stage.getIcons().setAll(icon);
        } else {
            Logger.error("Could not access stage icon");
        }
    }

    private String titleForCurrentGameScene(GameAppContext actionContext) {
        final GameScene gameScene = actionContext.ui().gameScenes().optCurrentGameScene().orElse(null);

        final boolean debug = actionContext.ui().viewModel().debugModeOnProperty.get();
        final boolean is3D = actionContext.ui().viewModel().common3D.view3DEnabledProperty.get();
        final boolean paused = actionContext.clock().getUpdatesDisabled();

        final String normalTitle = stageTitle(actionContext, paused, is3D);
        return (gameScene == null || !debug)
            ? normalTitle
            : "%s [%s]".formatted(normalTitle, gameScene.getClass().getSimpleName());
    }

    private String stageTitle(GameAppContext actionContext, boolean paused, boolean is3D) {
        final String gameVariantName = actionContext.variants().currentVariantName();
        if (gameVariantName == null) {
            return "";
        }

        final String viewModeKey = actionContext.ui().translations().translate(is3D ?
            "view_mode.3d" : "view_mode.2d");

        // In game-variant specific resource bundles, there should be two entries with placeholder
        // app.title = Game Variant Name {0}
        // app.title = Game Variant Name {0} (paused)

        final TranslationManager variantTranslations = actionContext.variants().currentVariant().config().translations();
        final String titleKey = paused ? "app.title.paused" : "app.title";
        if (variantTranslations.textBundle() != null
            && variantTranslations.textBundle().containsKey(titleKey)) {
            return variantTranslations.translate(titleKey, viewModeKey);
        } else {
            return "Unspecified Pac-Man Game Variant";
        }
    }
}
