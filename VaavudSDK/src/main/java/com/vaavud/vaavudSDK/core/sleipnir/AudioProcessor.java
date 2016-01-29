package com.vaavud.vaavudSDK.core.sleipnir;


import com.vaavud.vaavudSDK.core.sleipnir.listener.TickReceiver;
import com.vaavud.vaavudSDK.core.sleipnir.model.Tick;


public class AudioProcessor {
    private TickReceiver receiver;

    enum TopState {UP, DOWN}

    //Sound processing
    int[] botBuffer = new int[3];
    int bot = 0;
    int botMin = 0;
    int bufferIndex = 0;
    float botMinFiltered = - Short.MAX_VALUE/2;
    TopState state = TopState.UP;
    int time = 0;

    public AudioProcessor(TickReceiver receiver) {
        this.receiver = receiver;
    }

    public void processSamples(long timeStamp, short[] inputBuffer) {
        for (int i = 0; i < inputBuffer.length; i++) {
            bufferIndex = (bufferIndex+1)%3;
            botBuffer[bufferIndex] = inputBuffer[i];
            bot = Math.min(Math.min(botBuffer[0], botBuffer[1]), botBuffer[2]);
            botMin = Math.min(bot, botMin);

            time++;

            if (detectTick()) {
                receiver.newTick(new Tick(timeStamp + i, time));
                botMinFiltered = botMinFiltered*0.7f + botMin*0.3f;
                bot = 0;
                botMin = 0;
                time = 0;
                state = TopState.UP;
            }
        }
    }

    private boolean detectTick() {
        switch (state) {
            case UP:
                if (bot > 0) {
                    state = TopState.DOWN;
                }
                break;
            case DOWN:
                if (bot*10 < botMinFiltered*7) return true;
        }
        return false;
    }
}
