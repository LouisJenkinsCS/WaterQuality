<%@page import="java.util.stream.Collectors"%>
<%@page import="java.time.Period"%>
<%@page import="java.time.Instant"%>
<%@page import="async.DataReceiver"%>
<%@page import="java.util.List"%>
<%@page import="org.javatuples.Pair"%>
<%@page import="java.util.ArrayList"%>
<%--
Current bugs:
    -Can double highlight tabs when clicking dragging
--%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="styles/admin.css" type="text/css">
        <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
        <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
        <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
        <!--<script src="https://code.jquery.com/jquery-1.12.4.js"></script>-->
        <script src="https://cdn.datatables.net/1.10.14/js/jquery.dataTables.min.js"></script>
        <link rel="stylesheet" href="styles/datetimepicker.css" type="text/css">
        <script src="scripts/datetimepicker.js"></script>
        <script src="scripts/admin_insertion.js"></script>
        <script src="scripts/admin_register.js"></script>
        <script src="scripts/admin_removeuser.js"></script>
        <script src="scripts/admin_editdesc.js"></script>
        <script src="scripts/admin_deletion.js"></script>
        <script src="scripts/admin_errors.js"></script>
        <script src="scripts/admin_bayesian.js"></script>
        <script src="scripts/AJAX_functions.js"></script>
        <script src="scripts/protocol.js"></script>
        <script src="scripts/general.js"></script>

        <meta http-equiv="X-UA-Compatible" content="IE=edge">

        <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/v/dt/dt-1.10.13/b-1.2.4/b-colvis-1.2.4/b-html5-1.2.4/b-print-1.2.4/r-2.1.1/se-1.2.0/datatables.min.css"/>

        <script type="text/javascript" src="https://cdn.datatables.net/v/dt/dt-1.10.13/b-1.2.4/b-colvis-1.2.4/b-html5-1.2.4/b-print-1.2.4/r-2.1.1/se-1.2.0/datatables.min.js"></script>
        <noscript>
        <meta http-equiv="refresh" content="0; URL=/html/javascriptDisabled.html">
        </noscript>
        <title>Admin Page</title>
    </head>
    <body onload="onLoad();">

        <script>
            function onLoad()
            {
                document.getElementById("button_logout").disabled = true;
                document.getElementById("button_to_dashboard").disabled = true;
                sleep(900);
                document.getElementById("button_logout").disabled = false;
                document.getElementById("button_to_dashboard").disabled = false;
                console.log("Page loaded!");
            }
        </script>
        <img id="back_photo" src="images/Creek.jpeg">
        <header class="banner"> 
            <div id="banner__text">Water Quality</div>
            <a href="/WaterQuality/">
                <button class="banner_buttons" id="button_logout" onclick="requestLogout()">Logout</button>
            </a>
            <a href="/WaterQuality/">
                <button class="banner_buttons" id="button_to_dashboard">Dashboard</button>
            </a>
        </header>
        <section class = "content_box">
            <header class = "content_box__banner">
                <span id="content_box__banner__text">
                    Administrative Functions
                </span>
                </div>
            </header>
            <div class="content_box__tab_bar"><!--this div is needed to make the ul into a bar-->
                <ul class="tabs">
                    <!--href="javascript:void(0) allows the associated
                    javascript to run without redirecting to another page-->
                    <li><a href="javascript:void(0)" class="tabs" onclick="openTab(event, 'Input_Data'); hide();"
                           id="InputTab">Input Data</a></li>
                    <li><a href="javascript:void(0)" class="tabs" onclick="openTab(event, 'Delete_Data'); hide();"
                           id="DeleteTab">Delete Data</a></li>
                    <li><a href="javascript:void(0)" class="tabs" onclick="openTab(event, 'Register_User'); hide();"
                           id="RegisterTab">Register User</a></li>
                    <li><a href="javascript:void(0)" class="tabs" onclick="openTab(event, 'Remove_User'); hide();"
                           id="RemoveTab">Manage Users</a></li>
                    <li><a href="javascript:void(0)" class="tabs" onclick="openTab(event, 'Edit_Params'); hide();"
                           id="EditTab">Edit Parameters</a></li>
                    <li><a href="javascript:void(0)" class="tabs" onclick="openTab(event, 'Errors'); hide();"
                           id="ErrorsTab">Errors</a></li>
                    <li><a href="javascript:void(0)" class="tabs" onclick="openTab(event, 'Bayesian'); hide();"
                           id="BayesianTab">Bayesian</a></li>
                </ul>
            </div>
            <!--Admin insertion-->
            <admincontent id="Input_Data" class="tab_content">
                <script>loadInsert();
                </script>
                <!--Input Tab defined in admin_insertion.js-->
            </admincontent>


            <!--Admin deletion functionality and GUI are defined here-->
            <admincontent id="Delete_Data" class="tab_content">
                <script>loadDelete();</script>
            </admincontent>


            <admincontent id="Register_User" class="tab_content">
                <!--Register User Tab defined in admin_insertion.js-->
                <script>fillPageRegisterUser();</script>
                <%--option 2 would have been
                    <%@include file="jsp/admin_register.jsp"%>
                    however, this causes issues with admin_register.jsp 
                    accessing directories outside of /jsp.
                --%>
            </admincontent>


            <admincontent id="Remove_User" class="tab_content">
                <!--Remove User Tab defined in admin_removeuser.js-->
                <script>fillPageRemoveUser();</script>
            </admincontent>


            <admincontent id="Edit_Params" class="tab_content">
                <!--Edit Params Tab defined in admin_editdesc.js-->
                <script>fillPageEditParams();</script>
            </admincontent>

            <admincontent id="Errors" class="tab_content">
                <!--Errors Tab defined in admin_errors.js-->
                <script>fillPageErrors();</script>
            </admincontent>

            <admincontent id="Bayesian" class="tab_content">
                <!--Bayesian Tab defined in admin_bayesian.js-->
                <script>fillBayesianContent();</script>
            </admincontent>

        </section>

        <!--            <aside class = "content_container2" id = "dashboard_data_container">
                    <header class = "content_title_bar" id="login_header"> 
                        <div class = "title" >
                            Data Type
                        </div>
                    </header> 
        <%--The <code>data_type_form</code> allows the user to select
            the desired data to be outputed into either a table or
            a graph
        --%>
        <form id="data_type_form" action="ControlServlet" method = "POST">
            <div id="dateInstructDiv">Start Date to End Date</div>
            <div id="dateselectordiv"><input class="dateselector" type="date" name="startdate"> to
            <input class="dateselector" type="date" name="enddate"></div>
            <div class="" id="select_all_toggle"><input type="checkbox" onclick="toggle(this);" 
                   id="select_all_data" value="select_all_data">Select all</div><br>
        ${Parameters}
        <input type="submit" name="Get Data" value="Get Data" />
        <input type="hidden" name="control" value ="getData">
        <br>
        
        <div class="data_type_submit" id="Graph_submit"><input type="submit" value="Graph" onclick="graphSubmit()"></div>
        <div class="data_type_submit" id="Table_submit"><input type="submit" value="Table"></div>
        
    </form>
        
        <form id="submit_query" action="ControlServlet" value="Submit Query">
            <input type="hidden" name="control" value="submitQuery">
             <div class="data_type_submit" id="Graph_submit" onclick="graphSubmit()"><input type="submit" ></div>
            <div class="data_type_submit" id="Table_submit" ><input type="submit" ></div>
        </form>
