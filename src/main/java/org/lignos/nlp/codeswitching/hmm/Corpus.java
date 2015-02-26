package org.lignos.nlp.codeswitching.hmm;

import org.lignos.nlp.sequence.MalformedSequenceException;
import org.lignos.nlp.sequence.Sequence;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Corpus  implements Iterable<Sequence> {

	protected List<Sequence> sequences;

	public Corpus(String path, boolean keepPunc) throws IOException {
		sequences = new LinkedList<Sequence>();
		loadCorpus(path, keepPunc);
	}

	/**
	 * Create a hash key that can uniquely identify a state transition
	 * @param state1 first state
	 * @param state2 second state
	 * @return key representing the transition
	 */
	protected static String transKey(String state1, String state2) {
		return state1 + ":" + state2;
	}

	protected void loadCorpus(String path, boolean keepPunc) throws IOException {
		BufferedReader lineReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
		String line;
		int lineNum = 0;
		while ((line = lineReader.readLine()) != null) {
			lineNum++;
			try {
				sequences.add(new Sequence(line, keepPunc));
			}
			catch (MalformedSequenceException e) {
				System.err.println("Bad utterance at line " + lineNum);
				System.err.println("Line: " + line);
				System.err.println("Reason: " + e.getMessage());
			}
		}
		lineReader.close();
	}

	public int size() {
		return sequences.size();
	}

	@Override
	public Iterator<Sequence> iterator() {
		return sequences.iterator();
	}



}
