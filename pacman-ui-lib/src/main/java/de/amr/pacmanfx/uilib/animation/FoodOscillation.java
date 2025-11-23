/*
MIT License

Copyright (c) 2021-2026 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.math.Vector2f;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.scene.Group;
import javafx.util.Duration;

import static de.amr.pacmanfx.Globals.HTS;

/**
 * Oszillation animation for pellets (unused).
 */
public class FoodOscillation extends Transition {

    private static final Vector2f CENTER = Vector2f.of(28 * HTS, 36 * HTS);

    private final Group foodGroup;

    public FoodOscillation(Group foodGroup) {
        this.foodGroup = foodGroup;
        setCycleDuration(Duration.seconds(0.6));
        setCycleCount(INDEFINITE);
        setAutoReverse(true);
        setInterpolator(Interpolator.LINEAR);
    }

    @Override
    protected void interpolate(double t) {
        for (var node : foodGroup.getChildren()) {
            var position2D = new Vector2f((float) node.getTranslateX(), (float) node.getTranslateY());
            var centerDistance = position2D.euclideanDist(CENTER);
            double dz = 2 * Math.sin(2 * centerDistance) * t;
            node.setTranslateZ(-4 + dz);
        }
    }
}