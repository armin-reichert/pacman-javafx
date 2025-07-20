/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Destroyable;
import de.amr.pacmanfx.model.LevelCounter;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.Globals.HTS;
import static java.util.Objects.requireNonNull;

public class LevelCounter3D extends Group implements Destroyable {

    private final AnimationManager animationManager;
    private ManagedAnimation spinningAnimation;

    public LevelCounter3D(GameUI ui, AnimationManager animationManager, LevelCounter levelCounter) {
        this.animationManager = requireNonNull(animationManager);
        float cubeSize = ui.thePrefs().getFloat("3d.level_counter.symbol_size");
        for (int i = 0; i < levelCounter.symbols().size(); ++i) {
            Image symbolImage = ui.theConfiguration().bonusSymbolImage(levelCounter.symbols().get(i));
            var material = new PhongMaterial(Color.WHITE);
            material.setDiffuseMap(symbolImage);
            var cube = new Box(cubeSize, cubeSize, cubeSize);
            cube.setMaterial(material);
            cube.setTranslateX(-i * 16);
            cube.setTranslateY(0);
            cube.setTranslateZ(-HTS);
            getChildren().add(cube);
        }

        spinningAnimation = new ManagedAnimation(animationManager, "LevelCounter_Spinning") {
            @Override
            protected Animation createAnimation() {
                var cubesAnimation = new ParallelTransition();
                for (int i = 0; i < getChildren().size(); ++i) {
                    Node shape = getChildren().get(i);
                    var spinning = new RotateTransition(Duration.seconds(6), shape);
                    spinning.setCycleCount(Animation.INDEFINITE);
                    spinning.setInterpolator(Interpolator.LINEAR);
                    spinning.setAxis(Rotate.X_AXIS);
                    spinning.setByAngle(i % 2 == 0 ? 360 : -360);
                    cubesAnimation.getChildren().add(spinning);
                }
                return cubesAnimation;
            }
        };
    }

    public ManagedAnimation spinningAnimation() {
        return spinningAnimation;
    }

    @Override
    public void destroy() {
        if (spinningAnimation != null) {
            animationManager.stopAnimation(spinningAnimation);
            animationManager.destroyAnimation(spinningAnimation);
            spinningAnimation = null;
        }
        for (Node child : getChildren()) {
            if (child instanceof Box box) {
                box.setMaterial(null);
            }
        }
        getChildren().clear();
    }
}