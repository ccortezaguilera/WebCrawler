package webcrawler;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Carlos Aguilera
 */
public class Crawler extends Thread {
    private ThreadPoolExecutor threadPoolExecutor;
    private AtomicInteger maxDepth;
    private AtomicInteger depth;
    private int nextDepth;
    private URL seedURL;


    public Crawler(String seed, ThreadPoolExecutor executor, int depth, int maxdepth) {
        super();
        try {
            this.threadPoolExecutor = executor;
            this.seedURL = new URL(seed);
            this.maxDepth = new AtomicInteger(maxdepth);
            this.depth = new AtomicInteger(depth);
            this.nextDepth = this.depth.incrementAndGet();
        } catch (MalformedURLException mue) {
            System.err.println("The given seed url is malformed." + mue.getMessage());
        }
    }

    public Crawler(String seed, ThreadPoolExecutor executor) {
        super();
        try {
            this.threadPoolExecutor = executor;
            this.seedURL = new URL(seed);
        } catch (MalformedURLException mue) {
            System.err.println("The given seed url is malformed." + mue.getMessage());
        }
    }

    /*
    * Obtain the page for the given seed link
    * Clean the web page
    * Extract the links
    * Build a string with the appropriate format
    * */
    public void run() {
        if (this.maxDepth != null) {
            if (Integer.compare(this.maxDepth.get(), this.depth.get()) < 0) {
                return;
            }
        }
        String html = fetchHTML();
        String links = extractLinks(html);
        synchronized (System.out) {
            System.out.println(links);
        }
    }

    /**
     * Read the response from the server
     *
     * @param reader the reader used to read the input stream
     * @return The response from the server.
     * @implNote The reader here is assumed to be a BufferedReader it is chosen over casting the Reader
     * as a BufferedReader
     */
    private String readResponse(BufferedReader reader) {
        StringBuffer response = new StringBuffer();
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                response.append(line + '\n');
            }
            return response.toString();
        } catch (IOException ioe) {
            System.err.println("An error I/O exception occurred while reading the response");
        }
        return response.toString();
    }

    /**
     * Do an HTTP request
     *
     * @param method the type of method to use and open the connection.
     * @return The http url connection
     */
    private HttpURLConnection doAHTTPRequest(String method) {
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) this.seedURL.openConnection();
            httpURLConnection.setRequestMethod(method);
            int responeCode = httpURLConnection.getResponseCode();
            if (responeCode != HttpURLConnection.HTTP_OK
                    && responeCode != HttpURLConnection.HTTP_ACCEPTED) {
                httpURLConnection.disconnect();
                httpURLConnection = null;
            }
        } catch (IOException ioe) {
            System.err.println("An I/O exception occurred trying an http connection to " + this.seedURL.toString());
        }
        return httpURLConnection;
    }

    /**
     * Do an https request.
     *
     * @param method the method to use to connect to the server
     * @return The https connection
     * @implNote The implementation makes sure to avoid any self signing certificates that may cause problems by
     * defaulting the user-agent.
     */
    private HttpsURLConnection doAHTTPSRequest(String method) {
        HttpsURLConnection httpsURLConnection = null;
        try {
            httpsURLConnection = (HttpsURLConnection) this.seedURL.openConnection();
            httpsURLConnection.setRequestMethod(method);
            httpsURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            int responseCode = httpsURLConnection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK
                    && responseCode != HttpsURLConnection.HTTP_ACCEPTED) {
                httpsURLConnection.disconnect();
                httpsURLConnection = null;
            }
        } catch (IOException ioe) {
            System.err.println("An I/O exception occurred trying an https connection to " + this.seedURL.toString());
        }
        return httpsURLConnection;
    }

    /**
     * Fetches the html for the particular website.
     *
     * @return the html for the website.
     */
    private String fetchHTML() {
        String response = null;
        if (this.seedURL != null) {
            try {
                HttpsURLConnection httpsURLConnection = null;
                HttpURLConnection httpURLConnection = null;
                if (this.seedURL.toString().startsWith("https")) {
                    httpsURLConnection = doAHTTPSRequest("GET");
                    if (httpsURLConnection != null) {
                        InputStream inputStream = httpsURLConnection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        httpsURLConnection.connect();
                        response = readResponse(reader);
                        reader.close();
                    }
                } else {
                    httpURLConnection = doAHTTPRequest("GET");
                    if (httpURLConnection != null) {
                        InputStream inputStream = httpURLConnection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        httpURLConnection.connect();
                        response = readResponse(reader);
                        reader.close();
                    }
                }
            } catch (ProtocolException pe) {
                System.err.println("Protocol Exception" + pe.getMessage());
            } catch (SSLPeerUnverifiedException spue) {
                System.err.println("A SSLPeerUnverifiedException occurred " + spue.getMessage());
            } catch (IOException ioe) {
                System.err.println("An I/O exception occurred while connecting " + this.seedURL.toString() + " " + ioe.getMessage());
            }
        }
        return response;
    }

    /**
     * Extracts the links from the anchor tags of the website.
     *
     * @param html the html response
     * @return The results of the crawl which is all the links in the page and the link.
     */
    private String extractLinks(String html) {
        StringBuffer result = new StringBuffer();
        result.append(this.seedURL + "\n");
        if (html != null) {
            Pattern pattern = Pattern.compile(WebCrawler.REGEX);
            Matcher matcher = pattern.matcher(html);
            Set<String> links = new HashSet<String>();
            while (matcher.find()) {
                String newLink = matcher.group(WebCrawler.GROUP);
                if (links.contains(newLink)) {
                    continue;
                }
                links.add(newLink);
                result.append('\t');
                result.append(newLink);
                result.append('\n');
                Crawler newCrawler;
                if (this.maxDepth == null) {
                    newCrawler = new Crawler(newLink, this.threadPoolExecutor);
                } else {
                    newCrawler = new Crawler(newLink, this.threadPoolExecutor, this.nextDepth, this.maxDepth.get());
                }
                this.threadPoolExecutor.execute(newCrawler);
            }
        }
        return result.toString();
    }
}
