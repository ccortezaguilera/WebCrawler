package webcrawler;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Carlos Aguilera
 */

public class WebCrawler {
    public static final String REGEX = "(?ims)<\\s*[aA]\\s+[^>]*href=\"(https?://.[^\"]*+).[^/aA]*?";
    public static final int GROUP = 1;

    private final int INIT_DEPTH = 0;
    private String seed;
    private ThreadPoolExecutor executor;
    private int maxDepth;

    /* Crawlers - threads
    *  Cleaner
    *  Links - set of links crawled
    * 100 threads and max 200 threads
    *
    * */
    public WebCrawler(String seed) {
        this.seed = seed;
        this.executor = new ThreadPoolExecutor(100, 200, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    public WebCrawler(String seed, int threads, int maxThreads) {
        this.seed = seed;
        this.executor = new ThreadPoolExecutor(threads, maxThreads, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    public WebCrawler(String seed, int maxdepth) {
        this.seed = seed;
        this.maxDepth = maxdepth;
        this.executor = new ThreadPoolExecutor(100, 200, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    public WebCrawler(String seed, int threads, int maxThreads, int maxdepth) {
        this.seed = seed;
        this.executor = new ThreadPoolExecutor(threads, maxThreads, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        this.maxDepth = maxdepth;
    }

    public void crawl(boolean isMaxDepth) {
        if (isMaxDepth) {
            this.executor.execute(new Crawler(this.seed, this.executor, INIT_DEPTH, this.maxDepth));
        } else {
            this.executor.execute(new Crawler(this.seed, executor));
        }
    }
}
