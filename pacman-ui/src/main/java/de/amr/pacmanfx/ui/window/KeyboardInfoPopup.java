/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.window;

import de.amr.pacmanfx.game.PacManGamesCollection;
import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

public class KeyboardInfoPopup {

    public static final Font LABEL_FONT = Font.font("Monospace", FontWeight.NORMAL, 16);
    public static final Font TITLE_FONT = Font.font("Sans", FontWeight.BOLD, 16);
    public static final Font CLOSE_HINT_FONT = Font.font("Sans", FontWeight.NORMAL, 16);

    private final VBox rootPane = new VBox();
    private final VBox keyInfoBox = new VBox();

    public KeyboardInfoPopup() {
        rootPane.setPrefSize(280, 50);
        rootPane.setMaxSize(280, 200);
        rootPane.setSpacing(3);

        rootPane.setStyle(
            """
            -fx-border-color: #ccc;
            -fx-border-width: 2;
            -fx-border-radius: 12;
            -fx-background-color: rgb(100,100,100,0.66);
            -fx-background-radius: 12;
            """
        );

        StackPane.setMargin(rootPane, new Insets(10));
        StackPane.setAlignment(rootPane, Pos.TOP_RIGHT);

        final Text title = new Text("Keyboard State");
        title.setFill(Color.WHITE);
        title.setFont(TITLE_FONT);
        rootPane.getChildren().add(title);

        final Text closeHint = new Text("(Alt+K to close)");
        closeHint.setFill(Color.WHITE);
        closeHint.setFont(CLOSE_HINT_FONT);
        rootPane.getChildren().add(closeHint);

        keyInfoBox.setAlignment(Pos.CENTER);
        keyInfoBox.setPadding(new Insets(10));
        rootPane.getChildren().add(keyInfoBox);
    }

    public void connect(PacManGamesCollection game) {
        rootPane.visibleProperty().bind(game.ui().viewModel().keyboardMonitorOnProperty);

        game.machine().input().keyboard().addStateListener(state -> {
            keyInfoBox.getChildren().clear();
            final String modifiersText = createModifierString(state);
            keyInfoBox.getChildren().add(createInfoText("[" + modifiersText + "]"));
            for (KeyCode key : state.pressedKeys()) {
                keyInfoBox.getChildren().add(createInfoText(modifiersText + " " + key));
            }
        });
    }

    public VBox rootPane() {
        return rootPane;
    }

    private static String createModifierString(Keyboard keyboard) {
        final List<String> modifiers = new ArrayList<>();
        if (keyboard.altDown()) modifiers.add("Alt");
        if (keyboard.shiftDown()) modifiers.add("Shift");
        if (keyboard.controlDown()) modifiers.add("Control");
        if (keyboard.metaDown()) modifiers.add("Meta");
        return String.join("+", modifiers);
    }

    private static Text createInfoText(String text) {
        final Text infoText = new Text(text);
        infoText.setTextAlignment(TextAlignment.CENTER);
        infoText.setFill(Color.LIGHTGRAY);
        infoText.setFont(LABEL_FONT);
        return infoText;
    }
}
