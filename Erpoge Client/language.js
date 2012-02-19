/* language.js: Core code for language settings
 */
var Language = {
	currentLanguage : null
};
Language.setCurrentLanguage = function _(language) {
	localStorage.setItem("language", language);
	Language.currentLanguage = Language[language];
};
