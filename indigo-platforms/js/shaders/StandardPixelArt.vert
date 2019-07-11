#version 300 es

// Attributes
layout (location = 0) in vec4 a_vertices;
layout (location = 1) in vec2 a_texcoord;
layout (location = 2) in vec2 a_translation;
layout (location = 3) in vec2 a_scale;
layout (location = 4) in vec4 a_tint;
layout (location = 5) in vec2 a_frameTranslation;
layout (location = 6) in vec2 a_frameScale;
layout (location = 7) in float a_rotation;
layout (location = 8) in float a_fliph;
layout (location = 9) in float a_flipv;

// Uniforms
layout (std140) uniform DisplayObjectUBO {
  mat4 u_projection;
};

// Varying
out vec2 v_texcoord;
out vec4 v_tint;

mat4 rotate2d(float angle){
    return mat4(cos(angle), -sin(angle), 0, 0,
                sin(angle), cos(angle), 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
                );
}

mat4 translate2d(vec2 t){
    return mat4(1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                t.x, t.y, 0, 1
                );
}

mat4 scale2d(vec2 s){
    return mat4(s.x, 0, 0, 0,
                0, s.y, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
                );
}

vec2 scaleTextCoords(){
  mat4 transform = translate2d(a_frameTranslation) * scale2d(a_frameScale);
  return (transform * vec4(a_texcoord.x, a_texcoord.y, 1, 1)).xy;
}

void main(void) {

  vec2 moveToTopLeft = a_scale / 2.0;

  mat4 transform = translate2d(moveToTopLeft + a_translation) * rotate2d(a_rotation) * scale2d(a_scale) * scale2d(vec2(a_fliph, a_flipv));

  gl_Position = u_projection * transform * a_vertices;

  // Pass the texcoord to the fragment shader.
  v_texcoord = scaleTextCoords();
  v_tint = a_tint;
}
