package Games;

import java.io.IOException;

import Interfaces.ProtocolCallback;

public class BlufferPlayer {
	
	String nickName;
	Integer points;
	
	ProtocolCallback<String> callback;
	
	public BlufferPlayer(String name, ProtocolCallback<String> callback){
		this.nickName=name;
		this.callback=callback;
		points = new Integer(0);
	}
	
	public void addTenPoints(){
		points =points +10;
	}
	
	public void addFivePoints(){
		points=points+5;
	}
	
	public void sendQuestion(String question){
		try {
			callback.sendMessage(question);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
