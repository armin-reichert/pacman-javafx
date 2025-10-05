/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.lib.Vector2i;
import javafx.scene.paint.*;

/**
 * @see <a href="https://www.eggradients.com/">Eggradients</a>
 */
public interface Gradients {

    enum Axis {
        /** Left to Right */
        HORIZONTAL,
        /** Top to Bottom */
        VERTICAL,
        /** Top-Left to Bottom-Right */
        DIAGONAL;

        public Vector2i start() {
            return Vector2i.ZERO;
        }

        public Vector2i end() {
            return switch (this) {
                case HORIZONTAL -> Vector2i.of(1, 0);
                case VERTICAL   -> Vector2i.of(0, 1);
                case DIAGONAL   -> Vector2i.of(1, 1);
            };
        }
    }

    static Paint createGradient(String startColorCode, String stopColorCode, Axis axis) {
        return new LinearGradient(
            axis.start().x(), axis.end().y(), axis.end().x(), axis.end().y(),
            true, // proportional coordinates
            CycleMethod.NO_CYCLE,
            new Stop(0, Color.web(startColorCode)), new Stop(1, Color.web(stopColorCode))
        );
    }

    Paint BLUE_BELL_DREAMS = createGradient("#6495ed", "#7c9ec3", Axis.HORIZONTAL);
    Paint FINDING_NEMO = createGradient("#0047ab", "#1ca9c9", Axis.HORIZONTAL);
    Paint MOON_SPOT = createGradient("#8c92ac", "#f5f5f5", Axis.HORIZONTAL);
    Paint PRINCE_TO_KING = createGradient("#4169e1", "#89CFF0", Axis.HORIZONTAL);
    Paint STARRY_NIGHT = createGradient("#003366", "#0f52ba", Axis.HORIZONTAL);
    Paint TOP_GUN_FLYOVER = createGradient("#000080", "#00bfff", Axis.HORIZONTAL);
}
