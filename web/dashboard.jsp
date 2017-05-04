<%@page import="java.util.stream.Collectors"%>
<%@page import="java.time.Period"%>
<%@page import="java.time.Instant"%>
<%@page import="async.DataReceiver"%>
<%@page import="java.util.List"%>
<%@page import="org.javatuples.Pair"%>
<%@page import="java.util.ArrayList"%>

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        
        <link rel="stylesheet" href="styles/dashboard.css" type="text/css">
        <link rel="stylesheet" type="text/css" href="styles/popup.css">
        
        <script src="https://cdnjs.cloudflare.com/ajax/libs/marked/0.3.6/marked.js"></script>
        
        <link rel="stylesheet" href="styles/markdown.css" type="text/css">
        <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.1.4/Chart.min.js"></script>
        <script src="https://code.jquery.com/jquery-3.1.1.min.js"></script>
        <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
        <script src="https://code.highcharts.com/highcharts.js"></script>
        <script src="http://code.highcharts.com/modules/exporting.js"></script>
        <script src="http://code.highcharts.com/modules/offline-exporting.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/waypoints/4.0.1/jquery.waypoints.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/waypoints/4.0.1/shortcuts/sticky.min.js"></script>
        
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/simplemde/latest/simplemde.min.css">
        <script src="https://cdn.jsdelivr.net/simplemde/latest/simplemde.min.js"></script>
        <script src="scripts/chart_helpers.js"></script>
        <script src="scripts/protocol.js"></script>
        <script src="scripts/AJAX_functions.js"></script>
        <script src="scripts/dashboard.js"></script>
        
        <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/v/dt/dt-1.10.13/b-1.2.4/b-flash-1.2.4/b-html5-1.2.4/b-print-1.2.4/se-1.2.0/datatables.min.css"/>
        <script type="text/javascript" src="https://cdn.datatables.net/v/dt/dt-1.10.13/b-1.2.4/b-flash-1.2.4/b-html5-1.2.4/b-print-1.2.4/se-1.2.0/datatables.min.js"></script>
        <script type="text/javascript" src="//cdn.datatables.net/plug-ins/1.10.13/sorting/date-euro.js"></script>
        <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
        <link rel="stylesheet" href="styles/datetimepicker.css" type="text/css">
        <script src="scripts/datetimepicker.js"></script>
        
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <noscript>
        <meta http-equiv="refresh" content="0; URL=/html/javascriptDisabled.html">
        </noscript>
        <title>Dashboard</title>
    </head>
    <div id="myNav" class="overlay">
        <button style='float:right; margin-right: 1em; margin-top: 1em;' onclick="$('#myNav').slideUp('slow');" value="X"></button>
  <p class="overlay-content" id="overlayNote"></p>
