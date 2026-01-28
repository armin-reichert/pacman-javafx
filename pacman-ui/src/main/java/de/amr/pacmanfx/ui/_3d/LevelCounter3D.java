/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.GlobalPreferencesManager;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.RegisteredAnimation;
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

import static de.amr.pacmanfx.Globals.HTS;
import static java.util.Objects.requireNonNull;

public class LevelCounter3D extends Group implements Disposable {

    private final UIConfig uiConfig;
    private final AnimationRegistry animationRegistry;
    private RegisteredAnimation spinningAnimation;

    public LevelCounter3D(AnimationRegistry animationRegistry, UIConfig uiConfig) {
        this.animationRegistry = requireNonNull(animationRegistry);
        this.uiConfig = requireNonNull(uiConfig);
    }

    public void update(Game game) {
        getChildren().clear();
        float cubeSize = GlobalPreferencesManager.instance().getFloat("3d.level_counter.symbol_size");
        for (int i = 0; i < game.levelCounterSymbols().size(); ++i) {
            Byte symbol = game.levelCounterSymbols().get(i);
            Image symbolImage = uiConfig.bonusSymbolImage(symbol);
            var material = new PhongMaterial(Color.WHITE);
            material.setDiffuseMap(symbolImage);
            var cube = new Box(cubeSize, cubeSize, cubeSize);
            cube.setMaterial(material);
            cube.setTranslateX(-i * 16);
            cube.setTranslateY(0);
            cube.setTranslateZ(-HTS);
            getChildren().add(cube);
        }

        if (spinningAnimation != null) {
            spinningAnimation.stop();
            spinningAnimation.dispose();
        }
        spinningAnimation = new RegisteredAnimation(animationRegistry, "LevelCounter_Spinning") {
            @Override
            protected Animation createAnimationFX() {
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
        spinningAnimation.playFromStart();
    }

    @Override
    public void dispose() {
        if (spinningAnimation != null) {
            spinningAnimation.stop();
            spinningAnimation.dispose();
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