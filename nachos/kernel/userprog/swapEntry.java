package nachos.kernel.userprog;

public class swapEntry {
    
    private int spaceID;
    private int vpn;
    
    swapEntry(int spaceID, int vpn) {
	this.spaceID = spaceID;
	this.vpn = vpn;
    }
    
    public int getSpaceID() {
        return spaceID;
    }
    public void setSpaceID(int spaceID) {
        this.spaceID = spaceID;
    }
    public int getVpn() {
        return vpn;
    }
    public void setVpn(int vpn) {
        this.vpn = vpn;
    }
  

}
