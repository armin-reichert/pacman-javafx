/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_MapSelector;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.*;
import de.amr.pacmanfx.arcade.pacman.ArcadePacManGameVariant;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.WorldMapConfigKey;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.d3.Factory3D;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
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

import static de.amr.pacmanfx.core.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_RED;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacManGameVariant implements GameVariantConfig, ResourceManager {

    public static GameFlowController createGameFlow() {
        final var gameFlow = new GameFlowController("Arcade Ms. Pac-Man Game Flow");
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            gameFlow.addState(gameState.state());
        }
        return gameFlow;
    }

    @Override
    public Class<?> resourceRootClass() {
        return ArcadeMsPacManGameVariant.class;
    }

    private final ResourceBundle textBundle;
    private final AssetMap assets = new AssetMap();
    private final Factory3D factory3D = new ArcadeMsPacMan_Factory3D();
    private GameSceneConfig gameSceneConfig;
    private GameVariantRenderConfig renderConfig;
    private GameSoundEffects soundEffects;

    public ArcadeMsPacManGameVariant() {
        textBundle = ResourceBundle.getBundle("de.amr.pacmanfx.arcade.ms_pacman.localized_texts");
    }

    @Override
    public TranslationManager translations() {
        return () -> textBundle;
    }

    @Override
    public void init(GameAppContext appContext) {
        Logger.info("Init UI configuration {}", getClass().getSimpleName());
        loadAssets();
        initSound(appContext.ui().sounds());
        gameSceneConfig = new ArcadeMsPacMan_GameSceneConfig(appContext);
        renderConfig = new RenderConfig(assets);
    }

    @Override
    public void dispose() {
        Logger.info("Dispose UI configuration {}:", getClass().getSimpleName());
        assets().dispose();
        gameSceneConfig.dispose();
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
    public AssetMap assets() {
        return assets;
    }

    @Override
    public Factory3D factory3D() {
        return factory3D;
    }

    private void loadAssets() {
        assets.clear();
        assets.register("app_icon", loadImage("graphics/icons/mspacman.png"));
        assets.register("logo.midway", loadImage("graphics/midway_logo.png"));
        createBrightMazeImages();
        assets.register("color.game_over_message", ARCADE_RED);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public WorldSettings worldSettings() {
        return ArcadePacManGameVariant.WORLD_CONFIG;
    }

    @Override
    public Optional<GameSoundEffects> optSoundEffects() {
        return Optional.ofNullable(soundEffects);
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        requireNonNull(worldMap);
        final int index = worldMap.getConfigValue(WorldMapConfigKey.COLOR_MAP_INDEX);
        return GlobalAssets.enhanceContrast(
            worldSettings(),
            ArcadeMsPacMan_MapSelector.MAP_COLOR_SCHEMES[index]);
    }

    public Ghost createAnimatedGhost(SpriteAnimationContainer container, byte personality) {
        final Ghost ghost = ArcadeMsPacMan_ActorFactory.createGhost(personality);
        ghost.setAnimations(createGhostAnimations(container, personality));
        ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
        return ghost;
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
    public Image killedGhostPointsImage(int killedGhostIndex) {
        final RectShort[] numberSprites = spriteSheet().findSprites(SpriteID.GHOST_NUMBERS);
        return spriteSheet().image(numberSprites[killedGhostIndex]);
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

    // Private

    private void initSound(SoundManager soundManager) {
        soundManager.setMediaPlayer(PacManGameSoundID.BONUS_ACTIVE,      url("sound/Fruit_Bounce.mp3"));
        soundManager.setAudioClip(PacManGameSoundID.BONUS_EATEN,      url("sound/Fruit.mp3"));
        soundManager.setAudioClip(PacManGameSoundID.COIN_INSERTED,    url("sound/credit.wav"));
        soundManager.setAudioClip(PacManGameSoundID.EXTRA_LIFE,       url("sound/ExtraLife.mp3"));
        soundManager.setMediaPlayer(PacManGameSoundID.GAME_OVER,         url("sound/game-over.mp3"));
        soundManager.setMediaPlayer(PacManGameSoundID.GAME_READY,        url("sound/Start.mp3"));
        soundManager.setAudioClip(PacManGameSoundID.GHOST_EATEN,      url("sound/Ghost.mp3"));
        soundManager.setMediaPlayer(PacManGameSoundID.GHOST_RETURNS,     url("sound/GhostEyes.mp3"));
        soundManager.setMediaPlayer(PacManGameSoundID.INTERMISSION_1,    url("sound/Act_1_They_Meet.mp3"));
        soundManager.setMediaPlayer(PacManGameSoundID.INTERMISSION_2,    url("sound/Act_2_The_Chase.mp3"));
        soundManager.setMediaPlayer(PacManGameSoundID.INTERMISSION_3,    url("sound/Act_3_Junior.mp3"));
        soundManager.setAudioClip(PacManGameSoundID.LEVEL_CHANGED,    url("sound/sweep.mp3"));
        soundManager.setMediaPlayer(PacManGameSoundID.LEVEL_COMPLETE,    url("sound/level-complete.mp3"));
        soundManager.setMediaPlayer(PacManGameSoundID.PAC_MAN_DEATH,     url("sound/Died.mp3"));
        soundManager.setAudioClip(PacManGameSoundID.PAC_MAN_MUNCHING, url("sound/munch.wav"));
        soundManager.setMediaPlayer(PacManGameSoundID.PAC_MAN_POWER,     url("sound/ScaredGhost.mp3"));

        soundEffects = new GameSoundEffects(soundManager);

        soundEffects.registerSirens(
            url("sound/GhostNoise1.wav"),
            url("sound/GhostNoise2.wav"),
            url("sound/GhostNoise3.wav"),
            url("sound/GhostNoise4.wav")
        );

        soundEffects.setMunchingSoundDelay((byte) 0);
        soundEffects.setSirenVolume(0.33f);
    }

    private void createBrightMazeImages() {
        for (int i = 0; i < ArcadeMsPacMan_MapSelector.MAP_COLOR_SCHEMES.length; ++i) {
            assets.register("maze.bright.%d".formatted(i), createBrightMazeImage(i));
        }
    }

    // Creates the maze image used in the flash animation at the end of each level
    private Image createBrightMazeImage(int index) {
        final RectShort mazeSprite = spriteSheet().findSprites(SpriteID.EMPTY_MAPS)[index];
        final Image mazeImage = spriteSheet().image(mazeSprite);
        final WorldMapColorScheme colorScheme = ArcadeMsPacMan_MapSelector.MAP_COLOR_SCHEMES[index];
        final Map<Color, Color> colorChanges = Map.of(
            Color.valueOf(colorScheme.wallStroke()), ARCADE_WHITE,
            Color.valueOf(colorScheme.door()), Color.TRANSPARENT
        );
        return UfxImages.recolorImage(mazeImage, colorChanges);
    }
}