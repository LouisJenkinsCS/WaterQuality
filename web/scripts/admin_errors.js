
//This function simply pulls the AJAX_magic.js script
//to allow the current script to use AJAX functions
$.getScript("scripts/AJAX_magic.js", function () {});
$.getScript("scripts/datetimepicker.js", function () {});

function fillPageErrors()
{
    $('#Errors').append(
            '<div class="large_text">Time Frame:</div>' +
            '<div id="dateInstructDiv">Start Date:</div>' +
            '<input  id="errors_startdate" type="text"> ' +
            '<input id="errors_starttime" type="text"></div>' +
            '<div id="dateInstructDiv">End Date:</div>' +
            '<input class="dateselector" id="errors_enddate" type="text">' +
            '<input class="dateselector" id="errors_endtime" type="text">' +
            '<br/><br/>' +
            '<table id="error_table">' +
            '<thead><tr><th>Date-Time</th><th>Error Message</th></tr></thead>' +
            '</table><br/>'
            );
    $('#error_table').DataTable({
        columns: [
            {title: "Date-Time"},
            {title: "Error Message"}
        ],
        "order": [[ 0, "desc" ]]
    });


    var date = new Date();
    $("#errors_enddate").datetimepicker({
        controlType: 'select',
        oneLine: true,
        altField: "#errors_endtime"
    })
            .datepicker("setDate", date);

    date.setMonth(date.getMonth() - 1);
    $("#errors_startdate").datetimepicker({
        controlType: 'select',
        oneLine: true,
        altField: "#errors_starttime"
    })
            .datepicker("setDate", date);

    filterErrors();
}
;

function filterErrors()
{
    //To store the string to append to the document
    var htmlstring = "";

    //The entered/selected parameters are stored
    var deleteStartDate = new Date($('#errors_startdate').val());
    if (deleteStartDate.dst())
        deleteStartDate = deleteStartDate.getTime() - 14400000;
    else
        deleteStartDate = deleteStartDate.getTime() - 18000000;

    var deleteStartTime = $('#errors_starttime').val();

    var deleteEndDate = new Date($('#errors_enddate').val());
    if (deleteEndDate.dst())
        deleteEndDate = deleteEndDate.getTime() - 14400000;
    else
        deleteEndDate = deleteEndDate.getTime() - 18000000;

    var deleteEndTime = $('#errors_endtime').val();

    var starttime = deleteStartTime.split(':');
    var endtime = deleteEndTime.split(':');

    var startDateTime = new Date(deleteStartDate + starttime[0] * 3600000 + starttime[1] * 60000).getTime();
    var endDateTime = new Date(deleteEndDate + endtime[0] * 3600000 + endtime[1] * 60000).getTime();

    var filterRequest = {action: 'getFilteredErrors',
        start: startDateTime,
        end: endDateTime
    };
    
    post("AdminServlet", filterRequest, function (resp) {
        if (resp.hasOwnProperty("status")) {
            window.alert("Error Fetching Data from AdminServlet...\nError: \"" + resp.status + "\"");
            return;
        }
        
        var dataTable = $('#error_table').DataTable();

        dataTable.clear();

        var errors = JSON.parse(resp)["errors"];
        var htmlstring = '<thead><tr><th>Date-Time</th><th>Error Message</th></tr></thead>';
        for (var i = 0; i < errors.length; i++)
        {
            var item = errors[i];
            dataTable.rows.add([[formatDateSimple(item["time"]), item["errorMessage"]]]);
        }
       
        dataTable.draw();
    });

}

$(function () {
    $("#errors_startdate").datetimepicker("option", "onSelect", filterErrors);
    $("#errors_enddate").datetimepicker("option", "onSelect", filterErrors);
    $("#errors_starttime").datetimepicker("option", "onSelect", filterErrors);
    $("#errors_endtime").datetimepicker("option", "onSelect", filterErrors);
});