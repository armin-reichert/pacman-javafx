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

import java.net.URL;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class MainScene extends Scene {

    private final ObjectProperty<GameUI_View> propertyCurrentView = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            GameUI_View newView = get();
            if (newView != null) {
                rootPane().getChildren().set(0, newView.root());
                newView.root().requestFocus();
            }
        }
    };

    private final ObjectProperty<GameScene> propertyCurrentGameScene = new SimpleObjectProperty<>();

    private final FlashMessageView flashMessageLayer;
    private final FontIcon pausedIcon;
    private final StatusIconBox statusIconBox;

    public MainScene(GameUI ui, double width, double height) {
        super(new StackPane(), width, height);
        requireNonNull(ui);
        this.flashMessageLayer = new FlashMessageView();

        URL url = getClass().getResource("/de/amr/pacmanfx/ui/css/menu-style.css");
        if (url != null) {
            getStylesheets().add(url.toExternalForm());
        }

        // Large "paused" icon appears at center of UI
        pausedIcon = FontIcon.of(FontAwesomeSolid.PAUSE, 80, ArcadePalette.ARCADE_WHITE);
        StackPane.setAlignment(pausedIcon, Pos.CENTER);

        // Status icon box appears at bottom-left corner of any view except editor
        statusIconBox = new StatusIconBox();
        StackPane.setAlignment(statusIconBox, Pos.BOTTOM_LEFT);

        addEventFilter(KeyEvent.KEY_PRESSED, ui.keyboard()::onKeyPressed);
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
        return propertyCurrentView;
    }

    public GameUI_View currentView() {
        return propertyCurrentView.get();
    }

    public ObjectProperty<GameScene> currentGameSceneProperty() {
        return propertyCurrentGameScene;
    }

    public Optional<GameScene> currentGameScene() {
        return Optional.ofNullable(propertyCurrentGameScene.get());
    }
}