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
        <script src="scripts/chart_helpers.js"></script>
        <script src="scripts/protocol.js"></script>
        <script src="scripts/AJAX_magic.js"></script>
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
                    <li><a href="javascript:void(0)" class="tablinks" onclick="openTab(event, 'Graph'); hide();"
                           id="GraphTab">Graph</a></li>
                    <!--The table tab is used as the test event to pass information via a generic AJAX function
                        in this case passing a POST request to ControlServlet. Upon success, the callback function
                        is called, posting a message to the server log.-->
                    <li><a href="javascript:void(0)" class="tablinks" onclick="openTab(event, 'Table'); hide();
                            post_get('POST', 'ControlServlet', {control: 'test', value: 'Hello, world'}, function () {
                                console.log('SUCCESS');
                            });"
                           id="TableTab">Table</a></li>
                    <li>
                        <form><input id="exportbutton" type="submit" value="Export" onclick="exportData('exportbutton')"></form>
                    </li>
                </ul>
                <div id="Graph" class="tabcontent"></div>
                <div id="Table" class="tabcontent" style="height:400px;overflow:auto;">
                    ${Table}
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
                    <div class="data_type_submit" id="Graph_submit"><input type="submit" value="Graph"></div>
                    <input type="hidden" name="control" value ="getData">

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
                    <div id="select_all_toggle"><input type="checkbox" onclick="toggle(this);" 
                                                       id="select_all_data" value="select_all_data">Select all</div><br>
                        ${Parameters}
                    <br>
                    <div class="data_type_submit" id="Table_submit">
                        <input type="submit" value="Table" onclick="">
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
                <p>${Descriptions}</p>
            </section>



        </section> 

        <script>
            function pad(num, size) {
                var s = num + "";
                while (s.length < size)
                    s = "0" + s;
                return s;
            }

            function setDate(date, id) {
                var dateStr = date.getFullYear() + "-" + pad(date.getMonth() + 1, 2) + "-" + pad(date.getDate(), 2) + "T" + pad(date.getHours() + 1, 2) + ":" + pad(date.getMinutes() + 1, 2) + ":" + pad(0, 2);
                document.getElementById(id).value = dateStr;
                console.log("id: " + id + ", date: " + date, ", datestr: " + dateStr);
            }
            var end = new Date();
            end.setSeconds(0);
            var start = new Date();
            start.setSeconds(0);
            start.setMonth(start.getMonth() - 1);
            setDate(end, "enddate");
            setDate(start, "startdate");
            setDate(end, "enddate2");
            setDate(start, "startdate2");

            /**
             * Makes it so the date input fields can not be chosen for furture
             * dates. Also sets makes sure the <code>enddate</code> can not be a
             * date that is earlier than <code>startdate</code>
             */
            function dateLimits() {
                /*var today = new Date();
                 var dd = today.getDate();
                 var mm = today.getMonth()+1; //January is 0!
                 var yyyy = today.getFullYear();
                 if(dd<10){
                 dd='0'+dd;
                 } 
                 if(mm<10){
                 mm='0'+mm;
                 } 
                 today = yyyy+'-'+mm+'-'+dd; */
                var date = end;
                var dateStr = date.getFullYear() + "-" + pad(date.getMonth() + 1, 2) + "-" + pad(date.getDate(), 2) + "T" + pad(date.getHours() + 1, 2) + ":" + pad(date.getMinutes() + 1, 2) + ":" + pad(0, 2);
                document.getElementById("enddate").setAttribute("max",dateStr);
                document.getElementById("startdate").setAttribute("max",document.getElementById("enddate").value);
                document.getElementById("enddate").setAttribute("min",document.getElementById("startdate").value); 
            }
            </script>
