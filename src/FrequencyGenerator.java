import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FrequencyGenerator {
    public static void generateFrequency(String input, String output) throws IOException {
        double[] array = new double[128];
        int counter = 0;
        byte[] text = Files.readAllBytes(Paths.get(input));

        for(byte value : text) {
            if (value < 0 && isInRange(256 + value)) {
                array[128 + value]++;
                counter++;
            }
        }

        if (counter == 0) {
            return;
        }

        saveFreqToFile(array, output, counter);
    }

    private static void saveFreqToFile(double[] array, String filename, int counter) {
        try (PrintWriter writer = new PrintWriter(filename)) {
            double sum = 0;
            for (int i = 0; i < array.length; i++) {
                if (isInRange(128 + i)) {
                    double res = array[i] * 100 / counter;
                    writer.println(String.format("%,f", res));
                    writer.flush();
                    byte[] arr = new byte[1];
                    arr[0] = (byte) (i + 128);
                    System.out.println(new String(arr, Charset.forName("cp1251")) + " : " + res);
                    sum += res;
                }
            }

            System.out.println(sum);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isInRange(int value) {
        return (value > 191 && !(value > 217 && value < 222) && value != 250 && value != 251 && value != 253)
                || value == 191 || value == 186 || value == 175 || value == 170 || value == 165
                || (value > 177 && value < 181);
    }
}
