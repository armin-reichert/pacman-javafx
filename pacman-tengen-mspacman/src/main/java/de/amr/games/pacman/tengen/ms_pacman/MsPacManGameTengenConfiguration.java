/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.nes.NES_ColorScheme;
import de.amr.games.pacman.lib.nes.NES_Palette;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.assets.AssetStorage;
import de.amr.games.pacman.ui2d.assets.GameSpriteSheet;
import de.amr.games.pacman.ui2d.assets.ResourceManager;
import de.amr.games.pacman.ui2d.assets.WorldMapColoring;
import de.amr.games.pacman.ui2d.scene.GameConfiguration;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.tengen.ms_pacman.GlobalPropertiesTengen.PY_TENGEN_PLAY_SCENE_DISPLAY_MODE;

public class MsPacManGameTengenConfiguration implements GameConfiguration {

    public static Color nesPaletteColor(int index) {
        return Color.web(NES_Palette.color(index));
    }

    public static final Vector2i NES_TILES = new Vector2i(32, 30);
    public static final Vector2i NES_SIZE = NES_TILES.scaled(TS);

    private final MsPacManGameTengenSpriteSheet spriteSheet;
    private final MazeRepository mazeRepository;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public MsPacManGameTengenConfiguration(AssetStorage assets) {
        loadAssets(() -> MsPacManGameTengenConfiguration.class, assets);
        spriteSheet = new MsPacManGameTengenSpriteSheet(assets.image(assetKeyPrefix() + ".spritesheet"));
        mazeRepository = new MazeRepository(
            assets.image(assetKeyPrefix() + ".mazes.arcade"),
            assets.image(assetKeyPrefix() + ".mazes.non_arcade"));

        setGameScene("BootScene",      new BootScene());
        setGameScene("IntroScene",     new IntroScene());
        setGameScene("StartScene",     new OptionsScene());
        setGameScene("ShowingCredits", new CreditsScene());
        setGameScene("PlayScene2D",    new PlayScene2D());
        setGameScene("CutScene1",      new CutScene1());
        setGameScene("CutScene2",      new CutScene2());
        setGameScene("CutScene3",      new CutScene3());
        setGameScene("CutScene4",      new CutScene4());

        //TODO where is the best place to do that?
        PlayScene2D playScene2D = (PlayScene2D) getGameScene("PlayScene2D");
        playScene2D.displayModeProperty().bind(PY_TENGEN_PLAY_SCENE_DISPLAY_MODE);
    }

    @Override
    public String assetKeyPrefix() {
        return "tengen";
    }

    @Override
    public GameScene selectGameScene(GameContext context) {
        String sceneID = switch (context.gameState()) {
            case BOOT               -> "BootScene";
            case SETTING_OPTIONS    -> "StartScene";
            case SHOWING_CREDITS    -> "ShowingCredits";
            case INTRO              -> "IntroScene";
            case INTERMISSION       -> "CutScene" + context.level().intermissionNumber();
            case TESTING_CUT_SCENES -> "CutScene" + context.gameState().<Integer>getProperty("intermissionTestNumber");
            default                 -> "PlayScene2D";
        };
        return getGameScene(sceneID);
    }

    @Override
    public void setGameScene(String id, GameScene gameScene) {
        scenesByID.put(id, gameScene);
    }

