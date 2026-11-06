module experiments {
    requires de.amr.basics;
    requires de.amr.meshbuilder;
    requires de.amr.objparser;
    requires de.amr.pacmanfx.core;
    requires javafx.controls;
    requires javafx.graphics;
    requires org.tinylog.api;

    exports experiments.custommaps;
    exports experiments.scooter;
}