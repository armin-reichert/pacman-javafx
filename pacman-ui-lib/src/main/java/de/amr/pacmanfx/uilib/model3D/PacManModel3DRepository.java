/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import org.tinylog.Logger;

import java.net.URL;

public class PacManModel3DRepository implements Disposable {

    private static final ResourceManager RM = () -> PacManModel3DRepository.class;

    // Initialization-on-Demand Holder Idiom
    private static class Holder {
        static final PacManModel3DRepository INSTANCE = new PacManModel3DRepository();
    }

    public static PacManModel3DRepository instance() {
        return Holder.INSTANCE;
    }

    public static Model3D loadModelFromObjFile(String objFilePath) {
        final URL url = RM.url(objFilePath);
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

    private PacManModel3DRepository() {}

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