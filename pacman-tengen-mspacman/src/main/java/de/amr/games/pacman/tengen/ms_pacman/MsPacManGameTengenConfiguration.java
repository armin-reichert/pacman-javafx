/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme;
import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.ui.assets.AssetStorage;
import de.amr.games.pacman.ui.assets.GameSpriteSheet;
import de.amr.games.pacman.ui.assets.ResourceManager;
import de.amr.games.pacman.ui.assets.WorldMapColoring;
import de.amr.games.pacman.ui.scene.GameScene;
import de.amr.games.pacman.ui.scene.GameConfiguration;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.tengen.ms_pacman.GlobalProperties.PY_TENGEN_PLAY_SCENE_DISPLAY_MODE;
import static de.amr.games.pacman.ui.GameContext.PFX_MS_PACMAN_TENGEN;
import static de.amr.games.pacman.ui.lib.Ufx.coloredBackground;

public class MsPacManGameTengenConfiguration implements GameConfiguration {

    public static final Vector2i NES_TILES = new Vector2i(32, 30);
    public static final Vector2i NES_SIZE = NES_TILES.scaled(TS);

    private final AssetStorage assets;
    private final MsPacManGameTengenSpriteSheet spriteSheet;
    private final MazeRepository mazeRepository;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public MsPacManGameTengenConfiguration(AssetStorage assets) {
        this.assets = assets;
        loadAssets(() -> MsPacManGameTengenConfiguration.class);

        spriteSheet = new MsPacManGameTengenSpriteSheet(assets.image(PFX_MS_PACMAN_TENGEN + ".spritesheet"));
        mazeRepository = new MazeRepository(
            assets.image(PFX_MS_PACMAN_TENGEN + ".mazes.arcade"),
            assets.image(PFX_MS_PACMAN_TENGEN + ".mazes.non_arcade"));

        set("BootScene",      new BootScene());
        set("IntroScene",     new IntroScene());
        set("StartScene",     new OptionsScene());
        set("ShowingCredits", new CreditsScene());
        set("PlayScene2D",    new PlayScene2D());
        set("CutScene1",      new CutScene1());
        set("CutScene2",      new CutScene2());
        set("CutScene3",      new CutScene3());
        set("CutScene4",      new CutScene4());

        //TODO where is the best place to do that?
        PlayScene2D playScene2D = (PlayScene2D) get("PlayScene2D");
        playScene2D.displayModeProperty().bind(PY_TENGEN_PLAY_SCENE_DISPLAY_MODE);
    }

    public static Color nesPaletteColor(int index) {
        return Color.web(NES.Palette.color(index));
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
        return get(sceneID);
    }

    @Override
    public void set(String id, GameScene gameScene) {
        scenesByID.put(id, gameScene);
    }

