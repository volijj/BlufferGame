package Games;

public class BlufferRunner implements Runnable {
	Bluffer bluffer;
	
	public BlufferRunner(Bluffer b){
		bluffer=b;
	}
	
	@Override
	public void run(){
		
		while(bluffer.getCounter()<3){
			System.out.println(bluffer.counter);
			bluffer.SendQuestion();
			
			synchronized(bluffer){
			while(bluffer.counter2<bluffer.players.size()){
				try {
					bluffer.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			}
			
		}
		bluffer.endGame();
	}

}
