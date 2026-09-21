/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.basics.ui.ecs.GameEntity;
import de.amr.basics.ui.ecs.systems.ActorSpriteAnimController;
import de.amr.basics.ui.spriteanim.CommonSpriteAnimationID;
import de.amr.basics.ui.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.Energizer;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.world.house.House;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.basics.ui.entities.props.messageview.MessageType;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.basics.ui.entities.props.bonuspoints.BonusPoints;
import de.amr.basics.ui.entities.props.ghostpoints.GhostPoints;
import de.amr.basics.ui.entities.props.messageview.MessageView;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.gamescene.bootscene.TengenMsPacMan_BootScene;
import de.amr.pacmanfx.tengenmspacman.gamescene.creditsscene.TengenMsPacMan_CreditsScene;
import de.amr.pacmanfx.tengenmspacman.gamescene.creditsscene.TengenMsPacMan_CreditsScene_Renderer;
import de.amr.pacmanfx.tengenmspacman.gamescene.cutscenes.TengenMsPacMan_CutScene1;
import de.amr.pacmanfx.tengenmspacman.gamescene.cutscenes.TengenMsPacMan_CutScene2;
import de.amr.pacmanfx.tengenmspacman.gamescene.cutscenes.TengenMsPacMan_CutScene3;
import de.amr.pacmanfx.tengenmspacman.gamescene.cutscenes.TengenMsPacMan_CutScene4;
import de.amr.pacmanfx.tengenmspacman.gamescene.introscene.TengenMsPacMan_IntroScene;
import de.amr.pacmanfx.tengenmspacman.gamescene.introscene.TengenMsPacMan_IntroScene_Renderer;
import de.amr.pacmanfx.tengenmspacman.gamescene.optionsscene.TengenMsPacMan_OptionsScene;
import de.amr.pacmanfx.tengenmspacman.gamescene.optionsscene.TengenMsPacMan_OptionsScene_Renderer;
import de.amr.pacmanfx.tengenmspacman.gamescene.playscene.TengenMsPacMan_PlayScene2D;
import de.amr.pacmanfx.tengenmspacman.gamescene.playscene.TengenMsPacMan_PlaySceneDebugInfoRenderer;
import de.amr.pacmanfx.tengenmspacman.model.BonusSymbol;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_GhostSAM;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_PacSAM;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.assets.GlobalAssets;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.basics.ui.assets.AssetMap;
import de.amr.basics.ui.assets.SpriteSheet;
import de.amr.basics.ui.entities.hud.HUD_Style;
import de.amr.basics.ui.rendering.BaseRenderer;
import de.amr.basics.ui.rendering.Renderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.EnumMap;
import java.util.Map;

