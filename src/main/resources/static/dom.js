define({
	
	/*
	* create an element as a child of 'parent'
	* apply properties and return it
	*/
	createElement : function (parent, name, properties) {
		var element = document.createElement(name);
		for(var propertyName in properties){
		    element[propertyName] = properties[propertyName];
		}
		parent.appendChild(element);
		return element;
	},

	removeChildren : function(parent) {
		while (parent.firstChild) {
			parent.removeChild(parent.firstChild);
		}
	},
	
	getElementById : function(id) {
		return document.getElementById(id);
	},
	
	/**
	 * code to manage the worker
	 * worker manages the queries to ping the servers
	 */

	startProgressWorker : function () {

		if (typeof (Worker) !== "undefined") {
			// Yes! Web worker support!
			// Some code.....
			// var progress worker defined in the entry point.js
			if (typeof (progressWorker) == "undefined") {

				console.log("start progress worker");
				// show the progress bar
				
				var URI = window.location.origin + window.baseUrl + "/static/issueresolver/progressWorker.js";

				progressWorker = new Worker(URI);
				progressWorker.onmessage = function (event) {

					// progress bar - see creation of the progress bar in the TAB FACTORY
					var workerProgressBar = document.getElementById('issue-resolver-worker-id');
					if (workerProgressBar != undefined) {
						workerProgressBar.value = event.data;
					}

					var workerProgressValue = document.getElementById('issue-resolver-value-id');
					if (workerProgressValue != undefined) {
						workerProgressValue.innerHTML = event.data;
					}
				};
			}
		} else {
			// Sorry! No Web Worker support..
			console.log("Sorry - no web worker support by this browser");
		}
	},
	
	// called in the Import or Update function
	stopProgressWorker : function () {

		// progress worker variable is defined in the entry point
		if (progressWorker != undefined) {
			progressWorker.terminate();
			progressWorker = undefined;
			//console.log("progress worker is stopped !!!");

			var workerProgressBar = document.getElementById('issue-resolver-worker-id');
			if (workerProgressBar != undefined) {
				workerProgressBar.value = 0;
			}
		}
	}

});
