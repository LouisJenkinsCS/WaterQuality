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
import io.reactivex.Observable;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Louis Jenkins
 */
public class DataFilter {
    
    private static DataFilter INSTANCE = new DataFilter(); 
    
    public AtomicReference<Set<Long>> filter = new AtomicReference<>(new TreeSet<>());
    public static Map<Long, DataFilter> INSTANCES = new ConcurrentHashMap<>();
    
    private DataFilter() {
        
    }
    
    public static DataFilter getFilter(long id) {
        if (!INSTANCES.containsKey(id)) {
            INSTANCES.putIfAbsent(id, new DataFilter());
            System.out.println("Created filter for id: " + id);
        }
        
        return INSTANCES.get(id);
    }
    
    /**
     * Must be called before server is shutdown; saves everything to database to be used later.
     */
    public static void destroy() {
        
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
        
        System.out.println("Added Dates: " + times);
    }
    
    public Observable<DataValue> filter(Observable<DataValue> data) {
        Set<Long> currentFilter = filter.get();
        return data.filter((DataValue dv) -> !currentFilter.contains(dv.getTimestamp().toEpochMilli()));
    }
    
    
    public static void main(String[] args) {
        DataFilter instance = DataFilter.INSTANCE;
        Instant start = Instant.now().minus(Period.ofWeeks(8)).truncatedTo(ChronoUnit.DAYS);
        Instant filterStart = Instant.now().minus(Period.ofWeeks(4)).truncatedTo(ChronoUnit.DAYS);
        Instant end = Instant.now();
        
        Set<Long> filteredTime = new TreeSet<>();
        for (Instant time = filterStart; time.isBefore(end); time = time.plus(Duration.ofMinutes(15))) {
            filteredTime.add(time.toEpochMilli());
        }
        instance.add(filteredTime);
        
        DataReceiver.getRemoteData(start, end, 1050296639L)
                .getData()
                .compose(instance::filter)
                .blockingSubscribe(System.out::println);
    }
}
