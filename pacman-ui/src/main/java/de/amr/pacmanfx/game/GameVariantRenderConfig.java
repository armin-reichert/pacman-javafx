/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.spriteanim.SpriteAnimFacade;
import de.amr.basics.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapColorSchemeImpl;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
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

    SpriteSheet spriteSheet();

    WorldMapColorSchemeImpl colorScheme(WorldMap worldMap, WorldSettings worldSettings);

    GameLevelRenderer createGameLevelRenderer(ActorSpriteAnimController animSystem, Canvas canvas);

    GameScene2D_Renderer createGameSceneRenderer(GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas);

    HeadsUpDisplay_Renderer createHUDRenderer(GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas);

    ActorRenderer createActorRenderer(ActorSpriteAnimController animSystem, Canvas canvas);

    Ghost createAnimatedGhost(ActorSpriteAnimController animController, SpriteAnimContainer container, GhostPersonality personality);

    SpriteAnimFacade createGhostAnimations(SpriteAnimContainer container, GhostPersonality personality);

    SpriteAnimFacade createPacAnimations(SpriteAnimContainer container);

    Image killedGhostPointsImage(int killedGhostIndex);

    Image bonusSymbolImage(int symbolCode);

    Image bonusValueImage(int symbolCode);
}

