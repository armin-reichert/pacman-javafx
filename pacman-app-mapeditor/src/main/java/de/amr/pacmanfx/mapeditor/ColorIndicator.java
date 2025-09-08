/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class ColorIndicator extends HBox {
    private final VBox colorBox = new VBox();
    private final Text colorText = new Text("#RGB");

    public ColorIndicator(double width) {
        setMinWidth(width);
        setMaxWidth(width);
        setMinHeight(30);
        setSpacing(10);
        setPadding(new Insets(3));
        setBackground(Background.fill(Color.gray(0.4)));

        colorBox.setMinWidth(30);
        colorBox.setMaxWidth(30);
        colorBox.setMinHeight(30);
        colorBox.setMaxHeight(30);
        colorBox.setBorder(Border.stroke(Color.WHITE));

        colorText.setFont(Font.font("Sans", FontWeight.BOLD, 20));
        colorText.setFill(Color.WHITE);

        getChildren().addAll(colorBox, colorText);
    }

    public void setColor(Color color) {
        colorBox.setBackground(Background.fill(color));
        colorText.setText(color.equals(Color.TRANSPARENT) ? "Transparent" : EditorUtil.formatColorHex(color));
    }
}
