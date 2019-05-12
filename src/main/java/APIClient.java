import com.goebl.david.Webb;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class APIClient {
    private static final String ENDPOINT = "https://apitryout.qtestnet.com/";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'hh:mm:ss";

    protected static List<String> getUsersList(String users) {
        return Arrays.asList(users.split("\\s*,\\s*"));
    }

    protected static Date parseJsonDate(String date) {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static JSONArray getProjectList(String projects) {
        try {
            return CDL.rowToJSONArray(new JSONTokener(projects));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static Webb getAuthorisedWebb(String token) {
        Webb webb = Webb.create();
        webb.setBaseUri(ENDPOINT);
        webb.setDefaultHeader(Webb.HDR_AUTHORIZATION, "Bearer " + token);
        return webb;
    }
}
