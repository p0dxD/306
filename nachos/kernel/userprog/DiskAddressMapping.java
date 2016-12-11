package nachos.kernel.userprog;

public class DiskAddressMapping {
    int SpaceID;
    int VPN;
    //Creates a new swap mapping object
    //this object contains two fields
    //space id and vpn
    public DiskAddressMapping(int SpaceID, int VPN){
	this.SpaceID = SpaceID;
	this.VPN =VPN;
    }
    //checks if this is the address we want
    public boolean isAddressMapping(int SpaceID, int VPN){
	return ((this.SpaceID==SpaceID)&&(this.VPN==VPN));
    }
}
