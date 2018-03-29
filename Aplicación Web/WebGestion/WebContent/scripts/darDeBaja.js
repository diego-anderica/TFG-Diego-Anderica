function rellenarLista() {
				var select = document.getElementById("cbFamilias");
	var opt = null;
	
	firestore.collection("Usuarios").get().then(function(querySnapshot) {
	    querySnapshot.forEach(function(doc) {
	        opt = document.createElement('option');
	        opt.value = doc.id;
	        opt.innerHTML = doc.id;
	        select.appendChild(opt);
	        rellenarFila(select);
	    });
	});
}

function rellenarFila(cb) {
	var familia = document.getElementById("familia");
	var nombreT1 = document.getElementById("nombreT1");
	var nombreT2 = document.getElementById("nombreT2");
	var tfnoT1 = document.getElementById("tfnoT1");
	var tfnoT2 = document.getElementById("tfnoT2");
	var correoT1 = document.getElementById("correoT1");
	var correoT2 = document.getElementById("correoT2");
	
	var usuarioBBDD = cb.options[cb.selectedIndex].text;
	
	var docRef = firestore.collection("Usuarios").doc(usuarioBBDD);

	docRef.get().then(function(doc) {
	    if (doc.exists) {
	    		datos = doc.data();
			familia.value = doc.id;
			nombreT1.value = datos.NombreTutor1 + " " + datos.Apellido1Tutor1 + " " + datos.Apellido2Tutor1;
			
			if (datos.NombreTutor2 != "") {
				nombreT2.value = datos.NombreTutor2 + " " + datos.Apellido1Tutor2 + " " + datos.Apellido2Tutor2;
			}
			
			tfnoT1.value = datos.TelefonoTutor1;
			tfnoT2.value = datos.TelefonoTutor2;
			correoT1.value = datos.CorreoTutor1;
			correoT2.value = datos.CorreoTutor2;
	    }
	}).catch(function(error) {
	    alert ("Ha ocurrido un error al obtener los datos.")
	});
}

function darDeBaja(){
	var familia = document.getElementById("familia").value;
	
	if(confirm("¿Realmente desea eliminar el usuario " + familia + "?")) {
		firestore.collection("Usuarios").doc(familia).delete().then(function() {
		    alert("El usuario " + familia + " se ha borrado correctamente de la base de datos.");
		}).catch(function(error) {
		    alert("Ha ocurrido un problema al eliminar el usuario");
		});
	}
}