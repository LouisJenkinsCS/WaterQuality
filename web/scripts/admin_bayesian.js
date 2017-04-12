
$.getScript("scripts/datetimepicker.js", function () {});

function fillBayesianContent()
{
    $('#Bayesian').append(
            '<div class="large_text">Select your day:</div>' +
            '<div id="dateInstructDiv"></div>' +
            '<input  id="bayesian_startdate" type="text">' +
            '<input id="bayesian_starttime" type="text"></div>'
            +'<br/><br/><div>Click the button below to download your selected '
            + 'day\'s data. Data parameters downloaded are:</div></div>'
            + 'Time, Date, Barometric pressure, PAR, Depth, Dissolved oxygen, '
            + 'and Water temperature.<br/><br/>'
            + '<button type = "button" onclick="bayesianButton()">Download</button>'
            );

    $(function () {
        var date = new Date();
//            $( "#delete_endtime" ).timepicker();
//            $( "#delete_starttime" ).timepicker();
        $("#bayesian_enddate").datetimepicker({
            controlType: 'select',
            oneLine: true,
            altField: "#bayesian_endtime"
        })
                .datepicker("setDate", date);

        date.setMonth(date.getMonth() - 1);
        $("#bayesian_startdate").datetimepicker({
            controlType: 'select',
            oneLine: true,
            altField: "#bayesian_starttime"
        })
                .datepicker("setDate", date);
    });
}

function bayesianButton()
{
    //Get time, date, barometric pressure, PAR, depth, Dissolved Oxygen(mg/L), water temp
    //Fill missing data / Skip large blocks of missing data?

    /*var start = datepicker.starttime;
     var end = datepicker.endtime;
     var parameters = ids of [air pressure, par, depth, DO, water temp]
     
     var dr = new DataRequest(start, end, parameters);*/
}
