/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.livescounter;

import de.amr.basics.ui.assets.DisposableGraphicsObject;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.ui.gamescene.view3d.animation.NodePositionTracker;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

import java.util.ArrayList;
import java.util.List;

public class LivesCounterView3D implements DisposableGraphicsObject {

    private double pillarHeight;

    private PhongMaterial pillarMaterial;

    private double plateThickness;

    private double plateRadius;

    private PhongMaterial plateMaterial;

    private final List<NodePositionTracker> trackers = new ArrayList<>();

    private final Group root = new Group();

    private final List<Node> counterShapes = new ArrayList<>();

    public LivesCounterView3D() {
        pillarHeight = 8;
        pillarMaterial = Ufx.coloredPhongMaterial(Color.grayRgb(200));

        plateRadius = 6;
        plateThickness = 1;
        plateMaterial = Ufx.coloredPhongMaterial(Color.grayRgb(100));
    }

    public List<Node> counterShapes() {
        return counterShapes;
    }

    public PhongMaterial pillarMaterial() {
        return pillarMaterial;
    }

    public void setPillarMaterial(PhongMaterial material) {
        this.pillarMaterial = material;
    }

    public double pillarHeight() {
        return pillarHeight;
    }

    public void setPillarHeight(double height) {
        this.pillarHeight = height;
    }

    public double plateThickness() {
        return plateThickness;
    }

    public void setPlateThickness(double thickness) {
        this.plateThickness = thickness;
    }

    public double plateRadius() {
        return plateRadius;
    }

    public void setPlateRadius(double radius) {
        this.plateRadius = radius;
    }

    public PhongMaterial plateMaterial() {
        return plateMaterial;
    }

    public void setPlateMaterial(PhongMaterial material) {
        this.plateMaterial = material;
    }

    public Group root() {
        return root;
    }

    public List<NodePositionTracker> trackers() {
        return trackers;
    }

    @Override
    public void dispose() {
//        stopTracking();
        cleanupGroup(root, true);
    }
}
