package org.lignos.nlp.sequence;

public class TokenState {

    public final String token;
    public final String state;
    public final String comment;

    public TokenState(String token, String state, String comment) {
        this.token = token;
        this.state = state;
        this.comment = comment;
    }

    public String toString() {
		return this.token + "/" + this.state;
	}
}
