package edu.usfca.cs272;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Path;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Zoe Wong
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class Driver {

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		ArgumentParser map = new ArgumentParser(args);
		InvertedIndex index;
		ResultsInterface results;
		WorkQueue queue = null;
		ThreadedIndex safe = null;
		WebCrawler crawler = null;
		ThreadedResults safeResults = null;

		// Create threads
		if (map.hasFlag("-threads") || map.hasFlag("-server") || 
				(map.hasFlag("-html") && map.hasValue("-html"))) {
			int threads = map.getInteger("-threads");
			if (threads < 1) {
				queue = new WorkQueue();
			} else {
				queue = new WorkQueue(threads);
			}
			index = safe = new ThreadedIndex();
			results = safeResults = new ThreadedResults(safe, queue);
		} else {
			index = new InvertedIndex();
			results = new Results(index);
		}

		// Input links
		if(map.hasFlag("-html") && map.hasValue("-html")) {
			crawler = new WebCrawler(queue, safe);
			int crawl = 1;
			if(map.hasFlag("-crawl") && map.getInteger("-crawl") != 0) {
				crawl = map.getInteger("-crawl");
			}
			String input = map.getString("-html");
			try {
				crawler.build(input, crawl);
			} catch (URISyntaxException | MalformedURLException e) {
				System.out.println("Failed to read link: " + input);
			}
		}

		// Input text
		if(map.hasFlag("-text") && map.hasValue("-text")) {
			Path input = map.getPath("-text");
			try {
				if (queue != null) {
					ThreadedIndexBuilder.build(input, safe, queue);
				} else {
					InvertedIndexBuilder.build(input, index);
				}
			} catch (IOException e) {
				System.out.println("Failed to read text input from " + input);
			}
		}

		// Input queries
		if(map.hasFlag("-query") && map.hasValue("-query")) {
			Path output = map.getPath("-query");
			try {
				results.readQueries(output, map.hasFlag("-partial"));
			} catch (IOException e) {
				System.out.println("Unable to fetch queries from the path: " + output);
			}
		}

		// Start server
		if (map.hasFlag("-server")) {
			int port = map.getInteger("-server");
			if (port <= 0) {
				port = 8080;
			}
			try {
				SearchEngineServer server = new SearchEngineServer(safeResults, queue, crawler, safe);
				server.startServer(port);
			} catch (Exception e) {
				System.out.println("Error starting server");
			}
		}

		// Shutdown queue
		if(queue != null) {
			queue.shutdown();
		}

		// Output counts
		if(map.hasFlag("-counts")) { 
			Path output = map.getPath("-counts", Path.of("counts.json"));
			try {
				index.writeCounts(output);
			} catch (IOException e) {
				System.out.println("Unable to output the counts to the path: " + output);
			}
		}

		// Output index
		if(map.hasFlag("-index")) {
			Path output = map.getPath("-index", Path.of("index.json"));
			try {
				index.writeIndex(output);
			} catch (IOException e) {
				System.out.println("Unable to output the index to the path: " + output);
			}
		}

		// Output results
		if(map.hasFlag("-results")) {
			Path output = map.getPath("-results", Path.of("results.json"));
			try {
				results.writeResults(output);
			} catch (IOException e) {
				System.out.println("Unable to output results to the path: " + output);
			}
		}
	}
}
