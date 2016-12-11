package nachos.kernel.userprog;

public class swapMapEntry {
    
    private swapEntry swap;
    private int sector;
    
    public swapMapEntry(swapEntry swap, int sector) {
	this.swap = swap;
	this.sector = sector;
    }

    public swapEntry getSwap() {
        return swap;
    }

    public void setSwap(swapEntry swap) {
        this.swap = swap;
    }

    public int getSector() {
        return sector;
    }

    public void setSector(int sector) {
        this.sector = sector;
    }
    
}
