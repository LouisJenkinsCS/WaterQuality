<%-- 
    Document   : dashboard
    Created on : Feb 6, 2017, 3:14:50 PM
    Author     : Kevin
--%>

<!DOCTYPE html>
<html>
    <head>
         <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="styles/generalStyles.css" type="text/css">
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
        <section class = "content_container" id = "dashboard_container">
            <header class = "content_title_bar" id="login_header"> 
                <div class = "title" >
                    Dashboard
                </div> 
            </header>
            
            <section class = "content_container" id = "graph_container">
            <header class = "content_title_bar" id="login_header"> 
                <div class = "title" >
                    Graph
                </div> 
            </header>     
            <ul class="tab">
                <li><a href="javascript:void(0)" class="tablinks" onclick="openTab(event, 'Graph')"
                       id="defaultOpen">Graph</a></li>
                <li><a href="javascript:void(0)" class="tablinks" onclick="openTab(event, 'Table')">Table</a></li>
            </ul>
                <div id="Graph" class="tabcontent">
                    <img id="graphPic" src="images/graph.png">
                </div>
                <div id="Table" class="tabcontent">
                    <img id="tablePic" src="images/table.jpg">
                </div>
            </section>
            
            <aside class = "content_container" id = "dashboard_data_container">
            <header class = "content_title_bar" id="login_header"> 
                <div class = "title" >
                    Data Type
                </div> 
            </header> 
                <form>
                    <input type="checkbox" id="data" value="data">Data<br>
                    <input type="checkbox" id="data" value="data">Data<br>
                    <input type="checkbox" id="data" value="data">Data<br>
                    <input type="checkbox" id="data" value="data">Data<br>
                    <input type="checkbox" id="data" value="data">Data<br>
                    <input type="checkbox" id="data" value="data">Data<br>
                    <input type="checkbox" id="data" value="data">Data<br>
                    <input type="checkbox" id="data" value="data">Data<br>
                    <br>
                </form>
            </aside><br> 
            
        </section>   
        <script>
            document.getElementById("defaultOpen").click();
            function openTab(evt, tabName) {
                var i, tabcontent, tablinks;
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
            }
        </script>
    </body>
</html>
