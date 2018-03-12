// Initialize Firebase
var config = {
			apiKey: "AIzaSyDc2LIU42E1opUdDlfZ_G0WhSq2vtokiGs",
			authDomain: "tfg-diego-anderica.firebaseapp.com",
			databaseURL: "https://tfg-diego-anderica.firebaseio.com",
			projectId: "tfg-diego-anderica",
			storageBucket: "tfg-diego-anderica.appspot.com",
			messagingSenderId: "352882689555"
};
firebase.initializeApp(config);

var firestore = firebase.firestore();