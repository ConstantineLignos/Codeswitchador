package org.lignos.nlp.sequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sequence implements Iterable<TokenState> {
	protected static final String PUNC_TAG = "n";
	protected static final Pattern statePattern = Pattern.compile("(.+)/(.+?)");
	protected final List<TokenState> tokens;
    protected final String[] labels;

	public Sequence(String text, boolean keepPunc) throws MalformedSequenceException {
		tokens = new ArrayList<TokenState>();
		loadTokens(text, keepPunc);
        labels = new String[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            labels[i] = tokens.get(i).state;
        }
    }

	private void loadTokens(String text, boolean keepPunc) throws MalformedSequenceException {
		String[] tokenStates = text.split(" ");
		for (String tokenState : tokenStates) {
			Matcher stateMatcher = statePattern.matcher(tokenState);
			if (!stateMatcher.matches()) {
				System.err.println("Couldn't match token: '" + tokenState + "'");
				continue;
			}
			String token = stateMatcher.group(1);
			String tag = stateMatcher.group(2);
            String comment = tag.contains("t") ? "t" : (tag.contains("p") ? "p" : null);
			// Replace entity marks on tag
			tag = tag.replaceAll("t", "");
			tag = tag.replaceAll("p", "");
			if (tag.length() != 1) {
				throw new MalformedSequenceException("Tag is of unexpected length: " + tag);
			}
			else if (!keepPunc && tag.equals(PUNC_TAG)) {
				continue;
			}

			tokens.add(new TokenState(token, tag, comment));
		}
	}

	public int size() {
		return tokens.size();
	}

	public TokenState get(int index) {
		return tokens.get(index);
	}

    public String[] getLabels() {
        return this.labels;
    }

	@Override
	public Iterator<TokenState> iterator() {
		return tokens.iterator();
	}

	public String toString() {
		return this.tokens.toString();
	}
}
