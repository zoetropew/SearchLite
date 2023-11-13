package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.usfca.cs272.InvertedIndex.Result;

/**
 * Thread safe Results
 * 
 * @author zoe
 *
 */
public class ThreadedResults implements ResultsInterface {
	/**
	 * Data structure to store the results found from the queries and inverted index.
	 * String key is the query line, value is a list of result entries
	 */
	private final Map<String, List<InvertedIndex.Result>> results;

	/**
	 * The inverted index to be searched for these results
	 */
	private final ThreadedIndex index;

	/**
	 * The work queue to use for tasks
	 */
	private final WorkQueue queue;

	/**
	 * Constructor that includes lock
	 * 
	 * @param toSearch the inverted index to be searched
	 * @param queue the work queue
	 */
	public ThreadedResults(ThreadedIndex toSearch, WorkQueue queue) {
		this.results = new TreeMap<>();
		this.index = toSearch;
		this.queue = queue;
	}

	@Override
	public Set<String> viewQueries() {
		synchronized (results) {
			return Collections.unmodifiableSet(this.results.keySet());
		}
	}

	@Override
	public List<InvertedIndex.Result> viewResults(String query) {
		String queryLine = String.join(" ", FileStemmer.uniqueStems(query));
		synchronized (results) {
			if(this.results.containsKey(queryLine)) {
				return Collections.unmodifiableList(this.results.get(queryLine));
			}
		}
		return Collections.emptyList();
	}

	@Override
	public Boolean containsQuery(String query) {
		synchronized (results) {
			return ResultsInterface.super.containsQuery(query);
		}
	}

	@Override
	public Boolean containsResult(String query, InvertedIndex.Result result) {
		synchronized (results) {
			return ResultsInterface.super.containsResult(query, result);
		}
	}

	@Override
	public int numQueries() {
		synchronized (results) {
			return ResultsInterface.super.numQueries();
		}
	}

	@Override
	public int numResults(String query) {
		synchronized (results) {
			return ResultsInterface.super.numResults(query);
		}
	}

	@Override
	public void readQueries(Path file, boolean partial) throws IOException{
		ResultsInterface.super.readQueries(file, partial);
		queue.finish();
	}

	@Override
	public void readQueries(String line, boolean partial) {
		queue.execute(new ReadQueries(line, partial));
	}

	/**
	 * Task for reading queries
	 * 
	 * @author zoe
	 *
	 */
	private class ReadQueries implements Runnable {

		/**
		 * whether to search partial or exact
		 */
		private final boolean partial;

		/**
		 * the query line to process
		 */
		private final String line;

		/**
		 * Constructor
		 * 
		 * @param line the query line to process
		 * @param partial whether to search partial or exact
		 */
		public ReadQueries(String line, boolean partial) {
			this.line = line;
			this.partial = partial;
		}

		@Override
		public void run() {
			TreeSet<String> words = FileStemmer.uniqueStems(line);
			if (!words.isEmpty()) {
				String queryLine = String.join(" ", words);
				synchronized (results) {
					if (results.containsKey(queryLine)) {
						return;
					}
					results.put(queryLine, null);
				}
				List<Result> local = index.search(words, partial);
				synchronized (results) {
					results.put(queryLine, local);
				}
			}
		}

	}

	@Override
	public void writeResults(Path output) throws IOException {
		synchronized (results) {
			JsonWriter.writeObjectArrayResults(this.results, output);
		}
	}
}
