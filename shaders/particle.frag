#version 330 core

layout (location = 0) out vec4 color;

in DATA
{
	vec2 tc;
	vec3 position;
} fs_in;

uniform vec2 fish;
uniform int darkMode;
uniform sampler2D tex;
uniform int top;
uniform float lifeTime;
uniform vec4 partColor;//the color modifier
vec2 tcs = fs_in.tc;

void main()
{
	color = texture(tex, tcs);
	if(color.w < 0.4)
	    discard;
	color.r -= partColor.r;
	color.g -= partColor.g;
	color.b -= partColor.b;
	if(darkMode == 1){
	    color.r *= 0.5 / ((length(fish - fs_in.position.xy) + 1.5) + 0.1);
	    color.g *= 0.5 / ((length(fish - fs_in.position.xy) + 1.5) + 0.1);
	    color.b *= 0.5 / ((length(fish - fs_in.position.xy) + 1.5) + 0.1);
	}
	else{
	    color.r *= 3.0 / ((length(fish - fs_in.position.xy) + 1.5) + 0.1);
	    color.g *= 3.0 / ((length(fish - fs_in.position.xy) + 1.5) + 0.1);
	    color.b *= 3.0 / ((length(fish - fs_in.position.xy) + 1.5) + 0.1);
	}
	if(color.w > 1.4)
	    color.w = lifeTime;
}
