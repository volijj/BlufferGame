package main.java.Games;

import java.util.ArrayList;

public class DataObject {
	Question[] questions;
	public DataObject() {
		// TODO Auto-generated constructor stub
		
	}
	
	public ArrayList<Question> load(){
		ArrayList<Question> ans= new ArrayList<Question>();
		for(int i=0 ; i< questions.length ;i++){
			ans.add(questions[i]);
		}
		return ans;
	}

}
