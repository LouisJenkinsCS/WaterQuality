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
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <noscript>
            <meta http-equiv="refresh" content="0; URL=/html/javascriptDisabled.html">
        </noscript>
        <title>Dashboard</title>
    </head>
    <body>
        <img id="backPhoto" src="images/backgroundImage.JPG">
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
                       id="defaultOpen">Graph</a></li>
                <li><a href="javascript:void(0)" class="tablinks" onclick="openTab(event, 'Table'); hide();">
                        Table</a></li>
            </ul>
                <div id="Graph" class="tabcontent">
                    <canvas id="myChart" width=25% height=20%></canvas>
                </div>
                <div id="Table" class="tabcontent" style="height:400px;overflow:auto;">                    
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
                <form id="data_type_form" action="ControlServlet" method = "POST">
                    <div class="" id="select_all_toggle"><input type="checkbox" onclick="toggle(this);" 
                           id="select_all_data" value="select_all_data">Select all</div><br>
                    
                    ${DummyData}
                    <input type="submit" name="Get Data" value="Get Data" />
                    <input type="hidden" name="control" value ="getData">
<!--                    <input type="checkbox" onclick="if(current=='Graph')fullCheck('data1')" class="data" id="data1" value="data1">Data<br>
                    <input type="checkbox" onclick="if(current=='Graph')fullCheck('data2')" class="data" id="data2" value="data2">Data<br>
                    <input type="checkbox" onclick="if(current=='Graph')fullCheck('data3')" class="data" id="data3" value="data3">Data<br>
                    <input type="checkbox" onclick="if(current=='Graph')fullCheck('data4')" class="data" id="data4" value="data4">Data<br>
                    <input type="checkbox" onclick="if(current=='Graph')fullCheck('data5')" class="data" id="data5" value="data5">Data<br>
                    <input type="checkbox" onclick="if(current=='Graph')fullCheck('data6')" class="data" id="data6" value="data6">Data<br>
                    <input type="checkbox" onclick="if(current=='Graph')fullCheck('data7')" class="data" id="data7" value="data7">Data<br>
                    <input type="checkbox" onclick="if(current=='Graph')fullCheck('data8')" class="data" id="data8" value="data8">Data<br>-->
                    <br>
                    <div class="data_type_submit" id="Graph_submit" ><input type="submit" ></div>
                    <div class="data_type_submit" id="Table_submit" ><input type="submit" ></div>
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
                <p>This is where the description of the data will go! This will need to be pulled from a text file. 
                   This is where the description of the data will go! This will need to be pulled from a text file. 
                   This is where the description of the data will go! This will need to be pulled from a text file. 
                   This is where the description of the data will go! This will need to be pulled from a text file. 
                   This is where the description of the data will go! This will need to be pulled from a text file. </p>
            </section>
            
        </section> 
        
        <%
        Pair<String, String> data = DataReceiver.generateGraph();
        out.append(data.getValue0()).append(data.getValue1());
        %>
        
            <script>
                var ctx = document.getElementById('myChart').getContext('2d');
                var myChart = new Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: [timeStr],
                        datasets: [{
                            label: 'Current (~1 Month)',
                            data: [dataStr],
                            backgroundColor: 'transparent', borderColor: 'orange'
                        }]
                    }
                });
            </script>
        
        <script type="text/javascript">
            document.getElementById("defaultOpen").click();
            var current;
            /**
             * The <code>openTab</code> function activates a certain event
             * based on the provided <code>tabName</code> parameter
             * @param {type} evt
             * @param {type} tabName the tab that the user is switching to
             */
            function openTab(evt, tabName) {
                var i, tabcontent, tablinks,submitbutton;
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
                checkedBoxes=0;
                //<code>current</code>holds the current <code>tabName</code>
                //This is done because we need to limit the number of boxes checked
                //for the Graph tab and not the Table tab
                current=tabName;
                submitbutton=document.getElementsByClassName("data_type_submit");
                for(i=0; i<submitbutton.length; i++){
                    submitbutton[i].style.display="none";
                }
                document.getElementById(current+"_submit").style.display = "block";
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
            function hide(){
                var item=document.getElementById("select_all_toggle");
                if(item.className=='hide')
                    item.className='unhide';
                else
                    item.className='hide';
            }
            
            var checkedBoxes=0;
            /**
             * The <code>fullCheck</code> function limits the number of data
             * checkboxes checked at a time to 3 by unchecking <coe>id</code>
             * if <code>checkedBoxes</code> equals 3
             * @param {type} id the current data type the user is trying to check
             */
            function fullCheck(id){
                var item=document.getElementById(id);
                if(item.checked==true){
                    if(checkedBoxes<3)
                        checkedBoxes++;
                    else{
                        item.checked=false;
                    }
                }
                else
                    checkedBoxes--;
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
            function hide(){
                var item=document.getElementById("select_all_toggle");
                if(item.className=='hide')
                    item.className='unhide';
                else
                    item.className='hide';
            }
            
            var checkedBoxes=0;
            /**
             * The <code>fullCheck</code> function limits the number of data
             * checkboxes checked at a time to 3 by unchecking <coe>id</code>
             * if <code>checkedBoxes</code> equals 3
             * @param {type} id the current data type the user is trying to check
             */
            function fullCheck(id){
                var item=document.getElementById(id);
                if(item.checked==true){
                    if(checkedBoxes<3)
                        checkedBoxes++;
                    else{
                        item.checked=false;
                    }
                }
                else
                    checkedBoxes--;
            }
        </script>
    </body>
</html>