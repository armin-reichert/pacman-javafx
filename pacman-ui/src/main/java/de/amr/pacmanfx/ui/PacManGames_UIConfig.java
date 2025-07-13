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

import static de.amr.pacmanfx.ui.GameUI.theUI;

public interface PacManGames_UIConfig extends PacManGames_GameSceneConfig, Destroyable {

    String assetNamespace();

    void storeAssets(AssetStorage assetStorage);

    default String toGlobalKey(String localKey) { return assetNamespace() + "." + localKey; }

    /**
     * Stores key-value pair in the namespace of this configuration.
     *
     * @param assetStorage the asset storage
     * @param localKey the local asset key, absolute key is {@code asset_namespace + "." + key}
     * @param value the asset value
     */
    default void storeAssetNS(AssetStorage assetStorage, String localKey, Object value) {
        assetStorage.store(toGlobalKey(localKey), value);
    }

    /**
     * @param assetStorage the asset storage
     * @param localKey the local asset key, absolute key is {@code asset_namespace + "." + key}
     * @return the asset value
     * @param <T> expected asset value type
     */
    default <T> T getAssetNS(AssetStorage assetStorage, String localKey) {
        return assetStorage.get(toGlobalKey(localKey));
    }

    /**
     * @param localKey the local asset key, absolute key is {@code asset_namespace + "." + key}
     * @return the asset value from the global asset storage of the UI
     * @param <T> expected asset value type
     */
    default <T> T getAssetNS(String localKey) {
        return getAssetNS(theUI().theAssets(), localKey);
    }

    Image bonusSymbolImage(byte symbol);
    Image bonusValueImage(byte symbol);
    WorldMapColorScheme colorScheme(WorldMap worldMap);
    GameRenderer createGameRenderer(Canvas canvas);
    ActorAnimationMap createGhostAnimations(Ghost ghost);
    Node createLivesCounterShape3D(Model3DRepository model3DRepository);
    ActorAnimationMap createPacAnimations(Pac pac);
    PacBase3D createPac3D(Model3DRepository model3DRepository, AnimationManager animationMgr, Pac pac);
    default boolean hasGameCanvasRoundedBorder() { return true; }
    Image killedGhostPointsImage(Ghost ghost, int killedIndex);
    SpriteSheet<?> spriteSheet();
    SoundManager soundManager();
}