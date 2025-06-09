import java.io.*;

public class MandelBrot 
{
    public static void main(String[] args) throws IOException 
    {

	    if(args.length < 5)
	    {
		    System.out.println("Usage: java MandelBrot <CornerX> <CornerY> <Size> <SizePixels> <MinBox> [<ThreadPoolSize>]");
		    System.out.println("ThreadPoolSize is optional (default is 20)");
		    System.exit(1);
	    }

	    try 
	    {
	            System.out.println("Hello - Creating MandelBrot Diagram");

	            if(args.length >= 6) {
	                // With thread pool size specified
	                new MBFrame(Double.valueOf(args[0]),
			    	       Double.valueOf(args[1]),
			    	       Double.valueOf(args[2]),
			               Integer.valueOf(args[3]),
			               Integer.valueOf(args[4]),
			               Integer.valueOf(args[5]));
			    System.out.println("Using thread pool size: " + args[5]);
	            } else {
	                // Without thread pool size (use default)
		        new MBFrame(Double.valueOf(args[0]),
			    	       Double.valueOf(args[1]),
			    	       Double.valueOf(args[2]),
			               Integer.valueOf(args[3]),
			               Integer.valueOf(args[4]));
			    System.out.println("Using default thread pool size: 20");
	            }
	    }
	    catch(NumberFormatException e)
	    {
		    System.out.println("Error in arguments" + e);
		    System.out.println("Usage: java MandelBrot <CornerX> <CornerY> <Size> <SizePixels> <MinBox> [<ThreadPoolSize>]");
		    System.out.println("ThreadPoolSize is optional (default is 20)");
	    }

    }
}
