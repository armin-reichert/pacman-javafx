/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimFacade;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.HUD_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.HUD_Style;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.GameSceneRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

public interface GameVariantRenderConfig {

    AssetMap assets();

    default void addAssets() {}

    SpriteSheet spriteSheet();

    GenericWorldMapColorScheme colorScheme(WorldMap worldMap, WorldSettings worldSettings);

    BaseRenderer createGameLevelRenderer(ActorSpriteAnimController animController, Canvas canvas);

    GameSceneRenderer createGameSceneRenderer(GameScene gameScene, ActorSpriteAnimController animController, Canvas canvas);

    HUD_Style hudStyle();

    HUD_Renderer createHUDRenderer(GameScene gameScene, ActorSpriteAnimController animController, Canvas canvas);

    BaseRenderer createEntityRenderer(ActorSpriteAnimController animController, Canvas canvas);

    Ghost createAnimatedGhost(ActorSpriteAnimController animController, SpriteAnimContainer container, GhostPersonality personality);

    SpriteAnimFacade createGhostAnimations(SpriteAnimContainer container, GhostPersonality personality);

    SpriteAnimFacade createPacAnimations(SpriteAnimContainer container);

    Image killedGhostPointsImage(int killedGhostIndex);

    Image bonusSymbolImage(int symbolCode);

    Image bonusValueImage(int symbolCode);
}

