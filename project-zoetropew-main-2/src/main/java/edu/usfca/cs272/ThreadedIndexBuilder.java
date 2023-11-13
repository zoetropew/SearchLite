package edu.usfca.cs272;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Thread safe version of InvertedIndexBuilder
 * 
 * @author zoe
 *
 */
public class ThreadedIndexBuilder {

	/**
	 * Called in driver to begin building the inverted index. Calls traverseDirectory if the path
	 * is a directory, processFile if not.
	 * 
	 * @param path the path to traverse
	 * @param storage the data structure where file info will be stored
	 * @param queue the work queue
	 * @throws IOException if an IO error occurs
	 */
	public static void build(Path path, ThreadedIndex storage, WorkQueue queue) throws IOException {
		if(Files.isDirectory(path)) {
			traverseDirectory(path, storage, queue);
		}
		else {
			queue.execute(new ProcessFile(path, storage));
		}
		queue.finish();
	}

	/**
	 * Recursively traverses the given directory to find all text files, 
	 * unless the initial given -text path is not a directory.
	 * 
	 * @param path the path to traverse
	 * @param storage the data structure where file info will be stored
	 * @param queue the work queue
	 * @throws IOException if an IO error occurs
	 */
	public static void traverseDirectory(Path path, ThreadedIndex storage, WorkQueue queue) throws IOException {
		try (DirectoryStream<Path> walk = Files.newDirectoryStream(path)){
			for(Path thisPath:walk) {
				if(Files.isDirectory(thisPath)) {
					traverseDirectory(thisPath, storage, queue);
				}
				else if(InvertedIndexBuilder.isTextFile(thisPath)){
					queue.execute(new ProcessFile(thisPath, storage));
				}
			}
		}
	}

	/**
	 * A task for processing an individual file
	 * 
	 * @author zoe
	 *
	 */
	private static class ProcessFile implements Runnable {
		/**
		 * The file to read
		 */
		private final Path file;
		/**
		 * The ThreadedIndex to store the information in
		 */
		private final ThreadedIndex storage;

		/**
		 * Constructor for this task
		 * 
		 * @param file the file to read
		 * @param storage the ThreadedIndex to store the information in
		 */
		public ProcessFile(Path file, ThreadedIndex storage) {
			this.file = file;
			this.storage = storage;
		}

		@Override
		public void run() {
			try {
				InvertedIndex local = new InvertedIndex();
				InvertedIndexBuilder.processFile(file, local);
				storage.addAll(local);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
