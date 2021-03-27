/* Load the deck and get stats */
function loadDeck() {
    let fd = new FormData(document.getElementById('deckForm'));

    hideAll();
    $.ajax({
        url: '/deck',
        type: 'POST',
        data: fd,
        processData: false,
        cache: false,
        // contentType: 'multipart/form-data',
        contentType: false,

        success: function(data) {
            avgCosts(data);
            manaCurve(data);
            showAll();
        },

        error: function(errorData) {
            $('#myalerts').append(errorData.responseText);
            $('#myalerts').show();
        }
    });
}

// Hide and show deck info
function hideAll() {
    $('#deckinfo').hide();
}
function showAll() {
    $('#deckinfo').show();
}

/** Display Avg costs */
function avgCosts(data) {
    $('#avgCMC').val(data.avgCMC);
    $('#avgCMCNonLands').val(data.avgCMCNonLands);
}

/** Display the mana curve */
function manaCurve(data) {
    // Clear the canvas for reuploads
    $('#manaCurve').replaceWith('<canvas id="manaCurve"></canvas>');

    var ctx = document.getElementById('manaCurve').getContext('2d');
    var chartData = fixMCHoles(data.manaCurve);
    var myChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: chartData.labels,
            datasets: [{
                data: chartData.curve,
                backgroundColor: 'rgba(100, 100, 100, 0.2)',
                borderColor: 'rgba(100, 100, 100, 1)',
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                yAxes: [{
                    ticks: {
                        beginAtZero: true
                    }
                }]
            },
            title: {
                display: true,
                position: 'bottom',
                text: 'Mana Curve'
            },
            legend: { display: false }
        }
    });
}

/** Fill in holes in the mana curve
  * Returns the fixed mana curve and labels
  */
function fixMCHoles(curve) {
    var res    = [];
    var labels = [];
    var cp = 0;

    for (var i in curve) {
        while (curve[i].cost > cp) {
            res.push(0);
            labels.push(cp);
            cp += 1;
        }

        labels.push(cp);
        res.push(curve[i].amount);
        cp += 1;
    }

    return {
        "curve": res,
        "labels": labels
    };
}
