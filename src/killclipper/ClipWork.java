package killclipper;

import java.util.ArrayList;

public class ClipWork {

    private final ArrayList<Span> clipSpans;
    private int preceedingSeconds;
    private int trailingSeconds;

    public ClipWork(Killboard killboard, int preceedingSeconds, int trailingSeconds) {
        this.preceedingSeconds = preceedingSeconds;
        this.trailingSeconds = trailingSeconds;
        int maximumSecondsBetweenKills = preceedingSeconds + trailingSeconds;
        clipSpans = new ArrayList<>();
        int index = 0;
        while (index < killboard.size()) {
            long clipStart = killboard.characters_event_list.get(index).timestamp;

            Killboard.Entry pe = killboard.characters_event_list.get(index);
            index++;
            while (index < killboard.size()) {
                pe = killboard.characters_event_list.get(index - 1);
                Killboard.Entry ne = killboard.characters_event_list.get(index);
                if (ne.timestamp - pe.timestamp > maximumSecondsBetweenKills) {
                    break;
                }
                index++;
            }
            clipSpans.add(new Span(clipStart - preceedingSeconds, pe.timestamp + trailingSeconds));
        }
    }

    public ArrayList<Span> getSpans() {
        return clipSpans;
    }

    public class Span {

        private final long startTimeStamp;
        private final long endTimeStamp;

        public Span(long startTimeStamp, long endTimeStamp) {
            this.startTimeStamp = startTimeStamp;
            this.endTimeStamp = endTimeStamp;
        }

        public long getStartTimestamp() {
            return startTimeStamp;
        }

        public long getEndTimestamp() {
            return endTimeStamp;
        }
        
        public long getDurationSeconds() {
            return endTimeStamp - startTimeStamp;
        }

        @Override
        public String toString() {
            return String.format("Span: %s -- %s (%s)", startTimeStamp, endTimeStamp, getDurationSeconds());
        }
    }
}
