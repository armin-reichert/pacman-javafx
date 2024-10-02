/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.sound.GameSounds;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.IntStream;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui3d.PacManGames3dApp.PY_3D_DRAW_MODE;

public interface Factory3D {

    static Pac3D createPac3D(GameVariant variant, AssetStorage assets, GameSounds sounds, Pac pac, double size) {
        String prefix = GameAssets2D.assetPrefix(variant) + ".";
        Pac3D pac3D = switch (variant) {
            case MS_PACMAN, MS_PACMAN_TENGEN -> new MsPacMan3D(variant, pac, size, assets, sounds);
            case PACMAN, PACMAN_XXL          -> new PacMan3D(variant, pac, size, assets, sounds);
        };
        pac3D.shape3D().light().setColor(assets.color(prefix + "pac.color.head").desaturate());
        pac3D.shape3D().drawModeProperty().bind(PY_3D_DRAW_MODE);
        return pac3D;
    }

    static MutableGhost3D createMutableGhost3D(GameVariant variant, AssetStorage assets, Ghost ghost, double size) {
        var ghost3D = new MutableGhost3D(variant, assets.get("model3D.ghost"), assets, ghost, size);
        ghost3D.drawModePy.bind(PY_3D_DRAW_MODE);
        return ghost3D;
    }

    static LivesCounter3D createLivesCounter3D(GameVariant variant, AssetStorage assets, int maxShapes, double shapeSize, boolean hasCredit) {
        Node[] shapes = IntStream.range(0, maxShapes).mapToObj(i -> createLivesCounterShape(variant, assets, shapeSize)).toArray(Node[]::new);
        var counter3D = new LivesCounter3D(shapes, 10);
        counter3D.setTranslateX(2 * TS);
        counter3D.setTranslateY(2 * TS);
        counter3D.setVisible(hasCredit);
        counter3D.drawModePy.bind(PY_3D_DRAW_MODE);
        counter3D.light().colorProperty().set(Color.CORNFLOWERBLUE);
        counter3D.light().setLightOn(hasCredit);
        return counter3D;
    }

    private static Node createLivesCounterShape(GameVariant variant, AssetStorage assets, double size) {
        String assetPrefix = GameAssets2D.assetPrefix(variant) + ".";
        return switch (variant) {
            case MS_PACMAN, MS_PACMAN_TENGEN -> new Group(
                PacModel3D.createPacShape(
                    assets.get("model3D.pacman"), size,
                    assets.color(assetPrefix + "pac.color.head"),
                    assets.color(assetPrefix + "pac.color.eyes"),
                    assets.color(assetPrefix + "pac.color.palate")
                ),
                PacModel3D.createFemaleParts(size,
                    assets.color(assetPrefix + "pac.color.hairbow"),
                    assets.color(assetPrefix + "pac.color.hairbow.pearls"),
                    assets.color(assetPrefix + "pac.color.boobs")
                )
            );
            case PACMAN, PACMAN_XXL ->
                PacModel3D.createPacShape(
                    assets.get("model3D.pacman"), size,
                    assets.color(assetPrefix + "pac.color.head"),
                    assets.color(assetPrefix + "pac.color.eyes"),
                    assets.color(assetPrefix + "pac.color.palate")
                );
        };
    }

    static Node createLevelCounter3D(GameSpriteSheet spriteSheet, List<Byte> symbols, double x, double y) {
        double spacing = 2 * TS;
        var levelCounter3D = new Group();
        levelCounter3D.setTranslateX(x);
        levelCounter3D.setTranslateY(y);
        levelCounter3D.setTranslateZ(-6);
        levelCounter3D.getChildren().clear();
        int n = 0;
        for (byte symbol : symbols) {
            Box cube = new Box(TS, TS, TS);
            cube.setTranslateX(-n * spacing);
            cube.setTranslateZ(-HTS);
            levelCounter3D.getChildren().add(cube);

            var material = new PhongMaterial(Color.WHITE);
            material.setDiffuseMap(spriteSheet.subImage(spriteSheet.bonusSymbolSprite(symbol)));
            cube.setMaterial(material);

            var spinning = new RotateTransition(Duration.seconds(6), cube);
            spinning.setAxis(Rotate.X_AXIS);
            spinning.setCycleCount(Animation.INDEFINITE);
            spinning.setByAngle(360);
            spinning.setRate(n % 2 == 0 ? 1 : -1);
            spinning.setInterpolator(Interpolator.LINEAR);
            spinning.play();

            n += 1;
        }
        return levelCounter3D;
    }

    static Message3D createMessage3D(AssetStorage assets) {
        var message3D = new Message3D("", assets.font("font.arcade", 6), Color.YELLOW, Color.WHITE);
        message3D.setRotation(Rotate.X_AXIS, 90);
        message3D.setVisible(false);
        return message3D;
    }
}