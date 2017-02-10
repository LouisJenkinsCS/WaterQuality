<!DOCTYPE html>
<html>
    <head>
         <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="styles/dashboard.css" type="text/css">
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
            <header class = "content_title_bar" id="login_header"> 
                <div class = "title" >
                    Graph
                </div> 
            </header>     
            <ul class="tab">
                <li><a href="javascript:void(0)" class="tablinks" onclick="openTab(event, 'Graph'); hide();"
                       id="defaultOpen">Graph</a></li>
                <li><a href="javascript:void(0)" class="tablinks" onclick="openTab(event, 'Table'); hide();">
                        Table</a></li>
            </ul>
                <div id="Graph" class="tabcontent">
                    
                </div>
                <div id="Table" class="tabcontent">
                    
                </div>
            </section>
            
            <aside class = "content_container2" id = "dashboard_data_container">
            <header class = "content_title_bar" id="login_header"> 
                <div class = "title" >
                    Data Type
                </div> 
            </header> 
                <form id="data_type_form">
                    <div class="" id="select_all_toggle"><input type="checkbox" onclick="toggle(this);" 
                           id="select_all_data" value="select_all_data">Select all</div><br>
                    <input type="checkbox" class="data" id="data" value="data">Data<br>
                    <input type="checkbox" class="data" id="data" value="data">Data<br>
                    <input type="checkbox" class="data" id="data" value="data">Data<br>
                    <input type="checkbox" class="data" id="data" value="data">Data<br>
                    <input type="checkbox" class="data" id="data" value="data">Data<br>
                    <input type="checkbox" class="data" id="data" value="data">Data<br>
                    <input type="checkbox" class="data" id="data" value="data">Data<br>
                    <input type="checkbox" class="data" id="data" value="data">Data<br>
                    <br>
                    <input type="submit" id="data_type_submit">
                </form>
            </aside><br> 
            
        </section>   
        <script type="text/javascript">
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
            
            function toggle(source) {
                var checkboxes = document.querySelectorAll('input[type="checkbox"]');
                for (var i = 0; i < checkboxes.length; i++) {
                    if (checkboxes[i] != source)
                        checkboxes[i].checked = source.checked;
                }
            }
            
            function hide(){
                var item=document.getElementById("select_all_toggle");
                if(item.className=='hide')
                    item.className='unhide';
                else
                    item.className='hide';
            }
        </script>
    </body>
</html>