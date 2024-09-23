/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.ui2d.GameAssets2D;
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
public class GameAssets3D extends AssetStorage {
    
    public GameAssets3D(ResourceManager rm, GameAssets2D assets2D) {
        addAll(assets2D);
        addBundle(ResourceBundle.getBundle("de.amr.games.pacman.ui3d.texts.messages", rm.rootClass().getModule()));

        store("model3D.pacman", new Model3D(rm.url("model3D/pacman.obj")));
        store("model3D.ghost",  new Model3D(rm.url("model3D/ghost.obj")));
        store("model3D.pellet", new Model3D(rm.url("model3D/fruit.obj")));

        store("wallpaper.day",   rm.loadImage("graphics/sea-wallpaper.jpg"));
        store("wallpaper.night", rm.loadImage("graphics/sea-wallpaper-night.jpg"));

        Map<String, PhongMaterial> texturesByName = new LinkedHashMap<>();
        store("floorTextures", texturesByName);
        Stream.of("Carpet", "Rubber", "Wood").forEach(name -> {
            var texture = new PhongMaterial();
            texture.setBumpMap(rm.loadImage("graphics/textures/%s-bump.jpg".formatted(name.toLowerCase())));
            texture.setDiffuseMap(rm.loadImage("graphics/textures/%s-diffuse.jpg".formatted(name.toLowerCase())));
            texture.diffuseColorProperty().bind(PY_3D_FLOOR_COLOR);
            texturesByName.put(name, texture);
        });

        store("ghost.0.color.normal.dress",      color("palette.red"));
        store("ghost.0.color.normal.eyeballs",   color("palette.pale"));
        store("ghost.0.color.normal.pupils",     color("palette.blue"));

        store("ghost.1.color.normal.dress",      color("palette.pink"));
        store("ghost.1.color.normal.eyeballs",   color("palette.pale"));
        store("ghost.1.color.normal.pupils",     color("palette.blue"));

        store("ghost.2.color.normal.dress",      color("palette.cyan"));
        store("ghost.2.color.normal.eyeballs",   color("palette.pale"));
        store("ghost.2.color.normal.pupils",     color("palette.blue"));

        store("ghost.3.color.normal.dress",      color("palette.orange"));
        store("ghost.3.color.normal.eyeballs",   color("palette.pale"));
        store("ghost.3.color.normal.pupils",     color("palette.blue"));

        store("ghost.color.frightened.dress",    color("palette.blue"));
        store("ghost.color.frightened.eyeballs", color("palette.rose"));
        store("ghost.color.frightened.pupils",   color("palette.rose"));

        store("ghost.color.flashing.dress",      color("palette.pale"));
        store("ghost.color.flashing.eyeballs",   color("palette.rose"));
        store("ghost.color.flashing.pupils",     color("palette.red"));

        store("ms_pacman.color.head",            Color.rgb(255, 255, 0));
        store("ms_pacman.color.eyes",            Color.rgb(33, 33, 33));
        store("ms_pacman.color.palate",          Color.rgb(240, 180, 160));
        store("ms_pacman.color.boobs",           Color.rgb(255, 255, 0).deriveColor(0, 1.0, 0.96, 1.0));
        store("ms_pacman.color.hairbow",         Color.rgb(255, 0, 0));
        store("ms_pacman.color.hairbow.pearls",  Color.rgb(33, 33, 255));

        store("tengen.color.head",               Color.rgb(255, 255, 0));
        store("tengen.color.eyes",               Color.rgb(33, 33, 33));
        store("tengen.color.palate",             Color.rgb(240, 180, 160));
        store("tengen.color.boobs",              Color.rgb(255, 255, 0).deriveColor(0, 1.0, 0.96, 1.0));
        store("tengen.color.hairbow",            Color.rgb(255, 0, 0));
        store("tengen.color.hairbow.pearls",     Color.rgb(33, 33, 255));

        store("pacman.color.head",               Color.rgb(255, 255, 0));
        store("pacman.color.eyes",               Color.rgb(33, 33, 33));
        store("pacman.color.palate",             Color.rgb(240, 180, 160));

        store("pacman_xxl.color.head",           Color.rgb(255, 255, 0));
        store("pacman_xxl.color.eyes",           Color.rgb(33, 33, 33));
        store("pacman_xxl.color.palate",         Color.rgb(240, 180, 160));
    }
}
