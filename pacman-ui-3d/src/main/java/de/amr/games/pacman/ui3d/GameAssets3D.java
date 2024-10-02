/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import de.amr.games.pacman.ui2d.util.Ufx;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.ui2d.GameAssets2D.*;
import static de.amr.games.pacman.ui3d.PacManGames3dApp.PY_3D_FLOOR_COLOR;

/**
 * @author Armin Reichert
 */
public class GameAssets3D {
    
    public static void addTo(AssetStorage assets) {
        ResourceManager rm = () -> GameAssets3D.class;

        assets.addBundle(rm.getModuleBundle("de.amr.games.pacman.ui3d.texts.messages"));

        assets.store("model3D.pacman", new Model3D(rm.url("model3D/pacman.obj")));
        assets.store("model3D.ghost",  new Model3D(rm.url("model3D/ghost.obj")));
        assets.store("model3D.pellet", new Model3D(rm.url("model3D/fruit.obj")));

        assets.store("wallpaper.day",   Ufx.wallpaperBackground(rm.loadImage("graphics/abstract-wallpaper-1.jpg")));
        assets.store("wallpaper.night", Ufx.wallpaperBackground(rm.loadImage("graphics/abstract-wallpaper-2.jpg")));

        Map<String, PhongMaterial> texturesByName = new LinkedHashMap<>();
        Stream.of("Carpet", "Rubber", "Wood").forEach(name -> {
            var texture = new PhongMaterial();
            texture.setBumpMap(rm.loadImage("graphics/textures/%s-bump.jpg".formatted(name.toLowerCase())));
            texture.setDiffuseMap(rm.loadImage("graphics/textures/%s-diffuse.jpg".formatted(name.toLowerCase())));
            texture.diffuseColorProperty().bind(PY_3D_FLOOR_COLOR);
            texturesByName.put(name, texture);
        });
        assets.store("floorTextures", texturesByName);

        // Ms. Pac-Man

        assets.store("ms_pacman.pac.color.head",                  PALETTE_YELLOW);
        assets.store("ms_pacman.pac.color.eyes",                  Color.grayRgb(33));
        assets.store("ms_pacman.pac.color.palate",                Color.rgb(240, 180, 160));
        assets.store("ms_pacman.pac.color.boobs",                 PALETTE_YELLOW.deriveColor(0, 1.0, 0.96, 1.0));
        assets.store("ms_pacman.pac.color.hairbow",               PALETTE_RED);
        assets.store("ms_pacman.pac.color.hairbow.pearls",        PALETTE_BLUE);

        assets.store("ms_pacman.ghost.0.color.normal.dress",      PALETTE_RED);
        assets.store("ms_pacman.ghost.0.color.normal.eyeballs",   PALETTE_PALE);
        assets.store("ms_pacman.ghost.0.color.normal.pupils",     PALETTE_BLUE);
        assets.store("ms_pacman.ghost.1.color.normal.dress",      PALETTE_PINK);
        assets.store("ms_pacman.ghost.1.color.normal.eyeballs",   PALETTE_PALE);
        assets.store("ms_pacman.ghost.1.color.normal.pupils",     PALETTE_BLUE);
        assets.store("ms_pacman.ghost.2.color.normal.dress",      PALETTE_CYAN);
        assets.store("ms_pacman.ghost.2.color.normal.eyeballs",   PALETTE_PALE);
        assets.store("ms_pacman.ghost.2.color.normal.pupils",     PALETTE_BLUE);
        assets.store("ms_pacman.ghost.3.color.normal.dress",      PALETTE_ORANGE);
        assets.store("ms_pacman.ghost.3.color.normal.eyeballs",   PALETTE_PALE);
        assets.store("ms_pacman.ghost.3.color.normal.pupils",     PALETTE_BLUE);
        assets.store("ms_pacman.ghost.color.frightened.dress",    PALETTE_BLUE);
        assets.store("ms_pacman.ghost.color.frightened.eyeballs", PALETTE_ROSE);
        assets.store("ms_pacman.ghost.color.frightened.pupils",   PALETTE_ROSE);
        assets.store("ms_pacman.ghost.color.flashing.dress",      PALETTE_PALE);
        assets.store("ms_pacman.ghost.color.flashing.eyeballs",   PALETTE_ROSE);
        assets.store("ms_pacman.ghost.color.flashing.pupils",     PALETTE_RED);

        // Tengen Ms. Pac-Man

        assets.store("tengen.pac.color.head",                     Color.rgb(232, 208, 32));
        assets.store("tengen.pac.color.eyes",                     Color.rgb(32, 0, 176));
        assets.store("tengen.pac.color.palate",                   Color.grayRgb(66));
        assets.store("tengen.pac.color.boobs",                    Color.rgb(232, 208, 32).deriveColor(0, 1.0, 0.96, 1.0));
        assets.store("tengen.pac.color.hairbow",                  Color.rgb(176, 15, 48));
        assets.store("tengen.pac.color.hairbow.pearls",           Color.rgb(32, 0, 176));

        assets.store("tengen.ghost.0.color.normal.dress",         Color.rgb(176, 15, 48));
        assets.store("tengen.ghost.0.color.normal.eyeballs",      Color.WHITE);
        assets.store("tengen.ghost.0.color.normal.pupils",        Color.rgb(224, 80, 0));
        assets.store("tengen.ghost.1.color.normal.dress",         Color.rgb(255, 96, 176));
        assets.store("tengen.ghost.1.color.normal.eyeballs",      Color.WHITE);
        assets.store("tengen.ghost.1.color.normal.pupils",        Color.rgb(63, 96, 248));
        assets.store("tengen.ghost.2.color.normal.dress",         Color.rgb(63, 96, 248));
        assets.store("tengen.ghost.2.color.normal.eyeballs",      Color.WHITE);
        assets.store("tengen.ghost.2.color.normal.pupils",        Color.rgb(63, 96, 248));
        assets.store("tengen.ghost.3.color.normal.dress",         Color.rgb(224, 80, 0));
        assets.store("tengen.ghost.3.color.normal.eyeballs",      Color.WHITE);
        assets.store("tengen.ghost.3.color.normal.pupils",        Color.rgb(176, 15, 48));
        assets.store("tengen.ghost.color.frightened.dress",       Color.rgb(32, 0, 176));
        assets.store("tengen.ghost.color.frightened.eyeballs",    Color.WHITE);
        assets.store("tengen.ghost.color.frightened.pupils",      Color.WHITE);
        //TODO has two flashing colors
        assets.store("tengen.ghost.color.flashing.dress",         Color.WHITE);
        assets.store("tengen.ghost.color.flashing.eyeballs",      Color.rgb(176, 15, 48));
        assets.store("tengen.ghost.color.flashing.pupils",        Color.rgb(176, 15, 48));


        // Pac-Man

        assets.store("pacman.pac.color.head",                  PALETTE_YELLOW);
        assets.store("pacman.pac.color.eyes",                  Color.grayRgb(33));
        assets.store("pacman.pac.color.palate",                Color.rgb(240, 180, 160));

        assets.store("pacman.ghost.0.color.normal.dress",      PALETTE_RED);
        assets.store("pacman.ghost.0.color.normal.eyeballs",   PALETTE_PALE);
        assets.store("pacman.ghost.0.color.normal.pupils",     PALETTE_BLUE);
        assets.store("pacman.ghost.1.color.normal.dress",      PALETTE_PINK);
        assets.store("pacman.ghost.1.color.normal.eyeballs",   PALETTE_PALE);
        assets.store("pacman.ghost.1.color.normal.pupils",     PALETTE_BLUE);
        assets.store("pacman.ghost.2.color.normal.dress",      PALETTE_CYAN);
        assets.store("pacman.ghost.2.color.normal.eyeballs",   PALETTE_PALE);
        assets.store("pacman.ghost.2.color.normal.pupils",     PALETTE_BLUE);
        assets.store("pacman.ghost.3.color.normal.dress",      PALETTE_ORANGE);
        assets.store("pacman.ghost.3.color.normal.eyeballs",   PALETTE_PALE);
        assets.store("pacman.ghost.3.color.normal.pupils",     PALETTE_BLUE);
        assets.store("pacman.ghost.color.frightened.dress",    PALETTE_BLUE);
        assets.store("pacman.ghost.color.frightened.eyeballs", PALETTE_ROSE);
        assets.store("pacman.ghost.color.frightened.pupils",   PALETTE_ROSE);
        assets.store("pacman.ghost.color.flashing.dress",      PALETTE_PALE);
        assets.store("pacman.ghost.color.flashing.eyeballs",   PALETTE_ROSE);
        assets.store("pacman.ghost.color.flashing.pupils",     PALETTE_RED);

        // Pac-Man XXL

        assets.store("pacman_xxl.pac.color.head",                  PALETTE_YELLOW);
        assets.store("pacman_xxl.pac.color.eyes",                  Color.grayRgb(33));
        assets.store("pacman_xxl.pac.color.palate",                Color.rgb(240, 180, 160));

        assets.store("pacman_xxl.ghost.0.color.normal.dress",      PALETTE_RED);
        assets.store("pacman_xxl.ghost.0.color.normal.eyeballs",   PALETTE_PALE);
        assets.store("pacman_xxl.ghost.0.color.normal.pupils",     PALETTE_BLUE);
        assets.store("pacman_xxl.ghost.1.color.normal.dress",      PALETTE_PINK);
        assets.store("pacman_xxl.ghost.1.color.normal.eyeballs",   PALETTE_PALE);
        assets.store("pacman_xxl.ghost.1.color.normal.pupils",     PALETTE_BLUE);
        assets.store("pacman_xxl.ghost.2.color.normal.dress",      PALETTE_CYAN);
        assets.store("pacman_xxl.ghost.2.color.normal.eyeballs",   PALETTE_PALE);
        assets.store("pacman_xxl.ghost.2.color.normal.pupils",     PALETTE_BLUE);
        assets.store("pacman_xxl.ghost.3.color.normal.dress",      PALETTE_ORANGE);
        assets.store("pacman_xxl.ghost.3.color.normal.eyeballs",   PALETTE_PALE);
        assets.store("pacman_xxl.ghost.3.color.normal.pupils",     PALETTE_BLUE);
        assets.store("pacman_xxl.ghost.color.frightened.dress",    PALETTE_BLUE);
        assets.store("pacman_xxl.ghost.color.frightened.eyeballs", PALETTE_ROSE);
        assets.store("pacman_xxl.ghost.color.frightened.pupils",   PALETTE_ROSE);
        assets.store("pacman_xxl.ghost.color.flashing.dress",      PALETTE_PALE);
        assets.store("pacman_xxl.ghost.color.flashing.eyeballs",   PALETTE_ROSE);
        assets.store("pacman_xxl.ghost.color.flashing.pupils",     PALETTE_RED);
    }
}