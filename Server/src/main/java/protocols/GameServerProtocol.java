package main.java.protocols;
import java.io.IOException;

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
		String ans = null;
		boolean noNeed = false;
		if(this.isEnd(msg)){ 
			this._shouldClose=true;
				TBGPData.getInstance().removePlayer(userNickname);
				if(currentRoom!=null)
					currentRoom.leaveRoom2(this.userNickname);
				ans = "bye bye "+ userNickname;
			
		}
		String[] command = msg.split(" ");

		if (msg.startsWith("NICK")) {
			try{
			String nickname = command[1];
			boolean accepted = TBGPData.getInstance().addPlayer(nickname);
			if (accepted) {
				ans = "SYSMSG Nickname accepted.";
				userNickname = nickname;
			} else
				ans = "SYSMSG Nickname not valid. Please choose a different nickname.";
			} catch(ArrayIndexOutOfBoundsException e){
				ans = "SYSMSG Please enter nickname";
			}
		}
		// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

		else if (msg.startsWith("JOIN")) {
			String roomName = command[1];
			if (currentRoom != null) {
				if (userNickname != null) {
					boolean canLeave = currentRoom.leaveRoom(userNickname);
					if (!canLeave) {
						ans = "SYSMG " + userNickname + ", you cant leave this room during an open game session";
					} else
						currentRoom = null;
				}
			} if (userNickname != null && currentRoom == null) {
				if (TBGPData.getInstance().rooms.containsKey(roomName)) {
					if (TBGPData.getInstance().rooms.get(roomName).isAvailable()) {
						TBGPData.getInstance().rooms.get(roomName).addPlayer(userNickname, callback);
						currentRoom = TBGPData.getInstance().rooms.get(roomName);
						ans = "SYSMSG " + userNickname + ", you have successfuly joined room :" + roomName;

					} else
						ans = "SYSMSG Sorry " + userNickname + ", but the room " + roomName
								+ " is in the middle of a game session";
				} else {
					GameRoom gameRoom = new GameRoom(roomName);
					gameRoom.addPlayer(userNickname, callback);
					currentRoom = gameRoom;
					TBGPData.getInstance().addRoom(gameRoom);
					ans = "SYSMSG " + userNickname + ",you created the room " + roomName
							+ " please wait for more players to join!";
				}
			} else
				ans = "SYSMSG You do not have permission for this action, Please register by entering your nickname";

			// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

		} else if (msg.startsWith("MSG")) {
			if (userNickname != null) {
				if (currentRoom == null) {
					ans = "Please enter a room before chatting";
				} else {
					noNeed = true;
					String chatMessage = "";
					for (int i = 1; i < command.length; i++) {
						chatMessage = chatMessage + " " + command[i];
					}
					currentRoom.sendChat(chatMessage, userNickname);
				}
			} else
				ans = "You do not have permission for this action, Please register by entering your nickname";
			// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
		} else if (msg.startsWith("LISTGAMES")) {
			if(userNickname!=null){
				ans = TBGPData.getInstance().printGames();
			}
			else
				ans = "You do not have permission for this action, Please register by entering your nickname";

			// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
		} else if (msg.startsWith("STARTGAME")) {
			if(this.userNickname!=null){
				
				try{
					String gameName= command[1].toLowerCase();
					if(!TBGPData.getInstance().games.containsKey(gameName))
						ans = "SYSMSG game " + gameName + " not supported.";
					if(currentRoom==null)
						ans = "SYSMSG please enter a room. ";
					if(userNickname!=null && currentRoom!=null
						&& TBGPData.getInstance().games.containsKey(gameName)){
						GameFactory factory= TBGPData.getInstance().games.get(gameName);
						Game g = factory.create();
						currentRoom.setGame(g);
						currentRoom.startGame();
						noNeed=true;
					}
				}
				catch(ArrayIndexOutOfBoundsException e){
					ans = "SYSMSG Please enter name of a game";
				}
			}
			else
				ans = "You do not have permission for this action, Please register by entering your nickname";
			
			//needs to start the game

		} else if (msg.startsWith("TXTRESP")) {
			if(this.userNickname!=null && currentRoom!=null){
				String msg2 = "";
				for (int i = 1; i < command.length; i++) {
					msg2 = msg2 + command[i].toLowerCase() + " ";
				}
				msg2 = msg2.substring(0, msg2.length()-1);
				ans = "SYSMSG TXTRESP " +currentRoom.sendTXT(userNickname, msg2);
			}
			else{
				if (currentRoom==null)
					ans = "SYSMSG You do not have permission for this action, Please join a room.";
				if (userNickname==null) 
					ans = "SYSMSG You do not have permission for this action, Please register by entering your nickname";
				
					
			}
				

		} else if (msg.startsWith("SELECTRESP")) {
			if(this.userNickname!=null && currentRoom!=null){
			try{
				int choice = Integer.parseInt(command[1]);
				currentRoom.sendRESP(choice, userNickname);
			}
			catch(NumberFormatException e){
				ans = "SYSMSG SELECTRESP NOT ACCEPTED PLEASE ENTER A VALID INTEGER.";
			}
			noNeed =true;
			}
			else{
				if (currentRoom==null)
					ans = "SYSMSG You do not have permission for this action, Please join a room.";
				if (userNickname==null) 
					ans = "SYSMSG You do not have permission for this action, Please register by entering your nickname";
								
			}
			
		} 
		 else {
			ans = "Invalid input";
		}

		try {
			if (!noNeed)
				callback.sendMessage(ans);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public boolean shouldClose() {
		return this._shouldClose;
	}

	public void connectionTerminated() {
		this._connectionTerminated = true;		
		
	}
}
