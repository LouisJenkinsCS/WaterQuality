window.onload = function (){
    var ctx = document.getElementById("myChartA").getContext("2d");
    var data = {
    labels : labels,
    datasets : [
        {
            data : A.chain1,
            label: "Chain1",
            pointBackgroundColor: "rgba(75,192,0,1)",
            fill: false
        },
        {
            data : A.chain2,
            label: "Chain2",
            pointBackgroundColor: "rgba(75,192,50,1)",
            fill: false
        },
        {
            data : A.chain3,
            label: "Chain3",
            pointBackgroundColor: "rgba(75,192,100,1)",
            fill: false
        }
    ]};

    var myLineChart = new Chart(ctx, {
    type: 'line',
    data: data
    });


    var ctx = document.getElementById("myChartR").getContext("2d");
    var data = {
    labels : labels,
    datasets : [
        {
            data : R.chain1,
            label: "Chain1",
            pointBackgroundColor: "rgba(75,192,100,1)",
            fill: false
        },
        {
            data : R.chain2,
            label: "Chain2",
            pointBackgroundColor: "rgba(75,192,150,1)",
            fill: false
        },
        {
            data : R.chain3,
            label: "Chain3",
            pointBackgroundColor: "rgba(75,192,200,1)",
            fill: false
        }
    ]};

    var myLineChart = new Chart(ctx, {
    type: 'line',
    data: data
    });


    var ctx = document.getElementById("myChartp").getContext("2d");
    var data = {
    labels : labels,
    datasets : [
        {
            data : p.chain1,
            label: "Chain1",
            pointBackgroundColor: "rgba(75,192,200,1)",
            fill: false
        },
        {
            data : p.chain2,
            label: "Chain2",
            pointBackgroundColor: "rgba(75,192,250,1)",
            fill: false
        },
        {
            data : p.chain3,
            label: "Chain3",
            pointBackgroundColor: "rgba(75,192,300,1)",
            fill: false
        }
    ]};

    var myLineChart = new Chart(ctx, {
    type: 'line',
    data: data
    });


    var ctx = document.getElementById("myChartK_day").getContext("2d");
    var data = {
    labels : labels,
    datasets : [
        {
            data : K_day.chain1,
            label: "Chain1",
            pointBackgroundColor: "rgba(75,192,300,1)",
            fill: false
        },
        {
            data : K_day.chain2,
            label: "Chain2",
            pointBackgroundColor: "rgba(75,192,350,1)",
            fill: false
        },
        {
            data : K_day.chain3,
            label: "Chain3",
            pointBackgroundColor: "rgba(75,192,400,1)",
            fill: false
        }
    ]};

    var myLineChart = new Chart(ctx, {
    type: 'line',
    data: data
    });


    var ctx = document.getElementById("myCharttheta").getContext("2d");
    var data = {
    labels : labels,
    datasets : [
        {
            data : theta.chain1,
            label: "Chain1",
            pointBackgroundColor: "rgba(75,192,400,1)",
            fill: false
        },
        {
            data : theta.chain2,
            label: "Chain2",
            pointBackgroundColor: "rgba(75,192,450,1)",
            fill: false
        },
        {
            data : theta.chain3,
            label: "Chain3",
            pointBackgroundColor: "rgba(75,192,500,1)",
            fill: false
        }
    ]};

    var myLineChart = new Chart(ctx, {
    type: 'line',
    data: data
    });


    var ctx = document.getElementById("myChartPAR").getContext("2d");
    var data = {
    labels : obs,
    datasets : [
        {
            data : PAR,
            label: "PAR",
            pointBackgroundColor: "rgba(200,75,500,1)",
            fill: false
        }
    ]};

    var myLineChart = new Chart(ctx, {
    type: 'line',
    data: data
    });


    var ctx = document.getElementById("myCharttempC").getContext("2d");
    var data = {
    labels : obs,
    datasets : [
        {
            data : tempC,
            label: "tempC",
            pointBackgroundColor: "rgba(200,75,500,1)",
            fill: false
        }
    ]};

    var myLineChart = new Chart(ctx, {
    type: 'line',
    data: data
    });


    var ctx = document.getElementById("myChartDO_modelled").getContext("2d");
    var data = {
    labels : obs,
    datasets : [
        {
            data : DO_lower,
            label: " Lower fence ",
            pointBackgroundColor: "rgba(255, 0, 0,1)",
            fill: false
        },
        {
            data : DO_upper,
            label: " Upper fence ",
            pointBackgroundColor: "rgba(0,255,0,1)",
            fill: false
        },
        {
            data : DO_modeled_mean,
            label: " Average from Model ",
            pointBackgroundColor: "rgba(100,75,500,1)",
            fill: false
        }
    ]};

    var myLineChart = new Chart(ctx, {
    type: 'line',
    data: data
    });


};