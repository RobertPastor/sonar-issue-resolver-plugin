define(['config', 'dom'], function(config, dom) {
	return {
		create: function(projectKey) {
			return {
				projectKey: projectKey,
				show: function(parent) {
					dom.createElement(parent, 'h2', { className: 'issueresolver-header', textContent: 'Export'});
					dom.createElement(parent, 'h2', { className: 'issueresolver-description big-spacer-bottom', textContent: 'Export issues that are resolved as false positive or won\'t fix as a data file.'});
					
					// Export - form
					var formExport = dom.createElement( parent, 'form', { id: 'issueresolver-export-form' });
					
					// Export - form - target branch (optional)
					// 14 January 2019 - Robert PASTOR
					var formExportTargetBranch = dom.createElement(formExport, 'div', { className: 'modal-field'});
					var formExportTargetBranchLabel = dom.createElement(formExportTargetBranch, 'label', { for: 'issueresolver-export-target-branch'});
					
					formExportTargetBranchLabel.appendChild(document.createTextNode('Target Branch'));
					
					dom.createElement(formExportTargetBranch, 'input', { id: 'issueresolver-export-target-branch', type:'text', name:'target-branch'});
					dom.createElement(formExportTargetBranch, 'div', { className:'modal-field-description', textContent: 'The target branch where to search for issues matching'});

					// Export - form - button
					var formExportButton = dom.createElement(formExport, 'div', { className: 'modal-field'});
					dom.createElement(formExportButton, 'button', { textContent: 'Export'});

					// Export - form - onsubmit
					formExport.onsubmit = function() {
						window.location = config.basename + 'api/issueresolver/export?projectKey=' + encodeURI(projectKey);
						return false;
					};
				}
			};
		}
	};	
});
