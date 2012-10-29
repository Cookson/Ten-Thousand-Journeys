new Module("default", "cookson", "0.7", function() {
	module.registerGameFieldElementType("cellMarker", {
		onInit: function(x, y) {
			module.put(x, y, this);
		},
		cssRules: function() {
			return "					\
			div.$type$ {				\
				background-color: #f00;	\
				opacity: 0.5;			\
				width:32px;				\
				height: 32px;			\
			}							\
			";
		}
	});
});
	
