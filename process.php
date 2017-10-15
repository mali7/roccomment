<?php
session_start();
ini_set('max_execution_time', 3000);
if ($_GET['action'] == 'process') {
  
  $dataKey = $_GET["dataKey"];
  $extension = "mp4";
  echo "Processing $dataKey<br>";
  
  $audioVideoDir = "C:/inetpub/wwwroot/ToastMasterClass/uploads/";
  $exeDir = "C:/inetpub/wwwroot/NonverbalAnalysis/AVProcessor_2015_mar/AVProcessor/bin/Release/";
  
  $mp4Exists = file_exists("C:/inetpub/wwwroot/ToastMasterClass/uploads/{$dataKey}.mp4");
  $wavAndWebmExist = file_exists("C:/inetpub/wwwroot/ToastMasterClass/uploads/{$dataKey}.wav") && file_exists("C:/inetpub/wwwroot/ToastMasterClass/uploads/{$dataKey}.webm");
  if(!$mp4Exists && !$wavAndWebmExist) {
	sleep(10);
	$mp4Exists = file_exists("C:/inetpub/wwwroot/ToastMasterClass/uploads/{$dataKey}.mp4");
	$wavAndWebmExist = file_exists("C:/inetpub/wwwroot/ToastMasterClass/uploads/{$dataKey}.wav") && file_exists("C:/inetpub/wwwroot/ToastMasterClass/uploads/{$dataKey}.webm");
  }
  if(!$mp4Exists && !$wavAndWebmExist) {
	  echo "upload_error";
	  return;
  }
  
  $tryCount = 0;
  $stepCompleted = false;
  while(!$stepCompleted && $tryCount<2){
	try {
      if ($mp4Exists) {
        $output = shell_exec("cd $exeDir & AVProcessor.exe $audioVideoDir$dataKey.mp4 2>&1");
      } else {
		$output = shell_exec("cd $exeDir & AVProcessor.exe $audioVideoDir$dataKey.wav $audioVideoDir$dataKey.webm 2>&1");
	  }
	  $stepCompleted = true;
	  
	} catch (Exception $e) {
      $tryCount = $tryCount + 1;
	  echo "AVPROCESSOR EX\n";
	  echo $e;
	}
  }

  echo "\n<br>cd $exeDir & AVProcessor.exe $audioVideoDir$dataKey.wav $audioVideoDir$dataKey.webm 2>&1<br>\n";
  
  $audioVideoDir = "C:/inetpub/wwwroot/ToastMasterClass/";  
  
  $tryCount = 0;
  $stepCompleted = false;
  
  
  echo "\nSTART PRAAT\n";
  while(!$stepCompleted && $tryCount<2){
	echo "\nSTART PRAAT2\n";
	try {
	  $output = exec("cd C:/inetpub/wwwroot/ToastMasterClass/ & praatcon.exe {$audioVideoDir}praatscript {$audioVideoDir}uploads/ $dataKey.wav {$audioVideoDir}data/ wav 10 75 600 11025 2>&1");
	  $stepCompleted = true;
	} catch (Exception $e) {
      $tryCount = $tryCount + 1;
	  echo "praat EX\n";
	  echo $e;
	}
  }
  
  //$output = shell_exec("dasd");

  echo "\nrunning praat script\n";

  echo $dataKey;
  $tryCount = 0;
  $stepCompleted = false;
  while(!$stepCompleted && $tryCount<2){
	try {
	  echo "START KNN";
	  $output = exec("cd C:/inetpub/wwwroot/ToastMasterClass/ & java -Xmx2g KNN2 $dataKey > C:/inetpub/wwwroot/ToastMasterClass/data/{$dataKey}_comments.txt 2>&1");
	  $stepCompleted = true;
	} catch (Exception $e) {
	  $tryCount = $tryCount + 1;
	  echo "KNN EX\n<br>";
	  echo $e;
	}
  }
  
  exec("python posprocessing/dbscan.py {$dataKey}");

  echo "\nrunning DBSCAN\n";
  
}
else if ($_GET['action'] == 'checkstatus') {
	$status = 20;
	$dataKey = $_GET["dataKey"];
	if (file_exists("C:/inetpub/wwwroot/ToastMasterClass/data/{$dataKey}_comments.txt")) $status = $status + 30;
	if (file_exists("C:/inetpub/wwwroot/ToastMasterClass/final_output/{$dataKey}_dbscan.txt")) $status = $status + 50;
	echo $status;
}
?>