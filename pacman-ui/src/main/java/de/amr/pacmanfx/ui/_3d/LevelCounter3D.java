/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.model.LevelCounter;
import de.amr.pacmanfx.ui.GameUI_Config;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.List;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public class LevelCounter3D extends Group {

    private final ManagedAnimation spinningAnimation;

    public LevelCounter3D(GameUI_Config config, AnimationManager animationManager, LevelCounter levelCounter) {
        requireNonNull(animationManager);
        List<Byte> symbols = levelCounter.symbols();
        for (int i = 0; i < symbols.size(); ++i) {
            var material = new PhongMaterial(Color.WHITE);
            material.setDiffuseMap(config.bonusSymbolImage(symbols.get(i)));
            Box cube = new Box(TS, TS, TS);
            cube.setMaterial(material);
            cube.setTranslateX(-i * 16);
            cube.setTranslateZ(-HTS);
            getChildren().add(cube);
        }

        spinningAnimation = new ManagedAnimation(animationManager, "LevelCounter_Spinning") {
            @Override
            protected Animation createAnimation() {
                var cubesAnimation = new ParallelTransition();
                for (int i = 0; i < getChildren().size(); ++i) {
                    Node cube = getChildren().get(i);
                    var spinning = new RotateTransition(Duration.seconds(6), cube);
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
}