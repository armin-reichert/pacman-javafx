/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.util.SpriteSheet;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Function;

import static de.amr.games.pacman.lib.Globals.*;
import static java.util.Objects.requireNonNull;

/**
 * 3D level counter.
 *
 * @author Armin Reichert
 */
public class LevelCounter3D {

    private final Group root = new Group();

    public Node root() {
        return root;
    }

    public void update(Image[] symbolImages) {
        requireNonNull(symbolImages);
        root.getChildren().clear();
        for (int i = 0; i < symbolImages.length; ++i) {
            var symbolImage = symbolImages[i];
            Box cube = createSpinningCube(TS, symbolImage, isEven(i));
            cube.setTranslateX(-2 * i * TS);
            cube.setTranslateY(0);
            cube.setTranslateZ(-HTS);
            root.getChildren().add(cube);
        }
    }

    public void setRightPosition(double x, double y, double z) {
        root.setTranslateX(x);
        root.setTranslateY(y);
        root.setTranslateZ(z);
    }

    public void updateSprites(List<Byte> levelCounter, SpriteSheet spriteSheet, GameVariant variant) {
        update(sprites(levelCounter, spriteSheet, variant));
    }

    private Box createSpinningCube(double size, Image texture, boolean forward) {
        var material = new PhongMaterial(Color.WHITE);
        material.setDiffuseMap(texture);
        Box cube = new Box(size, size, size);
        cube.setMaterial(material);
        var spinning = new RotateTransition(Duration.seconds(6), cube);
        spinning.setAxis(Rotate.X_AXIS);
        spinning.setCycleCount(Animation.INDEFINITE);
        spinning.setByAngle(360);
        spinning.setRate(forward ? 1 : -1);
        spinning.setInterpolator(Interpolator.LINEAR);
        spinning.play();
        return cube;
    }

    private Image[] sprites(List<Byte> levelCounter, SpriteSheet spriteSheet, GameVariant variant) {
        Function<Byte, Rectangle2D> spriteSupplier = switch (variant) {
            case MS_PACMAN -> ((MsPacManGameSpriteSheet) spriteSheet)::bonusSymbolSprite;
            case PACMAN -> ((PacManGameSpriteSheet) spriteSheet)::bonusSymbolSprite;
        };
        return levelCounter.stream()
            .map(spriteSupplier)
            .map(spriteSheet::subImage)
            .toArray(Image[]::new);
    }
}