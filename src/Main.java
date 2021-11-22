import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.DoubleBinaryOperator;

public class Main {
    static double[] freq = new double[65];
    public static void main(String[] args) throws IOException {
        FrequencyGenerator.generateFrequency("FrequencyExampleText", "frequency");
        Scanner scan = new Scanner(new File("frequency"));

        for (int i = 0; scan.hasNext(); i++) {
            freq[i] = scan.nextDouble();
        }

        HashMap<String, double[]> freqByEncoding = new HashMap<>();
        scan = new Scanner(new File("codes"));

        while (scan.hasNext()) {
            String code = scan.nextLine();
            freqByEncoding.put(code, getFreqForEncoding(code));
        }

        // [-128; 128]
        byte[] text = Files.readAllBytes(Paths.get("Text"));
        // [128; 255]
        double[] textStatistic = getByteStatistics(text);

        String codeName = countDiffAndGetEncodingName(textStatistic, freqByEncoding);

        System.out.println(Arrays.stream(textStatistic).reduce(Double::sum));
        System.out.println(codeName);

        convertBytes(text, getNewByteList(freqByEncoding.get(codeName), textStatistic));


        System.out.println(new String(text, Charset.forName(codeName)));
        try(OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("Output", false), Charset.forName(codeName))) {
            writer.write(new String(text, Charset.forName(codeName)));
        }
    }

    private static void convertBytes(byte[] text, HashMap<Byte, Byte> newBytes) {
        for (int i = 0; i < text.length; i++) {
            if (text[i] < 0 && newBytes.containsKey((byte) (128 + text[i]))) {
                text[i] = (byte) (newBytes.get((byte) (128 + text[i])) - 128);
            }
        }
    }

    private static String countDiffAndGetEncodingName(double[] textStatistic, HashMap<String, double[]> freqByEncoding) {
        double min = Double.MAX_VALUE;
        String codeName = "cp1251";

        for (Map.Entry<String, double[]> codes : freqByEncoding.entrySet()) {
            double res = 0;

            for (int i = 0; i < textStatistic.length; i++) {
                res += Math.pow(Math.abs(codes.getValue()[i] - textStatistic[i]), 2);
            }

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

    private static HashMap<Byte, Byte> getNewByteList(double[] codeFreq, double[] textStatistic) {
        HashMap<Byte, Byte> changedBytes = new HashMap<>(65);

        for (int i = 0; i < textStatistic.length; i++) {
            if (textStatistic[i] == 0) {
                continue;
            }

            double freq = textStatistic[i];
            double minDiv = 100;
            byte index = 0;

            for (int b = 0; b < codeFreq.length; b++) {
                if (codeFreq[b] == 0) {
                    continue;
                }

                double div = Math.pow(Math.abs(freq - codeFreq[b]), 2);

                if (div < minDiv) {
                    minDiv = div;
                    index = (byte) b;
                }
            }

            changedBytes.put((byte) i, index);
        }

        return changedBytes;
    }

    private static double[] getFreqForEncoding(String encodingFileName) throws IOException {
        byte[] abcTable = Files.readAllBytes(Paths.get(encodingFileName));
        double[] freqStatistic = new double[128];

        for (int i = 0; i < abcTable.length; i++) {
            if (abcTable[i] < 0) {
                freqStatistic[abcTable[i] + 128] = freq[i];
            }
        }

        return freqStatistic;
    }
}
