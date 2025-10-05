/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import javafx.scene.paint.*;

/**
 * @see <a href="https://www.eggradients.com/">Eggradients</a>
 */
public interface Gradients {

    enum Axis {
        /** Left to Right */
        HORIZONTAL(1, 0),
        /** Top to Bottom */
        VERTICAL(0, 1),
        /** Top-Left to Bottom-Right */
        DIAGONAL(1, 1);

        public final int endX() { return endX; }

        public final int endY() { return endY; };

        private final byte endX;
        private final byte endY;

        Axis(int endX, int endY) {
            this.endX = (byte) endX;
            this.endY = (byte) endY;
        }
    }

    static Paint createGradient(String startColorCode, String stopColorCode, Axis axis) {
        return new LinearGradient(
            0, 0, axis.endX(), axis.endY(),
            true, // proportional coordinates
            CycleMethod.NO_CYCLE,
            new Stop(0, Color.web(startColorCode)), new Stop(1, Color.web(stopColorCode))
        );
    }

    Paint BLUE_BELL_DREAMS = createGradient("#6495ed", "#7c9ec3", Axis.HORIZONTAL);
    Paint FINDING_NEMO     = createGradient("#0047ab", "#1ca9c9", Axis.HORIZONTAL);
    Paint MOON_SPOT        = createGradient("#8c92ac", "#f5f5f5", Axis.HORIZONTAL);
    Paint PRINCE_TO_KING   = createGradient("#4169e1", "#89CFF0", Axis.HORIZONTAL);
    Paint STARRY_NIGHT     = createGradient("#003366", "#0f52ba", Axis.HORIZONTAL);
    Paint TOP_GUN_FLYOVER  = createGradient("#000080", "#00bfff", Axis.HORIZONTAL);
}