/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.scenes.bootscene.Arcade_BootScene;
import de.amr.pacmanfx.arcade.pacman.scenes.bootscene.Arcade_BootScene_Renderer;
import de.amr.pacmanfx.arcade.pacman.scenes.cutscenes.ArcadePacMan_CutScene1;
import de.amr.pacmanfx.arcade.pacman.scenes.cutscenes.ArcadePacMan_CutScene2;
import de.amr.pacmanfx.arcade.pacman.scenes.cutscenes.ArcadePacMan_CutScene3;
import de.amr.pacmanfx.arcade.pacman.scenes.introscene.ArcadePacMan_IntroScene;
import de.amr.pacmanfx.arcade.pacman.scenes.introscene.ArcadePacMan_IntroScene_Renderer;
import de.amr.pacmanfx.arcade.pacman.scenes.playscene.ArcadePacMan_GameLevel_Renderer;
import de.amr.pacmanfx.arcade.pacman.scenes.playscene.Arcade_PlayScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.playscene.Arcade_PlayScene2D_Renderer;
import de.amr.pacmanfx.arcade.pacman.scenes.startscene.ArcadePacMan_StartScene;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.entities.hud.comp.HUD_Style;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.CommonGameLevelRenderInfoKey;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.EnumMap;
import java.util.Map;

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_RenderConfig implements GameVariantRenderConfig {

    public static final Map<MessageType, String> MESSAGE_TEXTS = new EnumMap<>(MessageType.class);
    static {
        MESSAGE_TEXTS.put(MessageType.READY, "READY!");
        MESSAGE_TEXTS.put(MessageType.GAME_OVER, "GAME  OVER");
        MESSAGE_TEXTS.put(MessageType.NO_MESSAGE, "");
    }

    private static final GenericWorldMapColorScheme WORLD_MAP_COLOR_SCHEME = new GenericWorldMapColorScheme(
        ARCADE_BLACK.toString(), ARCADE_BLUE.toString(), ARCADE_PINK.toString(), ARCADE_ROSE.toString()
    );

    private static final Map<Color, Color> BRIGHT_MAZE_COLOR_CHANGES = Map.of(
        Color.valueOf(WORLD_MAP_COLOR_SCHEME.wallStroke()), ARCADE_WHITE,   // wall color change
        Color.valueOf(WORLD_MAP_COLOR_SCHEME.door()), Color.TRANSPARENT // door color change
    );

    private static final Rectangle2D BOOT_SCENE_SPRITES = new Rectangle2D(400, 0, 256, 160);

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
            ARCADE_WHITE,
            Color.GRAY,
            GlobalAssets.Fonts.ARCADE.font(),
            "CREDIT %2d"
        );
    }

    @Override
    public void addAssets() {
        assets.addAsset("maze.bright", createBrightEmptyMap());
    }

    private Image createBrightEmptyMap() {
        return Ufx.recolorImage(spriteSheet().image(SpriteID.MAP_EMPTY), BRIGHT_MAZE_COLOR_CHANGES);
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
        return GlobalAssets.enhanceContrast(worldSettings, WORLD_MAP_COLOR_SCHEME);
    }

    @Override
    public BaseRenderer createGameSceneRenderer(GameScene gameScene, ActorSpriteAnimController animController, Canvas canvas) {
        requireNonNull(gameScene);
        requireNonNull(animController);
        requireNonNull(canvas);

        return switch (gameScene) {
            case Arcade_BootScene ignored      -> new Arcade_BootScene_Renderer(gameScene, canvas, spriteSheet());
            case ArcadePacMan_IntroScene ignored -> new ArcadePacMan_IntroScene_Renderer(gameScene, canvas);
            case ArcadePacMan_StartScene ignored -> null;
            case Arcade_PlayScene2D ignored      -> new Arcade_PlayScene2D_Renderer(gameScene, animController, canvas, spriteSheet());
            case ArcadePacMan_CutScene1 ignored  -> null;
            case ArcadePacMan_CutScene2 ignored  -> null;
            case ArcadePacMan_CutScene3 ignored  -> null;
            default -> throw new IllegalStateException("Illegal game scene: " + gameScene);
        };
    }

    @Override
    public ArcadePacMan_GameLevel_Renderer createGameLevelRenderer(ActorSpriteAnimController animController, Canvas canvas) {
        requireNonNull(canvas);
        final var renderer = new ArcadePacMan_GameLevel_Renderer(canvas);
        renderer.info().put(CommonGameLevelRenderInfoKey.BRIGHT_MAZE_IMAGE, assets.image("maze.bright"));
        return renderer;
    }

    @Override
    public HUD_Style hudStyle() {
        return hudStyle;
    }

    @Override
    public BaseRenderer createEntityRenderer(ActorSpriteAnimController animController, Canvas canvas) {
        requireNonNull(animController);
        requireNonNull(canvas);
        return new ArcadePacMan_EntityRenderer(animController, canvas);
    }

    @Override
    public Ghost createAnimatedGhost(ActorSpriteAnimController animController, SpriteAnimContainer container, GhostPersonality personality) {
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
    public ArcadePacMan_GhostSAM createGhostAnimations(SpriteAnimContainer container, GhostPersonality personality) {
        requireNonNull(personality);
        return new ArcadePacMan_GhostSAM(container, personality);
    }

    @Override
    public ArcadePacMan_PacSAM createPacAnimations(SpriteAnimContainer container) {
        return new ArcadePacMan_PacSAM(container, spriteSheet());
    }

    @Override
    public Image killedGhostPointsImage(int killedGhostIndex) {
        final RectShort[] numberSprites = spriteSheet().findSpriteSequence(SpriteID.GHOST_NUMBERS);
        return spriteSheet().image(numberSprites[killedGhostIndex]);
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
}
