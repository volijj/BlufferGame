package Reactor;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;

import tokenizer.MessageTokenizer;
import tokenizer.StringMessage;
import protocols.GameServerProtocol;

/**
 * This class supplies some data to the protocol, which then processes the data,
 * possibly returning a reply. This class is implemented as an executor task.
 * 
 */
public class ProtocolTask implements Runnable {

	private final GameServerProtocol _protocol;
	private final MessageTokenizer<StringMessage> _tokenizer;
	private final ConnectionHandler _handler;

	public ProtocolTask(final GameServerProtocol protocol, final MessageTokenizer<StringMessage> tokenizer,
			final ConnectionHandler h) {
		this._protocol = protocol;
		this._tokenizer = tokenizer;
		this._handler = h;
	}

	// we synchronize on ourselves, in case we are executed by several threads
	// from the thread pool.
	public synchronized void run() {
		// go over all complete messages and process them.
		while (_tokenizer.hasMessage()) {
			String msg = _tokenizer.nextMessage().toString();

			System.out.println("Received \"" + msg.toString() + "\" from client");
			_protocol.processMessage(msg.toString(), v -> {
				if (v != null) {
					try {
						StringMessage ans = new StringMessage(v);
						ByteBuffer bytes = _tokenizer.getBytesForMessage(ans);
						this._handler.addOutData(bytes);
					} catch (CharacterCodingException e) {
						e.printStackTrace();
					}
				}
			});

		}
	}

	public void addBytes(ByteBuffer b) {
		_tokenizer.addBytes(b);
	}
}