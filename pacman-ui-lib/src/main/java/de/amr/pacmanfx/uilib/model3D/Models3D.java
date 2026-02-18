/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import org.tinylog.Logger;

import java.net.URL;

public class Models3D implements Disposable {

    private static final ResourceManager RESOURCE_MANAGER = () -> Models3D.class;

    public static Model3D loadModelFromObjFile(String objFilePath) {
        final URL url = RESOURCE_MANAGER.url(objFilePath);
        if (url == null) {
            throw new IllegalArgumentException("Could not access model3D with resource path '%s'".formatted(objFilePath));
        }
        try {
            return new Model3D(url);
        } catch (Exception x) {
            throw new IllegalArgumentException("Could not load 3D model from URL '%s'".formatted(url), x);
        }
    }

    private final PacManModel3D pacManModel = new PacManModel3D();
    private final GhostModel3D ghostModel = new GhostModel3D();
    private final PelletModel3D pelletModel = new PelletModel3D();

    public Models3D() {}

    public PacManModel3D pacManModel() {
        return pacManModel;
    }

    public GhostModel3D ghostModel() {
        return ghostModel;
    }

    public PelletModel3D pelletModel() {
        return pelletModel;
    }

    @Override
    public void dispose() {
        Logger.info("Dispose 3D model repository");
        pacManModel.dispose();
        ghostModel.dispose();
        pelletModel.dispose();
    }
}