<!--            <script>var d = new Date(); d.setMonth(d.getMonth() - 1); document.getElementById('startdate').valueAsDate = new Date(d.getFullYear(), d.getMonth(), d.getDate(), 12).toGMTString();</script>-->

        <script>
            function post(path, params, method) {
                method = method || "post"; // Set method to post by default if not specified.

                // The rest of this code assumes you are not using a library.
                // It can be made less wordy if you use one.
                var form = document.createElement("form");
                form.setAttribute("method", method);
                form.setAttribute("action", path);

                for (var key in params) {
                    if (params.hasOwnProperty(key)) {
                        var hiddenField = document.createElement("input");
                        hiddenField.setAttribute("type", "hidden");
                        hiddenField.setAttribute("name", key);
                        hiddenField.setAttribute("value", params[key]);

                        form.appendChild(hiddenField);
                    }
                }

                document.body.appendChild(form);
                form.submit();
            }

            function handleClick(cb)
            {
                if (current == 'Graph') {
                    fullCheck(cb.id);
                }
//                post("ControlServlet", {key: 'control', control: 'getDesc'});
            }

            function graphSubmit() {
                var checkboxes = document.querySelectorAll('input[type="checkbox"]');
                var data = "{ data: [";
                for (var i = 0; i < checkboxes.length; i++) {
                    if (checkboxes[i].checked == true) {
                        data += checkbox[i].id.toString();
                    }
                }
                data += "] }";

                post("ControlServlet", {key: 'control', control: 'getData ' + data});
            }

            function exportData(id) {
                document.write(id);
            }
        </script>
        <script>
            // This is new: Once we get data via AJAX, it's as easy as plugging it into DataResponse.
            var data = new DataResponse(${ChartData});
            var timeStamps = getTimeStamps(data);
            var timeStampStr = [];

            // Convert timestamps to string; HighCharts already defines a nice formatting one.
            for (i = 0; i < timeStamps.length; i++) {
                timeStampStr.push(Highcharts.dateFormat("%m/%d/%Y %H:%M %p", timeStamps[i], true));
            }

            var values = getDataValues(data);
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
                title: {
                    text: 'Water Creek Parameter Values',
                    x: -20 //center
                },
                subtitle: {
                    text: 'Source: environet.com',
                    x: -20
                },
                xAxis: {
                    categories: timeStampStr
                },
                yAxis: [{
                    title: {
                        text: '',
                        style:{color:'#7cb5ec'}
                    },
                    labels:{style:{color:'#7cb5ec'}},
                    plotLines: [{
                        value: 0,
                        width: 1,
                        color: '#808080'
                    }],
                },{ // Secondary yAxis
                    title: {
                        text: ''
                    },
                    opposite:true
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
                yAxis:i,
                name: data.data[i]["name"],
                data: values[i]
            }, false);
            chart.yAxis[i].setTitle({ text: data.data[i]["name"] });
         }
         
         // Limit the X-Axis to display only 5 at a time. Easier to read.
         //chart.xAxis[0].update({tickInterval: chart.xAxis[0].categories.length / 5});
         </script>
         
        <script type="text/javascript">
            //document.getElementById("GraphTab").click();
            if (getCookie("id") == "Table")
                document.getElementById("TableTab").click();
            else
                document.getElementById("GraphTab").click();

            var current;
            /**
             * The <code>openTab</code> function activates a certain event
             * based on the provided <code>tabName</code> parameter
             * @param {type} evt
             * @param {type} tabName the tab that the user is switching to
             */
            function openTab(evt, tabName) {
                var i, tabcontent, tablinks, submitbutton, form;
                tabcontent = document.getElementsByClassName("tabcontent");


                for (i = 0; i < tabcontent.length; i++) {
                    tabcontent[i].style.display = "none";
                }

                tablinks = document.getElementsByClassName("tablinks");
                for (i = 0; i < tablinks.length; i++) {
                    tablinks[i].className = tablinks[i].className.replace(" active", "");
                }
                document.getElementById(tabName).style.display = "block";
                evt.currentTarget.className += " active";

                //unchecks all of the checkboxes
                toggle(this);
                checkedBoxes = 0;
                //<code>current</code>holds the current <code>tabName</code>
                //This is done because we need to limit the number of boxes checked
                //for the Graph tab and not the Table tab
                current=tabName;
                
                form=document.getElementsByClassName("data_type_form");
                for(i=0; i<form.length; i++){
                    form[i].style.display="none";
                }
                document.getElementById(current + "_form").style.display = "block";
                setCookie("id", current, 1);
            }
            function setCookie(name, value, exdays) {
                var d = new Date();
                d.setTime(d.getTime() + (exdays*24*60*60*1000));
                var expires = "expires="+ d.toUTCString();
                document.cookie = name + "=" + value + ";" + expires + ";path=/";
            }

            function getCookie(cname) {
                var name = cname + "=";
                var decodedCookie = decodeURIComponent(document.cookie);
                var ca = decodedCookie.split(';');
                for (var i = 0; i < ca.length; i++) {
                    var c = ca[i];
                    while (c.charAt(0) == ' ') {
                        c = c.substring(1);
                    }
                    if (c.indexOf(name) == 0) {
                        return c.substring(name.length, c.length);
                    }
                }
                return "";
            }

            /**
             * The <code>toggle</code> function checks or unchecks
             * all of the checkboxes in the given <code>source</code> 
             * @param {type} source
             */
            function toggle(source) {
                var checkboxes = document.querySelectorAll('input[type="checkbox"]');
                for (var i = 0; i < checkboxes.length; i++) {
                    if (checkboxes[i] != source)
                        checkboxes[i].checked = source.checked;
                }
            }

            /**
             * The <code>hide</code> function hides the
             * <code>select_all_toggle</code> checkbox when the Graph tab
             * is selected and reveals the checkbox when the table
             * tab is selected
             */
            function hide() {
                var item = document.getElementById("select_all_toggle");
                if (current == 'Table')
                    item.className = 'unhide';
                else
                    item.className = 'hide';
            }

            var checkedBoxes = 0;
            /**
             * The <code>fullCheck</code> function limits the number of data
             * checkboxes checked at a time to 3 by unchecking <coe>id</code>
             * if <code>checkedBoxes</code> equals 3
             * @param {type} id the current data type the user is trying to check
             */
            function fullCheck(id) {
                var item = document.getElementById(id);
                if (item.checked == true) {
                    if (checkedBoxes < 2)
                        checkedBoxes++;
                    else {
                        item.checked = false;
                    }
                } else
                    checkedBoxes--;
            }
        </script>
    </body>
