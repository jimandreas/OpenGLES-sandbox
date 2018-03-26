package com.jimandreas.opengl.objects

/*
 * needed methods
 */
class XYZ {


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
    // Vertex Shader from lesson 2
    //    does all the lighting in the vertex shader - fragment shader just passes through
    //  the color calculation
    //
    // A constant representing the combined model/view/projection matrix.
    // A constant representing the combined model/view matrix.
    // The position of the light in eye space.
    // Per-vertex position information we will pass in.
    // Per-vertex color information we will pass in.
    // Per-vertex normal information we will pass in.
    // This will be passed into the fragment shader.
    // The entry point for our vertex shader.
    // Transform the vertex into eye space.
    // Transform the normal's orientation into eye space.
    // Will be used for attenuation.
    // Get a lighting direction vector from the light to the vertex.
    // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
    // pointing in the same direction then it will get max illumination. *** was 0.1, now *** 0.6
    // Attenuate the light based on distance.
    //  + "   diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance )));  \n"
    // HACK: minimal level of diffuse for ambient
    // result - blew out the highlihts, didn't bring up the shadows as expected
    //   + "   diffuse = min(diffuse, 0.2);  \n"
    // Multiply the color by the illumination level. It will be interpolated across the triangle.
    // gl_Position is a special variable used to store the final position.
    // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
    val vertexShaderLesson2: String
        get() = ("uniform mat4 u_MVPMatrix;      \n"
                + "uniform mat4 u_MVMatrix;       \n"
                + "uniform vec3 u_LightPos;       \n"

                + "attribute vec4 a_Position;     \n"
                + "attribute vec4 a_Color;        \n"
                + "attribute vec3 a_Normal;       \n"

                + "varying vec4 v_Color;          \n"

                + "void main()                    \n"
                + "{                              \n"
                + "   vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);              \n"
                + "   vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));     \n"
                + "   float distance = length(u_LightPos - modelViewVertex);             \n"
                + "   vec3 lightVector = normalize(u_LightPos - modelViewVertex);        \n"
                + "   float diffuse = max(dot(modelViewNormal, lightVector), 0.6);       \n"
                + "   diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance /* * distance */)));  \n"
                + "   v_Color = a_Color * diffuse;                                       \n"
                + "   gl_Position = u_MVPMatrix * a_Position;                            \n"
                + "}                                                                     \n")

    // Set the default precision to medium. We don't need as high of a
    // precision in the fragment shader.
    // This is the color from the vertex shader interpolated across the
    // triangle per fragment.
    // The entry point for our fragment shader.
    // Pass the color directly through the pipeline.
    val fragmentShaderLesson2: String
        get() = ("precision mediump float;       \n"
                + "varying vec4 v_Color;          \n"
                + "void main()                    \n"
                + "{                              \n"
                + "   gl_FragColor = v_Color;     \n"
                + "}                              \n")

    // Define our per-pixel lighting shader.
    // A constant representing the combined model/view/projection matrix.
    // A constant representing the combined model/view matrix.
    // Per-vertex position information we will pass in.
    // Per-vertex color information we will pass in.
    // Per-vertex normal information we will pass in.
    // This will be passed into the fragment shader.
    // This will be passed into the fragment shader.
    // This will be passed into the fragment shader.
    // The entry point for our vertex shader.
    // Transform the vertex into eye space.
    // Pass through the color.
    // Transform the normal's orientation into eye space.
    // gl_Position is a special variable used to store the final position.
    // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
    val vertexShaderLesson3: String
        get() = ("uniform mat4 u_MVPMatrix;      \n"
                + "uniform mat4 u_MVMatrix;       \n"

                + "attribute vec4 a_Position;     \n"
                + "attribute vec4 a_Color;        \n"
                + "attribute vec3 a_Normal;       \n"

                + "varying vec3 v_Position;       \n"
                + "varying vec4 v_Color;          \n"
                + "varying vec3 v_Normal;         \n"
                + "void main()                                                \n"
                + "{                                                          \n"
                + "   v_Position = vec3(u_MVMatrix * a_Position);             \n"
                + "   v_Color = a_Color;                                      \n"
                + "   v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));      \n"
                + "   gl_Position = u_MVPMatrix * a_Position;                 \n"
                + "}                                                          \n")

    // Set the default precision to medium. We don't need as high of a
    // precision in the fragment shader.
    // The position of the light in eye space.
    // Interpolated position for this fragment.
    // This is the color from the vertex shader interpolated across the
    // triangle per fragment.
    // Interpolated normal for this fragment.
    // The entry point for our fragment shader.
    // Will be used for attenuation.
    // Get a lighting direction vector from the light to the vertex.
    // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
    // pointing in the same direction then it will get max illumination.  *** was 0.1, now *** 0.6
    // Add attenuation.
    // + "   diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance /* * distance */)));  \n"
    // Multiply the color by the diffuse illumination level to get final output color.
    val fragmentShaderLesson3: String
        get() = ("precision mediump float;       \n"
                + "uniform vec3 u_LightPos;       \n"

                + "varying vec3 v_Position;		\n"
                + "varying vec4 v_Color;          \n"
                + "varying vec3 v_Normal;         \n"
                + "void main()                    \n"
                + "{                              \n"
                + "   float distance = length(u_LightPos - v_Position);                  \n"
                + "   vec3 lightVector = normalize(u_LightPos - v_Position);             \n"
                + "   float diffuse = max(dot(v_Normal, lightVector), 0.1);              \n"
                + "   gl_FragColor = v_Color * diffuse;                                  \n"
                + "}                                                                     \n")

    companion object {

        private val U = FloatArray(3)
        private val V = FloatArray(3)
        private val N = FloatArray(3)
        private val T = FloatArray(3)

        // https://www.opengl.org/wiki/Calculating_a_Surface_Normal
        fun getNormal(p1: FloatArray, p2: FloatArray, p3: FloatArray): FloatArray {
            U[0] = p2[0] - p1[0]
            U[1] = p2[1] - p1[1]
            U[2] = p2[2] - p1[2]

            V[0] = p3[0] - p1[0]
            V[1] = p3[1] - p1[1]
            V[2] = p3[2] - p1[2]

            N[0] = U[1] * V[2] - U[2] * V[1]
            N[1] = U[2] * V[0] - U[0] * V[2]
            N[2] = U[0] * V[1] - U[1] * V[0]
            return normalize(N)
        }

        private fun normalize(p1: FloatArray): FloatArray {
            val mag = Math.sqrt(
                    (p1[0] * p1[0] +
                            p1[1] * p1[1] +
                            p1[2] * p1[2]).toDouble()).toFloat()
            T[0] = p1[0] / mag
            T[1] = p1[1] / mag
            T[2] = p1[2] / mag
            return T
        }
    }

}