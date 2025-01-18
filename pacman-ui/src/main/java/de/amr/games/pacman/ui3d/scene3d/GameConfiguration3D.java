package de.amr.games.pacman.ui3d.scene3d;

import de.amr.games.pacman.ui2d.assets.AssetStorage;
import de.amr.games.pacman.ui2d.scene.GameConfiguration;
import de.amr.games.pacman.tilemap.rendering.WorldRenderer3D;
import javafx.scene.Node;

public interface GameConfiguration3D extends GameConfiguration {
    WorldRenderer3D createWorldRenderer();
    Node createLivesCounterShape(AssetStorage assets);
}
