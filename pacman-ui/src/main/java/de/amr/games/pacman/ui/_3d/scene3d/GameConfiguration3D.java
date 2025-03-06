package de.amr.games.pacman.ui._3d.scene3d;

import de.amr.games.pacman.tilemap.rendering.TerrainRenderer3D;
import de.amr.games.pacman.uilib.AssetStorage;
import de.amr.games.pacman.ui.GameConfiguration;
import javafx.scene.Node;

public interface GameConfiguration3D extends GameConfiguration {
    TerrainRenderer3D createTerrainRenderer3D();
    Node createLivesCounterShape(AssetStorage assets);
}
