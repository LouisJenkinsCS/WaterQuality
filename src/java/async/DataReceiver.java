/* BSD 3-Clause License
 *
 * Copyright (c) 2017, Louis Jenkins <LouisJenkinsCS@hotmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *     - Neither the name of Louis Jenkins, Bloomsburg University nor the names of its 
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package async;

import database.DatabaseManager;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.Period;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import utilities.FileUtils;
import utilities.JSONUtils;

/**
 *
 * @author Louis Jenkins
 * 
 * DataReceiver's purpose is to handle all API calls for data, as well as temporarily
 * (until a specific class is built for generation) generating the HTML required
 * for the chart, table, and descriptions.
 */
public class DataReceiver {
    
    /**
     * The maximum amount of data to be fetched from Netronix at once. It is limited to prevent
     * potential denial of service where we have requests asking for, say, years of data at a time.
     * In the case of too many requests asking for too much, we can end up with an OOM situation quickly.
     * This is merely a simple counter-measure to handle actually valid requests, a true DDOS would be
     * impossible to prevent.
     * 
     * Reminder: Netronix gets data in 15 minutes chunks, so you can easily calculate the amount of data
     * being obtained for each query. Too much means more memory usage and backpressure, too little means
     * we become IO Bound.
     */
    private static final Period MAX_CHUNK_PERIOD = Period.ofWeeks(4);
    
    /**
     * URL to obtain the most recent data; useful for obtaining all sensor parameter names, units, etc.
     */
    public static final String LATEST_DATE_URL = "https://ienvironet.com/api/data/last/0A178632?auth_token=avfzf6dn7xgv48qnpdhqzvlkz5ke7184";
    
    /**
     * Obtain DataValues from a query for all supplied keys that are within the
     * time frame of start to end. The data is returned as Data, which can be passed
     * to other DataReceiver methods, or even used by itself. For information on
     * how Data can be used, see the JavaDoc.
     * @param start Beginning of time frame
     * @param end End of time frame.
     * @param keys Selected data.
     * @return Data containing data from query.
     */
    public static Data getData(Instant start, Instant end, Long ...keys) {
        return new Data(Flowable
                // For each key
                .fromArray(keys)
                .flatMap((Long key) ->
                        DatabaseManager.parameterIdToName(key)
                                .flatMapPublisher(name -> DatabaseManager.getDataValues(start, end, name))
                    
                // 'replay' is a way to say that we want to take ALL items up to this point (being the DataValues), cache it, and then
                // resend it each and every time it is subscribed to (pretty much meaning this becomes reusable).
                ).replay());
    }
    
    /**
     * Obtains chunks of data based on request, one month at a time.
     * @param start
     * @param end
     * @param key
     * @return 
     */
    private static Flowable<DataValue> getChunkedData(Instant start, Instant end, long key) {
        // Require a mutable container to be used and updated within container.
        AtomicReference<Instant> currentTime = new AtomicReference<>(start);
        Queue<DataValue> chunks = new ArrayDeque<>();
        // Note: Flowable is synchronous, hence the queue does not need to require extra synchronization
        return Flowable.generate(emitter -> {
            // Request more data...
            while (chunks.isEmpty()) {
                // Get current week of data, advance for next query by a week.
                Instant current = currentTime.getAndUpdate(curr -> curr.plus(MAX_CHUNK_PERIOD));
                // Empty and obtained all data? We're finished.
                if (!current.isBefore(end)) {
                    emitter.onComplete();
                    return;
                }
                
                // Otherwise obtain data for that week...
                getJSONData(getParameterURL(current, current.plus(MAX_CHUNK_PERIOD), key))
                        .subscribeOn(Schedulers.io())
                        // For an example of the format given see: https://gist.github.com/LouisJenkinsCS/cca0069178f194329d55aabf33c28418
                        // We need to obtain the "data" parameter, which a JSONArray.
                        .map((JSONObject obj) -> (JSONArray) obj.get("data"))
                        // A JSONArray implements the Iterable interface, just like any Collection such as an ArrayList would.
                        // Because of this we can go from the Collection itself to the items it contains. Hence, a collection
                        // containing ({1, 2, 3}) will be converted into the respective elements, (1, 2, 3).
                        .flatMap(JSONUtils::flattenJSONArray)
                        // We take both the timestamp (X-Axis) and the value (Y-Axis).
                        // This data is what is returned as a DataValue.
                        .map((JSONObject obj) -> new DataValue(key, (String) obj.get("timestamp"), (Double) obj.get("value")))
                        // The server sometimes sends duplicate data values for timestamps, so we filter them here.
                        .distinct(dv -> dv.getTimestamp())
                        .blockingForEach(chunks::add);
                
                System.out.println("(" + current.toString() + ") - Retrieved " + chunks.size() + " units of data for " + DatabaseManager.remoteSourceToDatabaseId(key).flatMap(DatabaseManager::parameterIdToName).blockingGet());
            }
                        
            emitter.onNext(chunks.remove());
        });
    }
    
    public static Data getRemoteData(Instant start, Instant end, Long ...keys) {
        // We request data from Netronix in monthly intervals.
        return new Data(Flowable
                // For each key
                .fromArray(keys)
                // Chunk data for each key...
                .map((Long key) -> getChunkedData(start, end, key))
                // Merge all into one continuous stream
                .buffer(Integer.MAX_VALUE)
                .flatMap(Flowable::merge)
                // 'replay' is a way to say that we want to take ALL items up to this point (being the DataValues), cache it, and then
                // resend it each and every time it is subscribed to (pretty much meaning this becomes reusable).
                .replay());
    }
    
    /**
     * Obtain the API URL from the given time frame and parameter ID.
     * @param start Start.
     * @param end End.
     * @param id Id.
     * @return API URL.
     */
    private static String getParameterURL(Instant start, Instant end, long id) {
       return "https://ienvironet.com/api/data/" + start.getEpochSecond() + ":" + end.getEpochSecond() + "/" + id + ".json?auth_token=avfzf6dn7xgv48qnpdhqzvlkz5ke7184";
    }
    
    
    
    /**
     * Obtain the JSON from the passed URL. The URL SHOULD be one that corresponds to
     * the environet API. The data is taken as JSON.
     * @param url
     * @return Data from URL as JSON.
     */
    public static Observable<JSONObject> getJSONData(String url) {
        return Observable.just(url)
                // From the URL, construct an actual URL (remember, the above is a String)
                .map(URL::new)
                // From the actual URL, form a connection with the endpoint (URL -> URLConnection)
                .map(URL::openConnection)
                // Need to set this or else API will refuse our request. We need to act like a web browser
                .doOnNext((URLConnection conn) -> conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"))
                // Attempt to form actual connection
                .doOnNext(URLConnection::connect)
                // Obtain the input stream, of which we can obtain the data it is sending
                .map(URLConnection::getInputStream)
                // Obtain the data from the Stream as a String
                .map((InputStream stream) -> {
                    BufferedReader r  = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));

                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        sb.append(line);
                    }
                    
                    return sb.toString();
                })
                // Remove any and all unicode characters
//                .map((String json) -> json.replaceAll("\\P{Print}", ""))
                // From that received string, since it is in JSON, we can parse it into a JSONObject.
                .map((String json) -> (JSONObject) new JSONParser().parse(json));
    }
}
