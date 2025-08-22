/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.*;
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
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.MsPacMan3D;
import de.amr.pacmanfx.uilib.model3D.MsPacManBody;
import de.amr.pacmanfx.uilib.rendering.ActorSpriteRenderer;
import de.amr.pacmanfx.uilib.rendering.HUDRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;
import static de.amr.pacmanfx.ui.api.GameScene_Config.sceneID_CutScene;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_UIConfig implements GameUI_Config {

    private static final ResourceManager RES_GAME_UI = () -> GameUI_Implementation.class;
    private static final ResourceManager RES_ARCADE_MS_PAC_MAN = () -> ArcadeMsPacMan_UIConfig.class;

    private static final List<WorldMapColorScheme> WORLD_MAP_COLOR_SCHEMES = List.of(
        new WorldMapColorScheme("FFB7AE", "FF0000", "FCB5FF", "DEDEFF"),
        new WorldMapColorScheme("47B7FF", "DEDEFF", "FCB5FF", "FFFF00"),
        new WorldMapColorScheme("DE9751", "DEDEFF", "FCB5FF", "FF0000"),
        new WorldMapColorScheme("2121FF", "FFB751", "FCB5FF", "DEDEFF"),
        new WorldMapColorScheme("FFB7FF", "FFFF00", "FCB5FF", "00FFFF"),
        new WorldMapColorScheme("FFB7AE", "FF0000", "FCB5FF", "DEDEFF")
    );

    private final GameUI ui;
    private final AssetStorage assets = new AssetStorage();
    private final DefaultSoundManager soundManager = new DefaultSoundManager();
    private final Map<String, GameScene> scenesByID = new HashMap<>();
    private final ArcadeMsPacMan_SpriteSheet spriteSheet;
    private final BrightMazesSpriteSheet spriteSheetBrightMazes;

    public ArcadeMsPacMan_UIConfig(GameUI ui) {
        this.ui = requireNonNull(ui);
        spriteSheet = new ArcadeMsPacMan_SpriteSheet(RES_ARCADE_MS_PAC_MAN.loadImage("graphics/mspacman_spritesheet.png"));
        spriteSheetBrightMazes = new BrightMazesSpriteSheet(RES_ARCADE_MS_PAC_MAN.loadImage("graphics/mazes_flashing.png"));
        assets.setTextResources(ResourceBundle.getBundle("de.amr.pacmanfx.arcade.ms_pacman.localized_texts"));
    }

    @Override
    public GameUI theUI() {
        return ui;
    }

    @Override
    public AssetStorage assets() {
        return assets;
    }

    public void loadAssets() {
        assets.set("app_icon",                RES_ARCADE_MS_PAC_MAN.loadImage("graphics/icons/mspacman.png"));

        assets.set("logo.midway",             RES_ARCADE_MS_PAC_MAN.loadImage("graphics/midway_logo.png"));

        assets.set("startpage.image1",        RES_ARCADE_MS_PAC_MAN.loadImage("graphics/f1.jpg"));
        assets.set("startpage.image2",        RES_ARCADE_MS_PAC_MAN.loadImage("graphics/f2.jpg"));

        assets.set("color.game_over_message", ARCADE_RED);

        assets.set("pac.color.head",           ARCADE_YELLOW);
        assets.set("pac.color.eyes",           Color.grayRgb(33));
        assets.set("pac.color.palate",         ARCADE_BROWN);
        assets.set("pac.color.boobs",          ARCADE_YELLOW.deriveColor(0, 1.0, 0.96, 1.0));
        assets.set("pac.color.hairbow",        ARCADE_RED);
        assets.set("pac.color.hairbow.pearls", ARCADE_BLUE);

        assets.set("ghost.0.color.normal.dress",      ARCADE_RED);
        assets.set("ghost.0.color.normal.eyeballs",   ARCADE_WHITE);
        assets.set("ghost.0.color.normal.pupils",     ARCADE_BLUE);

        assets.set("ghost.1.color.normal.dress",      ARCADE_PINK);
        assets.set("ghost.1.color.normal.eyeballs",   ARCADE_WHITE);
        assets.set("ghost.1.color.normal.pupils",     ARCADE_BLUE);

        assets.set("ghost.2.color.normal.dress",      ARCADE_CYAN);
        assets.set("ghost.2.color.normal.eyeballs",   ARCADE_WHITE);
        assets.set("ghost.2.color.normal.pupils",     ARCADE_BLUE);

        assets.set("ghost.3.color.normal.dress",      ARCADE_ORANGE);
        assets.set("ghost.3.color.normal.eyeballs",   ARCADE_WHITE);
        assets.set("ghost.3.color.normal.pupils",     ARCADE_BLUE);

        assets.set("ghost.color.frightened.dress",    ARCADE_BLUE);
        assets.set("ghost.color.frightened.eyeballs", ARCADE_ROSE);
        assets.set("ghost.color.frightened.pupils",   ARCADE_ROSE);
        assets.set("ghost.color.flashing.dress",      ARCADE_WHITE);
        assets.set("ghost.color.flashing.eyeballs",   ARCADE_ROSE);
        assets.set("ghost.color.flashing.pupils",     ARCADE_RED);

        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_OFF,       RES_GAME_UI.url("sound/voice/autopilot-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_ON,        RES_GAME_UI.url("sound/voice/autopilot-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_OFF,        RES_GAME_UI.url("sound/voice/immunity-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_ON,         RES_GAME_UI.url("sound/voice/immunity-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_EXPLAIN,             RES_GAME_UI.url("sound/voice/press-key.mp3"));

        soundManager.registerMediaPlayer(SoundID.BONUS_ACTIVE,        RES_ARCADE_MS_PAC_MAN.url("sound/Fruit_Bounce.mp3"));
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
        assets.removeAll();
        soundManager.dispose();
    }

    @Override
    public SoundManager soundManager() {
        return soundManager;
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        return WORLD_MAP_COLOR_SCHEMES.get(worldMap.getConfigValue("colorMapIndex"));
    }

    @Override
    public ArcadeMsPacMan_GameLevelRenderer createGameLevelRenderer(Canvas canvas) {
        return new ArcadeMsPacMan_GameLevelRenderer(canvas, this, spriteSheetBrightMazes);
    }

    @Override
    public HUDRenderer createHUDRenderer(Canvas canvas) {
        return new ArcadeMsPacMan_HUDRenderer(canvas, this);
    }

    @Override
    public ActorSpriteRenderer createActorSpriteRenderer(Canvas canvas) {
        return new ArcadeMsPacMan_ActorSpriteRenderer(canvas, this);
    }

    @Override
    public ArcadeMsPacMan_GhostAnimationManager createGhostAnimations(Ghost ghost) {
        return new ArcadeMsPacMan_GhostAnimationManager(spriteSheet, ghost.id().personality());
    }

    @Override
    public ArcadeMsPacMan_PacAnimationManager createPacAnimations(Pac pac) {
        return new ArcadeMsPacMan_PacAnimationManager(spriteSheet);
    }

    @Override
    public Image killedGhostPointsImage(Ghost ghost, int killedIndex) {
        RectShort[] numberSprites = spriteSheet.spriteSequence(SpriteID.GHOST_NUMBERS);
        return spriteSheet.image(numberSprites[killedIndex]);
    }

    @Override
    public Image bonusSymbolImage(byte symbol) {
        RectShort[] sprites = spriteSheet.spriteSequence(SpriteID.BONUS_SYMBOLS);
        return spriteSheet.image(sprites[symbol]);
    }

    @Override
    public Image bonusValueImage(byte symbol) {
        RectShort[] sprites = spriteSheet.spriteSequence(SpriteID.BONUS_VALUES);
        return spriteSheet.image(sprites[symbol]);
    }

    @Override
    public MsPacManBody createLivesCounterShape3D() {
        return ui.assets().theModel3DRepository().createMsPacManBody(
            ui.preferences().getFloat("3d.lives_counter.shape_size"),
            assets.color("pac.color.head"),
            assets.color("pac.color.eyes"),
            assets.color("pac.color.palate"),
            assets.color("pac.color.hairbow"),
            assets.color("pac.color.hairbow.pearls"),
            assets.color("pac.color.boobs")
        );
    }

    @Override
    public MsPacMan3D createPac3D(AnimationRegistry animationRegistry, GameLevel gameLevel, Pac pac) {
        var pac3D = new MsPacMan3D(
            ui.assets().theModel3DRepository(),
            animationRegistry,
            pac,
            ui.preferences().getFloat("3d.pac.size"),
            assets.color("pac.color.head"),
            assets.color("pac.color.eyes"),
            assets.color("pac.color.palate"),
            assets.color("pac.color.hairbow"),
            assets.color("pac.color.hairbow.pearls"),
            assets.color("pac.color.boobs"));
        pac3D.light().setColor(assets.color("pac.color.head").desaturate());
        return pac3D;
    }

    // Game scenes

    @Override
    public void createGameScenes() {
        scenesByID.put(SCENE_ID_BOOT_SCENE_2D,  new ArcadePacMan_BootScene2D(ui));
        scenesByID.put(SCENE_ID_INTRO_SCENE_2D, new ArcadeMsPacMan_IntroScene(ui));
        scenesByID.put(SCENE_ID_START_SCENE_2D, new ArcadeMsPacMan_StartScene(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_2D,  new ArcadePacMan_PlayScene2D(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_3D,  new PlayScene3D(ui));
        scenesByID.put(sceneID_CutScene(1),     new ArcadeMsPacMan_CutScene1(ui));
        scenesByID.put(sceneID_CutScene(2),     new ArcadeMsPacMan_CutScene2(ui));
        scenesByID.put(sceneID_CutScene(3),     new ArcadeMsPacMan_CutScene3(ui));
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
                yield sceneID_CutScene(optCutSceneNumber.getAsInt());
            }
            case CutScenesTestState testState -> sceneID_CutScene(testState.testedCutSceneNumber);
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