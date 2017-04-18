/*
 * Administrators are given the ability to download a .csv file
 * containing the parameters required to run Dr. Rier's R script.
 * Parameters are downloaded for a given range of days.
 */

/*
 * The HTML is written here to keep admin.jsp looking clean.
 * The oldest date and most recent date, defining the range the user
 * has access to is defined here.
 */
function fillBayesianContent() {
    $('#Bayesian').append(
            '<div class="large_text">Select your date range:</div>'
            + '<div id="dateInstructDiv">First</div>'
            + '<input id="bayesian_startdate" type="text"></div>'
            + '<br/><br/>'
            + '<div id="dateInstructDiv">Last</div>'
            + '<input id="bayesian_lastdate" type="text"></div>'
            + '<br/><br/>'
            + '<div>Click the button below to download your selected '
            + 'day\'s data. Data parameters downloaded are:</div></div>'
            + 'Timestamp, Barometric pressure, PAR, Depth, Dissolved oxygen, '
            + 'and Water temperature.<br/><br/>'
            + '<button type="button" onclick="bayesianButton()">Download</button>'
            );

    $(function () {
        var min_date = new Date("January 1, 2007");
        var max_date = new Date();
        max_date.setDate(max_date.getDate() - 1);

        var date = new Date();

        $("#bayesian_startdate").datepicker({
            controlType: 'select',
            oneLine: true,
            minDate: min_date,
            maxDate: max_date
        })
                .datepicker("setDate", date);

        $("#bayesian_lastdate").datepicker({
            controlType: 'select',
            oneLine: true,
            minDate: min_date,
            maxDate: date
        })
                .datepicker("setDate", date);

    });
}

/*
 * TODO: Documentation
 */
function bayesianButton() {
    //Get time, date, barometric pressure, PAR, depth, Dissolved Oxygen(mg/L), water temp

    var request = {
        action: "getBayesianCSV",
        startDate: new Date($("#bayesian_startdate").val()).getTime(),
        endDate: new Date($("#bayesian_lastdate").val()).getTime()
    };

    post("AdminServlet", request, request => {
        var csvContent = "data:text/csv;charset=utf-8,";
        var encodedUri = encodeURI(csvContent + request);
        var link = document.createElement("a");
        link.setAttribute("href", encodedUri);
        link.setAttribute("download", "bayesian_csv.csv");
        document.body.appendChild(link); // Required for FF

        link.click();
    });
}
