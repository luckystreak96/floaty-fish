#version 330 core

layout (location = 0) out vec4 color;

in DATA
{
	vec2 tc;
	vec3 position;
} fs_in;

uniform sampler2D tex;
uniform vec2 center;
uniform int darkMode;
uniform int randColor;
uniform float rand1;
uniform float rand2;
uniform float rand3;

void main()
{
	color = texture(tex, fs_in.tc);
	if (color.w < 0.3)//if the color is transparent, ignore that pixel
		discard;
	if(randColor == 1)
		if(color.r > 0.0 && color.r < 1.0 && color.r == color.g && color.g == color.b){
			color += vec4(rand1, rand2, rand3, 0.0);
		}
	if(darkMode == 1){
    	color *= 0.5 / abs(distance(fs_in.position.x + fs_in.position.y, center.x + center.y) + 1.5) + 0.1;//[amount of light, the fade effect, lowest value]
	}
	else if(darkMode == 2){
    	color *= 4.5 / abs(distance(fs_in.position.x + fs_in.position.y, center.x + center.y) + 1.5) + 0.1;//[amount of light, the fade effect, lowest value]
    	if(color.w > 0.1){
    	    color.w = 0.4;
    	}
	}
	else{
    	color *= 3.0 / abs(distance(fs_in.position.x + fs_in.position.y, center.x + center.y) + 3.2);//[amount of light, the fade effect, lowest value]
	}
	if(darkMode != 2 && color.w > 0.1){
	    color.w = 1;
	}
}