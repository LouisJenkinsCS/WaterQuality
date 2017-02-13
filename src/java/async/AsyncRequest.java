/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package async;

import javax.servlet.AsyncContext;

/**
 *
 * @author lpj11535
 */
public class AsyncRequest {
    
    public final String path;
    public final AsyncContext context;

    public AsyncRequest(String path, AsyncContext context) {
        this.path = path;
        this.context = context;
    }
}
