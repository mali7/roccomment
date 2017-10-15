<?php
session_start();
if ($_GET['action'] == 'process') {
  $dataKey = $_GET["dataKey"];
  $extension = "mp4";
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
  
  $audioVideoDir = "C:/inetpub/wwwroot/ToastMasterClass/";  
  $output = exec("cd C:/inetpub/wwwroot/ToastMasterClass/ & praatcon.exe {$audioVideoDir}praatscript {$audioVideoDir}uploads/ $dataKey.wav {$audioVideoDir}data/ wav 10 75 600 11025 2>&1");
  //$output = shell_exec("dasd");

  echo "\nrunning praat script\n";
  var_dump($output);

  echo $dataKey;
  
  $output = exec("cd C:/inetpub/wwwroot/ToastMasterClass/ & java -Xmx2g KNN2 $dataKey > data/{$dataKey}_comments.txt 2>&1");

  echo "\nrunning KNN\n";
  var_dump($output);
  
}
else if ($_GET['action'] == 'checkstatus') {
	$status = 0;
	
	if (file_exists("C:/inetpub/wwwroot/ToastMasterClass/data/{$dataKey}_comments.txt")) $status = 30;
	if (file_exists("C:/inetpub/wwwroot/ToastMasterClass/final_output/{$dataKey}_best_phrases.txt")) $status = $status + 30;
	if (file_exists("C:/inetpub/wwwroot/ToastMasterClass/final_output/{$dataKey}_dbscan.txt")) $status = $status + 40;
	echo $status;
}
?>