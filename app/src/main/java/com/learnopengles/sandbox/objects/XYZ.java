package com.learnopengles.sandbox.objects;

/*
 * needed methods
 */
public class XYZ {

    private static float[] U = new float[3];
    private static float[] V = new float[3];
    private static float[] N = new float[3];
    private static float[] T = new float[3];

    public XYZ() {}

    /*
     * note:  NOT thread safe!
     */
    // https://www.opengl.org/wiki/Calculating_a_Surface_Normal
    public static float[] getNormal(float[] p1, float[] p2, float[] p3) {
        U[0] = p2[0] - p1[0];
        U[1] = p2[1] - p1[1];
        U[2] = p2[2] - p1[2];

        V[0] = p3[0] - p1[0];
        V[1] = p3[1] - p1[1];
        V[2] = p3[2] - p1[2];

        N[0] = U[1] * V[2] - U[2] * V[1];
        N[1] = U[2] * V[0] - U[0] * V[2];
        N[2] = U[0] * V[1] - U[1] * V[0];
        return normalize(N);
    }

    public static float[] normalize(float[] p1) {
        float mag = (float) (Math.sqrt(
                p1[0] * p1[0] +
                        p1[1] * p1[1] +
                        p1[2] * p1[2]));
        T[0] = p1[0] / mag;
        T[1] = p1[1] / mag;
        T[2] = p1[2] / mag;
        return T;
    }


/*
Reference:
https://www.opengl.org/wiki/Calculating_a_Surface_Normal

Algorithm
A surface normal for a triangle can be calculated by taking the vector cross product of two edges of that triangle. The order of the vertices used in the calculation will affect the direction of the normal (in or out of the face w.r.t. winding).

So for a triangle p1, p2, p3, if the vector U = p2 - p1 and the vector V = p3 - p1 then the normal N = U X V and can be calculated by:

Nx = UyVz - UzVy

Ny = UzVx - UxVz

Nz = UxVy - UyVx

Pseudo-code
Given that a vector is a structure composed of three floating point numbers and a Triangle is a structure composed of three Vectors, based on the above definitions:

Begin Function CalculateSurfaceNormal (Input Triangle) Returns Vector

	Set Vector U to (Triangle.p2 minus Triangle.p1)
	Set Vector V to (Triangle.p3 minus Triangle.p1)

	Set Normal.x to (multiply U.y by V.z) minus (multiply U.z by V.y)
	Set Normal.y to (multiply U.z by V.x) minus (multiply U.x by V.z)
	Set Normal.z to (multiply U.x by V.y) minus (multiply U.y by V.x)

	Returning Normal

End Function
*/
    
    
    /*
     * Shaders
     */

    /*
     * note - I remove the square function on the illumination fall-off
     * Things were just too dark.   Look for the following term commented out.  - jim a
     *  "* distance"
     *  Also upped the diffuse to 0.6 as a base.
     */
    public String getVertexShaderLesson2() {
        // Vertex Shader from lesson 2
        //    does all the lighting in the vertex shader - fragment shader just passes through
        //  the color calculation
        // TODO: Explain why we normalize the vectors, explain some of the vector math behind it all. Explain what is eye space.
        final String vertexShaderAllTheCalcsAreHere =
                "uniform mat4 u_MVPMatrix;      \n"        // A constant representing the combined model/view/projection matrix.
                        + "uniform mat4 u_MVMatrix;       \n"        // A constant representing the combined model/view matrix.
                        + "uniform vec3 u_LightPos;       \n"        // The position of the light in eye space.

                        + "attribute vec4 a_Position;     \n"        // Per-vertex position information we will pass in.
                        + "attribute vec4 a_Color;        \n"        // Per-vertex color information we will pass in.
                        + "attribute vec3 a_Normal;       \n"        // Per-vertex normal information we will pass in.

                        + "varying vec4 v_Color;          \n"        // This will be passed into the fragment shader.

                        + "void main()                    \n"    // The entry point for our vertex shader.
                        + "{                              \n"
                        // Transform the vertex into eye space.
                        + "   vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);              \n"
                        // Transform the normal's orientation into eye space.
                        + "   vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));     \n"
                        // Will be used for attenuation.
                        + "   float distance = length(u_LightPos - modelViewVertex);             \n"
                        // Get a lighting direction vector from the light to the vertex.
                        + "   vec3 lightVector = normalize(u_LightPos - modelViewVertex);        \n"
                        // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
                        // pointing in the same direction then it will get max illumination. *** was 0.1, now *** 0.6
                        + "   float diffuse = max(dot(modelViewNormal, lightVector), 0.6);       \n"
                        // Attenuate the light based on distance.
                        //  + "   diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance )));  \n"
                        + "   diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance /* * distance */)));  \n"
                        // HACK: minimal level of diffuse for ambient
                        // result - blew out the highlihts, didn't bring up the shadows as expected
                        //   + "   diffuse = min(diffuse, 0.2);  \n"
                        // Multiply the color by the illumination level. It will be interpolated across the triangle.
                        + "   v_Color = a_Color * diffuse;                                       \n"
                        // gl_Position is a special variable used to store the final position.
                        // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
                        + "   gl_Position = u_MVPMatrix * a_Position;                            \n"
                        + "}                                                                     \n";

        return vertexShaderAllTheCalcsAreHere;
    }

