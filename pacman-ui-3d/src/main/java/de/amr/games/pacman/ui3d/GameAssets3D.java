/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static de.amr.games.pacman.ui3d.PacManGames3dApp.PY_3D_FLOOR_COLOR;

/**
 * @author Armin Reichert
 */
public class GameAssets3D {
    
    public static void load(ResourceManager rm, AssetStorage assets) {
        assets.addBundle(ResourceBundle.getBundle("de.amr.games.pacman.ui3d.texts.messages", rm.rootClass().getModule()));

        assets.store("model3D.pacman", new Model3D(rm.url("model3D/pacman.obj")));
        assets.store("model3D.ghost",  new Model3D(rm.url("model3D/ghost.obj")));
        assets.store("model3D.pellet", new Model3D(rm.url("model3D/fruit.obj")));

        assets.store("wallpaper.day",   rm.loadImage("graphics/sea-wallpaper.jpg"));
        assets.store("wallpaper.night", rm.loadImage("graphics/sea-wallpaper-night.jpg"));

        Map<String, PhongMaterial> texturesByName = new LinkedHashMap<>();
        assets.store("floorTextures", texturesByName);
        Stream.of("Carpet", "Rubber", "Wood").forEach(name -> {
            var texture = new PhongMaterial();
            texture.setBumpMap(rm.loadImage("graphics/textures/%s-bump.jpg".formatted(name.toLowerCase())));
            texture.setDiffuseMap(rm.loadImage("graphics/textures/%s-diffuse.jpg".formatted(name.toLowerCase())));
            texture.diffuseColorProperty().bind(PY_3D_FLOOR_COLOR);
            texturesByName.put(name, texture);
        });

        // Ms. Pac-Man

        assets.store("ms_pacman.ghost.0.color.normal.dress",      assets.color("palette.red"));
        assets.store("ms_pacman.ghost.0.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.store("ms_pacman.ghost.0.color.normal.pupils",     assets.color("palette.blue"));
        assets.store("ms_pacman.ghost.1.color.normal.dress",      assets.color("palette.pink"));
        assets.store("ms_pacman.ghost.1.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.store("ms_pacman.ghost.1.color.normal.pupils",     assets.color("palette.blue"));
        assets.store("ms_pacman.ghost.2.color.normal.dress",      assets.color("palette.cyan"));
        assets.store("ms_pacman.ghost.2.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.store("ms_pacman.ghost.2.color.normal.pupils",     assets.color("palette.blue"));
        assets.store("ms_pacman.ghost.3.color.normal.dress",      assets.color("palette.orange"));
        assets.store("ms_pacman.ghost.3.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.store("ms_pacman.ghost.3.color.normal.pupils",     assets.color("palette.blue"));
        assets.store("ms_pacman.ghost.color.frightened.dress",    assets.color("palette.blue"));
        assets.store("ms_pacman.ghost.color.frightened.eyeballs", assets.color("palette.rose"));
        assets.store("ms_pacman.ghost.color.frightened.pupils",   assets.color("palette.rose"));
        assets.store("ms_pacman.ghost.color.flashing.dress",      assets.color("palette.pale"));
        assets.store("ms_pacman.ghost.color.flashing.eyeballs",   assets.color("palette.rose"));
        assets.store("ms_pacman.ghost.color.flashing.pupils",     assets.color("palette.red"));

        assets.store("ms_pacman.color.head",            Color.rgb(255, 255, 0));
        assets.store("ms_pacman.color.eyes",            Color.rgb(33, 33, 33));
        assets.store("ms_pacman.color.palate",          Color.rgb(240, 180, 160));
        assets.store("ms_pacman.color.boobs",           Color.rgb(255, 255, 0).deriveColor(0, 1.0, 0.96, 1.0));
        assets.store("ms_pacman.color.hairbow",         Color.rgb(255, 0, 0));
        assets.store("ms_pacman.color.hairbow.pearls",  Color.rgb(33, 33, 255));

        // Tengen Ms. Pac-Man

        assets.store("tengen.color.head",               Color.rgb(232, 208, 32));
        assets.store("tengen.color.eyes",               Color.rgb(32, 0, 176));
        assets.store("tengen.color.palate",             Color.rgb(240, 180, 160));
        assets.store("tengen.color.boobs",              Color.rgb(232, 208, 32).deriveColor(0, 1.0, 0.96, 1.0));
        assets.store("tengen.color.hairbow",            Color.rgb(176, 15, 48));
        assets.store("tengen.color.hairbow.pearls",     Color.rgb(32, 0, 176));

        assets.store("tengen.ghost.0.color.normal.dress",      Color.rgb(176, 15, 48));
        assets.store("tengen.ghost.0.color.normal.eyeballs",   Color.WHITE);
        assets.store("tengen.ghost.0.color.normal.pupils",     Color.rgb(224, 80, 0));
        assets.store("tengen.ghost.1.color.normal.dress",      Color.rgb(255, 96, 176));
        assets.store("tengen.ghost.1.color.normal.eyeballs",   Color.WHITE);
        assets.store("tengen.ghost.1.color.normal.pupils",     Color.rgb(63, 96, 248));
        assets.store("tengen.ghost.2.color.normal.dress",      Color.rgb(63, 96, 248));
        assets.store("tengen.ghost.2.color.normal.eyeballs",   Color.WHITE);
        assets.store("tengen.ghost.2.color.normal.pupils",     Color.rgb(63, 96, 248));
        assets.store("tengen.ghost.3.color.normal.dress",      Color.rgb(224, 80, 0));
        assets.store("tengen.ghost.3.color.normal.eyeballs",   Color.WHITE);
        assets.store("tengen.ghost.3.color.normal.pupils",     Color.rgb(176, 15, 48));
        assets.store("tengen.ghost.color.frightened.dress",    Color.rgb(32, 0, 176));
        assets.store("tengen.ghost.color.frightened.eyeballs", Color.WHITE);
        assets.store("tengen.ghost.color.frightened.pupils",   Color.WHITE);
        //TODO has two flashing colors
        assets.store("tengen.ghost.color.flashing.dress",      Color.WHITE);
        assets.store("tengen.ghost.color.flashing.eyeballs",   Color.rgb(176, 15, 48));
        assets.store("tengen.ghost.color.flashing.pupils",     Color.rgb(176, 15, 48));


        // Pac-Man

        assets.store("pacman.ghost.0.color.normal.dress",      assets.color("palette.red"));
        assets.store("pacman.ghost.0.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.store("pacman.ghost.0.color.normal.pupils",     assets.color("palette.blue"));
        assets.store("pacman.ghost.1.color.normal.dress",      assets.color("palette.pink"));
        assets.store("pacman.ghost.1.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.store("pacman.ghost.1.color.normal.pupils",     assets.color("palette.blue"));
        assets.store("pacman.ghost.2.color.normal.dress",      assets.color("palette.cyan"));
        assets.store("pacman.ghost.2.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.store("pacman.ghost.2.color.normal.pupils",     assets.color("palette.blue"));
        assets.store("pacman.ghost.3.color.normal.dress",      assets.color("palette.orange"));
        assets.store("pacman.ghost.3.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.store("pacman.ghost.3.color.normal.pupils",     assets.color("palette.blue"));
        assets.store("pacman.ghost.color.frightened.dress",    assets.color("palette.blue"));
        assets.store("pacman.ghost.color.frightened.eyeballs", assets.color("palette.rose"));
        assets.store("pacman.ghost.color.frightened.pupils",   assets.color("palette.rose"));
        assets.store("pacman.ghost.color.flashing.dress",      assets.color("palette.pale"));
        assets.store("pacman.ghost.color.flashing.eyeballs",   assets.color("palette.rose"));
        assets.store("pacman.ghost.color.flashing.pupils",     assets.color("palette.red"));


        assets.store("pacman.color.head",               Color.rgb(255, 255, 0));
        assets.store("pacman.color.eyes",               Color.rgb(33, 33, 33));
        assets.store("pacman.color.palate",             Color.rgb(240, 180, 160));

        // Pac-Man XXL

        assets.store("pacman_xxl.ghost.0.color.normal.dress",      assets.color("palette.red"));
        assets.store("pacman_xxl.ghost.0.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.store("pacman_xxl.ghost.0.color.normal.pupils",     assets.color("palette.blue"));
        assets.store("pacman_xxl.ghost.1.color.normal.dress",      assets.color("palette.pink"));
        assets.store("pacman_xxl.ghost.1.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.store("pacman_xxl.ghost.1.color.normal.pupils",     assets.color("palette.blue"));
        assets.store("pacman_xxl.ghost.2.color.normal.dress",      assets.color("palette.cyan"));
        assets.store("pacman_xxl.ghost.2.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.store("pacman_xxl.ghost.2.color.normal.pupils",     assets.color("palette.blue"));
        assets.store("pacman_xxl.ghost.3.color.normal.dress",      assets.color("palette.orange"));
        assets.store("pacman_xxl.ghost.3.color.normal.eyeballs",   assets.color("palette.pale"));
        assets.store("pacman_xxl.ghost.3.color.normal.pupils",     assets.color("palette.blue"));
        assets.store("pacman_xxl.ghost.color.frightened.dress",    assets.color("palette.blue"));
        assets.store("pacman_xxl.ghost.color.frightened.eyeballs", assets.color("palette.rose"));
        assets.store("pacman_xxl.ghost.color.frightened.pupils",   assets.color("palette.rose"));
        assets.store("pacman_xxl.ghost.color.flashing.dress",      assets.color("palette.pale"));
        assets.store("pacman_xxl.ghost.color.flashing.eyeballs",   assets.color("palette.rose"));
        assets.store("pacman_xxl.ghost.color.flashing.pupils",     assets.color("palette.red"));

        assets.store("pacman_xxl.color.head",           Color.rgb(255, 255, 0));
        assets.store("pacman_xxl.color.eyes",           Color.rgb(33, 33, 33));
        assets.store("pacman_xxl.color.palate",         Color.rgb(240, 180, 160));
    }
}
