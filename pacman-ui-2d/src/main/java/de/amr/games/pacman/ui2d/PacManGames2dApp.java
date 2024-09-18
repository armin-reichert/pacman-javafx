/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.pacmanxxl.MapSelectionMode;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.pacman.PacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.tengen.TengenMsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.*;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.GameClockFX;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author Armin Reichert
 */
public class PacManGames2dApp extends Application {

    public static final BooleanProperty PY_AUTOPILOT           = new SimpleBooleanProperty(false);
    public static final ObjectProperty<Color> PY_CANVAS_COLOR  = new SimpleObjectProperty<>(Color.BLACK);
    public static final BooleanProperty PY_CANVAS_DECORATED    = new SimpleBooleanProperty(true);
    public static final ObjectProperty<MapSelectionMode> PY_MAP_SELECTION_MODE = new SimpleObjectProperty<>(MapSelectionMode.CUSTOM_MAPS_FIRST);
    public static final BooleanProperty PY_DEBUG_INFO          = new SimpleBooleanProperty(false);
    public static final BooleanProperty PY_IMMUNITY            = new SimpleBooleanProperty(false);
    public static final BooleanProperty PY_NIGHT_MODE          = new SimpleBooleanProperty(false);
    public static final IntegerProperty PY_PIP_HEIGHT          = new SimpleIntegerProperty(GameModel.ARCADE_MAP_SIZE_Y);
    public static final BooleanProperty PY_PIP_ON              = new SimpleBooleanProperty(false);
    public static final IntegerProperty PY_PIP_OPACITY_PERCENT = new SimpleIntegerProperty(100);
    public static final IntegerProperty PY_SIMULATION_STEPS    = new SimpleIntegerProperty(1);

    public static String assetPrefix(GameVariant variant) {
        return switch (variant) {
            case MS_PACMAN -> "ms_pacman";
            case MS_PACMAN_TENGEN -> "tengen";
            case PACMAN -> "pacman";
            case PACMAN_XXL -> "pacman_xxl";
        };
    }

    public static void addAssets(AssetStorage assets) {
        ResourceManager rm = () -> PacManGames2dApp.class;

        assets.addBundle(ResourceBundle.getBundle("de.amr.games.pacman.ui2d.texts.messages", rm.rootClass().getModule()));

        // Dashboard

        assets.store("image.armin1970",                 rm.loadImage("graphics/armin.jpg"));
        assets.store("icon.mute",                       rm.loadImage("graphics/icons/mute.png"));
        assets.store("icon.play",                       rm.loadImage("graphics/icons/play.png"));
        assets.store("icon.stop",                       rm.loadImage("graphics/icons/stop.png"));
        assets.store("icon.step",                       rm.loadImage("graphics/icons/step.png"));

        assets.store("infobox.background_color",        Color.rgb(0,0,50,1.0));
        assets.store("infobox.min_label_width",         110);
        assets.store("infobox.min_col_width",           180);
        assets.store("infobox.text_color",              Color.WHITE);
        assets.store("infobox.label_font",              Font.font("Sans", 12));
        assets.store("infobox.text_font",               Font.font("Sans", 12));

        //
        // Common to all game variants
        //

        assets.store("palette.black",                   Color.rgb(0, 0, 0));
        assets.store("palette.red",                     Color.rgb(255, 0, 0));
        assets.store("palette.yellow",                  Color.rgb(255, 255, 0));
        assets.store("palette.pink",                    Color.rgb(252, 181, 255));
        assets.store("palette.cyan",                    Color.rgb(0, 255, 255));
        assets.store("palette.orange",                  Color.rgb(251, 190, 88));
        assets.store("palette.blue",                    Color.rgb(33, 33, 255));
        assets.store("palette.pale",                    Color.rgb(222, 222, 255));
        assets.store("palette.rose",                    Color.rgb(252, 187, 179));

        assets.store("startpage.arrow.left",            rm.loadImage("graphics/icons/arrow-left.png"));
        assets.store("startpage.arrow.right",           rm.loadImage("graphics/icons/arrow-right.png"));
        assets.store("startpage.button.bgColor",        Color.rgb(0, 155, 252, 0.6));
        assets.store("startpage.button.color",          Color.WHITE);
        assets.store("startpage.button.font",           rm.loadFont("fonts/emulogic.ttf", 32));

        assets.store("wallpaper.background",            Ufx.imageBackground(rm.loadImage("graphics/pacman_wallpaper.png")));
        assets.store("wallpaper.color",                 Color.rgb(72, 78, 135));

        assets.store("font.arcade",                     rm.loadFont("fonts/emulogic.ttf", 8));
        assets.store("font.handwriting",                rm.loadFont("fonts/Molle-Italic.ttf", 9));
        assets.store("font.monospaced",                 rm.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12));

