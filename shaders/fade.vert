#version 330 core

in vec4 position;
uniform mat4 vw_matrix = mat4(1.0);
uniform mat4 pr_matrix;

out vec4 color;

void main()
{
	vec4 vertices[6] = vec4[6](	vec4( 1.0, -1.0, -0.5, 1.0),
								vec4(-1.0, -1.0, -0.5, 1.0),
								vec4( 1.0,  1.0, -0.5, 1.0),
								vec4( 1.0,  1.0, -0.5, 1.0),
								vec4(-1.0, -1.0, -0.5, 1.0),
								vec4(-1.0,  1.0, -0.5, 1.0));
	//gl_Position = position;
	gl_Position = pr_matrix * vw_matrix * position;
}