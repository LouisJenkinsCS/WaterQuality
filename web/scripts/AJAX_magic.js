/* 
 * Author: Brandon Meglathery
 * 
 * Javascript defining a generic AJAX function
 */

/*
 * @param method - 'POST' or 'GET' to define the request type
 * @param target - the url the request is passed to
 * @param data - the {key, value} passed
 * @param callback - the function called upon a successful request
 */
function post_get(method, target, data, callback)
{
    $.ajax({
        type: method,
        url: target,
        data: data,
        success: callback,
        error: function () {
            alert('Error connecting to:' + target +'.');
        }
    });
}