import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.glsl.sdk.CompileShader;
import com.jogamp.common.nio.Buffers; // New import for Buffers

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.FloatBuffer; // New import for FloatBuffer
import java.nio.IntBuffer; // New import for IntBuffer
import java.nio.ByteBuffer;

/**
 * OuterMilds.java
 *
 * V2 11/12/2025:
 * Draws a single 3d triangle. This requires setting up:
 * 	1. Vertex Data (the vertices of the triangle)
 * 	2. VBO: To send the data to the GPU memory
 * 	3. VAO: To link the VBOs
 * 	4. Shaders (Vertex and Fragment): Code that runs on the GPU
 * 	5. A Shader Program: The linked shaders, ready to be used
 *
 *
 * V1 11/11/2025:
 * The main entry point for my solar system simulation.
 * This class sets up the AWT/Swing window (JFrame) and
 * initializes the JOGL GLCanvas, which is the "drawing surface".
 * It implements GLEventListener to handle the core OpenGL events.
 *
 * @author CLLFTVTV
 */
public class OuterMilds implements GLEventListener{
	
	
	//Set the dimensions of our display window
	private static final int WINDOW_WIDTH = 1280;
	private static final int WINDOW_HEIGHT = 720;
	
	
	// We need to store the names (which are just integers) of the objects we create on the GPU
	private int shaderProgramID;
	private int vaoID;
	private int vboID;
	
	
	//Main method. program starts here. set up OpenGL profile, window and animation loop
	public static void main(String[] args) {
		
		//1. Get the default OpenGL profile.
		GLProfile glp = GLProfile.get(GLProfile.GL3);
		
		
		//2. Define the capabilities we need from the profile.
		//Stuff like anti-aliasing, stencil buffers, etc.
		//For now, the default capabilities are fine.
		GLCapabilities caps = new GLCapabilities(glp);
		caps.setHardwareAccelerated(true); // Ensure the GPU is being used
		caps.setDoubleBuffered(true); // Standard for smooth animation
		
		
		//3. Create the JOGL canvas. This is the drawing surface, inside the JFrame, which is the OS window.
		GLCanvas canvas = new GLCanvas(caps); // Create a new canvas
		canvas.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT)); // Give it the predefined width and height
		
		
		//4. Create an instance of the OuterMilds class
		//This class is the listener that will listen to the OpenGL events
		OuterMilds outerMilds = new OuterMilds(); // Create a new instance of the class
		canvas.addGLEventListener(outerMilds); // add that class as an event listener, on the canvas
		
		
		//5. Create the Swing window (JFrame) to hold the canvas.
		final JFrame frame = new JFrame("Outer Milds: Echoes of the I");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Tell the frame to do nothing on close. will handle that myself
		frame.getContentPane().add(canvas); // Add the canvas to the window
		frame.setResizable(false);
		frame.pack(); // Sizes the window to the preferred size of the components
		frame.setLocationRelativeTo(null); // Center the window
		
		
		//6. Create an Animator
		//This is a JOGL utility that creates a dedicated thread
		// to call our display method continuously.
		final Animator animator = new Animator(canvas);
		
		
		//7. Add a window listener to handle closing the application.
		//This is important for cleanly shutting down the animator's thread
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// Use a new thread to avoid a potential deadlock
				new Thread(() -> {
					if (animator.isStarted()) {
						System.out.println("Stopping Animator...");
						animator.stop(); // Stop the render loop
					}
					System.out.println("Disposing Window...");
					frame.dispose(); // Close the window
					System.out.println("Exiting...");
					System.exit(0); // Shut down the Java Virtual Machine
				}).start();
			}
		});
		
		
		//8. Finally, make the window visible and start the animation.
		frame.setVisible(true);
		animator.start();
		System.out.println("Animator started.");
		
	}
	// main function ends here.
	
	
	// Called once by the JOGL framework when the canvas is first created.
	// This is where we do our one time setup (like loading shaders, models etc.) and set inital OpenGL state
	@Override
	public void init(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3(); // The gl object becomes our main channel to the GPU. we call methods on this object.
		
		System.out.println("JOGL: init() called. OpenGL Version: " + gl.glGetString(GL3.GL_VERSION));
		
		// Set the "clear" color. This is the color the screen will be wiped to at the start of every frame.
		// A nice dark space blue/black.
		gl.glClearColor(0.0f, 0.0f, 0.1f, 1.0f);
		
		
		// Define the vertices of the triangle then setup VAO and VBO and send to GPU
		float [] vertices = {
				0.0f,  0.5f, 0.0f, // Top vertex
	           -0.5f, -0.5f, 0.0f, // Bottom-left vertex
	            0.5f, -0.5f, 0.0f  // Bottom-right vertex
		};
		
		// Create VBO
		// VBOs are buffers on the GPU that store vertex data
		
		// We need an IntBuffer to hold the ID of the VBO:
		// Create a new IntBuffer object, called vboBuffers.
		// Buffers.newDirectIntBuffer(1), create "1" new IntBuffer on Native Memory (RAM).
		// Direct means its on direct memory that C++ (OpenGL) can access, without Java
		// The C++ OpenGL driver can't access the JVM space so we use the unsafe buffer for communication
		IntBuffer vboBuffers = Buffers.newDirectIntBuffer(1);
		// Tell OpenGL(gl.GenBuffers), we need one, 1, spot on your list.
		// It writes that spot in the unsafe buffer
		gl.glGenBuffers(1, vboBuffers);
		// We copy that value from the unsafe buffer into out JVM space vboID variable
		vboID = vboBuffers.get(0); // Get the generated ID
		
		// "Bind" (haha hollow knight silksong i miss that game, times were good then)
		// this current buffer as the active one.
		// vboBuffers has the memory address of our unsafe buffer.
		// vboID has the ID of our place on OpenGL's list, which it got from the unsafe buffer.
		// OpenGL (the gl object) is a "state machine." It has a set of global "slots" (like GL_ARRAY_BUFFER, GL_TEXTURE_2D, etc.).
		// We put the vboID, our thing on the list, and put it in the GL_ARRAY_BUFFER slot
		// OpenGL driver has slots, we're saying that the GL_ARRAY_BUFFER slot should be
		// whatever is at number vboID on your list. We'll give it our vertex data later.
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboID);
		
		// Convert our Java float[], for vertex data, to a FloatBuffer type for JOGL
		FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(vertices);
		
		// Send the data to the GPU
		// GL_STATIC_DRAW is a hint that we won't change this data often (helps opengl make
		// better use of it)
		// OpenGL has a list for each type of thing and "arms". We told it above to hold the vboID from the vertex buffer list,
		// and hold it in it's GL_ARRAY_BUFFER arm. opengl uses that arm for vertex data.
		// we wrote a name on it's vertex buffer list above, here we allocate our vertex data's worth of
		// GPU memory to our name on the vertex buffer list. then we reference that name on the list
		// on it's vertex data arm
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, 
				vertexBuffer.limit() * Buffers.SIZEOF_FLOAT, // Total size in bytes
				vertexBuffer, 
				GL3.GL_STATIC_DRAW);
		
		
		// Create a VAO. VAO stores how our VBOs are laid out.
		// Same as before but this time get a name on the VAO list
		IntBuffer vaoBuffers = Buffers.newDirectIntBuffer(1);
		gl.glGenVertexArrays(1, vaoBuffers);
		vaoID = vaoBuffers.get(0);
		
		// Bind VAO
		// Diff command cuz buffer is generic object, vao is special object
		gl.glBindVertexArray(vaoID);
		
		// Bind the VBO again. This "associates" the VAO with the VBO
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboID);
		
		// 5. Define the "layout" of our vertex data (the "Vertex Attributes")
        // "Hey, OpenGL, our data for attribute '0' (layout = 0 in the shader):
        //   - has 3 components (x, y, z)
        //   - is of type GL_FLOAT
        //   - is NOT normalized
        //   - has a 'stride' (gap) of 0 bytes between vertices
        //   - starts at an 'offset' of 0 bytes in the buffer"
		// This lines tells the VAO how to read the VBO
        gl.glVertexAttribPointer(0, 3, GL3.GL_FLOAT, false, 0, 0);
        
        // Enable this attribute
        gl.glEnableVertexAttribArray(0);
        
        // Unbind the VAO to be safe.
        gl.glBindVertexArray(0);
        
        
        // Create the shaders
        // Shaders are small programs written in GLSL (OpenGL Shading Language)
        // These programs run ON the GPU. Like physically on top of the hardware.
        
        
        // The Vertex Shaders runs once for each vertex.
        // It's job is to set the final position of our vertices on the screen.
        String vertexShaderSource = 
        		"#version 330 core\n" + 
        		"layout (location = 0) in vec3 aPos;\n" + // "layout = 0" matches our glVertexAttribPointer(0, ...)
        		"void main() {\n" + 
        		"	gl_Position = vec4(aPos, 1.0);\n" + // Set the final position
        		"}\n";
        
        // The Fragment Shader runs for every pixel inside the triangle
        // Which pixels are inside the triangle? well, it gets that information from the non-programmable
        // Rasterizer stage that comes right before the fragment shader
        // It's job is to determine the final color of that pixel.
        String fragmentShaderSource = 
        		"#version 330 core\n" + 
        		"out vec4 FragColor;\n" + // The final color output
        		"void main() {\n" + 
        		"	FragColor = vec4(1.0, 0.5, 0.2, 1.0);\n" + // A nice "Outer Milds" orange
        		"}\n";
		
        
        // Compile (into machine code) and Link the Shaders into a combined "Shader Program"
        // The compileShader helper function is defined later
        int vertexShader = compileShader(gl, GL3.GL_VERTEX_SHADER, vertexShaderSource);
        int fragmentShader = compileShader(gl, GL3.GL_FRAGMENT_SHADER, fragmentShaderSource);
        
        // The linkProgram helper function is defined later
        shaderProgramID = linkProgram(gl, vertexShader, fragmentShader);
        
        // We can delete the individual shaders, now that they're linked
        gl.glDeleteShader(vertexShader);
        gl.glDeleteShader(fragmentShader);
        
		System.out.println("JOGL: init() complete");
	}
	
	
	// Helper method to compile a shader.
	// Includes error checking
	
	private int compileShader(GL3 gl, int type, String source) {
		int shaderID = gl.glCreateShader(type);
		gl.glShaderSource(shaderID, 1, new String[]{source}, null);
		gl.glCompileShader(shaderID);
		
		// Error Checking
		IntBuffer compileStatus = Buffers.newDirectIntBuffer(1);
		gl.glGetShaderiv(shaderID, GL3.GL_COMPILE_STATUS, compileStatus);
		
		if (compileStatus.get(0) == GL3.GL_FALSE) {
            IntBuffer logLengthBuffer = Buffers.newDirectIntBuffer(1); 
            gl.glGetShaderiv(shaderID, GL3.GL_INFO_LOG_LENGTH, logLengthBuffer);
            int logLength = logLengthBuffer.get(0); 

            // 1. Allocate a direct ByteBuffer (native memory)
            ByteBuffer log = Buffers.newDirectByteBuffer(logLength);
            
            // 2. Pass the ByteBuffer (not a byte[]) to OpenGL
            gl.glGetShaderInfoLog(shaderID, logLength, null, log); 

            // 3. Copy the data from the ByteBuffer into a byte[] for printing
            byte[] logBytes = new byte[logLength];
            log.get(logBytes); 
            // --- END OF FIX ---

            System.err.println("!!! SHADER COMPILE ERROR !!!");
            System.err.println(new String(logBytes)); // Print from the new byte[]
            System.exit(1);
        }
		// End Error Checking
		
		return shaderID;
	}
	
	
	// Helper method to link the shaders in a single program
	// Also includes error checking
	private int linkProgram(GL3 gl, int vertexShader, int fragmentShader) {
        int programID = gl.glCreateProgram();
        gl.glAttachShader(programID, vertexShader);
        gl.glAttachShader(programID, fragmentShader);
        gl.glLinkProgram(programID);

        // Error Checking
        IntBuffer linkStatus = Buffers.newDirectIntBuffer(1);
        gl.glGetProgramiv(programID, GL3.GL_LINK_STATUS, linkStatus);

        if (linkStatus.get(0) == GL3.GL_FALSE) {
            IntBuffer logLengthBuffer = Buffers.newDirectIntBuffer(1);
            gl.glGetProgramiv(programID, GL3.GL_INFO_LOG_LENGTH, logLengthBuffer);
            int logLength = logLengthBuffer.get(0);

            // Allocate a direct ByteBuffer (native memory) to receive the log
            ByteBuffer log = Buffers.newDirectByteBuffer(logLength);
            gl.glGetProgramInfoLog(programID, logLength, null, log);

            // Create a byte[] (JVM memory) to copy the log into for printing
            byte[] logBytes = new byte[logLength];
            log.get(logBytes); // Copy from the ByteBuffer to the byte[]

            System.err.println("Shader Link Error!");
            System.err.println(new String(logBytes)); // Print from the byte[]
            System.exit(1);
        }
        // Error Checking Complete

        return programID;
    }
	
	
	// Called by the JOGL framework for every frame,
	// This is our main RENDER LOOP. All drawing code goes inside this one.
	@Override
	public void display(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3(); // The gl object becomes our main channel to the GPU. we call methods on this object.
		
		// Clear the color buffer and depth buffer.
		// This wipes the canvas clean to our glClearColor (from init).
		
		// The Color Buffer is a 2D block of memory. It's a grid of pixels. Each 'cell' in this grid holds a color value.
		// This is what you actually see on the screen.
		
		// The Depth Buffer (or Z Buffer) is another 2D block of memory, with the exact same dimensions as the Color Buffer.
		// But instead of storing the colors, each 'cell' in this grid stores a single number usually between 0.0 and 1.0)
		// This represents the depth of that pixel. Or the distance of the pixel from the camera.
		
		// These buffers are stored in the VRAM
		
		// The BIT just represents an ID for the buffer, not the actual buffer itself.
		
		// GL_DEPTH_BUFFER_BIT is important for 3D, to make sure objects in front occlude objects behind.
		gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT); // Wipe the Color and Depth Buffers
		
		// The Solar System drawing code will go here
		
		// Tell OpenGL which shader program to use
		gl.glUseProgram(shaderProgramID);
		
		// Tell OpenGL which VAO to use
		gl.glBindVertexArray(vaoID);
		
		// Draw!
		// Draw 3 vertices starting from index 0
		// and interpret them as GL_TRIANGLES
		gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 3);
		
		// Unbind the VAO, good practice
		gl.glBindVertexArray(0);
		
		
		// Not necessary with Animator and Double Buffering,
		// but it ensures all buffered OpenGL commands are sent to the GPU
		gl.glFlush();
	}
	
	// Reshape is called by the JOGL framework when the window is resized.
	// This is where we update our viewport and projection matrix.
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL3 gl = drawable.getGL().getGL3();
		
		// Set the GL viewport to cover the new window size.
        // This maps OpenGL's -1 to 1 coordinates to the window's pixel coordinates.
		if (height <= 0) height = 1; // Avoid division by zero
		gl.glViewport(0, 0, width, height);
		
	    // Later, we will update our camera's projection matrix here.
        // e.g.,
        // projectionMatrix.setPerspective(45.0f, (float)width / (float)height, 0.1f, 1000.0f);
        System.out.println("JOGL: reshape() called. New dimensions: " + width + "x" + height);
	}
	
	
	// Dispose is called by the JOGL framework when the canvas is about to be destroyed.
	// This is where we should clean up any OpenGL resources (buffers, shaders, etc.).
	@Override
	public void dispose(GLAutoDrawable drawable) {
		System.out.println("JOGL: dispose() called. Cleaning up.");
		// We'll add resource cleaning here as we go along
		GL3 gl = drawable.getGL().getGL3();
		
		// Delete the Shader Program
		gl.glDeleteProgram(shaderProgramID);
		
		// Delete the VBO
		IntBuffer vboBuf = Buffers.newDirectIntBuffer(1);
		vboBuf.put(vboID);
		vboBuf.rewind(); // rewind the buffer to the start
		gl.glDeleteBuffers(1, vboBuf);
		
		// Delete the VBO
		IntBuffer vaoBuf = Buffers.newDirectIntBuffer(1);
		vaoBuf.put(vaoID);
		vaoBuf.rewind(); // rewind the buffer to the start
		gl.glDeleteVertexArrays(1, vboBuf);
		
		System.out.println("JOGL: dispose() finished.");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}