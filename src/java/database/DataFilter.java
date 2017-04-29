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
package database;

import async.DataReceiver;
import async.DataValue;
import com.github.davidmoten.rx.jdbc.Database;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.log4j.BasicConfigurator;
import org.javatuples.Pair;

/**
 *
 * @author Louis Jenkins
 */
public class DataFilter {
    
    public AtomicReference<Set<Long>> filter = new AtomicReference<>(new TreeSet<>());
    public static Map<Long, DataFilter> INSTANCES = new ConcurrentHashMap<>();
    
    static {
        init();
    }
    
    private final long id;
    
    /**
     * Initialize all filters (for each identifier in the database). If a filter already
     * exists for that data parameter, it will restore it.
     */
    public static void init() {
        Database db = Database.from(Web_MYSQL_Helper.getConnection());
        
        db.select("select id from data_parameters")
                .getAs(Long.class)
                .flatMap(id -> db.select("select time from data_filter where parameter_id = " + id)
                        .getAs(Long.class)
                        .toList()
                        .map(list -> {
                            Set<Long> set = new TreeSet<>();
                            set.addAll(list);
                            return set;
                        })
                        .timeout(30, TimeUnit.SECONDS)
                        .onErrorReturn((Throwable t) -> {
                            DatabaseManager.LogError("Error occurred while restoring data filter for id: " + id + " of type: " + t.getClass().getName() + "\nMessage: " + t.getMessage());
                            return new TreeSet<>();
                        })
                        .map((Set<Long> timestamps) -> Pair.with(id, timestamps))
                )
                .toMap((Pair<Long, Set<Long>> pair) -> pair.getValue0(), (Pair<Long, Set<Long>> pair) -> new DataFilter(pair.getValue0(), pair.getValue1()))
                .toBlocking()
                .subscribe(INSTANCES::putAll);
                
    }
    
    private DataFilter(long id, Set<Long> set) {
        this.id = id;
        if (set != null) {
            filter.set(set);
        }
    }
    
    public static DataFilter getFilter(long id) {
        if (!INSTANCES.containsKey(id)) {
            DataFilter dataFilter = new DataFilter(id, null); 
            INSTANCES.putIfAbsent(id, dataFilter);
            System.out.println("Created filter for id: " + id);
            return dataFilter;
        }
        
        return INSTANCES.get(id);
    }
    
    
    
    
    /**
     * Uses a RCU (Read-Copy-Update) synchronization strategy to update the filter.
     * This approach allows the filter to be used by clients, locally, without needing
     * to access the database, while also allowing concurrent writes (and since technically
     * we will only have one concurrent writer, contention is no issue; in the case of contention
     * it is still safe, as it is linearizable at the Compare-And-Set operation, and the loser just
     * gets to start over).
     * @param times Set of new times to delete
     */
    public void add(Set<Long> times) {
        while (true) {
            // Read...
            Set<Long> currentFilter = filter.get();
            if (currentFilter.containsAll(times)) {
                System.out.println("Contains all items, skipping...");
                return;
            }
            
            // Copy..
            Set<Long> localFilter = new TreeSet<>(currentFilter);
            localFilter.addAll(times);
            // Update...
            if (filter.compareAndSet(currentFilter, localFilter)) {
                break;
            }
        }
        
        DatabaseManager.insertFilteredData(id, filter.get());
    }
    
    public Observable<DataValue> filter(Observable<DataValue> data) {
        Set<Long> currentFilter = filter.get();
        return data.filter((DataValue dv) -> !currentFilter.contains(dv.getTimestamp().toEpochMilli()));
    }
    
    public Flowable<DataValue> filter(Flowable<DataValue> data) {
        Set<Long> currentFilter = filter.get();
        return data.filter((DataValue dv) -> !currentFilter.contains(dv.getTimestamp().toEpochMilli()));
    }
}
