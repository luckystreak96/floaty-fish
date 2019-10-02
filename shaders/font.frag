#version 330 core

layout (location = 0) out vec4 color;

in DATA
{
	vec2 tc;
} fs_in;

uniform sampler2D tex;

void main()
{
	color = texture(tex, fs_in.tc);
	if (color.r <= 0.05 && color.g <= 0.05 && color.b <= 0.05){
		discard;
	}
}