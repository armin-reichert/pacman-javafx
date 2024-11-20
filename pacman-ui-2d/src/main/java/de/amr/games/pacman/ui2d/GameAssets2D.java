/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.scene.ms_pacman.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManGameTengenSpriteSheet;
import de.amr.games.pacman.ui2d.scene.pacman.PacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManGameTengenSceneConfig.nesPaletteColor;
import static de.amr.games.pacman.ui2d.util.Ufx.coloredBackground;
import static de.amr.games.pacman.ui2d.util.Ufx.imageBackground;

/**
 * @author Armin Reichert
 */
public class GameAssets2D extends AssetStorage {

    public static String assetPrefix(GameVariant variant) {
        return switch (variant) {
            case MS_PACMAN        -> "ms_pacman";
            case MS_PACMAN_TENGEN -> "tengen";
            case PACMAN           -> "pacman";
            case PACMAN_XXL       -> "pacman_xxl";
        };
    }

    public static void addTo(AssetStorage assets) {
        ResourceManager rm = () -> GameAssets2D.class;

        assets.addBundle(rm.getModuleBundle("de.amr.games.pacman.ui2d.texts.messages"));

        // Dashboard

        assets.store("photo.armin1970",                 rm.loadImage("graphics/armin.jpg"));
        assets.store("icon.mute",                       rm.loadImage("graphics/icons/mute.png"));
        assets.store("icon.pause",                      rm.loadImage("graphics/icons/pause.png"));
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

        assets.store("startpage.arrow.left",            rm.loadImage("graphics/icons/arrow-left.png"));
        assets.store("startpage.arrow.right",           rm.loadImage("graphics/icons/arrow-right.png"));

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

        assets.store("ms_pacman.scene_background",      imageBackground(rm.loadImage("graphics/pacman_wallpaper.png")));

        assets.store("ms_pacman.spritesheet",           new MsPacManGameSpriteSheet(rm.loadImage("graphics/mspacman/mspacman_spritesheet.png")));
        assets.store("ms_pacman.flashing_mazes",        rm.loadImage("graphics/mspacman/mazes_flashing.png"));

        assets.store("ms_pacman.startpage.image1",      rm.loadImage("graphics/mspacman/f1.jpg"));
        assets.store("ms_pacman.startpage.image2",      rm.loadImage("graphics/mspacman/f2.jpg"));

        assets.store("ms_pacman.helpButton.icon",       rm.loadImage("graphics/icons/help-red-64.png"));
        assets.store("ms_pacman.icon",                  rm.loadImage("graphics/icons/mspacman.png"));
        assets.store("ms_pacman.logo.midway",           rm.loadImage("graphics/mspacman/midway_logo.png"));

        assets.store("ms_pacman.ghost.0.color.normal.dress", Color.valueOf(Arcade.Palette.RED));
        assets.store("ms_pacman.ghost.1.color.normal.dress", Color.valueOf(Arcade.Palette.PINK));
        assets.store("ms_pacman.ghost.2.color.normal.dress", Color.valueOf(Arcade.Palette.CYAN));
        assets.store("ms_pacman.ghost.3.color.normal.dress", Color.valueOf(Arcade.Palette.ORANGE));

        assets.store("ms_pacman.color.game_over_message",    Color.valueOf(Arcade.Palette.RED));
        assets.store("ms_pacman.color.ready_message",        Color.valueOf(Arcade.Palette.YELLOW));
        assets.store("ms_pacman.color.clapperboard",         Color.valueOf(Arcade.Palette.WHITE));

        // Clips
        assets.store("ms_pacman.audio.bonus_eaten",     rm.loadAudioClip("sound/mspacman/Fruit.mp3"));
        assets.store("ms_pacman.audio.credit",          rm.loadAudioClip("sound/mspacman/Credit.mp3"));
        assets.store("ms_pacman.audio.extra_life",      rm.loadAudioClip("sound/mspacman/ExtraLife.mp3"));
        assets.store("ms_pacman.audio.ghost_eaten",     rm.loadAudioClip("sound/mspacman/Ghost.mp3"));
        assets.store("ms_pacman.audio.sweep",           rm.loadAudioClip("sound/common/sweep.mp3"));

        // Audio played by MediaPlayer
        assets.store("ms_pacman.audio.game_ready",      rm.url("sound/mspacman/Start.mp3"));
        assets.store("ms_pacman.audio.game_over",       rm.url("sound/common/game-over.mp3"));
        assets.store("ms_pacman.audio.intermission.1",  rm.url("sound/mspacman/Act_1_They_Meet.mp3"));
        assets.store("ms_pacman.audio.intermission.2",  rm.url("sound/mspacman/Act_2_The_Chase.mp3"));
        assets.store("ms_pacman.audio.intermission.3",  rm.url("sound/mspacman/Act_3_Junior.mp3"));
        assets.store("ms_pacman.audio.level_complete",  rm.url("sound/common/level-complete.mp3"));
        assets.store("ms_pacman.audio.pacman_death",    rm.url("sound/mspacman/Died.mp3"));
        assets.store("ms_pacman.audio.pacman_munch",    rm.url("sound/mspacman/munch.wav"));
        assets.store("ms_pacman.audio.pacman_power",    rm.url("sound/mspacman/ScaredGhost.mp3"));
        assets.store("ms_pacman.audio.siren.1",         rm.url("sound/mspacman/GhostNoise1.wav"));
        assets.store("ms_pacman.audio.siren.2",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.store("ms_pacman.audio.siren.3",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.store("ms_pacman.audio.siren.4",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.store("ms_pacman.audio.ghost_returns",   rm.url("sound/mspacman/GhostEyes.mp3"));
        assets.store("ms_pacman.audio.bonus_bouncing",  rm.url("sound/mspacman/Fruit Bounce.mp3"));

        assets.store("tengen.audio.bonus_bouncing",     rm.url("sound/tengen/fruitbounce.wav"));

        //
        // Ms. Pac-Man game Tengen
        //

        assets.store("tengen.scene_background",               coloredBackground(Color.BLACK));

        assets.store("tengen.spritesheet",                    new MsPacManGameTengenSpriteSheet(rm.loadImage("graphics/tengen/spritesheet.png")));
        assets.store("tengen.mazes.arcade",                   rm.loadImage("graphics/tengen/arcade_mazes.png"));
        assets.store("tengen.mazes.non_arcade",               rm.loadImage("graphics/tengen/non_arcade_mazes.png"));

        assets.store("tengen.startpage.image1",               rm.loadImage("graphics/tengen/f1.png"));
        assets.store("tengen.startpage.image2",               rm.loadImage("graphics/tengen/f2.png"));

        assets.store("tengen.helpButton.icon",                rm.loadImage("graphics/icons/help-red-64.png"));
        assets.store("tengen.icon",                           rm.loadImage("graphics/icons/mspacman.png"));
        assets.store("tengen.logo.midway",                    rm.loadImage("graphics/mspacman/midway_logo.png"));
        assets.store("tengen.image.nes-controller",           rm.loadImage("graphics/tengen/nes-controller.jpg"));

        assets.store("tengen.color.game_over_message",        nesPaletteColor(0x11));
        assets.store("tengen.color.ready_message",            nesPaletteColor(0x28));
        assets.store("tengen.color.clapperboard",             nesPaletteColor(0x20));

        assets.store("tengen.pac.color.head",                 nesPaletteColor(0x28));
        assets.store("tengen.pac.color.eyes",                 nesPaletteColor(0x02));
        assets.store("tengen.pac.color.palate",               nesPaletteColor(0x2d));
        assets.store("tengen.pac.color.boobs",                nesPaletteColor(0x28).deriveColor(0, 1.0, 0.96, 1.0));
        assets.store("tengen.pac.color.hairbow",              nesPaletteColor(0x05));
        assets.store("tengen.pac.color.hairbow.pearls",       nesPaletteColor(0x02));

        assets.store("tengen.ghost.0.color.normal.dress",     nesPaletteColor(0x05));
        assets.store("tengen.ghost.0.color.normal.eyeballs",  nesPaletteColor(0x20));
        assets.store("tengen.ghost.0.color.normal.pupils",    nesPaletteColor(0x16));

        assets.store("tengen.ghost.1.color.normal.dress",     nesPaletteColor(0x25));
        assets.store("tengen.ghost.1.color.normal.eyeballs",  nesPaletteColor(0x20));
        assets.store("tengen.ghost.1.color.normal.pupils",    nesPaletteColor(0x11));

        assets.store("tengen.ghost.2.color.normal.dress",     nesPaletteColor(0x11));
        assets.store("tengen.ghost.2.color.normal.eyeballs",  nesPaletteColor(0x20));
        assets.store("tengen.ghost.2.color.normal.pupils",    nesPaletteColor(0x11));

        assets.store("tengen.ghost.3.color.normal.dress",     nesPaletteColor(0x16));
        assets.store("tengen.ghost.3.color.normal.eyeballs",  nesPaletteColor(0x20));
        assets.store("tengen.ghost.3.color.normal.pupils",    nesPaletteColor(0x05));

        assets.store("tengen.ghost.color.frightened.dress",   nesPaletteColor(0x01));
        assets.store("tengen.ghost.color.frightened.eyeballs", nesPaletteColor(0x20));
        assets.store("tengen.ghost.color.frightened.pupils",  nesPaletteColor(0x20));

        //TODO has two flashing colors, when to use which?
        assets.store("tengen.ghost.color.flashing.dress",     nesPaletteColor(0x20));
        assets.store("tengen.ghost.color.flashing.eyeballs",  nesPaletteColor(0x20));
        assets.store("tengen.ghost.color.flashing.pupils",    nesPaletteColor(0x20));

        assets.store("tengen.audio.option.selection_changed", rm.loadAudioClip("sound/tengen/ms-select1.wav"));
        assets.store("tengen.audio.option.value_changed",     rm.loadAudioClip("sound/tengen/ms-select2.wav"));

        assets.store("tengen.audio.bonus_eaten",        rm.loadAudioClip("sound/tengen/ms-fruit.wav"));
        assets.store("tengen.audio.extra_life",         rm.loadAudioClip("sound/tengen/ms-extralife.wav"));
        assets.store("tengen.audio.ghost_eaten",        rm.loadAudioClip("sound/tengen/ms-ghosteat.wav"));

        assets.store("tengen.audio.intermission.4.junior.1", rm.loadAudioClip("sound/tengen/ms-theend1.wav"));
        assets.store("tengen.audio.intermission.4.junior.2", rm.loadAudioClip("sound/tengen/ms-theend2.wav"));


        // used only in 3D scene when level is completed:
        assets.store("tengen.audio.level_complete",     rm.url("sound/common/level-complete.mp3"));
        assets.store("tengen.audio.sweep",              rm.loadAudioClip("sound/common/sweep.mp3"));

        // Audio played by MediaPlayer
        assets.store("tengen.audio.game_ready",         rm.url("sound/tengen/ms-start.wav"));
        assets.store("tengen.audio.intermission.1",     rm.url("sound/tengen/theymeet.wav"));
        assets.store("tengen.audio.intermission.2",     rm.url("sound/tengen/thechase.wav"));
        assets.store("tengen.audio.intermission.3",     rm.url("sound/tengen/junior.wav"));
        assets.store("tengen.audio.intermission.4",     rm.url("sound/tengen/theend.wav"));
        assets.store("tengen.audio.pacman_death",       rm.url("sound/tengen/ms-death.wav"));
        assets.store("tengen.audio.pacman_munch",       rm.url("sound/tengen/ms-dot.wav"));
        assets.store("tengen.audio.pacman_power",       rm.url("sound/tengen/ms-power.wav"));
        assets.store("tengen.audio.siren.1",            rm.url("sound/tengen/ms-siren1.wav"));
        assets.store("tengen.audio.siren.2",            rm.url("sound/tengen/ms-siren2.wav"));
        assets.store("tengen.audio.siren.3",            rm.url("sound/tengen/ms-siren2.wav"));
        assets.store("tengen.audio.siren.4",            rm.url("sound/tengen/ms-siren2.wav"));
        assets.store("tengen.audio.ghost_returns",      rm.url("sound/tengen/ms-eyes.wav"));
        assets.store("tengen.audio.bonus_bouncing",     rm.url("sound/tengen/fruitbounce.wav"));

        //
        // Pac-Man game
        //

        assets.store("pacman.scene_background",         imageBackground(rm.loadImage("graphics/pacman_wallpaper.png")));

        assets.store("pacman.spritesheet",              new PacManGameSpriteSheet(rm.loadImage("graphics/pacman/pacman_spritesheet.png")));
        assets.store("pacman.flashing_maze",            rm.loadImage("graphics/pacman/maze_flashing.png"));

        assets.store("pacman.startpage.image1",         rm.loadImage("graphics/pacman/f1.jpg"));
        assets.store("pacman.startpage.image2",         rm.loadImage("graphics/pacman/f2.jpg"));
        assets.store("pacman.startpage.image3",         rm.loadImage("graphics/pacman/f3.jpg"));

        assets.store("pacman.helpButton.icon",          rm.loadImage("graphics/icons/help-blue-64.png"));
        assets.store("pacman.icon",                     rm.loadImage("graphics/icons/pacman.png"));

        assets.store("pacman.color.game_over_message",  Color.RED);
        assets.store("pacman.color.ready_message",      Color.YELLOW);

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
        assets.store("pacman.audio.pacman_munch",       rm.url("sound/pacman/munch.wav"));
        assets.store("pacman.audio.pacman_power",       rm.url("sound/pacman/ghost-turn-to-blue.mp3"));
        assets.store("pacman.audio.level_complete",     rm.url("sound/common/level-complete.mp3"));
        assets.store("pacman.audio.siren.1",            rm.url("sound/pacman/siren_1.mp3"));
        assets.store("pacman.audio.siren.2",            rm.url("sound/pacman/siren_2.mp3"));
        assets.store("pacman.audio.siren.3",            rm.url("sound/pacman/siren_3.mp3"));
        assets.store("pacman.audio.siren.4",            rm.url("sound/pacman/siren_4.mp3"));
        assets.store("pacman.audio.ghost_returns",      rm.url("sound/pacman/retreating.mp3"));

        //
        // Pac-Man XXL
        //
        assets.store("pacman_xxl.scene_background",     imageBackground(rm.loadImage("graphics/pacman_wallpaper.png")));

        assets.store("pacman_xxl.icon",                 rm.loadImage("graphics/icons/pacman.png"));
        assets.store("pacman_xxl.helpButton.icon",      rm.loadImage("graphics/icons/help-blue-64.png"));
        assets.store("pacman_xxl.startpage.source",     rm.loadImage("graphics/pacman_xxl/pacman_xxl_logo.png"));

        assets.store("pacman_xxl.spritesheet",          new PacManGameSpriteSheet(rm.loadImage("graphics/pacman/pacman_spritesheet.png")));

        assets.store("pacman_xxl.color.game_over_message", Color.RED);
        assets.store("pacman_xxl.color.ready_message",  Color.YELLOW);

        // Clips
        assets.store("pacman_xxl.audio.bonus_eaten",    rm.loadAudioClip("sound/pacman/eat_fruit.mp3"));
        assets.store("pacman_xxl.audio.credit",         rm.loadAudioClip("sound/pacman/credit.wav"));
        assets.store("pacman_xxl.audio.extra_life",     rm.loadAudioClip("sound/pacman/extend.mp3"));
        assets.store("pacman_xxl.audio.ghost_eaten",    rm.loadAudioClip("sound/pacman/eat_ghost.mp3"));
        assets.store("pacman_xxl.audio.sweep",          rm.loadAudioClip("sound/common/sweep.mp3"));

        // Media player sounds
        assets.store("pacman_xxl.audio.game_ready",     rm.url("sound/pacman/game_start.mp3"));
        assets.store("pacman_xxl.audio.game_over",      rm.url("sound/common/game-over.mp3"));
        assets.store("pacman_xxl.audio.intermission",   rm.url("sound/pacman/intermission.mp3"));
        assets.store("pacman_xxl.audio.pacman_death",   rm.url("sound/pacman/pacman_death.wav"));
        assets.store("pacman_xxl.audio.pacman_munch",   rm.url("sound/pacman/munch.wav"));
        assets.store("pacman_xxl.audio.pacman_power",   rm.url("sound/pacman/ghost-turn-to-blue.mp3"));
        assets.store("pacman_xxl.audio.level_complete", rm.url("sound/common/level-complete.mp3"));
        assets.store("pacman_xxl.audio.siren.1",        rm.url("sound/pacman/siren_1.mp3"));
        assets.store("pacman_xxl.audio.siren.2",        rm.url("sound/pacman/siren_2.mp3"));
        assets.store("pacman_xxl.audio.siren.3",        rm.url("sound/pacman/siren_3.mp3"));
        assets.store("pacman_xxl.audio.siren.4",        rm.url("sound/pacman/siren_4.mp3"));
        assets.store("pacman_xxl.audio.ghost_returns",  rm.url("sound/pacman/retreating.mp3"));
    }
}