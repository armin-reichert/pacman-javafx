/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.ui._2d.ArcadePalette;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.uilib.widgets.FlashMessageView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tinylog.Logger;

import java.net.URL;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class MainScene extends Scene {

    private static final String CONTEXT_MENU_STYLE_PATH = "/de/amr/pacmanfx/ui/css/menu-style.css";

    private final ObjectProperty<GameUI_View> currentView = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            GameUI_View newView = get();
            if (newView != null) {
                rootPane().getChildren().set(0, newView.root());
                newView.root().requestFocus();
            }
        }
    };

    private final ObjectProperty<GameScene> currentGameScene = new SimpleObjectProperty<>();

    private final FlashMessageView flashMessageLayer;
    private final FontIcon pausedIcon;
    private final StatusIconBox statusIconBox;

    public MainScene(GameUI ui, double width, double height) {
        super(new StackPane(), width, height);
        requireNonNull(ui);

        flashMessageLayer = new FlashMessageView();

        URL url = getClass().getResource(CONTEXT_MENU_STYLE_PATH);
        if (url != null) {
            getStylesheets().add(url.toExternalForm());
        } else {
            Logger.error("Could not access menu style CSS file at '{}'", CONTEXT_MENU_STYLE_PATH);
        }

        // Large "paused" icon wich appears at center of scene
        pausedIcon = FontIcon.of(FontAwesomeSolid.PAUSE, 80, ArcadePalette.ARCADE_WHITE);
        StackPane.setAlignment(pausedIcon, Pos.CENTER);

        // Status icon box appears at bottom-left corner of all views except editor view
        statusIconBox = new StatusIconBox();
        StackPane.setAlignment(statusIconBox, Pos.BOTTOM_LEFT);

        // Keyboard events are processed by global keyboard instance and key state is stored
        addEventFilter(KeyEvent.KEY_PRESSED,  ui.keyboard()::onKeyPressed);
        addEventFilter(KeyEvent.KEY_RELEASED, ui.keyboard()::onKeyReleased);

        var viewPlaceholder = new Region();
        rootPane().getChildren().addAll(viewPlaceholder, pausedIcon, statusIconBox, flashMessageLayer);
    }

    public FontIcon pausedIcon() {
        return pausedIcon;
    }

    public StatusIconBox statusIconBox() {
        return statusIconBox;
    }

    public StackPane rootPane() {
        return (StackPane) getRoot();
    }

    public FlashMessageView flashMessageLayer() {
        return flashMessageLayer;
    }

    public ObjectProperty<GameUI_View> currentViewProperty() {
        return currentView;
    }

    public GameUI_View currentView() {
        return currentView.get();
    }

    public ObjectProperty<GameScene> currentGameSceneProperty() {
        return currentGameScene;
    }

    public Optional<GameScene> currentGameScene() {
        return Optional.ofNullable(currentGameScene.get());
    }
}