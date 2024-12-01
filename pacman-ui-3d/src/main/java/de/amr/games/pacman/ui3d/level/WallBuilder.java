/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMapPath;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui3d.PacManGames3dApp.PY_3D_DRAW_MODE;

/**
 * @author Armin Reichert
 */
public interface WallBuilder {

    static Node createWall(
        Vector2i tile1, Vector2i tile2,
        double thickness, DoubleProperty wallHeightPy, double coatHeight,
        Property<PhongMaterial> fillMaterialPy, Property<PhongMaterial> strokeMaterialPy)
    {
        if (tile1.y() == tile2.y()) { // horizontal wall
            Vector2i left  = tile1.x() < tile2.x() ? tile1 : tile2;
            Vector2i right = tile1.x() < tile2.x() ? tile2 : tile1;
            Vector2i origin = left.plus(right).scaled(HTS).plus(HTS, HTS);
            int length = right.minus(left).scaled(TS).x();
            return createWall(origin, length + thickness, thickness, wallHeightPy, coatHeight, fillMaterialPy, strokeMaterialPy);
        }
        else if (tile1.x() == tile2.x()) { // vertical wall
            Vector2i top    = tile1.y() < tile2.y() ? tile1 : tile2;
            Vector2i bottom = tile1.y() < tile2.y() ? tile2 : tile1;
            Vector2i origin = top.plus(bottom).scaled(HTS).plus(HTS, HTS);
            int length = bottom.minus(top).scaled(TS).y();
            return createWall(origin, thickness, length, wallHeightPy, coatHeight, fillMaterialPy, strokeMaterialPy);
        }
        throw new IllegalArgumentException(String.format("Cannot build wall between tiles %s and %s", tile1, tile2));
    }

    static Node createWall(
        Vector2i origin, double sizeX, double sizeY, DoubleProperty wallHeightPy, double coatHeight,
        Property<PhongMaterial> fillMaterialPy, Property<PhongMaterial> strokeMaterialPy) {

        var base = new Box(sizeX, sizeY, wallHeightPy.get());
        base.setTranslateX(origin.x());
        base.setTranslateY(origin.y());
        base.translateZProperty().bind(wallHeightPy.multiply(-0.5).add(2*coatHeight));
        base.depthProperty().bind(wallHeightPy);
        base.materialProperty().bind(fillMaterialPy);
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);

        var top = new Box(sizeX, sizeY, coatHeight);
        top.translateXProperty().bind(base.translateXProperty());
        top.translateYProperty().bind(base.translateYProperty());
        top.translateZProperty().bind(wallHeightPy.add(0.5*coatHeight).multiply(-1).add(2*coatHeight));
        top.materialProperty().bind(strokeMaterialPy);
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);

        return new Group(base, top);
    }

    static void createCircularWall(
        Group parent, TileMapPath path,
        DoubleProperty wallHeightPy, double coatHeight,
        Property<PhongMaterial> wallFillMaterialPy, Property<PhongMaterial> wallStrokeMaterialPy) {

        Vector2i center = path.startTile().plus(1, 1).scaled(TS);

        Cylinder base = new Cylinder(HTS, wallHeightPy.get());
        base.setRotationAxis(Rotate.X_AXIS);
        base.setRotate(90);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());
        base.translateZProperty().bind(wallHeightPy.multiply(-0.5).add(2*coatHeight));
        base.heightProperty().bind(wallHeightPy);
        base.materialProperty().bind(wallFillMaterialPy);
        base.drawModeProperty().bind(PY_3D_DRAW_MODE);

        Cylinder top = new Cylinder(HTS, coatHeight);
        top.setRotationAxis(Rotate.X_AXIS);
        top.setRotate(90);
        top.translateXProperty().bind(base.translateXProperty());
        top.translateYProperty().bind(base.translateYProperty());
        top.translateZProperty().bind(wallHeightPy.add(0.5*coatHeight).multiply(-1).add(2*coatHeight));
        top.materialProperty().bind(wallStrokeMaterialPy);
        top.drawModeProperty().bind(PY_3D_DRAW_MODE);

        parent.getChildren().add(new Group(base, top));
    }

    static void buildWallAlongPath(
        Group parent, TileMapPath path,
        DoubleProperty wallHeightPy, double thickness, double coatHeight,
        Property<PhongMaterial> wallFillMaterialPy, Property<PhongMaterial> wallStrokeMaterialPy)
    {
        if (path.size() == 4) {
            createCircularWall(parent, path, wallHeightPy, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
            return;
        }

        Vector2i startTile = path.startTile(), endTile = startTile;
        Vector2i prev = null;
        Node segment;
        for (Vector2i vector : path) {
            if (!vector.equals(prev)) {
                segment = createWall(startTile, endTile, thickness, wallHeightPy, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
                parent.getChildren().add(segment);
                startTile = endTile;
            }
            endTile = endTile.plus(vector);
            prev = vector;
        }
        segment = createWall(startTile, endTile, thickness, wallHeightPy, coatHeight, wallFillMaterialPy, wallStrokeMaterialPy);
        parent.getChildren().add(segment);
    }
}