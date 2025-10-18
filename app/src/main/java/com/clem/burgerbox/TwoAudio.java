package com.clem.burgerbox;

import java.util.ArrayList;

public class TwoAudio{

    Audio audio1;
    Audio audio2;
    public TwoAudio(String text1, String text2){
        this.audio1=new Audio(text1);
        this.audio2=new Audio(text2);
    }

    public TwoAudio(Audio audio1, Audio audio2){
        this.audio1=audio1;
        this.audio2=audio2;
    }

    public Audio getAudio1() {
        return audio1;
    }

    public Audio getAudio2() {
        return audio2;
    }

    public static ArrayList<TwoAudio> createTwoAudioArrays(ArrayList<Audio> audio){
        int finalIndex;
        ArrayList<TwoAudio> twoAudioList = new ArrayList<>();
        boolean isOdd;
        if(audio.size()%2==0){
            finalIndex= audio.size();
            isOdd=false;
        }
        else{
            finalIndex= audio.size()-1;
            isOdd=true;
        }
        for(int i=0; i<finalIndex-1; i=i+2){
            twoAudioList.add(
                    new TwoAudio(
                            audio.get(i),
                            audio.get(i+1)
                    )
            );
        }

        if(isOdd){
            twoAudioList.add(
                    new TwoAudio(
                            audio.get(audio.size()-1),
                            null
                    )
            );
        }

        return twoAudioList;
    }
}
