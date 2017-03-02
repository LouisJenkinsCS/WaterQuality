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
package protocol;

import async.Data;
import async.DataReceiver;
import async.DataValue;
import io.reactivex.Observable;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utilities.JSONUtils;

/**
 *
 * Process AJAX requests sent following this protocol. The protocol can be seen
 * here: https://gist.github.com/LouisJenkinsCS/54bdb64628a478bfc57d77dd64c6bb82
 * 
 * @author Louis Jenkins
 */
public class JSONProtocol implements Protocol<JSONObject, JSONObject> {

    public JSONProtocol() {
        
    }
    
    /**
     * Process a request whose data from the query was already retrieved. The assumption
     * is made that the source contains all data from the query.
     * @param source Data query's results.
     * @return Response
     */
    @Override
    public Observable<JSONObject> processUsing(Data source) {
        return source.getData()
                // Each id signifies a new parameter, and we need to separate the data values
                // for each parameter.
                .groupBy(DataValue::getId)
                // For each group of DataValue (remember they are grouped by the parameter's id)
                // we must construct a unique JSONObject, as per protocol.
                .flatMap((GroupedObservable<Long, DataValue> group) -> {
                    JSONObject obj = new JSONObject();
                    // The key is the actual 'id' for the parameter.
                    obj.put("param", group.getKey());
                    obj.put("name", DataReceiver.getParameterName(group.getKey()));

                    // Create a JSONArray filled with the DataValues. They are sorted
                    // by timestamp to make it easier on the front-end.
                    return group
                            .sorted()
                            // DataValue -> JSONObject
                            .map((DataValue dv) -> {
                                JSONObject dataField = new JSONObject();
                                dataField.put("timestamp", dv.getTimestamp().getEpochSecond() * 1000);
                                dataField.put("value", dv.getValue());
                                return dataField;
                            })
                            // Collect all JSONObjects into a list
                            .buffer(Integer.MAX_VALUE)
                            // Construct the JSONArray to contain the list of JSONObjects
                            .map((List<JSONObject> list) -> {
                                JSONArray arr = new JSONArray();
                                arr.addAll(list);
                                return arr;
                            })
                            // As per protocol, the JSONArray is stored in the root
                            // "data" field of each parameter
                            .map((JSONArray arr) -> {
                                obj.put("data", arr);
                                return obj;
                            });
                })
                // Obtains all JSONObjects containing the data values for each parameter.
                .buffer(Integer.MAX_VALUE)
                // Same as before, construct a JSONArray from it.
                .map((List<JSONObject> list) -> {
                    JSONArray arr = new JSONArray();
                    arr.addAll(list);
                    return arr;
                })
                // As per protocol, the JSONArray containing the per-parameter data
                // is held in the "resp" field.
                .map((JSONArray arr) -> {
                    JSONObject response = new JSONObject();
                    
                    // TODO: Refactor .jsp to generate table and descriptions.
                    response.put("descriptions", DataReceiver.generateDescriptions(source));
                    response.put("table", DataReceiver.generateTable(source));
                    response.put("resp", arr);
                    return response;
                });
    }
    
    /**
     * Process the request, returning an asynchronous response. The request MUST
     * follow the protocol.
     * @param request Request data.
     * @return Async request result.
     */
    @Override
    public Observable<JSONObject> process(JSONObject request) { 
        return Observable.just(request)
                // From the request, process and obtain the data for it.
                .map((JSONObject queryData) -> {
                    Long startTime = (Long) queryData.get("startTime");
                    Long endTime = (Long) queryData.get("endTime");
                    JSONArray arr = (JSONArray) queryData.get("params");
                    Long id[] = (Long[]) arr.toArray(new Long[0]);
                    
                    System.out.println("startTime: " + startTime + ", endTime: " + endTime + ", ids: " + Arrays.deepToString(id));
                    return DataReceiver.getData(Instant.ofEpochMilli(startTime), Instant.ofEpochMilli(endTime), id);
                })
                // Defer further processing
                .flatMap(this::processUsing);
    }
    
    
}
