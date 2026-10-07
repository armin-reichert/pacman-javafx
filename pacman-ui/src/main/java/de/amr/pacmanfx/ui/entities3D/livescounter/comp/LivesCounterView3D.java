/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.livescounter.comp;


import de.amr.basics.ui.assets.DisposableGraphicsObject;
import de.amr.pacmanfx.ui.gamescene.d3.animation.NodePositionTracker;
import javafx.beans.property.*;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

import java.util.ArrayList;
import java.util.List;

public class LivesCounterView3D implements DisposableGraphicsObject {

    private final ObjectProperty<Color> pillarColor = new SimpleObjectProperty<>(Color.grayRgb(200));
    private final ObjectProperty<PhongMaterial> pillarMaterial = new SimpleObjectProperty<>(new PhongMaterial());
    private final DoubleProperty pillarHeight = new SimpleDoubleProperty(8);

    private final Group root = new Group();

    private final DoubleProperty plateThickness = new SimpleDoubleProperty(1);
    private final DoubleProperty plateRadius = new SimpleDoubleProperty(6);
    private final ObjectProperty<Color> plateColor = new SimpleObjectProperty<>(Color.grayRgb(100));
    private final ObjectProperty<PhongMaterial> plateMaterial = new SimpleObjectProperty<>(new PhongMaterial());

    private final IntegerProperty livesCount = new SimpleIntegerProperty(0);
    private final List<NodePositionTracker> trackers = new ArrayList<>();

    public LivesCounterView3D() {}

    public Color getPillarColor() {
        return pillarColor.get();
    }

    public ObjectProperty<Color> pillarColorProperty() {
        return pillarColor;
    }

    public void setPillarColor(Color pillarColor) {
        this.pillarColor.set(pillarColor);
    }

    public PhongMaterial getPillarMaterial() {
        return pillarMaterial.get();
    }

    public ObjectProperty<PhongMaterial> pillarMaterialProperty() {
        return pillarMaterial;
    }

    public void setPillarMaterial(PhongMaterial pillarMaterial) {
        this.pillarMaterial.set(pillarMaterial);
    }

    public double getPillarHeight() {
        return pillarHeight.get();
    }

    public DoubleProperty pillarHeightProperty() {
        return pillarHeight;
    }

    public void setPillarHeight(double pillarHeight) {
        this.pillarHeight.set(pillarHeight);
    }

    public double getPlateThickness() {
        return plateThickness.get();
    }

    public DoubleProperty plateThicknessProperty() {
        return plateThickness;
    }

    public void setPlateThickness(double plateThickness) {
        this.plateThickness.set(plateThickness);
    }

    public double getPlateRadius() {
        return plateRadius.get();
    }

    public DoubleProperty plateRadiusProperty() {
        return plateRadius;
    }

    public void setPlateRadius(double plateRadius) {
        this.plateRadius.set(plateRadius);
    }

    public Color getPlateColor() {
        return plateColor.get();
    }

    public ObjectProperty<Color> plateColorProperty() {
        return plateColor;
    }

    public void setPlateColor(Color plateColor) {
        this.plateColor.set(plateColor);
    }

    public PhongMaterial getPlateMaterial() {
        return plateMaterial.get();
    }

    public ObjectProperty<PhongMaterial> plateMaterialProperty() {
        return plateMaterial;
    }

    public void setPlateMaterial(PhongMaterial plateMaterial) {
        this.plateMaterial.set(plateMaterial);
    }

    public int getLivesCount() {
        return livesCount.get();
    }

    public void setLivesCount(int livesCount) {
        this.livesCount.set(livesCount);
    }

    public Group root() {
        return root;
    }

    public IntegerProperty livesCountProperty() {
        return livesCount;
    }

    public List<NodePositionTracker> trackers() {
        return trackers;
    }

    @Override
    public void dispose() {
//        stopTracking();
        livesCount.unbind();
        pillarHeight.unbind();
        pillarMaterial.unbind();
        pillarColor.unbind();
        plateColor.unbind();
        plateThickness.unbind();
        plateRadius.unbind();
        plateMaterial.unbind();

        cleanupGroup(root, true);
    }
}
