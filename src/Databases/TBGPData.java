package Databases;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import Games.BlufferFactory;
import Interfaces.GameFactory;

//this singleton handles the data of all the games that are going on.
public class TBGPData {

	public ConcurrentHashMap<String, GameFactory> games;
	ArrayList<String> names;
	public ConcurrentHashMap<String, GameRoom> rooms;

	private static class TBGPDataHolder {
		private static TBGPData instance = new TBGPData();
	}

	public static TBGPData getInstance() {
		return TBGPDataHolder.instance;
	}

	private TBGPData() {
		names = new ArrayList<String>();
		rooms = new ConcurrentHashMap<String, GameRoom>();
		games = new ConcurrentHashMap<String, GameFactory>();
		games.put("bluffer", new BlufferFactory());
	}

	public synchronized boolean addPlayer(String name) {
		if (names.contains(name)) {
			return false;
		} else {
			names.add(name);
		}
		return true;
	}

	public synchronized void addRoom(GameRoom room) {
		rooms.put(room.getName(), room);
	}
	
	

	public String printGames() {
		String ans = "";
		if (games.isEmpty()) {
			ans = "No games currently available on this server";
		} else {
			for (String n : this.games.keySet()) {
				ans = ans + "[" + n + "]";
			}
		}
		return ans;

	}
	
	public synchronized void removePlayer(String name){
		names.remove(name);
	}
	
	public synchronized void addGame(String gameName, GameFactory f) {
		games.put(gameName.toLowerCase(), f);
	}

}
