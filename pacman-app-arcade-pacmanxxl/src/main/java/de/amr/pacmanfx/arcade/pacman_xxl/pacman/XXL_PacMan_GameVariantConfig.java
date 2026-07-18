/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.pacman;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameVariantConfig;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState;
import de.amr.pacmanfx.arcade.pacman.rendering.SpriteID;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.WorldMapConfigKey;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.d3.DefaultFactory3D;
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

public final class XXL_PacMan_GameVariantConfig implements GameVariantConfig, ResourceManager {

    public static GameFlowController createGameFlow() {
        final var gameFlow = new GameFlowController("Arcade Pac-Man XXL Game Flow");
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            gameFlow.addState(gameState.state());
        }
        return gameFlow;
    }

    private static final ResourceManager ARCADE_PACMAN_RES = () -> ArcadePacMan_GameVariantConfig.class;

    private static final String XXL_PATH = "/de/amr/pacmanfx/arcade/pacman_xxl/";
    private static final String XXL_PKG = "de.amr.pacmanfx.arcade.pacman_xxl.";

    private final ResourceBundle textBundle;
    private final AssetMap assets = new AssetMap();
    private final Factory3D factory3D = new DefaultFactory3D();

    private GameSceneConfig gameSceneConfig;
    private XXL_PacMan_RenderConfig renderConfig;
    private GameSoundEffects soundEffects;

    private GameAppContext appContext;

    public XXL_PacMan_GameVariantConfig() {
        textBundle = ResourceBundle.getBundle(XXL_PKG + "localized_texts_pacman");
    }

    @Override
    public TranslationManager translations() {
        return () -> textBundle;
    }

    @Override
    public Class<?> resourceRootClass() {
        return getClass();
    }

    @Override
    public void init(GameAppContext appContext) {
        this.appContext = appContext;

        Logger.info("Load assets of UI configuration {}", getClass().getSimpleName());
        loadAssets();

        gameSceneConfig = new XXL_PacMan_GameSceneConfig(appContext);
        renderConfig = new XXL_PacMan_RenderConfig(assets);

        Logger.info("Register sounds and effects of UI configuration {}", getClass().getSimpleName());
        registerSounds(appContext.ui().sounds());
        soundEffects = new GameSoundEffects(appContext.ui().sounds());
        initSoundEffects();
    }

    @Override
    public void dispose() {
        Logger.info("Dispose UI configuration {}:", getClass().getSimpleName());

        gameSceneConfig.dispose();

        Logger.info("Dispose assets of UI configuration {}", getClass().getSimpleName());
        assets().dispose();

        if (appContext != null) {
            Logger.info("Unregister sounds and effects of UI configuration {}", getClass().getSimpleName());
            unregisterSounds(appContext.ui().sounds());
            soundEffects.dispose();
        }
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

    @Override
    public WorldSettings worldSettings() {
        return ArcadePacMan_GameVariantConfig.WORLD_CONFIG;
    }

    @Override
    public Image killedGhostPointsImage(int killedGhostIndex) {
        final RectShort[] numberSprites = renderConfig.spriteSheet().findSprites(SpriteID.GHOST_NUMBERS);
        return renderConfig.spriteSheet().image(numberSprites[killedGhostIndex]);
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
    public Optional<GameSoundEffects> optSoundEffects() {
        return Optional.ofNullable(soundEffects);
    }

    // -----

    private void loadAssets() {
        assets.clear();
        assets.register("app_icon", loadImage(XXL_PATH + "graphics/icons/pacman.png"));
        assets.register("color.game_over_message", ARCADE_RED);
    }

    private void registerSounds(SoundManager sounds) {
        sounds.setAudioClip    (PacManGameSoundID.BONUS_EATEN,           ARCADE_PACMAN_RES.url("sound/eat_fruit.mp3"));
        sounds.setAudioClip    (PacManGameSoundID.COIN_INSERTED,         ARCADE_PACMAN_RES.url("sound/credit.wav"));
        sounds.setAudioClip    (PacManGameSoundID.ENERGIZER_EXPLOSION_1, url(XXL_PATH + "sound/explosion1.mp3"));
        sounds.setAudioClip    (PacManGameSoundID.ENERGIZER_EXPLOSION_2, url(XXL_PATH + "sound/explosion2.mp3"));
        sounds.setAudioClip    (PacManGameSoundID.EXTRA_LIFE,            ARCADE_PACMAN_RES.url("sound/extend.mp3"));
        sounds.setAudioClip    (PacManGameSoundID.GAME_OVER,             ARCADE_PACMAN_RES.url("sound/common/game-over.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.GAME_READY,            ARCADE_PACMAN_RES.url("sound/game_start.mp3"));
        sounds.setAudioClip    (PacManGameSoundID.GHOST_EATEN,           ARCADE_PACMAN_RES.url("sound/eat_ghost.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.GHOST_RETURNS,         ARCADE_PACMAN_RES.url("sound/retreating.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.INTERMISSION_1,        ARCADE_PACMAN_RES.url("sound/intermission.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.INTERMISSION_2,        ARCADE_PACMAN_RES.url("sound/intermission.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.INTERMISSION_3,        ARCADE_PACMAN_RES.url("sound/intermission.mp3"));
        sounds.setAudioClip    (PacManGameSoundID.LEVEL_CHANGED,         ARCADE_PACMAN_RES.url("sound/common/sweep.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.LEVEL_COMPLETE,        ARCADE_PACMAN_RES.url("sound/common/level-complete.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.PAC_MAN_DEATH,         ARCADE_PACMAN_RES.url("sound/pacman_death.wav"));
        sounds.setAudioClip    (PacManGameSoundID.PAC_MAN_MUNCHING,      ARCADE_PACMAN_RES.url("sound/munch.wav"));
        sounds.setMediaPlayer  (PacManGameSoundID.PAC_MAN_POWER,         ARCADE_PACMAN_RES.url("sound/ghost-turn-to-blue.mp3"));
    }

    private void unregisterSounds(SoundManager sounds) {
        sounds.unregisterSound(PacManGameSoundID.BONUS_EATEN);
        sounds.unregisterSound(PacManGameSoundID.COIN_INSERTED);
        sounds.unregisterSound(PacManGameSoundID.ENERGIZER_EXPLOSION_1);
        sounds.unregisterSound(PacManGameSoundID.ENERGIZER_EXPLOSION_2);
        sounds.unregisterSound(PacManGameSoundID.EXTRA_LIFE);
        sounds.unregisterSound(PacManGameSoundID.GAME_OVER);
        sounds.unregisterSound(PacManGameSoundID.GAME_READY);
        sounds.unregisterSound(PacManGameSoundID.GHOST_EATEN);
        sounds.unregisterSound(PacManGameSoundID.GHOST_RETURNS);
        sounds.unregisterSound(PacManGameSoundID.INTERMISSION_1);
        sounds.unregisterSound(PacManGameSoundID.INTERMISSION_2);
        sounds.unregisterSound(PacManGameSoundID.INTERMISSION_3);
        sounds.unregisterSound(PacManGameSoundID.LEVEL_CHANGED);
        sounds.unregisterSound(PacManGameSoundID.LEVEL_COMPLETE);
        sounds.unregisterSound(PacManGameSoundID.PAC_MAN_DEATH);
        sounds.unregisterSound(PacManGameSoundID.PAC_MAN_MUNCHING);
        sounds.unregisterSound(PacManGameSoundID.PAC_MAN_POWER);
    }

    private void initSoundEffects() {
        soundEffects.setMunchingSoundDelay((byte) 9);

        soundEffects.registerSirens(
            ARCADE_PACMAN_RES.url("sound/siren_1.mp3"),
            ARCADE_PACMAN_RES.url("sound/siren_2.mp3"),
            ARCADE_PACMAN_RES.url("sound/siren_3.mp3"),
            ARCADE_PACMAN_RES.url("sound/siren_4.mp3")
        );
        soundEffects.setSirenVolume(0.33f);
    }
}