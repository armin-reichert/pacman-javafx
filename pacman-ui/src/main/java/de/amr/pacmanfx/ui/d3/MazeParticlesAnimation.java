/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector3f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.EnergizerParticlesAnimation;
import de.amr.pacmanfx.uilib.model3D.world.Energizer3D;
import javafx.geometry.Point3D;
import javafx.scene.paint.PhongMaterial;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static java.util.Objects.requireNonNull;

public class MazeParticlesAnimation implements Disposable {

    private EnergizerParticlesAnimation particlesAnimation;

    public MazeParticlesAnimation(
        AnimationRegistry animationRegistry,
        GameLevel level,
        List<PhongMaterial> ghostMaterials,
        Maze3D maze3D)
    {
        requireNonNull(animationRegistry);
        requireNonNull(level);
        requireNonNull(ghostMaterials);
        requireNonNull(maze3D);

        // The bottom center positions of the swirls where the particles of exploded energizers eventually are displayed
        final List<Vector2f> swirlBaseCenters = Stream.of(CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, ORANGE_GHOST_POKEY)
            .map(level::ghost)
            .map(Ghost::startPosition)
            .map(pos -> pos.plus(HTS, HTS))
            .toList();

        particlesAnimation = new EnergizerParticlesAnimation(
            EnergizerParticlesAnimation.DEFAULT_CONFIG,
            animationRegistry,
            swirlBaseCenters,
            ghostMaterials,
            maze3D.floor(),
            maze3D.particlesGroup());
    }

    @Override
    public void dispose() {
        if (particlesAnimation != null) {
            particlesAnimation.dispose();
            particlesAnimation = null;
        }
    }

    public void startParticlesAnimation() {
        particlesAnimation.playFromStart();
    }

    public void stopParticlesAnimation() {
        particlesAnimation.stop();
    }

    public void createEnergizerExplosion(Energizer3D energizer) {
        final Point3D point = energizer.shape().localToScene(Point3D.ZERO);
        final Vector3f origin = new Vector3f(point.getX(), point.getY(), point.getZ());
        particlesAnimation.addEnergizerExplosion(origin);
    }
}
