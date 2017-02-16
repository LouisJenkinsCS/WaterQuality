/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package async;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import javax.servlet.AsyncContext;

/**
 *
 * @author lpj11535
 */
public class RequestProcessor {
    
    private PublishSubject<AsyncRequest> processor = PublishSubject.create();
    private PublishSubject<Object> responseData = PublishSubject.create();
    
//    public RequestProcessor() {
//        processor
//                .map(this::processConnection)
//                .subscribe(responseData::onNext);
//    }
//    
//    private Object processConnection(AsyncContext request) {
//        AsyncContext context = request.context;
//        String path = request.path;
//        String action = request.context.getRequest().getParameter("control");
//        
//        if (context.getRequest().)
//    }
//    
//    public void process(AsyncContext context) {
//        processor.onNext(context);
//    }
    
}
