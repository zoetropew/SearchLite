package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using spaces.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class JsonWriter {
	/**
	 * Indents the writer by the specified Object of times. Does nothing if the
	 * indentation level is 0 or less.
	 *
	 * @param writer the writer to use
	 * @param indent the Object of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(Writer writer, int indent) throws IOException {
		while (indent-- > 0) {
			writer.write("  ");
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the Object of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write(element);
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the Object of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQuote(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeArray(Collection<? extends Object> elements, Writer writer, int indent) throws IOException {
		writer.write("[\n");
		Iterator<? extends Object> iterator = elements.iterator();
		if(iterator.hasNext()) {
			writeIndent(iterator.next().toString(), writer, indent + 1);
			while(iterator.hasNext()) {
				writer.write(",\n");
				writeIndent(iterator.next().toString(), writer, indent + 1);
			}
			writer.write("\n");
		}
		writeIndent("]", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static void writeArray(Collection<? extends Object> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static String writeArray(Collection<? extends Object> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArray(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Helper function for writeObject
	 * 
	 * @param element the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObjectEntry(Map.Entry<String, ? extends Object> element, Writer writer, int indent) throws IOException {
		writer.write("\n");
		writeQuote(element.getKey(), writer, indent+1);
		writer.write(": " + element.getValue());
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Object> elements, Writer writer, int indent) throws IOException {
		var iterator = elements.entrySet().iterator();	
		writer.write("{");
		if(iterator.hasNext()) {
			writeObjectEntry(iterator.next(), writer, indent);
			while(iterator.hasNext()) {
				writer.write(",");
				writeObjectEntry(iterator.next(), writer, indent);
			}
		}
		writer.write("\n");
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObject(Map, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Object> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObject(Map, Writer, int)
	 */
	public static String writeObject(Map<String, ? extends Object> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObject(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Helper function for writeObjectArray
	 * 
	 * @param element the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObjectArrayEntry(Map.Entry<String, ? extends Collection<? extends Object>> element, Writer writer, int indent) throws IOException {
		writer.write("\n");
		writeQuote(element.getKey(), writer, indent+1);
		writer.write(": ");
		writeArray(element.getValue(), writer, indent+1);
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of Object objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeArray(Collection)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Object>> elements, Writer writer,
			int indent) throws IOException {	
		var iterator = elements.entrySet().iterator();
		writer.write("{");
		if(iterator.hasNext()) {
			writeObjectArrayEntry(iterator.next(), writer, indent);
			while(iterator.hasNext()) {
				writer.write(",");
				writeObjectArrayEntry(iterator.next(), writer, indent);
			}
		}
		writer.write("\n");
		writeIndent("}", writer, indent);		
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Object>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectArrays(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static String writeObjectArrays(Map<String, ? extends Collection<? extends Object>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObjectArrays(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Helper function for writeArrayObjects
	 * 
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeArrayObjectEntry(Map<String, ? extends Object> elements, Writer writer, int indent) throws IOException {
		writer.write("\n");
		writeIndent(writer, indent + 1);
		writeObject(elements, writer, indent + 1);
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects. The generic
	 * notation used allows this method to be used for any type of collection with
	 * any type of nested map of String keys to Object objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeObject(Map)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Object>> elements, Writer writer,
			int indent) throws IOException {
		Iterator<? extends Map<String, ? extends Object>> iterator = elements.iterator();	
		writer.write("[");
		if (iterator.hasNext()) {
			writeArrayObjectEntry(iterator.next(), writer, indent);
			while (iterator.hasNext()) {
				writer.write(",");
				writeArrayObjectEntry(iterator.next(), writer, indent);
			}
		}
		writer.write("\n");
		writeIndent("]", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArrayObjects(Collection)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Object>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArrayObjects(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array with nested objects.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArrayObjects(Collection)
	 */
	public static String writeArrayObjects(Collection<? extends Map<String, ? extends Object>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArrayObjects(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Helper function for writeObjectObjects
	 * 
	 * @param element the elements to use
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   elements within are indented to be nested, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObjectObjectEntry(Entry<String, ? extends Map<String, ? extends Collection<? extends Object>>> element, Writer writer, int indent) throws IOException {
		writer.write("\n");
		writeQuote(element.getKey(), writer, indent+1);
		writer.write(": ");
		writeObjectArrays(element.getValue(), writer, indent+1);
	}

	/**
	 * Writes the elements as a pretty JSON object with nested objects. The generic
	 * notation used allows this method to be used for any type of map of String keys 
	 * to any type of nested map of String keys to any type of Collection of Object objects.
	 * 
	 * @param elements the elements to use
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   elements within are indented to be nested, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObjectObjects(Map<String, ? extends Map<String, ? extends Collection<? extends Object>>> elements, Writer writer, int indent) throws IOException {
		var iterator = elements.entrySet().iterator();
		writer.write("{");
		if(iterator.hasNext()) {
			writeObjectObjectEntry(iterator.next(), writer, indent);
			while(iterator.hasNext()) {
				writer.write(",");
				writeObjectObjectEntry(iterator.next(), writer, indent);
			}
		}
		writer.write("\n");
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects to file.
	 * 
	 * @param index the elements to use
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObjectObjects(Map<String, ? extends Map<String, ? extends Collection<? extends Object>>> index, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectObjects(index, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array with nested objects.
	 * 
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 */
	public static String writeObjectObjects(Map<String, ? extends Map<String, ? extends Collection<? extends Object>>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObjectObjects(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 */
	public static void writeArrayResults(Collection<InvertedIndex.Result> elements, Writer writer, int indent) throws IOException {
		writer.write("[\n");
		var iterator = elements.iterator();
		if(iterator.hasNext()) {
			writeResult(iterator.next(), writer, indent + 1);
			while(iterator.hasNext()) {
				writer.write(",\n");
				writeResult(iterator.next(), writer, indent + 1);
			}
			writer.write("\n");
		}
		JsonWriter.writeIndent("]", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static void writeArrayResults(Collection<InvertedIndex.Result> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArrayResults(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static String writeArrayResults(Collection<InvertedIndex.Result> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArrayResults(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Helper function for writeObjectArray
	 * 
	 * @param element the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObjectArrayResultsEntry(Map.Entry<String, ? extends Collection<InvertedIndex.Result>> element, Writer writer, int indent) throws IOException {
		writer.write("\n");
		JsonWriter.writeQuote(element.getKey(), writer, indent+1);
		writer.write(": ");
		writeArrayResults(element.getValue(), writer, indent+1);
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of Object objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeArray(Collection)
	 */
	public static void writeObjectArrayResults(Map<String, ? extends Collection<InvertedIndex.Result>> elements, Writer writer,
			int indent) throws IOException {	
		var iterator = elements.entrySet().iterator();
		writer.write("{");
		if(iterator.hasNext()) {
			writeObjectArrayResultsEntry(iterator.next(), writer, indent);
			while(iterator.hasNext()) {
				writer.write(",");
				writeObjectArrayResultsEntry(iterator.next(), writer, indent);
			}
		}
		writer.write("\n");
		JsonWriter.writeIndent("}", writer, indent);		
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static void writeObjectArrayResults(Map<String, ? extends Collection<InvertedIndex.Result>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectArrayResults(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static String writeObjectArrayResults(Map<String, ? extends Collection<InvertedIndex.Result>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObjectArrayResults(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Helper function for writeObject
	 * @param category the category type
	 * 
	 * @param element the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeResultEntry(String category, Object element, Writer writer, int indent) throws IOException {
		writer.write("\n");
		JsonWriter.writeQuote(category, writer, indent+1);
		writer.write(": " + element);
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at the
	 *   initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 */
	public static void writeResult(InvertedIndex.Result elements, Writer writer, int indent) throws IOException {
		JsonWriter.writeIndent("{", writer, indent);
		writeResultEntry("count", elements.getCount(), writer, indent);
		writer.write(",");
		String score = String.format("%.8f", elements.getScore());
		writeResultEntry("score", score, writer, indent);
		writer.write(",");
		writeResultEntry("where", "\"" + elements.getLocation() + "\"", writer, indent);
		writer.write("\n");
		JsonWriter.writeIndent("}", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 */
	public static void writeResult(InvertedIndex.Result elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeResult(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 */
	public static String writeResult(InvertedIndex.Result elements) {
		try {
			StringWriter writer = new StringWriter();
			writeResult(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}
}
