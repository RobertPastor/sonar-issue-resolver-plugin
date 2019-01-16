define(['dom'], function(dom) {
	return {
		create: function(parent) {
			
			// Setup the tabs
			// see new definition of create element in dom.js
			var divLayout = dom.createElement(parent, 'div', { className: 'settings-layout' });
			var divSide = dom.createElement(divLayout, 'div', { className: 'settings-side'});
			var divMain = dom.createElement(divLayout, 'div', { className: 'settings-main'});
			var ulMenu = dom.createElement(divSide, 'ul', { className: 'settings-menu'});		
			
			// create table as children of the divSide
			
			var actionsTable = dom.createElement(divSide, "table", {});
			actionsTable.style.border  = "thin dotted black";
			
			var trOne = dom.createElement(actionsTable, 'tr');
			
			const textSize = "12px";
			
			// --- first td
			var tdHeadActionExport = dom.createElement(trOne, 'td',{});
			tdHeadActionExport.style.backgroundColor = "yellow";
			var divHeadActionExport = dom.createElement(tdHeadActionExport, 'div', { className: 'feature-description', textContent: 'export issue data of issues that have been confirmed, reopened or resolved'});

			divHeadActionExport.style.fontSize = textSize;
			divHeadActionExport.style.fontWeight = "normal";
			divHeadActionExport.style.border = "thin dotted blue";
						
			// --- second td
			var tdHeadActionImport = dom.createElement(trOne, 'td', {});
			tdHeadActionImport.style.backgroundColor = "yellow";

			var divHeadActionImport = dom.createElement(tdHeadActionImport, 'div', { className: 'feature-description', textContent: 'import issue data and transition issues that are matching a set of criteria'});

			divHeadActionImport.style.fontSize = textSize;
			divHeadActionImport.style.fontWeight = "normal";
			divHeadActionImport.style.border = "thin dotted blue";
			
			
			// ----- third td
			var tdHeadActionUpdate = dom.createElement(trOne, 'td', {});
			tdHeadActionUpdate.style.backgroundColor = "yellow";

			var divHeadActionUpdate = dom.createElement(tdHeadActionUpdate, 'div', { className: 'feature-description', textContent: 'update in a target project issue data and transition issues that are matching a set of criteria'});

			divHeadActionUpdate.style.fontSize = textSize;
			divHeadActionUpdate.style.fontWeight = "normal";
			divHeadActionUpdate.style.border = "thin dotted blue";
			

			//----------- second row with the links
			var trTwo = dom.createElement(actionsTable, 'tr', {});
			
			var tdActionExport = dom.createElement(trTwo, 'td', {});
			var divActionExport = dom.createElement(tdActionExport, 'div', {});
			divActionExport.id = "Export";
			
			
			var tdActionImport = dom.createElement(trTwo, 'td', {});
			var divActionImport = dom.createElement(tdActionImport, 'div', {});
			divActionImport.id = "Import";


			var tdActionUpdate = dom.createElement(trTwo, 'td');
			var divActionUpdate = dom.createElement(tdActionUpdate, 'div', {});
			divActionUpdate.id = "Update";
			

			// Return the tabs object
			return {
				tabParent: divMain,
				menuParent: ulMenu,
				links: [],
				show: function(name){
					dom.removeChildren(this.tabParent);
					this.links.forEach(function(item) {
						if(item.name == name) {
							item.link.className = 'active';
							item.tab.show(divMain);
						} else {
							item.link.className = '';
						}
					});
				},
				tab: function(name, tab) {
					var div = dom.getElementById(name);
					var a = dom.createElement(div, 'a', { textContent: name, href: '#' });
					var thisObject = this;
						
					a.onclick = function() {
						thisObject.show(name);
					}
					
					this.links.push({ name: name, link: a, tab: tab });
				},
			};
		},
	}
});
