import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.util.*;

class VideoInfo{
    public String Datakey;
    ArrayList<Double> time =  new ArrayList<Double>();
    ArrayList<Double> pitch =  new ArrayList<Double>();
    ArrayList<Double> intensity =  new ArrayList<Double>();
    ArrayList<Double> formant1 =  new ArrayList<Double>();
    ArrayList<Double> formant2 =  new ArrayList<Double>();
    ArrayList<Double> formant3 =   new ArrayList<Double>();
    ArrayList<Double> band1 =  new ArrayList<Double>();
    ArrayList<Double> band2 =  new ArrayList<Double>();
    ArrayList<Double> band3 =  new ArrayList<Double>();
    ArrayList<Double> soundIntensity_DB =  new ArrayList<Double>();
    ArrayList<Double> smile_cubicSpline =  new ArrayList<Double>();
    ArrayList<Double> movement_cubicSpline =  new ArrayList<Double>();
    ArrayList<Double> pitch_Hz =  new ArrayList<Double>();
    
    
    public void VideoInfo(){
        Datakey = "";

    }
    public void setValues(String datakey, double t, double p, double i, double f1, double f2, double f3, double b1, double b2, double b3){
        this.Datakey = datakey;
        time.add(t);
        pitch.add(p);
        intensity.add(i);
        formant1.add(f1);
        formant2.add(f2);
        formant3.add(f3);
        band1.add(b1);
        band2.add(b2);
        band3.add(b3);
    }
    public void setValueFromSmile( double sound , double pitch, double smile, double move){
        soundIntensity_DB.add(sound);
        pitch_Hz.add(pitch);
        smile_cubicSpline.add(smile);
        movement_cubicSpline.add(move);
    }
    public void addTime(double t){
        time.add(t);
    }
}