import static de.amr.pacmanfx.game.GameVariantRenderConfig.*;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_RenderConfig implements GameVariantRenderConfig {

    public static final Map<MessageType, String> MESSAGE_TEXTS = new EnumMap<>(MessageType.class);
    static {
        MESSAGE_TEXTS.put(MessageType.READY, "READY!");
        MESSAGE_TEXTS.put(MessageType.GAME_OVER, "GAME OVER");
        MESSAGE_TEXTS.put(MessageType.NO_MESSAGE, "");
    }

        /*
        final MessageAnimation animation = session.value(
            TengenMsPacMan_Extras.GAME_OVER_MESSAGE_ANIMATION, MessageAnimation.class);

        final Vector2f pos = animation != null
            ? animation.pos().asVector2f()
            : messagePosition(level);


        final NES_WorldMapColorScheme colorScheme = level.worldMap()
            .getConfigValue(WorldMapConfigKey.COLOR_SCHEME);

        final Color color = session.isAttractMode()
            ? Color.valueOf(colorScheme.wallStroke())
            : style.messageColor().apply(MessageType.GAME_OVER);

         */


    // Note: Order of bonus symbols in spritesheet is not 1:1 with order of bonus values!
    // 0=100,1=200,2=500,3=700,4=1000,5=2000,6=3000,7=4000,8=5000,9=6000,10=7000,11=8000,12=9000, 13=10_000
    private static final Map<BonusSymbol, Integer> BONUS_VALUE_SPRITE_INDEX = new EnumMap<>(BonusSymbol.class);
    static {
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.CHERRY,      0); // "100"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.STRAWBERRY,  1); // "200"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.ORANGE,      2); // "500"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.PRETZEL,     3); // "700"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.APPLE,       4); // "1000"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.PEAR,        5); // "2000"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.BANANA,      8); // 6 -> 8 ("5000")
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.MILK,        6); // 7 -> 6 ("3000")
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.ICE_CREAM,   7); // 8 -> 7 ("4000")
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.HIGH_HEELS,  9); // "6000"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.STAR,       10); // "7000"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.HAND,       11); // "8000"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.RING,       12); // "9000"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.FLOWER,     13); // "TEN!000"
    }

    /** Shades of blue sequence used by animation. */
    private static final Color[] SHADES_OF_BLUE = {
        NES_Palette.color(0x01), NES_Palette.color(0x11), NES_Palette.color(0x21), NES_Palette.color(0x31)
    };

    /**
     * Blue color, changing from dark to brighter blue. Cycles through NES palette indices 0x01, 0x11, 0x21, 0x31 each 16 ticks.
     */
    public static Color shadeOfBlue(long tick) {
        return SHADES_OF_BLUE[(int) (tick % 64) / 16];
    }

    private final AssetMap assets;

    private final HUD_Style hudStyle = new HUD_Style(
        spriteSheet(),
        spriteSheet().findSprite(SpriteID.LIVES_COUNTER_SYMBOL),
        spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS),
        "1UP",
        "HIGH SCORE",
        NES_Palette.color(0x20),
        NES_Palette.color(0x10),
        GlobalFonts.ARCADE.font(),
        "CREDIT %d" // not used in Tengen
    );

    public TengenMsPacMan_RenderConfig(AssetMap assets) {
        this.assets = requireNonNull(assets);
    }

    @Override
    public SpriteSheet<SpriteID> spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    @Override
    public GenericWorldMapColorScheme colorScheme(WorldMap worldMap, WorldSettings worldSettings) {
        requireNonNull(worldMap);
        requireNonNull(worldSettings);

        final WorldMapColorScheme spec = worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME);
        final GenericWorldMapColorScheme colorScheme = new GenericWorldMapColorScheme(
            spec.wallFill(), spec.wallStroke(), spec.door(), spec.pellet());
        return GlobalAssets.enhanceContrast(worldSettings, colorScheme);
    }

    @Override
    public Renderable renderable(GameEntity gameEntity) {
        return switch(gameEntity) {
            case Pac pac     -> renderablePac(pac);
            case Ghost ghost -> renderableGhost(ghost);
            case Bonus bonus -> renderableBonus(bonus);
            case Energizer energizer -> renderableGameEntity(energizer, RenderingLayer.WORLD, 0);
            case House house -> renderableGameEntity(house, RenderingLayer.WORLD, 0);
            case MessageView messageView -> renderableGameEntity(messageView, RenderingLayer.MESSAGE, 0);
            case GhostPoints ghostPoints -> renderableGameEntity(ghostPoints, RenderingLayer.PROPS, 0);
            case BonusPoints bonusPoints -> renderableGameEntity(bonusPoints, RenderingLayer.PROPS, 0);
            default -> renderableGameEntity(gameEntity, RenderingLayer.PROPS, 0);
        };
    }

    @Override
    public BaseRenderer createGameSceneRenderer(GameScene gameScene, ActorSpriteAnimController animController, Canvas canvas) {
        return switch (gameScene) {
            case TengenMsPacMan_BootScene ignore    -> null;
            case TengenMsPacMan_IntroScene ignore   -> new TengenMsPacMan_IntroScene_Renderer(gameScene.app().variantManager().currentRuntime(), canvas);
            case TengenMsPacMan_OptionsScene ignore -> new TengenMsPacMan_OptionsScene_Renderer(canvas);
            case TengenMsPacMan_PlayScene2D ignore  -> null;
            case TengenMsPacMan_CreditsScene ignore -> new TengenMsPacMan_CreditsScene_Renderer(canvas);
            case TengenMsPacMan_CutScene1 ignore    -> null;
            case TengenMsPacMan_CutScene2 ignore    -> null;
            case TengenMsPacMan_CutScene3 ignore    -> null;
            case TengenMsPacMan_CutScene4 ignore    -> null;
            default -> throw new IllegalStateException("Unexpected value: " + gameScene);
        };
    }

    @Override
    public Renderer createGameSceneDebugRenderer(GameScene gameScene, ActorSpriteAnimController animController, Canvas canvas) {
        return switch (gameScene) {
            case TengenMsPacMan_PlayScene2D _ -> new TengenMsPacMan_PlaySceneDebugInfoRenderer(animController, canvas);
            default -> GameVariantRenderConfig.super.createGameSceneDebugRenderer(gameScene, animController, canvas);
        };
    }

    @Override
    public TengenMsPacMan_GameLevelRenderer createGameLevelRenderer(ActorSpriteAnimController animController, Canvas canvas) {
        return new TengenMsPacMan_GameLevelRenderer(canvas);
    }

    @Override
    public HUD_Style hudStyle() {
        return hudStyle;
    }

    @Override
    public TengenMsPacMan_VariantRenderer createVariantRenderer(ActorSpriteAnimController animController, Canvas canvas) {
        return new TengenMsPacMan_VariantRenderer(animController, canvas);
    }

    @Override
    public Ghost createAnimatedGhost(ActorSpriteAnimController animController, SpriteAnimationContainer animContainer, GhostPersonality personality) {
        final var factory = TengenMsPacMan_ActorFactory.instance();
        final Ghost ghost = switch (personality) {
            case RED_GHOST_SHADOW -> factory.createRedGhost();
            case PINK_GHOST_SPEEDY -> factory.createPinkGhost();
            case CYAN_GHOST_BASHFUL -> factory.createCyanGhost();
            case ORANGE_GHOST_POKEY -> factory.createOrangeGhost();
        };

        animController.setAnimations(ghost, createGhostAnimations(animContainer, personality));
        animController.select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);

        return ghost;
    }

    @Override
    public TengenMsPacMan_GhostSAM createGhostAnimations(SpriteAnimationContainer animContainer, GhostPersonality personality) {
        return new TengenMsPacMan_GhostSAM(animContainer, personality);
    }

    @Override
    public TengenMsPacMan_PacSAM createPacAnimations(SpriteAnimationContainer animContainer) {
        return new TengenMsPacMan_PacSAM(animContainer);
    }

    @Override
    public Image killedGhostPointsImage(int killedGhostIndex) {
        final RectShort[] numberSprites = spriteSheet().findSpriteSequence(SpriteID.GHOST_NUMBERS);
        return spriteSheet().image(numberSprites[killedGhostIndex]);
    }

    @Override
    public Image bonusSymbolImage(int bonusCode) {
        final RectShort[] symbolSprites = spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS);
        return spriteSheet().image(symbolSprites[bonusCode]);
    }

    @Override
    public Image bonusValueImage(int bonusCode) {
        final int spriteIndex = bonusValueSpriteIndex(bonusCode);
        final RectShort sprite = spriteSheet().findSpriteSequence(SpriteID.BONUS_VALUES)[spriteIndex];
        return spriteSheet().image(sprite);
    }

    public int bonusValueSpriteIndex(int bonusCode) {
        if (bonusCode < 0 || bonusCode >= BonusSymbol.values().length) {
            throw new IllegalArgumentException("Illegal bonus symbol code: " + bonusCode);
        }
        final BonusSymbol symbol = BonusSymbol.values()[bonusCode];
        return BONUS_VALUE_SPRITE_INDEX.getOrDefault(symbol, bonusCode);
    }
}
