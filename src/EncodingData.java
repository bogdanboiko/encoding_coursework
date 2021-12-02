import java.util.Map;

public class EncodingData {
    private final double[] freqStatistic;
    private final Map<Byte, Character> display;

    public EncodingData(double[] freqStatistic, Map<Byte, Character> display) {
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
