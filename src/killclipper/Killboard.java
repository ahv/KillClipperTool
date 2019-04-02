package killclipper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class Killboard implements Serializable {

    int returned;
    public ArrayList<Entry> characters_event_list;

    public Killboard() {
        characters_event_list = new ArrayList<>();
    }

    public void append(Killboard killboard) {
        for (Entry e : killboard.characters_event_list) {
            characters_event_list.add(e);
        }
        sort();
    }

    public void sort() {
        characters_event_list.sort(Comparator.naturalOrder());
    }

    public int size() {
        return characters_event_list.size();
    }

    public long nextTimestampAfter(double timeStamp) {
        for (int i = 0; i < size(); i++) {
            if (characters_event_list.get(i).timestamp > timeStamp) {
                return characters_event_list.get(i).timestamp;
            }
        }
        return 0;
    }

    public long lastTimestampBefore(double timeStamp) {
        for (int i = size()-1; i >= 0; i--) {
            if (characters_event_list.get(i).timestamp < timeStamp) {
                return characters_event_list.get(i).timestamp;
            }
        }
        return 0;
    }

    public class Entry implements Comparable, Serializable {

        public String character_id;
        String attacker_character_id;
        boolean is_headshot;
        boolean is_critical;
        int attacker_weapon_id;
        int attacker_vehicle_id;
        public long timestamp;
        int zone_id;
        int world_id;
        int character_loadout_id;
        int attacker_loadout_id;
        public String table_type;

        @Override
        public int compareTo(Object o) {
            Entry e = (Entry) o;
            return timestamp > e.timestamp ? 1 : -1;
        }

        @Override
        public String toString() {
            return "" + timestamp;
        }
    }

}
