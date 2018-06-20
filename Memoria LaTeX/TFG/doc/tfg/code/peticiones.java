// Argumento de fecha representa fecha de versión del servicio
ToneAnalyzer toneAnalyzer = new ToneAnalyzer("2018-05-19");
LanguageTranslator translator = new LanguageTranslator();

// Se crea el objeto con el texto a traducir y los idiomas destino-origen y se obtiene el resultado
TranslateOptions translateOptions = new TranslateOptions.Builder().addText(mensaje.getMensaje())
		.modelId("es-en")
		.build();
TranslationResult result = translator.translate(translateOptions)
		.execute();

// Se crea el objeto con el texto a traducir y se obtiene el tono del mismo
ToneOptions toneOptions = new ToneOptions.Builder()
		.html(result.getTranslations().get(0).getTranslation())
		.build();
ToneAnalysis tone = toneAnalyzer.tone(toneOptions).execute();