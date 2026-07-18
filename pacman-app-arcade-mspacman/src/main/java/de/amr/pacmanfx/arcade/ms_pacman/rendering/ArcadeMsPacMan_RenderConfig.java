/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;


import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.*;
import de.amr.pacmanfx.arcade.pacman.rendering.Arcade_BootScene2D_Renderer;
import de.amr.pacmanfx.arcade.pacman.rendering.Arcade_PlayScene2D_Renderer;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_BootScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.core.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.WorldMapConfigKey;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.UfxImages;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Map;

import static de.amr.pacmanfx.core.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_RenderConfig implements GameVariantRenderConfig {

    private static final Rectangle2D BOOT_SCENE_SPRITES = new Rectangle2D(380, 0, 204, 208);

    /** Colors used by the six Ms. Pac-Man Arcade maps. */
    private static final WorldMapColorScheme[] MAP_COLOR_SCHEMES = {
        new WorldMapColorScheme("ffb7ae", "ff0000", "fcb5ff", "dedeff"),
        new WorldMapColorScheme("47b7ff", "dedeff", "fcb5ff", "ffff00"),
        new WorldMapColorScheme("de9751", "dedeff", "fcb5ff", "ff0000"),
        new WorldMapColorScheme("2121ff", "ffb751", "fcb5ff", "dedeff"),
        new WorldMapColorScheme("ffb7ff", "ffff00", "fcb5ff", "00ffff"),
        new WorldMapColorScheme("ffb7ae", "ff0000", "fcb5ff", "dedeff")
    };

    private final AssetMap assets;

    public ArcadeMsPacMan_RenderConfig(AssetMap assets) {
        this.assets = assets;
        createBrightMazeImages();
    }

    private void createBrightMazeImages() {
        for (int i = 0; i < MAP_COLOR_SCHEMES.length; ++i) {
            assets.register("maze.bright.%d".formatted(i), createBrightMazeImage(i));
        }
    }

    // Creates the maze image used in the flash animation at the end of each level
    private Image createBrightMazeImage(int index) {
        final RectShort mazeSprite = spriteSheet().findSprites(SpriteID.EMPTY_MAPS)[index];
        final Image mazeImage = spriteSheet().image(mazeSprite);
        final WorldMapColorScheme colorScheme = MAP_COLOR_SCHEMES[index];
        final Map<Color, Color> colorChanges = Map.of(
            Color.valueOf(colorScheme.wallStroke()), ARCADE_WHITE,
            Color.valueOf(colorScheme.door()), Color.TRANSPARENT
        );
        return UfxImages.recolorImage(mazeImage, colorChanges);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap, WorldSettings worldSettings) {
        requireNonNull(worldMap);
        final int index = worldMap.getConfigValue(WorldMapConfigKey.COLOR_MAP_INDEX);
        return GlobalAssets.enhanceContrast(worldSettings, MAP_COLOR_SCHEMES[index]);
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(AbstractGameScene2D gameScene2D, Canvas canvas) {
        requireNonNull(canvas);
        requireNonNull(gameScene2D);
        final GameScene2D_Renderer renderer = switch (gameScene2D) {
            case Arcade_BootScene2D ignored -> new Arcade_BootScene2D_Renderer(gameScene2D, canvas, spriteSheet(), BOOT_SCENE_SPRITES);
            case ArcadeMsPacMan_IntroScene ignored -> new ArcadeMsPacMan_IntroScene_Renderer(this, gameScene2D, canvas);
            case ArcadeMsPacMan_StartScene ignored -> new ArcadeMsPacMan_StartScene_Renderer(this, gameScene2D, canvas);
            case Arcade_PlayScene2D ignored -> new Arcade_PlayScene2D_Renderer(gameScene2D, canvas, spriteSheet());
            case ArcadeMsPacMan_CutScene1 ignored -> new ArcadeMsPacMan_CutScene1_Renderer(this, gameScene2D, canvas);
            case ArcadeMsPacMan_CutScene2 ignored -> new ArcadeMsPacMan_CutScene2_Renderer(this, gameScene2D, canvas);
            case ArcadeMsPacMan_CutScene3 ignored -> new ArcadeMsPacMan_CutScene3_Renderer(this, gameScene2D, canvas);
            default -> throw new IllegalStateException("Illegal game scene: " + gameScene2D);
        };
        return gameScene2D.configureRenderer(renderer);
    }

    @Override
    public ArcadeMsPacMan_GameLevelRenderer createGameLevelRenderer(Canvas canvas) {
        requireNonNull(canvas);
        return new ArcadeMsPacMan_GameLevelRenderer(canvas, assets);
    }

    @Override
    public HeadsUpDisplay_Renderer createHUDRenderer(AbstractGameScene2D gameScene2D, Canvas canvas) {
        requireNonNull(canvas);
        requireNonNull(gameScene2D);
        final var hudRenderer = new ArcadeMsPacMan_HeadsUpDisplayRenderer(canvas);
        hudRenderer.setImageSmoothing(true);
        gameScene2D.configureRenderer(hudRenderer);
        return hudRenderer;
    }

    @Override
    public ActorRenderer createActorRenderer(Canvas canvas) {
        requireNonNull(canvas);
        final var actorRenderer = new ArcadeMsPacMan_ActorRenderer(canvas);
        actorRenderer.setImageSmoothing(true);
        return actorRenderer;
    }

    @Override
    public ArcadeMsPacMan_GhostAnimations createGhostAnimations(SpriteAnimationContainer container, byte personality) {
        requireValidGhostPersonality(personality);
        return new ArcadeMsPacMan_GhostAnimations(container, personality);
    }

    @Override
    public ArcadeMsPacMan_PacAnimations createPacAnimations(SpriteAnimationContainer container) {
        return new ArcadeMsPacMan_PacAnimations(container);
    }

    @Override
    public Ghost createAnimatedGhost(SpriteAnimationContainer container, byte personality) {
        final Ghost ghost = ArcadeMsPacMan_ActorFactory.createGhost(personality);
        ghost.setAnimations(createGhostAnimations(container, personality));
        ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
        return ghost;
    }
}
