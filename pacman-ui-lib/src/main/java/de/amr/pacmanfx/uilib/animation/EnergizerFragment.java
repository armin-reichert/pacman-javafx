/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector3f;
import javafx.geometry.Point3D;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Material;
import javafx.scene.shape.Sphere;

import static de.amr.pacmanfx.lib.math.RandomNumberSupport.randomFloat;

public class EnergizerFragment extends Sphere implements Disposable {

    // TODO: Better use smaller meshes than spheres?
    private static final short SPHERE_DIVISIONS = 8;
    private static final Vector3f GRAVITY = new Vector3f(0, 0, 0.18f);

    public boolean moving_home = false;
    public boolean part_of_swirl = false;
    public byte ghost_personality = -1;
    public Point3D houseTargetPosition;
    public Vector3f velocity;

    public EnergizerFragment(double radius, Material material, Vector3f velocity, Point3D origin) {
        super(radius, SPHERE_DIVISIONS);
        this.velocity = velocity;
        setMaterial(material);
        setTranslateX(origin.getX());
        setTranslateY(origin.getY());
        setTranslateZ(origin.getZ());
        setEffect(randomGlow());
    }

    private Glow randomGlow() {
        return new Glow(0.5 + randomFloat(0, 0.5f));
    }

    @Override
    public void dispose() {
        setMaterial(null);
    }

    public void fly() {
        move();
        velocity = velocity.add(GRAVITY);
    }

    public void move() {
        setTranslateX(getTranslateX() + velocity.x());
        setTranslateY(getTranslateY() + velocity.y());
        setTranslateZ(getTranslateZ() + velocity.z());
    }
}
