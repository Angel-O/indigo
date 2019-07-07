#version 300 es

// Attributes
layout (location = 0) in vec4 a_vertices;
layout (location = 1) in vec2 a_texcoord;

// Uniforms
layout (std140) uniform DisplayObjectUBO {
  mat4 u_projection;
  vec2 u_translation;
  vec2 u_scale;
  vec4 u_tint;
  vec2 u_frameTranslation;
  vec2 u_frameScale;
  float u_rotation;
  float u_fliph;
  float u_flipv;
};

out vec2 v_texcoord;
out vec4 v_tint;

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
  mat4 transform = translate2d(u_frameTranslation) * scale2d(u_frameScale);
  return (transform * vec4(a_texcoord.x, a_texcoord.y, 1, 1)).xy;
}

void main(void) {

  vec2 moveToTopLeft = u_scale / 2.0;

  mat4 transform = translate2d(moveToTopLeft + u_translation) * scale2d(u_scale);

  gl_Position = u_projection * transform * a_vertices;

  v_texcoord = scaleTextCoords();
  v_tint = u_tint;
}
