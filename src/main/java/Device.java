public class Device {

	private String mac;
	private String brand;
	private String device;
	private String model;
	private String sdk;
	private int frequency;
	private String name;

	public Device(String mac, String brand, String device, String model, String sdk, int frequency, String name) {
		this.mac = mac;
		this.brand = brand;
		this.device = device;
		this.model = model;
		this.sdk = sdk;
		this.frequency = frequency;
		this.name = name;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getSdk() {
		return sdk;
	}

	public void setSdk(String sdk) {
		this.sdk = sdk;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Device{" +
				"mac='" + mac + '\'' +
				", brand='" + brand + '\'' +
				", device='" + device + '\'' +
				", model='" + model + '\'' +
				", sdk='" + sdk + '\'' +
				", frequency=" + frequency +
				", name='" + name + '\'' +
				'}';
	}

}