    @Override
    public GameScene getGameScene(String id) {
        return scenesByID.get(id);
    }

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }

    @Override
    public GameScene2D createPiPScene(GameContext context, Canvas canvasNotUsed) {
        var gameScene = new TengenPiP_PlayScene();
        gameScene.setGameContext(context);
        gameScene.setGameRenderer(createRenderer(context.assets(), gameScene.canvas()));
        return gameScene;
    }

    @Override
    public boolean isGameCanvasDecorated() { return false; }

    @Override
    public GameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public MsPacManGameTengenRenderer createRenderer(AssetStorage assets, Canvas canvas) {
        return new MsPacManGameTengenRenderer(assets, spriteSheet, mazeRepository, canvas);
    }

    @Override
    public WorldMapColoring worldMapColoring(WorldMap worldMap) {
        return new WorldMapColoring((NES_ColorScheme) worldMap.getConfigValue("nesColorScheme"));
    }

    @Override
    public void createActorAnimations(GameLevel level) {
        level.pac().setAnimations(new PacAnimations(spriteSheet));
        level.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id())));
    }

    private void loadAssets(ResourceManager rm, AssetStorage assets) {
        assets.store(assetKeyPrefix() + ".spritesheet",                      rm.loadImage("graphics/spritesheet.png"));
        assets.store(assetKeyPrefix() + ".mazes.arcade",                     rm.loadImage("graphics/arcade_mazes.png"));
        assets.store(assetKeyPrefix() + ".mazes.non_arcade",                 rm.loadImage("graphics/non_arcade_mazes.png"));

        assets.store(assetKeyPrefix() + ".startpage.image1",                 rm.loadImage("graphics/f1.png"));
        assets.store(assetKeyPrefix() + ".startpage.image2",                 rm.loadImage("graphics/f2.png"));

        assets.store(assetKeyPrefix() + ".icon",                             rm.loadImage("graphics/icons/mspacman.png"));

        assets.store(assetKeyPrefix() + ".color.game_over_message",          nesPaletteColor(0x11));
        assets.store(assetKeyPrefix() + ".color.ready_message",              nesPaletteColor(0x28));

        assets.store(assetKeyPrefix() + ".pac.color.head",                   nesPaletteColor(0x28));
        assets.store(assetKeyPrefix() + ".pac.color.eyes",                   nesPaletteColor(0x02));
        assets.store(assetKeyPrefix() + ".pac.color.palate",                 nesPaletteColor(0x2d));
        assets.store(assetKeyPrefix() + ".pac.color.boobs",                  nesPaletteColor(0x28).deriveColor(0, 1.0, 0.96, 1.0));
        assets.store(assetKeyPrefix() + ".pac.color.hairbow",                nesPaletteColor(0x05));
        assets.store(assetKeyPrefix() + ".pac.color.hairbow.pearls",         nesPaletteColor(0x02));

        assets.store(assetKeyPrefix() + ".ghost.0.color.normal.dress",       nesPaletteColor(0x05));
        assets.store(assetKeyPrefix() + ".ghost.0.color.normal.eyeballs",    nesPaletteColor(0x20));
        assets.store(assetKeyPrefix() + ".ghost.0.color.normal.pupils",      nesPaletteColor(0x16));

        assets.store(assetKeyPrefix() + ".ghost.1.color.normal.dress",       nesPaletteColor(0x25));
        assets.store(assetKeyPrefix() + ".ghost.1.color.normal.eyeballs",    nesPaletteColor(0x20));
        assets.store(assetKeyPrefix() + ".ghost.1.color.normal.pupils",      nesPaletteColor(0x11));

        assets.store(assetKeyPrefix() + ".ghost.2.color.normal.dress",       nesPaletteColor(0x11));
        assets.store(assetKeyPrefix() + ".ghost.2.color.normal.eyeballs",    nesPaletteColor(0x20));
        assets.store(assetKeyPrefix() + ".ghost.2.color.normal.pupils",      nesPaletteColor(0x11));

        assets.store(assetKeyPrefix() + ".ghost.3.color.normal.dress",       nesPaletteColor(0x16));
        assets.store(assetKeyPrefix() + ".ghost.3.color.normal.eyeballs",    nesPaletteColor(0x20));
        assets.store(assetKeyPrefix() + ".ghost.3.color.normal.pupils",      nesPaletteColor(0x05));

        assets.store(assetKeyPrefix() + ".ghost.color.frightened.dress",     nesPaletteColor(0x01));
        assets.store(assetKeyPrefix() + ".ghost.color.frightened.eyeballs",  nesPaletteColor(0x20));
        assets.store(assetKeyPrefix() + ".ghost.color.frightened.pupils",    nesPaletteColor(0x20));

        //TODO has two flashing colors, when to use which?
        assets.store(assetKeyPrefix() + ".ghost.color.flashing.dress",       nesPaletteColor(0x20));
        assets.store(assetKeyPrefix() + ".ghost.color.flashing.eyeballs",    nesPaletteColor(0x20));
        assets.store(assetKeyPrefix() + ".ghost.color.flashing.pupils",      nesPaletteColor(0x20));
        assets.store(assetKeyPrefix() + ".audio.option.selection_changed",   rm.loadAudioClip("sound/ms-select1.wav"));
        assets.store(assetKeyPrefix() + ".audio.option.value_changed",       rm.loadAudioClip("sound/ms-select2.wav"));

        assets.store(assetKeyPrefix() + ".audio.bonus_eaten",                rm.loadAudioClip("sound/ms-fruit.wav"));
        assets.store(assetKeyPrefix() + ".audio.extra_life",                 rm.loadAudioClip("sound/ms-extralife.wav"));
        assets.store(assetKeyPrefix() + ".audio.ghost_eaten",                rm.loadAudioClip("sound/ms-ghosteat.wav"));

        assets.store(assetKeyPrefix() + ".audio.intermission.4.junior.1",    rm.loadAudioClip("sound/ms-theend1.wav"));
        assets.store(assetKeyPrefix() + ".audio.intermission.4.junior.2",    rm.loadAudioClip("sound/ms-theend2.wav"));


        // used only in 3D scene when level is completed:
        assets.store(assetKeyPrefix() + ".audio.level_complete",             rm.url("sound/common/level-complete.mp3"));
        assets.store(assetKeyPrefix() + ".audio.sweep",                      rm.loadAudioClip("sound/common/sweep.mp3"));

        // Audio played by MediaPlayer
        assets.store(assetKeyPrefix() + ".audio.game_ready",                 rm.url("sound/ms-start.wav"));
        assets.store(assetKeyPrefix() + ".audio.intermission.1",             rm.url("sound/theymeet.wav"));
        assets.store(assetKeyPrefix() + ".audio.intermission.2",             rm.url("sound/thechase.wav"));
        assets.store(assetKeyPrefix() + ".audio.intermission.3",             rm.url("sound/junior.wav"));
        assets.store(assetKeyPrefix() + ".audio.intermission.4",             rm.url("sound/theend.wav"));
        assets.store(assetKeyPrefix() + ".audio.pacman_death",               rm.url("sound/ms-death.wav"));
        assets.store(assetKeyPrefix() + ".audio.pacman_munch",               rm.url("sound/ms-dot.wav"));
        assets.store(assetKeyPrefix() + ".audio.pacman_power",               rm.url("sound/ms-power.wav"));
        assets.store(assetKeyPrefix() + ".audio.siren.1",                    rm.url("sound/ms-siren1.wav"));
        assets.store(assetKeyPrefix() + ".audio.siren.2",                    rm.url("sound/ms-siren2.wav"));
        assets.store(assetKeyPrefix() + ".audio.siren.3",                    rm.url("sound/ms-siren2.wav"));
        assets.store(assetKeyPrefix() + ".audio.siren.4",                    rm.url("sound/ms-siren2.wav"));
        assets.store(assetKeyPrefix() + ".audio.ghost_returns",              rm.url("sound/ms-eyes.wav"));
        assets.store(assetKeyPrefix() + ".audio.bonus_bouncing",             rm.url("sound/fruitbounce.wav"));
    }
}