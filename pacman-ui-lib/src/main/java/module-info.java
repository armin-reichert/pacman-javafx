// module is open to allow access to non-class resources
open module de.amr.pacmanfx.uilib {
    requires java.prefs;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;
    requires org.tinylog.api;

    requires de.amr.basics;
    requires de.amr.meshviewer;
    requires de.amr.objparser;
    requires de.amr.pacmanfx.core;

    exports de.amr.pacmanfx.uilib;
    exports de.amr.pacmanfx.uilib.animation;
    exports de.amr.pacmanfx.uilib.assets;
    exports de.amr.pacmanfx.uilib.model3D;
    exports de.amr.pacmanfx.uilib.model3D.animation;
    exports de.amr.pacmanfx.uilib.model3D.actor;
    exports de.amr.pacmanfx.uilib.model3D.world;
    exports de.amr.pacmanfx.uilib.rendering;
    exports de.amr.pacmanfx.uilib.widgets;
}