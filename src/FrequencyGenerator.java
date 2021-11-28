import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FrequencyGenerator {
    public static void generateFrequency(String input, String output) throws FrequencyGenerationException {
        double[] array = new double[128];
        int counter = 0;
        byte[] text;

        try {
            text = Files.readAllBytes(Paths.get(input));
        } catch (IOException e) {
            throw new FrequencyGenerationException("Can't read from example text file " + input);
        }

        if (text.length < 2000) {
            throw new FrequencyGenerationException("Input text example is too small to make accurate frequency");
        }

        for (byte value : text) {
            if (value < 0 && isInRange(256 + value)) {
                array[128 + value]++;
                counter++;
            }
        }

        if (counter == 0) {
            throw new FrequencyGenerationException("Input text has no ukraine letters");
        }

        saveFreqToFile(array, output, counter);
    }

    private static void saveFreqToFile(double[] array, String filename, int counter) throws FrequencyGenerationException {
        try (PrintWriter writer = new PrintWriter(filename)) {

            for (int i = 0; i < array.length; i++) {
                if (isInRange(128 + i)) {
                    double res = array[i] * 100 / counter;
                    writer.println(String.format("%,f", res));
                    writer.flush();
                }
            }
        } catch (IOException e) {
            throw new FrequencyGenerationException("Can't save generated frequency to file " + filename);
        }
    }

    private static boolean isInRange(int value) {
        return (value > 191 && !(value > 217 && value < 222) && value != 250 && value != 251 && value != 253)
                || value == 191 || value == 186 || value == 175 || value == 170 || value == 165
                || (value > 177 && value < 181);
    }
}
