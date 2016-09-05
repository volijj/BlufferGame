package main.java.Games;

import main.java.Databases.GameRoom;

public abstract class Game {
	
	public abstract void load(GameRoom room);
	public abstract String acceptTXT(String name, String msg);
	public abstract void endGame();
	public abstract void acceptRESP(int choice, String nickname);
		
		
}

