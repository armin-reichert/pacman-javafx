/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.Disposable;
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
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import java.util.stream.Stream;

public interface GameUI_Config extends Disposable {

    // scene config
    void createGameScenes(GameUI ui);
    Stream<GameScene> gameScenes();
    boolean gameSceneHasID(GameScene gameScene, String sceneID);
    GameScene selectGameScene(GameContext gameContext);

    GameUI theUI();

    String assetNamespace();

    void storeAssets(AssetStorage assetStorage);

    default String fullAssetKey(String partialKey) { return assetNamespace() + "." + partialKey; }

    /**
     * Stores key-value pair in the namespace of this configuration.
     *
     * @param assetStorage the asset storage
     * @param partialKey the partial asset key, full key is {@code asset_namespace + "." + key}
     * @param value the asset value
     */
    default void storeAssetNS(AssetStorage assetStorage, String partialKey, Object value) {
        assetStorage.store(fullAssetKey(partialKey), value);
    }

    /**
     * @param assetStorage the asset storage
     * @param localKey the partial asset key, full key is {@code asset_namespace + "." + key}
     * @return the asset value
     * @param <T> expected asset value type
     */
    default <T> T getAssetNS(AssetStorage assetStorage, String localKey) {
        return assetStorage.get(fullAssetKey(localKey));
    }

    /**
     * @param partialKey the partial asset key, full key is {@code asset_namespace + "." + key}
     * @return the asset value from the global asset storage of the UI
     * @param <T> expected asset value type
     */
    default <T> T getAssetNS(String partialKey) {
        return getAssetNS(theUI().theAssets(), partialKey);
    }

    Image bonusSymbolImage(byte symbol);
    Image bonusValueImage(byte symbol);
    WorldMapColorScheme colorScheme(WorldMap worldMap);
    GameRenderer createGameRenderer(Canvas canvas);
    ActorAnimationMap createGhostAnimations(Ghost ghost);
    Node createLivesCounterShape3D();
    ActorAnimationMap createPacAnimations(Pac pac);
    PacBase3D createPac3D(AnimationManager animationMgr, Pac pac);
    default boolean hasGameCanvasRoundedBorder() { return true; }
    Image killedGhostPointsImage(Ghost ghost, int killedIndex);
    SpriteSheet<?> spriteSheet();
    SoundManager soundManager();
}