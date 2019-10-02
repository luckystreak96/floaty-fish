#version 330 core

layout (location = 0) out vec4 color;

in DATA
{
	vec2 tc;
	vec3 position;
} fs_in;

uniform sampler2D tex;
uniform vec2 fish;
uniform int darkMode;

void main()
{
	color = texture(tex, fs_in.tc);
	
	if (color.w < 0.1)
		discard;
	color.b += 0.1;//add a blue-ish tint
	if(darkMode == 1){
    	color *= 0.5 / (length(fish - fs_in.position.xy) + 1.5) + 0.1;//[amount of light, the fade effect, lowest value]
	}
	else{
    	color *= 3.0 / (length(fish - fs_in.position.xy) + 1.5) + 0.1;//[amount of light, the fade effect, lowest value]
	}
	if(color.w > 0.0) color.w = 1.0;
}