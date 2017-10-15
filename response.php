<?php
session_start();
//====================================================
// Uploading a recorded video or audio file to server
//----------------------------------------------------
if ($_GET['action'] == 'upload') {
  if (!is_dir("uploads/")) mkdir("uploads/");
  if (!is_dir("data/")) mkdir("data/");
  if (isset($_FILES["blob"])) {
    $tempName = $_FILES["blob"]["tmp_name"];
    $destination = "uploads/".$_FILES["blob"]["name"];
    file_put_contents($destination, file_get_contents($tempName,'r'),FILE_APPEND);
  }
}
//====================================================
// Upload the transcript
//----------------------------------------------------
else if ($_GET['action'] == 'uploadtranscriptandvideotimes') {
  $transcript = $_POST['transcript'];
  //$videoTimesMillisecs = $_POST['videoTimesMillisecs'];
  $dataKey = $_GET['dataKey'];
  
  echo "original transcript: ".$transcript."<br><br>";
  if (strlen($transcript) > 0) {
	$transcriptFilename = "data/transcript-google-$dataKey.txt";
    $transcriptFile = fopen($transcriptFilename,"w");
    fwrite($transcriptFile, $transcript);
    fclose($transcriptFile);
  }
}
//====================================================
// Checking if transcript alignment files exist
//----------------------------------------------------
else if ($_GET['action'] == 'checkgettranscriptvideomillis') {
  $dataKey = $_GET["dataKey"];
  $transcriptFilename = "data/transcript-google-$dataKey.txt";
  if (file_exists($transcriptFilename)) echo "true";
  else echo "false";
}
//====================================================
// Processing the files
//----------------------------------------------------
else if ($_GET['action'] == 'process') {
  $dataKey = $_GET["dataKey"];
  $extension = $_GET["extension"];
  echo $dataKey;
  
  $audioVideoDir = "C:/inetpub/wwwroot/ToastMasterClass/uploads/";
  $exeDir = "C:/inetpub/wwwroot/NonverbalAnalysis/AVProcessor_2015_mar/AVProcessor/bin/Release/";
  
  if ($extension == "mp4") {
    $output = shell_exec("cd $exeDir & AVProcessor.exe $audioVideoDir$dataKey.mp4 2>&1");
  } else {
    $output = shell_exec("cd $exeDir & AVProcessor.exe $audioVideoDir$dataKey.wav $audioVideoDir$dataKey.webm 2>&1");
  }

  echo "\n<br>cd $exeDir & AVProcessor.exe $audioVideoDir$dataKey.wav $audioVideoDir$dataKey.webm 2>&1<br>\n";
  var_dump($output);
}
//====================================================
// Processing the files 2
//----------------------------------------------------
else if ($_GET['action'] == 'process2') {
  $dataKey = $_GET["dataKey"];
  echo $dataKey;
  
  $audioVideoDir = "C:/inetpub/wwwroot/ToastMasterClass/";  
  $output = exec("cd C:/inetpub/wwwroot/ToastMasterClass/ & praatcon.exe {$audioVideoDir}praatscript {$audioVideoDir}uploads/ $dataKey.wav {$audioVideoDir}data/ wav 10 75 600 11025 2>&1");
  //$output = shell_exec("dasd");

  echo "\nrunning praat script\n";
  var_dump($output);
}
//====================================================
// Processing the files 3
//----------------------------------------------------
else if ($_GET['action'] == 'process3') {
  $dataKey = $_GET["dataKey"];
  echo $dataKey;
  
  $output = exec("cd C:/inetpub/wwwroot/ToastMasterClass/ & java -Xmx1g KNN $dataKey > data/{$dataKey}_comments.txt 2>&1");
  exec("cd C:/inetpub/wwwroot/ToastMasterClass/ & java -Xmx2g KNN2 $dataKey > data/{$dataKey}_comments.txt 2>&1");
  //$output = shell_exec("dasd");

  echo "\nrunning KNN\n";
  var_dump($output);
}
//====================================================
// Checking if formatted data files exist
//----------------------------------------------------
else if ($_GET['action'] == 'checkformatteddata') {
  $dataKey = $_GET["dataKey"];
  $averageFeaturesFile = "data/average-features-$dataKey.js";
  $audioVideoFeaturesFile = "data/audio-video-features-$dataKey.js";
  $voicedAudioIntervalFeatureFile = "data/voiced-audio-interval-features-$dataKey.js";

  if (file_exists($audioVideoFeaturesFile) and file_exists($averageFeaturesFile)) {
    echo "true";
    if ($_GET["private"] == "true") {
	    $_SESSION["$dataKey-averageAudioVideoFeatureData"] = file_get_contents($averageFeaturesFile);
	    $_SESSION["$dataKey-serialAudioVideoFeatureData"] = file_get_contents($audioVideoFeaturesFile);

      if (!is_dir("temp_uploads/")) mkdir("temp_uploads/");
      rename("uploads/$dataKey-merge.webm","temp_uploads/$dataKey-merge.webm");
      $_SESSION["$dataKey-mergedVideoFilepath"] = "temp_uploads/$dataKey-merge.webm";
      
	    unlink($audioVideoFeaturesFile);
	    unlink($averageFeaturesFile);
      unlink("uploads/$dataKey.webm");
      unlink("uploads/$dataKey.wav");
    }
  } else {
    $analysisProgressFile = "D:/analysis-progress-$dataKey.txt";
    if (file_exists($analysisProgressFile)) echo file_get_contents($analysisProgressFile);
    else echo "Continuing to process data...";
  }
}
?>