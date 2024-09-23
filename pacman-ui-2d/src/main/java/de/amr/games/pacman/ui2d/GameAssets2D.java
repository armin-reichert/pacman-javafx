/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.pacman.PacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.tengen.TengenMsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ResourceBundle;

/**
 * @author Armin Reichert
 */
public class GameAssets2D extends AssetStorage {

    public static String assetPrefix(GameVariant variant) {
        return switch (variant) {
            case MS_PACMAN -> "ms_pacman";
            case MS_PACMAN_TENGEN -> "tengen";
            case PACMAN -> "pacman";
            case PACMAN_XXL -> "pacman_xxl";
        };
    }

    public GameAssets2D(ResourceManager rm) {
        addBundle(ResourceBundle.getBundle("de.amr.games.pacman.ui2d.texts.messages", rm.rootClass().getModule()));

        // Dashboard

        store("source.armin1970",                 rm.loadImage("graphics/armin.jpg"));
        store("icon.mute",                       rm.loadImage("graphics/icons/mute.png"));
        store("icon.play",                       rm.loadImage("graphics/icons/play.png"));
        store("icon.stop",                       rm.loadImage("graphics/icons/stop.png"));
        store("icon.step",                       rm.loadImage("graphics/icons/step.png"));

        store("infobox.background_color",        Color.rgb(0,0,50,1.0));
        store("infobox.min_label_width",         110);
        store("infobox.min_col_width",           180);
        store("infobox.text_color",              Color.WHITE);
        store("infobox.label_font",              Font.font("Sans", 12));
        store("infobox.text_font",               Font.font("Sans", 12));

        //
        // Common to all game variants
        //

        store("palette.black",                   Color.rgb(0, 0, 0));
        store("palette.red",                     Color.rgb(255, 0, 0));
        store("palette.yellow",                  Color.rgb(255, 255, 0));
        store("palette.pink",                    Color.rgb(252, 181, 255));
        store("palette.cyan",                    Color.rgb(0, 255, 255));
        store("palette.orange",                  Color.rgb(251, 190, 88));
        store("palette.blue",                    Color.rgb(33, 33, 255));
        store("palette.pale",                    Color.rgb(222, 222, 255));
        store("palette.rose",                    Color.rgb(252, 187, 179));

        store("startpage.arrow.left",            rm.loadImage("graphics/icons/arrow-left.png"));
        store("startpage.arrow.right",           rm.loadImage("graphics/icons/arrow-right.png"));
        store("startpage.button.bgColor",        Color.rgb(0, 155, 252, 0.6));
        store("startpage.button.color",          Color.WHITE);
        store("startpage.button.font",           rm.loadFont("fonts/emulogic.ttf", 32));

        store("wallpaper.background",            Ufx.imageBackground(rm.loadImage("graphics/pacman_wallpaper.png")));
        store("wallpaper.color",                 Color.rgb(72, 78, 135));

        store("font.arcade",                     rm.loadFont("fonts/emulogic.ttf", 8));
        store("font.handwriting",                rm.loadFont("fonts/Molle-Italic.ttf", 9));
        store("font.monospaced",                 rm.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12));

        store("voice.explain",                   rm.url("sound/voice/press-key.mp3"));
        store("voice.autopilot.off",             rm.url("sound/voice/autopilot-off.mp3"));
        store("voice.autopilot.on",              rm.url("sound/voice/autopilot-on.mp3"));
        store("voice.immunity.off",              rm.url("sound/voice/immunity-off.mp3"));
        store("voice.immunity.on",               rm.url("sound/voice/immunity-on.mp3"));

        //
        // Ms. Pac-Man game
        //

        store("ms_pacman.spritesheet",           new MsPacManGameSpriteSheet(rm.loadImage("/de/amr/games/pacman/ui2d/graphics/mspacman/mspacman_spritesheet.png")));
        store("ms_pacman.flashing_mazes",        rm.loadImage("/de/amr/games/pacman/ui2d/graphics/mspacman/mazes_flashing.png"));

        store("ms_pacman.startpage.source",      rm.loadImage("graphics/mspacman/mspacman_flyer.png"));
        store("ms_pacman.startpage.image1",      rm.loadImage("graphics/mspacman/mspacman_flyer1.jpg"));
        store("ms_pacman.startpage.image2",      rm.loadImage("graphics/mspacman/mspacman_flyer2.jpg"));
        store("ms_pacman.helpButton.icon",       rm.loadImage("graphics/icons/help-red-64.png"));
        store("ms_pacman.icon",                  rm.loadImage("graphics/icons/mspacman.png"));
        store("ms_pacman.logo.midway",           rm.loadImage("graphics/mspacman/midway_logo.png"));

