define(['dom', 'tabsFactory', 'tabUpdate', 'tabExport', 'tabImport'], function(dom, tabsFactory, tabUpdate, tabExport, tabImport) {
	return {
		main: function(options) {
			
			dom.removeChildren(options.el);
			
			/* add the header */
			var header = dom.createElement(options.el, 'header', { className: 'page-header'});
			var textContent = 'Issue Resolver: allows you to synchronise issues that are resolved with false positive and won\'t fix.';
			
			dom.createElement(header, 'h3', { className: 'page-title', textContent: textContent});
			//dom.createElement(header, 'div', { className: 'page-description', textContent: 'Allows you to synchronise issues that are resolved with false positive and won\'t fix.'});
			
			var tabs = tabsFactory.create(options.el);
			// create the tabs for each feature
			tabs.tab('Update', tabUpdate.create(options.component.key));
			tabs.tab('Export', tabExport.create(options.component.key));
			tabs.tab('Import', tabImport.create(options.component.key));
			
			// do not set any feature activated by default
			//tabs.show('Update');
			
			/**
			 * worker responsible for the progress bar
			 */
			var progressWorker = undefined;

		}
	}
});
