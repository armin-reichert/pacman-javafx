/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.gamescene.*;
import de.amr.pacmanfx.tengenmspacman.model.BonusSymbol;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_GhostSAM;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_PacSAM;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.ui.gamescene.d2.HUD_Style;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.EnumMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_RenderConfig implements GameVariantRenderConfig {

    private static final EnumMap<MessageType, Color> MESSAGE_COLORS = new EnumMap<>(Map.of(
        MessageType.READY,     NES_Palette.color(0x28),
        MessageType.GAME_OVER, NES_Palette.color(0x11)  // blue
    ));

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
        GlobalAssets.Fonts.ARCADE8.font(),
        "CREDIT %d", // not used
        GlobalAssets.Fonts.ARCADE8.font(),
        MESSAGE_COLORS::get
    );

    public TengenMsPacMan_RenderConfig(AssetMap assets) {
        this.assets = requireNonNull(assets);
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
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
    public BaseRenderer createGameSceneRenderer(GameScene gameScene, ActorSpriteAnimController animController, Canvas canvas) {
        final CanvasRenderingComp r2D = gameScene.reqComp(CanvasRenderingComp.class);
        final BaseRenderer renderer = switch (gameScene) {
            case TengenMsPacMan_BootScene    ignored -> new TengenMsPacMan_BootScene_Renderer(this, gameScene, animController, canvas);
            case TengenMsPacMan_IntroScene   ignored -> new TengenMsPacMan_IntroScene_Renderer(this, gameScene, animController, canvas);
            case TengenMsPacMan_OptionsScene ignored -> new TengenMsPacMan_OptionsScene_Renderer(gameScene, canvas);
            case TengenMsPacMan_PlayScene2D  ignored -> new TengenMsPacMan_PlayScene2D_Renderer(this, gameScene, animController, canvas);
            case TengenMsPacMan_CreditsScene ignored -> new TengenMsPacMan_CreditsScene_Renderer(gameScene, canvas);
            case TengenMsPacMan_CutScene1    ignored -> new TengenMsPacMan_CutScene_Renderer(this, gameScene, animController, canvas);
            case TengenMsPacMan_CutScene2    ignored -> new TengenMsPacMan_CutScene_Renderer(this, gameScene, animController, canvas);
            case TengenMsPacMan_CutScene3    ignored -> new TengenMsPacMan_CutScene_Renderer(this, gameScene, animController, canvas);
            case TengenMsPacMan_CutScene4    ignored -> new TengenMsPacMan_CutScene_Renderer(this, gameScene, animController, canvas);
            default -> throw new IllegalStateException("Unexpected value: " + gameScene);
        };
        return r2D.configureRenderer(renderer);
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
    public TengenMsPacMan_HUD_Renderer createHUDRenderer(GameScene gameScene, ActorSpriteAnimController animController, Canvas canvas) {
        final CanvasRenderingComp r2D = gameScene.reqComp(CanvasRenderingComp.class);
        return r2D.configureRenderer(new TengenMsPacMan_HUD_Renderer(hudStyle, canvas));
    }

    @Override
    public TengenMsPacMan_ActorRenderer createActorRenderer(ActorSpriteAnimController animController, Canvas canvas) {
        return new TengenMsPacMan_ActorRenderer(animController, canvas);
    }

    @Override
    public Ghost createAnimatedGhost(ActorSpriteAnimController animController, SpriteAnimContainer container, GhostPersonality personality) {
        final var factory = TengenMsPacMan_ActorFactory.instance();
        final Ghost ghost = switch (personality) {
            case RED_GHOST_SHADOW -> factory.createRedGhost();
            case PINK_GHOST_SPEEDY -> factory.createPinkGhost();
            case CYAN_GHOST_BASHFUL -> factory.createCyanGhost();
            case ORANGE_GHOST_POKEY -> factory.createOrangeGhost();
        };

        animController.setAnimations(ghost, createGhostAnimations(container, personality));
        animController.select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);

        return ghost;
    }

    @Override
    public TengenMsPacMan_GhostSAM createGhostAnimations(SpriteAnimContainer container, GhostPersonality personality) {
        return new TengenMsPacMan_GhostSAM(container, personality);
    }

    @Override
    public TengenMsPacMan_PacSAM createPacAnimations(SpriteAnimContainer container) {
        return new TengenMsPacMan_PacSAM(container);
    }

    @Override
    public Image killedGhostPointsImage(int killedGhostIndex) {
        final RectShort[] numberSprites = spriteSheet().findSpriteSequence(SpriteID.GHOST_NUMBERS);
        return spriteSheet().image(numberSprites[killedGhostIndex]);
    }

    @Override
    public Image bonusSymbolImage(int symbolCode) {
        final RectShort[] symbolSprites = spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS);
        return spriteSheet().image(symbolSprites[symbolCode]);
    }

    @Override
    public Image bonusValueImage(int symbolCode) {
        final int spriteIndex = bonusValueSpriteIndex(symbolCode);
        final RectShort sprite = spriteSheet().findSpriteSequence(SpriteID.BONUS_VALUES)[spriteIndex];
        return spriteSheet().image(sprite);
    }

    public int bonusValueSpriteIndex(int bonusSymbolCode) {
        if (bonusSymbolCode < 0 || bonusSymbolCode >= BonusSymbol.values().length) {
            throw new IllegalArgumentException("Illegal bonus symbol code: " + bonusSymbolCode);
        }
        final BonusSymbol symbol = BonusSymbol.values()[bonusSymbolCode];
        return BONUS_VALUE_SPRITE_INDEX.getOrDefault(symbol, bonusSymbolCode);
    }
}
