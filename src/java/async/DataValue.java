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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import org.javatuples.Pair;

/**
 *
 * @author Louis Jenkins
 * 
 * Represents data values for different parameters.
 */
public class DataValue implements Comparable<DataValue> {
    long id;
    Instant timestamp; 
    Double value;
    
    public DataValue(long id, Instant timestamp, Double value) {
        this.value = value;
        this.id = id;
        this.timestamp = timestamp;
    }
    
    public DataValue(long id, String timestamp, Double value) throws ParseException {
        // Parse the timestamp into an equivalent Instant
        // Example of timestamp can be seen here: 
        // https://gist.github.com/LouisJenkinsCS/cca0069178f194329d55aabf33c28418#file-environet_api_data_specific-json-L12
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss z");
        this.timestamp = format.parse(timestamp).toInstant();
        this.value = value;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public int compareTo(DataValue o) {
        return this.timestamp.compareTo(o.timestamp);
    }

    @Override
    public String toString() {
        return "DataValue{" + "id=" + id + ", timestamp=" + timestamp.toEpochMilli() + ", value=" + value + '}';
    }
    
    
}
