package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Interface for storing results
 * 
 * @author zoe
 *
 */
public interface ResultsInterface {

	/**
	 * Returns an unmodifiable view of the queries for this search
	 * 
	 * @return an unmodifiable view of the queries in results
	 */
	public Set<String> viewQueries();

	/**
	 * Returns an unmodifiable view of the results for a specific query
	 * 
	 * @param query the query
	 * @return the set of results
	 */
	public List<InvertedIndex.Result> viewResults(String query);

	/**
	 * Returns whether the given query is in the results
	 * 
	 * @param query the query
	 * @return true if query is in the results, false if not
	 */
	public default Boolean containsQuery(String query) {
		return viewQueries().contains(query);
	}

	/**
	 * Returns whether the given result for the given query is in the results
	 * 
	 * @param query the query
	 * @param result the result
	 * @return true if the result for the query is in the results, false if not
	 */
	public default Boolean containsResult(String query, InvertedIndex.Result result) {
		return viewResults(query).contains(result);
	}

	/**
	 * Returns the number of queries for this search
	 * 
	 * @return the number of queries
	 */
	public default int numQueries() {
		return viewQueries().size();
	}

	/**
	 * Returns the number of results for a given query
	 * 
	 * @param query the query
	 * @return the number of results
	 */
	public default int numResults(String query) {
		return viewResults(query).size();
	}

	/**
	 * Read queries from a file line by line
	 * 
	 * @param file the file to read queries from
	 * @param partial whether to search for partial or exact
	 * @throws IOException if an IO error occurs
	 */
	public default void readQueries(Path file, boolean partial) throws IOException {
		try(BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)){
			String line;
			while ((line = br.readLine()) != null) {
				readQueries(line, partial);
			}
		}
	}

	/**
	 * Finds and stores results based on a single query line
	 * 
	 * @param line the line of queries to read
	 * @param partial whether to search for partial or exact
	 */
	public void readQueries(String line, boolean partial);

	/**
	 * Writes the results as JSON to the specified path
	 * 
	 * @param output the path to write to
	 * @throws IOException if an IO error occurs
	 */
	public void writeResults(Path output) throws IOException;

}
