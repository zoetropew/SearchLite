package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Data structure class for the inverted index and word counts
 * 
 * @author Zoe Wong
 *
 */
public class InvertedIndex {

	/**
	 * Data structure to store counts of word stems in each file.
	 * String key is the location of the file, 
	 * Integer value is the number of word stems in the file.
	 */
	private final TreeMap<String, Integer> counts;

	/**
	 * Data structure to store an inverted index of words.
	 * String key is the word, value is a nested TreeMap.
	 * String key of the nested TreeMap is the filename location,
	 * value is a TreeSet of Integer positions of the word.
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;

	/**
	 * Constructor for InvertedIndex
	 */
	public InvertedIndex(){
		counts = new TreeMap<>();
		index = new TreeMap<>();
	}

	/**
	 * Returns an unmodifiable view of the counts
	 * 
	 * @return an unmodifiable view of the counts
	 */
	public Map<String, Integer> viewCounts(){
		return Collections.unmodifiableMap(counts);
	}

	/**
	 * Returns an unmodifiable view of the words in the index
	 * 
	 * @return an unmodifiable view of the words in the index
	 */
	public Set<String> viewWords(){
		return Collections.unmodifiableSet(index.keySet());
	}

	/**
	 * Returns an unmodifiable view of the locations in the index
	 * 
	 * @param word the word in the index
	 * @return an unmodifiable view of the locations in the index
	 */
	public Set<String> viewLocations(String word){
		TreeMap<String, TreeSet<Integer>> theWord = index.get(word);
		if(theWord != null) {
			return Collections.unmodifiableSet(theWord.keySet());
		}
		return Collections.emptySet();
	}

	/**
	 * Returns an unmodifiable view of the positions in the index
	 * 
	 * @param word the word in the index
	 * @param location the file location in the index
	 * @return an unmodifiable view of the positions in the index
	 */
	public Set<Integer> viewPositions(String word, String location) {
		TreeMap<String, TreeSet<Integer>> locations = index.get(word);
		if (locations != null) {
			TreeSet<Integer> positions = locations.get(location);
			if (positions != null) {
				return Collections.unmodifiableSet(positions);
			}
		}
		return Collections.emptySet();
	}

	/**
	 * Writes the counts to the file in pretty JSON format
	 * 
	 * @param path the path to write the counts to
	 * @throws IOException if an IO error occurs
	 */
	public void writeCounts(Path path) throws IOException {
		JsonWriter.writeObject(this.counts, path);
	}

	/**
	 * Writes the index to the file in pretty JSON format
	 * 
	 * @param path the path to write the inverted index to
	 * @throws IOException if an IO error occurs
	 */
	public void writeIndex(Path path) throws IOException {
		JsonWriter.writeObjectObjects(this.index, path);
	}

	/**
	 * Adds the number of word stems to counts
	 * 
	 * @param file the filename
	 * @param count the number of word stems in the file
	 */
	private void addCount(String file, int count) {
		Integer n = this.counts.get(file);
		if(n == null || n < count) {
			counts.put(file, count);
		}
	}

	/**
	 * Adds an instance of a word to the index
	 * Calls addCont to update counts accordingly
	 * 
	 * @param word the word to add
	 * @param location the filename where the word was found
	 * @param position the position the word was found in
	 */
	public void addIndexEntry(String word, String location, int position) {
		TreeMap<String, TreeSet<Integer>> wordMap = index.get(word);
		if(wordMap == null) {
			wordMap = new TreeMap<>();
			index.put(word, wordMap);
		}
		TreeSet<Integer> positionList = wordMap.get(location);
		if(positionList == null) {
			positionList = new TreeSet<>();
			wordMap.put(location, positionList);
		}
		positionList.add(position);
		addCount(location, position);
	}

	/**
	 * Adds all words in a file to the index
	 * 
	 * @param words the list of words to add
	 * @param location the filename where the word was found
	 */
	public void addAll(List<String> words, String location) {
		int position = 0;
		for (String word : words) {
			addIndexEntry(word, location, ++position);
		}
	}

	/**
	 * Adds all words from the given InvertedIndex to this index
	 * 
	 * @param storage the index to add from 
	 */
	public void addAll(InvertedIndex storage) {
		for (var entry : storage.index.entrySet()) {
			String word = entry.getKey();
			TreeMap<String, TreeSet<Integer>> storageLocations = entry.getValue();
			TreeMap<String, TreeSet<Integer>> thisLocations = this.index.get(word);
			if (thisLocations == null) {
				this.index.put(word, storageLocations);
			} else {
				for (Entry<String, TreeSet<Integer>> stored : storageLocations.entrySet()) {
					String loc = stored.getKey();
					TreeSet<Integer> overlap = thisLocations.get(loc);
					if (overlap != null) {
						overlap.addAll(stored.getValue());
					} else {
						thisLocations.put(loc, stored.getValue());
					}
				}
			}
		}
		for (var entry : storage.counts.entrySet()) {
			String file = entry.getKey();
			Integer thisCount = this.counts.get(file);
			if (thisCount == null) {
				this.counts.put(file, entry.getValue());
			} else {
				this.counts.put(file, thisCount + entry.getValue());
			}
		}
	}

