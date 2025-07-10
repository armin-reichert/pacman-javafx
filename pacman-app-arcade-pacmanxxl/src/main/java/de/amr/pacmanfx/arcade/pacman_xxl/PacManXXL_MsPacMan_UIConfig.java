/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_GhostAnimationMap;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_PacAnimationMap;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.*;
import de.amr.pacmanfx.arcade.pacman.scenes.ArcadeCommon_BootScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.ArcadeCommon_PlayScene2D;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.PacManGames_UI;
import de.amr.pacmanfx.ui.PacManGames_UIConfig;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui.sound.DefaultSoundManager;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.model3D.MsPacMan3D;
import de.amr.pacmanfx.uilib.model3D.MsPacManBody;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.optGameLevel;
import static de.amr.pacmanfx.Globals.theGameLevel;
import static de.amr.pacmanfx.arcade.pacman.rendering.ArcadePalette.*;
import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static de.amr.pacmanfx.ui.PacManGames_UI.PY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class PacManXXL_MsPacMan_UIConfig implements PacManGames_UIConfig {

    private static final String NAMESPACE = "ms_pacman_xxl";

    private boolean assetsLoaded;
    private ArcadeMsPacMan_SpriteSheet spriteSheet;
    private final DefaultSoundManager soundManager = new DefaultSoundManager();

    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public void loadAssets(AssetStorage assets) {
        if (assetsLoaded) {
            Logger.warn("Assets are already loaded");
            return;
        }
        assetsLoaded = true;

        final ResourceManager msPacManXXLRes = this::getClass;
        final ResourceManager msPacManRes = () -> ArcadeMsPacMan_UIConfig.class;

        spriteSheet = new ArcadeMsPacMan_SpriteSheet(msPacManRes.loadImage("graphics/mspacman_spritesheet.png"));

        storeInMyNamespace(assets, "app_icon",                msPacManRes.loadImage("graphics/icons/mspacman.png"));
        storeInMyNamespace(assets, "flashing_mazes",          msPacManRes.loadImage("graphics/mazes_flashing.png"));
        storeInMyNamespace(assets, "startpage.image1",        msPacManRes.loadImage("graphics/f1.jpg"));
        storeInMyNamespace(assets, "startpage.image2",        msPacManRes.loadImage("graphics/f2.jpg"));
        storeInMyNamespace(assets, "logo.midway",             msPacManRes.loadImage("graphics/midway_logo.png"));
        storeInMyNamespace(assets, "color.game_over_message", ARCADE_RED);

        RectShort[] symbolSprites = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS);
        RectShort[] valueSprites  = spriteSheet.spriteSeq(SpriteID.BONUS_VALUES);
        for (byte symbol = 0; symbol <= 6; ++symbol) {
            storeInMyNamespace(assets, "bonus_symbol_" + symbol, spriteSheet.image(symbolSprites[symbol]));
            storeInMyNamespace(assets, "bonus_value_"  + symbol, spriteSheet.image(valueSprites[symbol]));
        }

        storeInMyNamespace(assets, "pac.color.head",           ARCADE_YELLOW);
        storeInMyNamespace(assets, "pac.color.eyes",           Color.grayRgb(33));
        storeInMyNamespace(assets, "pac.color.palate",         Color.rgb(240, 180, 160));
        storeInMyNamespace(assets, "pac.color.boobs",          ARCADE_YELLOW.deriveColor(0, 1.0, 0.96, 1.0));
        storeInMyNamespace(assets, "pac.color.hairbow",        ARCADE_RED);
        storeInMyNamespace(assets, "pac.color.hairbow.pearls", ARCADE_BLUE);

        RectShort[] numberSprites = spriteSheet.spriteSeq(SpriteID.GHOST_NUMBERS);
        storeInMyNamespace(assets, "ghost_points_0", spriteSheet.image(numberSprites[0]));
        storeInMyNamespace(assets, "ghost_points_1", spriteSheet.image(numberSprites[1]));
        storeInMyNamespace(assets, "ghost_points_2", spriteSheet.image(numberSprites[2]));
        storeInMyNamespace(assets, "ghost_points_3", spriteSheet.image(numberSprites[3]));

        storeInMyNamespace(assets, "ghost.0.color.normal.dress",      ARCADE_RED);
        storeInMyNamespace(assets, "ghost.0.color.normal.eyeballs",   ARCADE_WHITE);
        storeInMyNamespace(assets, "ghost.0.color.normal.pupils",     ARCADE_BLUE);
        storeInMyNamespace(assets, "ghost.1.color.normal.dress",      ARCADE_PINK);
        storeInMyNamespace(assets, "ghost.1.color.normal.eyeballs",   ARCADE_WHITE);
        storeInMyNamespace(assets, "ghost.1.color.normal.pupils",     ARCADE_BLUE);
        storeInMyNamespace(assets, "ghost.2.color.normal.dress",      ARCADE_CYAN);
        storeInMyNamespace(assets, "ghost.2.color.normal.eyeballs",   ARCADE_WHITE);
        storeInMyNamespace(assets, "ghost.2.color.normal.pupils",     ARCADE_BLUE);
        storeInMyNamespace(assets, "ghost.3.color.normal.dress",      ARCADE_ORANGE);
        storeInMyNamespace(assets, "ghost.3.color.normal.eyeballs",   ARCADE_WHITE);
        storeInMyNamespace(assets, "ghost.3.color.normal.pupils",     ARCADE_BLUE);
        storeInMyNamespace(assets, "ghost.color.frightened.dress",    ARCADE_BLUE);
        storeInMyNamespace(assets, "ghost.color.frightened.eyeballs", ARCADE_ROSE);
        storeInMyNamespace(assets, "ghost.color.frightened.pupils",   ARCADE_ROSE);
        storeInMyNamespace(assets, "ghost.color.flashing.dress",      ARCADE_WHITE);
        storeInMyNamespace(assets, "ghost.color.flashing.eyeballs",   ARCADE_ROSE);
        storeInMyNamespace(assets, "ghost.color.flashing.pupils",     ARCADE_RED);

        soundManager.registerAudioClip(SoundID.BONUS_ACTIVE,          msPacManRes.url("sound/Fruit_Bounce.mp3"));
        soundManager.registerAudioClip(SoundID.BONUS_EATEN,           msPacManRes.url("sound/Fruit.mp3"));
        soundManager.registerAudioClip(SoundID.COIN_INSERTED,         msPacManRes.url("sound/credit.wav"));
        soundManager.registerAudioClip(SoundID.EXTRA_LIFE,            msPacManRes.url("sound/ExtraLife.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_OVER,           msPacManRes.url("sound/game-over.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_READY,          msPacManRes.url("sound/Start.mp3"));
        soundManager.registerAudioClip(SoundID.GHOST_EATEN,           msPacManRes.url("sound/Ghost.mp3"));
        soundManager.registerMediaPlayer(SoundID.GHOST_RETURNS,       msPacManRes.url("sound/GhostEyes.mp3"));
        soundManager.registerMediaPlayer("audio.intermission.1",      msPacManRes.url("sound/Act_1_They_Meet.mp3"));
        soundManager.registerMediaPlayer("audio.intermission.2",      msPacManRes.url("sound/Act_2_The_Chase.mp3"));
        soundManager.registerMediaPlayer("audio.intermission.3",      msPacManRes.url("sound/Act_3_Junior.mp3"));
        soundManager.registerAudioClip(SoundID.LEVEL_CHANGED,         msPacManRes.url("sound/sweep.mp3"));
        soundManager.registerMediaPlayer(SoundID.LEVEL_COMPLETE,      msPacManRes.url("sound/level-complete.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_DEATH,       msPacManRes.url("sound/Died.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_MUNCHING,    msPacManRes.url("sound/munch.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_POWER,       msPacManRes.url("sound/ScaredGhost.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_1,             msPacManRes.url("sound/GhostNoise1.wav"));
        soundManager.registerMediaPlayer(SoundID.SIREN_2,             msPacManRes.url("sound/GhostNoise1.wav"));// TODO
        soundManager.registerMediaPlayer(SoundID.SIREN_3,             msPacManRes.url("sound/GhostNoise1.wav"));// TODO
        soundManager.registerMediaPlayer(SoundID.SIREN_4,             msPacManRes.url("sound/GhostNoise1.wav"));// TODO

        storeInMyNamespace(assets, "audio.option.selection_changed",  msPacManXXLRes.loadAudioClip("sound/ms-select1.wav"));
        storeInMyNamespace(assets, "audio.option.value_changed",      msPacManXXLRes.loadAudioClip("sound/ms-select2.wav"));
    }

    @Override
    public void unloadAssets(AssetStorage assetStorage) {
        assetStorage.removeAll(NAMESPACE + ".");
    }

    @Override
    public String assetNamespace() {
        return NAMESPACE;
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() { return spriteSheet; }

    @Override
    public WorldMapColorScheme worldMapColorScheme(WorldMap worldMap) {
        Map<String, String> colorMap = worldMap.getConfigValue("colorMap");
        return new WorldMapColorScheme(
            colorMap.get("fill"), colorMap.get("stroke"), colorMap.get("door"), colorMap.get("pellet"));
    }

    @Override
    public PacManXXL_MsPacMan_GameRenderer createGameRenderer(Canvas canvas) {
        return new PacManXXL_MsPacMan_GameRenderer(spriteSheet, canvas);
    }

    @Override
    public SpriteAnimationMap<SpriteID> createGhostAnimations(Ghost ghost) {
        return new ArcadeMsPacMan_GhostAnimationMap(spriteSheet, ghost.personality());
    }

    @Override
    public SpriteAnimationMap<SpriteID> createPacAnimations(Pac pac) {
        return new ArcadeMsPacMan_PacAnimationMap(spriteSheet);
    }

    @Override
    public Image bonusSymbolImage(byte symbol) {
        return theAssets().image(NAMESPACE + ".bonus_symbol_" + symbol);
    }

    @Override
    public Image bonusValueImage(byte symbol) {
        return theAssets().image(NAMESPACE + ".bonus_value_" + symbol);
    }

    @Override
    public MsPacManBody createLivesCounterShape3D(Model3DRepository model3DRepository) {
        return model3DRepository.createMsPacManBody(
            PacManGames_UI.LIVES_COUNTER_3D_SHAPE_SIZE,
            theAssets().color(NAMESPACE + ".pac.color.head"),
            theAssets().color(NAMESPACE + ".pac.color.eyes"),
            theAssets().color(NAMESPACE + ".pac.color.palate"),
            theAssets().color(NAMESPACE + ".pac.color.hairbow"),
            theAssets().color(NAMESPACE + ".pac.color.hairbow.pearls"),
            theAssets().color(NAMESPACE + ".pac.color.boobs")
        );
    }

    @Override
    public PacBase3D createPac3D(Model3DRepository model3DRepository, AnimationManager animationManager, Pac pac) {
        var pac3D = new MsPacMan3D(model3DRepository, animationManager, pac, PacManGames_UI.PAC_3D_SIZE,
            theAssets().color(NAMESPACE + ".pac.color.head"),
            theAssets().color(NAMESPACE + ".pac.color.eyes"),
            theAssets().color(NAMESPACE + ".pac.color.palate"),
            theAssets().color(NAMESPACE + ".pac.color.hairbow"),
            theAssets().color(NAMESPACE + ".pac.color.hairbow.pearls"),
            theAssets().color(NAMESPACE + ".pac.color.boobs"));
        pac3D.light().setColor(theAssets().color(NAMESPACE + ".pac.color.head").desaturate());
        return pac3D;
    }

    // Game scenes

    @Override
    public void createGameScenes() {
        scenesByID.put("BootScene",   new ArcadeCommon_BootScene2D());
        scenesByID.put("IntroScene",  new ArcadeMsPacMan_IntroScene());
        scenesByID.put("StartScene",  new ArcadeMsPacMan_StartScene());
        scenesByID.put("PlayScene2D", new ArcadeCommon_PlayScene2D());
        scenesByID.put("PlayScene3D", new PlayScene3D());
        scenesByID.put("CutScene1",   new ArcadeMsPacMan_CutScene1());
        scenesByID.put("CutScene2",   new ArcadeMsPacMan_CutScene2());
        scenesByID.put("CutScene3",   new ArcadeMsPacMan_CutScene3());
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

    @Override
    public GameScene selectGameScene(GameModel game, GameState gameState) {
        String sceneID = switch (gameState) {
            case GameState.BOOT               -> "BootScene";
            case GameState.SETTING_OPTIONS    -> "StartScene";
            case GameState.INTRO              -> "IntroScene";
            case GameState.INTERMISSION       -> {
                if (optGameLevel().isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene, no game level available");
                }
                int levelNumber = theGameLevel().number();
                if (game.cutSceneNumber(levelNumber).isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene after level %d".formatted(levelNumber));
                }
                yield "CutScene" + game.cutSceneNumber(levelNumber).getAsInt();
            }
            case GameState.TESTING_CUT_SCENES -> "CutScene" + game.<Integer>getProperty("intermissionTestNumber");
            default -> PY_3D_ENABLED.get() ?  "PlayScene3D" : "PlayScene2D";
        };
        return scenesByID.get(sceneID);
    }

    @Override
    public SoundManager soundManager() {
        return soundManager;
    }
}