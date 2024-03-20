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
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

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
        root.getChildren().clear();
        for (byte symbol : game.levelCounter()) {
            var sprite = switch (game.variant()) {
                case MS_PACMAN -> ((MsPacManGameSpriteSheet) spriteSheet).bonusSymbolSprite(symbol);
                case    PACMAN -> ((PacManGameSpriteSheet)   spriteSheet).bonusSymbolSprite(symbol);
            };
            int count = root.getChildren().size();
            var entry = createEntry(-count * 2 * TS, spriteSheet.subImage(sprite), count % 2 == 0 ? 1 : -1);
            root.getChildren().add(entry);
        }
    }

    private Box createEntry(double x, Image texture, double rate) {
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
        return cube;
    }
}