/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import javafx.scene.text.Text;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Text that is dynamically updated by the dashboard
 */
public class DynamicInfoText extends Text {

    private final Supplier<?> infoSupplier;

    public DynamicInfoText(Supplier<?> infoSupplier) {
        this.infoSupplier = requireNonNull(infoSupplier);
    }

    public void update() {
        setText(String.valueOf(infoSupplier.get()));
    }
}