	/**
	 * Returns whether the given word is in the index
	 * 
	 * @param word the word to be found
	 * @return true if in index, false if not
	 */
	public boolean containsWord(String word) {
		return viewWords().contains(word); 
	}

	/**
	 * Returns whether the given location with the given word is in the index
	 * 
	 * @param word the word in the index
	 * @param location the location to be found
	 * @return true if location is in the index and has the word, false if not
	 */
	public boolean containsLocation(String word, String location) {
		return viewLocations(word).contains(location);
	}

	/**
	 * Returns whether the given position in the given location of the given word is in the index
	 * 
	 * @param word the word in the index
	 * @param location the location in the index
	 * @param position the position to be found
	 * @return true if position is in the index, false if not
	 */
	public boolean containsPosition(String word, String location, int position) {
		return viewPositions(word, location).contains(position);
	}

	/**
	 * Returns the number of words in the index
	 * 
	 * @return the number of words in the index
	 */
	public int numWords() {
		return viewWords().size();
	}

	/**
	 * Returns the number of locations for the word in the index
	 * 
	 * @param word the word in the index
	 * @return the number of locations for the word in the index
	 */
	public int numLocations(String word) {
		return viewLocations(word).size();
	}

	/**
	 * Returns the number of positions at the location for the word in the index
	 * 
	 * @param word the word in the index
	 * @param location the location in the index
	 * @return the number of positions at the location for the word in the index
	 */
	public int numPositions(String word, String location) {
		return viewPositions(word, location).size();
	}

	/**
	 * Returns the size of the index, meaning how many words are in the index
	 * 
	 * @return the number of words in the index
	 */
	public int size() {
		return index.size();
	}

	@Override
	public String toString() {
		return "Counts: " + JsonWriter.writeObject(counts) + "\nIndex: " + JsonWriter.writeObjectObjects(index);
	}

	/**
	 * Finds search results
	 * 
	 * @param queries the queries to search for
	 * @param partial whether to run partial or exact search
	 * @return the search results
	 */
	public List<Result> search(Set<String> queries, boolean partial) {
		if (partial) {
			return partialSearch(queries);
		}
		return exactSearch(queries);
	}

	/**
	 * Finds exact search results
	 * 
	 * @param queries the queries to search for
	 * @return the search results
	 */
	public List<Result> exactSearch(Set<String> queries) {
		List<Result> results = new ArrayList<>();
		Map<String, Result> lookup = new HashMap<>();
		for (String query : queries) {
			createResults(query, results, lookup);
		}
		Collections.sort(results);
		return results;
	}

	/**
	 * Finds partial search results
	 * 
	 * @param queries the queries to search for
	 * @return the search results
	 */
	public List<Result> partialSearch(Set<String> queries) {
		List<Result> results = new ArrayList<>();
		Map<String, Result> lookup = new HashMap<>();
		for (String query : queries) {
			for (String key : index.tailMap(query).keySet()) {
				if (!key.startsWith(query)) {
					break;
				}
				createResults(key, results, lookup);
			}
		}
		Collections.sort(results);
		return results;
	}

	/**
	 * A helper function for searching
	 * 
	 * @param query the word stem to search for
	 * @param results the Results list to add to
	 * @param lookup the map to keep track of which locations have been stored in results
	 */
	private void createResults(String query, List<Result> results, Map<String, Result> lookup) {
		TreeMap<String, TreeSet<Integer>> locations = index.get(query);
		if (locations != null) {
			for (Entry<String, TreeSet<Integer>> entry : locations.entrySet()) {
				String location = entry.getKey();
				Result result = lookup.get(location);
				if (result == null) {
					result = new Result(location);
					results.add(result);
					lookup.put(location, result);
				}
				result.update(entry.getValue().size());
			}
		}
	}

	/**
	 * Custom class for each result
	 */
	public class Result implements Comparable<Result> {

		/**
		 * The percent of words in the file that match the query
		 */
		private double score;

		/**
		 * The total number of matches found for the current result
		 */
		private int count;

		/**
		 * The location of the Result
		 */
		private final String location;

		/**
		 * Constructs a Result
		 * 
		 * @param file the location
		 */
		public Result(String file) {
			count = 0;
			score = 0;
			location = file;
		}

		/**
		 * Getter for score
		 * @return the score
		 */
		public double getScore() {
			return this.score;
		}

		/**
		 * Getter for count
		 * @return the count
		 */
		public int getCount() {
			return this.count;
		}

		/**
		 * Getter for location
		 * @return the location
		 */
		public String getLocation() {
			return this.location;
		}

		/**
		 * Resets the count to the new given count and recalculates the score
		 * 
		 * @param count the new count
		 */
		private void update(int count) {
			this.count = count + this.count;
			this.score = (double) this.count/counts.get(location);
		}

		@Override
		public String toString() {
			return this.location;
		}

		@Override
		public int compareTo(Result o) {
			if(Double.compare(o.getScore(), this.score) == 0) {
				if(Integer.compare(o.getCount(), this.count) == 0) {
					return this.location.compareToIgnoreCase(o.getLocation());
				}
				return Integer.compare(o.getCount(), this.count);
			}
			return Double.compare(o.getScore(), this.score);
		}

	}
}
