/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.ms_pacman.actors.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.*;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.*;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_BootScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.controller.test.CutScenesTestState;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
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
import de.amr.pacmanfx.uilib.model3D.MsPacMan3D;
import de.amr.pacmanfx.uilib.model3D.MsPacManBody;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.HUDRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;
import static de.amr.pacmanfx.ui.api.GameScene_Config.sceneID_CutScene;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_UIConfig implements GameUI_Config {

    private static final ResourceManager RES_GAME_UI = () -> GameUI_Implementation.class;
    private static final ResourceManager RES_ARCADE_MS_PAC_MAN = () -> ArcadeMsPacMan_UIConfig.class;

    private final GameUI ui;
    private final AssetStorage assets = new AssetStorage();
    private final DefaultSoundManager soundManager = new DefaultSoundManager();
    private final Map<String, GameScene> scenesByID = new HashMap<>();
    private final ArcadeMsPacMan_SpriteSheet spriteSheet;

    public ArcadeMsPacMan_UIConfig(GameUI ui) {
        this.ui = requireNonNull(ui);
        spriteSheet = new ArcadeMsPacMan_SpriteSheet(RES_ARCADE_MS_PAC_MAN.loadImage("graphics/mspacman_spritesheet.png"));
        assets.setTextResources(ResourceBundle.getBundle("de.amr.pacmanfx.arcade.ms_pacman.localized_texts"));
    }

    private Image brightMaze(int index) {
        RectShort mazeSprite = spriteSheet().spriteSequence(SpriteID.FULL_MAZES)[index];
        Image mazeImage = spriteSheet.image(mazeSprite);
        WorldMapColorScheme colorScheme = ArcadeMsPacMan_MapSelector.WORLD_MAP_COLOR_SCHEMES.get(index);
        Map<Color, Color> changes = Map.of(
            colorScheme.stroke(), ARCADE_WHITE,
            colorScheme.door(), Color.TRANSPARENT
        );
        return Ufx.recolorImage(mazeImage, changes);
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

        for (int i = 0; i < ArcadeMsPacMan_MapSelector.WORLD_MAP_COLOR_SCHEMES.size(); ++i) {
            assets.set("maze.bright.%d".formatted(i), brightMaze(i));
        }

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
        int index = worldMap.getConfigValue("colorMapIndex");
        return ArcadeMsPacMan_MapSelector.WORLD_MAP_COLOR_SCHEMES.get(index);
    }

    @Override
    public ArcadeMsPacMan_GameLevelRenderer createGameLevelRenderer(Canvas canvas) {
        return new ArcadeMsPacMan_GameLevelRenderer(canvas, this);
    }

    @Override
    public HUDRenderer createHUDRenderer(Canvas canvas) {
        var hudRenderer = new ArcadeMsPacMan_HUDRenderer(canvas, this);
        hudRenderer.setImageSmoothing(true);
        return hudRenderer;
    }

    @Override
    public ActorRenderer createActorRenderer(Canvas canvas) {
        var actorRenderer = new ArcadeMsPacMan_ActorRenderer(canvas, this);
        actorRenderer.setImageSmoothing(true);
        return actorRenderer;
    }

    public Ghost createAnimatedGhost(byte personality) {
        requireValidGhostPersonality(personality);
        Ghost ghost = switch (personality) {
            case RED_GHOST_SHADOW   -> ArcadeMsPacMan_ActorFactory.createBlinky();
            case PINK_GHOST_SPEEDY  -> ArcadeMsPacMan_ActorFactory.createPinky();
            case CYAN_GHOST_BASHFUL -> ArcadeMsPacMan_ActorFactory.createInky();
            case ORANGE_GHOST_POKEY -> ArcadeMsPacMan_ActorFactory.createSue();
            default -> throw new IllegalArgumentException("Illegal ghost personality " + personality);
        };
        ghost.setAnimationManager(createGhostAnimations(personality));
        ghost.selectAnimation(CommonAnimationID.ANIM_GHOST_NORMAL);
        return ghost;
    }

    @Override
    public ArcadeMsPacMan_GhostAnimationManager createGhostAnimations(byte personality) {
        return new ArcadeMsPacMan_GhostAnimationManager(spriteSheet, personality);
    }

    @Override
    public ArcadeMsPacMan_PacAnimationManager createPacAnimations() {
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
        scenesByID.put(SCENE_ID_BOOT_SCENE_2D,  new Arcade_BootScene2D(ui));
        scenesByID.put(SCENE_ID_INTRO_SCENE_2D, new ArcadeMsPacMan_IntroScene(ui));
        scenesByID.put(SCENE_ID_START_SCENE_2D, new ArcadeMsPacMan_StartScene(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_2D,  new Arcade_PlayScene2D(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_3D,  new PlayScene3D(ui));
        scenesByID.put(sceneID_CutScene(1),     new ArcadeMsPacMan_CutScene1(ui));
        scenesByID.put(sceneID_CutScene(2),     new ArcadeMsPacMan_CutScene2(ui));
        scenesByID.put(sceneID_CutScene(3),     new ArcadeMsPacMan_CutScene3(ui));
    }

    @Override
    public boolean showWithDecoration(GameScene gameScene) {
        return true;
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
                Optional<Integer> optCutSceneNumber = gameContext.game().optCutSceneNumber(levelNumber);
                if (optCutSceneNumber.isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene after level %d".formatted(levelNumber));
                }
                yield sceneID_CutScene(optCutSceneNumber.get());
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