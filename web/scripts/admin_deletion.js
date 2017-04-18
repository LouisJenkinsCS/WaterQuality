/*
 * Administrators may want to delete errant data points,
 * or remove data that was entered incorrectly. The functionality
 * for removal of data is defined here.
 * 
 * DateTimePicker is utilized to allow users to filter the
 * data they wish to choose from. The DataTables API (https://datatables.net/)
 * is used to display the desired data, and allows the user to select
 * single or multiple entries.
 */

// del_options is a global variable to allow all following functions to use
// the same list of parameters
var del_options = "";
var cached_deletion_ids = new Map();

/*
 * Upon loading admin.jsp, this function appends the HTML which builds
 * the page which the user interacts with.
 */
function loadDelete() {
    // A mask of 3 is used to indicate to the servlet that we
    // want parameter names for both sensor data, and manually collected data
    var ALL_MASK = 3;
    var parameterRequest = new ParameterRequest(ALL_MASK);
    parameterRequest.action = "getParameters";

    post("AdminServlet", parameterRequest, function (response) {
        
        var resp = new ParameterResponse(response);

        /*
         * The JSON-formatted response is parsed appropriately, and the
         * drop-down options are created.
         */
        for (var k = 0; k < resp.data.length; k++) {
            if (resp.data[k]["mask"] === 1) {
                del_options += '<option disabled selected hidden>Please Choose...</option>';
                del_options += '<option disabled>-----Sensor Parameters-----</option>';
            } else
                del_options += '<option disabled>-----Manual Parameters-----</option>';

            resp.descriptors = resp.data[k]["descriptors"];
            resp.names = [];

            for (var i = 0; i < resp.descriptors.length; i++) {
                resp.piece = resp.descriptors[i];
                cached_deletion_ids.set(resp.piece["name"],resp.piece["id"]);
                resp.names.push(resp.piece["name"]);                
            }

            (resp.names).forEach(function (item) {
                del_options += '<option>';
                del_options += item;
                del_options += '</option>';
            });
        }

        $('#Delete_Data').append(
                '<div class="large_text">Time Frame:</div>' +
                '<div id="dateInstructDiv">Start Date:</div>' +
                '<input id="delete_startdate" type="text">' +
                '<input id="delete_starttime" type="text"></div>' +
                '<div id="dateInstructDiv">End Date:</div>' +
                '<input class="dateselector" id="delete_enddate" type="text">' +
                '<input class="dateselector" id="delete_endtime" type="text"></div>' +
                '<br/><br/>' +
                '<div class="large_text">Parameter to delete:</div>' +
                '<select id="delete_param" onchange="filterData()">' + del_options +
                '</select><br/><br/>' +
                '<div class="large_text">Please select the data entry from below:</div>' +
                '<table id="delete_table">' +
                '<thead><tr><th>Date-Time</th><th>Actual-Time</th><th>Name</th><th>Value</th></tr></thead>' +
                '</table><br/>' +
                '<input type="submit" id="delete_data" value="Delete Data" onclick="deleteData()">'
                );

        // The DataTable's columns and formatting is defined here
        $('#delete_table').DataTable({
            columns: [
                {title: "Date-Time"},
                {title: "Actual-Time"},
                {title: "Name"},
                {title: "Value"}
            ],
            "order": [[1, "desc"]],
            select: 'multi'
        });
        
        createDatePickers();
    });
}

/*
 * This function is called whenever the user selects a new parameter,
 * or changes the timeframe from which data is retrieved. Within this 
 * function, the desired data is retrieved, the local time zone is accounted
 * for, and the request to the servlet for data is defined.
 */
function filterData() {
    // The entered/selected parameter is stored
    var $paramName = $('#delete_param').val();

    var deleteStartDate = new Date($('#delete_startdate').val());
    if (deleteStartDate.dst())
        deleteStartDate = deleteStartDate.getTime() - 14400000;
    else
        deleteStartDate = deleteStartDate.getTime() - 18000000;
    
    var deleteEndDate = new Date($('#delete_enddate').val());
    if (deleteEndDate.dst())
        deleteEndDate = deleteEndDate.getTime() - 14400000;
    else
        deleteEndDate = deleteEndDate.getTime() - 18000000;
    
    var deleteStartTime = $('#delete_starttime').val();
    var deleteEndTime = $('#delete_endtime').val();

    var starttime = deleteStartTime.split(':');
    var endtime = deleteEndTime.split(':');

    var startDateTime = new Date(deleteStartDate + starttime[0] * 3600000 + starttime[1] * 60000).getTime();
    var endDateTime = new Date(deleteEndDate + endtime[0] * 3600000 + endtime[1] * 60000).getTime();
    
    
    var filterRequest = {
        action: 'getDataDeletion',
        parameter: $paramName,
        start: startDateTime,
        end: endDateTime
    };

    post("AdminServlet", filterRequest, function (resp) {
        if (resp.hasOwnProperty("status")) {
            window.alert("Error fetching data from AdminServlet...\nError: \"" + resp.status + "\"");
            return;
        }

        var dataTable = $('#delete_table').DataTable();
        dataTable.clear();

        // Rows are added to the DataTable in a loop
        var data = JSON.parse(resp)["data"];
        
        for (var i = 0; i < data.length; i++) {
            var item = data[i];
            var name = item.name;
            var dataValues = item.dataValues;
            for (var j = 0; j < dataValues.length; j++) {
                item = dataValues[j];
                dataTable.rows.add([[formatDateSimple2(new Date(item["timestamp"])), item["timestamp"], name, item["value"]]]);
            }
        }

        dataTable.draw();
    });
}

/*
 * When the user submits their request to delete data, the DataTable's
 * state is stored, selections are read and parsed, and the request is formed
 * and sent.
 */
function deleteData() {
    var table = $('#delete_table').DataTable();
    var selectedCells = table.rows('.selected').data();
    var deletionIDs = [];
    for (var i = 0; i < selectedCells.length; i++)
        deletionIDs.push(selectedCells[i][1]);

    post("AdminServlet", 
    {action: "RemoveData", data: JSON.stringify({parameter: $('#delete_param').val(), time: deletionIDs})},
    function (resp) {
        alert(resp);
        //Data shown has to be refreshed after deletion occurs
        filterData();
    });
}

/*
 * The DatePickers are initialized here.
 */
function createDatePickers() {
    var date = new Date();
    // The default time range is a month before
    // the current date to today's date
    $("#delete_enddate").datetimepicker({
        controlType: 'select',
        oneLine: true,
        altField: "#delete_endtime"
    })
            .datepicker("setDate", date);

    date.setMonth(date.getMonth() - 1);
    
    $("#delete_startdate").datetimepicker({
        controlType: 'select',
        oneLine: true,
        altField: "#delete_starttime"
    })
            .datepicker("setDate", date);

    changeOnSelect();
}

/*
 * If a new date or time is selected in the DatePicker
 * we want the table to reflect that immediately. This function
 * sets an onSelect listener to call filterData() whenever a
 * new date or time is selected.
 */
function changeOnSelect() {
    $("#delete_enddate").datetimepicker("option", "onSelect", filterData);
    $("#delete_startdate").datetimepicker("option", "onSelect", filterData);
    $("#delete_starttime").datetimepicker("option", "onSelect", filterData);
    $("#delete_endtime").datetimepicker("option", "onSelect", filterData);
}
