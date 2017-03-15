package org.vitrivr.cineast.core.util.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.util.LogHelper;

import java.io.IOException;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.03.17
 */
public class MeshParser extends DataURLParser {

    /** Some format specific constants. */
    private static final String MIME_TYPE = "application/3d-json";
    private static final String VERTICES_PROPERTY_NAME_THREEV4 = "position";
    private static final String NORMAL_PROPERTY_NAME_THREEV4 = "normal";
    private static final String ARRAY_PROPERTY_NAME_THREEV4 = "array";

    /**
     * Parses a Base64 encoded data url and treats it as Geometry JSON used by the Three.js JavaScript library.
     * Tries to parse the structure into a 3D mesh.
     *
     * @param dataUrl Data URL that should be parsed.
     * @return Mesh, if parsing fails that Mesh will be empty!
     */
    public static Mesh parseThreeJSV4Geometry(String dataUrl) {
		/* Convert Base64 string into byte array. */
        byte[] bytes = dataURLtoByteArray(dataUrl, MIME_TYPE);

        ObjectMapper mapper = new ObjectMapper();
        try {
            /* Read the JSON structure of the transmitted mesh data. */
            JsonNode node = mapper.readTree(bytes);
            JsonNode vertices = node.get(VERTICES_PROPERTY_NAME_THREEV4);
            if (vertices == null) {
                LOGGER.error("Submitted mesh does not contain a position array. Aborting...");
                return Mesh.EMPTY;
            }

            vertices = vertices.get(ARRAY_PROPERTY_NAME_THREEV4);
            if (vertices == null || !vertices.isArray() || vertices.size() == 0)  {
                LOGGER.error("Submitted mesh does not contain any vertices. Aborting...");
                return Mesh.EMPTY;
            }

            JsonNode normals = node.get(NORMAL_PROPERTY_NAME_THREEV4);
            if (normals == null) {
                LOGGER.error("Submitted mesh does not contain any normals. Aborting...");
                return Mesh.EMPTY;
            }

            normals = normals.get(ARRAY_PROPERTY_NAME_THREEV4);
            if (normals == null  || !normals.isArray() || normals.size() == 0) {
                LOGGER.error("Submitted mesh does not contain any normals. Aborting...");
                return Mesh.EMPTY;
            }

            /* Create new Mesh. */
            Mesh mesh = new Mesh(vertices.size()/3, vertices.size(),vertices.size());

            /* Add all the vertices and normals in the structure. */
            for (int i=0; i<(vertices.size()/3); i++) {
                int idx = 3*i;
                Vector3f vertex = new Vector3f((float)vertices.get(idx).asDouble(), (float)vertices.get(idx+1).asDouble(),(float)vertices.get(idx+2).asDouble());
                mesh.addVertex(vertex);
            }

            for (int i=0; i<(normals.size()/3); i++) {
                int idx = 3*i;
                Vector3f normal = new Vector3f((float)normals.get(idx).asDouble(), (float)normals.get(idx+1).asDouble(),(float)normals.get(idx+2).asDouble());
                mesh.addNormal(normal);
            }

            /* Add the faces to the mesh. */
            for (int i = 0; i<mesh.numberOfVertices()/3; i++) {
                int idx = 3*i;
                mesh.addFace(new Vector3i(idx+1, idx+2, idx+3), new Vector3i(idx+1, idx+2, idx+3));
            }

            return mesh;
        } catch (IOException e) {
            LOGGER.error("Could not create 3d mesh from Base64 input because the file-format is not supported. {}", LogHelper.getStackTrace(e));
            return Mesh.EMPTY;
        }
    }
}
