/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.gamescene.cutscenes.ArcadeMsPacMan_CutScene1;
import de.amr.pacmanfx.arcade.ms_pacman.gamescene.cutscenes.ArcadeMsPacMan_CutScene2;
import de.amr.pacmanfx.arcade.ms_pacman.gamescene.cutscenes.ArcadeMsPacMan_CutScene3;
import de.amr.pacmanfx.arcade.ms_pacman.gamescene.introscene.ArcadeMsPacMan_IntroScene;
import de.amr.pacmanfx.arcade.ms_pacman.gamescene.startscene.ArcadeMsPacMan_StartScene;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.gamescene.bootscene.Arcade_BootScene;
import de.amr.pacmanfx.arcade.pacman.gamescene.bootscene.Arcade_BootScene_Renderer;
import de.amr.pacmanfx.arcade.pacman.gamescene.playscene.Arcade_PlayScene2D;
import de.amr.pacmanfx.arcade.pacman.gamescene.playscene.Arcade_PlayScene2D_Renderer;
import de.amr.pacmanfx.core.Energizer;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.rendering.RenderableGameEntity;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.assets.GlobalAssets;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.entities.hud.comp.HUD_Style;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.core.rendering.RenderableGameEntity.*;
import static de.amr.pacmanfx.core.rendering.RenderableGameEntity.renderableBonus;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_RenderConfig implements GameVariantRenderConfig {

    private static final Rectangle2D BOOT_SCENE_SPRITES = new Rectangle2D(380, 0, 204, 208);

    protected final HUD_Style hudStyle;

    protected final AssetMap assets;

    public ArcadeMsPacMan_RenderConfig(AssetMap assets) {
        this.assets = assets;

        hudStyle = new HUD_Style(
            spriteSheet(),
            spriteSheet().findSprite(SpriteID.LIVES_COUNTER_SYMBOL),
            spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS),
            "SCORE",
            "HIGH SCORE",
            ARCADE_WHITE,
            Color.GRAY,
            GlobalFonts.ARCADE.font(),
            "CREDIT %2d");
    }

    @Override
    public SpriteSheet<SpriteID> spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    @Override
    public GenericWorldMapColorScheme colorScheme(WorldMap worldMap, WorldSettings worldSettings) {
        requireNonNull(worldMap);
        final int index = worldMap.getConfigValue(WorldMapConfigKey.COLOR_MAP_INDEX);
        return GlobalAssets.enhanceContrast(worldSettings, ArcadeMsPacMan_UIConfig.MAP_COLOR_SCHEMES[index]);
    }

    @Override
    public Renderable renderable(GameEntity gameEntity) {
        return switch(gameEntity) {
            case Pac pac     -> renderablePac(pac);
            case Ghost ghost -> renderableGhost(ghost);
            case Bonus bonus -> renderableBonus(bonus);
            case Energizer energizer -> renderableGameEntity(energizer, RenderingLayer.WORLD, 0);
            case House house -> renderableGameEntity(house, RenderingLayer.WORLD, 0);
            case MessageView messageView -> renderableGameEntity(messageView, RenderingLayer.WORLD, 0);
            case GhostPoints ghostPoints -> renderableGameEntity(ghostPoints, RenderingLayer.PROPS, 0);
            case BonusPoints bonusPoints -> renderableGameEntity(bonusPoints, RenderingLayer.PROPS, 0);
            default -> renderableGameEntity(gameEntity, RenderingLayer.PROPS, 0);
        };
    }

    @Override
    public BaseRenderer createGameSceneRenderer(GameScene gameScene, ActorSpriteAnimController animController, Canvas canvas) {
        requireNonNull(canvas);
        requireNonNull(gameScene);

        return switch (gameScene) {
            case Arcade_BootScene ignored        -> new Arcade_BootScene_Renderer(canvas, spriteSheet());
            case ArcadeMsPacMan_IntroScene ignored -> null;
            case ArcadeMsPacMan_StartScene ignored -> null;
            case Arcade_PlayScene2D ignored        -> new Arcade_PlayScene2D_Renderer(canvas, createGameLevelRenderer(animController, canvas));
            case ArcadeMsPacMan_CutScene1 ignored  -> null;
            case ArcadeMsPacMan_CutScene2 ignored  -> null;
            case ArcadeMsPacMan_CutScene3 ignored  -> null;
            default -> throw new IllegalStateException("Illegal game scene: " + gameScene);
        };
    }

    @Override
    public Renderer createGameLevelRenderer(ActorSpriteAnimController animSystem, Canvas canvas) {
        requireNonNull(animSystem);
        requireNonNull(canvas);
        return new ArcadeMsPacMan_GameLevelRenderer(animSystem, canvas, assets);
    }

    @Override
    public HUD_Style hudStyle() {
        return hudStyle;
    }

    @Override
    public BaseRenderer createVariantRenderer(ActorSpriteAnimController animController, Canvas canvas) {
        requireNonNull(animController);
        requireNonNull(canvas);

        return new ArcadeMsPacMan_VariantRenderer(animController, canvas);
    }

    @Override
    public ArcadeMsPacMan_GhostSAM createGhostAnimations(SpriteAnimationContainer container, GhostPersonality personality) {
        requireNonNull(personality);
        return new ArcadeMsPacMan_GhostSAM(container, personality);
    }

    @Override
    public ArcadeMsPacMan_PacSAM createPacAnimations(SpriteAnimationContainer container) {
        return new ArcadeMsPacMan_PacSAM(container);
    }

    @Override
    public Ghost createAnimatedGhost(ActorSpriteAnimController animController, SpriteAnimationContainer container, GhostPersonality personality) {
        final var factory = new ArcadeMsPacMan_ActorFactory();

        final Ghost ghost = switch (personality) {
            case RED_GHOST_SHADOW   -> factory.createRedGhost();
            case PINK_GHOST_SPEEDY  -> factory.createPinkGhost();
            case CYAN_GHOST_BASHFUL -> factory.createCyanGhost();
            case ORANGE_GHOST_POKEY -> factory.createOrangeGhost();
        };

        animController.setAnimations(ghost, createGhostAnimations(container, personality));
        animController.select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);

        return ghost;
    }

    @Override
    public Image bonusSymbolImage(int bonusCode) {
        final RectShort[] sprites = spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS);
        return spriteSheet().image(sprites[bonusCode]);
    }

    @Override
    public Image bonusValueImage(int bonusCode) {
        final RectShort[] sprites = spriteSheet().findSpriteSequence(SpriteID.BONUS_VALUES);
        return spriteSheet().image(sprites[bonusCode]);
    }

    @Override
    public Image killedGhostPointsImage(int killedGhostIndex) {
        final RectShort[] numberSprites = spriteSheet().findSpriteSequence(SpriteID.GHOST_NUMBERS);
        return spriteSheet().image(numberSprites[killedGhostIndex]);
    }
}
