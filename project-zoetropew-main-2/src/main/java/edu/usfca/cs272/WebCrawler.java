package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Builds the inverted index from web pages
 * 
 * @author zoe
 *
 */
public class WebCrawler {

	/**
	 * Already crawled web pages
	 */
	private final Collection<URL> crawled;

	/**
	 * How many web pages to crawl
	 */
	private int crawl;

	/**
	 * The work queue
	 */
	private final WorkQueue queue;

	/**
	 * The ThreadedIndex to store the information in
	 */
	private final ThreadedIndex storage;

	/**
	 * Constructor
	 * 
	 * @param queue the work queue
	 * @param storage the ThreadedIndex to store the information in
	 */
	public WebCrawler(WorkQueue queue, ThreadedIndex storage) {
		this.crawled = new HashSet<>();
		this.crawl = 0;
		this.queue = queue;
		this.storage = storage;
	}

	/**
	 * Returns an unmodifiable view of the crawled URLs
	 * 
	 * @return an unmodifiable view of the crawled URLs
	 */
	public Collection<URL> getCrawled() {
		return Collections.unmodifiableCollection(crawled);
	}

	/**
	 * Builds the index from the given seed and the number of pages to crawl
	 * 
	 * @param seed the url for the web page
	 * @param crawl the number of pages to crawl
	 * @throws MalformedURLException if a MalformedURLException occurs
	 * @throws URISyntaxException if a URISyntaxException occurs
	 */
	public void build(String seed, int crawl) throws MalformedURLException, URISyntaxException {
		if (crawl > 1) {
			this.crawl = crawl;
			crawlPages(seed);
		} else {
			singlePage(seed);
		}
	}

	/**
	 * Add words from the seed page to the index.
	 * 
	 * @param seed the url for the web page
	 * @param html the cleaned html from the seed
	 */
	private void addToIndex(String seed, String html) {
		ArrayList<String> words = new ArrayList<>();
		Stemmer stemmer = new SnowballStemmer(ENGLISH);
		int position = 0;
		FileStemmer.addStems(html, stemmer, words);
		for (String word : words) {
			storage.addIndexEntry(word, seed, ++position);
		}
	}

	/**
	 * Adds all words from this web page to the inverted index
	 * 
	 * @param seed the url for the web page
	 * @throws MalformedURLException if a MalformedURLException occurs
	 * @throws URISyntaxException if a URISyntaxException occurs
	 */
	private void singlePage(String seed) throws MalformedURLException, URISyntaxException {
		String html = HtmlFetcher.fetch(seed, 3);
		if (html != null) {		
			addToIndex(seed, HtmlCleaner.stripHtml(html));
		}
	}

	/**
	 * Crawls the pages from the given seed and adds them to the inverted index
	 * 
	 * @param seed the url for the web page
	 * @throws MalformedURLException if a MalformedURLException occurs
	 * @throws URISyntaxException if a URISyntaxException occurs
	 */
	private void crawlPages(String seed) throws MalformedURLException, URISyntaxException {
		URL url = new URL(seed);
		crawled.add(url);
		crawl--;
		queue.execute(new Crawl(url));
		queue.finish();
	}

	/**
	 * A task for processing a web page
	 * 
	 * @author zoe
	 *
	 */
	private class Crawl implements Runnable {

		/**
		 * the seed url
		 */
		private final URL seed;

		/**
		 * Constructor for this task
		 * 
		 * @param seed the url
		 * @throws MalformedURLException if a MalformedURLException occurs
		 * @throws URISyntaxException if a URISyntaxException occurs
		 */
		public Crawl(URL seed) throws MalformedURLException, URISyntaxException {
			this.seed = LinkFinder.normalize(seed);
		}

		@Override
		public void run() {
			try {
				String html = HtmlFetcher.fetch(seed, 3);
				if (html != null) {
					html = HtmlCleaner.stripBlockElements(html);
					Collection<URL> urls = new LinkedHashSet<>();
					LinkFinder.findUrls(seed, html, urls);
					html = HtmlCleaner.stripEntities(HtmlCleaner.stripTags(html));
					for (URL url : urls) {
						if (crawl > 0) {
							synchronized (crawled) {
								if (crawled.contains(url)) {
									continue;
								}
								crawled.add(url);
							}
							crawl--;
							queue.execute(new Crawl(url));
						} else {
							break;
						}
					}
					addToIndex(seed.toString(), html);
				}
			} catch (MalformedURLException | URISyntaxException e) {
				System.out.println("Failed to read link: " + seed);
			}
		}
	}

}
