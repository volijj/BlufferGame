package main.java.protocols;
import java.io.IOException;
import java.util.logging.Logger;

import main.java.Databases.GameRoom;
import main.java.Databases.TBGPData;
import main.java.Games.Game;
import main.java.Interfaces.GameFactory;
import main.java.Interfaces.ProtocolCallback;
import main.java.Interfaces.ServerProtocol;

public class GameServerProtocol implements ServerProtocol<String> {
	private boolean _shouldClose = false;
	boolean _connectionTerminated=false;
	String userNickname = null;
	GameRoom currentRoom = null;

	@Override
	public boolean isEnd(String msg) {
		return msg.equals("QUIT");
	}

	@Override
	public void processMessage(String msg, ProtocolCallback<String> callback) {
		String ans = processMessage_internal (msg, callback);
		
		if (ans == "")
			return;
		
		try {
			callback.sendMessage(ans);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private String processMessage_internal (String msg, ProtocolCallback<String> callback) {
		
		String ans = null;
		boolean noNeed = false;
		if(this.isEnd(msg)){ 
				TBGPData.getInstance().removePlayer(userNickname);
				if(currentRoom != null)
					currentRoom.leaveRoom2(this.userNickname);
				ans = "bye bye "+ userNickname;
				return ans;
			
		}
		
		String[] command = msg.split(" ");
		
		if (msg.startsWith("COMMANDS")){
			ans = "SYSMSG commands: " +  getCommands();
			return ans;
		}

		if (msg.startsWith("NICK")) {
			try{
				String nickname = command[1];
				boolean accepted = TBGPData.getInstance().addPlayer(nickname);
				if (accepted)
				{
					ans = "SYSMSG Nickname accepted.";
					userNickname = nickname;
				}
				else
					ans = "SYSMSG Nickname not valid. Please choose a different nickname.";
				}
			catch(ArrayIndexOutOfBoundsException e)
			{
				ans = "SYSMSG usage NICK <nickname>";
			}
			return ans;
		}
		// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
		
		//all other commands request that the user already specified a nickname
		if (userNickname == null){
			ans = "SYSMSG You do not have permission for this action, Please register by entering your nickname";
			return ans;
		}
		if (msg.startsWith("JOIN")) {
			String roomName;
			try {
				roomName = command[1];
			}
			catch (ArrayIndexOutOfBoundsException e){
				ans = "SYSMSG usage JOIN <RoomName>";
				return ans;
			}
			if (currentRoom != null) {
				boolean canLeave = currentRoom.leaveRoom(userNickname);
				if (!canLeave)
				{
					ans = "SYSMG " + userNickname + ", you cant leave this room during an open game session";
					return ans;
				}
				currentRoom = null;
			}
			if (currentRoom == null) {
				if (TBGPData.getInstance().rooms.containsKey(roomName)) {
					if (TBGPData.getInstance().rooms.get(roomName).isAvailable()) {
						TBGPData.getInstance().rooms.get(roomName).addPlayer(userNickname, callback);
						currentRoom = TBGPData.getInstance().rooms.get(roomName);
						ans = "SYSMSG " + userNickname + ", you have successfuly joined room :" + roomName;
						return ans;
					}
					else
					{
						ans = "SYSMSG Sorry " + userNickname + ", but the room " + roomName
								+ " is in the middle of a game session";
						return ans; 
					}
				}
				else {
					GameRoom gameRoom = new GameRoom(roomName);
					gameRoom.addPlayer(userNickname, callback);
					currentRoom = gameRoom;
					TBGPData.getInstance().addRoom(gameRoom);
					ans = "SYSMSG " + userNickname + ",you created the room " + roomName
							+ " please wait for more players to join!";
					return ans;
				}
			} 
			// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

		}
		if (msg.startsWith("MSG")) {
			if (currentRoom == null) {
				ans = "Please enter a room before chatting";
				return ans;
			}
			else
			{
				noNeed = true;
				String chatMessage = msg.substring(4); // strlen(MSG) + 1 space
				currentRoom.sendChat(chatMessage, userNickname);
				return "";
			}
	// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
		}
		if (msg.startsWith("LISTGAMES")) {
				return TBGPData.getInstance().printGames();
			
		// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
		}
		if (msg.startsWith("STARTGAME"))
		{
			try{
				String gameName= command[1].toLowerCase();
				if(!TBGPData.getInstance().games.containsKey(gameName)){
					ans = "SYSMSG game " + gameName + " not supported.";
					return ans;
				}
				if(currentRoom == null){
					ans = "SYSMSG please enter a room.";
					return ans;
				}
				GameFactory factory= TBGPData.getInstance().games.get(gameName);
				Game g = factory.create();
				currentRoom.setGame(g);
				currentRoom.startGame();
				return "";
				}
			
			catch(ArrayIndexOutOfBoundsException e){
				ans = "SYSMSG Please enter name of a game";
				return ans;
			}
			//needs to start the game

		}
		if (msg.startsWith("TXTRESP")) {
			if (currentRoom==null)
				return "SYSMSG You do not have permission for this action, Please join a room.";
			String msg2 = "";
			for (int i = 1; i < command.length; i++) {
				msg2 += command[i].toLowerCase() + " ";
			}
			msg2 = msg2.substring(0, msg2.length()-1);
			ans = "SYSMSG TXTRESP " +currentRoom.sendTXT(userNickname, msg2);
			return ans;
		}	
		if (msg.startsWith("SELECTRESP")) {
			if (currentRoom==null)
				return "SYSMSG You do not have permission for this action, Please join a room.";
			if (command.length != 2){
				ans = "SYSMSG usage SELECTRESP <choice>";
				return ans;
			}
			try{
				int choice = Integer.parseInt(command[1]);
				currentRoom.sendRESP(choice, userNickname);
			}
			catch(NumberFormatException e){
				ans = "SYSMSG SELECTRESP NOT ACCEPTED PLEASE ENTER A VALID INTEGER.";
				return ans;
			}
			noNeed =true;
		}
			
		 
		if (ans == null){
			return "Invalid input";
		}
		return "error";
	}
	
	private String getCommands() {
		// TODO Auto-generated method stub
		
		return "NICK, JOIN, MSG, LISTGAMES, TXTRESP, SELECTRESP, STARTGAME.";
	}

	public boolean shouldClose() {
		return this._shouldClose;
	}

	public void connectionTerminated() {
		this._connectionTerminated = true;		
		
	}
}
