/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.view;

import de.amr.basics.math.RandomNumberSupport;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.gamescene.GameScene;
import de.amr.pacmanfx.ui.gamescene.GameSceneManager;
import de.amr.pacmanfx.ui.input.KeyboardInfo;
import de.amr.pacmanfx.ui.subviews.SubView;
import de.amr.pacmanfx.ui.subviews.SubViewManager;
import de.amr.pacmanfx.ui.subviews.editor.EditorView;
import de.amr.pacmanfx.ui.subviews.playview.GamePlayView;
import de.amr.pacmanfx.ui.subviews.startpages.StartPagesView;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

public class GameViewImpl implements GameView {

    private final ObjectProperty<Stage> stage = new SimpleObjectProperty<>();

    private final GameViewMainScene mainScene;

    private Game game;

    private StatusIconBox statusIconBox;
    private KeyboardInfo keyboardInfo;
    private StringBinding stageTitleBinding;

    public GameViewImpl(int width, int height) {
        mainScene = new GameViewMainScene(width, height);
    }

    @Override
    public void connect(Game game) {

        if (this.game != null) {
            Logger.warn("Game view already connect to game!");
            return;
        }

        this.game = requireNonNull(game);

        final GameUI ui = game.ui();
        final SubViewManager subViews = ui.subViews();
        final GameSceneManager gameScenes = ui.gameScenes();

        subViews.setStartView(new StartPagesView(game));
        subViews.setGamePlayView(createGamePlaySubView(game));
        subViews.setEditorViewFactory(() -> createEditorSubView(subViews, game));

        createStatusIconBox(subViews, game);
        createKeyboardInfo(game);
        createStageTitleBinding(ui, subViews, gameScenes);
        populateMainScene(ui);
        initMainScene(game, subViews, gameScenes);

        // Some status icons are bound to the game model of the current game variant
        game.gameVariantNameProperty().addListener(
            (_,_,variantName) -> statusIconBox.bind(game.gameVariant(variantName).gameModel()));
    }

    @Override
    public void show() {
        final Stage theStage = stage();
        if (theStage == null) {
            throw new IllegalStateException("No stage assigned to game view");
        }
        theStage.setScene(mainScene);

        theStage.titleProperty().bind(stageTitleBindingProperty());

        updateStageIcon(game);
        registerIconUpdater(game);

        theStage.setMinWidth(GameUI_Constants.MIN_STAGE_WIDTH);
        theStage.setMinHeight(GameUI_Constants.MIN_STAGE_HEIGHT);
        theStage.centerOnScreen();

        theStage.show();
    }

    @Override
    public void replaceSubView(SubView subView) {
        mainScene.replaceSubView(subView);
    }

    @Override
    public ObjectProperty<Stage> stageProperty() {
        return stage;
    }

    @Override
    public GameViewMainScene mainScene() {
        return mainScene;
    }

    @Override
    public StatusIconBox statusIconBox() {
        return statusIconBox;
    }

    public StringBinding stageTitleBindingProperty() {
        return stageTitleBinding;
    }

    private String computeStageTitle(Game game) {
        final SubView view = game.ui().subViews().currentView();
        return view == null
            ? game.ui().translations().translate("view.missing") // Should never happen
            : view.optTitleSupplier().map(Supplier::get).orElse(titleForCurrentGameScene(game));
    }

    // Private area

    private void populateMainScene(GameUI ui) {
        mainScene.rootPane().getChildren().addAll(
            new Region(), // placeholder, will be replaced by current view (start, play, edit)
            statusIconBox.rootPane(),
            ui.flashMessages().messageView().rootPane(),
            keyboardInfo.rootPane()
        );
    }

    private void initMainScene(Game game, SubViewManager subViews, GameSceneManager gameScenes) {
        mainScene.rootPane().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> gameScenes.currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_3D)
                ? GameUI_Constants.WALLPAPERS[RandomNumberSupport.randomInt(0, GameUI_Constants.WALLPAPERS.length)]
                : GameUI_Constants.BACKGROUND_PAC_MAN_WALLPAPER,
            subViews.selectedSubViewProperty(),
            gameScenes.gameSceneProperty()
        ));

        mainScene.init(game);
    }

    private void createStageTitleBinding(GameUI ui, SubViewManager subViews, GameSceneManager gameScenes) {
        stageTitleBinding = createStringBinding(
            () -> computeStageTitle(game),
            game.clock().updatesDisabledProperty(),
            game.gameVariantNameProperty(),
            subViews.selectedSubViewProperty(),
            gameScenes.gameSceneProperty(),
            ui.settings().debugInfoVisibleProperty,
            ui.settings3D().view3DEnabledProperty()
        );
    }

    private void createStatusIconBox(SubViewManager subViews, Game game) {
        statusIconBox = new StatusIconBox(game);
        statusIconBox.rootPane().visibleProperty().bind(
            Bindings.createBooleanBinding(
                () -> subViews.isSelected(subViews.gamePlayView()) || subViews.isSelected(subViews.startView()),
                subViews.selectedSubViewProperty()
            )
        );
        StackPane.setAlignment(statusIconBox.rootPane(), Pos.BOTTOM_LEFT);
    }

    private void createKeyboardInfo(Game game) {
        keyboardInfo = new KeyboardInfo(game.ui(), game.input().keyboard());
        keyboardInfo.rootPane().setAlignment(Pos.TOP_CENTER);
    }

    private GamePlayView createGamePlaySubView(Game game) {
        final var playView = new GamePlayView(game, GameUI_Constants.DEFAULT_DASHBOARD_CONFIG);
        final ChangeListener<? super Number> resizeHandler = (_,_,_) -> playView.resizeToFit(mainScene);
        mainScene.widthProperty().addListener(resizeHandler);
        mainScene.heightProperty().addListener(resizeHandler);
        return playView;
    }

    private EditorView createEditorSubView(SubViewManager subViews, Game game) {
        final var editorView = new EditorView(stage(), game);
        editorView.editor().setOnQuit(_ -> {
            // restore title (editor changed it)
            stage().titleProperty().unbind();
            stage().titleProperty().bind(stageTitleBindingProperty());
            subViews.selectStartView();
        });
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

    private final ChangeListener<String> iconUpdateListener = (_, _, _) -> updateStageIcon(game);

    private void registerIconUpdater(Game game) {
        game.gameVariantNameProperty().removeListener(iconUpdateListener);
        game.gameVariantNameProperty().addListener(iconUpdateListener);
    }

    private String titleForCurrentGameScene(Game game) {
        final GameScene gameScene = game.ui().gameScenes().optCurrentGameScene().orElse(null);

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
