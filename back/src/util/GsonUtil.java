package util; 

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import java.sql.Date; // Certifique-se de que é java.sql.Date

public class GsonUtil {

    // Configuração do objeto Gson estático com o adaptador de data
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(java.sql.Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> {
                // Converte a string JSON (ex: "2025-12-09") para java.sql.Date
                return java.sql.Date.valueOf(json.getAsString());
            })
            .create();

    // Constante para Content-Type
    public static final String APPLICATION_JSON = "application/json";

    // Método para fornecer a instância configurada do Gson
    public static Gson getGson() {
        return gson;
    }
}