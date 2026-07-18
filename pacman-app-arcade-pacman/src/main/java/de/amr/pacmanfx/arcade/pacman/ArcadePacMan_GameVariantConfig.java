/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState;
import de.amr.pacmanfx.arcade.pacman.rendering.*;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.d3.Factory3D;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.JsonConfigLoader;
import de.amr.pacmanfx.uilib.UfxImages;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;
import static java.util.Objects.requireNonNull;

/**
 * The Arcade Pac‑Man game variant.
 */
public class ArcadePacMan_GameVariantConfig implements GameVariantConfig, ResourceManager {

    public static GameFlowController createGameFlow() {
        final var gameFlow = new GameFlowController("Arcade Pac-Man Game Flow");
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            gameFlow.addState(gameState.state());
        }
        return gameFlow;
    }

    public static final WorldSettings WORLD_CONFIG = JsonConfigLoader.load(
        GameUI.class.getResource("/de/amr/pacmanfx/ui/world.json"), WorldSettings.class);

    public static final WorldMapColorScheme WORLD_MAP_COLOR_SCHEME = new WorldMapColorScheme(
        ARCADE_BLACK.toString(), ARCADE_BLUE.toString(), ARCADE_PINK.toString(), ARCADE_ROSE.toString()
    );

    private static final Map<Color, Color> BRIGHT_MAZE_COLOR_CHANGES = Map.of(
        Color.valueOf(WORLD_MAP_COLOR_SCHEME.wallStroke()), ARCADE_WHITE,   // wall color change
        Color.valueOf(WORLD_MAP_COLOR_SCHEME.door()), Color.TRANSPARENT // door color change
    );

    private final ResourceBundle textBundle;
    private final AssetMap assets = new AssetMap();
    private final Factory3D factory3D = new ArcadePacMan_Factory3D();

    private GameVariantRenderConfig renderConfig;
    private GameSceneConfig gameSceneConfig;
    private GameSoundEffects soundEffects;

    public ArcadePacMan_GameVariantConfig() {
        textBundle = ResourceBundle.getBundle("de.amr.pacmanfx.arcade.pacman.localized_texts");
    }

    @Override
    public Class<?> resourceRootClass() {
        return ArcadePacMan_GameVariantConfig.class;
    }

    // GameVariant interface

    @Override
    public void init(GameAppContext appContext) {
        Logger.info("Init UI configuration {}", getClass().getSimpleName());
        loadAssets();
        initSound(appContext.ui().sounds());
        gameSceneConfig = new ArcadePacMan_GameSceneConfig(appContext);
        renderConfig = new RenderConfig(assets);
    }

    @Override
    public void dispose() {
        Logger.info("Dispose UI configuration {}:", getClass().getSimpleName());
        assets().dispose();
        gameSceneConfig.dispose();
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    @Override
    public Factory3D factory3D() {
        return factory3D;
    }

    @Override
    public GameSceneConfig gameSceneConfig() {
        return gameSceneConfig;
    }

    @Override
    public GameVariantRenderConfig renderConfig() {
        return renderConfig;
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public TranslationManager translations() {
        return () -> textBundle;
    }

    @Override
    public WorldSettings worldSettings() {
        return WORLD_CONFIG;
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        requireNonNull(worldMap);
        return GlobalAssets.enhanceContrast(worldSettings(), WORLD_MAP_COLOR_SCHEME);
    }

    @Override
    public Image killedGhostPointsImage(int killedGhostIndex) {
        final RectShort[] numberSprites = spriteSheet().findSprites(SpriteID.GHOST_NUMBERS);
        return spriteSheet().image(numberSprites[killedGhostIndex]);
    }

    @Override
    public Optional<GameSoundEffects> optSoundEffects() {
        return Optional.ofNullable(soundEffects);
    }

    @Override
    public Image bonusSymbolImage(int symbolCode) {
        final RectShort[] sprites = spriteSheet().findSprites(SpriteID.BONUS_SYMBOLS);
        return spriteSheet().image(sprites[symbolCode]);
    }

    @Override
    public Image bonusValueImage(int symbolCode) {
        final RectShort[] sprites = spriteSheet().findSprites(SpriteID.BONUS_VALUES);
        return spriteSheet().image(sprites[symbolCode]);
    }

    // private

    private void loadAssets() {
        assets.clear();
        assets.register("app_icon", loadImage("graphics/icons/pacman.png"));
        assets.register("color.game_over_message", ARCADE_RED);
        assets.register("maze.bright", UfxImages.recolorImage(spriteSheet().image(SpriteID.MAP_EMPTY), BRIGHT_MAZE_COLOR_CHANGES));
    }

    private void initSound(SoundManager soundManager) {
        soundManager.setAudioClip    (PacManGameSoundID.BONUS_EATEN,      url("sound/eat_fruit.mp3"));
        soundManager.setAudioClip    (PacManGameSoundID.COIN_INSERTED,    url("sound/credit.wav"));
        soundManager.setAudioClip    (PacManGameSoundID.EXTRA_LIFE,       url("sound/extend.mp3"));
        soundManager.setAudioClip    (PacManGameSoundID.GAME_OVER,        url("sound/common/game-over.mp3"));
        soundManager.setMediaPlayer  (PacManGameSoundID.GAME_READY,       url("sound/game_start.mp3"));
        soundManager.setAudioClip    (PacManGameSoundID.GHOST_EATEN,      url("sound/eat_ghost.mp3"));
        soundManager.setMediaPlayer  (PacManGameSoundID.GHOST_RETURNS,    url("sound/retreating.mp3"));
        soundManager.setMediaPlayer  (PacManGameSoundID.INTERMISSION_1,   url("sound/intermission.mp3"));
        soundManager.setMediaPlayer  (PacManGameSoundID.INTERMISSION_2,   url("sound/intermission.mp3"));
        soundManager.setMediaPlayer  (PacManGameSoundID.INTERMISSION_3,   url("sound/intermission.mp3"));
        soundManager.setAudioClip    (PacManGameSoundID.LEVEL_CHANGED,    url("sound/common/sweep.mp3"));
        soundManager.setMediaPlayer  (PacManGameSoundID.LEVEL_COMPLETE,   url("sound/common/level-complete.mp3"));
        soundManager.setMediaPlayer  (PacManGameSoundID.PAC_MAN_DEATH,    url("sound/pacman_death.wav"));
        soundManager.setAudioClip    (PacManGameSoundID.PAC_MAN_MUNCHING, url("sound/munch.wav"));
        soundManager.setMediaPlayer  (PacManGameSoundID.PAC_MAN_POWER,    url("sound/ghost-turn-to-blue.mp3"));

        soundEffects = new GameSoundEffects(soundManager);
        soundEffects.setMunchingSoundDelay((byte) 9);
        soundEffects.registerSirens(
            url("sound/siren_1.mp3"),
            url("sound/siren_2.mp3"),
            url("sound/siren_3.mp3"),
            url("sound/siren_4.mp3")
        );
        soundEffects.setSirenVolume(0.33f);
    }
}