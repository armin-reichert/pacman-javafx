/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ui.assets.AssetMap;
import de.amr.basics.ui.assets.SpriteSheet;
import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.entities.hud.HUD_Style;
import de.amr.basics.ui.rendering.GameEntityView;
import de.amr.basics.ui.rendering.Renderer;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.ui.spriteanim.SpriteAnimationAPI;
import de.amr.basics.ui.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
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

    static GameEntityView createEntityView(GameEntity gameEntity, RenderingLayer layer, int z) {
        return new GameEntityView(gameEntity, layer, z);
    }

    static GameEntityView createPropView(GameEntity gameEntity, int z) {
        return createEntityView(gameEntity, RenderingLayer.PROPS, z);
    }

    static GameEntityView createPropView(GameEntity gameEntity) {
        return createPropView(gameEntity, 0);
    }

    static GameEntityView createPacView(Pac pac) {
        return createEntityView(pac, RenderingLayer.ACTORS, PAC_Z);
    }

    static GameEntityView createGhostView(Ghost ghost) {
        return createEntityView(ghost, RenderingLayer.ACTORS, ghostZ(ghost.personality()));
    }

    static GameEntityView createBonusView(Bonus bonus) {
        return createEntityView(bonus, RenderingLayer.ACTORS, BONUS_Z);
    }

    AssetMap assets();

    SpriteSheet<?> spriteSheet();

    GenericWorldMapColorScheme colorScheme(WorldMap worldMap, WorldSettings worldSettings);

    HUD_Style hudStyle();

    GameEntityView createEntityView(GameEntity gameEntity);

    Renderer createGameLevelRenderer(ActorSpriteAnimController animController, Canvas canvas);

    Renderer createGameSceneRenderer(GameScene gameScene, ActorSpriteAnimController animController, Canvas canvas);

    default Renderer createGameSceneDebugRenderer(GameScene gameScene, ActorSpriteAnimController animController, Canvas canvas) {
        return BaseGameSceneDebugInfoRenderer.createDefaultGameSceneDebugRenderer(gameScene, canvas);
    }

    Renderer createVariantRenderer(ActorSpriteAnimController animController, Canvas canvas);

    Ghost createAnimatedGhost(ActorSpriteAnimController animController, SpriteAnimationContainer container, GhostPersonality personality);

    SpriteAnimationAPI createGhostAnimations(SpriteAnimationContainer container, GhostPersonality personality);

    SpriteAnimationAPI createPacAnimations(SpriteAnimationContainer container);

    Image createGhostPointsImage(int killedGhostIndex);

    Image createBonusSymbolImage(int bonusCode);

    Image createBonusPointsImage(int bonusCode);
}

