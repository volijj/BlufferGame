package Databases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import Games.Game;
import Interfaces.ProtocolCallback;

public class GameRoom {
	// this class will hold all the relevant data of a game room.
	// the names of all the players are in a queue
	// maybe we will need to have an object player
	ArrayList<String> players;
	// the names of the players with their current points
	ConcurrentHashMap<String, Integer> playerPoints;
	// THIS MAP SAVES THE CALLBACKS OF THE CLIENTS CURRENTLY IN THE ROOM
	public ConcurrentHashMap<String, ProtocolCallback<String>> clientCallbacks;
	// the name of the gameRoom that was defined by the first user.
	private String name;
	Game game;

	// indicates if the game has started and cannot add new players
	boolean started;

	public GameRoom(String name) {
		this.name = name;
		players = new ArrayList<String>();
		playerPoints = new ConcurrentHashMap<String, Integer>();
		clientCallbacks = new ConcurrentHashMap<String, ProtocolCallback<String>>();
		started = false;
		game = null;
	}

	// adds the player to the room. if the game has started returns false
	public synchronized void addPlayer(String nickname, ProtocolCallback v) {
		players.add(nickname);
		if (!this.clientCallbacks.containsKey(nickname))
			this.clientCallbacks.put(nickname, v);
		playerPoints.put(nickname, new Integer(0));
	}

	public synchronized boolean leaveRoom(String nickname) {
		if (this.isAvailable()) {
			players.remove(nickname);
			try {
				clientCallbacks.get(nickname).sendMessage(nickname + " has left the room" + " " + this.name);
			} catch (IOException e) {
				e.printStackTrace();
			}
			clientCallbacks.remove(nickname);
			playerPoints.remove(nickname);
			return true;
		}
		return false;
	}

	// in order to leave a room in the middle of a game.
	public synchronized void leaveRoom2(String nickname) {

		players.remove(nickname);
		try {
			clientCallbacks.get(nickname).sendMessage(nickname + " has left the room" + " " + this.name);
		} catch (IOException e) {
			e.printStackTrace();
		}
		clientCallbacks.remove(nickname);
		playerPoints.remove(nickname);

	}

	public boolean isAvailable() {
		return !started;
	}

	public synchronized void sendChat(String message, String name) {
		for (String n : this.clientCallbacks.keySet()) {
			try {
				this.clientCallbacks.get(n).sendMessage("USERMSG " + name + ":" + message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void startGame() {
		started = true;
		// send messages to everyone that the game has started
		for (String n : this.clientCallbacks.keySet()) {
			try {
				this.clientCallbacks.get(n).sendMessage("GAMEMSG " + game.toString() + " has Started!");
			} catch (IOException e) {
				e.printStackTrace();
			}
			game.load(this);
		}

	}

	public void endGame() {
		started = false;
	}

	public String getName() {
		return name;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public String sendTXT(String name, String msg2) {
		return game.acceptTXT(name, msg2);

	}
	public void sendRESP(Integer choice,String nickname){
		this.game.acceptRESP(choice,nickname);
	}

}
