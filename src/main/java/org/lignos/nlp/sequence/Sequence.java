package org.lignos.nlp.sequence;

/**
 * Copyright 2012-2015 Constantine Lignos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represent a sequence of tagged tokens.
 */
public class Sequence implements Iterable<TokenTag> {
	protected static final String NON_TAG = "unk";
	protected static final Pattern statePattern = Pattern.compile("^(.+)/(\\w+)(?:-(\\w+))?$");
	protected final List<TokenTag> tokens;
    protected final String[] labels;

    /**
     * Create a new sequence of tagged tokens.
     * @param text the text representing the sequence, in the form w1/tag1 w2/tag2
     * @param ignoreTags tags marking tokens that should be ignored
     * @throws MalformedSequenceException if the sequence cannot be parsed
     */
	public Sequence(String text, Set<String> ignoreTags) throws MalformedSequenceException {
		tokens = new ArrayList<TokenTag>();
		loadTokens(text, ignoreTags);
        labels = new String[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            labels[i] = tokens.get(i).tag;
        }
    }

	private void loadTokens(String text, Set<String> ignoreTags) throws MalformedSequenceException {
		String[] tokenTags = text.split(" ");
		for (String tokenTag : tokenTags) {
			Matcher stateMatcher = statePattern.matcher(tokenTag);
			if (!stateMatcher.matches()) {
				System.err.println("Couldn't match token: '" + tokenTag + "'");
				continue;
			}
            // Extract matches. Note that comment will be null if there is no dash tag.
			String token = stateMatcher.group(1);
			String tag = stateMatcher.group(2);
            String comment = stateMatcher.group(3);

            // Add the token to the sequence if it does not have a tag to be ignored
            if (ignoreTags == null || !ignoreTags.contains(tag)) {
                tokens.add(new TokenTag(token, tag, comment));
            }
        }
	}

	public int size() {
		return tokens.size();
	}

	public TokenTag get(int index) {
		return tokens.get(index);
	}

    public String[] getLabels() {
        return this.labels;
    }

	@Override
	public Iterator<TokenTag> iterator() {
		return tokens.iterator();
	}

	public String toString() {
		return this.tokens.toString();
	}
}
