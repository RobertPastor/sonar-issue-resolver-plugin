
/**
 * worker used to show a progress bar
 */

var i = 0;

function timedCount() {
    if (i < 100) {
        i = i + 1;
    } else {
        i = 1;
    }
    postMessage(String(i));
    setTimeout("timedCount()", 500);
}

timedCount();