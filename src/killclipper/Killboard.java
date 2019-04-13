package killclipper;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class Killboard implements Serializable {

    int returned;
    @SerializedName("characters_event_list")
    public ArrayList<Entry> entries;

    public Killboard() {
        entries = new ArrayList<>();
    }

    public void append(Killboard killboard) {
        for (Entry e : killboard.entries) {
            entries.add(e);
        }
        sort();
    }

    public void sort() {
        entries.sort(Comparator.naturalOrder());
    }

    public int size() {
        return entries.size();
    }

    public long nextTimestampAfter(double timeStamp) {
        for (int i = 0; i < size(); i++) {
            if (entries.get(i).timestamp > timeStamp) {
                return entries.get(i).timestamp;
            }
        }
         // TODO: Could also return the last item, right now the video will return to start
        return 0;
    }

    public long lastTimestampBefore(double timeStamp) {
        for (int i = size()-1; i >= 0; i--) {
            if (entries.get(i).timestamp < timeStamp) {
                return entries.get(i).timestamp;
            }
        }
        return 0;
    }

    // TODO: Getters yo
    public class Entry implements Comparable, Serializable {
        @SerializedName("character_id")
        public String target_id;
        public Character character;
        @SerializedName("attacker_character_id")
        String attacker_id;
        boolean is_headshot;
        boolean is_critical;
        int attacker_weapon_id;
        int attacker_vehicle_id;
        public long timestamp;
        int zone_id;
        int world_id;
        @SerializedName("character_loadout_id")
        int target_loadout_id;
        int attacker_loadout_id;
        @SerializedName("table_type")
        public String type;
        
        // TODO: can be flattened with a custom type adapter
        public class Character {
            public Name name;
            public class Name {
                public String first;
            }
        }

        @Override
        public int compareTo(Object o) {
            Entry e = (Entry) o;
            return timestamp > e.timestamp ? 1 : -1;
        }
        
        public class Type {
            public static final String KILLS = "kills";
            public static final String DEATHS = "deaths";
        }
    }
}
