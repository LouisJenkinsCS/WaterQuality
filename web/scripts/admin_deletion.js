/*
 * Deletion requests are handled here.
 * 
 */

$.getScript("scripts/AJAX_magic.js", function () {});

var deleteRequest = {action: 'RemoveData',
    dataName: 'TestData',
    startTime: '2007-12-03T10:15:30',
    endTime: '2017-03-19T09:15:00'};

var options_params = "";
var dataRequest = {action: 'getManualItems'};

function deleteTheStuff()
{
    get("AdminServlet", dataRequest, function (response)
    {
        console.log(response);
        console.log("Connection made!" + response);
        var thing = JSON.parse(response)["data"];
        for (var i = 0; i < thing.length; i++)
        {
            options_params += '<option>';
            var subthing = thing[i];
            console.log(subthing["name"]);
            options_params += subthing["name"];
            options_params += '</option>';
        }

        console.log("Thing: " + thing);
        console.log("Subthing: " + subthing["name"]);

        $('#Delete_Data').append(
                '<div class="large_text">Time Frame:</div>' +
                '<div id="dateInstructDiv">Start Date to End Date (Format: yyyy-mm-ddThh:mm:ss)</div>' +
                '<div id="dateselectordiv" onclick="dateLimits();">' +
                '<input class="dateselector" id="delete_startdate" type="date" min="2016-01-01" max="" value="2017-03-15T08:15:00"> to ' +
                '<input class="dateselector" id="delete_enddate" type="date" min="2016-01-01" max="" value="2017-03-20T08:15:00"></div>' +
                '<div class="large_text">Parameter to delete:</div>' +
                '<select id="delete_param">' + options_params +
                '</select><br/><br/>' +
                '<button type="button" onclick="filterData()">Filter</button><br/><br/>' +
                '<div class="large_text">Please select the data entry from below:</div>' +
                '<table id="deletion_space">' +
                '<tr><th>Date</th><th>Time</th><th>Parameter</th><th>Value</th></tr>' +
                '</table>'
                );
    });
}

function filterData()
{
    var $paramName = $('#delete_param').val();
    var $deleteStart = $('#delete_startdate').val();
    var $deleteEnd = $('#delete_enddate').val();

    var filterRequest = {action: 'getFilteredData',
        parameter: '$paramName',
        startTime: '$deleteStart',
        endTime: '$deleteEnd'};

    post("AdminServlet", filterRequest, function (resp) {
        var items = JSON.parse(resp)["data"];
        var filteredItems = $(items).filter(function (index) {
            return items[index].name === $paramName;
        });
        console.log(JSON.stringify(filteredItems));
    });

    filteredItems.forEach(function() {
        
    });
    $('#deletion_space').append(
            '<tr id = deletion_row class = datadeletion>'
            + '<td><input type="date" name="data_date" id = "date" value=2007-12-03></td>'
            + '<td><input type="time" name="data_time" id = "time" value=10:15:30></td>'
            + '<td><select id="select_param">' + options_params + '</select></td>'
            + '<td><input type="text" name="value" id = "value" value=3.0></td>'
            + '</tr>')

    console.log("Filtered Data: " + $paramName + " from " + $deleteStart + " to " + $deleteEnd);
}