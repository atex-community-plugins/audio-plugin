function onDocumentLoaded() {
	console.log("Start Load");

	ACInitFFT();
	initializeAudioLayerControls();

	var audioLayerControl = document.querySelector("#audioLayerControl");
    audioLayerControl.removeAllSequenceEditors();
    var leftEditor = audioLayerControl.createSequenceEditor("Left Channel");
    var rightEditor = audioLayerControl.createSequenceEditor("Right Channel");
    audioLayerControl.setLinkMode(true);
    
    console.log("Finish Load");
}