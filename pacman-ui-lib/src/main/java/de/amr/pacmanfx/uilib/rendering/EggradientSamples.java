/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.uilib.Ufx;
import javafx.scene.layout.Background;
import javafx.scene.paint.LinearGradient;

import java.util.stream.Stream;

import static de.amr.basics.math.RandomNumberSupport.randomInt;

/**
 * Samples taken from <a href="https://www.eggradients.com/">Eggradients website.</a>.
 */
public enum EggradientSamples {

    BLUE_BELL_DREAMS(
        "#6495ed", "#7c9ec3", GradientAxis.HORIZONTAL),

    CLOUD_TECH(
        "#1e90ff", "#FFFFFF", GradientAxis.HORIZONTAL),

    FINDING_NEMO(
        "#0047ab", "#1ca9c9", GradientAxis.HORIZONTAL),

    MOON_SPOT(
        "#8c92ac", "#f5f5f5", GradientAxis.HORIZONTAL),

    PRINCE_TO_KING(
        "#4169e1", "#89CFF0", GradientAxis.HORIZONTAL),

    STARRY_NIGHT(
        "#003366", "#0f52ba", GradientAxis.HORIZONTAL),

    TOP_GUN_FLYOVER(
        "#000080", "#00bfff", GradientAxis.HORIZONTAL);

    private final LinearGradient gradient;

    public LinearGradient gradient() {
        return gradient;
    }

    EggradientSamples(String start, String end, GradientAxis axis) {
        gradient = Ufx.createGradient(start, end, axis);
    }

    public static Background[] backgrounds() {
        return Stream.of(values()).map(EggradientSamples::gradient).map(Background::fill).toArray(Background[]::new);
    }

    public static LinearGradient random() {
        return values()[randomInt(0, values().length)].gradient();
    }
}
