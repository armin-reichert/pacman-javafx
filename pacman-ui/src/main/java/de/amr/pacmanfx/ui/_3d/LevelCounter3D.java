/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
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

    private final PreferencesManager prefs;
    private final UIConfig uiConfig;
    private final AnimationRegistry animationRegistry;
    private ManagedAnimation spinningAnimation;

    public LevelCounter3D(AnimationRegistry animationRegistry, UIConfig uiConfig, PreferencesManager prefs) {
        this.animationRegistry = requireNonNull(animationRegistry);
        this.uiConfig = requireNonNull(uiConfig);
        this.prefs = requireNonNull(prefs);
    }

    public void rebuild(GameLevel level) {
        final Game game = level.game();
        final float cubeSize = PlayScene3D.LEVEL_COUNTER_SYMBOL_SIZE;
        getChildren().clear();
        for (int i = 0; i < game.levelCounterSymbols().size(); ++i) {
            final Byte symbol = game.levelCounterSymbols().get(i);
            final Image symbolImage = uiConfig.bonusSymbolImage(symbol);
            final var texture = new PhongMaterial(Color.WHITE);
            texture.setDiffuseMap(symbolImage);
            final var cube = new Box(cubeSize, cubeSize, cubeSize);
            cube.setMaterial(texture);
            cube.setTranslateX(-i * 16); // arranged from right to left
            cube.setTranslateY(0);
            cube.setTranslateZ(-HTS);
            getChildren().add(cube);
        }

        if (spinningAnimation != null) {
            spinningAnimation.stop();
            spinningAnimation.dispose();
        }
        spinningAnimation = new ManagedAnimation(animationRegistry, "LevelCounter_Spinning");
        spinningAnimation.setFactory(() -> {
            final var cubesAnimation = new ParallelTransition();
            for (int i = 0; i < getChildren().size(); ++i) {
                final Node cube = getChildren().get(i);
                final var spinning = new RotateTransition(Duration.seconds(6), cube);
                spinning.setCycleCount(Animation.INDEFINITE);
                spinning.setInterpolator(Interpolator.LINEAR);
                spinning.setAxis(Rotate.X_AXIS);
                spinning.setByAngle(i % 2 == 0 ? 360 : -360); // alternate spinning direction
                cubesAnimation.getChildren().add(spinning);
            }
            return cubesAnimation;
        });
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
            if (child instanceof Box cube) {
                cube.setMaterial(null);
            }
        }
        getChildren().clear();
    }
}