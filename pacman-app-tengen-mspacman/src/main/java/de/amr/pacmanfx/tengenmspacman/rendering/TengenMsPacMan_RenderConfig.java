/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;


import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.world.MapColorScheme;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.WorldMapConfigKey;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.gamescene.*;
import de.amr.pacmanfx.tengenmspacman.model.BonusSymbol;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_GhostAnimations;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_PacAnimations;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.Map;

public class TengenMsPacMan_RenderConfig implements GameVariantRenderConfig {

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

    public static int bonusValueSpriteIndex(int bonusSymbolCode) {
        if (bonusSymbolCode < 0 || bonusSymbolCode >= BonusSymbol.values().length) {
            throw new IllegalArgumentException("Illegal bonus symbol code: " + bonusSymbolCode);
        }
        final BonusSymbol symbol = BonusSymbol.values()[bonusSymbolCode];
        return BONUS_VALUE_SPRITE_INDEX.getOrDefault(symbol, bonusSymbolCode);
    }

    private final AssetMap assets;

    public TengenMsPacMan_RenderConfig(AssetMap assets) {
        this.assets = assets;
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
    public WorldMapColorScheme colorScheme(WorldMap worldMap, WorldSettings worldSettings) {
        final MapColorScheme scheme = worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME);
        final WorldMapColorScheme colorScheme = new WorldMapColorScheme(
            scheme.wallFill(), scheme.wallStroke(), scheme.door(), scheme.pellet());
        return GlobalAssets.enhanceContrast(worldSettings, colorScheme);
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(AbstractGameScene2D gameScene2D, Canvas canvas) {
        final GameScene2D_Renderer renderer = switch (gameScene2D) {
            case TengenMsPacMan_BootScene ignored -> new TengenMsPacMan_BootScene_Renderer(this, gameScene2D, canvas);
            case TengenMsPacMan_IntroScene ignored -> new TengenMsPacMan_IntroScene_Renderer(this, gameScene2D, canvas);
            case TengenMsPacMan_OptionsScene ignored -> new TengenMsPacMan_OptionsScene_Renderer(gameScene2D, canvas);
            case TengenMsPacMan_PlayScene2D ignored -> new TengenMsPacMan_PlayScene2D_Renderer(this, gameScene2D, canvas);
            case TengenMsPacMan_CreditsScene ignored -> new TengenMsPacMan_CreditsScene_Renderer(gameScene2D, canvas);
            case TengenMsPacMan_CutScene1    ignored -> new TengenMsPacMan_CutScene1_Renderer(this, gameScene2D, canvas);
            case TengenMsPacMan_CutScene2    ignored -> new TengenMsPacMan_CutScene2_Renderer(this, gameScene2D, canvas);
            case TengenMsPacMan_CutScene3    ignored -> new TengenMsPacMan_CutScene3_Renderer(this, gameScene2D, canvas);
            case TengenMsPacMan_CutScene4    ignored -> new TengenMsPacMan_CutScene4_Renderer(this, gameScene2D, canvas);
            default -> throw new IllegalStateException("Unexpected value: " + gameScene2D);
        };
        return gameScene2D.configureRenderer(renderer);
    }

    @Override
    public TengenMsPacMan_GameLevelRenderer createGameLevelRenderer(Canvas canvas) {
        return new TengenMsPacMan_GameLevelRenderer(assets, canvas);
    }

    @Override
    public TengenMsPacMan_HeadsUpDisplay_Renderer createHUDRenderer(AbstractGameScene2D gameScene2D, Canvas canvas) {
        return gameScene2D.configureRenderer(new TengenMsPacMan_HeadsUpDisplay_Renderer(canvas));
    }

    @Override
    public TengenMsPacMan_ActorRenderer createActorRenderer(Canvas canvas) {
        return new TengenMsPacMan_ActorRenderer(canvas);
    }

    @Override
    public Ghost createAnimatedGhost(SpriteAnimationContainer container, byte personality) {
        final Ghost ghost = TengenMsPacMan_ActorFactory.createGhost(personality);
        ghost.setAnimations(createGhostAnimations(container, personality));
        ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
        return ghost;
    }

    @Override
    public TengenMsPacMan_GhostAnimations createGhostAnimations(SpriteAnimationContainer container, byte personality) {
        return new TengenMsPacMan_GhostAnimations(container, personality);
    }

    @Override
    public TengenMsPacMan_PacAnimations createPacAnimations(SpriteAnimationContainer container) {
        return new TengenMsPacMan_PacAnimations(container);
    }

    @Override
    public Image killedGhostPointsImage(int killedGhostIndex) {
        final RectShort[] numberSprites = spriteSheet().findSprites(SpriteID.GHOST_NUMBERS);
        return spriteSheet().image(numberSprites[killedGhostIndex]);
    }

    @Override
    public Image bonusSymbolImage(int symbolCode) {
        final RectShort[] symbolSprites = spriteSheet().findSprites(SpriteID.BONUS_SYMBOLS);
        return spriteSheet().image(symbolSprites[symbolCode]);
    }

    @Override
    public Image bonusValueImage(int symbolCode) {
        final int spriteIndex = bonusValueSpriteIndex(symbolCode);
        final RectShort sprite = spriteSheet().findSprites(SpriteID.BONUS_VALUES)[spriteIndex];
        return spriteSheet().image(sprite);
    }

}
