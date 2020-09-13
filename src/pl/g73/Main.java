package pl.g73;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        StringBuffer sb = loadString(args);
        Map<String, Integer> statsMap = textAnalyser(sb.toString());
        double[] results = calculator(statsMap);

        //console
        System.out.println("The text is:");
        System.out.println(sb.toString() + "\n");
        showStatistics(statsMap);

        System.out.print("Enter the score you want to calculate (ARI, FK, SMOG, CL, all):");
        switch (new Scanner(System.in).nextLine().toLowerCase()) {
            case "ari":
                showResult(Arrays.copyOfRange(results, 0, 2), "Automated Readability Index");
                break;
            case "fk":
                showResult(Arrays.copyOfRange(results, 2, 4), "Flesch–Kincaid readability tests");
                break;
            case "smog":
                showResult(Arrays.copyOfRange(results, 4, 6), "Simple Measure of Gobbledygook");
                break;
            case "cl":
                showResult(Arrays.copyOfRange(results, 6, 8), "Coleman–Liau index");
                break;
            case "all":
                System.out.println();
                showResult(Arrays.copyOfRange(results, 0, 2), "Automated Readability Index");
                showResult(Arrays.copyOfRange(results, 2, 4), "Flesch–Kincaid readability tests");
                showResult(Arrays.copyOfRange(results, 4, 6), "Simple Measure of Gobbledygook");
                showResult(Arrays.copyOfRange(results, 6, 8), "Coleman–Liau index");
                double v = (results[1] + results[3] + results[5] + results[7]) / 4;
                System.out.printf("\nThis text should be understood in average by %.2f year olds.",v);
                break;
        }
    }

    private static double[] calculator(Map<String, Integer> statsMap) {
        double[] dataFromCalc = new double[8];
        int words = statsMap.get("words");
        int sentences = statsMap.get("sentences");
        int characters = statsMap.get("characters");
        int syllables = statsMap.get("syllables");
        int polysyllables = statsMap.get("polysyllables");
        int[] ageArray = {6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 24, 24};

        // Automated Readability Index (https://en.wikipedia.org/wiki/Automated_readability_index)
        dataFromCalc[0] = (4.71 * ((double) characters / words) + 0.5 * ((double) words / sentences) - 21.43);
        dataFromCalc[1] = ageArray[(int) Math.round(dataFromCalc[0]) - 1];

        // Flesch–Kincaid readability tests (https://en.wikipedia.org/wiki/Flesch%E2%80%93Kincaid_readability_tests)
        dataFromCalc[2] = (0.39 * ((double) words / sentences) + 11.8 * ((double) syllables / words) - 15.59);
        dataFromCalc[3] = ageArray[(int) Math.round(dataFromCalc[2]) - 1];

        // Simple Measure of Gobbledygook index (https://en.wikipedia.org/wiki/SMOG) (minimum 30 sentences to count properly)
        dataFromCalc[4] = (1.043 * Math.pow(polysyllables * (double) 30 / sentences, 0.5) + 3.1291);
        dataFromCalc[5] = ageArray[(int) Math.round(dataFromCalc[4]) - 1];

        // Coleman–Liau index (https://en.wikipedia.org/wiki/Coleman%E2%80%93Liau_index)
        dataFromCalc[6] = (0.0588 * ((double) characters * 100 / words) - 0.296 * ((double) sentences * 100 / words) - 15.8);
        dataFromCalc[7] = ageArray[(int) Math.round(dataFromCalc[6]) - 1];
        return dataFromCalc;
    }

    private static void showResult(double[] score, String indexName) { //todo
        System.out.printf("%s: %.2f (about %d year olds).\n", indexName, score[0], (int) Math.ceil(score[1]));
    }

    private static void showStatistics(Map<String, Integer> statsMap) {
        System.out.println("Words: " + statsMap.get("words"));
        System.out.println("Sentences: " + statsMap.get("sentences"));
        System.out.println("Characters: " + statsMap.get("characters"));
        System.out.println("Syllables: " + statsMap.get("syllables"));
        System.out.println("Polysyllables: " + statsMap.get("polysyllables"));
    }

    private static StringBuffer loadString(String[] args) throws FileNotFoundException {
        Scanner scFile = new Scanner(new File(args[0]));
        StringBuffer sb = new StringBuffer();
        while (scFile.hasNextLine()) {
            sb.append(scFile.nextLine());
        }
        scFile.close();
        return sb;
    }

    private static Map<String, Integer> textAnalyser(String text) {
        Map<String, Integer> statsMap = new HashMap<>();
        statsMap.put("sentences", text.split("[.!?]").length);
        statsMap.put("words", text.split("\\s+").length);
        statsMap.put("characters", text.replaceAll("[\\s+\\n\\r]", "").length());

        int[] syllablesAndPolysyllables = syllablesCounter(text);
        statsMap.put("syllables", syllablesAndPolysyllables[0]);
        statsMap.put("polysyllables", syllablesAndPolysyllables[1]);

        return statsMap;
    }

    private static int[] syllablesCounter(String text) {
        String textWithoutEonTheEnd = text.replaceAll("e\\b", "j");
        String[] textWithoutEonTheEndArray = textWithoutEonTheEnd.split(" ");
        int syllables = 0;
        int polysyllables = 0;
        for (String s : textWithoutEonTheEndArray) {
            String regex = "(?i)[aeiouy]+|[aeiouy]";
            Matcher m = Pattern.compile(regex).matcher(s);
            int count = 0;
//            long matches = m.results().count();
            while (m.find()) {
                count++;
            }
            if (count > 2) polysyllables++;
            syllables += Math.max(count, 1);
        }
        return new int[]{syllables, polysyllables};
    }
}