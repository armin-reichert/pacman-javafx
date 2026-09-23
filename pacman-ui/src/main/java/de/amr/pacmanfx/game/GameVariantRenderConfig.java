/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.ui.entities.hud.HUD_Style;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.ui.ecs.GameEntity;
import de.amr.basics.ui.ecs.systems.ActorSpriteAnimController;
import de.amr.basics.ui.spriteanim.SpriteAnimFacade;
import de.amr.basics.ui.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.GameEntityView;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.basics.ui.assets.AssetMap;
import de.amr.basics.ui.assets.SpriteSheet;
import de.amr.basics.ui.rendering.Renderer;
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

    static GameEntityView renderableGameEntity(GameEntity gameEntity, RenderingLayer layer, int z) {
        return new GameEntityView(gameEntity, layer, z);
    }

    static GameEntityView renderableProp(GameEntity gameEntity) {
        return renderableGameEntity(gameEntity, RenderingLayer.PROPS, 0);
    }

    static GameEntityView renderablePac(Pac pac) {
        return renderableGameEntity(pac, RenderingLayer.ACTORS, PAC_Z);
    }

    static GameEntityView renderableGhost(Ghost ghost) {
        return renderableGameEntity(ghost, RenderingLayer.ACTORS, ghostZ(ghost.personality()));
    }

    static GameEntityView renderableBonus(Bonus bonus) {
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

