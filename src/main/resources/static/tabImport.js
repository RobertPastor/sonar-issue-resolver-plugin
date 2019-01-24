define(['dom', 'result'], function(dom, result) {
	return {
		create: function(projectKey) {	
			return {
				projectKey: projectKey,
				show: function(parent) {
					
					// Header and description
					dom.removeChildren(parent);
					
					//dom.createElement(parent, 'h2', { className: 'issueresolver-header', textContent: 'Import'});
					dom.createElement(parent, 'h2', { className: 'issueresolver-description big-spacer-bottom', textContent: 'Import a datafile with issues (created using export), that will be matched to current issues using rule key, component and location.'});
					
					// Import - form
					var formImport = dom.createElement(parent, 'form', { id: 'issueresolver-import-form' , 'enctype': 'multipart/form-data' });
					
					// Import - form - projectKey (hidden)
					dom.createElement(formImport, 'input', { id: 'issueresolver-import-projectKey', type:'text', name: 'projectKey', value: projectKey });
					document.getElementById('issueresolver-import-projectKey').readOnly = true;
					document.getElementById('issueresolver-import-projectKey').style.width = "400px";
					document.getElementById('issueresolver-import-projectKey').style.display = "none";

					// Import - form - data file
					var formImportData = dom.createElement(formImport, 'div', { className: 'modal-field'});
					var formImportDataLabel = dom.createElement(formImportData, 'label', { for: 'issueresolver-import-data'});
					formImportDataLabel.appendChild(document.createTextNode('Data'));
					
					dom.createElement(formImportDataLabel, 'em', { className:'mandatory', textContent: '*'});
					dom.createElement(formImportData, 'input', { id: 'issueresolver-import-data', type:'file', name:'data', accept: 'application/json,.json'});
					
					dom.createElement(formImportData, 'div', { className:'modal-field-description', textContent: 'The issues data to be matched with'});

					// Import - form - target branch (optional)
					// 14 January 2019 - Robert PASTOR
					var formImportTargetBranch = dom.createElement(formImport, 'div', { className: 'modal-field'});
					var formImportTargetBranchLabel = dom.createElement(formImportTargetBranch, 'label', { for: 'issueresolver-import-target-branch'});
					
					formImportTargetBranchLabel.appendChild(document.createTextNode('Target Branch'));
					
					var branchInput = dom.createElement(formImportTargetBranch, 'input', { id: 'issueresolver-import-target-branch', type:'text', name:'target-branch'});
					branchInput.readOnly = true;
					document.getElementById('issueresolver-import-target-branch').style.visibility = "hidden";
					document.getElementById('issueresolver-import-target-branch').style.display = "none";

					// selection of a branch
					var branchSelectInput = dom.createElement(formImportTargetBranch, 'select', { id: 'issueresolver-import-branch', name: 'select-branch' });
					
					// listen to the branch selection changes
					document.getElementById('issueresolver-import-branch').onchange = function() {
						// set the value of hidden input
						document.getElementById('issueresolver-import-target-branch').value = this.options[this.selectedIndex].value;
					};
					
					dom.createElement(formImportTargetBranch, 'div', { className:'modal-field-description', textContent: 'The target branch in the current project'});
					
					// Import - form - preview (checkbox, optional)
					var formImportPreview = dom.createElement(formImport, 'div', { className: 'modal-field' });				
					var formImportPreviewLabel = dom.createElement(formImportPreview, 'label', { for: 'issueresolver-import-preview' });
					formImportPreviewLabel.appendChild(document.createTextNode('Preview'));
					
					dom.createElement(formImportPreview, 'input', { id: 'issueresolver-import-preview', type: 'checkbox', name: 'preview', value: 'true'});
					dom.createElement(formImportPreview, 'div', { className: 'modal-field-description', textContent: 'If set, issues are not actually resolved, but only matched and checked, no changes are made' });

					// Import - form - skipAssign (checkbox, optional)
					var formImportSkipAssign = dom.createElement(formImport, 'div', { className: 'modal-field' });				
					var formImportSkipAssignLabel = dom.createElement(formImportSkipAssign, 'label', { for: 'issueresolver-import-skipassign' });
					formImportSkipAssignLabel.appendChild(document.createTextNode('Skip assignments'));
					
					dom.createElement(formImportSkipAssign, 'input', { id: 'issueresolver-import-skipassign', type: 'checkbox', name: 'skipAssign', value: 'true'});
					dom.createElement(formImportSkipAssign, 'div', { className: 'modal-field-description', textContent: 'If set, issue assignments are skipped' });

					// Import - form - skipComments (checkbox, optional)
					var formImportSkipComments = dom.createElement(formImport, 'div', { className: 'modal-field' });				
					var formImportSkipCommentsLabel = dom.createElement(formImportSkipComments, 'label', { for: 'issueresolver-import-skipcomments' });
					formImportSkipCommentsLabel.appendChild(document.createTextNode('Skip comments'));
					
					dom.createElement(formImportSkipComments, 'input', { id: 'issueresolver-import-skipcomments', type: 'checkbox', name: 'skipComments', value: 'true'});
					dom.createElement(formImportSkipComments, 'div', { className: 'modal-field-description', textContent: 'If set, issue comments are skipped' });
				
					// Import - form - button
					var formImportButton = dom.createElement(formImport, 'div', { className: 'modal-field' });
					var formImportButtonButton = dom.createElement(formImportButton, 'button', { textContent: 'Import' });

					// Result placeholder
					var divImportResult = dom.createElement(parent, 'div', {});
					divImportResult.style.display = 'none';
					dom.createElement(divImportResult, 'h2', { className: 'issueresolver-header', textContent: 'Import result'});
					
					// Import - form - onsubmit
					formImport.onsubmit = function() {
						
						// erase previous results
						dom.removeChildren(divImportResult);
						
						// start progress worker
						dom.startProgressWorker();
						
						// disable the submit button
						formImportButtonButton.disabled=true;
						
						// call new api defined by this plugin
						window.SonarRequest.postJSON(
							    '/api/issueresolver/import',
							    new FormData(formImport)
							).then(function(response) {
								
								dom.stopProgressWorker();
								
								if (response.hasOwnProperty('error')) {
									
									dom.removeChildren(divImportResult);
									divImportResult.appendChild( document.createTextNode( 'Error: ' + response.error) );
									divImportResult.style.display='block';
									formImportButtonButton.disabled=false;
									
								} else {
									
									divImportResult.appendChild(result.formatResult('Import', response));
									divImportResult.style.display='block';
									formImportButtonButton.disabled=false;
									
								}
								
							}).catch(function (error) {
								console.log(error);
								//divImportResult.appendChild(result.formatError('Import', (error.response.status)));
								divImportResult.style.display='block';
								formImportButtonButton.disabled=false;
							});
						
						// avoid event bubbling
						return false;
					};
					
					// Populate branch drop down list
					window.SonarRequest.getJSON(
						'/api/project_branches/list?project=' + encodeURI(projectKey)
					).then(function(response) {
						
						var initialBranchName;
						var initial = true;
						
						for(var branchIndex = 0; branchIndex < response.branches.length; branchIndex++) {
							
							var branch = response.branches[branchIndex];
							if (initial) {
								initialBranchName = branch.name;
								initial = false;
							}
							dom.createElement(branchSelectInput, 'option', { value: branch.name, textContent: branch.name });
						}
						// set the content of the read only input
						if (document.getElementById('issueresolver-import-target-branch')) {
							document.getElementById('issueresolver-import-target-branch').value = initialBranchName;
						} else {
							alert ("Get Element By Id - issueresolver-import-target-branch - is not defined");
						}
						
					}).catch(function(error) {
						
						var errorMessage = 'status: ' + error.response.status + ' - text: ' + error.response.statusText;
						alert(errorMessage);
						divExportResult.appendChild(document.createTextNode(errorMessage));
						divExportResult.style.display='block';
						
					});
				}
			};
		}
	};
});
