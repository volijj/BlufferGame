package main.java.tokenizer;

public interface TokenizerFactory<T> {
	   MessageTokenizer<T> create();
	}