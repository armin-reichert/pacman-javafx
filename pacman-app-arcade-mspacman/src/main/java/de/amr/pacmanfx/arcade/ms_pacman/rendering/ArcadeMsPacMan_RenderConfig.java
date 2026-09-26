/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.math.RectShort;
import de.amr.basics.ui.assets.AssetMap;
import de.amr.basics.ui.assets.SpriteSheet;
import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.entities.hud.HUD_Style;
import de.amr.basics.ui.entities.props.bonuspoints.BonusPoints;
import de.amr.basics.ui.entities.props.ghostpoints.GhostPoints;
import de.amr.basics.ui.entities.props.messageview.MessageView;
import de.amr.basics.ui.rendering.BaseRenderer;
import de.amr.basics.ui.rendering.GameEntityView;
import de.amr.basics.ui.rendering.Renderer;
import de.amr.basics.ui.spriteanim.CommonSpriteAnimationID;
import de.amr.basics.ui.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.core.Energizer;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.assets.GlobalAssets;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.ArcadeColor;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder.*;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_RenderConfig implements GameVariantRenderConfig {

//    private static final Rectangle2D BOOT_SCENE_SPRITES = new Rectangle2D(380, 0, 204, 208);

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
            ArcadeColor.WHITE.color(),
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
    public GameEntityView createEntityView(GameEntity gameEntity) {
        return switch(gameEntity) {
            case Pac pac -> pacView(pac);
            case Ghost ghost -> ghostView(ghost);
            case Bonus bonus -> bonusView(bonus);
            case Energizer energizer -> levelEntityView(energizer);
            case House house -> levelEntityView(house);
            case MessageView messageView -> messageEntityView(messageView);
            case GhostPoints ghostPoints -> propView(ghostPoints);
            case BonusPoints bonusPoints -> propView(bonusPoints);
            default -> propView(gameEntity);
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
    public Image createBonusSymbolImage(int bonusCode) {
        final RectShort[] sprites = spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS);
        return spriteSheet().createImage(sprites[bonusCode]);
    }

    @Override
    public Image createBonusPointsImage(int bonusCode) {
        final RectShort[] sprites = spriteSheet().findSpriteSequence(SpriteID.BONUS_VALUES);
        return spriteSheet().createImage(sprites[bonusCode]);
    }

    @Override
    public Image createGhostPointsImage(int killedGhostIndex) {
        final RectShort[] numberSprites = spriteSheet().findSpriteSequence(SpriteID.GHOST_NUMBERS);
        return spriteSheet().createImage(numberSprites[killedGhostIndex]);
    }
}
