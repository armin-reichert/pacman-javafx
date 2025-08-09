/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._2d.ArcadePalette;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.uilib.widgets.FlashMessageView;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class MainScene extends Scene {

    private final ObjectProperty<PacManGames_View> propertyCurrentView = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            PacManGames_View newView = get();
            if (newView != null) {
                rootPane().getChildren().set(0, newView.rootNode());
                newView.rootNode().requestFocus();
            }
        }
    };

    private final ObjectProperty<GameScene> propertyCurrentGameScene = new SimpleObjectProperty<>();

    private final GameUI ui;
    private final FlashMessageView flashMessageLayer;

    public MainScene(GameUI ui, double width, double height) {
        super(new StackPane(), width, height);
        this.ui = requireNonNull(ui);
        this.flashMessageLayer = new FlashMessageView();

        URL url = getClass().getResource("css/menu-style.css");
        if (url != null) {
            getStylesheets().add(url.toExternalForm());
        }

        // Large "paused" icon appears at center of UI
        var pausedIcon = FontIcon.of(FontAwesomeSolid.PAUSE, 80, ArcadePalette.ARCADE_WHITE);
        StackPane.setAlignment(pausedIcon, Pos.CENTER);

        pausedIcon.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> currentView() == ui.thePlayView() && ui.theGameClock().isPaused(),
            propertyCurrentView, ui.theGameClock().pausedProperty()));

        // Status icon box appears at bottom-left corner of any view except editor
        var iconBox = new StatusIconBox();
        StackPane.setAlignment(iconBox, Pos.BOTTOM_LEFT);

        // No icons when editor is open
        iconBox.visibleProperty().bind(propertyCurrentView.map(
            currentView -> ui.theEditorView().isEmpty() || currentView != ui.theEditorView().get()));

        iconBox.iconMuted().visibleProperty().bind(GameUI.PROPERTY_MUTED);
        iconBox.icon3D().visibleProperty().bind(GameUI.PROPERTY_3D_ENABLED);
        iconBox.iconAutopilot().visibleProperty().bind(ui.theGameContext().theGameController().propertyUsingAutopilot());
        iconBox.iconImmune().visibleProperty().bind(ui.theGameContext().theGameController().propertyImmunity());

        addEventFilter(KeyEvent.KEY_PRESSED, ui.theKeyboard()::onKeyPressed);
        addEventFilter(KeyEvent.KEY_RELEASED, ui.theKeyboard()::onKeyReleased);

        var viewPlaceholder = new Region();
        rootPane().getChildren().addAll(viewPlaceholder, pausedIcon, iconBox, flashMessageLayer);
    }

    public StackPane rootPane() {
        return (StackPane) getRoot();
    }

    public FlashMessageView flashMessageLayer() {
        return flashMessageLayer;
    }

    public ObjectProperty<PacManGames_View> currentViewProperty() {
        return propertyCurrentView;
    }

    public PacManGames_View currentView() {
        return propertyCurrentView.get();
    }

    public ObjectProperty<GameScene> currentGameSceneProperty() {
        return propertyCurrentGameScene;
    }

    public Optional<GameScene> currentGameScene() {
        return Optional.ofNullable(propertyCurrentGameScene.get());
    }

    // Asset key regex: app.title.(ms_pacman|ms_pacman_xxl|pacman,pacman_xxl|tengen)(.paused)?
    public String computeTitle(boolean threeDModeEnabled, boolean modeDebug) {
        String ans = ui.theConfiguration().assetNamespace();
        String paused = ui.theGameClock().isPaused() ? ".paused" : "";
        String key = "app.title." + ans + paused;
        String modeText = ui.theAssets().text(threeDModeEnabled ? "threeD" : "twoD");
        GameScene currentGameScene = currentGameScene().orElse(null);
        if (currentGameScene == null || !modeDebug) {
            return ui.theAssets().text(key, modeText);
        }
        String sceneClassName = currentGameScene.getClass().getSimpleName();
        if (currentGameScene instanceof GameScene2D gameScene2D) {
            return ui.theAssets().text(key, modeText)
                + " [%s]".formatted(sceneClassName)
                + " (%.2fx)".formatted(gameScene2D.scaling());
        }
        return ui.theAssets().text(key, modeText) + " [%s]".formatted(sceneClassName);
    }
}