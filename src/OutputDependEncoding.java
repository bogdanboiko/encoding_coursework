import java.util.Map;

public class OutputDependEncoding {
    private final double[] freqStatistic;
    private final Map<Byte, Character> display;

    public OutputDependEncoding(double[] freqStatistic, Map<Byte, Character> display) {
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
