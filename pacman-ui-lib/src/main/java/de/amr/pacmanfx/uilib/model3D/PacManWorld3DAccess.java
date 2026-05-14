package de.amr.pacmanfx.uilib.model3D;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.List;

public interface PacManWorld3DAccess {
    public static MeshView createMeshView(Mesh mesh, PhongMaterial material) {
        final MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(material);
        return meshView;
    }

    public static void centerOverOrigin(Node master, List<Node> slaves) {
        final Bounds b = master.getBoundsInLocal();
        final var centerOverOrigin = new Translate(-b.getCenterX(), -b.getCenterY(), -b.getCenterZ());
        master.getTransforms().add(centerOverOrigin);
        slaves.stream().map(Node::getTransforms).forEach(tf -> tf.add(centerOverOrigin));
    }

    public static <T extends Node> T resize(T body, float size) {
        final Bounds b = body.getBoundsInLocal();
        body.getTransforms().add(new Scale(size / b.getWidth(), size / b.getHeight(), size / b.getDepth()));
        return body;
    }

    /**
     * Rotates Pac-Man / the used ghost to fit into the 3D play scene.
     */
    public static <T extends Node> T fixShapeOrientation(T node) {
        node.getTransforms().add(new Rotate(270, Rotate.X_AXIS));
        return node;
    }
}
