/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.api;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.AnimationManager;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import de.amr.pacmanfx.uilib.rendering.ActorSpriteRenderer;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import de.amr.pacmanfx.uilib.rendering.HUDRenderer;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

public interface GameUI_Config extends GameScene_Config, Disposable {

    GameUI theUI();

    AssetStorage assets();

    void loadAssets();

    SpriteSheet<?> spriteSheet();

    Image bonusSymbolImage(byte symbol);

    Image bonusValueImage(byte symbol);

    WorldMapColorScheme colorScheme(WorldMap worldMap);

    GameLevelRenderer createGameLevelRenderer(Canvas canvas);

    HUDRenderer createHUDRenderer(Canvas canvas);

    ActorSpriteRenderer createActorSpriteRenderer(Canvas canvas);

    AnimationManager createGhostAnimations(Ghost ghost);

    Node createLivesCounterShape3D();

    AnimationManager createPacAnimations(Pac pac);

    PacBase3D createPac3D(AnimationRegistry animationMgr, GameLevel gameLevel, Pac pac);

    default boolean hasGameCanvasRoundedBorder() { return true; }

    Image killedGhostPointsImage(Ghost ghost, int killedIndex);

    SoundManager soundManager();
}