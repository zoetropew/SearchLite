package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Stores queries and results
 * 
 * @author Zoe Wong
 */
public class Results implements ResultsInterface {
	/**
	 * Data structure to store the results found from the queries and inverted index.
	 * String key is the query line, value is a list of result entries
	 */
	private final Map<String, List<InvertedIndex.Result>> results;

	/**
	 * The inverted index to be searched for these results
	 */
	private final InvertedIndex index;

	/**
	 * A stemmer to use in reading queries
	 */
	private final Stemmer stemmer;

	/**
	 * Constructor for Results
	 * 
	 * @param toSearch the inverted index to be searched
	 */
	public Results(InvertedIndex toSearch) {
		this.results = new TreeMap<>();
		this.index = toSearch;
		this.stemmer = new SnowballStemmer(ENGLISH);
	}

	@Override
	public Set<String> viewQueries() {
		return Collections.unmodifiableSet(this.results.keySet());
	}

	@Override
	public List<InvertedIndex.Result> viewResults(String query) {
		String queryLine = String.join(" ", FileStemmer.uniqueStems(query, stemmer));
		if(this.results.containsKey(queryLine)) {
			return Collections.unmodifiableList(this.results.get(queryLine));
		}
		return Collections.emptyList();
	}

	@Override
	public void readQueries(String line, boolean partial) {
		TreeSet<String> words = FileStemmer.uniqueStems(line, stemmer);
		if (!words.isEmpty()) {
			String queryLine = String.join(" ", words);
			if (results.get(queryLine) == null) {
				this.results.put(queryLine, this.index.search(words, partial));
			}
		}
	}

	@Override
	public void writeResults(Path output) throws IOException {
		JsonWriter.writeObjectArrayResults(this.results, output);
	}
}
