package edu.oregonstate.edu.Replayer;

public class Main {

    public static void main(String[] args) {
        // args[0] - replaySourceFile
        // args[1] - ZipSourceFile
        // args[2] - JsonDestinationFile

        if(args.length < 3){
            System.out.println("Usage: java CommandLineReplay ReplaySourceFile ZipSourceFile JsonOutputDir");
            return;
        }
        
        String replaySourceFile = args[0];
        String zipSourceFile = args[1];
        String jsonOutputDir = args[2];

        Replay r = new Replay();
        r.setDir(jsonOutputDir);

        if(zipSourceFile.equals("NONE")) {
            System.out.println("NO ZIP");
        } else {
            r.unzipInitialState(args[1]);
        }

        r.replayFile(replaySourceFile);
    }
}
