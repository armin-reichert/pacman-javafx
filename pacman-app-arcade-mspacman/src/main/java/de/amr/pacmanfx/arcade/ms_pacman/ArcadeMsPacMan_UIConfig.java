/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_MapSelector;
import de.amr.pacmanfx.arcade.ms_pacman.model.actors.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.*;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.*;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_BootScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene3D;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.ui.GameUI_Implementation;
import de.amr.pacmanfx.ui._2d.HUD_Renderer;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameScene_Config;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.MsPacMan3D;
import de.amr.pacmanfx.uilib.model3D.MsPacManBody;
import de.amr.pacmanfx.uilib.model3D.PacManModel3DRepository;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState.*;
import static de.amr.pacmanfx.ui.api.ArcadePalette.*;
import static de.amr.pacmanfx.ui.api.GameScene_Config.sceneID_CutScene;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_UIConfig implements GameUI_Config, GameScene_Config {

    public enum AnimationID {PAC_MAN_MUNCHING}

    private static final ResourceManager GLOBAL_RESOURCES = () -> GameUI_Implementation.class;
    private static final ResourceManager ARCADE_MS_PAC_MAN_RESOURCES = () -> ArcadeMsPacMan_UIConfig.class;

    private final GameUI ui;
    private final AssetMap assets = new AssetMap();
    private final SoundManager soundManager = new SoundManager();
    private final Map<String, GameScene> scenesByID = new HashMap<>();
    private final ArcadeMsPacMan_SpriteSheet spriteSheet;

    public ArcadeMsPacMan_UIConfig(GameUI ui) {
        this.ui = requireNonNull(ui);
        spriteSheet = new ArcadeMsPacMan_SpriteSheet(ARCADE_MS_PAC_MAN_RESOURCES.loadImage("graphics/mspacman_spritesheet.png"));
        assets.setLocalizedTexts(ResourceBundle.getBundle("de.amr.pacmanfx.arcade.ms_pacman.localized_texts"));
    }

    // Creates the maze image used in the flash animation at the end of each level
    private Image createBrightMazeImage(int index) {
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
    public AssetMap assets() {
        return assets;
    }

    public void loadAssets() {
        assets.set("app_icon",                ARCADE_MS_PAC_MAN_RESOURCES.loadImage("graphics/icons/mspacman.png"));

        assets.set("logo.midway",             ARCADE_MS_PAC_MAN_RESOURCES.loadImage("graphics/midway_logo.png"));

        for (int i = 0; i < ArcadeMsPacMan_MapSelector.WORLD_MAP_COLOR_SCHEMES.size(); ++i) {
            assets.set("maze.bright.%d".formatted(i), createBrightMazeImage(i));
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

        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_OFF,       GLOBAL_RESOURCES.url("sound/voice/autopilot-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_ON,        GLOBAL_RESOURCES.url("sound/voice/autopilot-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_OFF,        GLOBAL_RESOURCES.url("sound/voice/immunity-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_ON,         GLOBAL_RESOURCES.url("sound/voice/immunity-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_EXPLAIN,             GLOBAL_RESOURCES.url("sound/voice/press-key.mp3"));

        soundManager.registerMediaPlayer(SoundID.BONUS_ACTIVE,        ARCADE_MS_PAC_MAN_RESOURCES.url("sound/Fruit_Bounce.mp3"));
        soundManager.registerAudioClipURL(SoundID.BONUS_EATEN,           ARCADE_MS_PAC_MAN_RESOURCES.url("sound/Fruit.mp3"));
        soundManager.registerAudioClipURL(SoundID.COIN_INSERTED,         ARCADE_MS_PAC_MAN_RESOURCES.url("sound/credit.wav"));
        soundManager.registerAudioClipURL(SoundID.EXTRA_LIFE,            ARCADE_MS_PAC_MAN_RESOURCES.url("sound/ExtraLife.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_OVER,           ARCADE_MS_PAC_MAN_RESOURCES.url("sound/game-over.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_READY,          ARCADE_MS_PAC_MAN_RESOURCES.url("sound/Start.mp3"));
        soundManager.registerAudioClipURL(SoundID.GHOST_EATEN,           ARCADE_MS_PAC_MAN_RESOURCES.url("sound/Ghost.mp3"));
        soundManager.registerMediaPlayer(SoundID.GHOST_RETURNS,       ARCADE_MS_PAC_MAN_RESOURCES.url("sound/GhostEyes.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_1,      ARCADE_MS_PAC_MAN_RESOURCES.url("sound/Act_1_They_Meet.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_2,      ARCADE_MS_PAC_MAN_RESOURCES.url("sound/Act_2_The_Chase.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_3,      ARCADE_MS_PAC_MAN_RESOURCES.url("sound/Act_3_Junior.mp3"));
        soundManager.registerAudioClipURL(SoundID.LEVEL_CHANGED,         ARCADE_MS_PAC_MAN_RESOURCES.url("sound/sweep.mp3"));
        soundManager.registerMediaPlayer(SoundID.LEVEL_COMPLETE,      ARCADE_MS_PAC_MAN_RESOURCES.url("sound/level-complete.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_DEATH,       ARCADE_MS_PAC_MAN_RESOURCES.url("sound/Died.mp3"));
        soundManager.registerAudioClipURL(SoundID.PAC_MAN_MUNCHING,      ARCADE_MS_PAC_MAN_RESOURCES.url("sound/munch.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_POWER,       ARCADE_MS_PAC_MAN_RESOURCES.url("sound/ScaredGhost.mp3"));
        soundManager.registerMedia(SoundID.SIREN_1,                   ARCADE_MS_PAC_MAN_RESOURCES.url("sound/GhostNoise1.mp3"));
        soundManager.registerMedia(SoundID.SIREN_2,                   ARCADE_MS_PAC_MAN_RESOURCES.url("sound/GhostNoise2.mp3"));
        soundManager.registerMedia(SoundID.SIREN_3,                   ARCADE_MS_PAC_MAN_RESOURCES.url("sound/GhostNoise3.mp3"));
        soundManager.registerMedia(SoundID.SIREN_4,                   ARCADE_MS_PAC_MAN_RESOURCES.url("sound/GhostNoise4.mp3"));
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
        return new ArcadeMsPacMan_GameLevelRenderer(canvas, spriteSheet, assets);
    }

    @Override
    public HUD_Renderer createHUDRenderer(Canvas canvas) {
        var hudRenderer = new ArcadeMsPacMan_HUDRenderer(canvas, spriteSheet);
        hudRenderer.setImageSmoothing(true);
        return hudRenderer;
    }

    @Override
    public ActorRenderer createActorRenderer(Canvas canvas) {
        var actorRenderer = new ArcadeMsPacMan_ActorRenderer(canvas, spriteSheet);
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
    public Image killedGhostPointsImage(int killedIndex) {
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
        return PacManModel3DRepository.theRepository().createMsPacManBody(
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
    public MsPacMan3D createPac3D(AnimationRegistry animationRegistry, Pac pac, double size) {
        var pac3D = new MsPacMan3D(
            PacManModel3DRepository.theRepository(),
            animationRegistry,
            pac,
            size,
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
    public GameScene_Config sceneConfig() {
        return this;
    }

    @Override
    public void createGameScenes(GameUI ui) {
        scenesByID.put(SCENE_ID_BOOT_SCENE,     new Arcade_BootScene2D(ui));
        scenesByID.put(SCENE_ID_INTRO_SCENE,    new ArcadeMsPacMan_IntroScene(ui));
        scenesByID.put(SCENE_ID_START_SCENE,    new ArcadeMsPacMan_StartScene(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_2D,  new Arcade_PlayScene2D(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_3D,  new Arcade_PlayScene3D(ui));
        scenesByID.put(sceneID_CutScene(1),     new ArcadeMsPacMan_CutScene1(ui));
        scenesByID.put(sceneID_CutScene(2),     new ArcadeMsPacMan_CutScene2(ui));
        scenesByID.put(sceneID_CutScene(3),     new ArcadeMsPacMan_CutScene3(ui));
    }

    @Override
    public boolean canvasDecorated(GameScene gameScene) {
        return true;
    }

    @Override
    public GameScene selectGameScene(Game game) {
        String sceneID = switch (game.control().state()) {
            case BOOT -> SCENE_ID_BOOT_SCENE;
            case SETTING_OPTIONS_FOR_START -> SCENE_ID_START_SCENE;
            case INTRO -> SCENE_ID_INTRO_SCENE;
            case INTERMISSION -> {
                if (game.optGameLevel().isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene, no game level available");
                }
                int cutSceneNumber = game.level().cutSceneNumber();
                if (cutSceneNumber == 0) {
                    throw new IllegalStateException("Cannot determine cut scene after level %d".formatted(game.level().number()));
                }
                yield sceneID_CutScene(cutSceneNumber);
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