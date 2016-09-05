package Games;

import Interfaces.GameFactory;

public class BlufferFactory implements GameFactory {

	
	public Game create() {
		
		return new Bluffer();
	}

}
