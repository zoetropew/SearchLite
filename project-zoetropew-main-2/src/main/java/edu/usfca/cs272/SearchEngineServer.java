package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import edu.usfca.cs272.InvertedIndex.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Server for the search engine
 * 
 * @author zoe
 *
 */
public class SearchEngineServer {

	/**
	 * The results to store to and read from
	 */
	private final ThreadedResults results;

	/**
	 * The WorkQueue to use
	 */
	private final WorkQueue queue;

	/**
	 * The WebCrawler to use
	 */
	private final WebCrawler crawler;

	/**
	 * The index to search
	 */
	private final ThreadedIndex index;

	/** 
	 * Base path with HTML templates
	 */
	private static final Path base = Path.of("src", "main", "resources", "html");

	/** 
	 * Template for starting HTML
	 */
	private final String headTemplate;

	/**
	 * Template for ending HTML
	 */
	private final String footTemplate;

	/**
	 * Constructor for SearchEngineServer
	 * 
	 * @param results the results to store to and read from
	 * @param queue the queue to use
	 * @param crawler the crawler
	 * @param index the index to search
	 * @throws IOException if an IOException occurs
	 */
	public SearchEngineServer(ThreadedResults results, WorkQueue queue, WebCrawler crawler, ThreadedIndex index) throws IOException {
		this.results = results;
		this.queue = queue;
		this.crawler = crawler;
		this.index = index;

		// load templates
		headTemplate = Files.readString(base.resolve("head.html"), UTF_8);
		footTemplate = Files.readString(base.resolve("foot.html"), UTF_8);
	}

	/**
	 * Starts the server at the given port
	 * 
	 * @param port the port
	 * @throws Exception if an Exception occurs
	 */
	public void startServer(int port) throws Exception {
		System.out.println("Server starting...");

		Server server = new Server(port);
		System.out.println("Server started with thread: " + Thread.currentThread().getName());
		ServletHandler handler = new ServletHandler();
		handler.addServletWithMapping(new ServletHolder(new SearchEngineServlet()), "/");
		handler.addServletWithMapping(new ServletHolder(new CrawlServlet()), "/addSeed");
		handler.addServletWithMapping(new ServletHolder(new BrowseServlet()), "/browse");
		handler.addServletWithMapping(new ServletHolder(new LocationsServlet()), "/browseLocations");
		handler.addServletWithMapping(new ServletHolder(new DownloadServlet()), "/download");
		server.setHandler(handler);
		server.start();
		server.join();
	}

	/**
	 * Servlet for search engine using Bulma
	 *
	 * @author zoe
	 */
	private class SearchEngineServlet extends HttpServlet {

		/** 
		 * For serialization
		 */
		private static final long serialVersionUID = 302;

		/** 
		 * Template for search bar HTML
		 */
		private final String searchTemplate;

		/**
		 * Template for individual result HTML
		 */
		private final String textTemplate;

		/**
		 * Initializes search engine with header, search bar, results area, and footer. 
		 * 
		 * @throws IOException if unable to read templates
		 */
		public SearchEngineServlet() throws IOException {
			super();

			// load templates
			searchTemplate = Files.readString(base.resolve("searchbar.html"), UTF_8);
			textTemplate = Files.readString(base.resolve("results.html"), UTF_8);
		}

		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {

			// Collect values for template
			Map<String, String> values = new HashMap<>();
			values.put("title", "Search Engine");
			values.put("action", request.getServletPath());

			// Collect input
			String query = request.getParameter("query");
			query = StringEscapeUtils.escapeHtml4(query);
			if (query == null) {
				query = "";
				values.put("query", "");
			} else {
				values.put("query", "Searching for \"" + query + "\"");
			}

			// Process queries
			System.out.println("Query: " + query);
			synchronized (results) {
				results.readQueries(query, true);
			}
			queue.finish();

			// Replace values in template
			StringSubstitutor replacer = new StringSubstitutor(values);
			PrintWriter out = response.getWriter();
			out.println(replacer.replace(headTemplate));
			out.println(replacer.replace(searchTemplate));
			if (query.equals("")) {
				out.printf("    <p class=\"has-text-centered\">Nothing to search.</p>%n");
			} else {
				List<Result> list;
				synchronized (results) {
					list = results.viewResults(query);
				}
				if (list.size() != 0) {
					for (Result result : list) {
						Map<String, Object> map = Map.of("location", result.getLocation(), "count", result.getCount(), "score", result.getScore());
						String html = StringSubstitutor.replace(textTemplate, map);
						out.println(html);
					}
				} else {
					out.printf("    <p class=\"has-text-centered\">No results found.</p>%n");
				}
			}
			out.println(replacer.replace(footTemplate));
			out.flush();

			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
		}
	}

