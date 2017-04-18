/*
 * Author: Brandon Meglathery
 * 
 * AJAX script for Dashboard
 */

var $sample = $('#dateselectordiv');

$('#data_submit').on('click', function () {


    var data = {
        name: $sample.val()
    };

    $.ajax({
        type: 'POST',
        url: 'http://rest.learncode.academy/api/Brandons/friends',
        data: data,
        success: function () {
//            console.log('Connected');
        },
        error: function () {
            alert('Error connecting');
        }
    });
});