</aside><br>-->

        <!--The data description box is defined here. Sample text is shown-->
        <!--to provide an indication of the text-wrapping.-->
        <!--This will need to pull text from a file which Brandon already-->
        <!--typed and had Dr. Rier edit.-->
        <!--            <section class = "content_container2" id = "dashboard_data_description">
                        <header class = "content_title_bar" id = "login_header">
                            <div class = "title">
                                Description
                            </div>
                        </header>
                        
                        <p id="tmp"> </p>
                        datadesc is supposed to act the same as DummyData, it's the placeholder for the information from ControlServlet
                        <p>${Descriptions}</p>
                    </section>-->



    </section> 

    <script>
        var ADMIN_SERVLET = "AdminServlet";
    </script>

    <script>
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
    ${ChartJS}
    <script type="text/javascript">
        document.getElementById("InputTab").click();
        var current;
        /**
         * The <code>openTab</code> function activates a certain event
         * based on the provided <code>tabName</code> parameter
         * @param {type} evt
         * @param {type} tabName the tab that the user is switching to
         */
        function openTab(evt, tabName) {
            var i, tabcontent, tablinks, submitbutton;
            tabcontent = document.getElementsByClassName("tab_content");
            for (i = 0; i < tabcontent.length; i++) {
                tabcontent[i].style.display = "none";
            }
            tablinks = document.getElementsByClassName("tabs");
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
            current = tabName;
//                submitbutton = document.getElementsByClassName("data_type_submit");
//                for (i = 0; i < submitbutton.length; i++) {
//                    submitbutton[i].style.display = "none";
//                }
//                document.getElementById(current + "_submit").style.display = "block";
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
//                var item = document.getElementById("select_all_toggle");
//                if (current == 'Table')
//                    item.className = 'unhide';
//                else
//                    item.className = 'hide';
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
        /**
         * Makes it so the date input fields can not be chosen for future
         * dates. Also sets makes sure the <code>enddate</code> can not be a
         * date that is earlier than <code>startdate</code>
         */
        function dateLimits() {
            var today = new Date();
            var dd = today.getDate();
            var mm = today.getMonth() + 1; //January is 0!
            var yyyy = today.getFullYear();
            if (dd < 10) {
                dd = '0' + dd;
            }
            if (mm < 10) {
                mm = '0' + mm;
            }
            today = yyyy + '-' + mm + '-' + dd;
            document.getElementById("enddate").setAttribute("max", today);
            document.getElementById("startdate").setAttribute("max", today);
            document.getElementById("enddate").setAttribute("min", document.getElementById("startdate").value);
            if (document.getElementById("enddate").value != null)
                document.getElementById("startdate").setAttribute("max", document.getElementById("enddate").value);
        }

        function requestLogout() {
            var reqObj = {"action": "logout"};
            post(ADMIN_SERVLET, reqObj, responseLogout);
        }

        function responseLogout(response) {
            console.log(response.status);
            if (response.status[0].errorCode == 0)
            {
                alert("You have successfully been logged out.");
                console.log("Logout successful.");
                window.location.href = "/WaterQuality/";
            } else {
                var errorMsg = "";
                for (var i = 0; i < response.status.length; ++i) {
                    errorMsg += "Error Code: " + response.status[i].errorCode + " ";
                    errorMsg += response.status[i].errorMsg + "\n";
                }
                alert("Failed to logout\n" + errorMsg);
                console.log("Logout failed.");
            }
        }
    </script>
</body>
</html>
