package de.amr.pacmanfx.ui.input;

import de.amr.pacmanfx.ui.GameUIConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

public class KeyboardInfo {

    public static final Font LABEL_FONT = Font.font("Monospace", FontWeight.NORMAL, 16);
    public static final Font TITLE_FONT = Font.font("Sans", FontWeight.BOLD, 16);
    public static final Font CLOSE_HINT_FONT = Font.font("Sans", FontWeight.NORMAL, 16);

    private final VBox rootPane = new VBox();

    public KeyboardInfo(Keyboard keyboard) {
        rootPane.setBackground(Background.fill(Color.TRANSPARENT));
        rootPane.setPrefSize(280, 50);
        rootPane.setMaxSize(280, 200);
        rootPane.setSpacing(3);
        rootPane.setStyle("""
            -fx-border-color: #ccc;
            -fx-border-width: 2;
            -fx-border-radius: 12;
            -fx-background-radius: 12;
            """);
        StackPane.setMargin(rootPane, new Insets(10));
        StackPane.setAlignment(rootPane, Pos.TOP_RIGHT);
        rootPane.visibleProperty().bind(GameUIConstants.PROPERTY_KEYBOARD_MONITOR_VISIBLE);

        final Text title = new Text("Keyboard State");
        title.setFill(Color.WHITE);
        title.setFont(TITLE_FONT);
        rootPane.getChildren().add(title);

        final Text closeHint = new Text("(Alt+K to close)");
        closeHint.setFill(Color.WHITE);
        closeHint.setFont(CLOSE_HINT_FONT);
        rootPane.getChildren().add(closeHint);

        final VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(10));
        rootPane.getChildren().add(vBox);

        keyboard.addStateListener(keyboardState -> {
            vBox.getChildren().clear();
            String modifiers = createModifierString(keyboardState);
            vBox.getChildren().add(createInfoText("[" + modifiers + "]"));
            for (KeyCode key : keyboardState.pressedKeys()) {
                vBox.getChildren().add(createInfoText(modifiers + " " + key));
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
