<!DOCTYPE html>
<html><head>
<meta http-equiv="content-type" content="text/html; charset=windows-1252">
        <title>Admin</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="styles/generalStyles.css" type="text/css">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <noscript>
            <meta http-equiv="refresh" content="0; URL=/html/javascriptDisabled.html">
        </noscript>
    </head>
    <body>
        <header class="title_bar_container"> 
            <div id="HeaderText">Water Quality Admin Page</div>
        </header>
        <img id="backPhoto" src="images/backgroundImage.JPG">
        
        
        <section class="content_container" id="admin_container">

            <header class="content_title_bar" id="admin_header"> 
                <div class="title">
                    Insert Additional Data
                </div> 
            </header>

            <form id="admin_form" action="admin2.jsp" method="POST">              
                <div>
                    <img id="huskyIcon" src="images/husky.png"><br><br><br><br>
                    <div id="image_message_container">
                        
                    </div>
                </div>
                <div id="admin_request_container">Upload CSV file: <input id="button_browse" name="get_file" value="Browse" type="button"> <br>
                    &lt; no file has been selected&gt;<br><br>
                    Enter data manually:<br>
                    <input class="date_box" name="date_start" placeholder="02-06-2017" type="text">
                    &nbsp;to&nbsp;
                    <input class="date_box" name="date_end" placeholder="02-07-2017" type="text">
                    <br>
parameter: <input value="param1" type="text">   value: <input value="0.00" type="text"><br>
                    <input id="add_row" name="add_row" value="+" type="button">
                </div>
                <br>
                
            <input name="admin" value="Submit" class="submit_button" type="submit"></form>
                    
        </section>      
    


</body></html>