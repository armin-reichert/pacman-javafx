module de.amr.basics.ui {

    requires de.amr.basics;
    requires org.tinylog.api;
    requires javafx.base;
    requires javafx.graphics;

    exports de.amr.basics.ui.ecs;
    exports de.amr.basics.ui.ecs.comp;
    exports de.amr.basics.ui.ecs.systems;
    exports de.amr.basics.ui.spriteanim;
    exports de.amr.basics.ui.entities.props.textdisplay;
    exports de.amr.basics.ui.entities.props.marquee;
    exports de.amr.basics.ui.entities.props.imagedisplay;
    exports de.amr.basics.ui.entities.props;
    exports de.amr.basics.ui.entities.props.clapperboard;
}