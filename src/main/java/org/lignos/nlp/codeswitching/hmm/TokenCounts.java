package org.lignos.nlp.codeswitching.hmm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Representation of tokens and their frequencies. All tokens are normalized to lowercase form.
 */
public class TokenCounts {
	private Map<String, Integer> wordCounts;
	private int totalCount;

    /**
     * Initialize counts from a file where each line contains a token and its frequency.
     *
     * @param path to the file
     * @throws IOException if the file cannot be opened
     */
	public TokenCounts(String path) throws IOException{
		wordCounts = new HashMap<String, Integer>();
		loadTokenCounts(path);
	}

    /** Load token counts from a file.
     *
     * @param path the path of the file
     * @throws IOException if the file cannot be opened
     */
    private void loadTokenCounts(String path) throws IOException {
        // Open the word list and get each word
        BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF8"));

        // Parse all lines
        String line;
        while ((line = input.readLine()) != null) {
            // Format is: word frequency
            // Parse the line, report if parsing fails
            String[] parts = line.split("\\s");
            if (parts.length != 2) {
                System.err.println("Couldn't parse line: " + line);
                continue;
            }

            // Parse the second item as a count
            try {
                int count = Integer.parseInt(parts[1]);
                incrementCount(parts[0], count);
            }
            catch (NumberFormatException e) {
                System.err.println("Couldn't parse count: " + parts[0]);
                continue;
            }
        }
        input.close();
    }

    /**
     * Increment the count for the specified token, creating a new entry for the token if this is
     * the first time it is counted.
     * @param token the token
     * @param count the count
     */
	public void incrementCount(String token, int count){
		wordCounts.put(token, wordCounts.getOrDefault(token.toLowerCase(), 0) + count);
		totalCount += count;
	}

    /**
     * Return the raw count for a token.
     * @param token the token
     * @return the count
     */
	public int getCount(String token) {
		Integer count = wordCounts.get(token.toLowerCase());
		return count != null ? count : 0;
	}

    /**
     * Return the frequency (count / total count) for a token.
     * @param token the token
     * @return the count
     */
    public double getFreq(String token) {
		return getCount(token.toLowerCase()) / (double) totalCount;
	}

    /**
     * Return the total occurrences of tokens counted.
     * @return the token count
     */
	public int nTokens() {
		return totalCount;
	}

    /**
     * Return the number of types (unique tokens) counted.
     * @return the type count
     */
	public int nTypes() {
		return wordCounts.size();
	}

    /**
     * Return the set of all tokens counted.
     * @return the set of all tokens
     */
    public Set<String> tokenSet() {
        return wordCounts.keySet();
    }
}
