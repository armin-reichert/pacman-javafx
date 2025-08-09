package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.Optional;

import static de.amr.pacmanfx.ui.GameUI_Config.SCENE_ID_PLAY_SCENE_3D;
import static java.util.Objects.requireNonNull;

public class GameUI_MainScene extends Scene {

    private final PacManGames_Assets assets;
    private GameUI_Config uiConfig;
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
