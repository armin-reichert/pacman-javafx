package de.amr.games.pacman.ui3d.scene3d;

import de.amr.games.pacman.ui2d.scene.GameConfiguration;
import de.amr.games.pacman.ui3d.level.WorldRenderer3D;

public interface GameConfiguration3D extends GameConfiguration {
    WorldRenderer3D createWorldRenderer();
}
