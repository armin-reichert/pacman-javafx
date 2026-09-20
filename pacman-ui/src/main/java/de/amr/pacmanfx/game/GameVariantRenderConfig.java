/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.bonus.Bonus;
import de.amr.pacmanfx.core.entities.ghost.Ghost;
import de.amr.pacmanfx.core.entities.pac.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.rendering.RenderableGameEntity;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimFacade;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.entities.hud.comp.HUD_Style;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

public interface GameVariantRenderConfig {

    int PAC_Z = 0;
    int BONUS_Z = -1;

    static int ghostZ(GhostPersonality p) {
        return switch (p) {
            case RED_GHOST_SHADOW   -> 13; // on top of all other ghosts
            case PINK_GHOST_SPEEDY  -> 12;
            case CYAN_GHOST_BASHFUL -> 11;
            case ORANGE_GHOST_POKEY -> 10; // behind all other ghosts
        };
    }

    static RenderableGameEntity renderableGameEntity(GameEntity gameEntity, RenderingLayer layer, int z) {
        return new RenderableGameEntity(gameEntity, layer, z);
    }

    static RenderableGameEntity renderablePac(Pac pac) {
        return renderableGameEntity(pac, RenderingLayer.ACTORS, PAC_Z);
    }

    static RenderableGameEntity renderableGhost(Ghost ghost) {
        return renderableGameEntity(ghost, RenderingLayer.ACTORS, ghostZ(ghost.personality()));
    }

    static RenderableGameEntity renderableBonus(Bonus bonus) {
        return renderableGameEntity(bonus, RenderingLayer.ACTORS, BONUS_Z);
    }

    AssetMap assets();

    SpriteSheet<?> spriteSheet();

    GenericWorldMapColorScheme colorScheme(WorldMap worldMap, WorldSettings worldSettings);

    HUD_Style hudStyle();

    Renderable renderable(GameEntity gameEntity);

    Renderer createGameLevelRenderer(ActorSpriteAnimController animController, Canvas canvas);

    Renderer createGameSceneRenderer(GameScene gameScene, ActorSpriteAnimController animController, Canvas canvas);

    default Renderer createGameSceneDebugRenderer(GameScene gameScene, ActorSpriteAnimController animController, Canvas canvas) {
        return BaseGameSceneDebugInfoRenderer.createDefaultGameSceneDebugRenderer(gameScene, canvas);
    }

    Renderer createVariantRenderer(ActorSpriteAnimController animController, Canvas canvas);

    Ghost createAnimatedGhost(ActorSpriteAnimController animController, SpriteAnimationContainer container, GhostPersonality personality);

    SpriteAnimFacade createGhostAnimations(SpriteAnimationContainer container, GhostPersonality personality);

    SpriteAnimFacade createPacAnimations(SpriteAnimationContainer container);

    Image killedGhostPointsImage(int killedGhostIndex);

    Image bonusSymbolImage(int bonusCode);

    Image bonusValueImage(int bonusCode);
}

