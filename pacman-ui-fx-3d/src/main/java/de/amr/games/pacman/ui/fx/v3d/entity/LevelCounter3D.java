/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.model.GameModel;
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

import java.util.function.Function;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;

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

    public void populate(GameModel game, SpriteSheet spriteSheet) {
        // that's ugly:
        Function<Byte, Rectangle2D> fnSprite = switch (game.variant()) {
            case MS_PACMAN -> ((MsPacManGameSpriteSheet) spriteSheet)::bonusSymbolSprite;
            case    PACMAN -> ((PacManGameSpriteSheet)   spriteSheet)::bonusSymbolSprite;
        };
        root.getChildren().clear();
        double rate = 1;
        double x = 0;
        for (var image : game.levelCounter().stream().map(fnSprite).map(spriteSheet::subImage).toList()) {
            addSpinningCube(x, image, rate);
            rate = -rate;
            x -= 2 * TS;
        }
    }

    private void addSpinningCube(double x, Image texture, double rate) {
        Box cube = new Box(TS, TS, TS);
        cube.setTranslateX(x);
        cube.setTranslateZ(-HTS);
        var material = new PhongMaterial(Color.WHITE);
        material.setDiffuseMap(texture);
        cube.setMaterial(material);
        var spinning = new RotateTransition(Duration.seconds(6), cube);
        spinning.setAxis(Rotate.X_AXIS);
        spinning.setCycleCount(Animation.INDEFINITE);
        spinning.setByAngle(360);
        spinning.setRate(rate);
        spinning.setInterpolator(Interpolator.LINEAR);
        spinning.play();
        root.getChildren().add(cube);
    }
}