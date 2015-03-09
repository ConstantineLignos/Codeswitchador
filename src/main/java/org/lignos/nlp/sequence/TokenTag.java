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

/**
 * Represents a (token, tag) pair.
 */
public class TokenTag {

    /** The text of the token */
    public final String token;
    /** The tag of the token */
    public final String tag;
    /** Any comments for the token, often used for dash tags */
    public final String comment;

    /** Initialize a TokenTag with the specified token, tag, and comment. */
    public TokenTag(String token, String tag, String comment) {
        this.token = token;
        this.tag = tag;
        this.comment = comment;
    }

    public String toString() {
		return this.token + "/" + this.tag + (this.comment != null ? "-" + this.comment : "");
	}
}