	/**
	 * Servlet for adding new seed to index
	 * 
	 * @author zoe
	 */
	private class CrawlServlet extends HttpServlet {

		/** 
		 * For serialization
		 */
		private static final long serialVersionUID = 302;

		/**
		 * Template for add seed HTML
		 */
		private final String seedTemplate;

		/**
		 * Initializes search engine with header, search bar, results area, and footer. 
		 * 
		 * @throws IOException if unable to read templates
		 */
		public CrawlServlet() throws IOException {
			super();

			// load templates
			seedTemplate = Files.readString(base.resolve("seed.html"), UTF_8);
		}

		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			// Collect values for template
			Map<String, String> values = new HashMap<>();
			values.put("title", "Search Engine - Add Seed");
			values.put("action", request.getServletPath());

			// Collect input
			String url = request.getParameter("seed");
			String crawl = request.getParameter("crawl");
			url = StringEscapeUtils.escapeHtml4(url);

			int intCrawl = 1;
			if (crawl != null) {
				try {
					intCrawl = Integer.parseInt(crawl);
				} catch (NumberFormatException e) {
					System.out.println("could not parse crawl: " + crawl);
				}
			}

			if (url == null) {
				url = "";
				values.put("added", "Please input a URL to add to the index.");
			} else if (crawler.getCrawled().contains(new URL(url))) {
				values.put("added", "\"" + url + "\" already added to the index.");
			} else {
				try {
					crawler.build(url, intCrawl);
					values.put("added", "Added \"" + url + "\" to the index.");
				} catch (MalformedURLException | URISyntaxException e) {
					values.put("added", "Error adding \"" + url + "\" to the index.");
				}
			}

