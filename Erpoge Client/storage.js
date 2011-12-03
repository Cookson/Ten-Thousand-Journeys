/*
	Использованные индексы:
	1 - анимация
	101 - быстрое обновление
*/
if (useDB) { // Если работа с базой данных включена в игре
// Работа с local storage и WebSQL
DB=openDatabase("storage", "1.0", "Local database", 200000);
DB.transaction(function(tx,result) {
	tx.executeSql("create table if not exists seenCells (x int, y int)");
	tx.executeSql("select * from seenCells limit 1", [], function(tx,result) {
		if (result.rows.length==0) {
			for (var i=0;i<100;i++) {	
				for (var j=0;j<100;j++) {
					tx.executeSql("insert into seenCells (x,y) values (?,?)", [i,j]);
				}
			}
		} else {
		}
	});
});
}
onLoadEvents['storage']=function() {
	if (!localStorage.getItem(3)) {
		localStorage.setItem(3, 1024);
		localStorage.setItem(4, 600);
	}
};

