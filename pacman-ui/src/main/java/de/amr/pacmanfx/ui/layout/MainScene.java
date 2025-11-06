/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.ui.api.ArcadePalette;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_View;
import de.amr.pacmanfx.uilib.widgets.FlashMessageView;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tinylog.Logger;

import java.net.URL;

import static java.util.Objects.requireNonNull;

public class MainScene {

    private static final String CONTEXT_MENU_STYLE_PATH = "/de/amr/pacmanfx/ui/css/menu-style.css";

    private final Scene scene;
    private final FlashMessageView flashMessageLayer;
    private final FontIcon pausedIcon;
    private final StatusIconBox statusIconBox;

    public MainScene(GameUI ui, double width, double height) {
        requireNonNull(ui);

        scene = new Scene(new StackPane(), width, height);
        flashMessageLayer = new FlashMessageView();

        URL url = getClass().getResource(CONTEXT_MENU_STYLE_PATH);
        if (url != null) {
            scene.getStylesheets().add(url.toExternalForm());
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
        scene.addEventFilter(KeyEvent.KEY_PRESSED,  ui.keyboard()::onKeyPressed);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, ui.keyboard()::onKeyReleased);

        var viewPlaceholder = new Region();
        rootPane().getChildren().addAll(viewPlaceholder, pausedIcon, statusIconBox, flashMessageLayer);
    }

    public Scene scene() {
        return scene;
    }

    public FontIcon pausedIcon() {
        return pausedIcon;
    }

    public StatusIconBox statusIconBox() {
        return statusIconBox;
    }

    public StackPane rootPane() {
        return (StackPane) scene.getRoot();
    }

    public FlashMessageView flashMessageLayer() {
        return flashMessageLayer;
    }

    public void embedView(GameUI_View view) {
        requireNonNull(view);
        rootPane().getChildren().set(0, view.root());
        view.root().requestFocus();
    }
}