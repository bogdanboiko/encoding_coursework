import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
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
                System.out.println("Can't find or access to " + code + "file with encoding abc");
                continue;
            }

            freqByEncoding.put(code, tableFreq);
        }


        // [128; 255]
        double[] textStatistic = getByteStatistics(text);

        // Gets source text encoding name
        String codeName = countDiffAndGetEncodingName(textStatistic, freqByEncoding);

        System.out.println(codeName);

        Map<Byte, Character> display = freqByEncoding.get(codeName).getDisplay();
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

        System.out.println(res);
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

    private static String countDiffAndGetEncodingName(double[] textStatistic, HashMap<String, OutputDependEncoding> freqByEncoding) {
        double min = Double.MAX_VALUE;
        String codeName = "cp1251";

        for (Map.Entry<String, OutputDependEncoding> codes : freqByEncoding.entrySet()) {
            double res = 0;

            for (int i = 0; i < textStatistic.length; i++) {
                res += Math.pow(Math.abs(codes.getValue().getAbcTable()[i] - textStatistic[i]), 2);
            }

            System.out.println(codes.getKey() + " : " + res);

            if (min > res) {
                min = res;
                codeName = codes.getKey();
            }
        }

        return codeName;
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
