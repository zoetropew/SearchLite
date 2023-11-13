package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Processes files and builds an InvertedIndex of word stems, 
 * file locations, positions in files, and word counts.
 * 
 * @author Zoe Wong
 */
public class InvertedIndexBuilder {

	/**
	 * Called in driver to begin building the inverted index. Calls traverseDirectory if the path
	 * is a directory, processFile if not.
	 * 
	 * @param path the path to traverse
	 * @param storage the data structure where file info will be stored
	 * @throws IOException if an IO error occurs
	 */
	public static void build(Path path, InvertedIndex storage) throws IOException {
		if(Files.isDirectory(path)) {
			traverseDirectory(path, storage);
		}
		else {
			processFile(path, storage);
		}
	}

	/**
	 * Processes a file and builds the word counts and word index
	 * 
	 * @param file the file to process
	 * @param storage the data structure where file info will be stored
	 * @throws IOException if an IO error occurs
	 */
	public static void processFile(Path file, InvertedIndex storage) throws IOException {
		try(BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)){
			Stemmer stemmer = new SnowballStemmer(ENGLISH);
			String location = file.toString();
			String line;
			int position = 0;
			while ((line = br.readLine()) != null) {
				String[] words = FileStemmer.parse(line);
				for (String word : words) {
					storage.addIndexEntry(stemmer.stem(word).toString(), location, ++position);
				}
			}
		}
	}

	/**
	 * Recursively traverses the given directory to find all text files, 
	 * unless the initial given -text path is not a directory.
	 * 
	 * @param path the path to traverse
	 * @param storage the data structure where file info will be stored
	 * @throws IOException if an IO error occurs
	 */
	public static void traverseDirectory(Path path, InvertedIndex storage) throws IOException {
		try (DirectoryStream<Path> walk = Files.newDirectoryStream(path)){
			for(Path thisPath:walk) {
				if(Files.isDirectory(thisPath)) {
					traverseDirectory(thisPath, storage);
				}
				else if(isTextFile(thisPath)){
					processFile(thisPath, storage);
				}
			}
		}
	}

	/**
	 * Checks if a path is a .txt or .text file
	 * 
	 * @param path the file to check
	 * @return whether the path is a .txt or .text file
	 */
	public static boolean isTextFile(Path path) {
		String checkEnding = path.toString().toLowerCase();
		return checkEnding.endsWith(".txt") || checkEnding.endsWith(".text");
	}

}
