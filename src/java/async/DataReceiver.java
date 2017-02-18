/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package async;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.javatuples.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author lpj11535
 */
public class DataReceiver {
    
    private static final Map<String, Long> idMap = new HashMap<>();
    
    static {
        // Fill the map so we only need to obtain it once on startup.
        // The map will contain a list of identifiers (being the very same
        // identifier displayed in dashboard.jsp) to their actual identifier. This
        // MUST be refactored before actual release, but should be sufficient for now.
        
        // This is the URL used to obtain ALL data for the sensor within the past
        // 15 minutes. This is important as, again, it holds ALL of the data. So
        // we can use this to construct our map.
        String url = "https://ienvironet.com/api/data/last/0A178632?auth_token=avfzf6dn7xgv48qnpdhqzvlkz5ke7184";
        
        // We want to make some API calls next. Given the above URL, we can then
        // make a connection and obtain the JSON data sent.
        
        /*
            Walkthrough of what each computation in the pipeline does, and why.
        */
        
        // Observable now emits a single item, the URL seen above.
        Observable.just(url)
                // From the URL, construct an actual URL (remember, the above is a String)
                .map(URL::new)
                // From the actual URL, form a connection with the endpoint (URL -> URLConnection)
                .map(URL::openConnection)
                // Need to set this or else API will refuse our request. We need to act like a web browser
                .doOnNext(conn -> conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"))
                // Attempt to form actual connection
                .doOnNext(URLConnection::connect)
                // Obtain the input stream, of which we can obtain the data it is sending
                .map(URLConnection::getInputStream)
                // Obtain the data from the Stream as a String
                .map(stream -> {
                    BufferedReader r  = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));

                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        sb.append(line);
                    }
                    
                    return sb.toString();
                })
                // From that received string, since it is in JSON, we can parse it into a JSONObject.
                .map(str -> (JSONObject) new JSONParser().parse(str))
                // For an example of the format given see: https://gist.github.com/LouisJenkinsCS/cca0069178f194329d55aabf33c28418
                // We need to obtain the "data" parameter, which a JSONArray.
                .map((JSONObject obj) -> (JSONArray) obj.get("data"))
                // A JSONArray implements the Iterable interface, just like any Collection such as an ArrayList would.
                // Because of this we can go from the Collection itself to the items it contains. Hence, a collection
                // containing ({1, 2, 3}) will be converted into the respective elements, (1, 2, 3).
                .flatMap(Observable::fromIterable)
                // With the JSONObjects being the data we need, we can easily parse it into tuples
                // of (Name, Id) pairs, or rather Pair<String, Long>
                .map(obj -> Pair.with(((JSONObject) obj).get("name") + " (" + ((JSONObject)obj).get("unit") + ")", (Long) ((JSONObject)obj).get("id")))
                .doOnNext(System.out::println)
                .subscribe(pair -> idMap.put(((Pair<String, Long>) pair).getValue0(), ((Pair<String, Long>)pair).getValue1()));
    };
        
    public static Observable<String> getParameters() {
        return Observable.fromIterable(idMap.keySet());
    }
    
    public static Pair<String, String> generateGraph(String key) {
        long id = idMap.get(key);
        List<Pair<String, Double>> data = new ArrayList<>();
        String url = "https://ienvironet.com/api/data/" + Instant.now().minus(Period.ofWeeks(4)).getEpochSecond() 
                + ":" + Instant.now().getEpochSecond() + "/" + id + ".json?auth_token=avfzf6dn7xgv48qnpdhqzvlkz5ke7184";
        getData(url)
                .map((JSONObject obj) -> (JSONArray) obj.get("data"))
                .flatMap(Observable::fromIterable)
                .map(obj -> Pair.with((String)((JSONObject) obj).get("timestamp"), (Double) ((JSONObject) obj).get("value")))
                .blockingSubscribe(pair -> data.add((Pair<String, Double>) pair));
                
        
        String timeStr = data
                .stream()
                .map(p -> "\"" +  p.getValue0() + "\"")
                .collect(Collectors.joining(","));
        String dataStr = data
                .stream()
                .map(p -> "" + p.getValue1())
                .collect(Collectors.joining(","));
        
        StringBuilder out = new StringBuilder();
        String chart = 
            "<script>" +
            "var ctx = document.getElementById('myChart').getContext('2d');\n" + 
            "var myChart = new Chart(ctx, {\n" +
             "  type: 'line',\n" +
             "  data: {\n" +
             "    labels: [" + timeStr + "],\n" +
             "    datasets: [{\n" +
             "      label: '" + key + "',\n" +
             "      data: [" + dataStr + "],\n" +
             "      backgroundColor: 'transparent', borderColor: 'orange'\n" +
             "    }]\n" +
             "  }\n" +
             "});" + 
             "</script>";
        
        String table = "<script>"
                + "var table = document.getElementById('Table').innerHTML = "
                + "\"<table border='1'><tr><th>Timestamp</th><th>Value</th></tr>";
        table += data
                .stream()
                .map(p -> "<tr><td>" + p.getValue0() + "</td><td>" + p.getValue1() + "</td></tr>")
                .collect(Collectors.joining());
        table += "</table>\"</script>";
        
        return Pair.with(chart, table);
    }
    
    public static Observable<JSONObject> getData(String url) {
        return Observable.just(url)
                .map(URL::new)
                .map(URL::openConnection)
                .doOnNext(conn -> conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"))
                .doOnNext(URLConnection::connect)
                .map(URLConnection::getInputStream)
                .map(stream -> {
                    BufferedReader r  = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));

                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        sb.append(line);
                    }
                    
                    return sb.toString();
                })
                .map(str -> (JSONObject) new JSONParser().parse(str));
    }
}
