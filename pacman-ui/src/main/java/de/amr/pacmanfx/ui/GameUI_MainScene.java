package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui._2d.ArcadePalette;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.layout.PacManGames_View;
import de.amr.pacmanfx.ui.layout.StatusIconBox;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.Optional;

import static de.amr.pacmanfx.ui.GameUI_Config.SCENE_ID_PLAY_SCENE_3D;
import static java.util.Objects.requireNonNull;

public class GameUI_MainScene extends Scene {

    private final PacManGames_Assets assets;
    private GameUI_Config uiConfig;

    private final ObjectProperty<PacManGames_View> propertyCurrentView = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            PacManGames_View newView = get();
            rootPane().getChildren().set(0, newView.rootNode());
            newView.rootNode().requestFocus();
        }
    };

    private final ObjectProperty<GameScene> propertyCurrentGameScene = new SimpleObjectProperty<>();

    public GameUI_MainScene(PacManGames_Assets assets, Keyboard keyboard, double width, double height) {
        super(new StackPane(), width, height);
        this.assets = requireNonNull(assets);
        addEventFilter(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
        addEventFilter(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);
        URL url = getClass().getResource("css/menu-style.css");
        if (url != null) {
            getStylesheets().add(url.toExternalForm());
        }
        rootPane().getChildren().add(new Pane()); // will be replaced by current view
    }

    public void createStatusIcons(GameUI ui) {
        // Large "paused" icon appears at center of UI
        var pausedIcon = FontIcon.of(FontAwesomeSolid.PAUSE, 80, ArcadePalette.ARCADE_WHITE);
        StackPane.setAlignment(pausedIcon, Pos.CENTER);

        pausedIcon.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> currentView() == ui.thePlayView() && ui.theGameClock().isPaused(),
            propertyCurrentView, ui.theGameClock().pausedProperty()));

        // Status icon box appears at bottom-left corner of any view except editor
        var iconBox = new StatusIconBox();
        StackPane.setAlignment(iconBox, Pos.BOTTOM_LEFT);

        rootPane().getChildren().addAll(pausedIcon, iconBox);

        // No icons when editor is open
        iconBox.visibleProperty().bind(propertyCurrentView.map(
            currentView -> ui.theEditorView().isEmpty() || currentView != ui.theEditorView().get()));

        iconBox.iconMuted().visibleProperty().bind(ui.propertyMuted());
        iconBox.icon3D().visibleProperty().bind(ui.property3DEnabled());
        iconBox.iconAutopilot().visibleProperty().bind(ui.theGameContext().theGameController().propertyUsingAutopilot());
        iconBox.iconImmune().visibleProperty().bind(ui.theGameContext().theGameController().propertyImmunity());
    }

    public void setUiConfig(GameUI_Config uiConfig) {
        this.uiConfig = uiConfig;
        rootPane().backgroundProperty().bind(propertyCurrentGameScene.map(gameScene ->
            isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_3D)
                ? assets.get("background.play_scene3d")
                : assets.get("background.scene"))
        );
    }

    public StackPane rootPane() {
        return (StackPane) getRoot();
    }

    public ObjectProperty<PacManGames_View> currentViewProperty() {
        return propertyCurrentView;
    }

    public PacManGames_View currentView() {
        return propertyCurrentView.get();
    }

    public void setCurrentView(PacManGames_View view) {
        requireNonNull(view);
        propertyCurrentView.set(view);
    }

    public ObjectProperty<GameScene> currentGameSceneProperty() {
        return propertyCurrentGameScene;
    }

    public Optional<GameScene> currentGameScene() {
        return Optional.ofNullable(propertyCurrentGameScene.get());
    }

    public boolean isCurrentGameSceneID(String id) {
        GameScene currentGameScene = currentGameScene().orElse(null);
        return currentGameScene != null && uiConfig.gameSceneHasID(currentGameScene, id);
    }
}
