define(['dom'], function(dom) {
	return {
		formatFailures: function(type, failures) {
			
			var divFailures = document.createElement('div');
			divFailures.style = "paddding-top: 0.5em;"
			dom.createElement(divFailures, 'span', { style: 'font-weight: bold; font-style: italic;', textContent: type+':'});
			var ulFailures = dom.createElement(divFailures, 'ul', {});
			ulFailures.style = "list-style-type: disc; padding-left: 1.5em;"
			failures.forEach(function(item) {
				dom.createElement(ulFailures, 'li', { textContent: item });
			});
			
			return divFailures;
		},
		
		formatIssuesWouldHaveBeen: function(preview, size) {
			var result = ", " + size + " issue";
			if(size > 1) {
				result = result + "s";
			}
			result = result + " ";
			
			if(preview) {
				result = result + "would have";
			} else if(size == 1) {
				result = result + "has";
			} else {
				result = result + "have";
			}
			
			result = result + " been ";
			return result;
		},
		
		formatIssues: function(type, response) {
			var currentDate = new Date();
			var resultText = ("0" + currentDate.getHours()).slice(-2) + ":"  
	            + ("0" + currentDate.getMinutes()).slice(-2) + "." 
	            + ("0" + currentDate.getSeconds()).slice(-2) + " - " + type + " succeeded";
	            
	        // Issues
	        resultText = resultText + "; "
	        + response.issues + " issue" + (response.issues > 1 ? "s": "") + " read";
	        
	        // Duplicate keys
	        if(response.duplicateKeys>0) {
	        	resultText = resultText + " ("+ response.duplicateKeys+" duplicate keys)";
	        }
	        
	        // Matched issues
	        resultText = resultText + this.formatIssuesWouldHaveBeen(false, response.matchedIssues) + "matched";
	        
	        // Transitioned issues
	        if(response.transitionedIssues > 0) {
	        	resultText = resultText + this.formatIssuesWouldHaveBeen(response.preview, response.transitionedIssues) + "resolved";
	        }
	        
	        // Assigned issues
	        if(response.assignedIssues > 0) {
	        	resultText = resultText + this.formatIssuesWouldHaveBeen(response.preview, response.assignedIssues) + "assigned";
	        }
	        
	        // Commented issues
	        if(response.commentedIssues > 0) {
	        	resultText = resultText + this.formatIssuesWouldHaveBeen(response.preview, response.commentedIssues) + "commented";
	        }      
	        
	        resultText = resultText + ".";
	        return resultText;
		},
		
		formatMatchingIssues: function( matchingIssues, preview) {
			
			/** write the content of the IssueKey class **/
			
			var matchingIssuesDiv = document.createElement('div');
			var matchingIssuesTable = dom.createElement(matchingIssuesDiv, "table", {} );
			matchingIssuesTable.style.border = "thin dotted red";
			
			const textSize = "12px";
			
			// add the header
			var tr;
			tr = dom.createElement(matchingIssuesTable, 'tr');
			tr.style.border = "thin dotted red";
			
			var tdHeader;
			var divHead;
			
			var headerArray = ['#', 'File Path', 'Rule', 'Hash', 'Line', 'Message', 'Status', 'Resolution', 'Severity', 'Transition', 'Assign', 'Comment'];
			headerArray.forEach ( function ( head ) {
				
				  tdHeader = dom.createElement(tr, 'td', {});
				  tdHeader.style.backgroundColor = "yellow";
				  tdHeader.style.border = "thin dotted red";
				  
				  if (preview) {
					  // if preview = true -> consider the to be transitioned , to be assigned and to be commented
					  head = head + '-?';
				  }
				  divHead = dom.createElement(tdHeader, 'div', { className: 'feature-description', textContent: head });
				  divHead.style.fontSize = textSize;
				  divHead.style.fontWeight = 'bold';
				  
			});
			
			var index = 1;
			var first = true;
			var td;
			
			// insert the matching issues data
			matchingIssues.forEach( function (item) {
				
				first = true;
				
				tr = dom.createElement(matchingIssuesTable, 'tr');
				tr.style.border = "thin dotted blue";
				
				if (first) {
					
					td = dom.createElement(tr, 'td',{});
					td.style.border = "thin dotted blue";

					dom.createElement(td, 'div', { className: 'feature-description', textContent: (index).toString() });
					//divIssueIndex.style.border = 'thin dotted blue';
					index = index + 1;
					
					first = false;
					
				} else {
					index = index + 1;
				}
				
				// these keys are defined in the JAVA Issue Key, Issue Data and Issue Key Extensions
				var keyArray = ['longName' , 'rule' , 'hash' , 'line' , 'message' , 'status', 'resolution', 'severity', 'transitioned' , 'assigned' , 'commented']
				keyArray.forEach ( function ( key ) {
					
					td = dom.createElement(tr, 'td',{});
					td.style.border = "thin dotted blue";
					
					if (item && item.hasOwnProperty(key) ) {
						
						var div = dom.createElement(td, 'div', { className: 'feature-description', textContent: item[key] });
						div.style.textAlign = 'center';
						
						if ( ( (key === 'transitioned') || (key === 'assigned') || (key === 'commented') ) && ( item[key] === true ) ) {
							div.style.fontWeight = 'bold';
							div.style.backgroundColor = 'cyan';	
						}
					} else {
						dom.createElement(td, 'div', { className: 'feature-description', textContent: '' });
					}
				} );
			});
			
			// return the first div
			return matchingIssuesDiv;
			
		},
		
		formatResult: function (type, response) {
	        var divResult = document.createElement('div');

	        // Base result
			var baseResult = this.formatIssues(type, response);
	        dom.createElement(divResult, 'span', { style: 'font-weight:bold;', textContent: baseResult });
	        
	        // Match failures
	        if(response.matchFailures.length > 0) {
	        	divResult.appendChild(this.formatFailures('Matching failures', response.matchFailures));
	        }
	        
	        // Transition failures
	        if(response.transitionFailures.length > 0) {
	        	divResult.appendChild(this.formatFailures('Transition failures', response.transitionFailures));
	        }
	        
	        // Assign failures
	        if(response.assignFailures.length > 0) {
	        	divResult.appendChild(this.formatFailures('Assign failures', response.assignFailures));
	        }
	        
	        // Comment failures
	        if(response.commentFailures.length > 0) {
	        	divResult.appendChild(this.formatFailures('Comment failures', response.commentFailures));
	        }
	        
	        // 18 january 2019 - add the matching issues in a results table
	        if (response.matchingIssues && (response.matchingIssues.length > 0)) {
	        	divResult.appendChild(this.formatMatchingIssues( response.matchingIssues, response.preview));
	        }
	        
	        return divResult;
		},
	
		formatError: function(type, error) {
			var currentDate = new Date();
			var resultText = ("0" + currentDate.getHours()).slice(-2) + ":"  
            	+ ("0" + currentDate.getMinutes()).slice(-2) + "." 
            	+ ("0" + currentDate.getSeconds()).slice(-2) + " - "+type+" failed";
			
			 resultText = resultText + "; " + error;
			return document.createTextNode(resultText);
		}
	};
});
