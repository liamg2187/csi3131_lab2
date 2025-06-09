import javax.swing.*;     
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

/*-------------------------------------------------
 * Set up a simple frame for displaying
 * MBCanvas object
 * ------------------------------------------------*/
public class MBFrame extends JFrame
{
   private MBCanvas cv;
   private Timer renderTimer;
   private static final int RENDER_INTERVAL = 10000; // 10 seconds in milliseconds

   public MBFrame(double ucx, double ucy,  // Upper left hand corner (real/imag)
		  double bxdim, // Size of the box (real values)
		  int pixeldim, // Size of box in terms of dimensions of pixels
		  int minBxSize)  // minimum size of square to start filling
   {
        this(ucx, ucy, bxdim, pixeldim, minBxSize, 20); // Default thread pool size of 20
   }

   public MBFrame(double ucx, double ucy,  // Upper left hand corner (real/imag)
		  double bxdim, // Size of the box (real values)
		  int pixeldim, // Size of box in terms of dimensions of pixels
		  int minBxSize,  // minimum size of square to start filling
		  int threadPoolSize) // size of thread pool
   {
        setTitle("Mandlebrot Diagram");
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MBGlobals mg = new MBGlobals(ucx,ucy,bxdim/pixeldim,pixeldim,minBxSize,threadPoolSize);
	cv = new MBCanvas(mg);
	add(cv);
	pack();
        setResizable(false);
	setVisible(true);

        // Set up timer for auto-rerendering
        setupRenderTimer();
   }

   private void setupRenderTimer() {
        // Calculate delay until next 10-second mark
        Calendar now = Calendar.getInstance();
        int seconds = now.get(Calendar.SECOND);
        int milliseconds = now.get(Calendar.MILLISECOND);

        // Calculate milliseconds until next 10-second mark
        int secondsUntilNext10 = 10 - (seconds % 10);
        if (secondsUntilNext10 == 10) secondsUntilNext10 = 0;
        int initialDelay = (secondsUntilNext10 * 1000) - milliseconds;

        // Create and start the timer
        renderTimer = new Timer(RENDER_INTERVAL, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Auto-rerendering at: " + Calendar.getInstance().getTime());
                cv.repaint();
            }
        });

        renderTimer.setInitialDelay(initialDelay);
        renderTimer.start();

        System.out.println("Timer started. First render in " + initialDelay + "ms, then every " + RENDER_INTERVAL + "ms");
   }
}
