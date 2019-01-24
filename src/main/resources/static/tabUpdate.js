define(['dom', 'result'], function(dom, result) {
	return {
		create: function(projectKey) {	
			return {
				projectKey: projectKey,
				show: function(parent) {

					dom.removeChildren(parent);

					// Header and description
					//dom.createElement(parent, 'h2', { className: 'issueresolver-header', textContent: 'Update'});
					dom.createElement(parent, 'h2', { className: 'issueresolver-description big-spacer-bottom', textContent: 'Update issues in a current project branch from issue data of a read-only source project branch.'});

					// Update - form
					var formUpdate = dom.createElement(parent, 'form', { id: 'issueresolver-update-form' , 'enctype': 'application/x-www-form-urlencoded' });

					// Update - form - projectKey  -> it is the current project
					var formUpdateCurrentProjectDiv = dom.createElement(formUpdate, 'div', { className: 'modal-field'});

					// first input in the DIV
					dom.createElement(formUpdateCurrentProjectDiv, 'input', { id: 'issueresolver-update-projectKey', name: 'projectKey', value: projectKey , type: 'text'});
					document.getElementById('issueresolver-update-projectKey').readOnly = true;
					document.getElementById('issueresolver-update-projectKey').style.width = "400px";

					// Import - form - target branch in the current project
					// 14 January 2019 - Robert PASTOR

					var formUpdateCurrentProjectLabel = dom.createElement(formUpdateCurrentProjectDiv, 'label', { for: 'issueresolver-update-current-project'});
					formUpdateCurrentProjectLabel.appendChild(document.createTextNode('Target Project'));

					// selector for the target branch in the current project
					// selection of a branch being not necessarily the master branch
					var currentBranchSelectInput = dom.createElement(formUpdateCurrentProjectDiv, 'select', { id: 'issueresolver-update-current-project-select-branch', name: 'select-branch' });
					currentBranchSelectInput.style.width = "auto";

					// read only input with selected branch of Current PROJECT
					var inputTargetProjectBranch = dom.createElement(formUpdateCurrentProjectDiv, 'input', { id: 'issueresolver-update-toProject-master-branch', name: 'toProject-branch' });
					document.getElementById('issueresolver-update-toProject-master-branch').readOnly = true;
					document.getElementById('issueresolver-update-toProject-master-branch').style.display = "none";

					// listen to the FROM PROJECT BRANCH selection changes
					document.getElementById('issueresolver-update-current-project-select-branch').onchange = function() {

						// set the value of hidden input
						document.getElementById('issueresolver-update-toProject-master-branch').value = this.options[this.selectedIndex].value;
					}

					// Update - FROM Source project key
					var formUpdateFromProjectDiv = dom.createElement(formUpdate, 'div', { className: 'modal-field' });	

					var formUpdateFromProjectKeyLabel = dom.createElement(formUpdateFromProjectDiv, 'label', { for: 'issueresolver-update-fromprojectkey' });
					formUpdateFromProjectKeyLabel.appendChild(document.createTextNode('Source Project'));
					dom.createElement(formUpdateFromProjectKeyLabel, 'em', { className:'mandatory', textContent:'*' });

					// project selector => FROM PROJECT it is the origin project
					var formUpdateFromProjectKey = dom.createElement(formUpdateFromProjectDiv, 'select', { id: 'issueresolver-update-fromProjectkey', name: 'fromProjectSelectKey' });
					formUpdateFromProjectKey.style.width = "auto";
					dom.createElement(formUpdateFromProjectKey, 'div', {className:'modal-field-description', textContent: 'The project to import and to find issues from - using its master branch'});

					// hidden input with the content of the selected project
					var updateFromProjectKeyInput = dom.createElement(formUpdateFromProjectDiv, 'input', { id: 'issueresolver-update-fromProjectKey-input', name: 'fromProjectKey' });
					updateFromProjectKeyInput.readOnly = true;
					updateFromProjectKeyInput.style.width = "400px";
					//updateFromProjectKeyInput.style.display = "none";

					// selection of a branch being not necessarily the master branch
					var branchSelectInput = dom.createElement(formUpdateFromProjectDiv, 'select', { id: 'issueresolver-update-select-branch', name: 'select-branch' });
					branchSelectInput.style.width = "auto";

					// input containing the branch of the FROM PROJECT
					var fromProjectBranchInput = dom.createElement(formUpdateFromProjectDiv, 'input', { id: 'issueresolver-update-from-branch', type:'text', name:'fromProject-branch'});
					fromProjectBranchInput.readOnly = true;
					fromProjectBranchInput.style.display = "none";

					dom.createElement(formUpdateFromProjectDiv, 'div', { className:'modal-field-description', textContent: 'The origin branch in the FROM project where to take matching issues'});

					// listen to the branch selection changes
					document.getElementById('issueresolver-update-select-branch').onchange = function() {
						// set the value of hidden input
						document.getElementById('issueresolver-update-from-branch').value = this.options[this.selectedIndex].value;
					};

					// listen to the FROM PROJECT selection changes
					document.getElementById('issueresolver-update-fromProjectkey').onchange = function() {
						// set the value of hidden input

						document.getElementById('issueresolver-update-fromProjectKey-input').value = this.options[this.selectedIndex].value;
						var modifiedProject = this.options[this.selectedIndex].value;

						// Populate branch drop down list - for the FROM project
						window.SonarRequest.getJSON(
								'/api/project_branches/list?project=' + encodeURI(modifiedProject)
						).then(function(response) {

							dom.removeChildren(branchSelectInput);

							var initialModifiedBranchName;
							var initialModifiedBranch = true;

							for (var modifiedBranchIndex = 0; modifiedBranchIndex < response.branches.length; modifiedBranchIndex++) {

								var modifiedBranch = response.branches[modifiedBranchIndex];
								if ( initialModifiedBranch ) {
									initialModifiedBranchName = modifiedBranch.name;
									initialModifiedBranch = false;
								}
								dom.createElement(branchSelectInput, 'option', { value: modifiedBranch.name, textContent: modifiedBranch.name });
							}
							// set the content of the read only input
							if ( fromProjectBranchInput ) {
								fromProjectBranchInput.value = initialModifiedBranchName;
							} else {
								alert("getElementById issueresolver-update-from-branch is not defined");
							}

						}).catch(function(error) {

							var errorMessage = 'status: ' + error.response.status + ' - text: ' + error.response.statusText;
							alert(errorMessage);
							divUpdateResult.appendChild(document.createTextNode(errorMessage));
							divUpdateResult.style.display ='block';

						});
					};

					// Update - form - preview (checkbox, optional)
					var formUpdatePreview = dom.createElement(formUpdate, 'div', { className: 'modal-field' });				
					var formUpdatePreviewLabel = dom.createElement(formUpdatePreview, 'label', { for: 'issueresolver-update-preview' });
					formUpdatePreviewLabel.appendChild(document.createTextNode('Preview'));
					dom.createElement(formUpdatePreview, 'input', { id: 'issueresolver-update-preview', type: 'checkbox', name: 'preview', value: 'true'});
					dom.createElement(formUpdatePreview, 'div', { className: 'modal-field-description', textContent: 'If set, issues are not actually resolved, but only matched and checked, no changes are made' });

					// Update - form - skipAssign (checkbox, optional)
					var formUpdateSkipAssign = dom.createElement(formUpdate, 'div', { className: 'modal-field' });				
					var formUpdateSkipAssignLabel = dom.createElement(formUpdateSkipAssign, 'label', { for: 'issueresolver-update-skipassign' });
					formUpdateSkipAssignLabel.appendChild(document.createTextNode('Skip assignments'));
					dom.createElement(formUpdateSkipAssign, 'input', { id: 'issueresolver-update-skipassign', type: 'checkbox', name: 'skipAssign', value: 'true'});
					dom.createElement(formUpdateSkipAssign, 'div', { className: 'modal-field-description', textContent: 'If set, issue assignments are skipped' });

					// Update - form - skipComments (checkbox, optional)
					var formUpdateSkipComments = dom.createElement(formUpdate, 'div', { className: 'modal-field' });				
					var formUpdateSkipCommentsLabel = dom.createElement(formUpdateSkipComments, 'label', { for: 'issueresolver-update-skipcomments' });
					formUpdateSkipCommentsLabel.appendChild(document.createTextNode('Skip comments'));
					dom.createElement(formUpdateSkipComments, 'input', { id: 'issueresolver-update-skipcomments', type: 'checkbox', name: 'skipComments', value: 'true'});
					dom.createElement(formUpdateSkipComments, 'div', { className: 'modal-field-description', textContent: 'If set, issue comments are skipped' });

					// internet explorer BUG add a last checked check-box 
					dom.createElement(formUpdate, 'input', { id: 'issueresolver-update-internet-explorer-workaround', type: 'checkbox', name: 'dontCare', value: 'true'});
					document.getElementById('issueresolver-update-internet-explorer-workaround').checked = true;
					document.getElementById('issueresolver-update-internet-explorer-workaround').style.display = "none";

					// wait until dynamic elements are created
					setTimeout( function() {
						
						// Update - form - button
						var formUpdateButton = dom.createElement(formUpdate, 'div', { className: 'modal-field' });
						var formUpdateButtonButton = dom.createElement(formUpdateButton, 'button', { textContent: 'Update' });

						// Result placeholder
						var divUpdateResult = dom.createElement(parent, 'div', {});
						divUpdateResult.style.display = 'none';
						dom.createElement(divUpdateResult, 'h2', { className: 'issueresolver-header', textContent: 'Update results'});

						// Update - form - onsubmit
						formUpdate.onsubmit = function() {
							
							// erase previous results
							dom.removeChildren(divUpdateResult);
							
							// start progress worker
							dom.startProgressWorker();
							
							// submit button is disabled
							formUpdateButtonButton.disabled=true;

							window.SonarRequest.postJSON(
									'/api/issueresolver/update',
									new FormData(formUpdate)
							).then(function(response) {
								
								dom.stopProgressWorker();
								
								if (response.hasOwnProperty('error')) {
									
									dom.removeChildren(divUpdateResult);
									divUpdateResult.appendChild( document.createTextNode( 'Error: ' + response.error) );
									divUpdateResult.style.display='block';
									formUpdateButtonButton.disabled=false;
									
								} else {
									
									divUpdateResult.appendChild(result.formatResult('Update', response));
									divUpdateResult.style.display='block';
									formUpdateButtonButton.disabled=false;
								}

							}).catch(function (error) {
								
								dom.stopProgressWorker();

								divUpdateResult.appendChild(result.formatError('Update', error));
								divUpdateResult.style.display='block';
								formUpdateButtonButton.disabled=false;

							});
							// avoid event bubbling
							return false;
						};
						
						// wait until From Branch input is loaded

						// Populate project key drop down list - used for the FROM PROJECT
						window.SonarRequest.getJSON(
								'/api/components/search',
								{ 'ps':100 ,'qualifiers':'TRK'}		
						).then(function(response) {

							var firstProjectKey ;
							var firstProject = true;

							for (var componentIndex = 0; componentIndex < response.components.length; componentIndex++) {
								var component = response.components[componentIndex];
								if ( firstProject ) {
									firstProjectKey = component.key;
									firstProject = false;
								}
								// fill the options
								dom.createElement(formUpdateFromProjectKey, 'option', { value: component.key, textContent: component.name });	
							}
							// set the hidden input
							if ( updateFromProjectKeyInput ) {
								updateFromProjectKeyInput.value = firstProjectKey;
							} else {
								alert("getElementById - issueresolver-update-fromProjectKey-input is not defined");
							}

							// need to populate the select containing the FROM PROJECT Branch

							// Populate branch drop down list - for the FROM project
							window.SonarRequest.getJSON(
									'/api/project_branches/list?project=' + encodeURI(firstProjectKey)
							).then(function(response) {

								var initialFromBranchName;
								var initialFromBranch = true;

								for (var fromBranchIndex = 0; fromBranchIndex < response.branches.length; fromBranchIndex++) {

									var fromBranch = response.branches[fromBranchIndex];
									if ( initialFromBranch ) {
										initialFromBranchName = fromBranch.name;
										initialFromBranch = false;
									}
									dom.createElement(branchSelectInput, 'option', { value: fromBranch.name, textContent: fromBranch.name });
								}
								// set the content of the read only input
								if ( fromProjectBranchInput ) {
									fromProjectBranchInput.value = initialFromBranchName;
								} else {
									alert("getElementById - issueresolver-update-from-branch is not defined");
								}

							}).catch(function(error) {

								var errorMessage = 'status: ' + error.response.status + ' - text: ' + error.response.statusText;
								alert(errorMessage);
								divUpdateResult.appendChild(document.createTextNode(errorMessage));
								divUpdateResult.style.display='block';

							});

						}).catch(function(error) {

							console.log(String(error));
							divUpdateResult.appendChild(result.formatError('Update', error.response.status));
							divUpdateResult.style.display='block';
							formUpdateButtonButton.disabled=false;

						});	
						
						// Populate branch drop down list - for the current target project
						window.SonarRequest.getJSON(
								'/api/project_branches/list?project=' + encodeURI(projectKey)
						).then(function(response) {

							var initialBranchName;
							var initialBranch = true;

							for (var branchIndex = 0; branchIndex < response.branches.length; branchIndex++) {

								var branch = response.branches[branchIndex];
								if ( initialBranch ) {
									initialBranchName = branch.name;
									initialBranch = false;
								}
								dom.createElement(currentBranchSelectInput, 'option', { value: branch.name, textContent: branch.name });
							}
							// set the content of the read only input
							if ( inputTargetProjectBranch ) {
								inputTargetProjectBranch.value = initialBranchName;
							} else {
								alert("getElementById - issueresolver-update-toProject-master-branch is not defined");
							}

						}).catch(function(error) {

							var errorMessage = 'status: ' + error.response.status + ' - text: ' + error.response.statusText;
							alert(errorMessage);
							divUpdateResult.appendChild(document.createTextNode(errorMessage));
							divUpdateResult.style.display='block';

						});
						
					} , 500 );

				}
			};
		}
	};
});
