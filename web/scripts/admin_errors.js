
//This function simply pulls the AJAX_magic.js script
//to allow the current script to use AJAX functions
$.getScript("scripts/AJAX_magic.js", function () {});
$.getScript("scripts/general.js", function () {});
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
            '<button type="button" onclick="filterErrors()">Filter</button><br/><br/>' +
            '<table id="error_table">' +
            '<thead><tr><th>Date-Time</th><th>Error Message</th></tr></thead>' +
            '</table><br/>'
            );
    $('#error_table').DataTable({
        columns: [
            {title: "Date-Time"},
            {title: "Error Message"}
        ]
    });
    
    $(function () 
    {
        var date = new Date();
        //$( "#delete_endtime" ).timepicker();
        //$( "#delete_starttime" ).timepicker();
        $("#errors_enddate").datetimepicker({
            controlType: 'select',
            oneLine: true,
            timeFormat: 'hh:mm tt',
            altField: "#errors_endtime"
        })
                .datepicker("setDate", date);

        date.setMonth(date.getMonth() - 1);
        $("#errors_startdate").datetimepicker({
            controlType: 'select',
            oneLine: true,
            timeFormat: 'hh:mm tt',
            altField: "#errors_starttime"
        })
                .datepicker("setDate", date);
    });
    
    
};

function filterErrors()
    {


        //To store the string to append to the document
        var htmlstring = "";

        //The entered/selected parameters are stored
        var $deleteStartDate = new Date($('#errors_startdate').val()).getTime();
        var $deleteEndDate = new Date($('#errors_enddate').val()).getTime();

        var filterRequest = {action: 'getFilteredErrors',
            start: $deleteStartDate,
            end: $deleteEndDate
        };

        /*
         * POST request:
         * {
         *  action: 'getFilteredErrors',
         *  startDate : '3/19/2017',
         *  endDate : '3/20/2017',
         *  startTime : '08:00',
         *  endTime : '18:00'
         * }
         * 
         * POST response:
         * data: [
         *  {
         *      entryID : '1',
         *      name : 'Soluble Reactive Phosphorus',
         *      submittedBy : 'Test User',
         *      date : '3/20/2017',
         *      time : '08:30'
         *      value : '4.0'
         *  }
         * ]
         * 
         */
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
                dataTable.rows.add([[formatDate(new Date(item["time"])),item["errorMessage"]]]);
            }
            dataTable.draw();
            $('#error_table tbody').on('click', 'td', function () {
                var cellData = dataTable.cell(this).data();
                console.log("cellData" + cellData);
            });

        });
    }