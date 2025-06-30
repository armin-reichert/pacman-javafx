/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.arcade.pacman.rendering.*;
import de.amr.pacmanfx.arcade.pacman.scenes.*;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.PacManGames_Assets;
import de.amr.pacmanfx.ui.PacManGames_UIConfig;
import de.amr.pacmanfx.ui._3d.PacMan3D;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui._3d.Settings3D;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.optGameLevel;
import static de.amr.pacmanfx.Globals.theGameLevel;
import static de.amr.pacmanfx.arcade.pacman.rendering.ArcadePalette.*;
import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static de.amr.pacmanfx.ui.PacManGames_UI.PY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_UIConfig implements PacManGames_UIConfig, ResourceManager {

    private static final String ANS = "pacman";

    public static final Vector2i ARCADE_MAP_SIZE_IN_TILES  = new Vector2i(28, 36);
    public static final Vector2f ARCADE_MAP_SIZE_IN_PIXELS = new Vector2f(224, 288);

    public static final String ANIM_BIG_PAC_MAN               = "big_pac_man";
    public static final String ANIM_BLINKY_DAMAGED            = "blinky_damaged";
    public static final String ANIM_BLINKY_PATCHED            = "blinky_patched";
    public static final String ANIM_BLINKY_NAIL_DRESS_RAPTURE = "blinky_nail_dress_rapture";
    public static final String ANIM_BLINKY_NAKED              = "blinky_naked";

    private static final WorldMapColorScheme MAP_COLORING = new WorldMapColorScheme("#000000", "#2121ff", "#fcb5ff", "#febdb4");

    private final ArcadePacMan_SpriteSheet spriteSheet;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    @Override
    public Class<?> resourceRootClass() {
        return ArcadePacMan_UIConfig.class;
    }

    public ArcadePacMan_UIConfig(PacManGames_Assets assets) {
        spriteSheet = new ArcadePacMan_SpriteSheet(loadImage("graphics/pacman_spritesheet.png"));

        storeLocalAsset(assets, "app_icon",                        loadImage("graphics/icons/pacman.png"));
        storeLocalAsset(assets, "flashing_maze",                   loadImage("graphics/maze_flashing.png"));

        storeLocalAsset(assets, "startpage.image1",                loadImage("graphics/f1.jpg"));
        storeLocalAsset(assets, "startpage.image2",                loadImage("graphics/f2.jpg"));
        storeLocalAsset(assets, "startpage.image3",                loadImage("graphics/f3.jpg"));

        storeLocalAsset(assets, "color.game_over_message",         ARCADE_RED);

        storeLocalAsset(assets, "pac.color.head",                  ARCADE_YELLOW);
        storeLocalAsset(assets, "pac.color.eyes",                  Color.grayRgb(33));
        storeLocalAsset(assets, "pac.color.palate",                Color.rgb(240, 180, 160));

        Sprite[] numberSprites = spriteSheet.spriteSeq(SpriteID.GHOST_NUMBERS);
        storeLocalAsset(assets, "ghost_points_0", spriteSheet.image(numberSprites[0]));
        storeLocalAsset(assets, "ghost_points_1", spriteSheet.image(numberSprites[1]));
        storeLocalAsset(assets, "ghost_points_2", spriteSheet.image(numberSprites[2]));
        storeLocalAsset(assets, "ghost_points_3", spriteSheet.image(numberSprites[3]));

        storeLocalAsset(assets, "ghost.0.color.normal.dress",      ARCADE_RED);
        storeLocalAsset(assets, "ghost.0.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAsset(assets, "ghost.0.color.normal.pupils",     ARCADE_BLUE);
        storeLocalAsset(assets, "ghost.1.color.normal.dress",      ARCADE_PINK);
        storeLocalAsset(assets, "ghost.1.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAsset(assets, "ghost.1.color.normal.pupils",     ARCADE_BLUE);
        storeLocalAsset(assets, "ghost.2.color.normal.dress",      ARCADE_CYAN);
        storeLocalAsset(assets, "ghost.2.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAsset(assets, "ghost.2.color.normal.pupils",     ARCADE_BLUE);
        storeLocalAsset(assets, "ghost.3.color.normal.dress",      ARCADE_ORANGE);
        storeLocalAsset(assets, "ghost.3.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAsset(assets, "ghost.3.color.normal.pupils",     ARCADE_BLUE);
        storeLocalAsset(assets, "ghost.color.frightened.dress",    ARCADE_BLUE);
        storeLocalAsset(assets, "ghost.color.frightened.eyeballs", ARCADE_ROSE);
        storeLocalAsset(assets, "ghost.color.frightened.pupils",   ARCADE_ROSE);
        storeLocalAsset(assets, "ghost.color.flashing.dress",      ARCADE_WHITE);
        storeLocalAsset(assets, "ghost.color.flashing.eyeballs",   ARCADE_ROSE);
        storeLocalAsset(assets, "ghost.color.flashing.pupils",     ARCADE_RED);

        // Clips
        storeLocalAsset(assets, "audio.bonus_eaten",               loadAudioClip("sound/eat_fruit.mp3"));
        storeLocalAsset(assets, "audio.credit",                    loadAudioClip("sound/credit.wav"));
        storeLocalAsset(assets, "audio.extra_life",                loadAudioClip("sound/extend.mp3"));
        storeLocalAsset(assets, "audio.ghost_eaten",               loadAudioClip("sound/eat_ghost.mp3"));
        storeLocalAsset(assets, "audio.sweep",                     loadAudioClip("sound/common/sweep.mp3"));

        // Media player URL
        storeLocalAsset(assets, "audio.game_ready",                url("sound/game_start.mp3"));
        storeLocalAsset(assets, "audio.game_over",                 url("sound/common/game-over.mp3"));
        storeLocalAsset(assets, "audio.intermission",              url("sound/intermission.mp3"));
        storeLocalAsset(assets, "audio.pacman_death",              url("sound/pacman_death.wav"));
        storeLocalAsset(assets, "audio.pacman_munch",              url("sound/munch.wav"));
        storeLocalAsset(assets, "audio.pacman_power",              url("sound/ghost-turn-to-blue.mp3"));
        storeLocalAsset(assets, "audio.level_complete",            url("sound/common/level-complete.mp3"));
        storeLocalAsset(assets, "audio.siren.1",                   url("sound/siren_1.mp3"));
        storeLocalAsset(assets, "audio.siren.2",                   url("sound/siren_2.mp3"));
        storeLocalAsset(assets, "audio.siren.3",                   url("sound/siren_3.mp3"));
        storeLocalAsset(assets, "audio.siren.4",                   url("sound/siren_4.mp3"));
        storeLocalAsset(assets, "audio.ghost_returns",             url("sound/retreating.mp3"));
    }

    @Override
    public String assetNamespace() {
        return ANS;
    }

    @Override
    public ArcadePacMan_GameRenderer createGameRenderer(Canvas canvas) {
        return new ArcadePacMan_GameRenderer(spriteSheet, canvas);
    }

    @Override
    public WorldMapColorScheme worldMapColorScheme(WorldMap worldMap) {
        return MAP_COLORING;
    }

    @Override
    public SpriteSheet<SpriteID> spriteSheet() {return spriteSheet;}

    @Override
    public SpriteAnimationMap<SpriteID> createGhostAnimations(Ghost ghost) {
        return new ArcadePacMan_GhostAnimationMap(spriteSheet, ghost.personality());
    }

    @Override
    public SpriteAnimationMap<SpriteID> createPacAnimations(Pac pac) {
        return new ArcadePacMan_PacAnimationMap(spriteSheet);
    }

    @Override
    public Image createBonusSymbolImage(byte symbol) {
        Sprite[] symbolSprites = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS);
        return spriteSheet.image(symbolSprites[symbol]);
    }

    @Override
    public Image createBonusValueImage(byte symbol) {
        Sprite[] valueSprites = spriteSheet.spriteSeq(SpriteID.BONUS_VALUES);
        return spriteSheet.image(valueSprites[symbol]);
    }

    @Override
    public Node createLivesCounter3D() {
        return Model3DRepository.get().createPacShape(
                Settings3D.LIVES_COUNTER_3D_SHAPE_SIZE,
                theAssets().color(ANS + ".pac.color.head"),
                theAssets().color(ANS + ".pac.color.eyes"),
                theAssets().color(ANS + ".pac.color.palate")
        );
    }

    @Override
    public PacBase3D createPac3D(AnimationManager animationMgr, Pac pac) {
        var pac3D = new PacMan3D(animationMgr, pac, Settings3D.PAC_3D_SIZE, theAssets(), ANS);
        pac3D.light().setColor(theAssets().color(ANS + ".pac.color.head").desaturate());
        return pac3D;
    }

    // Game scene config

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }

    @Override
    public void createGameScenes() {
        scenesByID.put("BootScene",   new ArcadeCommon_BootScene2D());
        scenesByID.put("IntroScene",  new ArcadePacMan_IntroScene());
        scenesByID.put("StartScene",  new ArcadePacMan_StartScene());
        scenesByID.put("PlayScene2D", new ArcadeCommon_PlayScene2D());
        scenesByID.put("PlayScene3D", new PlayScene3D());
        scenesByID.put("CutScene1",   new ArcadePacMan_CutScene1());
        scenesByID.put("CutScene2",   new ArcadePacMan_CutScene2());
        scenesByID.put("CutScene3",   new ArcadePacMan_CutScene3());
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
    public boolean gameSceneHasID(GameScene gameScene, String sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        return scenesByID.get(sceneID) == gameScene;
    }
}