        assets.store("voice.explain",                   rm.url("sound/voice/press-key.mp3"));
        assets.store("voice.autopilot.off",             rm.url("sound/voice/autopilot-off.mp3"));
        assets.store("voice.autopilot.on",              rm.url("sound/voice/autopilot-on.mp3"));
        assets.store("voice.immunity.off",              rm.url("sound/voice/immunity-off.mp3"));
        assets.store("voice.immunity.on",               rm.url("sound/voice/immunity-on.mp3"));

        //
        // Ms. Pac-Man game
        //

        assets.store("ms_pacman.spritesheet",           new MsPacManGameSpriteSheet("/de/amr/games/pacman/ui2d/graphics/mspacman/"));
        assets.store("ms_pacman.startpage.image",       rm.loadImage("graphics/mspacman/mspacman_flyer.png"));
        assets.store("ms_pacman.startpage.image1",      rm.loadImage("graphics/mspacman/mspacman_flyer1.jpg"));
        assets.store("ms_pacman.startpage.image2",      rm.loadImage("graphics/mspacman/mspacman_flyer2.jpg"));
        assets.store("ms_pacman.helpButton.icon",       rm.loadImage("graphics/icons/help-red-64.png"));
        assets.store("ms_pacman.icon",                  rm.loadImage("graphics/icons/mspacman.png"));
        assets.store("ms_pacman.logo.midway",           rm.loadImage("graphics/mspacman/midway_logo.png"));

        // Clips
        assets.store("ms_pacman.audio.bonus_eaten",     rm.loadAudioClip("sound/mspacman/Fruit.mp3"));
        assets.store("ms_pacman.audio.credit",          rm.loadAudioClip("sound/mspacman/Credit.mp3"));
        assets.store("ms_pacman.audio.extra_life",      rm.loadAudioClip("sound/mspacman/ExtraLife.mp3"));
        assets.store("ms_pacman.audio.ghost_eaten",     rm.loadAudioClip("sound/mspacman/Ghost.mp3"));
        assets.store("ms_pacman.audio.sweep",           rm.loadAudioClip("sound/common/sweep.mp3"));

