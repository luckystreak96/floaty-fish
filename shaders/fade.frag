#version 330 core

layout (location = 0) out vec4 color;

uniform float time;

void main()
{
	if(time > 1.0)
		discard;
	color = vec4(0.2, 0.1f, 0.3f, 1.0f - time);
}