			// Replace values in template
			StringSubstitutor replacer = new StringSubstitutor(values);
			PrintWriter out = response.getWriter();
			out.println(replacer.replace(headTemplate));
			out.println(replacer.replace(seedTemplate));
			out.println(replacer.replace(footTemplate));
			out.flush();

			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
		}

	}

	/**
	 * Servlet for browsing index
	 * 
	 * @author zoe
	 */
	private class BrowseServlet extends HttpServlet {

		/** 
		 * For serialization
		 */
		private static final long serialVersionUID = 302;

		/**
		 * Template for add browse form HTML
		 */
		private final String browseTemplate;

		/**
		 * Template for beginning of index list HTML
		 */
		private final String beginTemplate;

		/**
		 * Template for entry in index list HTML
		 */
		private final String listTemplate;

		/**
		 * Initializes search engine with header, search bar, results area, and footer. 
		 * 
		 * @throws IOException if unable to read templates
		 */
		public BrowseServlet() throws IOException {
			super();

			// load templates
			browseTemplate = Files.readString(base.resolve("browse.html"), UTF_8);
			beginTemplate = Files.readString(base.resolve("beginList.html"), UTF_8);
			listTemplate = Files.readString(base.resolve("list.html"), UTF_8);
		}

		/**
		 * Helper method to display the list of locations and number of positions
		 * 
		 * @param out the PrintWriter
		 * @param word the word to write the list for
		 * @param replacer the StringSubstitutor
		 * @param values the values to template
		 */
		private void writeList(PrintWriter out, String word, StringSubstitutor replacer, Map<String, String> values) {
			values.put("word", word);
			String begin = replacer.replace(beginTemplate);
			out.println(begin);

			for (String location : index.viewLocations(word)) {
				Map<String, Object> map = Map.of("location", location, "count", index.numPositions(word, location));
				String html = StringSubstitutor.replace(listTemplate, map);
				out.println(html);
			}

			out.println("</ul></div></div>");
		}

		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			// Collect values for template
			Map<String, String> values = new HashMap<>();
			values.put("title", "Search Engine - Browse Index");
			values.put("action", request.getServletPath());

			// Collect input
			String query = request.getParameter("query");
			query = StringEscapeUtils.escapeHtml4(query);
			if (query == null) {
				values.put("query", "Complete index");
			} else {
				values.put("query", "Searching for \"" + query + "\"");
			}

			// Replace values in template
			StringSubstitutor replacer = new StringSubstitutor(values);
			PrintWriter out = response.getWriter();
			out.println(replacer.replace(headTemplate));
			out.println(replacer.replace(browseTemplate));
			if (query == null) {
				for (String word : index.viewWords()) {
					writeList(out, word, replacer, values);
				}
			} else if (index.containsWord(query)) {
				writeList(out, query, replacer, values);
			} else {
				out.printf("    <p>Not found.</p>%n");
			}
			out.println(replacer.replace(footTemplate));
			out.flush();

			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
		}

	}

	/**
	 * Servlet for browsing locations
	 * 
	 * @author zoe
	 */
	private class LocationsServlet extends HttpServlet {

		/** 
		 * For serialization
		 */
		private static final long serialVersionUID = 302;

		/**
		 * Template for add browse form HTML
		 */
		private final String locationsTemplate;

		/**
		 * Template for index list HTML
		 */
		private final String listTemplate;

		/**
		 * Initializes search engine with header, search bar, results area, and footer. 
		 * 
		 * @throws IOException if unable to read templates
		 */
		public LocationsServlet() throws IOException {
			super();

			// load templates
			locationsTemplate = Files.readString(base.resolve("locations.html"), UTF_8);
			listTemplate = Files.readString(base.resolve("list.html"), UTF_8);
		}

		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			// Collect values for template
			Map<String, String> values = new HashMap<>();
			values.put("title", "Search Engine - Browse Locations");
			values.put("action", request.getServletPath());

			// Replace values in template
			StringSubstitutor replacer = new StringSubstitutor(values);
			PrintWriter out = response.getWriter();
			out.println(replacer.replace(headTemplate));
			out.println(replacer.replace(locationsTemplate));
			for (Entry<String, Integer> entry : index.viewCounts().entrySet()) {
				Map<String, Object> map = Map.of("location", entry.getKey(), "count", entry.getValue());
				String html = StringSubstitutor.replace(listTemplate, map);
				out.println(html);
			}
			out.println("</ul></div></div>");
			out.println(replacer.replace(footTemplate));
			out.flush();

			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
		}

	}

	/**
	 * Servlet for downloading index.json
	 * 
	 * @author zoe
	 */
	private class DownloadServlet extends HttpServlet {

		/** 
		 * For serialization
		 */
		private static final long serialVersionUID = 302;

		/**
		 * Initializes search engine with header, search bar, results area, and footer. 
		 * 
		 * @throws IOException if unable to read templates
		 */
		public DownloadServlet() throws IOException {
			super();
		}

		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			Path output = Path.of("index.json");
			try {
				index.writeIndex(output);
			} catch (IOException e) {
				System.out.println("Unable to output the index to the path: " + output);
			}

			// Write output
			response.setContentType("text/plain");
			response.setHeader("Content-disposition", "attachment; filename=index.json");

			try(InputStream in = Files.newInputStream(output);
					OutputStream out = response.getOutputStream()) {
				byte[] buffer = new byte[500];
				int read;
				while ((read = in.read(buffer)) > 0) {
					out.write(buffer, 0, read);
				}
			}
		}

	}

}
