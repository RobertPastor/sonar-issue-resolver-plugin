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
	/*
	* wrapper 
	*/
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

				//console.log("start progress worker");
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
	},
	
	// create the three check boxes that are appearing at the bottom of the import or update pageX
	createCheckBoxes : function ( parent ) {
		
		var checkBoxesTable = this.createElement(parent, "table", { });
		var trOne = this.createElement(checkBoxesTable, 'tr');
		
		var tdPreview = this.createElement(trOne, 'td',{});
		
		// Update - form - preview (checkbox, optional)
		var formUpdatePreview = this.createElement(tdPreview, 'div', { className: 'modal-field' });				
		var formUpdatePreviewLabel = this.createElement(formUpdatePreview, 'label', { for: 'issueresolver-update-preview' });
		formUpdatePreviewLabel.appendChild(document.createTextNode('Preview'));
		this.createElement(formUpdatePreview, 'input', { id: 'issueresolver-update-preview', type: 'checkbox', name: 'preview', value: 'true'});
		this.createElement(formUpdatePreview, 'div', { className: 'modal-field-description', textContent: 'If set, issues are not actually resolved, but only matched and checked, no changes are made' });

		var tdAssign = this.createElement(trOne, 'td',{});
		
		// Update - form - skipAssign (checkbox, optional)
		var formUpdateSkipAssign = this.createElement(tdAssign, 'div', { className: 'modal-field' });				
		var formUpdateSkipAssignLabel = this.createElement(formUpdateSkipAssign, 'label', { for: 'issueresolver-update-skipassign' });
		formUpdateSkipAssignLabel.appendChild(document.createTextNode('Skip assignments'));
		this.createElement(formUpdateSkipAssign, 'input', { id: 'issueresolver-update-skipassign', type: 'checkbox', name: 'skipAssign', value: 'true'});
		this.createElement(formUpdateSkipAssign, 'div', { className: 'modal-field-description', textContent: 'If set, issue assignments are skipped' });

		var tdComment = this.createElement(trOne, 'td',{});
		
		// Update - form - skipComments (checkbox, optional)
		var formUpdateSkipComments = this.createElement(tdComment, 'div', { className: 'modal-field' });				
		var formUpdateSkipCommentsLabel = this.createElement(formUpdateSkipComments, 'label', { for: 'issueresolver-update-skipcomments' });
		formUpdateSkipCommentsLabel.appendChild(document.createTextNode('Skip comments'));
		this.createElement(formUpdateSkipComments, 'input', { id: 'issueresolver-update-skipcomments', type: 'checkbox', name: 'skipComments', value: 'true'});
		this.createElement(formUpdateSkipComments, 'div', { className: 'modal-field-description', textContent: 'If set, issue comments are skipped' });

		var tdBug = this.createElement(trOne, 'td',{});

		// internet explorer BUG add a last checked check-box 
		this.createElement(tdBug, 'input', { id: 'issueresolver-update-internet-explorer-workaround', type: 'checkbox', name: 'dontCare', value: 'true'});
		document.getElementById('issueresolver-update-internet-explorer-workaround').checked = true;
		document.getElementById('issueresolver-update-internet-explorer-workaround').style.display = "none";
		
	}
	
});
