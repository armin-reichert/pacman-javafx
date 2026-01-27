/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.rendering;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import static de.amr.pacmanfx.lib.math.RandomNumberSupport.randomInt;

/**
 * @see <a href="https://www.eggradients.com/">Eggradients</a>
 */
public interface Gradients {

    enum Samples {
        BLUE_BELL_DREAMS("#6495ed", "#7c9ec3", Axis.HORIZONTAL),
        CLOUD_TECH      ("#1e90ff", "#FFFFFF", Axis.HORIZONTAL),
        FINDING_NEMO    ("#0047ab", "#1ca9c9", Axis.HORIZONTAL),
        MOON_SPOT       ("#8c92ac", "#f5f5f5", Axis.HORIZONTAL),
        PRINCE_TO_KING  ("#4169e1", "#89CFF0", Axis.HORIZONTAL),
        STARRY_NIGHT    ("#003366", "#0f52ba", Axis.HORIZONTAL),
        TOP_GUN_FLYOVER ("#000080", "#00bfff", Axis.HORIZONTAL);

        private final LinearGradient gradient;

        public LinearGradient gradient() {
            return gradient;
        }

        Samples(String startColorCode, String endColorCode, Axis axis) {
            gradient = createGradient(startColorCode, endColorCode, axis);
        }

        public static LinearGradient random() {
            return values()[randomInt(0, values().length)].gradient();
        }
    }

    enum Axis {
        /** Left to Right */
        HORIZONTAL(1, 0),
        /** Top to Bottom */
        VERTICAL(0, 1),
        /** Top-Left to Bottom-Right */
        DIAGONAL(1, 1);

        public final int endX() { return endX; }

        public final int endY() { return endY; }

        private final byte endX;
        private final byte endY;

        Axis(int endX, int endY) {
            this.endX = (byte) endX;
            this.endY = (byte) endY;
        }
    }

    static LinearGradient createGradient(String startColorCode, String stopColorCode, Axis axis) {
        return new LinearGradient(
            0, 0, axis.endX(), axis.endY(),
            true, // proportional coordinates
            CycleMethod.NO_CYCLE,
            new Stop(0, Color.valueOf(startColorCode)), new Stop(1, Color.valueOf(stopColorCode))
        );
    }
}