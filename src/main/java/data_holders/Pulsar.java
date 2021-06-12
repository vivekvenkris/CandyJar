package data_holders;

public class Pulsar {
	
	private String name;
	private Angle ra;
	private Angle dec;
	private Double f0;
	private Double f1;
	private Double p0;
	private Double p1;
	private Double dm;
	private Boolean binary;
	private Double pb;
	private Double a1;
	private Double ecc;
	private String ephemerides;
	
	public Pulsar(){
		ephemerides = "";
		name = "PSR NULL";
		f0 = 0.0;
		f1 = 0.0;
		p0 = 0.0;
		p1 = 0.0;
		dm = 0.0;
		
	}

	public Angle getRa() {
		return ra;
	}
	public void setRa(Angle ra) {
		this.ra = ra;
	}
	public Angle getDec() {
		return dec;
	}
	public void setDec(Angle dec) {
		this.dec = dec;
	}
	
	public Double getDm() {
		return dm;
	}
	public void setDm(Double dm) {
		this.dm = dm;
	}
	public Boolean getBinary() {
		return binary;
	}
	public void setBinary(Boolean binary) {
		this.binary = binary;
	}
	public Double getPb() {
		return pb;
	}
	public void setPb(Double pb) {
		this.pb = pb;
	}
	public Double getA1() {
		return a1;
	}
	public void setA1(Double a1) {
		this.a1 = a1;
	}
	public Double getEcc() {
		return ecc;
	}
	public void setEcc(Double ecc) {
		this.ecc = ecc;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getP0() {
		return p0;
	}
	public void setP0(Double p0) {
		this.p0 = p0;
		this.f0 = 1/this.p0;
	}
	public Double getP1() {
		return p1;
	}
	public void setP1(Double p1) {
		this.p1 = p1;
		this.f1 = 1/this.p1;
	}
	public Double getF0() {
		return f0;
	}
	public void setF0(Double f0) {
		this.f0 = f0;
		this.p0 = 1/this.f0;
	}
	public Double getF1() {
		return f1;
	}
	public void setF1(Double f1) {
		this.f1 = f1;
		this.p1 = 1/this.f1;
	}
	public String getEphemerides() {
		return ephemerides;
	}

	public void setEphemerides(String ephemerides) {
		this.ephemerides = ephemerides;
	}

	public void addToEphemerides( String str){
		this.ephemerides+=(str+"\n");
	}
	
	@Override
	public String toString() {
		
		return this.getName() + " " + this.ra + " "+ this.dec +"\n";
	}
	

}
