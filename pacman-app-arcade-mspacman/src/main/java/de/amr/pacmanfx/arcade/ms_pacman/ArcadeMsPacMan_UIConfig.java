/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.basics.Named;
import de.amr.basics.math.RectShort;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_RenderConfig;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.arcade.pacman.gamestate.Arcade_GameState;
import de.amr.pacmanfx.core.gamestate.GameFlow;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.ui.action.core.GameApp;
import de.amr.pacmanfx.ui.gamescene.common.GameVariantGameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.d3.Factory3D;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.basics.ui.assets.AssetMap;
import de.amr.basics.ui.assets.ResourceManager;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.pacmanfx.uilib.ArcadeColor;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.pacmanfx.ui.sound.SoundManager.SoundEntry.audioClip;
import static de.amr.pacmanfx.ui.sound.SoundManager.SoundEntry.mediaPlayer;

public class ArcadeMsPacMan_UIConfig implements GameVariantUIConfig {

    /** Colors used by the six Ms. Pac-Man Arcade maps. */
    public static final GenericWorldMapColorScheme[] MAP_COLOR_SCHEMES = {
        new GenericWorldMapColorScheme("ffb7ae", "ff0000", "fcb5ff", "dedeff"),
        new GenericWorldMapColorScheme("47b7ff", "dedeff", "fcb5ff", "ffff00"),
        new GenericWorldMapColorScheme("de9751", "dedeff", "fcb5ff", "ff0000"),
        new GenericWorldMapColorScheme("2121ff", "ffb751", "fcb5ff", "dedeff"),
        new GenericWorldMapColorScheme("ffb7ff", "ffff00", "fcb5ff", "00ffff"),
        new GenericWorldMapColorScheme("ffb7ae", "ff0000", "fcb5ff", "dedeff")
    };

    private static final ResourceManager RM = () -> ArcadeMsPacMan_UIConfig.class;

    private static final List<SoundManager.SoundEntry> SOUND_ENTRIES = Arrays.asList(
        mediaPlayer(PacManGameSoundID.BONUS_ACTIVE, RM.url("sound/Fruit_Bounce.mp3")),
        audioClip(PacManGameSoundID.BONUS_EATEN, RM.url("sound/Fruit.mp3")),
        audioClip(PacManGameSoundID.COIN_INSERTED, RM.url("sound/credit.wav")),
        audioClip(PacManGameSoundID.EXTRA_LIFE, RM.url("sound/ExtraLife.mp3")),
        mediaPlayer(PacManGameSoundID.GAME_OVER, RM.url("sound/game-over.mp3")),
        mediaPlayer(PacManGameSoundID.GAME_READY, RM.url("sound/Start.mp3")),
        audioClip(PacManGameSoundID.GHOST_EATEN, RM.url("sound/Ghost.mp3")),
        mediaPlayer(PacManGameSoundID.GHOST_RETURNS, RM.url("sound/GhostEyes.mp3")),
        mediaPlayer(PacManGameSoundID.INTERMISSION_1, RM.url("sound/Act_1_They_Meet.mp3")),
        mediaPlayer(PacManGameSoundID.INTERMISSION_2, RM.url("sound/Act_2_The_Chase.mp3")),
        mediaPlayer(PacManGameSoundID.INTERMISSION_3, RM.url("sound/Act_3_Junior.mp3")),
        audioClip(PacManGameSoundID.LEVEL_CHANGED, RM.url("sound/sweep.mp3")),
        mediaPlayer(PacManGameSoundID.LEVEL_COMPLETE, RM.url("sound/level-complete.mp3")),
        mediaPlayer(PacManGameSoundID.PAC_MAN_DEATH, RM.url("sound/Died.mp3")),
        audioClip(PacManGameSoundID.PAC_MAN_MUNCHING, RM.url("sound/munch.wav")),
        mediaPlayer(PacManGameSoundID.PAC_MAN_POWER, RM.url("sound/ScaredGhost.mp3"))
    );