        // Clips
        store("ms_pacman.audio.bonus_eaten",     rm.loadAudioClip("sound/mspacman/Fruit.mp3"));
        store("ms_pacman.audio.credit",          rm.loadAudioClip("sound/mspacman/Credit.mp3"));
        store("ms_pacman.audio.extra_life",      rm.loadAudioClip("sound/mspacman/ExtraLife.mp3"));
        store("ms_pacman.audio.ghost_eaten",     rm.loadAudioClip("sound/mspacman/Ghost.mp3"));
        store("ms_pacman.audio.sweep",           rm.loadAudioClip("sound/common/sweep.mp3"));

        // Audio played by MediaPlayer
        store("ms_pacman.audio.game_ready",      rm.url("sound/mspacman/Start.mp3"));
        store("ms_pacman.audio.game_over",       rm.url("sound/common/game-over.mp3"));
        store("ms_pacman.audio.intermission.1",  rm.url("sound/mspacman/Act1TheyMeet.mp3"));
        store("ms_pacman.audio.intermission.2",  rm.url("sound/mspacman/Act2TheChase.mp3"));
        store("ms_pacman.audio.intermission.3",  rm.url("sound/mspacman/Act3Junior.mp3"));
        store("ms_pacman.audio.level_complete",  rm.url("sound/common/level-complete.mp3"));
        store("ms_pacman.audio.pacman_death",    rm.url("sound/mspacman/Died.mp3"));
        store("ms_pacman.audio.pacman_munch",    rm.url("sound/mspacman/Pill.wav"));
        store("ms_pacman.audio.pacman_power",    rm.url("sound/mspacman/ScaredGhost.mp3"));
        store("ms_pacman.audio.siren.1",         rm.url("sound/mspacman/GhostNoise1.wav"));
        store("ms_pacman.audio.siren.2",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        store("ms_pacman.audio.siren.3",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        store("ms_pacman.audio.siren.4",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        store("ms_pacman.audio.ghost_returning", rm.url("sound/mspacman/GhostEyes.mp3"));

        //
        // Ms. Pac-Man game Tengen
        //

        store("tengen.spritesheet",              new TengenMsPacManGameSpriteSheet(rm.loadImage("/de/amr/games/pacman/ui2d/graphics/tengen/spritesheet.png")));
        store("tengen.mazes.arcade",             rm.loadImage("graphics/tengen/arcade_mazes.png"));
        store("tengen.mazes.non_arcade",         rm.loadImage("graphics/tengen/non_arcade_mazes.png"));
        store("tengen.startpage.source",         rm.loadImage("graphics/tengen/tengen_flyer.jpg"));
        store("tengen.helpButton.icon",          rm.loadImage("graphics/icons/help-red-64.png"));
        store("tengen.icon",                     rm.loadImage("graphics/icons/mspacman.png"));
        store("tengen.logo.midway",              rm.loadImage("graphics/mspacman/midway_logo.png"));

        // Clips
        store("tengen.audio.bonus_eaten",        rm.loadAudioClip("sound/mspacman/Fruit.mp3"));
        store("tengen.audio.credit",             rm.loadAudioClip("sound/mspacman/Credit.mp3"));
        store("tengen.audio.extra_life",         rm.loadAudioClip("sound/mspacman/ExtraLife.mp3"));
        store("tengen.audio.ghost_eaten",        rm.loadAudioClip("sound/mspacman/Ghost.mp3"));
        store("tengen.audio.sweep",              rm.loadAudioClip("sound/common/sweep.mp3"));

        // Audio played by MediaPlayer
        store("tengen.audio.game_ready",         rm.url("sound/mspacman/Start.mp3"));
        store("tengen.audio.game_over",          rm.url("sound/common/game-over.mp3"));
        store("tengen.audio.intermission.1",     rm.url("sound/mspacman/Act1TheyMeet.mp3"));
        store("tengen.audio.intermission.2",     rm.url("sound/mspacman/Act2TheChase.mp3"));
        store("tengen.audio.intermission.3",     rm.url("sound/mspacman/Act3Junior.mp3"));
        store("tengen.audio.level_complete",     rm.url("sound/common/level-complete.mp3"));
        store("tengen.audio.pacman_death",       rm.url("sound/mspacman/Died.mp3"));
        store("tengen.audio.pacman_munch",       rm.url("sound/mspacman/Pill.wav"));
        store("tengen.audio.pacman_power",       rm.url("sound/mspacman/ScaredGhost.mp3"));
        store("tengen.audio.siren.1",            rm.url("sound/mspacman/GhostNoise1.wav"));
        store("tengen.audio.siren.2",            rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        store("tengen.audio.siren.3",            rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        store("tengen.audio.siren.4",            rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        store("tengen.audio.ghost_returning",    rm.url("sound/mspacman/GhostEyes.mp3"));

        //
        // Pac-Man game
        //

        store("pacman.spritesheet",       new PacManGameSpriteSheet(rm.loadImage("/de/amr/games/pacman/ui2d/graphics/pacman/pacman_spritesheet.png")));
        store("pacman.flashing_maze",     rm.loadImage("/de/amr/games/pacman/ui2d/graphics/pacman/maze_flashing.png"));
        store("pacman.startpage.image1",  rm.loadImage("graphics/pacman/pacman_flyer.png"));
        store("pacman.startpage.image2",  rm.loadImage("graphics/pacman/pacman_flyer2.jpg"));
        store("pacman.helpButton.icon",   rm.loadImage("graphics/icons/help-blue-64.png"));
        store("pacman.icon",              rm.loadImage("graphics/icons/pacman.png"));

        // Clips
        store("pacman.audio.bonus_eaten", rm.loadAudioClip("sound/pacman/eat_fruit.mp3"));
        store("pacman.audio.credit",      rm.loadAudioClip("sound/pacman/credit.wav"));
        store("pacman.audio.extra_life",  rm.loadAudioClip("sound/pacman/extend.mp3"));
        store("pacman.audio.ghost_eaten", rm.loadAudioClip("sound/pacman/eat_ghost.mp3"));
        store("pacman.audio.sweep",       rm.loadAudioClip("sound/common/sweep.mp3"));

        // Media player sounds
        store("pacman.audio.game_ready",         rm.url("sound/pacman/game_start.mp3"));
        store("pacman.audio.game_over",          rm.url("sound/common/game-over.mp3"));
        store("pacman.audio.intermission",       rm.url("sound/pacman/intermission.mp3"));
        store("pacman.audio.pacman_death",       rm.url("sound/pacman/pacman_death.wav"));
        store("pacman.audio.pacman_munch",       rm.url("sound/pacman/doublemunch.wav"));
        store("pacman.audio.pacman_power",       rm.url("sound/pacman/ghost-turn-to-blue.mp3"));
        store("pacman.audio.level_complete",     rm.url("sound/common/level-complete.mp3"));
        store("pacman.audio.siren.1",            rm.url("sound/pacman/siren_1.mp3"));
        store("pacman.audio.siren.2",            rm.url("sound/pacman/siren_2.mp3"));
        store("pacman.audio.siren.3",            rm.url("sound/pacman/siren_3.mp3"));
        store("pacman.audio.siren.4",            rm.url("sound/pacman/siren_4.mp3"));
        store("pacman.audio.ghost_returning",    rm.url("sound/pacman/retreating.mp3"));

        //
        // Pac-Man XXL
        //
        store("pacman_xxl.icon",                 rm.loadImage("graphics/icons/pacman.png"));
        store("pacman_xxl.helpButton.icon",      rm.loadImage("graphics/icons/help-blue-64.png"));
        store("pacman_xxl.startpage.source",     rm.loadImage("graphics/pacman_xxl/pacman_xxl_logo.png"));

        store("pacman_xxl.spritesheet",          new PacManGameSpriteSheet(rm.loadImage("/de/amr/games/pacman/ui2d/graphics/pacman/pacman_spritesheet.png")));

        // Clips
        store("pacman_xxl.audio.bonus_eaten",        rm.loadAudioClip("sound/pacman/eat_fruit.mp3"));
        store("pacman_xxl.audio.credit",             rm.loadAudioClip("sound/pacman/credit.wav"));
        store("pacman_xxl.audio.extra_life",         rm.loadAudioClip("sound/pacman/extend.mp3"));
        store("pacman_xxl.audio.ghost_eaten",        rm.loadAudioClip("sound/pacman/eat_ghost.mp3"));
        store("pacman_xxl.audio.sweep",              rm.loadAudioClip("sound/common/sweep.mp3"));

        // Media player sounds
        store("pacman_xxl.audio.game_ready",         rm.url("sound/pacman/game_start.mp3"));
        store("pacman_xxl.audio.game_over",          rm.url("sound/common/game-over.mp3"));
        store("pacman_xxl.audio.intermission",       rm.url("sound/pacman/intermission.mp3"));
        store("pacman_xxl.audio.pacman_death",       rm.url("sound/pacman/pacman_death.wav"));
        store("pacman_xxl.audio.pacman_munch",       rm.url("sound/pacman/doublemunch.wav"));
        store("pacman_xxl.audio.pacman_power",       rm.url("sound/pacman/ghost-turn-to-blue.mp3"));
        store("pacman_xxl.audio.level_complete",     rm.url("sound/common/level-complete.mp3"));
        store("pacman_xxl.audio.siren.1",            rm.url("sound/pacman/siren_1.mp3"));
        store("pacman_xxl.audio.siren.2",            rm.url("sound/pacman/siren_2.mp3"));
        store("pacman_xxl.audio.siren.3",            rm.url("sound/pacman/siren_3.mp3"));
        store("pacman_xxl.audio.siren.4",            rm.url("sound/pacman/siren_4.mp3"));
        store("pacman_xxl.audio.ghost_returning",    rm.url("sound/pacman/retreating.mp3"));
    }
}