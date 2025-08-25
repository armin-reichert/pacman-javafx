/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.pacman.rendering.*;
import de.amr.pacmanfx.arcade.pacman.scenes.*;
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
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.PacBody;
import de.amr.pacmanfx.uilib.model3D.PacMan3D;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.HUDRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;
import static de.amr.pacmanfx.ui.api.GameScene_Config.sceneID_CutScene;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_UIConfig implements GameUI_Config {

    private static final ResourceManager RES_GAME_UI = () -> GameUI_Implementation.class;
    private static final ResourceManager RES_ARCADE_PAC_MAN = () -> ArcadePacMan_UIConfig.class;

    public static final String ANIM_BIG_PAC_MAN               = "big_pac_man";
    public static final String ANIM_BLINKY_DAMAGED            = "blinky_damaged";
    public static final String ANIM_BLINKY_PATCHED            = "blinky_patched";
    public static final String ANIM_BLINKY_NAIL_DRESS_RAPTURE = "blinky_nail_dress_rapture";
    public static final String ANIM_BLINKY_NAKED              = "blinky_naked";

    private static final Map<Color, Color> BRIGHT_MAZE_COLOR_CHANGES = Map.of(
        ArcadePacMan_GameModel.MAP_COLOR_SCHEME.stroke(), ARCADE_WHITE,   // wall color change
        ArcadePacMan_GameModel.MAP_COLOR_SCHEME.door(), Color.TRANSPARENT // door color change
    );

    private final GameUI ui;
    private final AssetStorage assets = new AssetStorage();
    private final DefaultSoundManager soundManager = new DefaultSoundManager();
    private final Map<String, GameScene> scenesByID = new HashMap<>();
    private final ArcadePacMan_SpriteSheet spriteSheet;

    public ArcadePacMan_UIConfig(GameUI ui) {
        this.ui = requireNonNull(ui);
        assets.setTextResources(ResourceBundle.getBundle("de.amr.pacmanfx.arcade.pacman.localized_texts"));
        spriteSheet = new ArcadePacMan_SpriteSheet(RES_ARCADE_PAC_MAN.loadImage("graphics/pacman_spritesheet.png"));
    }

    @Override
    public AssetStorage assets() {
        return assets;
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void loadAssets() {
        assets.set("app_icon",         RES_ARCADE_PAC_MAN.loadImage("graphics/icons/pacman.png"));

        assets.set("startpage.image1", RES_ARCADE_PAC_MAN.loadImage("graphics/f1.jpg"));
        assets.set("startpage.image2", RES_ARCADE_PAC_MAN.loadImage("graphics/f2.jpg"));
        assets.set("startpage.image3", RES_ARCADE_PAC_MAN.loadImage("graphics/f3.jpg"));

        assets.set("color.game_over_message", ARCADE_RED);

        assets.set("maze.bright", Ufx.recolorImage(spriteSheet.image(SpriteID.MAP_EMPTY), BRIGHT_MAZE_COLOR_CHANGES));

        assets.set("pac.color.head",   ARCADE_YELLOW);
        assets.set("pac.color.eyes",   Color.grayRgb(33));
        assets.set("pac.color.palate", ARCADE_BROWN);

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

        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_OFF, RES_GAME_UI.url("sound/voice/autopilot-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_ON,  RES_GAME_UI.url("sound/voice/autopilot-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_OFF,  RES_GAME_UI.url("sound/voice/immunity-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_ON,   RES_GAME_UI.url("sound/voice/immunity-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_EXPLAIN,       RES_GAME_UI.url("sound/voice/press-key.mp3"));

        soundManager.registerAudioClip(SoundID.BONUS_EATEN,        RES_ARCADE_PAC_MAN.url("sound/eat_fruit.mp3"));
        soundManager.registerAudioClip(SoundID.COIN_INSERTED,      RES_ARCADE_PAC_MAN.url("sound/credit.wav"));
        soundManager.registerAudioClip(SoundID.EXTRA_LIFE,         RES_ARCADE_PAC_MAN.url("sound/extend.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_OVER,        RES_ARCADE_PAC_MAN.url("sound/common/game-over.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_READY,       RES_ARCADE_PAC_MAN.url("sound/game_start.mp3"));
        soundManager.registerAudioClip(SoundID.GHOST_EATEN,        RES_ARCADE_PAC_MAN.url("sound/eat_ghost.mp3"));
        soundManager.registerMediaPlayer(SoundID.GHOST_RETURNS,    RES_ARCADE_PAC_MAN.url("sound/retreating.mp3"));
        soundManager.registerMediaPlayer("audio.intermission",     RES_ARCADE_PAC_MAN.url("sound/intermission.mp3"));
        soundManager.registerAudioClip(SoundID.LEVEL_CHANGED,      RES_ARCADE_PAC_MAN.url("sound/common/sweep.mp3"));
        soundManager.registerMediaPlayer(SoundID.LEVEL_COMPLETE,   RES_ARCADE_PAC_MAN.url("sound/common/level-complete.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_DEATH,    RES_ARCADE_PAC_MAN.url("sound/pacman_death.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_MUNCHING, RES_ARCADE_PAC_MAN.url("sound/munch.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_POWER,    RES_ARCADE_PAC_MAN.url("sound/ghost-turn-to-blue.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_1,          RES_ARCADE_PAC_MAN.url("sound/siren_1.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_2,          RES_ARCADE_PAC_MAN.url("sound/siren_2.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_3,          RES_ARCADE_PAC_MAN.url("sound/siren_3.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_4,          RES_ARCADE_PAC_MAN.url("sound/siren_4.mp3"));
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
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        return ArcadePacMan_GameModel.MAP_COLOR_SCHEME;
    }

    @Override
    public ArcadePacMan_GameLevelRenderer createGameLevelRenderer(Canvas canvas) {
        return new ArcadePacMan_GameLevelRenderer(canvas, this);
    }

    @Override
    public HUDRenderer createHUDRenderer(Canvas canvas) {
        return new ArcadePacMan_HUDRenderer(canvas, this);
    }

    @Override
    public ActorRenderer createActorRenderer(Canvas canvas) {
        return new ArcadePacMan_ActorRenderer(canvas, this);
    }

    @Override
    public ArcadePacMan_GhostAnimationManager createGhostAnimations(Ghost ghost) {
        return new ArcadePacMan_GhostAnimationManager(spriteSheet, ghost.id().personality());
    }

    @Override
    public ArcadePacMan_PacAnimationManager createPacAnimations(Pac pac) {
        return new ArcadePacMan_PacAnimationManager(spriteSheet);
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
    public PacBody createLivesCounterShape3D() {
        return ui.assets().theModel3DRepository().createPacBody(
            ui.preferences().getFloat("3d.lives_counter.shape_size"),
            assets.color("pac.color.head"),
            assets.color("pac.color.eyes"),
            assets.color("pac.color.palate")
        );
    }

    @Override
    public PacMan3D createPac3D(AnimationRegistry animationRegistry, GameLevel gameLevel, Pac pac) {
        var pac3D = new PacMan3D(ui.assets().theModel3DRepository(),
            animationRegistry,
            gameLevel,
            pac,
            ui.preferences().getFloat("3d.pac.size"),
            assets.color("pac.color.head"),
            assets.color("pac.color.eyes"),
            assets.color("pac.color.palate"));
        pac3D.light().setColor(assets.color("pac.color.head").desaturate());
        return pac3D;
    }

    // Game scene config

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }

    @Override
    public void createGameScenes() {
        scenesByID.put(SCENE_ID_BOOT_SCENE_2D,  new Arcade_BootScene2D(ui));
        scenesByID.put(SCENE_ID_INTRO_SCENE_2D, new ArcadePacMan_IntroScene(ui));
        scenesByID.put(SCENE_ID_START_SCENE_2D, new ArcadePacMan_StartScene(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_2D,  new Arcade_PlayScene2D(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_3D,  new PlayScene3D(ui));
        scenesByID.put(sceneID_CutScene(1),     new ArcadePacMan_CutScene1(ui));
        scenesByID.put(sceneID_CutScene(2),     new ArcadePacMan_CutScene2(ui));
        scenesByID.put(sceneID_CutScene(3),     new ArcadePacMan_CutScene3(ui));
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
    public boolean gameSceneHasID(GameScene gameScene, String sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        return scenesByID.get(sceneID) == gameScene;
    }
}