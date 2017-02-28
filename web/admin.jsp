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
        <link rel="stylesheet" href="styles/admin.css" type="text/css">
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <noscript>
        <meta http-equiv="refresh" content="0; URL=/html/javascriptDisabled.html">
        </noscript>
        <title>Admin Page</title>
    </head>
    <body onload="onLoad();">
        <script>
            function onLoad()
            {
                console.log("Page loaded!");
            }
        </script>
        <img id="back_photo" src="images/Creek3.jpeg">
        <header class="banner"> 
            <div id="banner__text">Water Quality</div>
        </header>
        <section class = "content_box">
            <header class = "content_box__banner"> 
                <div class = "content_box__banner__text" >
                    Administrative Functions
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
                           id="RemoveTab">Remove User</a></li>
                    <li><a href="javascript:void(0)" class="tabs" onclick="openTab(event, 'Edit_Description'); hide();"
                           id="EditTab">Edit Desc</a></li>
                </ul>
            </div>
            <!--Admin insertion functionality and GUI are defined here-->
            <admincontent id="Input_Data" class="tab_content">

                <form id="csv_upload_form" action="ControlServlet" method="POST">
                    <div class="large_text">Upload .CSV File</div>
                    <input type="file" value="Browse..."><br/>
                    <input type="submit" value="Submit">
                </form>

<!--                    <form id="manual_data_entry_form" action="ControlServlet" method="POST">
                    <h2>Enter Data Manually:</h2>
                    Date: <input type="date" name="data_date"><br/>
                    Time: <input type="time" name="data_time"><br/>
                    Parameter: <select id="select_param" width:20px></select><br/>
                    Value: <input type="text" name="value"><br/>
                    <input type="submit" value="Submit">
                </form>

                <button type="button" onclick="createNewInput()">+</button>
                <div id="new_input_space"></div>-->

                <div id="data_entry"></div>
                <script src="data_entry.js"></script>

            </admincontent>

            <!--Admin deletion functionality and GUI are defined here-->
            <admincontent id="Delete_Data" class="tab_content">
                <form action="ControlServlet" method="POST">
                    <div class="large_text">Time Frame:</div>
                    <div id="dateInstructDiv">Start Date to End Date (Format: yyyy-mm-dd)</div>
                    <div id="dateselectordiv" onclick="dateLimits();">
                        <input class="dateselector" id="startdate" type="date" min="2016-01-01" max="" value="2017-02-18"> to
                        <input class="dateselector" id="enddate" type="date" min="2016-01-01" max="" value=""></div>
                    <div class="large_text">Parameter to delete:</div>
                    <select>
                        <!--populate with parameters-->
                        <option>Air Temp</option>
                        <option>Water Temp</option>
                    </select>
                    <div class="large_text">Please select the data entry from below:</div>
                    <table border="2">
                        <!--populate with data within specifications-->
                        <tr><td>paramname</td><td>value</td></tr>
                        <tr><td>Air Temp</td><td>5</td><td><input type="checkbox"></td></tr>
                        <tr><td>Water Temp</td><td>6</td><td><input type="checkbox"></td></tr>
                    </table><br/>
                    <input type="submit" value="Delete">
                </form>
            </admincontent>


            <admincontent id="Register_User" class="tab_content">
                <ul id="friends">
                </ul>

                <p>Name: <input type="text" id = "name"></p>
                <button id="add-friend">Add a friend</button>

                <script>
                    //$friends has a dollar sign to denote it holds a jQuery object
                    var $friends = $('#friends'); //selector puts the ul with id=friends into variable $friends
                    var $name = $('#name'); //selector puts the object with id='name' into $name

                    $.ajax({
                        type: 'GET', //Gets a collection objects and passes them to $friends
                        url: 'http://rest.learncode.academy/api/Brandons/friends',
                        success: function (friends) {
                            $.each(friends, function (friend) {
                                addFriend(friend);
                            });
                        }
                    });

                    $('#add-friend').on('click', function () {

                        var friend = {
                            name: $name.val(),
                        };

                        $.ajax({
                            type: 'POST',
                            url: 'http://rest.learncode.academy/api/Brandons/friends',
                            data: friend,
                            success: function (newFriend) {
                                addFriend(newFriend);
                            },
                            error: function () {
                                alert('error making friend');
                            }
                        });

                    });

                    $friends.delegate('.remove', 'click', function () {
                        var $li = $(this).closest('li');

                        $.ajax({
                            type: 'DELETE',
                            url: 'http://rest.learncode.academy/api/Brandons/friends/' +
                                    $(this).attr('data-id'),
                            success: function () {
                                $li.fadeOut(600, function () {
                                    $li.remove();
                                    console.log('Friend deleted successfully :(');
                                });
                            },
                            failure: function () {
                                console.log('Error, id: ' + $li.data("id"));
                            }
                        });
                    });

                    function addFriend(friend)
                    {
                        $friends.append('<li><p>Friend: ' + friend.name + '</p>'
                                + '<button data-id="' + friend.id + '" class = "remove">X</button></li>');
                    }



                </script>
            </admincontent>


            <admincontent id="Remove_User" class="tab_content">
                <div>
                    
                </div>
            </admincontent>


            <admincontent id="Edit_Description" class="tab_content">
                <div></div>
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
             * Makes it so the date input fields can not be chosen for furture
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


        </script>
    </body>
</html>
