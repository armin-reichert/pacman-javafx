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

    enum Progress {
        LEFT_TO_RIGHT, TOP_TO_BOTTOM, TOP_LEFT_TO_BOTTOM_RIGHT;
    }

    static Paint createGradient(String startColorCode, String stopColorCode, Progress progress) {
        Vector2i start = switch (progress) {
            case LEFT_TO_RIGHT, TOP_TO_BOTTOM, TOP_LEFT_TO_BOTTOM_RIGHT -> Vector2i.ZERO;
        };
        Vector2i stop = switch (progress) {
            case LEFT_TO_RIGHT -> Vector2i.of(1, 0);
            case TOP_TO_BOTTOM -> Vector2i.of(0, 1);
            case TOP_LEFT_TO_BOTTOM_RIGHT -> Vector2i.of(1, 1);
        };
        Stop[] stops = new Stop[] {
            new Stop(0, Color.web(startColorCode)),
            new Stop(1, Color.web(stopColorCode))
        };
        return new LinearGradient(
            start.x(), start.y(), stop.x(), stop.y(),
            true, // proportional coordinates
            CycleMethod.NO_CYCLE,
            stops
        );
    }

    Paint BLUE_BELL_DREAMS = createGradient("#6495ed", "#7c9ec3", Progress.LEFT_TO_RIGHT);
    Paint FINDING_NEMO = createGradient("#0047ab", "#1ca9c9", Progress.LEFT_TO_RIGHT);
    Paint PRINCE_TO_KING = createGradient("#4169e1", "#89CFF0", Progress.LEFT_TO_RIGHT);
    Paint STARRY_NIGHT = createGradient("#003366", "#0f52ba", Progress.LEFT_TO_RIGHT);
    Paint TOP_GUN_FLYOVER = createGradient("#000080", "#00bfff", Progress.LEFT_TO_RIGHT);
}
