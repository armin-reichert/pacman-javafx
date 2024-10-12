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

    public static final String NO_TEXTURE = "No Texture";

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
        assets.store("floor_textures", texturesByName);

        // Ms. Pac-Man

        assets.store("ms_pacman.pac.color.head", ARCADE_YELLOW);
        assets.store("ms_pacman.pac.color.eyes",                  Color.grayRgb(33));
        assets.store("ms_pacman.pac.color.palate",                Color.rgb(240, 180, 160));
        assets.store("ms_pacman.pac.color.boobs",                 ARCADE_YELLOW.deriveColor(0, 1.0, 0.96, 1.0));
        assets.store("ms_pacman.pac.color.hairbow", ARCADE_RED);
        assets.store("ms_pacman.pac.color.hairbow.pearls", ARCADE_BLUE);

        assets.store("ms_pacman.ghost.0.color.normal.dress", ARCADE_RED);
        assets.store("ms_pacman.ghost.0.color.normal.eyeballs", ARCADE_PALE);
        assets.store("ms_pacman.ghost.0.color.normal.pupils", ARCADE_BLUE);
        assets.store("ms_pacman.ghost.1.color.normal.dress", ARCADE_PINK);
        assets.store("ms_pacman.ghost.1.color.normal.eyeballs", ARCADE_PALE);
        assets.store("ms_pacman.ghost.1.color.normal.pupils", ARCADE_BLUE);
        assets.store("ms_pacman.ghost.2.color.normal.dress", ARCADE_CYAN);
        assets.store("ms_pacman.ghost.2.color.normal.eyeballs", ARCADE_PALE);
        assets.store("ms_pacman.ghost.2.color.normal.pupils", ARCADE_BLUE);
        assets.store("ms_pacman.ghost.3.color.normal.dress", ARCADE_ORANGE);
        assets.store("ms_pacman.ghost.3.color.normal.eyeballs", ARCADE_PALE);
        assets.store("ms_pacman.ghost.3.color.normal.pupils", ARCADE_BLUE);
        assets.store("ms_pacman.ghost.color.frightened.dress", ARCADE_BLUE);
        assets.store("ms_pacman.ghost.color.frightened.eyeballs", ARCADE_ROSE);
        assets.store("ms_pacman.ghost.color.frightened.pupils", ARCADE_ROSE);
        assets.store("ms_pacman.ghost.color.flashing.dress", ARCADE_PALE);
        assets.store("ms_pacman.ghost.color.flashing.eyeballs", ARCADE_ROSE);
        assets.store("ms_pacman.ghost.color.flashing.pupils", ARCADE_RED);

        // Tengen Ms. Pac-Man: see GameAssets2D

        // Pac-Man

        assets.store("pacman.pac.color.head",                     ARCADE_YELLOW);
        assets.store("pacman.pac.color.eyes",                     Color.grayRgb(33));
        assets.store("pacman.pac.color.palate",                   Color.rgb(240, 180, 160));

        assets.store("pacman.ghost.0.color.normal.dress", ARCADE_RED);
        assets.store("pacman.ghost.0.color.normal.eyeballs", ARCADE_PALE);
        assets.store("pacman.ghost.0.color.normal.pupils", ARCADE_BLUE);
        assets.store("pacman.ghost.1.color.normal.dress", ARCADE_PINK);
        assets.store("pacman.ghost.1.color.normal.eyeballs", ARCADE_PALE);
        assets.store("pacman.ghost.1.color.normal.pupils", ARCADE_BLUE);
        assets.store("pacman.ghost.2.color.normal.dress", ARCADE_CYAN);
        assets.store("pacman.ghost.2.color.normal.eyeballs", ARCADE_PALE);
        assets.store("pacman.ghost.2.color.normal.pupils", ARCADE_BLUE);
        assets.store("pacman.ghost.3.color.normal.dress", ARCADE_ORANGE);
        assets.store("pacman.ghost.3.color.normal.eyeballs", ARCADE_PALE);
        assets.store("pacman.ghost.3.color.normal.pupils", ARCADE_BLUE);
        assets.store("pacman.ghost.color.frightened.dress", ARCADE_BLUE);
        assets.store("pacman.ghost.color.frightened.eyeballs", ARCADE_ROSE);
        assets.store("pacman.ghost.color.frightened.pupils", ARCADE_ROSE);
        assets.store("pacman.ghost.color.flashing.dress", ARCADE_PALE);
        assets.store("pacman.ghost.color.flashing.eyeballs", ARCADE_ROSE);
        assets.store("pacman.ghost.color.flashing.pupils", ARCADE_RED);

        // Pac-Man XXL

        assets.store("pacman_xxl.pac.color.head", ARCADE_YELLOW);
        assets.store("pacman_xxl.pac.color.eyes",                  Color.grayRgb(33));
        assets.store("pacman_xxl.pac.color.palate",                Color.rgb(240, 180, 160));

        assets.store("pacman_xxl.ghost.0.color.normal.dress", ARCADE_RED);
        assets.store("pacman_xxl.ghost.0.color.normal.eyeballs", ARCADE_PALE);
        assets.store("pacman_xxl.ghost.0.color.normal.pupils", ARCADE_BLUE);
        assets.store("pacman_xxl.ghost.1.color.normal.dress", ARCADE_PINK);
        assets.store("pacman_xxl.ghost.1.color.normal.eyeballs", ARCADE_PALE);
        assets.store("pacman_xxl.ghost.1.color.normal.pupils", ARCADE_BLUE);
        assets.store("pacman_xxl.ghost.2.color.normal.dress", ARCADE_CYAN);
        assets.store("pacman_xxl.ghost.2.color.normal.eyeballs", ARCADE_PALE);
        assets.store("pacman_xxl.ghost.2.color.normal.pupils", ARCADE_BLUE);
        assets.store("pacman_xxl.ghost.3.color.normal.dress", ARCADE_ORANGE);
        assets.store("pacman_xxl.ghost.3.color.normal.eyeballs", ARCADE_PALE);
        assets.store("pacman_xxl.ghost.3.color.normal.pupils", ARCADE_BLUE);
        assets.store("pacman_xxl.ghost.color.frightened.dress", ARCADE_BLUE);
        assets.store("pacman_xxl.ghost.color.frightened.eyeballs", ARCADE_ROSE);
        assets.store("pacman_xxl.ghost.color.frightened.pupils", ARCADE_ROSE);
        assets.store("pacman_xxl.ghost.color.flashing.dress", ARCADE_PALE);
        assets.store("pacman_xxl.ghost.color.flashing.eyeballs", ARCADE_ROSE);
        assets.store("pacman_xxl.ghost.color.flashing.pupils", ARCADE_RED);
    }
}