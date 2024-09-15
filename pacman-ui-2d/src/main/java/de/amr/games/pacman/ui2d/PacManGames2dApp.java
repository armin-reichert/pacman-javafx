/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.PacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.*;
import de.amr.games.pacman.ui2d.util.AssetMap;
import de.amr.games.pacman.ui2d.util.GameClockFX;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.application.Application;
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

    public static void addAssets(AssetMap assets) {
        ResourceManager rm = () -> PacManGames2dApp.class;

        assets.addBundle(ResourceBundle.getBundle("de.amr.games.pacman.ui2d.texts.messages", rm.rootClass().getModule()));

        // Dashboard

        assets.set("image.armin1970",                 rm.loadImage("graphics/armin.jpg"));
        assets.set("icon.mute",                       rm.loadImage("graphics/icons/mute.png"));
        assets.set("icon.play",                       rm.loadImage("graphics/icons/play.png"));
        assets.set("icon.stop",                       rm.loadImage("graphics/icons/stop.png"));
        assets.set("icon.step",                       rm.loadImage("graphics/icons/step.png"));

        assets.set("infobox.background_color",        Color.rgb(0,0,50,1.0));
        assets.set("infobox.min_label_width",         110);
        assets.set("infobox.min_col_width",           180);
        assets.set("infobox.text_color",              Color.WHITE);
        assets.set("infobox.label_font",              Font.font("Sans", 12));
        assets.set("infobox.text_font",               Font.font("Sans", 12));

        //
        // Common to all game variants
        //

        assets.set("palette.black",                   Color.rgb(0, 0, 0));
        assets.set("palette.red",                     Color.rgb(255, 0, 0));
        assets.set("palette.yellow",                  Color.rgb(255, 255, 0));
        assets.set("palette.pink",                    Color.rgb(252, 181, 255));
        assets.set("palette.cyan",                    Color.rgb(0, 255, 255));
        assets.set("palette.orange",                  Color.rgb(251, 190, 88));
        assets.set("palette.blue",                    Color.rgb(33, 33, 255));
        assets.set("palette.pale",                    Color.rgb(222, 222, 255));
        assets.set("palette.rose",                    Color.rgb(252, 187, 179));

        assets.set("startpage.arrow.left",            rm.loadImage("graphics/icons/arrow-left.png"));
        assets.set("startpage.arrow.right",           rm.loadImage("graphics/icons/arrow-right.png"));
        assets.set("startpage.button.bgColor",        Color.rgb(0, 155, 252, 0.6));
        assets.set("startpage.button.color",          Color.WHITE);
        assets.set("startpage.button.font",           rm.loadFont("fonts/emulogic.ttf", 32));

        assets.set("wallpaper.background",            Ufx.imageBackground(rm.loadImage("graphics/pacman_wallpaper.png")));
        assets.set("wallpaper.color",                 Color.rgb(72, 78, 135));

        assets.set("font.arcade",                     rm.loadFont("fonts/emulogic.ttf", 8));
        assets.set("font.handwriting",                rm.loadFont("fonts/Molle-Italic.ttf", 9));
        assets.set("font.monospaced",                 rm.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12));

        assets.set("voice.explain",                   rm.url("sound/voice/press-key.mp3"));
        assets.set("voice.autopilot.off",             rm.url("sound/voice/autopilot-off.mp3"));
        assets.set("voice.autopilot.on",              rm.url("sound/voice/autopilot-on.mp3"));
        assets.set("voice.immunity.off",              rm.url("sound/voice/immunity-off.mp3"));
        assets.set("voice.immunity.on",               rm.url("sound/voice/immunity-on.mp3"));

        //
        // Ms. Pac-Man game
        //

        assets.set("ms_pacman.spritesheet",           new MsPacManGameSpriteSheet());
        assets.set("ms_pacman.startpage.image",       rm.loadImage("graphics/mspacman/mspacman_flyer.png"));
        assets.set("ms_pacman.startpage.image1",      rm.loadImage("graphics/mspacman/mspacman_flyer1.jpg"));
        assets.set("ms_pacman.startpage.image2",      rm.loadImage("graphics/mspacman/mspacman_flyer2.jpg"));
        assets.set("ms_pacman.helpButton.icon",       rm.loadImage("graphics/icons/help-red-64.png"));
        assets.set("ms_pacman.icon",                  rm.loadImage("graphics/icons/mspacman.png"));
        assets.set("ms_pacman.logo.midway",           rm.loadImage("graphics/mspacman/midway_logo.png"));

        // Clips
        assets.set("ms_pacman.audio.bonus_eaten",     rm.loadAudioClip("sound/mspacman/Fruit.mp3"));
        assets.set("ms_pacman.audio.credit",          rm.loadAudioClip("sound/mspacman/Credit.mp3"));
        assets.set("ms_pacman.audio.extra_life",      rm.loadAudioClip("sound/mspacman/ExtraLife.mp3"));
        assets.set("ms_pacman.audio.ghost_eaten",     rm.loadAudioClip("sound/mspacman/Ghost.mp3"));
        assets.set("ms_pacman.audio.sweep",           rm.loadAudioClip("sound/common/sweep.mp3"));

        // Audio played by MediaPlayer
        assets.set("ms_pacman.audio.game_ready",      rm.url("sound/mspacman/Start.mp3"));
        assets.set("ms_pacman.audio.game_over",       rm.url("sound/common/game-over.mp3"));
        assets.set("ms_pacman.audio.intermission.1",  rm.url("sound/mspacman/Act1TheyMeet.mp3"));
        assets.set("ms_pacman.audio.intermission.2",  rm.url("sound/mspacman/Act2TheChase.mp3"));
        assets.set("ms_pacman.audio.intermission.3",  rm.url("sound/mspacman/Act3Junior.mp3"));
        assets.set("ms_pacman.audio.level_complete",  rm.url("sound/common/level-complete.mp3"));
        assets.set("ms_pacman.audio.pacman_death",    rm.url("sound/mspacman/Died.mp3"));
        assets.set("ms_pacman.audio.pacman_munch",    rm.url("sound/mspacman/Pill.wav"));
        assets.set("ms_pacman.audio.pacman_power",    rm.url("sound/mspacman/ScaredGhost.mp3"));
        assets.set("ms_pacman.audio.siren.1",         rm.url("sound/mspacman/GhostNoise1.wav"));
        assets.set("ms_pacman.audio.siren.2",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.set("ms_pacman.audio.siren.3",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.set("ms_pacman.audio.siren.4",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.set("ms_pacman.audio.ghost_returning", rm.url("sound/mspacman/GhostEyes.mp3"));

        //
        // Ms. Pac-Man game Tengen
        //

        assets.set("tengen.spritesheet",           new MsPacManGameSpriteSheet());
        assets.set("tengen.startpage.image",       rm.loadImage("graphics/tengen/tengen_flyer.jpg"));
        assets.set("tengen.helpButton.icon",       rm.loadImage("graphics/icons/help-red-64.png"));
        assets.set("tengen.icon",                  rm.loadImage("graphics/icons/mspacman.png"));
        assets.set("tengen.logo.midway",           rm.loadImage("graphics/mspacman/midway_logo.png"));

        // Clips
        assets.set("tengen.audio.bonus_eaten",     rm.loadAudioClip("sound/mspacman/Fruit.mp3"));
        assets.set("tengen.audio.credit",          rm.loadAudioClip("sound/mspacman/Credit.mp3"));
        assets.set("tengen.audio.extra_life",      rm.loadAudioClip("sound/mspacman/ExtraLife.mp3"));
        assets.set("tengen.audio.ghost_eaten",     rm.loadAudioClip("sound/mspacman/Ghost.mp3"));
        assets.set("tengen.audio.sweep",           rm.loadAudioClip("sound/common/sweep.mp3"));

        // Audio played by MediaPlayer
        assets.set("tengen.audio.game_ready",      rm.url("sound/mspacman/Start.mp3"));
        assets.set("tengen.audio.game_over",       rm.url("sound/common/game-over.mp3"));
        assets.set("tengen.audio.intermission.1",  rm.url("sound/mspacman/Act1TheyMeet.mp3"));
        assets.set("tengen.audio.intermission.2",  rm.url("sound/mspacman/Act2TheChase.mp3"));
        assets.set("tengen.audio.intermission.3",  rm.url("sound/mspacman/Act3Junior.mp3"));
        assets.set("tengen.audio.level_complete",  rm.url("sound/common/level-complete.mp3"));
        assets.set("tengen.audio.pacman_death",    rm.url("sound/mspacman/Died.mp3"));
        assets.set("tengen.audio.pacman_munch",    rm.url("sound/mspacman/Pill.wav"));
        assets.set("tengen.audio.pacman_power",    rm.url("sound/mspacman/ScaredGhost.mp3"));
        assets.set("tengen.audio.siren.1",         rm.url("sound/mspacman/GhostNoise1.wav"));
        assets.set("tengen.audio.siren.2",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.set("tengen.audio.siren.3",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.set("tengen.audio.siren.4",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.set("tengen.audio.ghost_returning", rm.url("sound/mspacman/GhostEyes.mp3"));

        //
        // Pac-Man game
        //

        assets.set("pacman.spritesheet",              new PacManGameSpriteSheet());
        assets.set("pacman.startpage.image1",         rm.loadImage("graphics/pacman/pacman_flyer.png"));
        assets.set("pacman.startpage.image2",         rm.loadImage("graphics/pacman/pacman_flyer2.jpg"));
        assets.set("pacman.helpButton.icon",          rm.loadImage("graphics/icons/help-blue-64.png"));
        assets.set("pacman.icon",                     rm.loadImage("graphics/icons/pacman.png"));

        // Clips
        assets.set("pacman.audio.bonus_eaten",        rm.loadAudioClip("sound/pacman/eat_fruit.mp3"));
        assets.set("pacman.audio.credit",             rm.loadAudioClip("sound/pacman/credit.wav"));
        assets.set("pacman.audio.extra_life",         rm.loadAudioClip("sound/pacman/extend.mp3"));
        assets.set("pacman.audio.ghost_eaten",        rm.loadAudioClip("sound/pacman/eat_ghost.mp3"));
        assets.set("pacman.audio.sweep",              rm.loadAudioClip("sound/common/sweep.mp3"));

        // Media player sounds
        assets.set("pacman.audio.game_ready",         rm.url("sound/pacman/game_start.mp3"));
        assets.set("pacman.audio.game_over",          rm.url("sound/common/game-over.mp3"));
        assets.set("pacman.audio.intermission",       rm.url("sound/pacman/intermission.mp3"));
        assets.set("pacman.audio.pacman_death",       rm.url("sound/pacman/pacman_death.wav"));
        assets.set("pacman.audio.pacman_munch",       rm.url("sound/pacman/doublemunch.wav"));
        assets.set("pacman.audio.pacman_power",       rm.url("sound/pacman/ghost-turn-to-blue.mp3"));
        assets.set("pacman.audio.level_complete",     rm.url("sound/common/level-complete.mp3"));
        assets.set("pacman.audio.siren.1",            rm.url("sound/pacman/siren_1.mp3"));
        assets.set("pacman.audio.siren.2",            rm.url("sound/pacman/siren_2.mp3"));
        assets.set("pacman.audio.siren.3",            rm.url("sound/pacman/siren_3.mp3"));
        assets.set("pacman.audio.siren.4",            rm.url("sound/pacman/siren_4.mp3"));
        assets.set("pacman.audio.ghost_returning",    rm.url("sound/pacman/retreating.mp3"));

        //
        // Pac-Man XXL
        //
        assets.set("pacman_xxl.icon",                 rm.loadImage("graphics/icons/pacman.png"));
        assets.set("pacman_xxl.helpButton.icon",      rm.loadImage("graphics/icons/help-blue-64.png"));
        assets.set("pacman_xxl.startpage.image",      rm.loadImage("graphics/pacman_xxl/pacman_xxl_logo.png"));

        assets.set("pacman_xxl.spritesheet",              new PacManGameSpriteSheet());

        // Clips
        assets.set("pacman_xxl.audio.bonus_eaten",        rm.loadAudioClip("sound/pacman/eat_fruit.mp3"));
        assets.set("pacman_xxl.audio.credit",             rm.loadAudioClip("sound/pacman/credit.wav"));
        assets.set("pacman_xxl.audio.extra_life",         rm.loadAudioClip("sound/pacman/extend.mp3"));
        assets.set("pacman_xxl.audio.ghost_eaten",        rm.loadAudioClip("sound/pacman/eat_ghost.mp3"));
        assets.set("pacman_xxl.audio.sweep",              rm.loadAudioClip("sound/common/sweep.mp3"));

        // Media player sounds
        assets.set("pacman_xxl.audio.game_ready",         rm.url("sound/pacman/game_start.mp3"));
        assets.set("pacman_xxl.audio.game_over",          rm.url("sound/common/game-over.mp3"));
        assets.set("pacman_xxl.audio.intermission",       rm.url("sound/pacman/intermission.mp3"));
        assets.set("pacman_xxl.audio.pacman_death",       rm.url("sound/pacman/pacman_death.wav"));
        assets.set("pacman_xxl.audio.pacman_munch",       rm.url("sound/pacman/doublemunch.wav"));
        assets.set("pacman_xxl.audio.pacman_power",       rm.url("sound/pacman/ghost-turn-to-blue.mp3"));
        assets.set("pacman_xxl.audio.level_complete",     rm.url("sound/common/level-complete.mp3"));
        assets.set("pacman_xxl.audio.siren.1",            rm.url("sound/pacman/siren_1.mp3"));
        assets.set("pacman_xxl.audio.siren.2",            rm.url("sound/pacman/siren_2.mp3"));
        assets.set("pacman_xxl.audio.siren.3",            rm.url("sound/pacman/siren_3.mp3"));
        assets.set("pacman_xxl.audio.siren.4",            rm.url("sound/pacman/siren_4.mp3"));
        assets.set("pacman_xxl.audio.ghost_returning",    rm.url("sound/pacman/retreating.mp3"));

        GameSounds.setAssets(assets);
    }

    private static Map<GameVariant, Map<GameSceneID, GameScene>> createGameScenes() {
        Map<GameVariant, Map<GameSceneID, GameScene>> gameScenesForVariant = new EnumMap<>(GameVariant.class);
        for (GameVariant variant : GameVariant.values()) {
            switch (variant) {
                case MS_PACMAN ->
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
        var ui = new PacManGames2dUI(clock, createGameScenes());
        addAssets(ui.assets());
        ui.create(stage, computeSize());
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