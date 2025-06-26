/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.model.LevelCounter;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
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
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.PacManGames.theUI;
import static java.util.Objects.requireNonNull;

public class LevelCounter3D extends Group {

    private final AnimationManager animationManager;
    private Animation spinningAnimation;

    public LevelCounter3D(AnimationManager animationManager, LevelCounter levelCounter) {
        this.animationManager = requireNonNull(animationManager);
        setTranslateZ(-6);
        int n = 0;
        for (byte symbol : levelCounter.symbols()) {
            Image texture = theUI().configuration().createBonusSymbolImage(symbol);
            var material = new PhongMaterial(Color.WHITE);
            material.setDiffuseMap(texture);
            Box cube = new Box(TS, TS, TS);
            cube.setMaterial(material);
            cube.setTranslateX(-n * 16);
            cube.setTranslateZ(-HTS);
            getChildren().add(cube);
            n += 1;
        }
    }

    private Animation createSpinningAnimation() {
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

    public void playSpinningAnimation() {
        if (spinningAnimation == null) {
            spinningAnimation = createSpinningAnimation();
            animationManager.register("LevelCounter_Spinning", spinningAnimation);
        }
        spinningAnimation.playFromStart();
    }
}