import java.awt.*;     
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*-----------------------------------------------------------
 *
 * This is the component onto which the diagram is displayed
 * 
 * The diagram is filled using a thread pool:
 * - The canvas is recursively divided into smaller rectangles
 * - When a rectangle's width is <= minBoxSize, a task is submitted to the thread pool
 * - Each recursive division also submits tasks for the four quadrants
 * 
 * The minBoxSize parameter controls the number of tasks created:
 * - Smaller values create more tasks (more granular work units)
 * - Larger values create fewer tasks (less overhead, potentially more efficient)
 * 
 * The threadPoolSize parameter controls the number of concurrent threads:
 * - Larger values allow more concurrent execution (potentially faster)
 * - Smaller values limit concurrency (less resource usage)
 * - Experiment with different values to find the optimal setting for your system
 *
 * ----------------------------------------------------------*/

public class MBCanvas extends Canvas
{
   private MBGlobals mg;   // reference to global definitions
   private ExecutorService threadPool; // Thread pool for executing tasks

   public MBCanvas(MBGlobals mGlob)
   {
	mg = mGlob;
        setSize(mg.pixeldim, mg.pixeldim);
        // Create a fixed thread pool with the specified size
        threadPool = Executors.newFixedThreadPool(mg.threadPoolSize);
        System.out.println("Created thread pool with " + mg.threadPoolSize + " threads");

        // Add shutdown hook to ensure thread pool is properly closed when application exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            cleanupThreadPool();
        }));
   }

   /**
    * Properly shuts down the thread pool, ensuring all tasks complete or timeout.
    * This method should be called when the application is exiting.
    */
   public void cleanupThreadPool() {
       if (threadPool != null && !threadPool.isShutdown()) {
           System.out.println("Shutting down thread pool...");
           threadPool.shutdown();
           try {
               // Wait for tasks to complete or timeout after 10 seconds
               if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                   System.out.println("Thread pool did not terminate in time, forcing shutdown");
                   threadPool.shutdownNow();
               }
               System.out.println("Thread pool shutdown complete");
           } catch (InterruptedException e) {
               System.out.println("Thread pool shutdown interrupted: " + e);
               threadPool.shutdownNow();
               Thread.currentThread().interrupt();
           }
       }
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

	// Note: We don't shut down the thread pool after each rendering
	// This avoids RejectedExecutionException errors that occur when tasks
	// are submitted to a shutting down thread pool
	// The thread pool will be shut down when the application exits via the shutdown hook
   }

   /**
    * Recursively divides the canvas into smaller rectangles and submits tasks to the thread pool.
    * When a rectangle is small enough (width <= minBoxSize), a task is submitted to paint it.
    * For larger rectangles, the method divides it into four quadrants and submits tasks for each.
    * The number of tasks created depends on the minBoxSize parameter - smaller values create more tasks.
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
	    // Check if the thread pool is shutting down
	    if (threadPool.isShutdown()) {
	        // If the thread pool is shutting down, execute the task directly
	        mbp.run();
	    } else {
	        // Otherwise, submit the task to the thread pool
	        try {
	            threadPool.execute(mbp);
	        } catch (java.util.concurrent.RejectedExecutionException e) {
	            // If the task is rejected, execute it directly
	            mbp.run();
	        }
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

      // Check if the thread pool is shutting down
      if (threadPool.isShutdown()) {
          // If the thread pool is shutting down, execute the tasks directly
          findRectangles(rect1);
          findRectangles(rect2);
          findRectangles(rect3);
          findRectangles(rect4);
      } else {
          // Otherwise, submit tasks for all four quadrants to the thread pool
          try {
              threadPool.execute(() -> findRectangles(rect1));
              threadPool.execute(() -> findRectangles(rect2));
              threadPool.execute(() -> findRectangles(rect3));
              threadPool.execute(() -> findRectangles(rect4));
          } catch (java.util.concurrent.RejectedExecutionException e) {
              // If the tasks are rejected, execute them directly
              findRectangles(rect1);
              findRectangles(rect2);
              findRectangles(rect3);
              findRectangles(rect4);
          }
      }
   }

}
