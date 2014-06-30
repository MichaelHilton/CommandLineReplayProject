package edu.oregonstate.edu;

public class Main {

    public static void main(String[] args) {
        // args[0] - replaySourceFile
        // args[1] - ZipSourceFile
        // args[2] - JsonDestinationFile

        if(args.length < 3){
            System.out.println("Usage: java CommandLineReplay ReplaySourceFile ZipSourceFile JsonDestinationFile");
            return;
        }
        Replay r = new Replay();
        r.setDir(args[2]);
        if(args[1].equals("NONE")) {
            System.out.println("NO ZIP");
        } else {
            r.unzipInitialState(args[1]);
        }
        r.replayFile(args[0]);
    }
}
