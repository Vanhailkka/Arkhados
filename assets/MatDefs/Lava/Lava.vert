uniform float g_Time;
uniform mat4 g_WorldViewProjectionMatrix;
attribute vec3 inPosition;

varying vec2 texCoord;
attribute vec2 inTexCoord;

void main() {
    texCoord = inTexCoord *1.0;
    vec3 inPos = inPosition;
    inPos.y += sin(1.0 * inPos.x + g_Time) * cos(1.0 * inPos.z + g_Time) * 1.0;
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPos, 1.0); 
}