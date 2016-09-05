package protocols;
import Interfaces.ServerProtocol;
import Interfaces.ServerProtocolFactory;

public class GameServerProtocolFactory implements ServerProtocolFactory {

	@Override
	public ServerProtocol create() {
		// TODO Auto-generated method stub
		
		return new GameServerProtocol();
	}

}
