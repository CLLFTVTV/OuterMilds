import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * OuterMilds.java
 *
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
		
		// Set the "clear" color. This is the color the screen will be wiped to at the start of every frame.
		// A nice dark space blue/black.
		gl.glClearColor(0.0f, 0.0f, 0.1f, 1.0f);
		
		System.out.println("JOGL: init() called. OpenGL Version: " + gl.glGetString(GL3.GL_VERSION));
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
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}