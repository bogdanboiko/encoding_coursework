import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    static double[] freq = new double[65];
    public static void main(String[] args) throws IOException {
        // Generate custom frequency file from frequencyExampleText
        FrequencyGenerator.generateFrequency("FrequencyExampleText", "frequency");
        Scanner scan = new Scanner(new File("frequency"));

        for (int i = 0; scan.hasNext(); i++) {
            freq[i] = scan.nextDouble();
        }

        // Generate byte freq for each encoding table
        HashMap<String, DisplayDependEncoding> freqByEncoding = new HashMap<>();
        scan = new Scanner(new File("codes"));

        while (scan.hasNext()) {
            String code = scan.nextLine();
            DisplayDependEncoding tableFreq = getFreqForEncoding(code);
            freqByEncoding.put(code, tableFreq);
        }

        // [-128; 128]
        byte[] text = Files.readAllBytes(Paths.get("Text"));
        // [128; 255]
        double[] textStatistic = getByteStatistics(text);

        // Gets source text encoding name
        String codeName = countDiffAndGetEncodingName(textStatistic, freqByEncoding);

        System.out.println(codeName);

        Map<Byte, Character> display = freqByEncoding.get(codeName).getDisplay();
        StringBuilder res = new StringBuilder();

        for (byte b : text) {
            if (b < 0 && display.containsKey(b)) {
                res.append(display.get(b).charValue());
            } else {
                byte[] arr = new byte[1];
                arr[0] = b;
                res.append(new String(arr));
            }
        }

        System.out.println(res);

    }

    private static String countDiffAndGetEncodingName(double[] textStatistic, HashMap<String, DisplayDependEncoding> freqByEncoding) {
        double min = Double.MAX_VALUE;
        String codeName = "cp1251";

        for (Map.Entry<String, DisplayDependEncoding> codes : freqByEncoding.entrySet()) {
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

    private static DisplayDependEncoding getFreqForEncoding(String encodingFileName) throws IOException {
        byte[] abcTable = Files.readAllBytes(Paths.get(encodingFileName));
        double[] freqStatistic = new double[128];
        Map<Byte, Character> display = new HashMap<>();
        String abc = "ҐЄЇІіґєїАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЮЯабвгдежзийклмнопрстуфхцчшщьюя";

        for (int i = 0; i < abcTable.length; i++) {
            if (abcTable[i] < 0) {
                freqStatistic[abcTable[i] + 128] = freq[i];
                display.put(abcTable[i], abc.charAt(i));
            }
        }

        return new DisplayDependEncoding(freqStatistic, display);
    }
}

class DisplayDependEncoding {
    private final double[] freqStatistic;
    private final Map<Byte, Character> display;

    public DisplayDependEncoding(double[] freqStatistic, Map<Byte, Character> display) {
        this.freqStatistic = freqStatistic;
        this.display = display;
    }

    public double[] getAbcTable() {
        return freqStatistic;
    }

    public Map<Byte, Character> getDisplay() {
        return display;
    }
}
