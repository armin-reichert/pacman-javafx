/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.PacManGames2dApp;
import de.amr.games.pacman.ui2d.PacManGamesUI;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameSceneConfig;
import de.amr.games.pacman.ui2d.scene.common.WorldMapColoring;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.PacManGamesUI.PFX_MS_PACMAN_TENGEN;
import static de.amr.games.pacman.ui2d.util.Ufx.coloredBackground;

public class MsPacManGameTengenSceneConfig implements GameSceneConfig {

    public static final Vector2i NES_TILES = new Vector2i(32, 30);
    public static final Vector2i NES_SIZE = NES_TILES.scaled(TS);

    private final AssetStorage assets;
    private final MsPacManGameTengenSpriteSheet spriteSheet;
    private final SpriteSheet_ArcadeMaps arcadeMapSprites;
    private final SpriteSheet_NonArcadeMaps nonArcadeMapSprites;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public MsPacManGameTengenSceneConfig(AssetStorage assets) {
        this.assets = assets;
        loadAssets(() -> PacManGamesUI.class);

        spriteSheet = new MsPacManGameTengenSpriteSheet(assets.image(PFX_MS_PACMAN_TENGEN + ".spritesheet"));
        arcadeMapSprites = new SpriteSheet_ArcadeMaps(assets.image(PFX_MS_PACMAN_TENGEN + ".mazes.arcade"));
        nonArcadeMapSprites = new SpriteSheet_NonArcadeMaps(assets.image(PFX_MS_PACMAN_TENGEN + ".mazes.non_arcade"));

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
        playScene2D.displayModeProperty().bind(PacManGames2dApp.PY_TENGEN_PLAY_SCENE_DISPLAY_MODE);
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
        return new MsPacManGameTengenRenderer(assets, spriteSheet, arcadeMapSprites, nonArcadeMapSprites, canvas);
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

        assets.store(PFX_MS_PACMAN_TENGEN + ".spritesheet",                      rm.loadImage("graphics/tengen/spritesheet.png"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".mazes.arcade",                     rm.loadImage("graphics/tengen/arcade_mazes.png"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".mazes.non_arcade",                 rm.loadImage("graphics/tengen/non_arcade_mazes.png"));

        assets.store(PFX_MS_PACMAN_TENGEN + ".startpage.image1",                 rm.loadImage("graphics/tengen/f1.png"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".startpage.image2",                 rm.loadImage("graphics/tengen/f2.png"));

        assets.store(PFX_MS_PACMAN_TENGEN + ".helpButton.icon",                  rm.loadImage("graphics/icons/help-red-64.png"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".icon",                             rm.loadImage("graphics/icons/mspacman.png"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".logo.midway",                      rm.loadImage("graphics/mspacman/midway_logo.png"));

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
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.option.selection_changed",   rm.loadAudioClip("sound/tengen/ms-select1.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.option.value_changed",       rm.loadAudioClip("sound/tengen/ms-select2.wav"));

        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.bonus_eaten",                rm.loadAudioClip("sound/tengen/ms-fruit.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.extra_life",                 rm.loadAudioClip("sound/tengen/ms-extralife.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.ghost_eaten",                rm.loadAudioClip("sound/tengen/ms-ghosteat.wav"));

        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.intermission.4.junior.1",    rm.loadAudioClip("sound/tengen/ms-theend1.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.intermission.4.junior.2",    rm.loadAudioClip("sound/tengen/ms-theend2.wav"));


        // used only in 3D scene when level is completed:
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.level_complete",             rm.url("sound/common/level-complete.mp3"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.sweep",                      rm.loadAudioClip("sound/common/sweep.mp3"));

        // Audio played by MediaPlayer
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.game_ready",                 rm.url("sound/tengen/ms-start.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.intermission.1",             rm.url("sound/tengen/theymeet.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.intermission.2",             rm.url("sound/tengen/thechase.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.intermission.3",             rm.url("sound/tengen/junior.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.intermission.4",             rm.url("sound/tengen/theend.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.pacman_death",               rm.url("sound/tengen/ms-death.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.pacman_munch",               rm.url("sound/tengen/ms-dot.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.pacman_power",               rm.url("sound/tengen/ms-power.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.siren.1",                    rm.url("sound/tengen/ms-siren1.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.siren.2",                    rm.url("sound/tengen/ms-siren2.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.siren.3",                    rm.url("sound/tengen/ms-siren2.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.siren.4",                    rm.url("sound/tengen/ms-siren2.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.ghost_returns",              rm.url("sound/tengen/ms-eyes.wav"));
        assets.store(PFX_MS_PACMAN_TENGEN + ".audio.bonus_bouncing",             rm.url("sound/tengen/fruitbounce.wav"));
    }
}