/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import javafx.scene.text.Text;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class InfoText extends Text {

    public static final String NO_INFO = "n/a";

    private final Supplier<?> fnSupplyText;

    public InfoText(Supplier<?> fnSupplyText) {
        this.fnSupplyText = requireNonNull(fnSupplyText);
    }

    public void update() {
        setText(String.valueOf(fnSupplyText.get()));
    }
}