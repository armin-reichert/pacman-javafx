/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.actors.ActorAnimationMap;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameRenderer;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

public interface PacManGames_UIConfig extends PacManGames_GameSceneConfig {
    void loadAssets(AssetStorage assetStorage);
    void unloadAssets(AssetStorage assetStorage);
    String assetNamespace();
    default void storeLocalAsset(AssetStorage assetStorage, String key, Object value) {
        assetStorage.store(assetNamespace() + "." + key, value);
    }
    ActorAnimationMap createPacAnimations(Pac pac);
    ActorAnimationMap createGhostAnimations(Ghost ghost);
    Node createLivesCounterShape3D(Model3DRepository model3DRepository);
    PacBase3D createPac3D(Model3DRepository model3DRepository, AnimationManager animationMgr, Pac pac);
    Image bonusSymbolImage(byte symbol);
    Image bonusValueImage(byte symbol);
    GameRenderer createGameRenderer(Canvas canvas);
    default boolean hasGameCanvasRoundedBorder() { return true; }
    SpriteSheet<?> spriteSheet();
    WorldMapColorScheme worldMapColorScheme(WorldMap worldMap);
}