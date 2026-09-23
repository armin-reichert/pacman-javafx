/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.Disposable;
import de.amr.basics.math.RectShort;
import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.spriteanim.CommonSpriteAnimationID;
import de.amr.basics.ui.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.gamescene.bootscene.Arcade_BootScene;
import de.amr.pacmanfx.arcade.pacman.gamescene.bootscene.Arcade_BootScene_Renderer;
import de.amr.pacmanfx.arcade.pacman.gamescene.introscene.ArcadePacMan_IntroScene;
import de.amr.pacmanfx.arcade.pacman.gamescene.introscene.ArcadePacMan_IntroScene_Renderer;
import de.amr.pacmanfx.arcade.pacman.gamescene.playscene.ArcadePacMan_GameLevel_Renderer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
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
import de.amr.basics.ui.entities.props.bonuspoints.BonusPoints;
import de.amr.basics.ui.entities.props.ghostpoints.GhostPoints;
import de.amr.basics.ui.entities.props.messageview.MessageView;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.assets.GlobalAssets;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.basics.ui.assets.AssetMap;
import de.amr.basics.ui.entities.hud.HUD_Style;
import de.amr.basics.ui.rendering.BaseRenderer;
import de.amr.basics.ui.rendering.Renderer;
import de.amr.pacmanfx.uilib.ArcadeColor;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.EnumMap;
import java.util.Map;

import static de.amr.pacmanfx.game.GameVariantRenderConfig.*;
import static de.amr.pacmanfx.game.GameVariantRenderConfig.createBonusView;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_RenderConfig implements GameVariantRenderConfig, Disposable {

    public static final Map<MessageType, String> MESSAGE_TEXTS = new EnumMap<>(MessageType.class);
    static {
        MESSAGE_TEXTS.put(MessageType.READY, "READY!");
        MESSAGE_TEXTS.put(MessageType.GAME_OVER, "GAME  OVER");
        MESSAGE_TEXTS.put(MessageType.NO_MESSAGE, "");
    }

//    private static final Rectangle2D BOOT_SCENE_SPRITES = new Rectangle2D(400, 0, 256, 160);

    private final AssetMap assets;

    private final HUD_Style hudStyle;

    public ArcadePacMan_RenderConfig(AssetMap assets) {
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
            "CREDIT %2d"
        );
    }

    @Override
    public void dispose() {
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public GenericWorldMapColorScheme colorScheme(WorldMap worldMap, WorldSettings worldSettings) {
        requireNonNull(worldMap);
        return GlobalAssets.enhanceContrast(worldSettings, ArcadePacMan_UIConfig.WORLD_MAP_COLOR_SCHEME);
    }

    @Override
    public Renderable createEntityView(GameEntity gameEntity) {
        return switch(gameEntity) {
            case Pac pac     -> createPacView(pac);
            case Ghost ghost -> createGhostView(ghost);
            case Bonus bonus -> createBonusView(bonus);
            case Energizer energizer -> GameVariantRenderConfig.createEntityView(energizer, RenderingLayer.LEVEL, 0);
            case House house -> GameVariantRenderConfig.createEntityView(house, RenderingLayer.LEVEL, 0);
            case MessageView messageView -> GameVariantRenderConfig.createEntityView(messageView, RenderingLayer.MESSAGE, 0);
            case GhostPoints ghostPoints -> GameVariantRenderConfig.createEntityView(ghostPoints, RenderingLayer.PROPS, 0);
            case BonusPoints bonusPoints -> GameVariantRenderConfig.createEntityView(bonusPoints, RenderingLayer.PROPS, 0);
            default -> GameVariantRenderConfig.createEntityView(gameEntity, RenderingLayer.PROPS, 0);
        };
    }

    @Override
    public Renderer createGameSceneRenderer(GameScene gameScene, ActorSpriteAnimController animController, Canvas canvas) {
        requireNonNull(gameScene);
        requireNonNull(animController);
        requireNonNull(canvas);

        return switch (gameScene) {
            case Arcade_BootScene ignored -> new Arcade_BootScene_Renderer(canvas, spriteSheet());
            case ArcadePacMan_IntroScene ignored -> new ArcadePacMan_IntroScene_Renderer(canvas);
            default -> null;
        };
    }

    @Override
    public Renderer createGameLevelRenderer(ActorSpriteAnimController animController, Canvas canvas) {
        return new ArcadePacMan_GameLevel_Renderer(canvas, this);
    }

    @Override
    public HUD_Style hudStyle() {
        return hudStyle;
    }

    @Override
    public BaseRenderer createVariantRenderer(ActorSpriteAnimController animController, Canvas canvas) {
        requireNonNull(animController);
        requireNonNull(canvas);
        return new ArcadePacMan_VariantRenderer(animController, canvas);
    }

    @Override
    public Ghost createAnimatedGhost(ActorSpriteAnimController animController, SpriteAnimationContainer container, GhostPersonality personality) {
        final var factory = ArcadePacMan_ActorFactory.instance();
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
    public ArcadePacMan_GhostSAM createGhostAnimations(SpriteAnimationContainer container, GhostPersonality personality) {
        requireNonNull(personality);
        return new ArcadePacMan_GhostSAM(container, personality);
    }

    @Override
    public ArcadePacMan_PacSAM createPacAnimations(SpriteAnimationContainer container) {
        return new ArcadePacMan_PacSAM(container, spriteSheet());
    }

    @Override
    public Image createGhostPointsImage(int killedGhostIndex) {
        final RectShort[] numberSprites = spriteSheet().findSpriteSequence(SpriteID.GHOST_NUMBERS);
        return spriteSheet().createImage(numberSprites[killedGhostIndex]);
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
}
