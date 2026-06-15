/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.view;

import de.amr.basics.math.RandomNumberSupport;
import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.action.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.CommonSceneID;
import de.amr.pacmanfx.ui.input.KeyboardInfo;
import de.amr.pacmanfx.ui.subviews.SubView;
import de.amr.pacmanfx.ui.subviews.SubViewManager;
import de.amr.pacmanfx.ui.subviews.editor.EditorView;
import de.amr.pacmanfx.ui.subviews.playview.GamePlayView;
import de.amr.pacmanfx.ui.subviews.startpages.StartPagesView;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.Set;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

public class GameViewImpl implements GameView {

    private Game game;

    private final ChangeListener<String> iconUpdateListener = (_, _, _) -> updateStageIcon(game);
    private StringBinding stageTitleBinding;

    // The view components
    private final Stage stage;
    private final GameMainScene mainScene;
    private StatusIconBox statusIconBox;
    private KeyboardInfo keyboardInfoPopup;
    private final StartPagesView startPagesView;
    private final GamePlayView gamePlayView;

    public GameViewImpl(Stage stage, int width, int height) {
        this.stage = requireNonNull(stage);
        mainScene = new GameMainScene(width, height);
        mainScene.getStylesheets().add(GameUI_Constants.STYLE_SHEET_PATH);

        startPagesView = new StartPagesView();
        gamePlayView = createGamePlayView();
    }

    @Override
    public void connect(Game game) {
        requireNonNull(game);

        if (this.game != null) {
            Logger.warn("Game view already connect to game!");
            return;
        }
        this.game = game;

        // Set sub views
        final SubViewManager subViews = game.ui().subViews();
        subViews.setStartView(startPagesView);
        subViews.setGamePlayView(gamePlayView);
        subViews.setEditorViewFactory(() -> createEditorSubView(game));

        createStatusIconBox(game);
        createKeyboardInfoPopup(game);
        populateMainScene(game);

        createStageTitleBinding(game);
        initMainScene(game);
        registerCommonActions(game);

        startPagesView.connect(game);

        // Some status icons are bound to the game model of the *current* game variant
        game.gameVariantNameProperty().addListener((_,_,variantName) -> {
            statusIconBox.bind(game.gameVariant(variantName).gameModel());
            //TODO This does not belong here
            subViews.gamePlayView().gameSceneFrame().clearCanvas();
        });
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

    private void prepareStageForDisplay(Game game) {
        stage.setMinWidth(GameUI_Constants.MIN_STAGE_WIDTH);
        stage.setMinHeight(GameUI_Constants.MIN_STAGE_HEIGHT);
        stage.setScene(mainScene);
        stage.titleProperty().bind(stageTitleBinding);
        updateStageIcon(game);
        registerIconUpdater(game);
    }

    private void populateMainScene(Game game) {
        mainScene.rootPane().getChildren().addAll(
            new Region(), // placeholder, will be replaced by current view (start, play, edit)
            statusIconBox.rootPane(),
            game.ui().flashMessages().messageView().rootPane(),
            keyboardInfoPopup.rootPane()
        );
    }

    private void initMainScene(Game game) {
        mainScene.rootPane().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> game.ui().gameScenes().currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_3D)
                ? GameUI_Constants.WALLPAPERS[RandomNumberSupport.randomInt(0, GameUI_Constants.WALLPAPERS.length)]
                : GameUI_Constants.BACKGROUND_PAC_MAN_WALLPAPER,
            game.ui().subViews().selectedSubViewProperty(),
            game.ui().gameScenes().gameSceneProperty()
        ));
        mainScene.connect(game);
    }

    private void registerCommonActions(Game game) {
        final CommonActions actions = game.actions();
        final Set<ActionKeyBinding> bindings = actions.bindings();
        mainScene.actionBindings().selectAnyMatchingBinding(actions.uiSettingsActions().actionToggleKeyboardMonitor(), bindings);
        mainScene.actionBindings().selectAnyMatchingBinding(actions.uiSettingsActions().actionEnterFullScreen(), bindings);
        mainScene.actionBindings().selectAnyMatchingBinding(actions.simulationActions().actionToggleMuted(), bindings);
        mainScene.actionBindings().selectAnyMatchingBinding(actions.editorActions().actionOpenEditor(), bindings);
        Logger.info(mainScene.actionBindings());
    }

    private void createStageTitleBinding(Game game) {
        stageTitleBinding = createStringBinding(
            () -> computeStageTitle(game),
            game.clock().updatesDisabledProperty(),
            game.gameVariantNameProperty(),
            game.ui().subViews().selectedSubViewProperty(),
            game.ui().gameScenes().gameSceneProperty(),
            game.ui().settings().debugInfoVisibleProperty,
            game.ui().settings3D().view3DEnabledProperty()
        );
    }

    private String computeStageTitle(Game game) {
        final SubView currentSubView = game.ui().subViews().currentView();
        return currentSubView == null
            ? game.ui().translations().translate("view.missing") // Should never happen
            : currentSubView.optTitleSupplier().map(Supplier::get).orElse(titleForCurrentGameScene(game));
    }

    private void createStatusIconBox(Game game) {
        final SubViewManager subViews = game.ui().subViews();
        statusIconBox = new StatusIconBox(game);
        statusIconBox.rootPane().visibleProperty().bind(
            Bindings.createBooleanBinding(
                () -> subViews.isSelected(subViews.gamePlayView()) || subViews.isSelected(subViews.startView()),
                subViews.selectedSubViewProperty()
            )
        );
        StackPane.setAlignment(statusIconBox.rootPane(), Pos.BOTTOM_LEFT);
    }

    private void createKeyboardInfoPopup(Game game) {
        keyboardInfoPopup = new KeyboardInfo(game.ui(), game.input().keyboard());
        keyboardInfoPopup.rootPane().setAlignment(Pos.TOP_CENTER);
    }

    private GamePlayView createGamePlayView() {
        final var playView = new GamePlayView(GameUI_Constants.DEFAULT_DASHBOARD_CONFIG);
        final ChangeListener<? super Number> resizeHandler = (_,_,_) -> playView.resizeToFit(mainScene);
        mainScene.widthProperty().addListener(resizeHandler);
        mainScene.heightProperty().addListener(resizeHandler);
        return playView;
    }

    private EditorView createEditorSubView(Game game) {
        final var editorView = new EditorView(stage());
        editorView.editor().setOnQuit(_ -> {
            // restore title (editor changed it)
            stage().titleProperty().unbind();
            stage().titleProperty().bind(stageTitleBinding);
            game.ui().subViews().selectStartView();
        });
        editorView.connect(game);
        return editorView;
    }

    private void updateStageIcon(Game game) {
        final Image icon = game.currentUIConfig().assets().image("app_icon");
        if (icon != null) {
            game.ui().view().stage().getIcons().setAll(icon);
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
