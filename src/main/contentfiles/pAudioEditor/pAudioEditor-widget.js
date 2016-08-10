atex.onecms.register('ng-directive', 'pAudioEditor', ['spinjs', 'ACAAFilter', 'ACFFT', 'ACFIRFilter', 'ACSpectrum', 'audioLayerControl', 'audioplayback', 'audiosequence', 'AudioSequenceEditor', 'binarytoolkit', 'editorapp', 'fft', 'filedropbox', 'filesystemutility', 'mathutilities', 'recorder', 'recorderWorker', 'SpectrumDisplay', 'SpectrumWorker', 'wavetrack', 'volumeMeter'],
		function(spinjs, ACAAFilter, ACFFT, ACFIRFilter, ACSpectrum, audioLayerControl, audioplayback, audiosequence, AudioSequenceEditor, binarytoolkit, editorapp, fft, filedropbox, filesystemutility, mathutilities, recorder, recorderWorker, SpectrumDisplay, SpectrumWorker, wavetrack, volumeMeter) {
  var createSpinner = function() {
    return new spinjs({
      lines: 13, // The number of lines to draw
      length: 12, // The length of each line
      width: 4, // The line thickness
      radius: 16, // The radius of the inner circle
      corners: 1, // Corner roundness (0..1)
      rotate: 0, // The rotation offset
      direction: 1, // 1: clockwise, -1: counterclockwise
      color: '#aaa', // #rgb or #rrggbb or array of colors
      speed: 1, // Rounds per second
      trail: 60, // Afterglow percentage
      shadow: false, // Whether to render a shadow
      hwaccel: false, // Whether to use hardware acceleration
      className: 'spinner', // The CSS class to assign to the spinner
      zIndex: 2e9, // The z-index (defaults to 2000000000)
      top: '50px', // Top position relative to parent
      left: '50%' // Left position relative to parent
    }).spin();
  };

  return ['$timeout', '$q', 'FileService', 'Application', function($timeout, $q, FileService, Application) {
    return {
      replace: false,
      restrict: 'AE',

      scope: {
        'config': '=',
        'baseUrl': '@',
        'domainObjects': '=',
        'widgetId': '@'
      },

      templateUrl: atex.onecms.baseUrl + '/template.html',

      controller: function($scope) {
    	  
		$scope.getDurationStr = function(durationInSeconds) {
	      	var hours = Math.floor(durationInSeconds/360);
	      	var minutes = Math.floor((durationInSeconds - hours*360)/60);
	      	var seconds = Math.floor(durationInSeconds - hours*360 - minutes*60);
	      	var miliseconds = Math.floor((durationInSeconds % 1)*1000);
	      	
	      	var time = hours.toString().length == 1 ? '0' + hours.toString() : hours.toString();
	      	time += ':';
	      	time += minutes.toString().length == 1 ? '0' + minutes.toString() : minutes.toString();
	      	time += ':';
	      	time += seconds.toString().length == 1 ? '0' + seconds.toString() : seconds.toString();
	      	time += '.';
	      	time += miliseconds;
	      	return time;
	    }
    	  
        $scope.filePathDomainObject = $scope.domainObjects["filePath"];
        $scope.duration = $scope.domainObjects["duration"];
        $scope.filesDomainObject = $scope.domainObjects["files"];
        if ($scope.duration.getData()) {
        	$scope.durationStr = $scope.getDurationStr($scope.duration.getData());	
        } else {
        	$scope.durationStr = "00:00:00.000";
        }
        
        $scope.blacklist = $scope.config.blacklist || false;
        $scope.data = $scope.filePathDomainObject.getData() || undefined;
        $scope.configuredMimeTypes = $scope.config.mimeTypes || undefined;
        $scope.editable = $scope.config.editable || false;

        
        var downloadElement = window.document.createElement("a");
        window.document.body.appendChild(downloadElement);
        downloadElement.style.display = "none";
        
        
        $scope.download = function(e) {
        	
        	var fileServiceUri = $scope.filesDomainObject.getFile($scope.filePathDomainObject.getData()).fileUri;
        	var transferInfo = {
        		uri: fileServiceUri
        	};
        	
        	FileService.getFileAsBlob(transferInfo).whenCompleted().then(function(response) {
        		var url = window.URL.createObjectURL(response.data);
        		downloadElement.href = url;
        		downloadElement.download = $scope.filePathDomainObject.getData();
        		downloadElement.click();
                window.URL.revokeObjectURL(url);
        	}, function(error) {
        		console.log(error);
        	});
        }

        $scope.isAllowedMimeType = function(file){
          var accepted = false;
          if (typeof $scope.configuredMimeTypes !== 'undefined'){
            _.each($scope.configuredMimeTypes, function(mimeType){
              mimeType = mimeType.replace("*",".*");
              if (file.type.search(mimeType) != -1){
                accepted = true
              }
            })
          }

          if ($scope.blacklist === true){
            accepted = !accepted;
          }

          return accepted;
        };


        
        $scope.$on('$destroy', function () {
            if ($scope.loadMonitor.inProgress === true && typeof $scope.loadMonitor.session !== 'undefined') {
                try {
                    $scope.loadMonitor.session.abort();
                } catch (e) {
                    // Ignore
                }
            }
            $scope.loadMonitor.finish();
        });

        $scope.dismissError = function(event) {
          if (event && event.stopPropagation) {
              event.stopPropagation();
          }

          if (typeof $scope.error !== 'undefined' && $scope.error.dismissable !== true) {
              $scope.removeOrCancelFile();
          }
          delete $scope.error;
        };

        $scope.removeOrCancelFile = function(event){
          if (event && event.stopPropagation) {
            event.stopPropagation();
          }

          if (typeof $scope.loadMonitor.session !== 'undefined') {
            $scope.loadMonitor.session.abort();
            $scope.loadMonitor.finish();
          } else {
            var filePath = $scope.filePathDomainObject.getData();

            if (typeof filePath !== 'undefined' && filePath !== '') {
              $scope.filesDomainObject.removeFile(filePath);
              $scope.filesDomainObject.changed();

              $scope.filePathDomainObject.setData(undefined);
            }
            $scope.data = undefined;
          }
        };
        
        $scope.upload = function(event) {
        	var audioLayerControl = document.querySelector("#audioLayerControl");
        	var encodedWave = audioLayerControl.toWave().encodeWaveFile();
            var blob = new Blob([encodedWave], {type: "application/octet-stream"});
            blob.name = $scope.data; 
            $scope.uploadAndUseFile(blob);
            $scope.duration.setData(audioLayerControl.duration);
            $scope.duration.changed();
        };
        
        
        
        var recorder;
        $scope.recordInProgress = false;
        $scope.recordStream;
        $scope.record = function(event) {
            try {
                // webkit shim
	        	window.AudioContext = window.AudioContext || window.webkitAudioContext;
	            //window.requestAnimationFrame = window.requestAnimationFrame; //|| window.mozRequestAnimationFrame || window.webkitRequestAnimationFrame || window.msRequestAnimationFrame;
	        	
	            navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia;
	            
	            //window.URL = window.URL || window.webkitURL;
	            var audioContext = new AudioContext;
	            
	            var audioSettings = {
	                "audio": {
	                    "mandatory": {
	                        "googEchoCancellation": "false",
	                        "googAutoGainControl": "false",
	                        "googNoiseSuppression": "false",
	                        "googHighpassFilter": "false"
	                    },
	                    "optional": []
	                },
	            }
	            
	            $scope.recordInProgress = true;
 	            $scope.loadMonitor.begin();
 	            $scope.loadMonitor.recording = true;
 	            $scope.loadMonitor.cancellable = false;
	            
	            navigator.getUserMedia(audioSettings, function(stream) {
	            	console.log('Start');
	            	$scope.recordStream = stream;
	                var input = audioContext.createMediaStreamSource($scope.recordStream);
	                console.log('Media stream created.');
	                // Uncomment if you want the audio to feedback directly
	                //input.connect(audio_context.destination);
	                //__log('Input connected to audio context destination.');
	                
	                recorder = new Recorder(input,{workerPath: $scope.baseUrl + 'lib/recorderWorker.js'});
	                console.log('Recorder initialised.');
	            	
	                recorder && recorder.record();
	                
	                $scope.canvasContext = document.getElementById("recordMeter").getContext("2d");
	                $scope.recordMeter = createAudioMeter(audioContext);
	                input.connect($scope.recordMeter);
	                $scope.drawLoop();
	                
	            }, function(e) {
		            console.log('No live audio input: ' + e);
		            alert('No web audio allowed in this browser!');
		            $scope.recordInProgress = false;
		            $scope.loadMonitor.finish();
	            });
	            
            } catch (e) {
            	console.log(e);
                alert('No web audio support in this browser!');
                $scope.recordInProgress = false;
	            $scope.loadMonitor.finish();
            }
        };
        
        $scope.stopRecord = function(event) {
        	console.log('Stop');
        	$scope.recordInProgress = false;
        	$scope.recordStream.stop();
        	recorder && recorder.stop();
        	
        	window.cancelAnimationFrame = window.cancelAnimationFrame || window.mozCancelAnimationFrame;
        	window.cancelAnimationFrame($scope.drawLoopRequestId);
        	
        	recorder && recorder.exportWAV(function(blob) {
        		blob.name = 'recorded.wav'; 
        		console.log(blob);
                $scope.uploadAndUseFile(blob);
    	    });
        	
        	$scope.loadMonitor.finish();
        	
        };
        
        $scope.drawLoop = function() {
        	// clear the background
        	$scope.canvasContext.clearRect(0, 0, 20, 190);
            // check if we're currently clipping
            if ($scope.recordMeter.checkClipping()) {
            	$scope.canvasContext.fillStyle = "red";
            } else {
            	$scope.canvasContext.fillStyle = "green";
            }
            
            // draw a bar based on the current volume
            $scope.canvasContext.fillRect(0, 190 - 190 * $scope.recordMeter.volume * 2, 20, 190);

            // set up the next visual callback
            $scope.drawLoopRequestId = window.requestAnimationFrame($scope.drawLoop);
           
        };
        
        
        $scope.initAudioLayerControl = function() {
        	var audioLayerControl = document.querySelector("#audioLayerControl");
        	console.log(audioLayerControl.audioPlayback);
        	if (audioLayerControl.audioPlayback === undefined) {
		    	ACInitFFT();
		    	initializeAudioLayerControls();
        	} else {
        		audioLayerControl.removeAllSequenceEditors();
        		$('#app-progress').parent().show();
        	}
        	var audioLayerControl = document.querySelector("#audioLayerControl");
        	console.log($scope.filePathDomainObject.getData());
        	var fileServiceUri = $scope.filesDomainObject.getFile($scope.filePathDomainObject.getData()).fileUri;
        	var transferInfo = {
        		uri: fileServiceUri
        	};
        	console.log("Start read buffer");
        	FileService.getFileAsBlob(transferInfo).whenCompleted().then(function(response) {
        		var fileReader = new FileReader();
        		fileReader.addEventListener("loadend", function(e){
        			console.log("End read buffer");
        			var arrayBuffer = e.target.result;
        			console.log(arrayBuffer);
        			activeAudioLayerControl = audioLayerControl; 
        			activeAudioLayerControl.setLinkMode(true);
        			$('#app-progress')[0].style['width'] = '30%';
        			console.log("Start decode audio");
        			audioLayerControl.audioPlayback.audioContext.decodeAudioData(arrayBuffer, function(audioBuffer){
        				audioLayerControl.decodeAudioFinished(audioBuffer);
        				$scope.duration.setData(audioLayerControl.duration);
           	         	$scope.duration.changed();	
        			}, function(){});
        		});
        		fileReader.readAsArrayBuffer(response.data);
        	}, function(error) {
        		console.log(error);
        	});
        };

        $scope.loadMonitor = {
          progress: 0,
          useProgress: true,
          inProgress: false,
          recording: false,
          cancellable: true,

          begin: function() {
            this.progress = 0;
            this.inProgress = true;
            this.recording = false;
            $scope.showDropHelper = false;
          },

          finish: function() {
            this.progress = 0;
            this.useProgress = true;
            this.inProgress = false;
            this.recording = false;
            this.cancellable = true;
          }
        };
        
        if($scope.data) {
        	$scope.initAudioLayerControl();
        } 

      },

      link: function(scope, element, attrs) {
    	  
        var uploadAndUseFile = function(file) {
          return $q.when().then(function() {
            scope.loadMonitor.begin();
            scope.loadMonitor.cancellable = false;

            var currentUserId = Application.currentUser.userId;
            var uploadURI = atex.onecms.FileServiceUtils.makeTmpSpaceUri(currentUserId, file.name);

            var fileSize = file.size;
            var upload = FileService.putFile({ data: file, uri: uploadURI });

            scope.loadMonitor.session = upload;
            
            

            upload.onProgress = _.throttle(function(result) {
              if (typeof result.lengthComputable !== 'undefined') {
                scope.$apply(function() {
                  scope.loadMonitor.progress = Math.round((result.loaded / fileSize) * 100);
                });
              }
            }, 100);

            scope.loadMonitor.cancellable = true;

            return upload.whenCompleted().then(function(uploadInfo) {

              return $q.when().then(function() {
                delete scope.loadMonitor.session;
                scope.removeOrCancelFile();

                scope.filesDomainObject.addFile(file.name, {
                  _type: 'com.atex.onecms.content.ContentFileInfo',

                  filePath: file.name,
                  fileUri: uploadInfo.data.URI
                });
                scope.data = file.name;
                scope.filePathDomainObject.setData(file.name);

                scope.filePathDomainObject.changed();
                scope.filesDomainObject.changed();
                scope.loadMonitor.finish();
                scope.initAudioLayerControl();
              });
            });
          }).then(undefined, function(error) {
            console.log('Error while uploading file to the file service!', error);

            scope.error = {message: error.cause, dismissable: true};
            scope.loadMonitor.finish();
            delete scope.loadMonitor.session;

          });
        };
        scope.uploadAndUseFile = uploadAndUseFile;
        
        element.find('.file-uploader-spinner').prepend(createSpinner().el);

        // Event handlers!

        element.find('.upload-area').on('click', '.browse-button', function(event) {
            element.find('.file-browse').click();
        });

        element.find('.file-browse').bind('change', function(event) {
          var file = event.originalEvent.target.files[0];


          if (scope.isAllowedMimeType(file) || typeof scope.configuredMimeTypes === 'undefined'){
            scope.error = undefined;
            uploadAndUseFile(file);
          } else {
            scope.$apply(function(){
                scope.error = { message: "Unsupported filetype.", dismissable: true };
            });
          }

          //Clear filebrowser value
          var fileBrowser = element.find('.file-browse');
          fileBrowser.replaceWith( fileBrowser = fileBrowser.clone( true ) );
        });

        var initializeDragDrop = function() {
          var eventCache = [];

          //Drag-drop event handlers...

          element.on('dragenter', function(event) {
            event.preventDefault();

            if (eventCache.length === 0 && typeof scope.error === 'undefined') {
              scope.$apply(function() {
                scope.showDropHelper = true;
              });
            }

            if (eventCache.indexOf(event.target) === -1) {
              eventCache.push(event.target);
            }

            return false;
          });

          element.on('dragleave', function(event) {
            var index = eventCache.indexOf(event.target);

            if (index !== -1) {
              eventCache.splice(index, 1);
            }

            if (eventCache.length === 0) {
              scope.$apply(function() {
                scope.showDropHelper = false;
              });
            }
          });

          element.on('dragover', function(event) {

            event.preventDefault();

            return false;
          });

          element.on('drop', function(event) {
            var droppedFiles = event.originalEvent.dataTransfer.files;

            if (droppedFiles.length === 1) {
              var file = droppedFiles[0];

              if (scope.isAllowedMimeType(file) || typeof scope.configuredMimeTypes === 'undefined'){
                scope.error = undefined;
                uploadAndUseFile(file);
              } else {
                scope.$apply(function(){
                    scope.showDropHelper = false;
                    scope.error = { message: "Unsupported filetype.", dismissable: true };
                });
              }
            }
            else if (droppedFiles.length === 0) {
                scope.$apply(function(){
                    scope.showDropHelper = false;
                    scope.error = { message: "You may only drop files here.", dismissable: true };
                });
            }
            else {
                scope.$apply(function(){
                    scope.showDropHelper = false;
                    scope.error = { message: "You may only add one file at a time.", dismissable: true };
                });
            }
            eventCache = [];
            event.preventDefault();
          });
        };
        if(scope.editable) {
        	initializeDragDrop();
        }
      }
    };
  }];
});
