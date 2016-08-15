import java.util.*;

public class Cache {

    private Entry[] pageTable;
    private int hand;
    private int cacheBlockSize;
    private int pageTableLength;
    private Vector<byte[]> cache = null;

    public Cache(int blockSize, int cacheBlocks) {

        hand = 0;
        cacheBlockSize = blockSize;
        pageTable = new Entry[cacheBlocks];         // pageTable has an entry for each cache block

        cache = new Vector<byte[]>();
        //create a cache block
        byte[] block = new byte[blockSize];

        // add a cache block to the cache vector
        for(int i = 0; i < cacheBlocks; i++)
        {
            cache.add(block);
            pageTable[i] = new Entry();
        }
        pageTableLength = pageTable.length;
    }

    private class Entry {

        int blockNumber = -1;
        byte referenceBit = 0;
        byte dirtyBit = 0;

        public Entry(){

        }
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
        for (int i = 0; i < pageTableLength; ++i) {
            if (pageTable[hand].referenceBit == 1 && pageTable[hand].dirtyBit == 0) {
                pageTable[hand].referenceBit = 0;
                //advance hand
                hand = (hand + 1) % pageTableLength;

            }

            if (pageTable[hand].referenceBit == 0 && pageTable[hand].dirtyBit == 0) {
                nextVictim = hand;
                //advance hand
                hand = (hand + 1) % pageTableLength;
                return nextVictim;
            }
        }

        // (1, 1) recently used and modified
        for (int i = 0; i < pageTableLength; ++i) {
            if (pageTable[i].referenceBit == 1 && pageTable[i].dirtyBit == 1) {
                pageTable[hand].referenceBit = 0;
                //advance hand
                hand = (hand + 1) % pageTableLength;

            }

            if (pageTable[hand].referenceBit == 0 && pageTable[hand].dirtyBit == 1) {
                nextVictim = hand;
                //advance hand
                hand = (hand + 1) % pageTableLength;
                return nextVictim;
            }
        }

        return -1;
    }

    private void writeBack(int victimEntry) {

        if(pageTable[victimEntry].blockNumber != -1){
            SysLib.rawwrite(pageTable[victimEntry].blockNumber, cache.elementAt(victimEntry));
            pageTable[victimEntry].dirtyBit = 0;
        }

    }


    public synchronized boolean read(int blockId, byte buffer[]) {

        if(blockId < 0){
            return false;
        }

        //If the corresponding entry is in cache, read the contents from the cache
        for (int i = 0; i < pageTableLength; i++){
            if(pageTable[i].blockNumber == blockId)
            {
                byte [] tempBlock = cache.elementAt(i);
                System.arraycopy(tempBlock, 0, buffer, 0, cacheBlockSize);
                pageTable[i].referenceBit = 1;
                return true;
            }
        }

        // If the corresponding entry is not in cache

        // read the block from the disk into buffer
        SysLib.rawread(blockId, buffer);

        int freePage = findFreePage();
        if(freePage >= 0)
        {
            // add this block in cache
            cache.insertElementAt(buffer, freePage);

            // update pageTable with blockNumber and set reference bit = 1
            pageTable[freePage].blockNumber = blockId;
            pageTable[freePage].referenceBit = 1;
            return true;
        }

        int victim = nextVictim();
        if(victim < 0) {
            return false;
        }

        // if the victim is dirty
        if(pageTable[victim].dirtyBit == 1){
            writeBack(victim);
        }

        // add this block to cache
        cache.insertElementAt(buffer, victim);

        // update pageTable with blockNumber and set reference bit = 1
        pageTable[victim].blockNumber = blockId;
        pageTable[victim].referenceBit = 1;
        return true;
    }


    public synchronized boolean write(int blockId, byte buffer[]) {
        if(blockId < 0){
            return false;
        }

        //If the corresponding entry is in cache, read the contents from the cache
        for (int i = 0; i < pageTableLength; i++){
            if(pageTable[i].blockNumber == blockId)
            {
                cache.insertElementAt(buffer, i);
                pageTable[i].referenceBit = 1;
                pageTable[i].dirtyBit = 1;
                return true;
            }
        }

        int freePage = findFreePage();
        if(freePage >= 0)
        {
            cache.insertElementAt(buffer, freePage);
            pageTable[freePage].blockNumber = blockId;
            pageTable[freePage].referenceBit = 1;
            pageTable[freePage].dirtyBit = 1;
            return true;
        }

        int victim = nextVictim();
        if(victim < 0) {
            return false;
        }

        // if the victim is dirty
        if(pageTable[victim].dirtyBit == 1){
            writeBack(victim);
        }

        cache.insertElementAt(buffer, victim);
        pageTable[victim].blockNumber = blockId;
        pageTable[victim].referenceBit = 1;
        pageTable[victim].dirtyBit = 1;
        return true;


    }

    public synchronized void sync() {
        for(int i = 0; i < pageTableLength; i++){
            if(pageTable[i].dirtyBit == 1){
                writeBack(i);
            }
            SysLib.sync();
        }
    }

    public synchronized void flush() {
        for(int i = 0; i < pageTableLength; i++){
            if(pageTable[i].dirtyBit == 1){
                writeBack(i);
            }
            pageTable[i].referenceBit = 0;
            pageTable[i].blockNumber = -1;
            pageTable[i].dirtyBit = -1;
        }
        SysLib.sync();
    }
}
