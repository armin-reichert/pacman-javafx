/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.window;

import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

public class GameWindowImpl implements GameWindow {

    private Game game;

    private final ChangeListener<String> iconUpdateListener = (_, _, _) -> updateStageIcon(game);
    private StringBinding stageTitleBinding;

    private final Stage stage;
    private final GameMainScene mainScene;

    public GameWindowImpl(Stage stage, int width, int height) {
        this.stage = requireNonNull(stage);
        mainScene = new GameMainScene(width, height);
        mainScene.getStylesheets().add(GameUI_Constants.STYLE_SHEET_PATH);
    }

    @Override
    public void connect(Game game) {
        if (this.game != null) {
            Logger.warn("Game view already connect to game!");
            return;
        }
        this.game = requireNonNull(game);

        createStageTitleBinding(game);
        game.ui().window().stage().titleProperty().bind(stageTitleBinding);

        mainScene.connect(game);

        // Some status icons are bound to the game model of the *current* game variant
        game.gameVariantNameProperty().addListener((_, _, _) -> {
            //TODO This does not belong here
            game.ui().views().gamePlayView().gameSceneFrame().clearCanvas();
        });

        // Adapt stage title to current game view
        game.ui().views().currentViewIDProperty().addListener(
            (_, _, viewID) -> updateStageTitle(viewID));
    }

    @Override
    public void show() {
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

    private void updateStageTitle(GameViewID viewID) {
        switch (viewID) {
            case START_PAGES, GAMEPLAY -> stage.titleProperty().bind(stageTitleBinding);
            case EDITOR -> game.ui().views().optEditorView().ifPresent(editorView -> {
                stage.titleProperty().unbind();
                editorView.optTitleSupplier().ifPresent(titleSupplier -> stage.setTitle(titleSupplier.get()));
            });
        }
    }

    private void prepareStageForDisplay(Game game) {
        stage.setMinWidth(GameUI_Constants.MIN_STAGE_WIDTH);
        stage.setMinHeight(GameUI_Constants.MIN_STAGE_HEIGHT);
        stage.setScene(mainScene);
        stage.titleProperty().bind(stageTitleBinding);
        updateStageIcon(game);
        registerIconUpdater(game);
    }

    private void createStageTitleBinding(Game game) {
        stageTitleBinding = createStringBinding(
            () -> computeStageTitle(game),
            game.clock().updatesDisabledProperty(),
            game.gameVariantNameProperty(),
            game.ui().views().currentViewIDProperty(),
            game.ui().gameScenes().currentGameSceneProperty(),
            game.ui().settings().debugInfoVisibleProperty,
            game.ui().settings3D().view3DEnabledProperty()
        );
    }

    private String computeStageTitle(Game game) {
        final Optional<GameView> optCurrentGameView = game.ui().views().optCurrentView();
        return optCurrentGameView.isEmpty()
            ? game.ui().translations().translate("view.missing") // Should never happen
            : optCurrentGameView.get().optTitleSupplier().map(Supplier::get).orElse(titleForCurrentGameScene(game));
    }

    private void updateStageIcon(Game game) {
        final Image icon = game.currentUIConfig().assets().image("app_icon");
        if (icon != null) {
            game.ui().window().stage().getIcons().setAll(icon);
        } else {
            Logger.error("Could not access stage icon");
        }
    }

    private void registerIconUpdater(Game game) {
        game.gameVariantNameProperty().removeListener(iconUpdateListener);
        game.gameVariantNameProperty().addListener(iconUpdateListener);
    }

    private String titleForCurrentGameScene(Game game) {
        final AbstractGameScene gameScene = game.ui().gameScenes().optCurrentGameScene().orElse(null);

        final boolean debug = game.ui().settings().debugInfoVisibleProperty.get();
        final boolean is3D = game.ui().settings3D().view3DEnabledProperty().get();
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

        final String viewMode = game.ui().translations().translate(is3D ? "threeD" : "twoD");

        // In game-variant specific resource bundles, there should be two entries with placeholder
        // app.title = Game Variant Name {0}
        // app.title = Game Variant Name {0} (paused)

        final TranslationManager appSpecificTranslator = game.currentUIConfig();
        final String appTitleKey = paused ? "app.title.paused" : "app.title";
        if (appSpecificTranslator.textBundle() != null
            && appSpecificTranslator.textBundle().containsKey(appTitleKey)) {
            return appSpecificTranslator.translate(appTitleKey, viewMode);
        } else {
            return "Unspecified Game";
        }
    }
}
