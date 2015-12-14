uniform float g_Time;
uniform sampler2D m_Color;
uniform sampler2D m_Noise;

varying vec2 texCoord;

void main() {
    
    vec2 d = texCoord;

    d.x += (-0.5 + cos(g_Time)) * 0.01;
    d.y += (-0.5 + sin(g_Time)) * 0.02;
            
    vec4 noise = texture2D(m_Noise, -d) + texture2D(m_Noise, d*2.0);
    

    vec2 d2 = texCoord;

    d2.x += noise.r;
    d2.y += noise.r;

    vec4 color = texture2D(m_Color, d2);
 
    gl_FragColor = color;
}