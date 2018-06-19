// Argumento representa fecha de versión del servicio
ToneAnalyzer toneAnalyzer = new ToneAnalyzer("2018-05-19");
LanguageTranslator translator = new LanguageTranslator();

TranslateOptions translateOptions = new TranslateOptions.Builder()
		.addText(mensaje.getMensaje())
		.modelId("es-en")
		.build();

TranslationResult result = translator.translate(translateOptions)
		.execute();

ToneOptions toneOptions = new ToneOptions.Builder()
		.html(result.getTranslations().get(0).getTranslation())
		.build();

ToneAnalysis tone = toneAnalyzer.tone(toneOptions).execute();