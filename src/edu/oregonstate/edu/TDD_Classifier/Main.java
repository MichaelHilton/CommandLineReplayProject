package edu.oregonstate.edu.TDD_Classifier;

public class Main {

    public static void main(String[] args) {

        if(args.length < 2){
            System.out.println("Usage: java TDD_Classifier ReplaySourceFile JsonDestinationFile");
            return;
        }
        Classify newClassification = new Classify(args[0], args[1]);
        newClassification.classifyFile();
    }
}
