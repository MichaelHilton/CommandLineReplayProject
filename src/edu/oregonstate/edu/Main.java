package edu.oregonstate.edu;

public class Main {

    public static void main(String[] args) {
	// write your code here
        if(args.length < 1){
            System.out.println("Usage: java commandLineReplay Directory FILEtoReplay replayDir");
            return;
        }
        Replay r = new Replay();
        r.setDir(args[2]);
        r.unzipInitialState(args[1]);
        r.replayFile(args[0]);
        r.closeAllFiles();

    }
}
