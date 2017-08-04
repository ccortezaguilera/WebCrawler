package webcrawler;

/**
 * @author Carlos Aguilera
 * @version 1.0.0
 *          <p>
 *          The main class that runs the program.
 */
public class Driver {

    /**
     * Parses the command line for the correct arguments.
     *
     * @param args The arguments passed through the command line.
     * @implNote The implementation assumes that there will only be max three arguments.
     */
    public static void parse(String[] args) {
        if (args.length < 1 || args.length > 3) {
            usage();
            return;
        }

        Integer maxDepth = null;
        String seed = null;
        for (int i = 0; i < args.length; i++) {
            switch (args[i].charAt(0)) {
                case '-':
                    if (Character.compare(args[i].charAt(1), 'n') == 0) {
                        ;
                        i++;
                        if (i >= args.length) {
                            usage();
                            return;
                        }
                        try {
                            maxDepth = new Integer(args[i]);
                        } catch (NumberFormatException nfe) {
                            System.out.println(nfe.getMessage());
                            usage();
                            return;
                        }
                    } else {
                        usage();
                        return;
                    }
                    break;
                default:
                    seed = args[i];
            }
        }
        init(maxDepth, seed);
    }

    /**
     * Initializes the web crawl with the appropriate parsed data.
     *
     * @param maxDepth the maximum depth that the crawler will crawl (it's inclusive)
     * @param seed     the starting url for the crawler to crawl
     */
    public static void init(Integer maxDepth, String seed) {
        WebCrawler webCrawler;
        boolean hasDepth = false;
        if (maxDepth == null) {
            webCrawler = new WebCrawler(seed);
        } else {
            webCrawler = new WebCrawler(seed, maxDepth.intValue());
            hasDepth = true;
        }

        webCrawler.crawl(hasDepth);
    }


    /**
     * Prints how to run the program if the user has failed to provide the correct tags
     * <p>
     * The message is similar to the executables in linux where the usage and options are printed.
     * </p>
     */
    public static void usage() {
        System.out.println("Usage: java [OPTIONS] [seed url]\n");
        System.out.println("[OPTIONS]:\n \t-n count\t\tThe max depth to search");
    }

    public static void main(String[] args) {
        parse(args);
    }
}
