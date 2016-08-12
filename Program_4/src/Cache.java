import java.util.*;

public class Cache {


    private int numberOfCacheBlocks;
    private int cacheBlockSize;
    private Entry[] pageTable = new Entry[numberOfCacheBlocks];
    private int hand = 0;
    private int pageTableLength = pageTable.length;

    public Cache(int blockSize, int cacheBlocks) {

        numberOfCacheBlocks = cacheBlocks;
        cacheBlockSize = blockSize;
    }

    private class Entry {

        int blockNumber = -1;
        byte referenceBit = 0;
        byte dirtyBit = 0;
    }


    private int findFreePage() {
        int freePage = -1;
        for (int i = 0; i < pageTableLength; ++i) {
            // there is a free page
            if(pageTable[hand].blockNumber == -1){
                freePage = hand;
                //advance hand
                hand = (hand + 1) % pageTableLength;
                return freePage;
            }
        }

        /*
        // there are no free pages
        for (int i = 0; i < pageTableLength; ++i){

           // reference bit = 0; use this block
            if(pageTable[hand].referenceBit == 0){
                freePage = hand;
                //advance hand
                hand = (hand + 1) % pageTableLength;
                return freePage;
            }

            // reference bit = 1; set it to 0; advance the hand
            if(pageTable[hand].referenceBit == 1){
                pageTable[hand].referenceBit = 0;
                //advance hand
                hand = (hand + 1) % pageTableLength;
            }
        }

        */

        return -1;                                     // no free pages
    }

    private int nextVictim() {
        int nextVictim = -1;

        // (0,0) best page to replace
        for (int i = 0; i < pageTableLength; ++i)
        {
            if(pageTable[hand].referenceBit == 0 && pageTable[hand].dirtyBit == 0){
                nextVictim = hand;
                //advance hand
                hand = (hand + 1) % pageTableLength;
                return nextVictim;

            }
        }

        // (0, 1) not as quite good; need to write the page out before replacement
        for (int i = 0; i < pageTableLength; ++i)
        {
            if(pageTable[hand].referenceBit == 0 && pageTable[hand].dirtyBit == 1){
                nextVictim = hand;
                //advance hand
                hand = (hand + 1) % pageTableLength;
                return nextVictim;

            }
        }

        // (1, 0) recently used but clean
        for (int i = 0; i < pageTableLength; ++i)
        {
            if(pageTable[i].referenceBit == 1 && pageTable[i].dirtyBit == 0){
                return i;

            }
        }

        // (1, 1) recently used and modified
        for (int i = 0; i < pageTableLength; ++i)
        {
            if(pageTable[i].referenceBit == 1 && pageTable[i].dirtyBit == 1){
                return i;

            }
        }

        return -1;
    }

    private void writeBack(int victimEntry) {
    }

    public synchronized boolean read(int blockId, byte buffer[]) {
        return false;
    }

    public synchronized boolean write(int blockId, byte buffer[]) {
        return false;
    }

    public synchronized void sync() {
    }

    public synchronized void flush() {
    }
}
