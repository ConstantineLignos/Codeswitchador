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

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represent a message consisting of tokens where the entire message has a single tag.
 */
public class TokenMessage implements Iterable<TokenTag> {
	protected final List<TokenTag> tokens;
    protected final String label;

    /**
     * Create a new sequence of tokens that share a single label.
     * @param text the text representing the sequence, in the form tag\tw1 w2 w3
     * @throws org.lignos.nlp.sequence.MalformedSequenceException if the sequence cannot be parsed
     */
	public TokenMessage(String text) throws MalformedSequenceException {
		tokens = new ArrayList<TokenTag>();
		loadTokens(text);
        label = null;
    }

	private void loadTokens(String text) throws MalformedSequenceException {
        // TODO: Implement
        throw new NotImplementedException();
	}

	public int size() {
		return tokens.size();
	}

	public TokenTag get(int index) {
		return tokens.get(index);
	}

    public String getLabel() {
        return this.label;
    }

	@Override
	public Iterator<TokenTag> iterator() {
		return tokens.iterator();
	}

	public String toString() {
		return this.tokens.toString();
	}
}
