/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_GhostAnimationManager;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_PacAnimationManager;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.*;
import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_BootScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_PlayScene2D;
import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.controller.teststates.CutScenesTestState;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameUI_Implementation;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.DefaultSoundManager;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.MsPacMan3D;
import de.amr.pacmanfx.uilib.model3D.MsPacManBody;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Stream;

import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class PacManXXL_MsPacMan_UIConfig implements GameUI_Config {

    private static final String ASSET_NAMESPACE = "ms_pacman_xxl";

    private static final ResourceManager RES_GAME_UI           = () -> GameUI_Implementation.class;
    private static final ResourceManager RES_ARCADE_MS_PAC_MAN = () -> ArcadeMsPacMan_UIConfig.class;
    private static final ResourceManager RES_MS_PAC_MAN_XXL    = () -> PacManXXL_MsPacMan_UIConfig.class;

    private final GameUI ui;
    private final Map<String, GameScene> scenesByID = new HashMap<>();
    private final DefaultSoundManager soundManager = new DefaultSoundManager();
    private final ArcadeMsPacMan_SpriteSheet spriteSheet;

    public PacManXXL_MsPacMan_UIConfig(GameUI ui) {
        this.ui = requireNonNull(ui);
        spriteSheet = new ArcadeMsPacMan_SpriteSheet(RES_ARCADE_MS_PAC_MAN.loadImage("graphics/mspacman_spritesheet.png"));
    }

    @Override
    public GameUI theUI() {
        return ui;
    }

    public void storeAssets(AssetStorage assets) {
        storeLocalAssetValue(assets, "spritesheet", spriteSheet);
        storeLocalAssetValue(assets, "app_icon", RES_ARCADE_MS_PAC_MAN.loadImage("graphics/icons/mspacman.png"));
        storeLocalAssetValue(assets, "startpage.image1", RES_ARCADE_MS_PAC_MAN.loadImage("graphics/f1.jpg"));
        storeLocalAssetValue(assets, "startpage.image2", RES_ARCADE_MS_PAC_MAN.loadImage("graphics/f2.jpg"));
        storeLocalAssetValue(assets, "flashing_mazes", RES_ARCADE_MS_PAC_MAN.loadImage("graphics/mazes_flashing.png"));
        storeLocalAssetValue(assets, "logo.midway", RES_ARCADE_MS_PAC_MAN.loadImage("graphics/midway_logo.png"));
        storeLocalAssetValue(assets, "color.game_over_message", ARCADE_RED);

        RectShort[] symbolSprites = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS);
        RectShort[] valueSprites  = spriteSheet.spriteSeq(SpriteID.BONUS_VALUES);
        for (byte symbol = 0; symbol <= 6; ++symbol) {
            storeLocalAssetValue(assets, "bonus_symbol_" + symbol, spriteSheet.image(symbolSprites[symbol]));
            storeLocalAssetValue(assets, "bonus_value_"  + symbol, spriteSheet.image(valueSprites[symbol]));
        }

        storeLocalAssetValue(assets, "pac.color.head",           ARCADE_YELLOW);
        storeLocalAssetValue(assets, "pac.color.eyes",           Color.grayRgb(33));
        storeLocalAssetValue(assets, "pac.color.palate",         ARCADE_BROWN);
        storeLocalAssetValue(assets, "pac.color.boobs",          ARCADE_YELLOW.deriveColor(0, 1.0, 0.96, 1.0));
        storeLocalAssetValue(assets, "pac.color.hairbow",        ARCADE_RED);
        storeLocalAssetValue(assets, "pac.color.hairbow.pearls", ARCADE_BLUE);

        RectShort[] numberSprites = spriteSheet.spriteSeq(SpriteID.GHOST_NUMBERS);
        storeLocalAssetValue(assets, "ghost_points_0", spriteSheet.image(numberSprites[0]));
        storeLocalAssetValue(assets, "ghost_points_1", spriteSheet.image(numberSprites[1]));
        storeLocalAssetValue(assets, "ghost_points_2", spriteSheet.image(numberSprites[2]));
        storeLocalAssetValue(assets, "ghost_points_3", spriteSheet.image(numberSprites[3]));

        storeLocalAssetValue(assets, "ghost.0.color.normal.dress",      ARCADE_RED);
        storeLocalAssetValue(assets, "ghost.0.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAssetValue(assets, "ghost.0.color.normal.pupils",     ARCADE_BLUE);

        storeLocalAssetValue(assets, "ghost.1.color.normal.dress",      ARCADE_PINK);
        storeLocalAssetValue(assets, "ghost.1.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAssetValue(assets, "ghost.1.color.normal.pupils",     ARCADE_BLUE);

        storeLocalAssetValue(assets, "ghost.2.color.normal.dress",      ARCADE_CYAN);
        storeLocalAssetValue(assets, "ghost.2.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAssetValue(assets, "ghost.2.color.normal.pupils",     ARCADE_BLUE);

        storeLocalAssetValue(assets, "ghost.3.color.normal.dress",      ARCADE_ORANGE);
        storeLocalAssetValue(assets, "ghost.3.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAssetValue(assets, "ghost.3.color.normal.pupils",     ARCADE_BLUE);

        storeLocalAssetValue(assets, "ghost.color.frightened.dress",    ARCADE_BLUE);
        storeLocalAssetValue(assets, "ghost.color.frightened.eyeballs", ARCADE_ROSE);
        storeLocalAssetValue(assets, "ghost.color.frightened.pupils",   ARCADE_ROSE);
        storeLocalAssetValue(assets, "ghost.color.flashing.dress",      ARCADE_WHITE);
        storeLocalAssetValue(assets, "ghost.color.flashing.eyeballs",   ARCADE_ROSE);
        storeLocalAssetValue(assets, "ghost.color.flashing.pupils",     ARCADE_RED);

        storeLocalAssetValue(assets, "audio.option.selection_changed", RES_MS_PAC_MAN_XXL.loadAudioClip("sound/ms-select1.wav"));
        storeLocalAssetValue(assets, "audio.option.value_changed",     RES_MS_PAC_MAN_XXL.loadAudioClip("sound/ms-select2.wav"));

        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_OFF,       RES_GAME_UI.url("sound/voice/autopilot-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_ON,        RES_GAME_UI.url("sound/voice/autopilot-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_OFF,        RES_GAME_UI.url("sound/voice/immunity-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_ON,         RES_GAME_UI.url("sound/voice/immunity-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_EXPLAIN,             RES_GAME_UI.url("sound/voice/press-key.mp3"));

        soundManager.registerAudioClip(SoundID.BONUS_ACTIVE,          RES_ARCADE_MS_PAC_MAN.url("sound/Fruit_Bounce.mp3"));
        soundManager.registerAudioClip(SoundID.BONUS_EATEN,           RES_ARCADE_MS_PAC_MAN.url("sound/Fruit.mp3"));
        soundManager.registerAudioClip(SoundID.COIN_INSERTED,         RES_ARCADE_MS_PAC_MAN.url("sound/credit.wav"));
        soundManager.registerAudioClip(SoundID.EXTRA_LIFE,            RES_ARCADE_MS_PAC_MAN.url("sound/ExtraLife.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_OVER,           RES_ARCADE_MS_PAC_MAN.url("sound/game-over.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_READY,          RES_ARCADE_MS_PAC_MAN.url("sound/Start.mp3"));
        soundManager.registerAudioClip(SoundID.GHOST_EATEN,           RES_ARCADE_MS_PAC_MAN.url("sound/Ghost.mp3"));
        soundManager.registerMediaPlayer(SoundID.GHOST_RETURNS,       RES_ARCADE_MS_PAC_MAN.url("sound/GhostEyes.mp3"));
        soundManager.registerMediaPlayer("audio.intermission.1",      RES_ARCADE_MS_PAC_MAN.url("sound/Act_1_They_Meet.mp3"));
        soundManager.registerMediaPlayer("audio.intermission.2",      RES_ARCADE_MS_PAC_MAN.url("sound/Act_2_The_Chase.mp3"));
        soundManager.registerMediaPlayer("audio.intermission.3",      RES_ARCADE_MS_PAC_MAN.url("sound/Act_3_Junior.mp3"));
        soundManager.registerAudioClip(SoundID.LEVEL_CHANGED,         RES_ARCADE_MS_PAC_MAN.url("sound/sweep.mp3"));
        soundManager.registerMediaPlayer(SoundID.LEVEL_COMPLETE,      RES_ARCADE_MS_PAC_MAN.url("sound/level-complete.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_DEATH,       RES_ARCADE_MS_PAC_MAN.url("sound/Died.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_MUNCHING,    RES_ARCADE_MS_PAC_MAN.url("sound/munch.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_POWER,       RES_ARCADE_MS_PAC_MAN.url("sound/ScaredGhost.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_1,             RES_ARCADE_MS_PAC_MAN.url("sound/GhostNoise1.wav"));
        soundManager.registerMediaPlayer(SoundID.SIREN_2,             RES_ARCADE_MS_PAC_MAN.url("sound/GhostNoise1.wav"));// TODO
        soundManager.registerMediaPlayer(SoundID.SIREN_3,             RES_ARCADE_MS_PAC_MAN.url("sound/GhostNoise1.wav"));// TODO
        soundManager.registerMediaPlayer(SoundID.SIREN_4,             RES_ARCADE_MS_PAC_MAN.url("sound/GhostNoise1.wav"));// TODO
    }

    @Override
    public void dispose() {
        ui.assets().removeAll(ASSET_NAMESPACE + ".");
        soundManager.dispose();
    }

    @Override
    public SoundManager soundManager() {
        return soundManager;
    }

    @Override
    public String assetNamespace() {
        return ASSET_NAMESPACE;
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        Map<String, String> colorMap = worldMap.getConfigValue("colorMap");
        return new WorldMapColorScheme(
            colorMap.get("fill"), colorMap.get("stroke"), colorMap.get("door"), colorMap.get("pellet"));
    }

    @Override
    public PacManXXL_MsPacMan_GameRenderer createGameRenderer(Canvas canvas) {
        return new PacManXXL_MsPacMan_GameRenderer(ui.assets(), canvas, spriteSheet);
    }

    @Override
    public SpriteAnimationManager<SpriteID> createGhostAnimations(Ghost ghost) {
        return new ArcadeMsPacMan_GhostAnimationManager(spriteSheet, ghost.id().personality());
    }

    @Override
    public SpriteAnimationManager<SpriteID> createPacAnimations(Pac pac) {
        return new ArcadeMsPacMan_PacAnimationManager(spriteSheet);
    }

    @Override
    public Image killedGhostPointsImage(Ghost ghost, int killedIndex) {
        return localAssetImage("ghost_points_" + killedIndex);
    }

    @Override
    public Image bonusSymbolImage(byte symbol) {
        return localAssetImage("bonus_symbol_" + symbol);
    }

    @Override
    public Image bonusValueImage(byte symbol) {
        return localAssetImage("bonus_value_" + symbol);
    }

    @Override
    public MsPacManBody createLivesCounterShape3D() {
        return ui.assets().theModel3DRepository().createMsPacManBody(
            ui.uiPreferences().getFloat("3d.lives_counter.shape_size"),
            localAssetColor("pac.color.head"),
            localAssetColor("pac.color.eyes"),
            localAssetColor("pac.color.palate"),
            localAssetColor("pac.color.hairbow"),
            localAssetColor("pac.color.hairbow.pearls"),
            localAssetColor("pac.color.boobs")
        );
    }

    @Override
    public MsPacMan3D createPac3D(AnimationRegistry animationRegistry, GameLevel gameLevel, Pac pac) {
        var pac3D = new MsPacMan3D(
            ui.assets().theModel3DRepository(),
            animationRegistry,
            pac,
            ui.uiPreferences().getFloat("3d.pac.size"),
            localAssetColor("pac.color.head"),
            localAssetColor("pac.color.eyes"),
            localAssetColor("pac.color.palate"),
            localAssetColor("pac.color.hairbow"),
            localAssetColor("pac.color.hairbow.pearls"),
            localAssetColor("pac.color.boobs")
        );
        Color headColor = localAssetColor("pac.color.head");
        pac3D.light().setColor(headColor.desaturate());
        return pac3D;
    }

    // Game scenes

    @Override
    public void createGameScenes() {
        scenesByID.put(SCENE_ID_BOOT_SCENE_2D,               new ArcadePacMan_BootScene2D(ui));
        scenesByID.put(SCENE_ID_INTRO_SCENE_2D,              new ArcadeMsPacMan_IntroScene(ui));
        scenesByID.put(SCENE_ID_START_SCENE_2D,              new ArcadeMsPacMan_StartScene(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_2D,               new ArcadePacMan_PlayScene2D(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_3D,               new PlayScene3D(ui));
        scenesByID.put(SCENE_ID_CUT_SCENE_N_2D.formatted(1), new ArcadeMsPacMan_CutScene1(ui));
        scenesByID.put(SCENE_ID_CUT_SCENE_N_2D.formatted(2), new ArcadeMsPacMan_CutScene2(ui));
        scenesByID.put(SCENE_ID_CUT_SCENE_N_2D.formatted(3), new ArcadeMsPacMan_CutScene3(ui));
    }

    @Override
    public GameScene selectGameScene(GameContext gameContext) {
        String sceneID = switch (gameContext.gameState()) {
            case GamePlayState.BOOT -> SCENE_ID_BOOT_SCENE_2D;
            case GamePlayState.SETTING_OPTIONS_FOR_START -> SCENE_ID_START_SCENE_2D;
            case GamePlayState.INTRO -> SCENE_ID_INTRO_SCENE_2D;
            case GamePlayState.INTERMISSION -> {
                if (gameContext.optGameLevel().isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene, no game level available");
                }
                int levelNumber = gameContext.gameLevel().number();
                OptionalInt optCutSceneNumber = gameContext.game().optCutSceneNumber(levelNumber);
                if (optCutSceneNumber.isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene after level %d".formatted(levelNumber));
                }
                yield SCENE_ID_CUT_SCENE_N_2D.formatted(optCutSceneNumber.getAsInt());
            }
            case CutScenesTestState testState -> SCENE_ID_CUT_SCENE_N_2D.formatted(testState.testedCutSceneNumber);
            default -> PROPERTY_3D_ENABLED.get() ? SCENE_ID_PLAY_SCENE_3D : SCENE_ID_PLAY_SCENE_2D;
        };
        return scenesByID.get(sceneID);
    }

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }

    @Override
    public boolean gameSceneHasID(GameScene gameScene, String sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        return scenesByID.get(sceneID) == gameScene;
    }
}