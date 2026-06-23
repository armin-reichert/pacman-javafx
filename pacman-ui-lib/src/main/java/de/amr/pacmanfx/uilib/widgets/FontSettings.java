package de.amr.pacmanfx.uilib.widgets;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public record FontSettings(
    String family,
    double size,
    String weight,
    String posture)
{
    public Font toFont() {
        final FontWeight fontWeight = weight != null ? FontWeight.valueOf(weight) : FontWeight.NORMAL;
        final FontPosture fontPosture = posture != null ? FontPosture.valueOf(posture) : FontPosture.REGULAR;
        return Font.font(family, fontWeight, fontPosture, size);
    }

}