    @Override
    public GameScene get(String id) {
        return scenesByID.get(id);
    }

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }

    @Override
    public GameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public MsPacManGameTengenRenderer createRenderer(Canvas canvas) {
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

    private void loadAssets(ResourceManager rm) {
        assets.store(PFX_MS_PACMAN_TENGEN + ".scene_background",                 coloredBackground(Color.BLACK));

        assets.store(PFX_MS_PACMAN_TENGEN + ".spritesheet",                      rm.loadImage("graphics/spritesheet.png"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".mazes.arcade",                     rm.loadImage("graphics/arcade_mazes.png"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".mazes.non_arcade",                 rm.loadImage("graphics/non_arcade_mazes.png"));

        assets.store(PFX_MS_PACMAN_TENGEN + ".startpage.image1",                 rm.loadImage("graphics/f1.png"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".startpage.image2",                 rm.loadImage("graphics/f2.png"));

        assets.store(PFX_MS_PACMAN_TENGEN + ".icon",                             rm.loadImage("graphics/icons/mspacman.png"));

        assets.store(PFX_MS_PACMAN_TENGEN + ".color.game_over_message",          nesPaletteColor(0x11));
        assets.store(PFX_MS_PACMAN_TENGEN + ".color.ready_message",              nesPaletteColor(0x28));
        assets.store(PFX_MS_PACMAN_TENGEN + ".color.clapperboard",               nesPaletteColor(0x20));

        assets.store(PFX_MS_PACMAN_TENGEN + ".pac.color.head",                   nesPaletteColor(0x28));
        assets.store(PFX_MS_PACMAN_TENGEN + ".pac.color.eyes",                   nesPaletteColor(0x02));
        assets.store(PFX_MS_PACMAN_TENGEN + ".pac.color.palate",                 nesPaletteColor(0x2d));
        assets.store(PFX_MS_PACMAN_TENGEN + ".pac.color.boobs",                  nesPaletteColor(0x28).deriveColor(0, 1.0, 0.96, 1.0));
        assets.store(PFX_MS_PACMAN_TENGEN + ".pac.color.hairbow",                nesPaletteColor(0x05));
        assets.store(PFX_MS_PACMAN_TENGEN + ".pac.color.hairbow.pearls",         nesPaletteColor(0x02));

        assets.store(PFX_MS_PACMAN_TENGEN + ".ghost.0.color.normal.dress",       nesPaletteColor(0x05));
        assets.store(PFX_MS_PACMAN_TENGEN + ".ghost.0.color.normal.eyeballs",    nesPaletteColor(0x20));
        assets.store(PFX_MS_PACMAN_TENGEN + ".ghost.0.color.normal.pupils",      nesPaletteColor(0x16));

        assets.store(PFX_MS_PACMAN_TENGEN + ".ghost.1.color.normal.dress",       nesPaletteColor(0x25));
        assets.store(PFX_MS_PACMAN_TENGEN + ".ghost.1.color.normal.eyeballs",    nesPaletteColor(0x20));
        assets.store(PFX_MS_PACMAN_TENGEN + ".ghost.1.color.normal.pupils",      nesPaletteColor(0x11));

        assets.store(PFX_MS_PACMAN_TENGEN + ".ghost.2.color.normal.dress",       nesPaletteColor(0x11));
        assets.store(PFX_MS_PACMAN_TENGEN + ".ghost.2.color.normal.eyeballs",    nesPaletteColor(0x20));
        assets.store(PFX_MS_PACMAN_TENGEN + ".ghost.2.color.normal.pupils",      nesPaletteColor(0x11));

        assets.store(PFX_MS_PACMAN_TENGEN + ".ghost.3.color.normal.dress",       nesPaletteColor(0x16));
        assets.store(PFX_MS_PACMAN_TENGEN + ".ghost.3.color.normal.eyeballs",    nesPaletteColor(0x20));
        assets.store(PFX_MS_PACMAN_TENGEN + ".ghost.3.color.normal.pupils",      nesPaletteColor(0x05));

        assets.store(PFX_MS_PACMAN_TENGEN + ".ghost.color.frightened.dress",     nesPaletteColor(0x01));
        assets.store(PFX_MS_PACMAN_TENGEN + ".ghost.color.frightened.eyeballs",  nesPaletteColor(0x20));
        assets.store(PFX_MS_PACMAN_TENGEN + ".ghost.color.frightened.pupils",    nesPaletteColor(0x20));

        //TODO has two flashing colors, when to use which?
        assets.store(PFX_MS_PACMAN_TENGEN + ".ghost.color.flashing.dress",       nesPaletteColor(0x20));
        assets.store(PFX_MS_PACMAN_TENGEN + ".ghost.color.flashing.eyeballs",    nesPaletteColor(0x20));
        assets.store(PFX_MS_PACMAN_TENGEN + ".ghost.color.flashing.pupils",      nesPaletteColor(0x20));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.option.selection_changed",   rm.loadAudioClip("sound/ms-select1.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.option.value_changed",       rm.loadAudioClip("sound/ms-select2.wav"));

        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.bonus_eaten",                rm.loadAudioClip("sound/ms-fruit.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.extra_life",                 rm.loadAudioClip("sound/ms-extralife.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.ghost_eaten",                rm.loadAudioClip("sound/ms-ghosteat.wav"));

        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.intermission.4.junior.1",    rm.loadAudioClip("sound/ms-theend1.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.intermission.4.junior.2",    rm.loadAudioClip("sound/ms-theend2.wav"));


        // used only in 3D scene when level is completed:
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.level_complete",             rm.url("sound/common/level-complete.mp3"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.sweep",                      rm.loadAudioClip("sound/common/sweep.mp3"));

        // Audio played by MediaPlayer
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.game_ready",                 rm.url("sound/ms-start.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.intermission.1",             rm.url("sound/theymeet.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.intermission.2",             rm.url("sound/thechase.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.intermission.3",             rm.url("sound/junior.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.intermission.4",             rm.url("sound/theend.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.pacman_death",               rm.url("sound/ms-death.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.pacman_munch",               rm.url("sound/ms-dot.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.pacman_power",               rm.url("sound/ms-power.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.siren.1",                    rm.url("sound/ms-siren1.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.siren.2",                    rm.url("sound/ms-siren2.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.siren.3",                    rm.url("sound/ms-siren2.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.siren.4",                    rm.url("sound/ms-siren2.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.ghost_returns",              rm.url("sound/ms-eyes.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.bonus_bouncing",             rm.url("sound/fruitbounce.wav"));
    }
}