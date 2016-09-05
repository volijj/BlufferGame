package main.java.Games;

import main.java.Interfaces.GameFactory;

public class BlufferFactory implements GameFactory {

	
	public Game create() {
		
		return new Bluffer();
	}

}