    public String getFragmentShaderLesson2() {
        final String fragmentShaderJustPassesThroughColor =
                "precision mediump float;       \n"        // Set the default precision to medium. We don't need as high of a
                        // precision in the fragment shader.
                        + "varying vec4 v_Color;          \n"        // This is the color from the vertex shader interpolated across the
                        // triangle per fragment.
                        + "void main()                    \n"        // The entry point for our fragment shader.
                        + "{                              \n"
                        + "   gl_FragColor = v_Color;     \n"        // Pass the color directly through the pipeline.
                        + "}                              \n";

        return fragmentShaderJustPassesThroughColor;
    }

    public String getVertexShaderLesson3() {
        // Define our per-pixel lighting shader.
        final String perPixelVertexShader =
                "uniform mat4 u_MVPMatrix;      \n"        // A constant representing the combined model/view/projection matrix.
                        + "uniform mat4 u_MVMatrix;       \n"        // A constant representing the combined model/view matrix.

                        + "attribute vec4 a_Position;     \n"        // Per-vertex position information we will pass in.
                        + "attribute vec4 a_Color;        \n"        // Per-vertex color information we will pass in.
                        + "attribute vec3 a_Normal;       \n"        // Per-vertex normal information we will pass in.

                        + "varying vec3 v_Position;       \n"        // This will be passed into the fragment shader.
                        + "varying vec4 v_Color;          \n"        // This will be passed into the fragment shader.
                        + "varying vec3 v_Normal;         \n"        // This will be passed into the fragment shader.

                        // The entry point for our vertex shader.
                        + "void main()                                                \n"
                        + "{                                                          \n"
                        // Transform the vertex into eye space.
                        + "   v_Position = vec3(u_MVMatrix * a_Position);             \n"
                        // Pass through the color.
                        + "   v_Color = a_Color;                                      \n"
                        // Transform the normal's orientation into eye space.
                        + "   v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));      \n"
                        // gl_Position is a special variable used to store the final position.
                        // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
                        + "   gl_Position = u_MVPMatrix * a_Position;                 \n"
                        + "}                                                          \n";

        return perPixelVertexShader;
    }

    public String getFragmentShaderLesson3() {
        final String perPixelFragmentShader =
                "precision mediump float;       \n"        // Set the default precision to medium. We don't need as high of a
                        // precision in the fragment shader.
                        + "uniform vec3 u_LightPos;       \n"        // The position of the light in eye space.

                        + "varying vec3 v_Position;		\n"        // Interpolated position for this fragment.
                        + "varying vec4 v_Color;          \n"        // This is the color from the vertex shader interpolated across the
                        // triangle per fragment.
                        + "varying vec3 v_Normal;         \n"        // Interpolated normal for this fragment.

                        // The entry point for our fragment shader.
                        + "void main()                    \n"
                        + "{                              \n"
                        // Will be used for attenuation.
                        + "   float distance = length(u_LightPos - v_Position);                  \n"
                        // Get a lighting direction vector from the light to the vertex.
                        + "   vec3 lightVector = normalize(u_LightPos - v_Position);             \n"
                        // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
                        // pointing in the same direction then it will get max illumination.  *** was 0.1, now *** 0.6
                        + "   float diffuse = max(dot(v_Normal, lightVector), 0.1);              \n"
                        // Add attenuation.
                        // + "   diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance /* * distance */)));  \n"
                        // Multiply the color by the diffuse illumination level to get final output color.
                        + "   gl_FragColor = v_Color * diffuse;                                  \n"
                        + "}                                                                     \n";

        return perPixelFragmentShader;
    }

}