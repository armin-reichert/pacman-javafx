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
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.Destroyable;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

public interface PacManGames_UIConfig extends PacManGames_GameSceneConfig, Destroyable {
    void loadAssets(AssetStorage assetStorage);
    void destroy();
    String assetNamespace();
    default void storeInMyNamespace(AssetStorage assetStorage, String key, Object value) {
        store(assetStorage, assetNamespace(), key, value);
    }
    default void store(AssetStorage assetStorage, String namespace, String key, Object value) {
        assetStorage.store(namespace + "." + key, value);
    }
    SpriteSheet<?> spriteSheet();
    GameRenderer createGameRenderer(Canvas canvas);
    ActorAnimationMap createPacAnimations(Pac pac);
    ActorAnimationMap createGhostAnimations(Ghost ghost);
    Node createLivesCounterShape3D(Model3DRepository model3DRepository);
    PacBase3D createPac3D(Model3DRepository model3DRepository, AnimationManager animationMgr, Pac pac);
    Image bonusSymbolImage(byte symbol);
    Image bonusValueImage(byte symbol);
    default boolean hasGameCanvasRoundedBorder() { return true; }
    Image killedGhostPointsImage(Ghost ghost, int killedIndex);
    WorldMapColorScheme worldMapColorScheme(WorldMap worldMap);
    SoundManager soundManager();
}