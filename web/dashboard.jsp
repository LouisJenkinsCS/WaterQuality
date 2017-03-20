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
        <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.1.4/Chart.min.js"></script>
        <script src="https://code.jquery.com/jquery-3.1.1.min.js"></script>
        <script src="https://code.highcharts.com/highcharts.js"></script>
        <script src="http://code.highcharts.com/modules/exporting.js"></script>
        <script src="http://code.highcharts.com/modules/offline-exporting.js"></script>
        <script src="scripts/chart_helpers.js"></script>
        <script src="scripts/protocol.js"></script>
        <script src="scripts/AJAX_magic.js"></script>
        <script src="scripts/dashboard.js"></script>
        <link rel="stylesheet" type="text/css" href="styles/popup.css">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <noscript>
        <meta http-equiv="refresh" content="0; URL=/html/javascriptDisabled.html">
        </noscript>
        <title>Dashboard</title>
    </head>
    <body onload=>
        <img id="backPhoto" src="images/Creek3.jpeg">
        <header class="title_bar_container">
            <div id="HeaderText">Water Quality</div>
        </header>
        <section class = "content_container1" id = "dashboard_container">
            <header class = "content_title_bar" id="login_header">
                <div class = "title" >
                    Dashboard
                </div>
            </header>
            
            <section class = "content_container2" id = "graph_container">
                <ul class="tab">
                    <li><a href="javascript:void(0)" class="tablinks" onclick="openTab(event, 'Graph');"
                           id="GraphTab">Graph</a></li>
                    <!--The table tab is used as the test event to pass information via a generic AJAX function
                        in this case passing a POST request to ControlServlet. Upon success, the callback function
                        is called, posting a message to the server log.-->
                    <li><a href="javascript:void(0)" class="tablinks" onclick="openTab(event, 'Table');
                            post_get('POST', 'ControlServlet', {control: 'test', value: 'Hello, world'}, function () {
                                console.log('SUCCESS');
                            });"
                           id="TableTab">Table</a></li>
                    <li>
                        <form><input id="exportbutton" type="button" value="Export" 
                                     onclick="if(getCookie('id') == 'Table'){exportTable('dataTable');}
                                     if(getCookie('id') == 'Graph'){exportGraph();}"></form>
                    </li>
                </ul>
                <div id="Graph" class="tabcontent">
                </div>
                <div id="Table" class="tabcontent">
                    <table align="center" id="dataTable" onclick="openPopup()">
                        
                    </table>
                </div>
                <div id="Export" class="tabcontent">
                </div>
            </section>

            <aside class = "content_container2" id = "dashboard_data_container">
                <header class = "content_title_bar" id="login_header">
                    <div class = "title" >
                        Data Type
                    </div>
                </header>
                <%--The <code>data_type_form</code> allows the user to select
                    the desired data to be outputed into either a table or
                    a graph
                --%>
                <form class="data_type_form" id="Graph_form" action="ControlServlet" method = "POST">
                    <!--Allows the user to select a range of dates for data viewing-->
                    </br>
                    <div id="dateselectordiv" onclick="dateLimits();">
                        Start Date:
                        <input class="dateselector" id="startdate" name="startdate"type="datetime-local" min="" max="">
                        </BR>to</BR>
                        End Date:
                        <input class="dateselector" id="enddate" name="enddate" type="datetime-local" min="" max="">
                    </div>
                    ${Parameters}
                    <br>
                    <div class="data_type_submit" id="Graph_submit">
                        <input type="button" value="Graph" onclick="fetch()">
                    </div>

                </form>
                <form class="data_type_form" id="Table_form" action="ControlServlet" method = "POST">
                    <!--Allows the user to select a range of dates for data viewing-->
                    </br>
                    <div id="dateselectordiv" onclick="dateLimits();">
                        Start Date:
                        <input class="dateselector" id="startdate2" name="startdate"type="datetime-local" min="" max="">
                        </BR>to</BR>
                        End Date:
                        <input class="dateselector" id="enddate2" name="enddate" type="datetime-local" min="" max="">
                    </div>
                    <div id="select_all_div">
                        <input type="checkbox" onclick="toggle();"id="select_all_box" value="select_all_data">
                        Select all
                    </div><br>
                        ${Parameters}
                    <br>
                    <div class="data_type_submit" id="Table_submit">
                        <input type="button" value="Table" onclick="fetch()">
                    </div>
                    <input type="hidden" name="control" value ="Table">
                </form>
            </aside><br>

            <!--The data description box is defined here. Sample text is shown-->
            <!--to provide an indication of the text-wrapping.-->
            <!--This will need to pull text from a file which Brandon already-->
            <!--typed and had Dr. Rier edit.-->
            <section class = "content_container2" id = "dashboard_data_description">
                <header class = "content_title_bar" id = "login_header">
                    <div class = "title">
                        Description
                    </div>
                </header>

                <p id="tmp"> </p>
                <!--datadesc is supposed to act the same as DummyData, it's the placeholder for the information from ControlServlet-->
                <div id="description">${Descriptions}</div>
            </section>
            <!--The modal aka the popup for the table-->
            <div id="myModal" class="modal">
                <div class="modal-content">
                    <div class="modal-header">
                        <span class="close">&times;</span>
                        <h2>Table Data</h2>
                    </div>
                    <div class="modal-body">
                        <table id="popup" align="center"></table>
                    </div>
                </div>
            </div>
        </section> 
            
        <script>
            var end = new Date();
            var start = new Date();
            end.setSeconds(0);
            start.setSeconds(0);
            start.setMonth(start.getMonth() - 1);
            setDate(end, "enddate");
            setDate(start, "startdate");
            setDate(end, "enddate2");
            setDate(start, "startdate2");            
        </script>
            
        <script>
            function exportData(id){
                document.write(id);
            }
        </script>
        <script>
            // This is new: Once we get data via AJAX, it's as easy as plugging it into DataResponse.
            var data = new DataResponse(${ChartData});

            var timeStamps = getTimeStamps(data);
            var timeStampStr = [];
            var values = getDataValues(data);
            // Convert timestamps to string; HighCharts already defines a nice formatting one.
            for (i = 0; i < timeStamps.length; i++) {
                timeStampStr.push([new Date(timeStamps[i]), values[0][i]]);
            }

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
                    enabled:false,
                    /*chartOptions: { // specific options for the exported image
                        plotOptions: {
                            series: {
                                dataLabels: {
                                    enabled: true
                                }
                            }
                        }
                    },*/
                    //fallbackToExportServer: false
                },
                title: {
                    text: 'Water Creek Parameter Values',
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
                    valueSuffix: ''
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
            for (var i = 0; i < data.data.length; i++) {
                chart.addSeries({
                    yAxis: i,
                    name: data.data[i]["name"],
                    data: timeStampStr
                }, false);
                chart.yAxis[i].setTitle({text: data.data[i]["name"]});
            }
        </script>
        
        <script type="text/javascript">
            //document.getElementById("GraphTab").click();
            if (getCookie("id") == "Table")
                document.getElementById("TableTab").click();
            else
                document.getElementById("GraphTab").click();
        </script>
    </body>
