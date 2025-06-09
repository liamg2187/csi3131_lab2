import java.awt.*;     

/*-----------------------------------------------------------
 *
 * This is the component onto which the diagram is displayed
 * 
 * The diagram is filled using multiple threads:
 * - The canvas is recursively divided into smaller rectangles
 * - When a rectangle's width is <= minBoxSize, a thread is created to fill it
 * - Each recursive division also creates threads for the four quadrants
 * 
 * The minBoxSize parameter controls the number of threads used:
 * - Smaller values create more threads (more parallelism, potentially faster)
 * - Larger values create fewer threads (less overhead, potentially more efficient)
 * - Experiment with different values to find the optimal setting for your system
 *
 * ----------------------------------------------------------*/

public class MBCanvas extends Canvas
{
   private MBGlobals mg;   // reference to global definitions

   public MBCanvas(MBGlobals mGlob)
   {
	mg = mGlob;
        setSize(mg.pixeldim, mg.pixeldim);
   }

   public void paint(Graphics g)  // this method paints the canvas
   {
	   /* reset screen to blank */
        g.setColor(Color.white);
	g.fillRect(0,0,mg.pixeldim, mg.pixeldim);

	  /* Call method to add MandelBrot pattern */
	  /* Run MBCompute in this thread */
	Rectangle nrect = new Rectangle(0,0,mg.pixeldim,mg.pixeldim);
	findRectangles(nrect);
   }

   /**
    * Recursively divides the canvas into smaller rectangles and creates threads to fill them.
    * When a rectangle is small enough (width <= minBoxSize), a thread is created to fill it.
    * For larger rectangles, the method divides it into four quadrants and creates a thread for each.
    * The number of threads created depends on the minBoxSize parameter - smaller values create more threads.
    * 
    * @param mrect The rectangle to process
    */
   private void findRectangles(Rectangle mrect)
   {
      MBPaint mbp;
      Rectangle nrect;

      // Compute the maximum pixel values for hor (i) and vert (j) 
      int maxi = mrect.x + mrect.width;
      int maxj = mrect.y + mrect.height;

      // Only when the square is small enough do we fill
      if( (maxi - mrect.x) <= mg.minBoxSize)  
      {
            // Can now do the painting
	    mbp = new MBPaint(this, mg, mrect);
	    // Create a new thread for painting this rectangle
	    Thread thread = new Thread(mbp);
	    thread.start();
	    try {
	        // Wait for this thread to complete before returning
	        thread.join();
	    } catch (InterruptedException e) {
	        System.out.println("Thread interrupted: " + e);
	    }
	    return;
      }

            // recursiverly compute the four subquadrants
      int midw = mrect.width/2;
      int wover = mrect.width % 2;  // for widths not divisable by 2 
      int midh = mrect.height/2;
      int hover = mrect.height % 2;  // for heights not divisable by 2 

      // Create rectangles for all four quadrants
      Rectangle rect1 = new Rectangle(mrect.x, mrect.y, midw, midh);
      Rectangle rect2 = new Rectangle(mrect.x+midw, mrect.y, midw+wover, midh);
      Rectangle rect3 = new Rectangle(mrect.x, mrect.y+midh, midw, midh+hover);
      Rectangle rect4 = new Rectangle(mrect.x+midw, mrect.y+midh, midw+wover, midh+hover);

      // Create threads for all four quadrants
      Thread thread1 = new Thread(() -> findRectangles(rect1));
      Thread thread2 = new Thread(() -> findRectangles(rect2));
      Thread thread3 = new Thread(() -> findRectangles(rect3));
      Thread thread4 = new Thread(() -> findRectangles(rect4));

      // Start all threads
      thread1.start();
      thread2.start();
      thread3.start();
      thread4.start();

      // Wait for all threads to complete
      try {
          thread1.join();
          thread2.join();
          thread3.join();
          thread4.join();
      } catch (InterruptedException e) {
          System.out.println("Thread interrupted: " + e);
      }
   }

}