</div>
    <!--<body id="loader" onload="checkUser(); startingData();">-->
        <body id="loader" onload="blockButtons(); startingData();">
        <img id="backPhoto" src="images/Creek.jpeg">
        <header class="title_bar_container">
            <div id="HeaderText">Water Quality</div>
            <button id="Login_Button" onclick="location.href='loginScreen.jsp'">Login</button>
            <button id="Admin_Button" onclick="location.href='admin.jsp'">Admin</button>
        </header>
            
            <script>
                /**the <code>blockButtons</code> function prevents the use of 
                 * the admin abnd login button so that a user does not click
                 * either button too quickly since that causes and issue
                 */
                function blockButtons(){
                    document.getElementById("Admin_Button").disabled = true;
                    document.getElementById("Login_Button").disabled = true;
                    document.getElementById("Admin_Button").style.display = "none";
                    document.getElementById("Login_Button").style.display = "none";
                }
                /**the <code>checkuser</code> function checks whether or not the
                 * user is logged in as an admin and if they are then the admin 
                 * button is displayed otherwise the login button is displayed
                 */
                function checkuser(){
                    if (${loggedIn}) {
                        //The user is logged in as an admin so it
                        //enables the <code>Admin_button</code> and changes the
                        //display so that the user can see it and click it
                        //also disables and hides the <code>Login_button</code>
                        document.getElementById("Admin_Button").disabled = false;
                        document.getElementById("Login_Button").disabled = true;
                        document.getElementById("Admin_Button").style.display = "inline-block";
                        document.getElementById("Login_Button").style.display = "none";
                    } else {
                        //the user is not logged in as an admin so it
                        //enables the <code>Login_button</code> and changes the
                        //display so that the user can see it and click it
                        //also disables and hides the <code>Admin_button</code>
                        document.getElementById("Admin_Button").disabled = true;
                        document.getElementById("Login_Button").disabled = false;
                        document.getElementById("Admin_Button").style.display = "none";
                        document.getElementById("Login_Button").style.display = "inline-block";
                    }
                }
            </script>
        <!--The <code>dashboard_container</code> sections holds all of other 
            sections on the dashboard page-->
        <section class = "content_container1" id = "dashboard_container">
            <header class = "content_title_bar" id="dashboard_header">
                <div class = "title" >
                    Dashboard
                </div>
            </header>
            
            <!--This section contains the different tabs that can be 
                displayed (table and graph)-->
            <section class = "content_container2" id = "graph_container">
                <!--The table contains the links which opens the selected tab
                    by calling the openTab() function-->
                <ul class="tab">
                    <li><a href="javascript:void(0)" class="tablinks" onclick="openTab(event, 'Graph');"
                           id="GraphTab">Graph</a></li>
                    <li><a href="javascript:void(0)" class="tablinks" onclick="openTab(event, 'Table');" 
                           id="TableTab">Table</a></li>
                </ul>
                <!--The contents of the Graph tab-->
                <div id="Graph" class="tabcontent" style="width:auto; height:auto;">
                </div>
                <!--The contents of the table tab-->
                <div id="Table" class="tabcontent">
                    <h4>Excel and PDF downloads require Adobe Flash</h4>
                    <table id="data_table">
                        <thead><tr><th></th></tr></thead>
                    </table>
                </div>
            </section>

            <!--This contains the forms for selecting date range and parameters
                for each of the tabs in the <code>graph_container</code> section-->
            <aside class = "content_container2" id = "dashboard_data_container">
                <header class = "content_title_bar" id="parameters_header">
                    <div class = "title" >
                        Data Type
                    </div>
                </header>
                <!--The <code>Graph_form</code> is the form for selecting date 
                    range and parameters for the graph tab-->
                <form class="data_type_form" id="Graph_form" action="ControlServlet" method = "POST">
                    <!--Allows the user to select a range of dates for data viewing-->
                    <BR>
                    <div id="dateselectordiv" onclick="dateLimits();">
                        Start Date:
                        <input class="dateselector" id="graph_start_date" type="text">
                        <input class="dateselector" id="graph_start_time" type="text">
                        <!--<input class="dateselector" id="graph_start_date" name="graph_start_date"type="datetime-local" min="" max="">-->
                        <BR>to<BR>
                        End Date:
                        <input class="dateselector" id="graph_end_date" type="text">
                        <input class="dateselector" id="graph_end_time" type="text">
                        <!--<input class="dateselector" id="graph_end_date" name="graph_end_date" type="datetime-local" min="" max="">-->
                    </div>
                    <center>Sensor Data - Last Updated: <span class="last_updated">temp val</span></center><br>
                    <div id="graph_parameters" style="margin-left: 1em//0 auto;">
                        <div id="graph_sensor_parameters">
                            <table id="sensor_formatted_table" style="width: 100%; margin: 0 auto;" class="parameter_table">
                                <thead>
                                    <tr>
                                        <td>
                                            Parameter
                                        </td>
                                        <td>
                                            Unit
                                        </td>
                                        <td>
                                            Most Recent Data
                                        </td>
                                    </tr>
                                </thead>
                                <tbody>
                                    <!-- Gets filled -->
                                </tbody>
                            </table>
                        </div>
                        <br>
                        <div id="graph_manual_parameters">
                            <center>Manual Data</center> <br>
                            <table id="manual_formatted_table" style="width: 100%; margin: 0 auto;" class="parameter_table">
                                <thead>
                                    <tr>
                                        <td>
                                            Parameter
                                        </td>
                                        <td>
                                            Unit
                                        </td>
                                    </tr>
                                </thead>
                                <tbody>
                                    <!-- Gets filled -->
                                </tbody>
                            </table>
                        </div>
                    </div>
                    </center>
                    <br>
                </form>
                <!--The <code>Table_form</code> is the form for selecting date 
                    range and parameters for the table tab-->
                <form class="data_type_form" id="Table_form" action="ControlServlet" method = "POST">
                    <!--Allows the user to select a range of dates for data viewing-->
                    <br>
                    <div id="dateselectordiv" onclick="dateLimits();">
                        Start Date:
                        <input class="dateselector" id="table_start_date" type="text">
                        <input class="dateselector" id="table_start_time" type="text">
                        <BR>to<BR>
                        End Date:
                        <input class="dateselector" id="table_end_date" type="text">
                        <input class="dateselector" id="table_end_time" type="text">
                    </div>
                    <center>Sensor Data - Last Updated: <span class="last_updated">temp val</span></center>
                    <div id="select_all_div" style="padding-left:1em">
                        <input type="checkbox" onclick="toggle('Table_form',this); fetch();" class="select_all_box" value="select_all_data">
                        Select All Data
                    </div><br/>
                    <div id="table_parameters">
                        <div id="table_sensor_parameters">
                            <!--<center>Sensor Data - Last Updated: <span class="last_updated">temp val</span></center>-->
                            <br>
                            <input type="checkbox" onclick="toggle('table_sensor_parameters',this); fetch();"class="select_all_box" value="select_all_data">
                            Select All Sensor Data<br>
                            
                            <table id="table_sensor_formatted_table" style="width: 100%; margin: 0 auto;" class="parameter_table">
                                <thead>
                                    <tr>
                                        <td>
                                            Parameter
                                        </td>
                                        <td>
                                            Unit
                                        </td>
                                        <td>
                                            Most Recent Data
                                        </td>
                                    </tr>
                                </thead>
                                <tbody>
                                    <!-- Gets filled -->
                                </tbody>
                            </table>
                        </div>
                        <BR>
                        <div id="table_manual_parameters">
                            <center>Manual Data</center><br>
                            <input type="checkbox" onclick="toggle('table_manual_parameters',this); fetch();"class="select_all_box" value="select_all_data">
                            Select All Manual Data<br>
                            <table id="table_manual_formatted_table" style="width: 100%; margin: 0 auto;" class="parameter_table">
                                <thead>
                                    <tr>
                                        <td>
                                            Parameter
                                        </td>
                                        <td>
                                            Unit
                                        </td>
                                    </tr>
                                </thead>
                                <tbody>
                                    <!-- Gets filled -->
                                </tbody>
                            </table>
                        </div> 
                    </div>
                    </center>
                    <br>
                </form>
            </aside><br>

            <!--The data description box is defined here. Sample text is shown-->
            <!--to provide an indication of the text-wrapping.-->
            <!--This will need to pull text from a file which Brandon already-->
            <!--typed and had Dr. Rier edit.-->
            <section class = "content_container2" id = "dashboard_data_description">
                <header class = "content_title_bar" id = "description_header">
                    <div class = "title">
                        Description
                    </div>
                </header>
                
                <p id="tmp"> 
                <!--The <code>Graph_description</code> contains the descriptions
                    of selected data for the graph tab while the <code>Table_description</code>
                    contains the descriptions of selected data from the table tab.
                    Whichever tab is open is the description that is displayed
                    while the other description is hidden.-->
                <div style="margin: 8px; border: solid black 2px;">
                    <div class="description" id="Graph_description"></div>
                    <div class="description" id="Table_description"></div>
                </div>
            </section>
            
        </section>
        
        <script>
            // Custom this to set theme, see: http://www.highcharts.com/docs/chart-design-and-style/design-and-style
            Highcharts.theme = {
                chart: {
                    zoomType: 'x',
                    backgroundColor: 'white',
                    plotBackgroundColor: 'white',
                    width: null,
                    style: {
                        fontFamily: 'Ariel, Helvetica, san-serif'
                    }
                },
                title: {
                    style: {
                        fontSize: '16px',
                        fontWeight: 'bold',
                        textTransform: 'uppercase'
                    }
                },
                tooltip: {
                    borderWidth: 0,
                    backgroundColor: 'rgba(219,219,216,0.8)',
                    shadow: false
                },
                legend: {
                    itemStyle: {
                        fontWeight: 'bold',
                        fontSize: '13px'
                    }
                },
                xAxis: {
                    type: 'datetime',
                    dateTimeLabelFormats: {
                        minute: '%b/%e/%Y %H:%M'
                    },
                    gridLineWidth: 1,
                    labels: {
                        style: {
                            fontSize: '12px'
                        }
                    },
                    title: {
                        text: 'Date'
                    }
                },
                yAxis: {
                    minorTickInterval: 'auto',
                    title: {
                        style: {
                            textTransform: 'uppercase'
                        }
                    },
                    labels: {
                        style: {
                            fontSize: '12px'
                        }
                    }
                },
                plotOptions: {
                    candlestick: {
                        lineColor: '#404048'
                    }
                },
                // General
                background2: '#FFF2D7'
            };

            // Apply the theme
            Highcharts.setOptions(Highcharts.theme);

            // Setup chart, the data will be fed from the servlet through JSP (temporary)
            var chart = Highcharts.chart('Graph', {
                exporting: {
                    enabled:true,
                    buttons:{contextButton:{align:"left"}},
                    chartOptions: { // specific options for the exported image
                        plotOptions: {
                            series: {
                                dataLabels: {
                                    enabled: true
                                }
                            }
                        }
                    },
                    fallbackToExportServer: false
                },
                title: {
                    text: 'Fishing Creek Parameter Values',
                    x: -20 //center
                },
                subtitle: {
                    text: 'Source: environet.com',
                    x: -20
                },
                xAxis: {
                    type: 'datetime',
                    dateTimeLabelFormats: {
                            millisecond: '%H:%M:%S.%L',
                            second: '%H:%M:%S',
                            minute: '%H:%M',
                            hour: '%H:%M',
                            day: '%m/%e',
                            week: '%m/%b',
                            month: '%b \'%Y',
                            year: '%Y'
                    },
                    title: {
                        text: 'Date'
                    }
                },
                yAxis: [{
                        title: {
                            text: '',
                            style: {color: '#7cb5ec'}
                        },
                        labels: {style: {color: '#7cb5ec'}},
                        plotLines: [{
                                value: 0,
                                width: 1,
                                color: '#808080'
                            }]
                    }, {// Secondary yAxis
                        title: {
                            text: ''
                        },
                        opposite: true
                    }],
                tooltip: {
                    valueSuffix: '',
                    valueDecimals: 2
                },
                legend: {
                    layout: 'vertical',
                    align: 'right',
                    verticalAlign: 'top',
                    borderWidth: 0,
                    floating: true
                },
                series: []
            });
        </script>
    </body>
