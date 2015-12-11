uniform float g_Time;
uniform mat4 g_WorldViewProjectionMatrix;
attribute vec3 inPosition;

varying vec2 texCoord;
attribute vec2 inTexCoord;

void main() {
    texCoord = inTexCoord;
    vec3 inPos = inPosition;
    inPos.y += inPosition.y * -0.5 + sin(g_Time+inPosition.y) * inPosition.y; 
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPos, 1.0); 
}