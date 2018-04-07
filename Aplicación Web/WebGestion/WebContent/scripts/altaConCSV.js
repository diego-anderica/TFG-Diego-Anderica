function comprobarCompatibilidad() {
	// Check for the various File API support.
	if (window.FileReader) {
		// FileReader are supported.
	} else {
		alert("Su navegador no soporta la carga de archivos.");
		$("#csvFile").prop("disabled", true);
		$("#btnSubirCSV").prop("disabled", true);
	}
}

function leerCSV() {
	var inFile = document.getElementById("csvFile").files[0];
	var reader = new FileReader();
	
	if (inFile == null) {
		alert("No se ha seleccionado ningún archivo");
	} else {
		if (confirm("Se va a proceder a registrar en la base de datos el archivo con los nuevos usuarios. ¿Desea continuar?")) {
			reader.readAsText(inFile);
			
			reader.onload = function (event) {
				var csv = event.target.result;
				procesarDatos(csv);
			};
			
			reader.onerror = function (event) {
				alert("Ha ocurrido un error al leer el archivo");
			};
		}
	}
}

function procesarDatos(csv) {
	var allTextLines = csv.split(/\r\n|\n/);
	var lines = [];
	
	document.body.style.cursor = "progress";

	for (var i = 1; i < allTextLines.length; i++) {
		var data = allTextLines[i].split(',');
		
		crearEnBBDD(data, 0);
    }
	
	alert ("Se han creado los usuarios correctamente");
	document.body.style.cursor = "auto";
}

function crearEnBBDD(familia, i) {
	var nFamilia = null;
	var newDoc = null;
	
	var txtNombreT1 = "";
	var txtApellido1T1 = "";
	var txtApellido2T1 = "";
	var txtTfnoT1 = "";
	var txtCorreoT1 = "";
	
	var txtNombreT2 = "";
	var txtApellido1T2 = "";
	var txtApellido2T2 = "";
	var txtTfnoT2 = "";
	var txtCorreoT2 = "";
	
	if (familia.length == 5) { //Sólo un tutor
		nFamilia = "familia" + familia[1];
	} else if (familia.length > 5) {
		nFamilia = "familia" + familia[1] + familia[6];
		
		txtNombreT2 = familia[5];
		txtApellido1T2 = familia[6];
		txtApellido2T2 = familia[7];
		txtTfnoT2 = familia[8];
		txtCorreoT2 = familia[9];
	}
	
	txtNombreT1 = familia[0];
	txtApellido1T1 = familia[1];
	txtApellido2T1 = familia[2];
	txtTfnoT1 = familia[3];
	txtCorreoT1 = familia[4];
	
	newDoc = firestore.collection("Usuarios").doc(nFamilia + i);
	
	console.log(nFamilia+i);
	
	newDoc.get().then(function(doc) {
	    if (doc.exists) {
	        crearEnBBDD(familia, i + 1);
	    } else {
	    		newDoc.set({
	    			"NombreTutor1"    : txtNombreT1,
	    			"Apellido1Tutor1" : txtApellido1T1,
	    			"Apellido2Tutor1" : txtApellido2T1,
	    			"TelefonoTutor1"  : txtTfnoT1,
	    			"CorreoTutor1"    : txtCorreoT1,
	    			"NombreTutor2"    : txtNombreT2,
	    			"Apellido1Tutor2" : txtApellido1T2,
	    			"Apellido2Tutor2" : txtApellido2T2,
	    			"TelefonoTutor2"  : txtTfnoT2,
	    			"CorreoTutor2"    : txtCorreoT2
	    		}).then(function() {
				    
					});
	    }
	});
}
//https://mounirmesselmeni.github.io/2012/11/20/reading-csv-file-with-javascript-and-html5-file-api/





/**
 * Module for displaying "Waiting for..." dialog using Bootstrap
 *
 * @author Eugene Maslovich <ehpc@em42.ru>
 */

var waitingDialog = waitingDialog || (function ($) {
    'use strict';

	// Creating modal dialog's DOM
	var $dialog = $(
		'<div class="modal fade" data-backdrop="static" data-keyboard="false" tabindex="-1" role="dialog" aria-hidden="true" style="padding-top:15%; overflow-y:visible;">' +
		'<div class="modal-dialog modal-m">' +
		'<div class="modal-content">' +
			'<div class="modal-header"><h3 style="margin:0;"></h3></div>' +
			'<div class="modal-body">' +
				'<div class="progress progress-striped active" style="margin-bottom:0;"><div class="progress-bar" style="width: 100%"></div></div>' +
			'</div>' +
		'</div></div></div>');

	return {
		/**
		 * Opens our dialog
		 * @param message Custom message
		 * @param options Custom options:
		 * 				  options.dialogSize - bootstrap postfix for dialog size, e.g. "sm", "m";
		 * 				  options.progressType - bootstrap postfix for progress bar type, e.g. "success", "warning".
		 */
		show: function (message, options) {
			// Assigning defaults
			if (typeof options === 'undefined') {
				options = {};
			}
			if (typeof message === 'undefined') {
				message = 'Loading';
			}
			var settings = $.extend({
				dialogSize: 'm',
				progressType: '',
				onHide: null // This callback runs after the dialog was hidden
			}, options);

			// Configuring dialog
			$dialog.find('.modal-dialog').attr('class', 'modal-dialog').addClass('modal-' + settings.dialogSize);
			$dialog.find('.progress-bar').attr('class', 'progress-bar');
			if (settings.progressType) {
				$dialog.find('.progress-bar').addClass('progress-bar-' + settings.progressType);
			}
			$dialog.find('h3').text(message);
			// Adding callbacks
			if (typeof settings.onHide === 'function') {
				$dialog.off('hidden.bs.modal').on('hidden.bs.modal', function (e) {
					settings.onHide.call($dialog);
				});
			}
			// Opening dialog
			$dialog.modal();
		},
		/**
		 * Closes dialog
		 */
		hide: function () {
			$dialog.modal('hide');
		}
	};

})(jQuery);

