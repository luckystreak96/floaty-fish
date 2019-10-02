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
vec2 tcs = fs_in.tc;

void main()
{
	if (top == 1)
		tcs.y = 1.0 - tcs.y;		

	color = texture(tex, tcs);
	if (color.w < 0.5)
		discard;
	if(darkMode == 1){
    	color *= 0.8 / (length(fish - fs_in.position.xy) + 1.5) + 0.1;
	    if(color.r >= 0.2){
	        color += 1;
	    }
	}
	else{
    	color *= 3.0 / (length(fish - fs_in.position.xy) + 1.5) + 0.1;
	    if(color.r >= 0.8){
	        color += 1;
	    }
	}
	if(color.w > 0.05) color.w = 1.0;
}


