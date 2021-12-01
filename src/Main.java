import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    static double[] freq = new double[65];

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.println("Put in your text file path: ");
        File userText = new File(scan.nextLine());

        // [-128; 128]
        byte[] text;

        try {
            text = Files.readAllBytes(userText.toPath());
        } catch (IOException e) {
            System.out.println("Can't find or get access to file " + userText.getPath());
            return;
        }

        // Generate custom frequency file frequency from frequencyExampleText
        try {
            scan = new Scanner(generateFrequencyFromExampleText());
        } catch (FileNotFoundException e) {
            System.out.println("Can't find or access to frequency file, try to delete one if it exists");
            return;
        }

        for (int i = 0; scan.hasNext(); i++) {
            freq[i] = scan.nextDouble();
        }

        // Generate byte frequency for each encoding table
        HashMap<String, OutputDependEncoding> freqByEncoding = new HashMap<>();
        try {
            scan = new Scanner(new File("codes"));
        } catch (FileNotFoundException e) {
            System.out.println("Can't find or read file with encodings names");
            return;
        }

        while (scan.hasNext()) {
            String code = scan.nextLine();
            OutputDependEncoding tableFreq;

            try {
                tableFreq = getFreqForEncoding(code);
            } catch (IOException e) {
                System.out.println("Can't find or access to " + code + " file with encoding abc");
                continue;
            }

            freqByEncoding.put(code, tableFreq);
        }


        // [128; 255]
        double[] textStatistic = getByteStatistics(text);

        // Gets source text encoding name
        String[] codeNames = countDiffAndGetEncodingName(textStatistic, freqByEncoding);
        String[] decodedResults = new String[3];

        for (int i = 0; i < codeNames.length; i++) {
            System.out.println(i + 1 + ") " + codeNames[i]);
            decodedResults[i] = decodeInputText(text, freqByEncoding.get(codeNames[i]).getDisplay());
            System.out.println(decodedResults[i]);
        }

        scan = new Scanner(System.in);
        System.out.println("If you want to save decoded text, print one of given variants(1, 2, 3) or any symbol if you just want to exit");
        String variant = scan.nextLine();

        switch (variant) {
            case "1":
                saveResultToFile(decodedResults[0]);
                break;
            case "2":
                saveResultToFile(decodedResults[1]);
                break;
            case "3":
                saveResultToFile(decodedResults[2]);
                break;
            default: break;
        }
    }

    private static void saveResultToFile(String result) {
        File decodedTextFile = new File("decodedText");
        try {
            decodedTextFile.createNewFile();
            Files.write(decodedTextFile.toPath(), result.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Failed to create file " + decodedTextFile.getPath() + " or write data to it");
        }
    }

    private static String decodeInputText(byte[] text, Map<Byte, Character> display) {
        StringBuilder res = new StringBuilder();

        for (byte b : text) {
            char symbol;

            if (b < 0) {
                symbol = display.getOrDefault(b, '?');
            } else {
                symbol = (char) b;
            }

            res.append(symbol);
        }

        return res.toString();
    }

    private static File generateFrequencyFromExampleText() {
        File frequency = new File("frequency");

        if (!frequency.exists()) {
            try {
                FrequencyGenerator.generateFrequency("FrequencyExampleText", "frequency");
            } catch (FrequencyGenerationException e) {
                System.out.println(e.getMessage());
            }
        }

        return frequency;
    }

    private static String[] countDiffAndGetEncodingName(double[] textStatistic, HashMap<String, OutputDependEncoding> freqByEncoding) {
        double firstMin = Double.MAX_VALUE, secondMin = Double.MAX_VALUE, thirdMin = Double.MAX_VALUE;
        String[] names = new String[3];

        for (Map.Entry<String, OutputDependEncoding> codes : freqByEncoding.entrySet()) {
            double res = 0;

            for (int i = 0; i < textStatistic.length; i++) {
                res += Math.pow(Math.abs(codes.getValue().getAbcTable()[i] - textStatistic[i]), 2);
            }

            System.out.println(codes.getKey() + " : " + res);

            if (res <= firstMin) {
                thirdMin = secondMin;
                names[2] = names[1];
                secondMin = firstMin;
                names[1] = names[0];
                firstMin = res;
                names[0] = codes.getKey();
            } else if (res <= secondMin) {
                thirdMin = secondMin;
                names[2] = names[1];
                secondMin = res;
                names[1] = codes.getKey();
            } else if (res <= thirdMin){
                thirdMin = res;
                names[2] = codes.getKey();
            }
        }

        return names;
    }

    private static double[] getByteStatistics(byte[] text) {
        double[] textStatistic = new double[128];
        int counter = 0;

        for(byte value : text) {
            if (value < 0) {
                textStatistic[128 + value]++;
                counter++;
            }
        }

        for(int i = 0; i < textStatistic.length; i++) {
            double res = textStatistic[i] * 100 / counter;
            textStatistic[i] = res;
        }

        return textStatistic;
    }

    private static OutputDependEncoding getFreqForEncoding(String encodingFileName) throws IOException {
        byte[] abcTable = Files.readAllBytes(Paths.get(encodingFileName));
        double[] freqStatistic = new double[128];
        Map<Byte, Character> display = new HashMap<>(65);
        String abc = "ҐЄЇІіґєїАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЮЯабвгдежзийклмнопрстуфхцчшщьюя";

        for (int i = 0; i < abcTable.length; i++) {
            if (abcTable[i] < 0) {
                freqStatistic[abcTable[i] + 128] = freq[i];
                display.put(abcTable[i], abc.charAt(i));
            }
        }

        return new OutputDependEncoding(freqStatistic, display);
    }
}
