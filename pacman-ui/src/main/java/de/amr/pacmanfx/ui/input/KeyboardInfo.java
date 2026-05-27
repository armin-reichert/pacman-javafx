package de.amr.pacmanfx.ui.input;

import de.amr.pacmanfx.ui.GameUIConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class KeyboardInfo {
    private final VBox rootPane = new VBox();

    public KeyboardInfo() {
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

        Input.instance().keyboard.addListener(keyboard -> {
            rootPane.getChildren().clear();

            final Text title = new Text("Keyboard State");
            title.setFill(Color.WHITE);
            title.setFont(Font.font("Sans", FontWeight.BOLD, 16));
            rootPane.getChildren().add(title);

            final Text closeHint = new Text("(Alt+K to close)");
            closeHint.setFill(Color.WHITE);
            closeHint.setFont(Font.font("Sans", FontWeight.NORMAL, 16));
            rootPane.getChildren().add(closeHint);

            final VBox vBox = new VBox();
            vBox.setAlignment(Pos.CENTER);
            vBox.setPadding(new Insets(10));
            rootPane.getChildren().add(vBox);

            final Font labelFont = Font.font("Monospace", FontWeight.NORMAL, 16);
            keyboard.pressedKeys().stream().sorted().forEach(keyCode -> {
                final Text stateLabel = new Text();
                stateLabel.setTextAlignment(TextAlignment.CENTER);
                stateLabel.setFill(Color.LIGHTGRAY);
                stateLabel.setFont(labelFont);
                String modText = "";
                if (keyboard.altDown()) modText += "Alt ";
                if (keyboard.shiftDown()) modText += "Shift ";
                if (keyboard.controlDown()) modText += "Control ";
                if (keyboard.metaDown()) modText += "Meta ";
                stateLabel.setText(modText + keyCode.getName());
                vBox.getChildren().add(stateLabel);
            });
        });
    }

    public VBox rootPane() {
        return rootPane;
    }
}
