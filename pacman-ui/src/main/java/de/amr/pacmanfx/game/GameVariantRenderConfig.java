/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.spriteanim.SpriteAnimationAccessor;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

public interface GameVariantRenderConfig {

    AssetMap assets();

    default void addAssets() {}

    SpriteSheet<?> spriteSheet();

    WorldMapColorScheme colorScheme(WorldMap worldMap, WorldSettings worldSettings);

    GameLevelRenderer createGameLevelRenderer(Canvas canvas);

    GameScene2D_Renderer createGameSceneRenderer(AbstractGameScene2D gameScene2D, Canvas canvas);

    HeadsUpDisplay_Renderer createHUDRenderer(AbstractGameScene2D gameScene2D, Canvas canvas);

    ActorRenderer createActorRenderer(Canvas canvas);

    Ghost createAnimatedGhost(SpriteAnimationContainer container, byte personality);

    SpriteAnimationAccessor createGhostAnimations(SpriteAnimationContainer container, byte personality);

    SpriteAnimationAccessor createPacAnimations(SpriteAnimationContainer container);

    Image killedGhostPointsImage(int killedGhostIndex);

    Image bonusSymbolImage(int symbolCode);

    Image bonusValueImage(int symbolCode);
}

