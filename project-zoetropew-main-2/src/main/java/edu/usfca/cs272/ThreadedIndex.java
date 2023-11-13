package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A thread safe version of the InvertedIndex
 * 
 * @author zoe
 *
 */
public class ThreadedIndex extends InvertedIndex {

	/** The lock used to protect concurrent access to the index and counts. */
	private final MultiReaderLock lock;

	/**
	 * Constructor that includes a lock
	 */
	public ThreadedIndex() {
		super();
		lock = new MultiReaderLock();
	}

	@Override
	public Map<String, Integer> viewCounts(){
		lock.readLock().lock();
		try {
			return super.viewCounts();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> viewWords(){
		lock.readLock().lock();
		try {
			return super.viewWords();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> viewLocations(String word){
		lock.readLock().lock();
		try {
			return super.viewLocations(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<Integer> viewPositions(String word, String location) {
		lock.readLock().lock();
		try {
			return super.viewPositions(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void writeCounts(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.writeCounts(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void writeIndex(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.writeIndex(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void addIndexEntry(String word, String location, int position) {
		lock.writeLock().lock();
		try {
			super.addIndexEntry(word, location, position);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void addAll(List<String> words, String location) {
		lock.writeLock().lock();
		try {
			super.addAll(words, location);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void addAll(InvertedIndex storage) {
		lock.writeLock().lock();
		try {
			super.addAll(storage);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public boolean containsWord(String word) {
		lock.readLock().lock();
		try {
			return super.containsWord(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean containsLocation(String word, String location) {
		lock.readLock().lock();
		try {
			return super.containsLocation(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean containsPosition(String word, String location, int position) {
		lock.readLock().lock();
		try {
			return super.containsPosition(word, location, position);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int numWords() {
		lock.readLock().lock();
		try {
			return super.numWords();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int numLocations(String word) {
		lock.readLock().lock();
		try {
			return super.numLocations(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int numPositions(String word, String location) {
		lock.readLock().lock();
		try {
			return super.numPositions(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int size() {
		lock.readLock().lock();
		try {
			return super.size();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String toString() {
		lock.readLock().lock();
		try {
			return super.toString();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public List<Result> exactSearch(Set<String> queries) {
		lock.readLock().lock();
		try {
			return super.exactSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public List<Result> partialSearch(Set<String> queries) {
		lock.readLock().lock();
		try {
			return super.partialSearch(queries);
		} finally {
			lock.readLock().unlock();
		}
	}

}