    /**
     * Used in cartridge.
     *
     * @return the game flow controller for this game variant
     */
    public static GameFlow createGameFlow() {
        final var gameFlow = new GameFlow("Arcade Ms. Pac-Man Game Flow");
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            gameFlow.addState(gameState.state());
        }
        return gameFlow;
    }

    private final AssetMap assets;
    private final Factory3D factory3D;
    private final GameVariantGameSceneConfig gameSceneConfig;
    private final TranslationManager translations;

    private ArcadeMsPacMan_RenderConfig renderConfig;
    private GameSoundEffects soundEffects;

    public ArcadeMsPacMan_UIConfig() {
        assets = new AssetMap();
        factory3D = new ArcadeMsPacMan_Factory3D();
        gameSceneConfig = new ArcadeMsPacMan_GameSceneConfig();
        translations = () -> ResourceBundle.getBundle("de.amr.pacmanfx.arcade.ms_pacman.localized_texts");
    }

    @Override
    public void load(GameApp app) {
        assets.addAsset("app_icon",    RM.loadImage("graphics/icons/mspacman.png"));
        assets.addAsset("logo.midway", RM.loadImage("graphics/midway_logo.png"));
        assets.addAsset("color.game_over_message", ArcadeColor.RED.color());
        for (int i = 0; i < MAP_COLOR_SCHEMES.length; ++i) {
            assets.addAsset("maze.bright.%d".formatted(i), createBrightMazeImage(i));
        }
        assets.freeze();

        loadSounds(app.ui().soundManager());
        renderConfig = new ArcadeMsPacMan_RenderConfig(assets);
    }

    @Override
    public Map<Named, Object> createExtensions(GameApp app) {
        return Map.of(Arcade_GameExtensions.ACTIONS, new Arcade_Actions());
    }

    @Override
    public void unload(GameApp app) {
        unloadSounds(app.ui().soundManager());
        dispose();
    }

    @Override
    public void dispose() {
        Logger.info("Dispose game variant configuration {}:", getClass().getSimpleName());

        Logger.info("Dispose game scene configuration");
        gameSceneConfig.dispose();

        if (assets != null) {
            Logger.info("Dispose assets");
            assets.dispose();
        }
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
    public GameVariantGameSceneConfig gameSceneConfig() {
        return gameSceneConfig;
    }

    @Override
    public Optional<GameSoundEffects> optSoundEffects() {
        return Optional.ofNullable(soundEffects);
    }

    @Override
    public GameVariantRenderConfig renderConfig() {
        return renderConfig;
    }

    @Override
    public TranslationManager translations() {
        return translations;
    }

    @Override
    public WorldSettings worldSettings() {
        return WorldSettings.DEFAULT_SETTINGS;
    }

    // Private

    private void loadSounds(SoundManager soundManager) {
        soundManager.addMediaPlayer(PacManGameSoundID.BONUS_ACTIVE, RM.url("sound/Fruit_Bounce.mp3"));
        soundManager.addAudioClip(PacManGameSoundID.BONUS_EATEN, RM.url("sound/Fruit.mp3"));
        soundManager.addAudioClip(PacManGameSoundID.COIN_INSERTED, RM.url("sound/credit.wav"));
        soundManager.addAudioClip(PacManGameSoundID.EXTRA_LIFE, RM.url("sound/ExtraLife.mp3"));
        soundManager.addMediaPlayer(PacManGameSoundID.GAME_OVER, RM.url("sound/game-over.mp3"));
        soundManager.addMediaPlayer(PacManGameSoundID.GAME_READY, RM.url("sound/Start.mp3"));
        soundManager.addAudioClip(PacManGameSoundID.GHOST_EATEN, RM.url("sound/Ghost.mp3"));
        soundManager.addMediaPlayer(PacManGameSoundID.GHOST_RETURNS, RM.url("sound/GhostEyes.mp3"));
        soundManager.addMediaPlayer(PacManGameSoundID.INTERMISSION_1, RM.url("sound/Act_1_They_Meet.mp3"));
        soundManager.addMediaPlayer(PacManGameSoundID.INTERMISSION_2, RM.url("sound/Act_2_The_Chase.mp3"));
        soundManager.addMediaPlayer(PacManGameSoundID.INTERMISSION_3, RM.url("sound/Act_3_Junior.mp3"));
        soundManager.addAudioClip(PacManGameSoundID.LEVEL_CHANGED, RM.url("sound/sweep.mp3"));
        soundManager.addMediaPlayer(PacManGameSoundID.LEVEL_COMPLETE, RM.url("sound/level-complete.mp3"));
        soundManager.addMediaPlayer(PacManGameSoundID.PAC_MAN_DEATH, RM.url("sound/Died.mp3"));
        soundManager.addAudioClip(PacManGameSoundID.PAC_MAN_MUNCHING, RM.url("sound/munch.wav"));
        soundManager.addMediaPlayer(PacManGameSoundID.PAC_MAN_POWER, RM.url("sound/ScaredGhost.mp3"));

        soundEffects = new GameSoundEffects(soundManager);

        soundEffects.registerSirens(
            RM.url("sound/GhostNoise1.wav"),
            RM.url("sound/GhostNoise2.wav"),
            RM.url("sound/GhostNoise3.wav"),
            RM.url("sound/GhostNoise4.wav")
        );
    }

    private void unloadSounds(SoundManager soundManager) {
        Logger.info("Unload sounds");
        for (SoundManager.SoundEntry entry : SOUND_ENTRIES) {
            soundManager.remove(entry);
        }
        if (soundEffects != null) {
            soundEffects.dispose();
            soundEffects = null;
        }
    }

    // Creates the maze image used in the flash animation at the end of each level
    private Image createBrightMazeImage(int index) {
        final var spriteSheet = ArcadeMsPacMan_SpriteSheet.instance();
        final RectShort mazeSprite = spriteSheet.findSpriteSequence(SpriteID.EMPTY_MAPS)[index];
        final Image mazeImage = spriteSheet.image(mazeSprite);
        final GenericWorldMapColorScheme colorScheme = ArcadeMsPacMan_UIConfig.MAP_COLOR_SCHEMES[index];
        final Map<Color, Color> colorChanges = Map.of(
            Color.valueOf(colorScheme.wallStroke()), ArcadeColor.WHITE.color(),
            Color.valueOf(colorScheme.door()), Color.TRANSPARENT
        );
        return Ufx.recolorImage(mazeImage, colorChanges);
    }
}