define(['config', 'dom'], function(config, dom) {
	return {
		create: function(projectKey) {
			return {
				projectKey: projectKey,
				show: function(parent) {
					
					dom.removeChildren(parent);
					
					//dom.createElement(parent, 'h2', { className: 'issueresolver-header', textContent: 'Export'});
					dom.createElement(parent, 'h2', { className: 'issueresolver-description big-spacer-bottom', textContent: 'Export issues that are resolved as false positive or won\'t fix in a JSON data file.'});
					
					// Export - form
					var formExport = dom.createElement( parent, 'form', { id: 'issueresolver-export-form' });
					
					// Export - form - target branch (optional)
					// 14 January 2019 - Robert PASTOR
					var formExportTargetBranch = dom.createElement(formExport, 'div', { className: 'modal-field'});
					var formExportTargetBranchLabel = dom.createElement(formExportTargetBranch, 'label', { for: 'issueresolver-export-target-branch'});
					
					formExportTargetBranchLabel.appendChild(document.createTextNode('Target Branch'));
					
					// input with select for selecting the branch
					var branchSelectInput = dom.createElement(formExportTargetBranch, 'select', { id: 'issueresolver-export-branch', name: 'select-branch' });
					
					// warning - input id used below 
					var branchInput = dom.createElement(formExportTargetBranch, 'input', { id: 'issueresolver-export-target-branch', type:'text', name:'target-branch'});
					branchInput.readOnly = true;
					
					dom.createElement(formExportTargetBranch, 'div', { className:'modal-field-description', textContent: 'The project branch where to search for matching issues'});
					
					// listen to the branch selection changes
					document.getElementById('issueresolver-export-branch').onchange = function() {
						// set the value of hidden input
						document.getElementById('issueresolver-export-target-branch').value = this.options[this.selectedIndex].value;
					};
					// Export - form - button
					var formExportButton = dom.createElement(formExport, 'div', { className: 'modal-field'});
					dom.createElement(formExportButton, 'button', { textContent: 'Export'});
					
					// Result placeholder
					var divExportResult = dom.createElement(parent, 'div', {});
					divExportResult.style.display = 'none';
					dom.createElement(divExportResult, 'h2', { className: 'issueresolver-header', textContent: 'Export results'});

					// Export - form - on submit
					formExport.onsubmit = function() {
						// warning - use document here and not dom with restricted applied methods (see dom.js)
						var branch = document.getElementById("issueresolver-export-target-branch").value;
						if (branch && (branch.length>0)) {
							window.location = config.basename + 'api/issueresolver/export?projectKey=' + encodeURI(projectKey) + '&branch=' + encodeURI(branch);
						} else {
							window.location = config.basename + 'api/issueresolver/export?projectKey=' + encodeURI(projectKey) + '&branch=master' ;
						}
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
						if (document.getElementById('issueresolver-export-target-branch')) {
							document.getElementById('issueresolver-export-target-branch').value = initialBranchName;
						} else {
							alert ("getElementById issueresolver-export-target-branch is not defined");
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
