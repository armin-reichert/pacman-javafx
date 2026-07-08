/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.window;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.game.Game;
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

    public void connect(Game game) {
        mainScene.connect(game);

        titleBinding = createStageTitleBinding(game);
        stage.titleProperty().bind(titleBinding);

        //TODO Without this, the title is not changed when returning from the editor. Why?
        game.ui().viewManager().currentViewIDProperty().addListener((_, _, viewID) -> updateStageTitleBinding(game, viewID));

        game.variantManager().addVariantNameListener((_, _, _) -> updateStageIcon(game));

        // Triggers title update
        connected.set(true);
    }

    public void show(Game game) {
        updateStageIcon(game);
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

    private StringBinding createStageTitleBinding(Game game) {
        final GameUI ui = game.ui();
        return createStringBinding(
            () -> switch (ui.viewManager().currentViewID()) {
                case null -> ""; // happens initially, don't mind
                case START_PAGES, GAMEPLAY -> optCurrentViewTitle(ui).orElse(titleForCurrentGameScene(game));
                // Editor has its own title supplier → use it directly
                case EDITOR -> optCurrentViewTitle(ui).orElse(("Map Editor"));
            },
            connected,
            game.variantManager().variantNameProperty(),
            game.machine().clock().updatesDisabledProperty(),
            ui.viewModel().debugModeOnProperty,
            ui.viewModel().common3D.view3DEnabledProperty,
            ui.viewManager().currentViewIDProperty(),
            ui.gameSceneManager().currentGameSceneProperty()
        );
    }

    private Optional<String> optCurrentViewTitle(GameUI ui) {
        return ui.viewManager().optCurrentView().isEmpty()
            ? Optional.of("No View present")
            : ui.viewManager().assertCurrentView().optTitleSupplier().map(Supplier::get);
    }

    private void updateStageTitleBinding(Game game, GameViewID viewID) {
        switch (viewID) {
            case START_PAGES, GAMEPLAY -> stage.titleProperty().bind(titleBinding);
            case EDITOR -> game.ui().viewManager().optEditorView().ifPresent(editorView -> {
                stage.titleProperty().unbind();
                editorView.optTitleSupplier().ifPresent(titleSupplier -> stage.setTitle(titleSupplier.get()));
            });
        }
    }

    private void updateStageIcon(Game game) {
        final Image icon = game.variantManager().selectedVariant().config().assets().image("app_icon");
        if (icon != null) {
            stage.getIcons().setAll(icon);
        } else {
            Logger.error("Could not access stage icon");
        }
    }

    private String titleForCurrentGameScene(Game game) {
        final GameScene gameScene = game.ui().gameSceneManager().optCurrentGameScene().orElse(null);

        final boolean debug = game.ui().viewModel().debugModeOnProperty.get();
        final boolean is3D = game.ui().viewModel().common3D.view3DEnabledProperty.get();
        final boolean paused = game.machine().clock().getUpdatesDisabled();

        final String normalTitle = stageTitle(game, paused, is3D);
        return (gameScene == null || !debug)
            ? normalTitle
            : "%s [%s]".formatted(normalTitle, gameScene.getClass().getSimpleName());
    }

    private String stageTitle(Game game, boolean paused, boolean is3D) {
        final String gameVariantName = game.variantManager().selectedVariantName();
        if (gameVariantName == null) {
            return "";
        }

        final String viewModeKey = game.ui().translations().translate(is3D ?
            "view_mode.3d" : "view_mode.2d");

        // In game-variant specific resource bundles, there should be two entries with placeholder
        // app.title = Game Variant Name {0}
        // app.title = Game Variant Name {0} (paused)

        final TranslationManager variantTranslations = game.variantManager().selectedVariant().config().translations();
        final String titleKey = paused ? "app.title.paused" : "app.title";
        if (variantTranslations.textBundle() != null
            && variantTranslations.textBundle().containsKey(titleKey)) {
            return variantTranslations.translate(titleKey, viewModeKey);
        } else {
            return "Unspecified Pac-Man Game Variant";
        }
    }
}
