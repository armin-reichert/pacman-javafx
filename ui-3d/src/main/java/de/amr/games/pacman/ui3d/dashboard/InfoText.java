/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.dashboard;

import javafx.scene.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Text field whose text is conditionally computed.
 *
 * @author Armin Reichert
 */
public class InfoText extends Text {

    public static final String NO_INFO = "n/a";

    private BooleanSupplier fnAvailable = () -> true;
    private Supplier<?> fnText = () -> "Value";

    public InfoText(String text) {
        this(() -> text);
    }

    public InfoText(Supplier<?> fnText) {
        this.fnText = fnText;
    }

    public InfoText available(BooleanSupplier fnEvaluate) {
        this.fnAvailable = fnEvaluate;
        return this;
    }

    public void update() {
        if (fnAvailable.getAsBoolean()) {
            setText(String.valueOf(fnText.get()));
        } else {
            setText(InfoText.NO_INFO);
        }
    }
}