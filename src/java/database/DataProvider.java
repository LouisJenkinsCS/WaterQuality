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

import async.DataParameter;
import async.DataValue;
import java.time.Instant;
import java.util.List;

/**
 * A potential interface Tyler's DatabaseManager can implement so I can convert from Netronix
 * to the Database.
 * @author Louis Jenkins
 */
public interface DataProvider {
    /**
     * Called to obtain all parameter values. This can be used to display the names
     * of all data parameters for the front end. The type 'DataParameter' should be
     * similar enough to the one in the async package, and can be changed. If the concern
     * is that different parameters have different types of data, have an interface of
     * what they both share.
     * @return List of all data parameters.
     */
    List<DataParameter> getAllParameters();
    
    /**
     * For how to convert between Instant and LocalDateTime, see the LocalDateTime.toInstant
     * and LocalDateTime.ofInstant methods. It's literally a simple change for my part and
     * for yours. Note: I use Keys, and not the String name. The keys are unique.
     * 
     * DataValue does not need to the async package, but should be similar enough. Note that
     * any filtering is handled by me; do not worry about anything else.
     * @param start Start of range.
     * @param end End of range.
     * @param keys Keys.
     * @return List of data values across range.
     */
    List<DataValue> getDataValueRange(Instant start, Instant end, long ...keys);
}
