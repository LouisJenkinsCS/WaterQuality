//insertionid changes with each new field created
//for unique entries
var $insertionid = 0;

//Possible variable to hold the response from a GET request?
var $parameterlist;

//Sample of successful data format for input into the database - tested and approved
var sampledata = {action: 'getManualItems', dataName: 'Temperature',
    units: 'C', time: '2007-12-03T10:15:30',
    value: '13.0', delta: '2.0', id: '126',
    inputStatus: ''};

$.getScript("scripts/AJAX_magic.js", function(){

});

function doTheThing()
{
    var options = "";
    //This will hold the array of names? Maybe?
    get("AdminServlet", sampledata, function(response)
    {
        console.log("Connection made!" + response);
        var thing = response; //JSON.parse(response);
        for(var i = 0; i < thing.length; i++)
        {
            options += '<option>';
            var subthing = thing[i];
            options += subthing["name"];
            options += '</option>';
        }
        
        console.log("Thing: " + thing);
        console.log("Subthing: " + subthing);
        
        
    });

//This creates the browse area, then fires off the function createNewInput,
//then puts a button below for adding more data entry areas
    $('#Input_Data').append(
            '<div class="large_text">Upload .CSV File</div>' +
            '<input type="file" value="Browse..."><br/>' +
            '<input type="submit" value="Submit">' +
            '<a id="input_space"></a>');
    createNewInput(options);
    $('#Input_Data').append(
            '<button type="button" onclick="createNewInput()">+</button>');
};

function createNewInput(options)
{
    $('#input_space').append(
            '<span data-insertion_id = ' + $insertionid++ +
            ' class = datainsertion>' +
            '<h3>Enter Data Manually:</h3>' +
            'Date: <input type="date" name="data_date"><br/>' +
            'Time: <input type="time" name="data_time"><br/>' +
            'Parameter: <select id="select_param">' +
            options +
            '</select><br/>' +
            'Value: <input type="text" name="value"><br/>' +
            // The following line is to show the unique value
            // of each set of data to be inserted...doesn't need to be displayed
            // on launch, only for testing
            /*'Insertion ID: ' + $insertionid + '*/'<br/></span>'
            );
    
};
