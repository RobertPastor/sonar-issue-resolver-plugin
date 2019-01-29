define(['dom'], function(dom) {
	return {
		
		createProgressBar : function ( parent ) {
			
			var divProgressBar = dom.createElement( parent, 'div', { className: 'progressBarDiv' });
			var progressElement = dom.createElement( divProgressBar , 'progress', { id: 'issue-resolver-worker-id' , className: 'progressBar' , max: 100, value: 0 } );
			progressElement.style.width = "100%";
			progressElement.style.height = "5px";
			progressElement.style.backgroundColor = "cyan";
			var spanElement = dom.createElement( progressElement , 'span' , { id: 'issue-resolver-value-id' , className: 'progressVal' ,   value: 0 } );
			
		},
		
		isBrowserInternetExplorer : function () {
			
			var nAgt = window.navigator.userAgent;
			var browserName  = window.navigator.appName;
			var browserIsInternetExplorer = false;
			
			// In Opera, the true version is after "Opera" or after "Version"
			if ( nAgt.indexOf("Opera") != -1 ) {
			   browserName = "Opera";
			}
			
			// In MSIE, the true version is after "MSIE" in userAgent
			if ( ( (verOffset = nAgt.indexOf("MSIE"))!=-1) || ( nAgt.match(/Trident/)  ) ) {
			   browserName = "Microsoft Internet Explorer";
			   browserIsInternetExplorer = true;
			}
			
			// In Chrome, the true version is after "Chrome" 
			if ((verOffset=nAgt.indexOf("Chrome"))!=-1) {
			   browserName = "Chrome";
			}
			
			// In Safari, the true version is after "Safari" or after "Version" 
			if ((verOffset=nAgt.indexOf("Safari"))!=-1) {
			   browserName = "Safari";
			   
			}
			
			// In Firefox, the true version is after "Firefox" 
			if ((verOffset=nAgt.indexOf("Firefox"))!=-1) {
				browserName = "Firefox";
			}
			return browserIsInternetExplorer;
			
		},
		create: function( parent ) {
			
			// Setup the tabs
			// see new definition of createElement in dom.js
			var divLayout = dom.createElement(parent, 'div', { className: 'settings-layout' });
			var divSide = dom.createElement(divLayout, 'div', { className: 'settings-side'});
			var divMain = dom.createElement(divLayout, 'div', { className: 'settings-main'});
			var ulMenu = dom.createElement(divSide, 'ul', { className: 'settings-menu'});		
			
			// create the progress bar
			this.createProgressBar ( divSide );
			
			// create table as children of the divSide
			var actionsTable = dom.createElement(divSide, "table", { });
			actionsTable.style.border  = "thin dotted black";
			actionsTable.style.width = "100%";
			
			var trOne = dom.createElement(actionsTable, 'tr');
			
			const textSize = "12px";
			
			// --- first td
			var tdHeadActionExport = dom.createElement(trOne, 'td',{});
			tdHeadActionExport.style.backgroundColor = "yellow";
			var divHeadActionExport = dom.createElement(tdHeadActionExport, 'div', { className: 'feature-description', textContent: 'export issue data of issues that have been confirmed, reopened or resolved'});

			divHeadActionExport.style.fontSize = textSize;
			divHeadActionExport.style.fontSize = textSize;
			divHeadActionExport.style.border = "thin dotted blue";
						
			// --- second td
			if ( this.isBrowserInternetExplorer() === false) {
				
				var tdHeadActionImport = dom.createElement(trOne, 'td', {});
				tdHeadActionImport.style.backgroundColor = "yellow";

				var divHeadActionImport = dom.createElement(tdHeadActionImport, 'div', { className: 'feature-description', textContent: 'import issue data and transition issues that are matching a set of criteria'});

				divHeadActionImport.style.fontSize = textSize;
				divHeadActionImport.style.fontWeight = "normal";
				divHeadActionImport.style.border = "thin dotted blue";
				
			}
			
			// ----- third td
			var tdHeadActionUpdate = dom.createElement(trOne, 'td', {});
			tdHeadActionUpdate.style.backgroundColor = "yellow";

			var divHeadActionUpdate = dom.createElement(tdHeadActionUpdate, 'div', { className: 'feature-description', textContent: 'update issue data and transition issues that are matching a set of criteria'});

			divHeadActionUpdate.style.fontSize = textSize;
			divHeadActionUpdate.style.fontWeight = "normal";
			divHeadActionUpdate.style.border = "thin dotted blue";
			
			//----------- second row with the links
			var trTwo = dom.createElement(actionsTable, 'tr', {});
			
			var tdActionExport = dom.createElement(trTwo, 'td', {});
			var divActionExport = dom.createElement(tdActionExport, 'div', {});
			divActionExport.style.border = "thin dotted red";
			divActionExport.style.textAlign = "center";
			divActionExport.id = "Export";
			
			if ( this.isBrowserInternetExplorer() === false) {
				
				var tdActionImport = dom.createElement(trTwo, 'td', {});
				var divActionImport = dom.createElement(tdActionImport, 'div', {});
				divActionImport.style.border = "thin dotted red";
				divActionImport.style.textAlign = "center";
				divActionImport.id = "Import";
				
			}

			var tdActionUpdate = dom.createElement(trTwo, 'td');
			var divActionUpdate = dom.createElement(tdActionUpdate, 'div', {});
			divActionUpdate.style.border = "thin dotted red";
			divActionUpdate.style.textAlign = "center";
			divActionUpdate.id = "Update";
			
			// Return the tabs object
			return {
				tabParent: divMain,
				menuParent: ulMenu,
				links: [],
				show: function(name){
					dom.removeChildren(this.tabParent);
					this.links.forEach( function ( item ) {
						if(item.name == name) {
							item.link.className = 'active';
							item.tab.show(divMain);
						} else {
							item.link.className = '';
						}
					} ) ;
				},
				tab: function(name, tab) {
					// search for the DIV with name = Export, Import or Update (as defined in the divActionUpdate.id)
					var div = dom.getElementById(name);
					if (div) {
						var a = dom.createElement(div, 'a', { textContent: name, href: '#' });
						var thisObject = this;
							
						a.onclick = function() {
							// set a backgroundColor to the selected div
							var linkArray = ['Update', 'Import', 'Export'];
							linkArray.forEach ( function ( item ) {
								if ( dom.getElementById(item) ) {
									// need to do this as the Import feature is not available in Internet Explorer
									dom.getElementById(item).style.backgroundColor = "white";
								}
							} );
							div.style.backgroundColor = "cyan";
							thisObject.show(name);
							// avoid event bubbling
							return false;
						}
						// add the link to the links array
						this.links.push({ name: name, link: a, tab: tab });
					}
				},
			};
		},
	}
});
