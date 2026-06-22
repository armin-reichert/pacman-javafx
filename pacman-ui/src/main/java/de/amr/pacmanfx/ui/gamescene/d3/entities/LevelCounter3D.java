/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d3.entities;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelEntity;
import de.amr.pacmanfx.ui.config.LevelCounterConfig3D;
import de.amr.pacmanfx.ui.config.GameUIConfig;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
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

import java.util.List;

import static de.amr.pacmanfx.model.world.WorldMap.HTS;
import static java.util.Objects.requireNonNull;

public class LevelCounter3D extends Group implements GameLevelEntity, DisposableGraphicsObject {

    public enum AnimationID implements Identifier {
        LEVEL_COUNTER_SPINNING
    }

    private final AnimationRegistry animations;
    private final GameUIConfig uiConfig;

    public LevelCounter3D(AnimationRegistry animations, GameUIConfig uiConfig) {
        this.animations = requireNonNull(animations);
        this.uiConfig = requireNonNull(uiConfig);
    }

    @Override
    public void init(GameContext gameContext, GameLevel level) {
        final LevelCounterConfig3D config = uiConfig.worldConfig().levelCounter();
        final float cubeSize = config.symbolSize();
        final List<Integer> symbolCodes = level.game().levelCounter().symbolCodes();
        getChildren().clear();
        for (int i = 0; i < symbolCodes.size(); ++i) {
            final Integer symbolCode = symbolCodes.get(i);
            final Image symbolImage = uiConfig.bonusSymbolImage(symbolCode);
            final var texture = new PhongMaterial(Color.WHITE);
            texture.setDiffuseMap(symbolImage);
            final var cube = new Box(cubeSize, cubeSize, cubeSize);
            cube.setMaterial(texture);
            cube.setTranslateX(-i * 16); // arranged from right to left
            cube.setTranslateY(0);
            cube.setTranslateZ(-HTS);
            getChildren().add(cube);
        }

        animations.optAnimation(AnimationID.LEVEL_COUNTER_SPINNING).ifPresent(ManagedAnimation::dispose);
        final var spinningAnimation = new ManagedAnimation("Level Counter Spinning");
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
        animations.register(AnimationID.LEVEL_COUNTER_SPINNING, spinningAnimation);
        spinningAnimation.playFromStart();
    }

    @Override
    public void update(GameContext gameContext, GameLevel level) {}

    @Override
    public void dispose() {
        animations.optAnimation(AnimationID.LEVEL_COUNTER_SPINNING).ifPresent(ManagedAnimation::dispose);
        cleanupGroup(this, true);
    }
}