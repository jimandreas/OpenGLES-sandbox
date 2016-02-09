package com.learnopengles.sandbox.objects;

/*
 * needed methods
 */
public class XYZ {

    private static float[] U = new float[3];
    private static float[] V = new float[3];
    private static float[] N = new float[3];
    private static float[] T = new float[3];
    private XYZ() {
        // no instances
    }

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

        N[0] = U[1]*V[2] - U[2]*V[1];
        N[1] = U[2]*V[0] - U[0]*V[2];
        N[2] = U[0]*V[1] - U[1]*V[0];
        return normalize(N);
    }
    public static float[] normalize(float[] p1) {
        float mag = (float)(Math.sqrt(
                p1[0]*p1[0] +
                p1[1]*p1[1] +
                p1[2]*p1[2] ));
        T[0] = p1[0] / mag;
        T[1] = p1[1] / mag;
        T[2] = p1[2] / mag;
        return T;
    }
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