/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_RenderConfig;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameVariantConfig;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.d3.Factory3D;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.scene.image.Image;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_RED;

public class ArcadeMsPacMan_GameVariantConfig implements GameVariantConfig, ResourceManager {

    public static GameFlowController createGameFlow() {
        final var gameFlow = new GameFlowController("Arcade Ms. Pac-Man Game Flow");
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            gameFlow.addState(gameState.state());
        }
        return gameFlow;
    }

    @Override
    public Class<?> resourceRootClass() {
        return ArcadeMsPacMan_GameVariantConfig.class;
    }

    private final ResourceBundle textBundle;
    private final AssetMap assets = new AssetMap();
    private final Factory3D factory3D = new ArcadeMsPacMan_Factory3D();
    private GameSceneConfig gameSceneConfig;
    private ArcadeMsPacMan_RenderConfig renderConfig;
    private GameSoundEffects soundEffects;

    public ArcadeMsPacMan_GameVariantConfig() {
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
        renderConfig = new ArcadeMsPacMan_RenderConfig(assets);
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
        assets.register("color.game_over_message", ARCADE_RED);
    }

    @Override
    public WorldSettings worldSettings() {
        return ArcadePacMan_GameVariantConfig.WORLD_CONFIG;
    }

    @Override
    public Optional<GameSoundEffects> optSoundEffects() {
        return Optional.ofNullable(soundEffects);
    }

    @Override
    public Image bonusSymbolImage(int symbolCode) {
        final RectShort[] sprites = renderConfig.spriteSheet().findSprites(SpriteID.BONUS_SYMBOLS);
        return renderConfig.spriteSheet().image(sprites[symbolCode]);
    }

    @Override
    public Image bonusValueImage(int symbolCode) {
        final RectShort[] sprites = renderConfig.spriteSheet().findSprites(SpriteID.BONUS_VALUES);
        return renderConfig.spriteSheet().image(sprites[symbolCode]);
    }

    @Override
    public Image killedGhostPointsImage(int killedGhostIndex) {
        final RectShort[] numberSprites = renderConfig.spriteSheet().findSprites(SpriteID.GHOST_NUMBERS);
        return renderConfig.spriteSheet().image(numberSprites[killedGhostIndex]);
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

}