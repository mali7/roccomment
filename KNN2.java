import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.util.*;

class CommentInfo{
    public String Datakey;
    public String Comment;
    public String label;
    public String parseTree;
    public String category;
	
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
        Comment = "";
        label = "";
        category = "";
        parseTree = "";
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
    public void addAllValues(double t,double b1, double b2, double b3,double f1, double f2, double f3,  double i, double move, double p, double pitch_, double smile, double sound){
        time.add(t);
        pitch.add(p);
        intensity.add(i);
        formant1.add(f1);
        formant2.add(f2);
        formant3.add(f3);
        band1.add(b1);
        band2.add(b2);
        band3.add(b3);
        soundIntensity_DB.add(sound);
        pitch_Hz.add(pitch_);
        smile_cubicSpline.add(smile);
        movement_cubicSpline.add(move);
    }
    public void setDataKey(String datakey){
        this.Datakey = datakey;
    }
    public void setContent(String s){
        this.Comment = s;
    }
    public void setParseTree(String datakey, String parseTree, String label, String category){
        this.Datakey = datakey;
        this.parseTree = parseTree;
        this.label = label;
        this.category = category;
    }
   
}

public class KNN2{
	
	private static int KNN_LEVEL = 10;
	
    public static void main(String args[]){
		if(args.length>1) {
			KNN_LEVEL = Integer.parseInt(args[1]);
		}

        ArrayList <CommentInfo> data = new ArrayList<CommentInfo>();
        String FileNames[] = new String[200];
        int i=0;
        String NearDataKey[] = new String[KNN_LEVEL];
        String NearTree[] = new String[KNN_LEVEL];
        String NearComment[] = new String[KNN_LEVEL];
        double NearTime[] = new double[KNN_LEVEL];
        double NearDist[] = new double [KNN_LEVEL];
        String NearCategory[] = new String[KNN_LEVEL];
        String NearLabel[] = new String[KNN_LEVEL];
        ArrayList <String> predictedComment = new ArrayList<String>();
        ArrayList <String> predictedCategory = new ArrayList<String>();
        ArrayList <String> predictedlabel = new ArrayList<String>();
        ArrayList <Double> predictionDiff = new ArrayList<Double>();
        ArrayList <Double> predictionTime = new ArrayList<Double>();
        ArrayList <String> predictionTree = new ArrayList<String>();
        //String currentDir = System.getProperty("user.dir");
		//TODO:  FOR DEPLOY
		String currentDir = "C:\\inetpub\\wwwroot\\ToastMasterClass";
        //Load pre-parsed info
        try {
            CommentInfo ci = new CommentInfo(); 
            File f = new File(currentDir+"\\commentData.txt");
            FileReader fr = new FileReader(currentDir+"\\commentData.txt");
            BufferedReader br = new BufferedReader(fr);
            String s ;
            String line = br.readLine();
            
            while(line != null){
                i++;
                StringTokenizer st = new StringTokenizer(line,",");
                
                //Extract header of list "datakey,parseTree,label,category"
                if(st.countTokens()>=3 && st.countTokens()<=4){
                    data.add(ci);
                    ci = new CommentInfo();
                    String dataKey = st.nextToken();
                    String parseTree = st.nextToken();
                    String label = st.nextToken();
                    String category = st.nextToken();
                    ci.setParseTree(dataKey, parseTree, label, category);
                }
                else if(st.countTokens()==13){
                    //Extract data from each moment
                    //"time,band1,band2,band3,formant1,formant2,formant3,intensity,movement,pitch,pitchHz,smile,sound"
                    double time = Double.parseDouble(st.nextToken()) ;
                    double band1 = Double.parseDouble(st.nextToken()) ;
                    double band2 = Double.parseDouble(st.nextToken()) ;
                    double band3 = Double.parseDouble(st.nextToken()) ;
                    double formant1 = Double.parseDouble(st.nextToken()) ;
                    double formant2 = Double.parseDouble(st.nextToken()) ;
                    double formant3 = Double.parseDouble(st.nextToken()) ;
                    double intensity = Double.parseDouble(st.nextToken()) ;
                    double movement = Double.parseDouble(st.nextToken()) ;
                    double pitch = Double.parseDouble(st.nextToken()) ;
                    double pitchHz = Double.parseDouble(st.nextToken()) ;
                    double smile = Double.parseDouble(st.nextToken()) ;
                    double sound = Double.parseDouble(st.nextToken()) ;
                    ci.addAllValues(time, band1, band2, band3, formant1, formant2, formant3, intensity, movement, pitch, pitchHz, smile, sound);
                }
                else{
                    ci.setContent(st.nextToken());
                }
                line = br.readLine();
            }
            
            ///////////////Load Test Files///////////////////////
            //System.out.println(data.size()+" "+data.get(1).band1.size());


            fr = new FileReader(currentDir+"\\data\\"+args[0]);
            br = new BufferedReader(fr);
            CommentInfo testVideoInfo = new CommentInfo();
            s = br.readLine();
            s = br.readLine(); 
            while(s != null ){
                if ( s.contains("?")){
                    break;
                }
                //System.out.println(s);
                StringTokenizer st = new StringTokenizer(s);
                String datakey = "Test";
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
            fr = new FileReader(currentDir+"\\data\\audio-video-features-"+args[0]+".js");
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
            //////////////////////finding KNN////////////////////////////
            for(i=0;i<KNN_LEVEL;i++){
                NearDist[i]=Double.MAX_VALUE;
            }
            CommentInfo vi = new CommentInfo();
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
                for(j=1;j<data.size();j++){
                    
                    vi = data.get(j);
                    if (vi.label.contains("empty")==false){
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

                            for(m=0;m<KNN_LEVEL;m++){
                                if(NearDist[m]>dist && dist>0.0){
                                    //System.out.println("Dist = "+dist+" file="+vi.Datakey+" time = "+vi.time.get(l));
                                    break;
                                }
                            }
                            temp=m;
                            if(m<KNN_LEVEL){
                                int p=0;

                                for(p=9;p>m;p--){
                                    NearDataKey[p] = NearDataKey[p-1];
                                    NearDist[p] = NearDist[p-1];
                                    NearTime[p] = NearTime[p-1];
                                    NearTree[p] = NearTree[p-1];
                                    NearCategory[p] = NearCategory[p-1];
                                    NearLabel[p] = NearLabel[p-1];
                                    NearComment[p] = NearComment[p-1];
                                }
                                NearDataKey[temp] = vi.Datakey;
                                NearDist[temp] = dist;
                                NearTime[temp] = vi.time.get(l);
                                NearComment[temp] = vi.Comment;
                                NearTree[temp] = vi.parseTree;
                                NearCategory[temp] = vi.category;
                                NearLabel[temp] = vi.label;
                            }
                        }
                    }
                    
                    
                }
                int p;
                for (p=0;p<3;p++){
                    if(predictedComment.contains(NearComment[p])== false){
                        predictedComment.add(NearComment[p]);
                        predictedlabel.add(NearLabel[p]);
                        predictedCategory.add(NearCategory[p]);
                        predictionTime.add((double)i);

                        if (NearTree[p].contains("\"")==false){
                            predictionTree.add(NearTree[p]);
                        }
                    }
                }
                for(p=0;p<KNN_LEVEL;p++){
                    NearDist[p]=Double.MAX_VALUE;
                }
                
            }
            FileWriter fw = new FileWriter("comment_generation\\extracted_comments\\"+args[0]+"_generatedCommentsTree.txt");
            BufferedWriter bw = new BufferedWriter(fw);
            for(i=0;i<predictionTree.size();i++){
                System.out.println(predictionTime.get(i)/100+";"+predictedlabel.get(i) +";"+predictedCategory.get(i)+";"+predictedComment.get(i));
                bw.append(predictionTree.get(i)+"\n");
            }
            bw.close();
            Thread.sleep(3000);

            //PythonInterpreter interpreter = new PythonInterpreter(); 
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        } 
        
    }
}