# OuterMilds

A simple solar system rendered in real-time using modern OpenGL (GL3+) and Java (JOGL). This project is a step-by-step exploration of modern graphics programming concepts from the ground up.

## Current Status: "Hello, Triangle!"

The project currently renders a single, hard-coded orange triangle. This establishes the complete foundational pipeline for modern OpenGL rendering.

## Key Concepts Implemented

The current version (V2.java / OuterMilds.java) successfully implements:

- **JOGL Windowing:** A JFrame window with a GLCanvas attached.

- **Render Loop:** A main display() loop driven by an Animator.

- **VBO (Vertex Buffer Object):** Vertex data (the triangle's 3 points) is allocated on the GPU's VRAM using glGenBuffers and glBufferData.

- **GLSL Shaders:** A minimal Vertex Shader (for positioning) and Fragment Shader (for coloring) are defined and compiled at runtime.

- **Shader Program:** The two shaders are linked into a single, executable GPU program (shaderProgramID).

- **Robust Error Handling:** The shader compileShader and linkProgram helpers now include full error checking, using ByteBuffer to correctly retrieve info logs from the native driver.

- **VAO (Vertex Array Object):** A "blueprint" (vaoID) is set up to store the vertex data layout, linking the VBO to the vertex shader's inputs via glVertexAttribPointer.

- **Resource Management:** All GPU resources (VBO, VAO, ShaderProgram) are properly cleaned up in the dispose() method to prevent memory leaks.

## How to Build and Run

This project uses JOGL, which is not part of the standard Java library.

### 1. Dependencies
  
   You must have the JOGL JAR files. Download the following from the [JogAmp Download Page](https://jogamp.org/jogl/www/):

   - jogl-all.jar
   
   - gluegen-rt.jar
   
   Place these .jar files in the root of the project directory.
   
### 2. Compile
  
   You must compile with the JOGL libraries in your classpath.

   **On Windows:**
   
     #(Assuming V2.java is the latest file)
   
     javac -cp ".;jogl-all.jar;gluegen-rt.jar" V2.java
   
   **On macOS/Linux:**
   ```
     #(Note the colon ':' instead of ';')
   
     javac -cp ".:jogl-all.jar;gluegen-rt.jar" V2.java
   ```
### 3. Run
   
   You must also run with the JOGL libraries in your classpath.

   **On Windows:**
   ```
   #(Note: Use the class name 'V2', not 'V2.java')
   
   java -cp ".;jogl-all.jar;gluegen-rt.jar" V2
   ```
   **On macOS/Linux:**
   ```
   java -cp ".:jogl-all.jar:gluegen-rt.jar" V2
   ```
   
## Next Steps

With the core 2D pipeline in place, the next stage is to move into 3D:

- [ ] Introduce Model, View, and Projection (MVP) matrices to create a 3D perspective.

- [ ] Pass the matrices to the vertex shader as Uniforms.

- [ ] Create a simple Camera class to move around the scene.

- [ ] Draw a 3D cube instead of a 2D triangle.
