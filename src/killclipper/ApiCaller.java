package killclipper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class ApiCaller {

    private final static RestTemplate REST = new RestTemplate();
    private final static String API_URI = "http://census.daybreakgames.com/s:60606/get/ps2:v2/";
    private final static Gson GSON = new GsonBuilder().create();

    public static Killboard requestKillboard(String characterId, long afterTimestamp, long beforeTimestamp) {
        String killboard_query = String.format("characters_event/?type=KILL,DEATH&c:limit=200&after=%s&before=%s&character_id=%s", afterTimestamp, beforeTimestamp, characterId);
        ResponseEntity<String> responseEntity = REST.getForEntity(API_URI + killboard_query, String.class);
        Killboard killboardResponse = GSON.fromJson(responseEntity.getBody(), Killboard.class);
        return killboardResponse;
    }

    public static String getCharacterIdForName(String characterName) {
        String id_query = String.format("character/?name.first_lower=%s&c:show=character_id", characterName.toLowerCase());
        ResponseEntity<String> responseEntity = REST.getForEntity(API_URI + id_query, String.class);
        UserIdResponse response = GSON.fromJson(responseEntity.getBody(), UserIdResponse.class);
        return response.character_list.get(0).character_id;
    }

    public static String getCharacterNameForId(String target_id) {
        String query = String.format("character/?character_id=%s&c:show=name.first", target_id);
        ResponseEntity<String> responseEntity = REST.getForEntity(API_URI + query, String.class);
        UserNameResponse response = GSON.fromJson(responseEntity.getBody(), UserNameResponse.class);
        System.out.println("Returned: " + response.returned);
        return response.character_list.get(0).name.get("first");
    }

    class UserIdResponse implements Serializable {

        int returned;
        List<Data> character_list;

        class Data implements Serializable {

            String character_id;
        }
    }

    class UserNameResponse implements Serializable {
        int returned;
        List<Data> character_list;
        class Data implements Serializable {
            Map<String,String> name;
        }
    }
}
