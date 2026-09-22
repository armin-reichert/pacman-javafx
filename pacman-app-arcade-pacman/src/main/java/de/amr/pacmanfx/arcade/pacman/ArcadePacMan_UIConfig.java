/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.basics.Named;
import de.amr.basics.ui.assets.AssetMap;
import de.amr.basics.ui.assets.ResourceManager;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_RenderConfig;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.pacman.rendering.SpriteID;
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
import de.amr.pacmanfx.uilib.ArcadeColor;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.pacmanfx.ui.sound.SoundManager.SoundEntry.audioClip;
import static de.amr.pacmanfx.ui.sound.SoundManager.SoundEntry.mediaPlayer;

/**
 * The Arcade Pac‑Man game variant.
 */
public class ArcadePacMan_UIConfig implements GameVariantUIConfig {

    private final static ResourceManager RM = () -> ArcadePacMan_UIConfig.class;

    public static final GenericWorldMapColorScheme WORLD_MAP_COLOR_SCHEME = new GenericWorldMapColorScheme(
        ArcadeColor.BLACK.toString(),
        ArcadeColor.BLUE.toString(),
        ArcadeColor.PINK.toString(),
        ArcadeColor.ROSE.toString()
    );

    private static final Map<Color, Color> BRIGHT_MAZE_COLOR_CHANGES = Map.of(
        Color.valueOf(WORLD_MAP_COLOR_SCHEME.wallStroke()), ArcadeColor.WHITE.color(),   // wall color change
        Color.valueOf(WORLD_MAP_COLOR_SCHEME.door()), Color.TRANSPARENT // door color change
    );

    private static final List<SoundManager.SoundEntry> SOUND_ENTRIES = Arrays.asList(
        audioClip   (PacManGameSoundID.BONUS_EATEN,      RM.url("sound/eat_fruit.mp3")),
        audioClip   (PacManGameSoundID.COIN_INSERTED,    RM.url("sound/credit.wav")),
        audioClip   (PacManGameSoundID.EXTRA_LIFE,       RM.url("sound/extend.mp3")),
        audioClip   (PacManGameSoundID.GAME_OVER,        RM.url("sound/common/game-over.mp3")),
        mediaPlayer (PacManGameSoundID.GAME_READY,       RM.url("sound/game_start.mp3")),
        audioClip   (PacManGameSoundID.GHOST_EATEN,      RM.url("sound/eat_ghost.mp3")),
        mediaPlayer (PacManGameSoundID.GHOST_RETURNS,    RM.url("sound/retreating.mp3")),
        mediaPlayer (PacManGameSoundID.INTERMISSION_1,   RM.url("sound/intermission.mp3")),
        mediaPlayer (PacManGameSoundID.INTERMISSION_2,   RM.url("sound/intermission.mp3")),
        mediaPlayer (PacManGameSoundID.INTERMISSION_3,   RM.url("sound/intermission.mp3")),
        audioClip   (PacManGameSoundID.LEVEL_CHANGED,    RM.url("sound/common/sweep.mp3")),
        mediaPlayer (PacManGameSoundID.LEVEL_COMPLETE,   RM.url("sound/common/level-complete.mp3")),
        mediaPlayer (PacManGameSoundID.PAC_MAN_DEATH,    RM.url("sound/pacman_death.wav")),
        audioClip   (PacManGameSoundID.PAC_MAN_MUNCHING, RM.url("sound/munch.wav")),
        mediaPlayer (PacManGameSoundID.PAC_MAN_POWER,    RM.url("sound/ghost-turn-to-blue.mp3"))
    );

    private final TranslationManager translations;
    private final Factory3D factory3D;
    private final GameVariantGameSceneConfig gameSceneConfig;

    private ArcadePacMan_RenderConfig renderConfig;
    private AssetMap assets;
    private GameSoundEffects soundEffects;

    public ArcadePacMan_UIConfig() {
        translations = () -> ResourceBundle.getBundle("de.amr.pacmanfx.arcade.pacman.localized_texts");
        factory3D = new ArcadePacMan_Factory3D();
        gameSceneConfig = new ArcadePacMan_GameSceneConfig();
    }

    @Override
    public Map<Named, Object> createExtensions(GameApp app) {
        return Map.of(Arcade_GameExtensions.ACTIONS, new Arcade_Actions());
    }

    @Override
    public void load(GameApp app) {
        assets = new AssetMap();
        assets.addAsset("app_icon", RM.loadImage("graphics/icons/pacman.png"));
        assets.addAsset("color.game_over_message", ArcadeColor.RED.color());
        assets.addAsset("maze.bright", createBrightEmptyMap());
        assets.freeze();

        loadSounds(app.ui().soundManager());
        renderConfig = new ArcadePacMan_RenderConfig(assets);
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
            assets = null;
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

    // private

    private void loadSounds(SoundManager soundManager) {
        for (SoundManager.SoundEntry entry : SOUND_ENTRIES) {
            soundManager.add(entry);
        }
        soundEffects = new GameSoundEffects(soundManager);
        soundEffects.setMunchingSoundDelay((byte) 9);
        soundEffects.registerSirens(
            RM.url("sound/siren_1.mp3"),
            RM.url("sound/siren_2.mp3"),
            RM.url("sound/siren_3.mp3"),
            RM.url("sound/siren_4.mp3")
        );
        soundEffects.setSirenVolume(0.33f);
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

    private Image createBrightEmptyMap() {
        return Ufx.recolorImage(
            ArcadePacMan_SpriteSheet.instance().image(SpriteID.MAP_EMPTY),
            BRIGHT_MAZE_COLOR_CHANGES);
    }
}