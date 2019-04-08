package killclipper;

import java.util.ArrayList;

public class TimeSpan {

    private final long startTimestamp;
    private final long endTimestamp;

    public TimeSpan(long startTimestamp, long endTimestamp) {
        // TODO: Validate, start time can't be greater than end time
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public long getDurationSeconds() {
        return endTimestamp - startTimestamp;
    }

    @Override
    public String toString() {
        return String.format("Span: %s -- %s (%s)", startTimestamp, endTimestamp, getDurationSeconds());
    }

    public static ArrayList<TimeSpan> createSpansFor(Killboard killboard, int preceedingSeconds, int trailingSeconds) {
        ArrayList<TimeSpan> spans = new ArrayList<>();
        int maximumSecondsBetweenKills = preceedingSeconds + trailingSeconds;
        int index = 0;
        while (index < killboard.size()) {
            long clipStart = killboard.entries.get(index).timestamp;
            Killboard.Entry pe = killboard.entries.get(index);
            index++;
            while (index < killboard.size()) {
                pe = killboard.entries.get(index - 1);
                Killboard.Entry ne = killboard.entries.get(index);
                if (ne.timestamp - pe.timestamp > maximumSecondsBetweenKills) {
                    break;
                }
                index++;
            }
            spans.add(new TimeSpan(clipStart - preceedingSeconds, pe.timestamp + trailingSeconds));
        }
        return spans;
    }

}