        // Audio played by MediaPlayer
        assets.store("ms_pacman.audio.game_ready",      rm.url("sound/mspacman/Start.mp3"));
        assets.store("ms_pacman.audio.game_over",       rm.url("sound/common/game-over.mp3"));
        assets.store("ms_pacman.audio.intermission.1",  rm.url("sound/mspacman/Act1TheyMeet.mp3"));
        assets.store("ms_pacman.audio.intermission.2",  rm.url("sound/mspacman/Act2TheChase.mp3"));
        assets.store("ms_pacman.audio.intermission.3",  rm.url("sound/mspacman/Act3Junior.mp3"));
        assets.store("ms_pacman.audio.level_complete",  rm.url("sound/common/level-complete.mp3"));
        assets.store("ms_pacman.audio.pacman_death",    rm.url("sound/mspacman/Died.mp3"));
        assets.store("ms_pacman.audio.pacman_munch",    rm.url("sound/mspacman/Pill.wav"));
        assets.store("ms_pacman.audio.pacman_power",    rm.url("sound/mspacman/ScaredGhost.mp3"));
        assets.store("ms_pacman.audio.siren.1",         rm.url("sound/mspacman/GhostNoise1.wav"));
        assets.store("ms_pacman.audio.siren.2",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.store("ms_pacman.audio.siren.3",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.store("ms_pacman.audio.siren.4",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.store("ms_pacman.audio.ghost_returning", rm.url("sound/mspacman/GhostEyes.mp3"));

        //
        // Ms. Pac-Man game Tengen
        //

        // default until Tengen spritesheets are implemented
        assets.store("tengen.spritesheet.tmp",        assets.get("ms_pacman.spritesheet"));
        assets.store("tengen.spritesheet",            new TengenMsPacManGameSpriteSheet("/de/amr/games/pacman/ui2d/graphics/tengen/"));
        assets.store("tengen.startpage.image",        rm.loadImage("graphics/tengen/tengen_flyer.jpg"));
        assets.store("tengen.helpButton.icon",        rm.loadImage("graphics/icons/help-red-64.png"));
        assets.store("tengen.icon",                   rm.loadImage("graphics/icons/mspacman.png"));
        assets.store("tengen.logo.midway",            rm.loadImage("graphics/mspacman/midway_logo.png"));

        // Clips
        assets.store("tengen.audio.bonus_eaten",     rm.loadAudioClip("sound/mspacman/Fruit.mp3"));
        assets.store("tengen.audio.credit",          rm.loadAudioClip("sound/mspacman/Credit.mp3"));
        assets.store("tengen.audio.extra_life",      rm.loadAudioClip("sound/mspacman/ExtraLife.mp3"));
        assets.store("tengen.audio.ghost_eaten",     rm.loadAudioClip("sound/mspacman/Ghost.mp3"));
        assets.store("tengen.audio.sweep",           rm.loadAudioClip("sound/common/sweep.mp3"));

        // Audio played by MediaPlayer
        assets.store("tengen.audio.game_ready",      rm.url("sound/mspacman/Start.mp3"));
        assets.store("tengen.audio.game_over",       rm.url("sound/common/game-over.mp3"));
        assets.store("tengen.audio.intermission.1",  rm.url("sound/mspacman/Act1TheyMeet.mp3"));
        assets.store("tengen.audio.intermission.2",  rm.url("sound/mspacman/Act2TheChase.mp3"));
        assets.store("tengen.audio.intermission.3",  rm.url("sound/mspacman/Act3Junior.mp3"));
        assets.store("tengen.audio.level_complete",  rm.url("sound/common/level-complete.mp3"));
        assets.store("tengen.audio.pacman_death",    rm.url("sound/mspacman/Died.mp3"));
        assets.store("tengen.audio.pacman_munch",    rm.url("sound/mspacman/Pill.wav"));
        assets.store("tengen.audio.pacman_power",    rm.url("sound/mspacman/ScaredGhost.mp3"));
        assets.store("tengen.audio.siren.1",         rm.url("sound/mspacman/GhostNoise1.wav"));
        assets.store("tengen.audio.siren.2",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.store("tengen.audio.siren.3",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.store("tengen.audio.siren.4",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.store("tengen.audio.ghost_returning", rm.url("sound/mspacman/GhostEyes.mp3"));

        //
        // Pac-Man game
        //

        assets.store("pacman.spritesheet",              new PacManGameSpriteSheet("/de/amr/games/pacman/ui2d/graphics/pacman/"));
        assets.store("pacman.startpage.image1",         rm.loadImage("graphics/pacman/pacman_flyer.png"));
        assets.store("pacman.startpage.image2",         rm.loadImage("graphics/pacman/pacman_flyer2.jpg"));
        assets.store("pacman.helpButton.icon",          rm.loadImage("graphics/icons/help-blue-64.png"));
        assets.store("pacman.icon",                     rm.loadImage("graphics/icons/pacman.png"));

        // Clips
        assets.store("pacman.audio.bonus_eaten",        rm.loadAudioClip("sound/pacman/eat_fruit.mp3"));
        assets.store("pacman.audio.credit",             rm.loadAudioClip("sound/pacman/credit.wav"));
        assets.store("pacman.audio.extra_life",         rm.loadAudioClip("sound/pacman/extend.mp3"));
        assets.store("pacman.audio.ghost_eaten",        rm.loadAudioClip("sound/pacman/eat_ghost.mp3"));
        assets.store("pacman.audio.sweep",              rm.loadAudioClip("sound/common/sweep.mp3"));

        // Media player sounds
        assets.store("pacman.audio.game_ready",         rm.url("sound/pacman/game_start.mp3"));
        assets.store("pacman.audio.game_over",          rm.url("sound/common/game-over.mp3"));
        assets.store("pacman.audio.intermission",       rm.url("sound/pacman/intermission.mp3"));
        assets.store("pacman.audio.pacman_death",       rm.url("sound/pacman/pacman_death.wav"));
        assets.store("pacman.audio.pacman_munch",       rm.url("sound/pacman/doublemunch.wav"));
        assets.store("pacman.audio.pacman_power",       rm.url("sound/pacman/ghost-turn-to-blue.mp3"));
        assets.store("pacman.audio.level_complete",     rm.url("sound/common/level-complete.mp3"));
        assets.store("pacman.audio.siren.1",            rm.url("sound/pacman/siren_1.mp3"));
        assets.store("pacman.audio.siren.2",            rm.url("sound/pacman/siren_2.mp3"));
        assets.store("pacman.audio.siren.3",            rm.url("sound/pacman/siren_3.mp3"));
        assets.store("pacman.audio.siren.4",            rm.url("sound/pacman/siren_4.mp3"));
        assets.store("pacman.audio.ghost_returning",    rm.url("sound/pacman/retreating.mp3"));

        //
        // Pac-Man XXL
        //
        assets.store("pacman_xxl.icon",                     rm.loadImage("graphics/icons/pacman.png"));
        assets.store("pacman_xxl.helpButton.icon",          rm.loadImage("graphics/icons/help-blue-64.png"));
        assets.store("pacman_xxl.startpage.image",          rm.loadImage("graphics/pacman_xxl/pacman_xxl_logo.png"));

        assets.store("pacman_xxl.spritesheet",              new PacManGameSpriteSheet("/de/amr/games/pacman/ui2d/graphics/pacman/"));

        // Clips
        assets.store("pacman_xxl.audio.bonus_eaten",        rm.loadAudioClip("sound/pacman/eat_fruit.mp3"));
        assets.store("pacman_xxl.audio.credit",             rm.loadAudioClip("sound/pacman/credit.wav"));
        assets.store("pacman_xxl.audio.extra_life",         rm.loadAudioClip("sound/pacman/extend.mp3"));
        assets.store("pacman_xxl.audio.ghost_eaten",        rm.loadAudioClip("sound/pacman/eat_ghost.mp3"));
        assets.store("pacman_xxl.audio.sweep",              rm.loadAudioClip("sound/common/sweep.mp3"));

        // Media player sounds
        assets.store("pacman_xxl.audio.game_ready",         rm.url("sound/pacman/game_start.mp3"));
        assets.store("pacman_xxl.audio.game_over",          rm.url("sound/common/game-over.mp3"));
        assets.store("pacman_xxl.audio.intermission",       rm.url("sound/pacman/intermission.mp3"));
        assets.store("pacman_xxl.audio.pacman_death",       rm.url("sound/pacman/pacman_death.wav"));
        assets.store("pacman_xxl.audio.pacman_munch",       rm.url("sound/pacman/doublemunch.wav"));
        assets.store("pacman_xxl.audio.pacman_power",       rm.url("sound/pacman/ghost-turn-to-blue.mp3"));
        assets.store("pacman_xxl.audio.level_complete",     rm.url("sound/common/level-complete.mp3"));
        assets.store("pacman_xxl.audio.siren.1",            rm.url("sound/pacman/siren_1.mp3"));
        assets.store("pacman_xxl.audio.siren.2",            rm.url("sound/pacman/siren_2.mp3"));
        assets.store("pacman_xxl.audio.siren.3",            rm.url("sound/pacman/siren_3.mp3"));
        assets.store("pacman_xxl.audio.siren.4",            rm.url("sound/pacman/siren_4.mp3"));
        assets.store("pacman_xxl.audio.ghost_returning",    rm.url("sound/pacman/retreating.mp3"));

        GameSounds.setAssets(assets);
    }

    private static Map<GameVariant, Map<GameSceneID, GameScene>> createGameScenes() {
        Map<GameVariant, Map<GameSceneID, GameScene>> gameScenesForVariant = new EnumMap<>(GameVariant.class);
        for (GameVariant variant : GameVariant.values()) {
            switch (variant) {
                case MS_PACMAN, MS_PACMAN_TENGEN ->
                    gameScenesForVariant.put(variant, new EnumMap<>(Map.of(
                        GameSceneID.BOOT_SCENE,   new BootScene(),
                        GameSceneID.INTRO_SCENE,  new MsPacManIntroScene(),
                        GameSceneID.CREDIT_SCENE, new CreditScene(),
                        GameSceneID.PLAY_SCENE,   new PlayScene2D(),
                        GameSceneID.CUT_SCENE_1,  new MsPacManCutScene1(),
                        GameSceneID.CUT_SCENE_2,  new MsPacManCutScene2(),
                        GameSceneID.CUT_SCENE_3,  new MsPacManCutScene3()
                    )));
                case PACMAN, PACMAN_XXL ->
                    gameScenesForVariant.put(variant, new EnumMap<>(Map.of(
                        GameSceneID.BOOT_SCENE,   new BootScene(),
                        GameSceneID.INTRO_SCENE,  new PacManIntroScene(),
                        GameSceneID.CREDIT_SCENE, new CreditScene(),
                        GameSceneID.PLAY_SCENE,   new PlayScene2D(),
                        GameSceneID.CUT_SCENE_1,  new PacManCutScene1(),
                        GameSceneID.CUT_SCENE_2,  new PacManCutScene2(),
                        GameSceneID.CUT_SCENE_3,  new PacManCutScene3()
                    )));
                default -> throw new IllegalArgumentException("Unsupported game variant: " + variant);
            }
        }
        return gameScenesForVariant;
    }

    private final GameClockFX clock = new GameClockFX();

    @Override
    public void init() {
        File userDir = new File(System.getProperty("user.home"), ".pacmanfx");
        GameController.create(userDir);
        GameController.it().selectGame(GameVariant.PACMAN);
    }

    @Override
    public void start(Stage stage) {
        var ui = new PacManGames2dUI(computeSize());
        addAssets(ui.assets());
        ui.create(stage, clock, createGameScenes());
        ui.start();

        Logger.info("JavaFX version: {}", System.getProperty("javafx.runtime.version"));
        Logger.info("Assets loaded: {}", ui.assets().summary(
            Map.of(Image.class, "images",  Font.class, "fonts", Color.class, "colors", AudioClip.class, "audio clips")
        ));
        Logger.info("Application started. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
    }

    @Override
    public void stop() {
        clock.stop();
    }

    private Dimension2D computeSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = 1.2;
        double height = 0.8 * screenSize.getHeight();
        return new Dimension2D(aspect * height, height);
    }
}