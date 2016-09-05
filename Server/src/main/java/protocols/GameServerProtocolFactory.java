package main.java.protocols;
import main.java.Interfaces.ServerProtocol;
import main.java.Interfaces.ServerProtocolFactory;

public class GameServerProtocolFactory implements ServerProtocolFactory {

	@Override
	public ServerProtocol create() {
		// TODO Auto-generated method stub
		
		return new GameServerProtocol();
	}

}
