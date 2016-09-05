package main.java.Reactor;

import java.nio.channels.Selector;
import java.util.concurrent.ExecutorService;

import main.java.Interfaces.ServerProtocolFactory;
import main.java.tokenizer.StringMessage;
import main.java.tokenizer.TokenizerFactory;

/**
 * a simple data structure that hold information about the reactor, including getter methods
 */
public class ReactorData {

    private final ExecutorService _executor;
    private final Selector _selector;
    private final ServerProtocolFactory _protocolMaker;
    private final TokenizerFactory<StringMessage> _tokenizerMaker;
    
    public ExecutorService getExecutor() {
        return _executor;
    }

    public Selector getSelector() {
        return _selector;
    }

	public ReactorData(ExecutorService _executor, Selector _selector, ServerProtocolFactory protocol, TokenizerFactory<StringMessage> tokenizer) {
		this._executor = _executor;
		this._selector = _selector;
		this._protocolMaker = protocol;
		this._tokenizerMaker = tokenizer;
	}

	public ServerProtocolFactory getProtocolMaker() {
		return _protocolMaker;
	}

	public TokenizerFactory<StringMessage> getTokenizerMaker() {
		return _tokenizerMaker;
	}

}