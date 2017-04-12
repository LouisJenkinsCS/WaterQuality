
$.getScript("scripts/datetimepicker.js", function () {});

function fillBayesianContent()
{
    $('#Bayesian').append(
            '<div class="large_text">Select your date range:</div>'
            + '<div id="dateInstructDiv">First</div>'
            + '<input id="bayesian_startdate" type="text"></div>'
            + '<br/><br/>'
            + '<div id="dateInstructDiv">Last</div>'
            + '<input id="bayesian_lastdate" type="text"></div>'
            + '<br/><br/><div>Click the button below to download your selected '
            + 'day\'s data. Data parameters downloaded are:</div></div>'
            + 'Time, Date, Barometric pressure, PAR, Depth, Dissolved oxygen, '
            + 'and Water temperature.<br/><br/>'
            + '<button type = "button" onclick="bayesianButton()">Download</button>'
            );

    $(function () {

        var min_date = new Date("January 1, 2007");
        //We want to set earliest date to 1/1/07
        //August 24th, 2015 is earliest date with recorded data
        
        var max_date = new Date();
        max_date.setDate(max_date.getDate());


        var date = new Date();
//            $( "#delete_endtime" ).timepicker();
//            $( "#delete_starttime" ).timepicker();
        $("#bayesian_startdate").datepicker({
            controlType: 'select',
            oneLine: true,
            minDate: min_date
        })
                .datepicker("setDate", date);
        
        $("#bayesian_lastdate").datepicker({
            controlType: 'select',
            oneLine: true,
            maxDate: max_date
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
