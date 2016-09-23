package main.java.Games;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import main.java.Databases.GameRoom;
import main.java.Interfaces.ProtocolCallback;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Bluffer extends Game {
	
	private static final Logger logger = Logger.getLogger("BlufferLogger");

	ConcurrentHashMap<String, BlufferPlayer> players; // a map that maps names
														// to blufferplayers.
	int counter; // counts the number of questions sent
	int counter2; // counts the number of players that sent an answer
	GameRoom room; // room

	ArrayList<Question> questions; // a set of all the questions available
	ConcurrentHashMap<String, String> fakeAnswers; // a map that maps
													// fakeAnswers to usernames
	ArrayList<String> fakeAnswers2; // a list of fakeAnswers
	String currentRightAnswer;

	Bluffer() {
		players = new ConcurrentHashMap<String, BlufferPlayer>();
		counter = 0;
		counter2 = 0;
		fakeAnswers = new ConcurrentHashMap<String, String>();
		fakeAnswers2 = new ArrayList<String>();

	}

	public void load(GameRoom room) {
		ConcurrentHashMap<String, ProtocolCallback<String>> map = room.clientCallbacks;
		this.room = room;
		for (String playerName : map.keySet()) {
			ProtocolCallback<String> callback = map.get(playerName);
			BlufferPlayer blufPlayer = new BlufferPlayer(playerName, callback);
			players.put(playerName, blufPlayer);
		}
		DataObject q = readJson("bluffer.json");
		questions = q.load();
		
		SendQuestion();

	}

	void SendQuestion() {
		logger.info("SendQuestion called");
		
		if (counter < 3) {
			Random r = new Random();
			int i = r.nextInt(questions.size());
			Question q = questions.remove(i);
			String question = q.questionText;
			this.currentRightAnswer = q.realAnswer;
			fakeAnswers = new ConcurrentHashMap<String, String>();
			fakeAnswers.put(currentRightAnswer, "Rightans27771");
			
			for (String playerName : players.keySet()) {
				players.get(playerName).sendQuestion("ASKTXT " + question);

			}
			counter2 = 0;
			counter++;
		} else {
			endGame();
		}
	}

	public String acceptTXT(String name, String answer) {
		if (answer.equals(currentRightAnswer.toLowerCase()) || fakeAnswers2.contains(answer))
			return "REJECTED";
		fakeAnswers.put(answer, name);
		if (fakeAnswers.size() == players.size() + 1) {
			String ans = OpenCards();

			for (String playerName : players.keySet()) {
				try {
					players.get(playerName).callback.sendMessage("ASKCHOICES " + ans);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "ACCEPTED";
	}

	// processes a SELECTRESP message from username name

	public void endGame() {
		for (String n : this.players.keySet()) {
			try {
				BlufferPlayer player = this.players.get(n);
				int points = player.points;
				player.callback
						.sendMessage("GAMEMSG " + toString() + " has Ended! You have recieved " + points + "points.");
				player.callback.sendMessage("GAMEMSG :" + Score());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		room.endGame();
	}

	private String OpenCards() {
		String ans = shuffle(new ArrayList(fakeAnswers.values()));
		return ans;

	}

	private String shuffle(ArrayList arr) {
		String ans = "";
		int counter3 = 0;
		Collections.shuffle(arr);
		return arr.toString();
		/*
		while (counter3 < arr.size()) {
			String check = arr.get(counter3);
			if (check != null) {
				ans = ans + counter3 + ". " + check + " ";
				counter3++;
			}
		}
		return ans;*/
	}

	private DataObject readJson(String file) {
		Gson gson = new GsonBuilder().create();

		DataObject o = null;

		try {
			BufferedReader obj = new BufferedReader(new FileReader(file)); // this reads the json file and makes a bufferedReader 
																		   // the gson can convert to a java object.
																		
			o = gson.fromJson(obj, DataObject.class); // gson takes obj and
														// makes a DataObject -
														// look at that object I
														// made!

		}
		// new FileReader(string) throws an exception so this is where we catch
		// it
		catch (FileNotFoundException e) {
			System.out.println("File not found");

		}

		return o;
	}

	public void acceptRESP(int choice, String nickname) {
		BlufferPlayer player = players.get(nickname);
		if (choice < fakeAnswers2.size()) {
			String answer = fakeAnswers2.get(choice);
			try {
				player.callback.sendMessage("SYSMSG SELECTRESP ACCEPTED");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (answer.equals(currentRightAnswer)) {
				try {
					player.callback.sendMessage("GAMEMSG CORRECT ANSWER! GOT 10 POINTS!");
					player.addTenPoints();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				String otherName = fakeAnswers.get(answer);
				BlufferPlayer otherPlayer = players.get(otherName);
				try {
					if (!otherName.equals(nickname)) {
						otherPlayer.callback.sendMessage("GAMEMSG ANOTHER PLAYER CHOSE YOUR ANSWER! GOT 5 POINTS!!");
						otherPlayer.addFivePoints();
					} else
						otherPlayer.callback
								.sendMessage("You chose your own answer! Next time remember what you answered.");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			counter2++;
			if (counter2 == players.size()) {
				SendQuestion();
			}

		} else {
			try {
				player.callback.sendMessage("SYSMSG SELECTRESP NOT ACCEPTED. PLEASE ENTERT A VALID INTEGER");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String Score() {
		String score = "--------------SCOREBOARD---------------" + "\n";
		for (String player : players.keySet()) {
			score = score + player + ": " + players.get(player).points + "\n";
		}
		return score;
	}

	public String toString() {
		return "Bluffer Game";
	}

	public int getCounter() {
		return counter;
	}

	

}