public class KNN{
    public static void main(String args[]) throws IOException{
        ArrayList <VideoInfo> data = new ArrayList<VideoInfo>();
        ArrayList <String> content = new ArrayList<String>();
        ArrayList <String> CommentDatakey = new ArrayList<String>();
        ArrayList <String> category = new ArrayList<String>();
        ArrayList <Double> time = new ArrayList<Double>();
        ArrayList <String> label = new ArrayList<String>();
        ArrayList <String> predictedComment = new ArrayList<String>();
		ArrayList <String> predictedCategory = new ArrayList<String>();
		ArrayList <String> predictedLabel = new ArrayList<String>();
        ArrayList <Double> predictionDiff = new ArrayList<Double>();
        ArrayList <Double> predictionTime = new ArrayList<Double>();
        //VideoInfo data [] = new VideoInfo[10];
        String FileNames[] = new String[200];
        String currentDir = System.getProperty("user.dir");
        File folder = new File(currentDir+"\\features_temporal");
        File[] listOfFiles = folder.listFiles();
        int i = 0;
        int numOfFiles = 0;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                FileReader fr = null;
                try {
                    FileNames[i] = listOfFile.getName();
                    i++;
                    fr = new FileReader(currentDir+"\\features_temporal\\"+FileNames[i-1]);
                    BufferedReader br = new BufferedReader(fr);
                    //System.out.println("File " +i+" "+ FileNames[i-1] );
                    VideoInfo vi = new VideoInfo();
                    String s = br.readLine(); 
                    s = br.readLine(); 
                    while(s != null ){
                        if ( s.contains("?")){
                            break;
                        }
                        //System.out.println(s);
                        StringTokenizer st = new StringTokenizer(s);
                        String datakey = FileNames[i-1];
                        //System.out.println(st.nextToken());

                        double t = Double.parseDouble(st.nextToken()) ;
                        double p = Double.parseDouble(st.nextToken()) ;
                        double intensity = Double.parseDouble(st.nextToken()) ;
                        double f1 = Double.parseDouble(st.nextToken()) ;
                        double f2 = Double.parseDouble(st.nextToken()) ;
                        double f3 = Double.parseDouble(st.nextToken()) ;
                        double b1 = Double.parseDouble(st.nextToken()) ;
                        double b2 = Double.parseDouble(st.nextToken()) ;
                        double b3 = Double.parseDouble(st.nextToken()) ;
                        //System.out.println(t+" "+ p +" "+b3 );
                        vi.setValues(datakey, t, p, intensity, f1, f2, f3, b1, b2, b3);
                        s = br.readLine();
                        //break;
                    }
                    data.add(vi);
                    
                    //break;
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(KNN.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        fr.close();
                    } catch (IOException ex) {
                        Logger.getLogger(KNN.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else if (listOfFile.isDirectory()) {
                System.out.println("Directory " + listOfFile.getName());
            }
    
            
        }
        //System.out.println(data.size());
        numOfFiles = i-1;
        
        //System.out.println("Loading From Smile Folder...");
        FileNames = new String[200];
        folder = new File(currentDir+"\\smile");
        listOfFiles = folder.listFiles();
        i = 0;
        numOfFiles = 0;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                FileReader fr = null;
                try {
                    FileNames[i] = listOfFile.getName();
                    i++;
                    fr = new FileReader(currentDir+"\\smile\\"+FileNames[i-1]);
                    BufferedReader br = new BufferedReader(fr);
                    //System.out.println("File " +i+" "+ FileNames[i-1]);
                    VideoInfo vi = new VideoInfo();
                    for (int j = 0; j<data.size();j++){
                        vi = data.get(j);
                        if(FileNames[i-1].contains(vi.Datakey)){
                            //System.out.println("match "+j);
                            String s = br.readLine();
                            //System.out.println(s);
                            StringTokenizer st = new StringTokenizer(s,":,[{]}");
                            int k = 0;
                            double sound = 0,pitch = 0,smile = 0,movement = 0;
                            while(st.hasMoreTokens()){
                                String Token = st.nextToken();
                                if(Token.matches("[0-9.]+")){
                                    //k++;
                                    if(k==0){
                                        k++;
                                    }
                                    else if(k==1){
                                        k++;
                                        sound = Double.parseDouble(Token);
                                    }
                                    else if(k==2){
                                        k++;
                                        pitch = Double.parseDouble(Token);
                                        
                                    }
                                    else if(k==3){
                                        k++;
                                        smile = Double.parseDouble(Token);
                                        
                                    }
                                    else if(k==4){
                                        k=0;
                                        movement = Double.parseDouble(Token);
                                        //System.out.println(sound+" "+movement+" "+pitch+" "+smile);
                                        vi.setValueFromSmile(sound, pitch, smile, movement);
                                    }
                                }
                                    //System.out.println(Token);
                            }
                            //break;
                            data.remove(j);
                            data.add(j, vi);
                        }
                    }
                       
                    //break;
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(KNN.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        fr.close();
                    } catch (IOException ex) {
                        Logger.getLogger(KNN.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else if (listOfFile.isDirectory()) {
                System.out.println("Directory " + listOfFile.getName());
            }
        }
        VideoInfo vi = new VideoInfo();
        /*
        vi = data.get(20);
        System.out.println("Name="+vi.Datakey);
        
        for( i=0;i<vi.time.size();i++){
            System.out.println("Time = "+vi.time.get(i)+"Intensity ="+vi.soundIntensity_DB.get(i)+" Movement = "+vi.movement_cubicSpline.get(i)+"format = "+vi.formant1.get(i));
        }
                */
        //System.out.println("Loading Comments...");
        FileReader fr = new FileReader(currentDir+"\\labeled_comments.csv");
        BufferedReader br = new BufferedReader(fr);
        br.readLine();
        String s =  br.readLine();
        while(s  != null) {
            StringTokenizer st = new StringTokenizer(s,",");
            
            int x = st.countTokens();
            boolean isID=false;
            String token = "";
            if (x>0){
                token = st.nextToken();
                //System.out.print(token);
                if(token.matches("[0-9]+")){
                    isID=true;
                }
            }

            if(x>=5 && isID == true){
                token = st.nextToken();
                //System.out.print(token);
                CommentDatakey.add(token);
                token = st.nextToken();
                //System.out.println(token);
                category.add(token);

                token = st.nextToken();
                time.add(Double.parseDouble(token));
                if(x >=6){
                    token = st.nextToken();
                    label.add(token);
                }
                else label.add("empty");
                token="";
                while(st.hasMoreTokens()){
                    token += st.nextToken();
                }
                content.add(token);
            }
            s =  br.readLine();
        }
        
        /////////////////////////////////////////////////TEST/////////////////////////////////////
        
        fr = new FileReader(currentDir+"\\data\\"+args[0]);
        br = new BufferedReader(fr);
        VideoInfo testVideoInfo = new VideoInfo();
        s = br.readLine();
        s = br.readLine();
        while(s != null ){
            if ( s.contains("?")){
                break;
            }
            //System.out.println(s);
            StringTokenizer st = new StringTokenizer(s);
            String datakey = FileNames[i-1];
            //System.out.println(st.nextToken());

            double t = Double.parseDouble(st.nextToken()) ;
            double p = Double.parseDouble(st.nextToken()) ;
            double intensity = Double.parseDouble(st.nextToken()) ;
            double f1 = Double.parseDouble(st.nextToken()) ;
            double f2 = Double.parseDouble(st.nextToken()) ;
            double f3 = Double.parseDouble(st.nextToken()) ;
            double b1 = Double.parseDouble(st.nextToken()) ;
            double b2 = Double.parseDouble(st.nextToken()) ;
            double b3 = Double.parseDouble(st.nextToken()) ;
            //System.out.println(t+" "+ p +" "+b3 );
            testVideoInfo.setValues(datakey, t, p, intensity, f1, f2, f3, b1, b2, b3);
            s = br.readLine();
            //break;
        }
        
        ///////////////////////////////////////
        
        currentDir = System.getProperty("user.dir");
        fr = new FileReader(currentDir+"\\data\\"+"audio-video-features-"+args[0]+".js");
        br = new BufferedReader(fr);
        //String test = br.readLine();
        //VideoInfo testVideoInfo = new VideoInfo();
        s = br.readLine();
        StringTokenizer st = new StringTokenizer(s,":,[{]}");
        int k = 0;
        double t =0 ;
        double pit =0 ;
        double intensity =0 ;
        double f1 =0 ;
        double f2 =0 ;
        double f3 =0 ;
        double b1 =0 ;
        double b2 =0 ;
        double b3 =0 ;
        double sound = 0,pitch = 0,smile = 0,movement = 0, x=10;
        while(st.hasMoreTokens()){
            String Token = st.nextToken();
            if(Token.matches("[0-9.]+")){
                //k++;
                if(k==0){
                    k++;
                }
                else if(k==1){
                    k++;
                    sound = Double.parseDouble(Token);
                }
                else if(k==2){
                    k++;
                    pitch = Double.parseDouble(Token);

                }
                else if(k==3){
                    k++;
                    smile = Double.parseDouble(Token);

                }
                else if(k==4){
                    k=0;
                    movement = Double.parseDouble(Token);
                    //System.out.println(x+" "+sound+" "+movement+" "+pitch+" "+smile);
                    testVideoInfo.setValueFromSmile(sound, pitch, smile, movement);
                    //testVideoInfo.addTime(x);
                    x+=10;
                }
            }
        }
        //System.out.print(testVideoInfo.time.size());
        //System.out.print(testVideoInfo.soundIntensity_DB.size());
        String NearDataKey[] = new String[10];
        double NearTime[] = new double[10];
        double NearDist[] = new double [10];
        for(i=0;i<10;i++){
            NearDist[i]=Double.MAX_VALUE;
        }
        for(i = 0 ; i< (testVideoInfo.time.size())-100 ; i+=100){
            sound = 0;
            pitch = 0;
            smile = 0;
            movement = 0;
            k=i;
            while(k<i+100){
                sound += testVideoInfo.soundIntensity_DB.get(k);
                pitch += testVideoInfo.pitch_Hz.get(k);
                smile += testVideoInfo.smile_cubicSpline.get(k);
                movement += testVideoInfo.movement_cubicSpline.get(k);
                
                pit += testVideoInfo.pitch.get(k)  ;
                intensity += testVideoInfo.intensity.get(k)  ;
                f1 += testVideoInfo.formant1.get(k)  ;
                f2 += testVideoInfo.formant2.get(k)  ;
                f3 += testVideoInfo.formant3.get(k)  ;
                b1 += testVideoInfo.band1.get(k)  ;
                b2 += testVideoInfo.band2.get(k)  ;
                b3 += testVideoInfo.band3.get(k)  ;
                k++;
            }
            sound = sound/100;
            pitch = pitch/100;
            smile = smile/100;
            movement = movement/100;
            pit = pit /100;
            intensity = intensity/100;
            f1 = f1/100;
            f2 = f2/100;
            f3 = f3/100;
            b1 = b1/100;
            b2 = b2/100;
            b3 = b3/100;
            
            int j,l,m,temp;
            double dist=0;
            //System.out.println("Sound "+sound+" pitch "+pitch+" smile "+smile);
            for(j=0;j<data.size();j++){
                vi = data.get(j);
                //System.out.println(vi.time.size()+" "+vi.soundIntensity_DB.size()+" "+vi.pitch_Hz.size());
                for(l = 0;l<vi.soundIntensity_DB.size()-1;l++){
                    dist = 0;
                    dist += Math.abs(vi.soundIntensity_DB.get(l) - sound);
                    dist += Math.abs(vi.pitch_Hz.get(l) - pitch);
                    dist += Math.abs(vi.smile_cubicSpline.get(l) - smile);
                    dist += Math.abs(vi.movement_cubicSpline.get(l) - movement);
                    dist += Math.abs(vi.pitch.get(l) - pit);
                    dist += Math.abs(vi.formant1.get(l) - f1);
                    dist += Math.abs(vi.formant2.get(l) - f2);
                    dist += Math.abs(vi.formant3.get(l) - f3);
                    dist += Math.abs(vi.band1.get(l) - b1);
                    dist += Math.abs(vi.band2.get(l) - b2);
                    dist += Math.abs(vi.band3.get(l) - b3);
                    //System.out.println("Dist = "+dist);
                    for(m=0;m<10;m++){
                        if(NearDist[m]>dist && dist>0.0){
                            //System.out.println("Dist = "+dist+" file="+vi.Datakey+" time = "+vi.time.get(l));
                            break;
                        }
                    }
                    temp=m;
                    if(m<10){
                        int p=0;
                        for(p=9;p>m;p--){
                            NearDataKey[p] = NearDataKey[p-1];
                            NearDist[p] = NearDist[p-1];
                            NearTime[p] = NearTime[p-1];
                        }
                        /*
                        for(;m<9;m++){
                            NearDataKey[m+1] = NearDataKey[m];
                            NearDist[m+1] = NearDist[m];
                            NearTime[m+1] = NearTime[m];
                        }
                                */
                        NearDataKey[temp] = vi.Datakey;
                        NearDist[temp] = dist;
                        NearTime[temp] = vi.time.get(l);
                    }
                }
            }
            //System.out.println(".....Getting comments...."+ i);
            int q;
            int p;
            for(p=0;p<10;p++){
                double Dist = Double.MAX_VALUE;
                int index=0;
                //System.out.println(NearDataKey[p]);
                for(q=0;q<CommentDatakey.size();q++){
                    if(NearDataKey[p].contains(CommentDatakey.get(q))){
                        //System.out.println(content.get(j));
                        if(Math.abs(time.get(q)-(NearTime[p]*1000))<Dist){
                            Dist = Math.abs(time.get(q)-(NearTime[p]*1000));
                            index = q;
                        }
                    }
                }
                boolean flag = true;
                if(predictedComment.contains(content.get(index))){
                    s = content.get(index);
                    int commentIndex = predictedComment.indexOf(s);
                    if (predictionTime.get(commentIndex) >= Math.abs(time.get(index)-(NearTime[p]*1000))){
                        flag = true;
                        predictedComment.remove(commentIndex);
                        predictionDiff.remove(commentIndex);
                        predictionTime.remove(commentIndex);
						predictedLabel.remove(commentIndex);
						predictedCategory.remove(commentIndex);
                        //System.out.println("..............updating...............");
                    }
                    else flag = false;
                }
                if(Math.abs(time.get(index)-(NearTime[p]*1000))<7000 && flag && label.get(index).contains("empty")== false){
                    s = content.get(index);
                    /*
                    
                    */
                    predictedComment.add(s);
					predictedLabel.add(label.get(index));
					predictedCategory.add(category.get(index));
                    double timeDiff = Math.abs(time.get(index)-(NearTime[p]*1000));
                    predictionDiff.add(timeDiff);
                    predictionTime.add(((double)i)/1000);
                }
                //System.out.println(content.get(index)+" "+NearTime[p]);
            }
            for(p=0;p<10;p++){
                NearDist[p]=Double.MAX_VALUE;
            }
            
        }
        
        //System.out.println("??????????????????????Final Comments????????????????");
        for(i=0;i<predictedComment.size();i++){
            //System.out.println(predictionTime.get(i)+" "+predictedComment.get(i));
            s = predictedComment.get(i);
            if(s.contains(" he ")){
                s = s.replace(" he ", " he/she ");
            }
            else if(s.contains(" she ")){
                s = s.replace(" he ", " he/she ");
            }
            else if(s.contains("He ")){
                s = s.replace("He ", "He/She ");
            }
            else if(s.contains("She ")){
                s = s.replace("He ", "He/She ");
            }
            if(s.contains("His ")){
                s = s.replace("His ", "His/Her ");
            }
            else if(s.contains("Her ")){
                s = s.replace("Her ", "His/Her ");
            }
            else if(s.contains(" his ")){
                s = s.replace(" his ", " his/her ");
            }
            else if(s.contains(" her ")){
                s = s.replace(" her ", " his/her ");
            }
            System.out.println(predictionTime.get(i)+";"+predictedLabel.get(i)+";"+predictedCategory.get(i)+";"+s);
        }
    } 
}